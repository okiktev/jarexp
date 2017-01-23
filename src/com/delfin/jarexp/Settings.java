package com.delfin.jarexp;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class Settings {

	public final static String NAME_PLACEHOLDER = ".$placehoder|\\";

	private static Settings instance;

	private String version = Version.get();

	private static File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jarexp");

	public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();

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
		return 600;
	}

}
