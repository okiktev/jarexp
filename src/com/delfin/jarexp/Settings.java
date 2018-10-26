package com.delfin.jarexp;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.frame.resources.CropIconsBugResolver;

public class Settings {

	private static final Logger log = Logger.getLogger(Settings.class.getCanonicalName());

	public final static String NAME_PLACEHOLDER = ".$placehoder|\\";

	private static Settings instance;

	private String version = Version.get();

	private static File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jarexp" + System.currentTimeMillis());

	public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
	
	public static final Font DLG_TEXT_FONT = new Font("Consolas", Font.PLAIN, 10);

	public final static Dimension DLG_DIM = new Dimension(360, 290);

	public static final long ADMIN_WAIT_TIMEOUT = Long.getLong("jarexp.admin.wait.timeout", 10000L);

	public static final String EOL = System.getProperty("line.separator", "\r\n");

	private static DecompilerType decompilerType = DecompilerType.JDCORE;

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

	static void initLookAndFeel() {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
			CropIconsBugResolver.getInstance().fixOptionIcons();
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to set " + sysLookFeel + " as current. Using: " + UIManager.getLookAndFeel().getName(), e);
		}
	}

	public static DecompilerType getDecompilerType() {
		return decompilerType;
	}

	public static void setDecompilerType(DecompilerType type) {
		decompilerType = type;
	}

}
