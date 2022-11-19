package com.delfin.jarexp.settings;

public class Version {

	private static String ver = "@VERSION@";

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
	public static final String OS_VER = System.getProperty("os.version");

	public static final int JAVA_MAJOR_VER;
	static {
		String javaVersion = System.getProperty("java.version", "1.8.0_162");
		if (javaVersion.startsWith("1.")) {
			int j = javaVersion.lastIndexOf('.');
			JAVA_MAJOR_VER = Integer.parseInt(javaVersion.substring(2, j));
		} else if (javaVersion.indexOf('.') == -1) {
			JAVA_MAJOR_VER = Integer.parseInt(javaVersion);
		} else {
			int j = javaVersion.indexOf('.');
			JAVA_MAJOR_VER = Integer.parseInt(javaVersion.substring(0, j));
		}
	}

	static String get() {
		return ver;
	}

}
