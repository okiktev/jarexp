package com.delfin.jarexp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Version;

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

	public static class EnvironmentVariable {

		String name;
		String value;

		public EnvironmentVariable(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			StringBuilder out = new StringBuilder(name.length() + value.length() + 1);
			return out.append(name).append('=').append(value).toString();
		}
	}

	private static abstract class ConsoleReader {

		private BufferedReader reader;
		protected final StringBuilder out = new StringBuilder();

		public void start(InputStream in) {
			reader = new BufferedReader(new InputStreamReader(in));
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						for (String line; (line = reader.readLine()) != null;) {
							processLine(line);
						}
					} catch (IOException e) {
						out.append("\nAn Exception occurred: ").append(e);
					}
				}
			});
			thread.setDaemon(true);
			thread.setName(getThreadName());
			thread.start();
		}

		protected void processLine(String line) {
			out.append(line).append('\n');
		}

		@Override
		public String toString() {
			return out.toString();
		}

		protected abstract String getThreadName();

	}

	public static class OutputReader extends ConsoleReader {
		@Override
		protected String getThreadName() {
			return "Output console reader";
		}
	}

	public static class ErrorReader extends ConsoleReader {
		@Override
		protected String getThreadName() {
			return "Error console reader";
		}
	}

	public static Result run(String[] command, File workingDir, EnvironmentVariable...envv) {
		return run(command, workingDir, envv, new OutputReader(), new ErrorReader());
	}

	public static Result run(String[] command, File workingDir, EnvironmentVariable[] envv, OutputReader output, ErrorReader error) {
		if (Version.JAVA_MAJOR_VER == 6) {
			return Cmd.runWithJava6(command, workingDir, envv, output, error);
		} else {
			return Cmd.runWithJava7AndHigher(command, workingDir, envv, output, error);
		}
	}

	private static Result runWithJava6(String[] command, File workingDir, EnvironmentVariable[] envv, OutputReader output, ErrorReader error) {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		if (workingDir != null) {
			processBuilder.directory(workingDir);
			if (envv != null) {
				Map<String, String> env = processBuilder.environment();
				for (EnvironmentVariable v : envv) {
					env.put(v.name, v.value);
				}
			}
		}
		try {
			return handle(processBuilder.start(), output, error);
		} catch (Exception e) {
			throw new JarexpException("An exception occurred while running " + command, e);
		}
	}

	private static Result runWithJava7AndHigher(String[] command, File workingDir, EnvironmentVariable[] envv, OutputReader output, ErrorReader error) {
		Runtime runtime = Runtime.getRuntime();
		String[] envp = null;
		if (envv != null) {
			envp = new String[envv.length];
			for (int i = 0; i < envv.length; ++i) {
				envp[i] = envv[i].toString();
			}
		}
		try {
			return handle(runtime.exec(command, envp, workingDir), output, error);
		} catch (Exception e) {
			throw new JarexpException("An exception occurred while running " + command, e);
		}
	}

	private static Result handle(Process process, ConsoleReader stdout, ConsoleReader stderr) throws InterruptedException {
		InputStream out = null;
		InputStream err = null;
		try {
			stdout.start(out = process.getInputStream());
			stderr.start(err = process.getErrorStream());
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
