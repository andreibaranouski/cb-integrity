package com.couchbase.integrations.objects;

public class Server {

	private String id_ini = null;
	private String ip = "";
	private String user = "";
	private String password = "";
	private String userSSH = "";
	private String passwordSSH = "";
	private String sshKey = null;
	private String cbPort = "8091";
	private String sgPort = "4984";
	private String sgAdminPort = "4985";
	private boolean isCBNode = false;
	private boolean isSG = false;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserSSH() {
		return userSSH;
	}

	public void setUserSSH(String userSSH) {
		this.userSSH = userSSH;
	}

	public String getPasswordSSH() {
		return passwordSSH;
	}

	public void setPasswordSSH(String passwordSSH) {
		this.passwordSSH = passwordSSH;
	}

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}

	public String getCbPort() {
		return cbPort;
	}

	public void setCbPort(String cbPort) {
		this.cbPort = cbPort;
	}

	public String getSgPort() {
		return sgPort;
	}

	public void setSgPort(String sgPort) {
		this.sgPort = sgPort;
	}

	public String getSgAdminPort() {
		return sgAdminPort;
	}

	public void setSgAdminPort(String sgAdminPort) {
		this.sgAdminPort = sgAdminPort;
	}

	public boolean isCBNode() {
		return isCBNode;
	}

	public void setCBNode(boolean isCBNode) {
		this.isCBNode = isCBNode;
	}

	public boolean isSG() {
		return isSG;
	}

	public void setSG(boolean isSG) {
		this.isSG = isSG;
	}

	public String getId_ini() {
		return id_ini;
	}

	public void setId_ini(String id_ini) {
		this.id_ini = id_ini;
	}

	public String buildCbUrl() {
		return "http://" + ip + ":" + cbPort + "/";
	}

	public String buildSgUrl() {
		return "http://" + ip + ":" + sgPort + "/";
	}

	public String buildSgAdminUrl() {
		return "http://" + ip + ":" + sgAdminPort + "/";
	}

}
