package com.delfin.jarexp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.exception.SourceConnectionException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Cmd.Result;

public class FileUtils {

	private static abstract class PostAminExecution {

		void doWait() {
			long start = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - start > Settings.ADMIN_WAIT_TIMEOUT) {
					throw new JarexpException("Timeout of waiting after run with admin privileges was exceeded.");
				}
				if (accept()) {
					break;
				}
				Utils.sleep(100);
			}
		}

		protected abstract boolean accept();
	}

	private static abstract class Stream {

		private final InputStream is;

		Stream(InputStream is) {
			this.is = is;
		}

		void dumpTo(File dst) throws IOException {
			OutputStream os = null;
			try {
				os = new FileOutputStream(dst);
				byte[] buffer = new byte[BUFFER_SIZE];
				int length;
				while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
					os.write(buffer, 0, length);
				}
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.log(Level.WARNING, "Unable to close input stream for " + pathToSourceForError(), e);
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						log.log(Level.WARNING, "Unable to close output stream for " + dst.getAbsolutePath(), e);
					}
				}
			}
		}

		protected abstract String pathToSourceForError();

	}

	public interface ReadProcessor {
		void process(InputStream stream) throws IOException;
	}

	private static final Logger log = Logger.getLogger(FileUtils.class.getCanonicalName());

	private static final String EOL = Settings.EOL;

	private static final int BUFFER_SIZE = 1024;

	public static void read(File src, ReadProcessor readProcessor) {
		FileInputStream stream = null;
		try {
			readProcessor.process(stream = new FileInputStream(src));
		} catch (Exception e) {
			throw new JarexpException("An error occurred while processing file " + src, e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream to file " + src, e);
				}
			}
		}
	}

	public static boolean isImgFile(String fileName) {
		return fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".jpg")
				|| fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
	}

	public static String toString(File file) {
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new JarexpException("An error occurred while reading file " + file, e);
		}
	}

	public static String getFileName(String path) {
		String fileName = path;
		int i = path.lastIndexOf('/');
		if (i != -1) {
			fileName = path.substring(i + 1);
		}
		return fileName;
	}

	public static String toString(URL url) throws IOException {
		return read(url.openStream());
	}

	public static void toFile(File file, String content) throws IOException {
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(file));
			out.print(content);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void toFile(File file, InputStream stream) throws IOException {
	    OutputStream out = new FileOutputStream(file);
	    try {	    	
	    	byte[] buffer = new byte[8 * 1024];
	    	int read;
	    	while ((read = stream.read(buffer)) != -1) {
	    		out.write(buffer, 0, read);
	    	}
	    	out.flush();
	    } finally {
	    	try {
	    		stream.close();
	    	} catch (IOException e) {
	    		log.log(Level.WARNING, "Unable to close stream while dumping data to file " + file, e);
	    	}
	    	try {
	    		out.close();
	    	} catch (IOException e) {
	    		log.log(Level.WARNING, "Unable to close output stream to file " + file, e);
	    	}
	    }
	}

	public static byte[] toBytes(File file) throws IOException {
		InputStream ios = null;
		try {
			return toBytes(ios = new FileInputStream(file));
		} finally {
			if (ios != null) {
				ios.close();
			}
		}
	}

	public static byte[] toBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] data = new byte[BUFFER_SIZE];
		while ((read = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		return buffer.toByteArray();
	}

	private static String read(InputStream stream) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
			StringBuilder out = new StringBuilder();
			for (String line; (line = reader.readLine()) != null;) {
				out.append(line).append('\n');
			}
			return out.toString();
		} catch (Exception e) {
			throw new JarexpException("An error occurred while reading file", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close reader of file", e);
				}
			}
		}
	}

	public static void delete(File f) {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				try {
					delete(c);
				} catch (Exception e) {
					log.log(Level.SEVERE, "An error occurred while deleting the file " + f.getAbsolutePath(), e);
				}
			}
		}
		if (!f.delete()) {
			log.log(Level.SEVERE, "Could not delete file " + f.getAbsolutePath());
		}
	}

	public static void copy(File src, File dst) {
		if (dst.isDirectory()) {
			dst = new File(dst, src.getName());
		}
		if (src.isDirectory()) {
			dst.mkdirs();
			File[] files = src.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; ++i) {
					copy(files[i], new File(dst, files[i].getName()));
				}
			}
		} else {
			copyFile(src, dst);
		}

	}

	/**
	 * <a href="https://www.journaldev.com/861/java-copy-file">Java copy file</a>
	 */
	private static void copyFile(File src, File dst) {
		(Version.JAVA_MAJOR_VER > 6 ? new Java7FileCopier() : new Java6FileCopier()).copy(src, dst);
	}

	private static void copyWithAdminPrivileges(File src, File dst) throws IOException {
		File bat = File.createTempFile("jarexp-", ".bat");
		bat.deleteOnExit();
		final File copyRes = new File(bat.getParentFile(), "jarexp-copyres");
		copyRes.deleteOnExit();
		try {
			StringBuilder outBat = new StringBuilder();
			outBat.append("@echo off").append(EOL);
			outBat.append("set LOGFILE=%TEMP%\\jarexp-console-adm.txt").append(EOL);
			outBat.append("echo %date% %time% ADMIN COPY. Starting... >> %LOGFILE%").append(EOL);
			outBat.append("set COPYRES=%TEMP%\\jarexp-copyres").append(EOL);
			outBat.append("call copy /B/Y \"").append(src.getAbsolutePath()).append("\" \"")
					.append(dst.getAbsolutePath()).append("\" > %COPYRES%").append(EOL);
			outBat.append("set /p RES=<%COPYRES%").append(EOL);
			outBat.append("echo %date% %time% ADMIN COPY. %RES% >> %LOGFILE%").append(EOL);
			outBat.append("echo %date% %time% ADMIN COPY. Done. >> %LOGFILE%").append(EOL);
			outBat.append("set LOGFILE=").append(EOL);
			outBat.append("set RES=").append(EOL);
			outBat.append("set COPYRES=").append(EOL);
			toFile(bat, outBat.toString());

			if (copyRes.exists()) {
				copyRes.delete();
			}
			runWithAdminPrivileges(bat);
			new PostAminExecution() {
				@Override
				protected boolean accept() {
					return copyRes.exists();
				}
			}.doWait();
			if (!toString(copyRes).contains("1 file(s) copied.")) {
				throw new JarexpException("Unable copy file " + src
						+ " with admin privileges. See the logs for details. Output:\n" + toString(copyRes));
			}
		} finally {
			bat.delete();
			copyRes.delete();
		}
	}

	public static boolean isUnlocked(File file) {
		RandomAccessFile accessFile = null;
		try {
			accessFile = new RandomAccessFile(file, "rw");
			FileChannel channel = accessFile.getChannel();
			FileLock lock = channel.lock();
			try {
				lock = channel.tryLock();
				return false;
			} catch (OverlappingFileLockException e) {
				return true;
			} finally {
				lock.release();
			}
		} catch (Exception e) {
			if (Version.IS_WINDOWS && e instanceof FileNotFoundException
					&& e.getMessage().equals(file.getAbsolutePath() + " (Access is denied)")) {
				try {
					return isUnlockedWithAdminPriviliges(file);
				} catch (Exception ex) {
					throw new JarexpException("An error occurred while checking is file being used " + file, ex);
				}
			}
			throw new JarexpException("An error occurred while checking is file " + file + " locked", e);
		} finally {
			if (accessFile != null) {
				try {
					accessFile.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close random access for file " + file, e);
				}
			}
		}
	}

	private static boolean isUnlockedWithAdminPriviliges(File file) throws IOException {
		File bat = File.createTempFile("jarexp-", ".bat");
		bat.deleteOnExit();
		final File isfilelockedRes = new File(bat.getParentFile(), "jarexp-isfilelocked");
		try {
			StringBuilder outBat = new StringBuilder();
			outBat.append("@echo off").append(EOL);
			outBat.append("set LOGFILE=%TEMP%\\jarexp-console-adm.txt").append(EOL);
			outBat.append("echo %date% %time% ADMIN ISFILELOCKED. Starting... >> %LOGFILE%").append(EOL);
			outBat.append("set ISFILELOCKEDRES=%TEMP%\\jarexp-isfilelocked").append(EOL);
			outBat.append("call ren \"").append(file.getAbsolutePath()).append("\" \"").append(file.getName())
					.append("\" > %ISFILELOCKEDRES% 2>&1").append(EOL);
			outBat.append("echo %date% %time% ADMIN ISFILELOCKED. Done. >> %LOGFILE%").append(EOL);
			outBat.append("set LOGFILE=").append(EOL);
			outBat.append("set ISFILELOCKEDRES=").append(EOL);
			toFile(bat, outBat.toString());

			if (isfilelockedRes.exists()) {
				isfilelockedRes.delete();
			}
			runWithAdminPrivileges(bat);
			new PostAminExecution() {
				@Override
				protected boolean accept() {
					return isfilelockedRes.exists();
				}
			}.doWait();
			return !toString(isfilelockedRes)
					.contains("The process cannot access the file because it is being used by another process.");
		} finally {
			bat.delete();
			isfilelockedRes.delete();
		}
	}

	private static void runWithAdminPrivileges(File bat) {
		File vbs = null;
		try {
			vbs = File.createTempFile("jarexp-", ".vbs");
			vbs.deleteOnExit();
			StringBuilder outVbs = new StringBuilder();
			outVbs.append("Set UAC = CreateObject(\"Shell.Application\")").append(EOL);
			outVbs.append("UAC.ShellExecute \"" + bat.getAbsolutePath() + "\", \"\", \"\", \"runas\", 0").append(EOL);
			toFile(vbs, outVbs.toString());

			Result code = Cmd.run(new String[] { "cscript", vbs.getAbsolutePath(), "//B" }, null);
			if (!code.out.isEmpty() || !code.err.isEmpty()) {
				throw new JarexpException("Unable to run file " + bat
						+ " with admin privileges. See the logs for details. Result code:\n" + code);
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while running ", e);
		} finally {
			if (vbs != null) {
				vbs.delete();
			}
		}
	}

	private interface FileCopier {
		void copy(File src, File dst);
	}

	private static class Java7FileCopier implements FileCopier {
		@Override
		public void copy(File src, File dst) {
			try {
				java.nio.file.Files.copy(src.toPath(), dst.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				if (Version.IS_WINDOWS) {
					if (e instanceof java.nio.file.AccessDeniedException && dst.getAbsolutePath().equals(e.getMessage())) {
						try {
							copyWithAdminPrivileges(src, dst);
							return;
						} catch (Exception ex) {
							throw new JarexpException("An error occurred while copying file " + src + " into " + dst, ex);
						}
					}
				}
				throw new JarexpException("An error occurred while copying file " + src + " into " + dst, e);
			}
		}
	}

	private static class Java6FileCopier implements FileCopier {
		@Override
		public void copy(final File src, File dst) {
			try {
				new Stream(new FileInputStream(src)) {
					@Override
					protected String pathToSourceForError() {
						return "Unable to close input stream for " + src;
					}
				}.dumpTo(dst);
			} catch (Exception e) {
				if (e instanceof java.io.FileNotFoundException && e.getMessage().contains("(Access is denied)")) {
					try {
						copyWithAdminPrivileges(src, dst);
						return;
					} catch (Exception ex) {
						throw new JarexpException("An error occurred while copying file " + src + " into " + dst, ex);
					}
				}
				throw new JarexpException("An error occurred while copying file " + src + " into " + dst, e);
			}
		}
	}

	public static void download(final String from, File dst) {
		try {
			File tmp = File.createTempFile("jarexp", "download", Settings.getJarexpTmpDir());
			new Stream(new BufferedInputStream(new URL(from).openStream())) {
				@Override
				protected String pathToSourceForError() {
					return "Unable to close input stream for " + from;
				}
			}.dumpTo(tmp);
			copy(tmp, dst);
		} catch (Exception e) {
		    String errMsg = "An error occurred while downloading file " + from + " to " + dst;
		    if (e instanceof UnknownHostException && "dst.in.ua".equals(e.getMessage())) {
		        throw new SourceConnectionException(errMsg, e);
		    }
			throw new JarexpException(errMsg, e);
		}
	}

	/**
	 * To add jar into class pass for Java less than 9 we need just add file url
	 * into <code>URLClassLoader</code>. Starting from Java 9 this approach does not
	 * work. Because they did some changes in the class loading. In this case we
	 * need to retrieve <code>URLClassLoader</code> from deep of system class loader
	 * via reflection and final access. To let it do we have to run JVM with
	 * parameters "--add-opens java.base/jdk.internal.loader=ALL-UNNAMED". To run
	 * JVM which supports different JVM's (6,7,8,9 and so on) we need to add
	 * parameter -XX:+IgnoreUnrecognizedVMOptions.
	 */
	public static void addJarToClasspath(File jar) {
		try {
			Object urlClassLoader;
			Method addUrlMethod;
			if (Version.JAVA_MAJOR_VER < 16) {
				if (Version.JAVA_MAJOR_VER < 9) {
					urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
					addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				} else {
					Object systemClassLoader = ClassLoader.getSystemClassLoader();
					Field ucpField = systemClassLoader.getClass().getDeclaredField("ucp");
					FieldHelper.makeNonFinal(ucpField, Version.JAVA_MAJOR_VER);
					ucpField.setAccessible(true);
					urlClassLoader = ucpField.get(systemClassLoader);
					addUrlMethod = urlClassLoader.getClass().getDeclaredMethod("addURL", URL.class);
				}
				addUrlMethod.setAccessible(true);
				addUrlMethod.invoke(urlClassLoader, jar.toURI().toURL());
			} else {
				urlClassLoader = ClassLoader.getSystemClassLoader();
				addUrlMethod = urlClassLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
				addUrlMethod.setAccessible(true);
				addUrlMethod.invoke(urlClassLoader, jar.getAbsolutePath());
			}
		} catch (Exception e) {
			throw new JarexpException("Unable to add jar " + jar + " to classpath", e);
		}
	}

}
