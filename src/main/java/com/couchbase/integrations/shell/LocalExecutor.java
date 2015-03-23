package com.couchbase.integrations.shell;

import java.io.IOException;

public class LocalExecutor {

	public Process runCommand(String[] params) {

		// http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki

		ProcessBuilder builder = new ProcessBuilder().inheritIO().command(
				params);
		builder.redirectErrorStream(true);
		Process process = null;
		try {
			process = builder.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
		// check that process.exitValue()
		return process;
	}

}
