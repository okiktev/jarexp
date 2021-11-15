package com.delfin.jarexp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpException;

public class Cmd {

	private static final Logger log = Logger.getLogger(Cmd.class.getCanonicalName());

	public static class Result {

		public int code;
		public String out;
		public String err;

		Result(int code, ConsoleReader stdout, ConsoleReader stderr) {
			this.code = code;
			this.out = stdout.toString();
			this.err = stderr.toString();
		}

		Result(int code, String stdout, String stderr) {
			this.code = code;
			this.out = stdout;
			this.err = stderr;
		}

		@Override
		public String toString() {
			StringBuilder res = new StringBuilder();
			res.append("code:[" + code).append("]\n");
			res.append("stdout:[" + out).append("]\n");
			res.append("stderr:[" + err).append("]\n");
			return res.toString();
		}
	}

	private static class ConsoleReader {

		private final BufferedReader reader;
		private final StringBuilder out = new StringBuilder();

		public ConsoleReader(InputStream in) {
			reader = new BufferedReader(new InputStreamReader(in));
		}

		public void start() {
			Thread thread = new Thread(new Runnable() {

				public void run() {
					try {
						for (String line; (line = reader.readLine()) != null;) {
							out.append(line).append('\n');
						}
					} catch (IOException e) {
						out.append("\nAn Exception occurred: ").append(e);
					}
				}

			});
			thread.setDaemon(true);
			thread.setName("Console reader");
			thread.start();
		}

		public String toString() {
			return out.toString();
		}

	}

	public static Result runWithJava6(File workingDir, String...command) {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		if (workingDir != null) {
			processBuilder.directory(workingDir);
		}
		try {
			return handle(processBuilder.start());
		} catch (Exception e) {
			throw new JarexpException("An exception occurred while running " + command, e);
		}
	}

	public static Result run(String[] command, File workingDir, String... envp) {
		Runtime runtime = Runtime.getRuntime();
		try {
			return handle(runtime.exec(command, envp, workingDir));
		} catch (Exception e) {
			throw new JarexpException("An exception occurred while running " + command, e);
		}
	}

	private static Result handle(Process process) throws InterruptedException {
		InputStream out = null;
		InputStream err = null;
		try {
			ConsoleReader stdout = new ConsoleReader(out = process.getInputStream());
			ConsoleReader stderr = new ConsoleReader(err = process.getErrorStream());

			stdout.start();
			stderr.start();

			return new Result(process.waitFor(), stdout, stderr);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close output stream", e);
				}
			}
			if (err != null) {
				try {
					err.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close error stream", e);
				}
			}
		}
	}

}
