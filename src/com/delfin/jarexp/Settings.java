package com.delfin.jarexp;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class Settings {

	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static final boolean IS_WINDOWS = OS.indexOf("win") >= 0;

	public static final boolean IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;

	public static final boolean IS_SOLARIS = OS.indexOf("sunos") >= 0;

	public static final boolean IS_MAC = OS.indexOf("mac") >= 0;

	public final static String NAME_PLACEHOLDER = ".$placehoder|\\";

	private static Settings instance;

	private String version = Version.get();

	private static File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jarexp" + System.currentTimeMillis());

	public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();

	public static final long ADMIN_WAIT_TIMEOUT = Long.getLong("jarexp.admin.wait.timeout", 10000L);

	private Settings() {

	}

	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	public String getVersion() {
		return version;
	}

	public static File getTmpDir() {
		return tmpDir;
	}

	public int getFrameWidth() {
		return 800;
	}

}
