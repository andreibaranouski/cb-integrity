package com.couchbase.integrations.helpers;

import java.io.File;
import java.io.IOException;

import com.couchbase.integrations.shell.RemoteExecutor;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class SGHelper {

	public static void killAllSG(RemoteExecutor re) throws IOException,
			JSchException {

		re.execute("killall sync_gateway");

	}

	public static void runSG(RemoteExecutor re, String sgPath,
			String configFilePath) throws IOException, JSchException,
			SftpException {
		if (configFilePath != null) {
			re.writeFile(configFilePath, "/tmp");
			File f = new File(configFilePath);
			re.executeNoWaiting(sgPath + " /tmp/" + f.getName());
			// re.executeNoWaiting(sgPath + " -verbose /tmp/" + f.getName());
		} else {
			re.executeNoWaiting(sgPath);
		}
		// re.execute("exec > >(tee logfile.txt); exec 2>&1; " + sgPath +
		// " /tmp/"
		// + f.getName() + " 2>&1 < /dev/null & ");
		// re.execute("nohup " + sgPath + " /tmp/" + f.getName() + " &");
		// + ">/tmp/outputFile 2>&1 < /dev/null &");

	}

	public static void getAllDocs(RemoteExecutor re) throws IOException,
			JSchException {

	}
}
