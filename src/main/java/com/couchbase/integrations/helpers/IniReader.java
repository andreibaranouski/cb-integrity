package com.couchbase.integrations.helpers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.couchbase.integrations.objects.Server;

public class IniReader {
	public Ini ini;

	public IniReader(String pathToFile) throws InvalidFileFormatException,
			FileNotFoundException, IOException {
		ini = new Ini();
		ini.load(new FileReader(pathToFile));
	}

	public List<Server> getCBServers() {
		List<Server> servers = new ArrayList<Server>();
		Ini.Section commonSection = ini.get("common");

		String user = "Administrator";
		String password = "password";
		String userSSH = "";
		String passwordSSH = "";
		for (String el : commonSection.keySet()) {
			if (el.equals("user")) {
				user = commonSection.get("user");
			} else if (el.equals("password")) {
				password = commonSection.get("password");
			} else if (el.equals("userSSH")) {
				userSSH = commonSection.get("userSSH");
			} else if (el.equals("passwordSSH")) {
				passwordSSH = commonSection.get("passwordSSH");
			} else {
				Assert.fail("Option " + el + " in ini file not specified");
			}
		}

		Ini.Section serversSection = ini.get("cb_pool");
		for (String el : serversSection.keySet()) {
			Server server = new Server();
			server.setId_ini(serversSection.get(el));
			server.setUser(user);
			server.setPassword(password);
			server.setUserSSH(userSSH);
			server.setPasswordSSH(passwordSSH);

			Ini.Section vmSection = ini.get(server.getId_ini());
			for (String vmEl : vmSection.keySet()) {
				if (vmEl.equals("ip")) {
					server.setIp(vmSection.get("ip"));
				} else if (el.equals("user")) {
					server.setUser(user);
				} else if (el.equals("password")) {
					server.setPassword(password);
				} else {
					Assert.fail("Option " + el + " in ini file not specified");
				}
			}
			servers.add(server);
		}
		return servers;
	}

	public List<Server> getSGServers() {
		List<Server> servers = new ArrayList<Server>();

		Ini.Section commonSection = ini.get("common");

		String user = "Administrator";
		String password = "password";
		String userSSH = "";
		String passwordSSH = "";
		for (String el : commonSection.keySet()) {
			if (el.equals("user")) {
				user = commonSection.get("user");
			} else if (el.equals("password")) {
				password = commonSection.get("password");
			} else if (el.equals("userSSH")) {
				userSSH = commonSection.get("userSSH");
			} else if (el.equals("passwordSSH")) {
				passwordSSH = commonSection.get("passwordSSH");
			} else {
				Assert.fail("Option " + el + " in ini file not specified");
			}
		}

		Ini.Section serversSection = ini.get("sg_pool");
		for (String el : serversSection.keySet()) {
			Server server = new Server();
			server.setId_ini(serversSection.get(el));
			server.setUser(user);
			server.setPassword(password);
			server.setUserSSH(userSSH);
			server.setPasswordSSH(passwordSSH);
			Ini.Section vmSection = ini.get(server.getId_ini());
			for (String vmEl : vmSection.keySet()) {
				if (vmEl.equals("ip")) {
					server.setIp(vmSection.get("ip"));
				}

			}
			servers.add(server);
		}
		return servers;
	}

}
