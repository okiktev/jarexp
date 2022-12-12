package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Md5Checksum;
import com.delfin.jarexp.utils.Zip;

public class LibraryManager {
	
	private static final Logger log = Logger.getLogger(LibraryManager.class.getCanonicalName());

	public enum DependencyType {
		RSTA, FERNFLOWER, JD071, JD113, PROCYON, MAVEN
	}

	private static final String LIBRARIES_STORE_URL = 
			Settings.JAREXP_HOST_URL + "/downloads/libraries/";

	private static File libDir;

	private static Executor executor = Executors.newSingleThreadExecutor();

	private static boolean isDisabled;
	static {
		InputStream is = null;
		try {
			is = LibraryManager.class.getClassLoader().getResourceAsStream("dependencies");
			isDisabled = is == null;
		} catch (Throwable t) {
			log.log(Level.WARNING, "Unable open stream to dependencies file.", t);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream to dependencies file.", e);
				}
			}
		}
	}

	public static boolean isDone;
	private static Map<String, Dependency> dependencies;

	public static void prepareLibraries(final StatusBar statusBar) {
		initDefaultDecompiler();
		if (isDisabled) {
			statusBar.setDecompiler(Settings.getDecompilerType());
			return;
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					File libDir = getLibDir();
					if (!libDir.exists()) {
						libDir.mkdirs();
					}
					Dependency rstaJarDep = getDependency(DependencyType.RSTA);
					check(libDir, rstaJarDep, statusBar);
					FileUtils.addJarToClasspath(new File(libDir, rstaJarDep.fileName));

					Dependency decompilerDep = getDefaultDecompilerDependency(statusBar);
					check(libDir, decompilerDep, statusBar);
					FileUtils.addJarToClasspath(new File(libDir, decompilerDep.fileName));

					isDone = true;
				} catch (RuntimeException e) {
					log.log(Level.SEVERE, "An error occurred while preparing libraries.", e);
					throw e;
				} finally {
					statusBar.disableProgress();
				}
			}

			private void check(File libDir, Dependency jarDep, StatusBar statusBar) {
				if (isNeedToDownload(jarDep)) {
					statusBar.enableProgress("Downloading");
					FileUtils.download(LIBRARIES_STORE_URL + jarDep.fileName
							, new File(libDir, jarDep.fileName));
				}
			}

			private boolean isNeedToDownload(Dependency dep) {
				if (isDisabled) {
					return false;
				}
				File jar = new File(getLibDir(), dep.fileName);
				if (!jar.exists()) {
					return true;
				}
				if (dep.md5.equals(Md5Checksum.get(jar))) {
					return false;
				}
				log.info("Removing previous version of lib " + dep.fileName);
				jar.delete();
				return true;
			}
		});
	}

	private static Dependency getDefaultDecompilerDependency(StatusBar statusBar) {
		int ver = Version.JAVA_MAJOR_VER;
		Dependency res;
		if (ver == 6) {
			res = getDependency(DependencyType.JD071);
		} else if (ver == 7) {
			res = getDependency(DependencyType.PROCYON);
		} else if (ver >= 8) {
			res = getDependency(DependencyType.FERNFLOWER);
		} else {
			throw new JarexpException("Unable to select decompiler for java with version " + ver);
		}
		statusBar.setDecompiler(Settings.getDecompilerType());
		return res;
	}

	private static void initDefaultDecompiler() {
		int ver = Version.JAVA_MAJOR_VER;
		if (ver == 6) {
			Settings.setDecompilerType(DecompilerType.JDCORE);
		} else if (ver == 7) {
			Settings.setDecompilerType(DecompilerType.PROCYON);
		} else if (ver >= 8) {
			Settings.setDecompilerType(DecompilerType.FERNFLOWER);
		} else {
			throw new JarexpException("Unable to select decompiler for java with version " + ver);
		}
	}

	public static File getLibDir() {
		if (libDir == null) {
			libDir = new File(Settings.getAppDir(), "lib");
			libDir.mkdirs();
		}
		return libDir;
	}

	public static void prepareBinariesFor(DecompilerType type) throws IOException {
		if (isDisabled) {
			return;
		}
		String fileName = getDependency(type).fileName;
		try {
			File lib = getLibDir();
			if (!lib.exists()) {
				lib.mkdirs();
			}
			File jar = new File(lib, fileName);
			if (!jar.exists() || isNotUptodated(jar, type)) {
				FileUtils.download(LIBRARIES_STORE_URL + fileName, jar);
			}
			if (Decompiler.isNotLoaded(type)) {
				FileUtils.addJarToClasspath(jar);
			}
		} catch (Exception e) {
			throw new IOException("Unable to preapare binaries for " + type + " decompiler", e);
		}
	}

	private static boolean isNotUptodated(File jar, DecompilerType type) {
		Dependency dep = getDependency(type);
		if (dep.md5.equals(Md5Checksum.get(jar))) {
			return false;
		}
		log.info("Removing previous version of lib " + dep.fileName);
		jar.delete();
		return true;
	}

	private static boolean isNotUptodated(File zip, DependencyType type) {
		Dependency dep = getDependency(type);
		if (dep.md5.equals(Md5Checksum.get(zip))) {
			return false;
		}
		log.info("Removing previous version of lib " + dep.fileName);
		zip.delete();
		return true;
	}

	private static Dependency getDependency(DecompilerType type) {
		switch (type) {
		case PROCYON:
			return getDependency(DependencyType.PROCYON);
		case FERNFLOWER:
			return getDependency(DependencyType.FERNFLOWER);
		case JDCORE:
			return getDependency(getJavaDecompilerType());
		default:
			throw new JarexpException("Unable to identify type of decompiler " + type);
		}
	}

	public static Dependency getDependency(DependencyType type) {
		if (dependencies == null) {
			loadDependencies();
		}
		switch (type) {
		case RSTA:
			return dependencies.get("rsta");
		case FERNFLOWER:
			return dependencies.get("fernflower");
		case JD071:
			return dependencies.get("java.decompiler.071");
		case JD113:
			return dependencies.get("java.decompiler.113");
		case PROCYON:
			return dependencies.get("procyon");
		case MAVEN:
			return dependencies.get("maven");
		default:
			throw new JarexpException("Unknown dependency type " + type);
		}
	}

	private static DependencyType getJavaDecompilerType() {
		return Version.JAVA_MAJOR_VER >= 8 ? DependencyType.JD113 : DependencyType.JD071;
	}

	private static synchronized void loadDependencies() {
		if (dependencies != null) {
			return;
		}
		InputStream is = LibraryManager.class.getClassLoader()
				.getResourceAsStream("dependencies");
		Properties depProps = new Properties();
		try {
			depProps.load(is);
		} catch (IOException e) {
			throw new JarexpException("Unable to load dependencies", e);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				log.log(Level.WARNING, "Unable to close stream to dependencies file.", ex);
			}
		}
		Map<String, Dependency> res = new HashMap<String, Dependency>();
		for (Entry<Object, Object> e : depProps.entrySet()) {
			res.put((String)e.getKey(), new Dependency((String) e.getValue()));
		}
		dependencies = res;
	}

	public static void prepareMaven() {
		File mvnDir = new File(Settings.getAppDir(), "maven");
		if (isDisabled) {
			if (!mvnDir.exists()) {
				mvnDir.mkdirs();
				InputStream is = LibraryManager.class.getClassLoader()
						.getResourceAsStream("apache-maven.zip");
				try {
					Zip.unzip(is, mvnDir);
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						log.log(Level.WARNING, "Unable to close stream to maven archive.", e);
					}
				}
			}
			return;
		}
		File lib = getLibDir();
		if (!lib.exists()) {
			lib.mkdirs();
		}
		if (!mvnDir.exists()) {
			mvnDir.mkdirs();
		}
		String fileName = getDependency(DependencyType.MAVEN).fileName;
		File zip = new File(lib, fileName);
		if (!zip.exists() || isNotUptodated(zip, DependencyType.MAVEN)) {
			FileUtils.download(LIBRARIES_STORE_URL + fileName, zip);
			FileUtils.delete(mvnDir);
			Zip.unzip(zip, mvnDir);
		}
	}

	public static class Dependency {
		public final String fileName;
		public final String md5;
		private Dependency(String record) {
			String[] tokens = record.split(";");
			this.fileName = tokens[0];
			this.md5 = tokens[1];
		}
	}

}
