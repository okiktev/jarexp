package com.delfin.jarexp;

import java.io.File;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class Settings {

	private static final Logger log = Logger.getLogger(Settings.class.getCanonicalName());

	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static final boolean IS_WINDOWS = OS.indexOf("win") >= 0;

	public static final boolean IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;

	public static final boolean IS_SOLARIS = OS.indexOf("sunos") >= 0;

	public static final boolean IS_MAC = OS.indexOf("mac") >= 0;
	
	/**
	 * <a href="https://en.wikipedia.org/wiki/List_of_Microsoft_Windows_versions">Versions</a>
	 * <table border=1>
	 * <tr><td>Windows version</td><td>Value</td></tr>
	 * <tr><td>Windows 10</td><td>10.0</td></tr>
	 * <tr><td>Windows Server 2016</td><td>10.0</td></tr>
	 * <tr><td>Windows 8.1</td><td>6.3</td></tr>
	 * <tr><td>Windows 8</td><td>6.2</td></tr>
	 * <tr><td>Windows Server 2012</td><td>6.2</td></tr>
	 * <tr><td>Windows 7</td><td>6.1</td></tr>
	 * <tr><td>Windows Server 2008 R2</td><td>6.1</td></tr>
	 * <tr><td>Windows Server 2008</td><td>6.0</td></tr>
	 * <tr><td>Windows Vista</td><td>6.0</td></tr>
	 * <tr><td>Windows Server 2003 R2</td><td>5.2</td></tr>
	 * <tr><td>Windows Server 2003</td><td>5.2</td></tr>
	 * <tr><td>Windows XP 64-Bit Edition</td><td>5.2</td></tr>
	 * <tr><td>Windows XP</td><td>5.1</td></tr>
	 * <tr><td>Windows 2000</td><td>5.0</td></tr>
	 * </table>
	 */
	public static final BigDecimal OS_VER = BigDecimal.valueOf(Double.parseDouble(System.getProperty("os.version")));

	public final static String NAME_PLACEHOLDER = ".$placehoder|\\";

	private static Settings instance;

	private String version = Version.get();

	private static File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jarexp" + System.currentTimeMillis());

	public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();

	public static final long ADMIN_WAIT_TIMEOUT = Long.getLong("jarexp.admin.wait.timeout", 10000L);

	public static final String EOL = System.getProperty("line.separator", "\r\n");

	public static final int JAVA_MAJOR_VER;
	static {
		String javaVersion = System.getProperty("java.version", "1.8.0_162");
		int i, j;
		if (javaVersion.startsWith("1.")) {
			i = 2;
			j = javaVersion.lastIndexOf('.');
		} else {
			i = 0;
			j = javaVersion.indexOf('.');
		}
		JAVA_MAJOR_VER = Integer.parseInt(javaVersion.substring(i, j));
	}

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
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to set " + sysLookFeel + " as current. Using: " + UIManager.getLookAndFeel().getName(), e);
		}
	}

}
