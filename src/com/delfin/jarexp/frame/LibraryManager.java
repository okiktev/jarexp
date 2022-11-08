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

import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Md5Checksum;

public class LibraryManager {
	
	private static final Logger log = Logger.getLogger(LibraryManager.class.getCanonicalName());

	public enum DependencyType {
		RSTA, FERNFLOWER, JD071, PROCYON
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
		if (isDisabled) {
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

					Dependency jdecJarDep = getDependency(DependencyType.JD071);
					check(libDir, jdecJarDep, statusBar);
					FileUtils.addJarToClasspath(new File(libDir, jdecJarDep.fileName));

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
		});
	}

	public static boolean isNeedToDownload(Dependency dep) {
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
		String fileName;
		switch (type) {
		case PROCYON:
			fileName = getDependency(DependencyType.PROCYON).fileName;
			break;
		case FERNFLOWER:
			fileName = getDependency(DependencyType.FERNFLOWER).fileName;
			break;
		default:
			throw new IOException("Unable to identify type of decompiler " + type);
		}
		try {
			File lib = getLibDir();
			if (!lib.exists()) {
				lib.mkdirs();
			}
			File jar = new File(lib, fileName);
			if (!jar.exists()) {
				FileUtils.download(LIBRARIES_STORE_URL + fileName, jar);
				FileUtils.addJarToClasspath(jar);
			}
		} catch (Exception e) {
			throw new IOException("Unable to preapare binaries for " + type + " decompiler", e);
		}
	}

	public static Dependency getDependency(DecompilerType type) {
		switch (type) {
		case FERNFLOWER:
			return getDependency(DependencyType.FERNFLOWER);
		case JDCORE:
			return getDependency(DependencyType.JD071);
		case PROCYON:
			return getDependency(DependencyType.PROCYON);
		default:
			throw new JarexpException("Unknown decompiler type " + type);
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
		case PROCYON:
			return dependencies.get("procyon");
		default:
			throw new JarexpException("Unknown dependency type " + type);
		}
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
