package com.delfin.jarexp.settings;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.resources.CropIconsBugResolver;

public class Settings {

	private static final Logger log = Logger.getLogger(Settings.class.getName());

	private static final long AUTO_SAVE_DELAY = 30000L;
	private static File settingsFile;
	static {
		try {
			final File settingsFile = getSettingsFile();
			new Timer("SettingsDumper").scheduleAtFixedRate(new TimerTask() {
				public void run() {
					Properties settings = new Properties();
					settings.put("x", String.valueOf(X));
					settings.put("y", String.valueOf(Y));
					settings.put("search.history", ActionHistory.getSearchHistory());
					String newVersion = ActionHistory.getNewVersion();
					if (newVersion != null) {
						settings.put("new.version", newVersion);
					}
					settings.put("donate.url", ActionHistory.getDonateUrl());
					settings.put("last.update.check", ActionHistory.getLastUpdateCheck());
					settings.put("maven.repositories", ActionHistory.getMavenRepositories(String.class));
					try {
						OutputStream output = new FileOutputStream(settingsFile);
						settings.store(output, null);
						output.close();
					} catch (Exception e) {
						log.log(Level.SEVERE, "Unable to save settings into " + settingsFile, e);
					}
				}
			}, AUTO_SAVE_DELAY, AUTO_SAVE_DELAY);

			Properties settings = new Properties();
			InputStream input = new FileInputStream(settingsFile);
			settings.load(input);
			input.close();
			String x = (String)settings.get("x");
			X = Integer.valueOf(x == null ? "0" : x);
			String y = (String)settings.get("y");
			Y = Integer.valueOf(y == null ? "0" : y);
			ActionHistory.loadSearchHistory((String)settings.get("search.history"));
			ActionHistory.loadNewVersion((String)settings.get("new.version"));
			ActionHistory.loadDonateUrl((String)settings.get("donate.url"));
			ActionHistory.loadLastUpdateCheck((String)settings.get("last.update.check"));
			ActionHistory.loadMavenRepositories((String)settings.get("maven.repositories"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to init settings dumper from " + settingsFile, e);
		}
	}

	public static final String JAREXP_HOST_URL = "https://dst.in.ua/jarexp";

	public final static String NAME_PLACEHOLDER = ".$placehoder|\\";

	public static int X;

	public static int Y;

	private static Settings instance;

	private String version = Version.get();

	private static File tmpDir;
	private static File tmpJarexpDir;
	private static File appDir;

	public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();

	public static final Font DLG_TEXT_FONT = new Font("Consolas", Font.PLAIN, 10);

	public final static Dimension DLG_DIM = new Dimension(360, 290);

	public static final long ADMIN_WAIT_TIMEOUT = Long.getLong("jarexp.admin.wait.timeout", 10000L);

	public static final String EOL = System.getProperty("line.separator", "\r\n");

	public static final int HISTORY_BUFFER_SIZE = 30;

	public static final String LOCKER_FILE_NAME = "jefcbd";

    public static final int SEARCH_PROGRESS_THREADS_NUMBER = 5;

	private static File HOME_DIR;

	private static DecompilerType decompilerType = DecompilerType.JDCORE;

	private File executiveJarFile;

	private Settings() {

	}

	private static File getSettingsFile() throws IOException {
		if (settingsFile == null) {
			settingsFile = new File(getAppDir(), "settings.properties");
			settingsFile.createNewFile();
		}
		return settingsFile;
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

	public static File getJarexpTmpDir() {
		if (tmpJarexpDir == null) {
			tmpJarexpDir = new File(getAppDir(), "" + System.currentTimeMillis());
			tmpJarexpDir.mkdirs();
		}
		return tmpJarexpDir;
	}

	public static File getAppDir() {
		if (appDir == null) {
			appDir = new File(getTmpDir(), "jarexp");
			appDir.mkdirs();
		}
		return appDir;
	}

	private static File getTmpDir() {
		if (tmpDir == null) {
			tmpDir = new File(System.getProperty("java.io.tmpdir"));
		}
		return tmpDir;
	}

	public int getFrameWidth() {
		return 800;
	}

	public static void initLookAndFeel() {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
			CropIconsBugResolver.getInstance().fixOptionIcons();
		} catch (Exception e) {
			log.log(Level.WARNING,
					"Unable to set " + sysLookFeel + " as current. Using: " + UIManager.getLookAndFeel().getName(), e);
		}
	}

	public static DecompilerType getDecompilerType() {
		return decompilerType;
	}

	public static void setDecompilerType(DecompilerType type) {
		decompilerType = type;
	}

	public File getExecutiveJar() {
		if (executiveJarFile == null) {
			Class<?> clazz = this.getClass();
			CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
			try {
				if (codeSource.getLocation() != null) {
					executiveJarFile = new File(codeSource.getLocation().toURI());
				} else {
					String path = clazz.getResource(clazz.getSimpleName() + ".class").getPath();
					String jarFilePath = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
					executiveJarFile = new File(URLDecoder.decode(jarFilePath, "UTF-8"));
				}
			} catch (Exception e) {
				throw new JarexpException("Unable to define path to executive jar.", e);
			}
		}
		return executiveJarFile;
	}

	public static File getUserHome() {
		if (HOME_DIR == null) {
			HOME_DIR = new File(System.getProperty("user.home"));
		}
		return HOME_DIR;
	}

	public int getTabCount() {
		return 16;
	}

}
