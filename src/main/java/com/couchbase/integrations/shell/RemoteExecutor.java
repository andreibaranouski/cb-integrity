package com.couchbase.integrations.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.couchbase.integrations.objects.Server;
import com.couchbase.lite.util.Log;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class RemoteExecutor {
	private Session session;
	private String host;
	private String user;
	private String password;

	public RemoteExecutor(Server server) throws JSchException {
		this(server.getIp(), server.getUserSSH(), server.getPasswordSSH());
	}

	public RemoteExecutor(String host, String user, String password)
			throws JSchException {
		this.host = host;
		this.user = user;
		this.password = password;
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		this.session = jsch.getSession(this.user, this.host, 22);
		session.setPassword(this.password);
		session.setConfig(config);
		session.connect();
		Log.i(Log.TAG, "Connected to " + host + " via ssh");
	}

	public void execute(String command) throws IOException, JSchException {
		Log.i(Log.TAG, "run on " + this.host + ": " + command);
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err, true);

		InputStream in = channel.getInputStream();
		channel.connect();
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				Log.i(Log.TAG, new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				Log.i(Log.TAG, "exit-status: " + channel.getExitStatus());

				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		((ChannelExec) channel).setErrStream(System.err);// ?
		channel.disconnect();
	}

	public void executeNoWaiting(String command) throws IOException,
			JSchException {
		Log.i(Log.TAG, "run on " + this.host + ": " + command);
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err, true);

		InputStream in = channel.getInputStream();
		channel.connect();
		long time = System.currentTimeMillis();
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				time = System.currentTimeMillis();
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				Log.i(Log.TAG, new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				Log.i(Log.TAG, "exit-status: " + channel.getExitStatus());

				break;
			}
			try {
				if ((time + 2000) < System.currentTimeMillis()) {
					break;
				}
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		((ChannelExec) channel).setErrStream(System.err);// ?
	}

	public void writeFile(String sourceFile, String targetFolder)
			throws JSchException, SftpException, FileNotFoundException {
		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp sftpChannel = (ChannelSftp) channel;
		sftpChannel.cd(targetFolder);
		File f = new File(sourceFile);
		Log.i(Log.TAG, "will create on " + this.host + " file: " + targetFolder
				+ "/" + f.getName());
		sftpChannel.put(new FileInputStream(f), f.getName());

		sftpChannel.exit();
	}

	public void disconnect() {
		session.disconnect();
	}

}
