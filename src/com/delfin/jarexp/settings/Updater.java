package com.delfin.jarexp.settings;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.utils.FileUtils;

public class Updater {

	private static final Logger log = Logger.getLogger(Updater.class.getName());

	private static final long ONE_DAY = 86400000L;

	private static final int CONN_TIMEOUT = 30000;

	private static final String VERSION_URL = Settings.JAREXP_HOST_URL + "/version";

	private static final String DONATE_URL = Settings.JAREXP_HOST_URL + "/config/donate";

	private static final Pattern JAREXP_TEMP_DIR_PTRN = Pattern.compile("^jarexp[0-9]+$");

	private static Timer checker;

	public Updater(final JMenu update, final JMenu donate) {
		HttpURLConnection.setFollowRedirects(false);
		try {
			checker = new Timer("UpdateChecker");
			checker.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					checkDonate(donate);
					checkUpdate(update);
					checkTemp();
				}

				private void checkTemp() {
					boolean isSingletone = false;
					for (Window window : JDialog.getWindows()) {
						if (window instanceof JFrame && ((JFrame) window).getTitle().startsWith("Jar Explorer")) {
							if (isSingletone) {
								isSingletone = false;
								break;
							}
							isSingletone = true;
						}
					}
					if (!isSingletone) {
						return;
					}
					final File jarexpTempDir = Settings.getJarexpTmpDir();
					for (File f : jarexpTempDir.getParentFile().listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if (name.startsWith("jarexp") && JAREXP_TEMP_DIR_PTRN.matcher(name).find()) {
								File tmp = new File(dir, name);
								return tmp.isDirectory() ? !tmp.equals(jarexpTempDir) : false;
							}
							return false;
						}
					})) {
						FileUtils.delete(f);
					};
				}

				private void checkUpdate(JMenu update) {
					Date now = new Date();
					String newVersion = null;
					if (ActionHistory.getLastUpdateCheckDate().getTime() + ONE_DAY < now.getTime()) {
						newVersion = checkVersion();
						ActionHistory.loadLastUpdateCheck(now);
						ActionHistory.loadNewVersion(newVersion);
					} else {
						newVersion = ActionHistory.getNewVersion();
					}
					if (newVersion == null || newVersion.isEmpty()) {
						return;
					}
					if (newVersion.equals(Version.get())) {
						return;
					}
					if (isNewVersionDeployed(newVersion)) {
						ActionHistory.loadNewVersion(newVersion);
						update.setVisible(true);
						update.setToolTipText(
								"<html>\"Jar Explorer\" update<br/>v<b>" + newVersion + "</b> is available</html>");
						update.addMouseListener(new MousePressedListener() {
							@Override
							public void mousePressed(MouseEvent event) {
								if (Desktop.isDesktopSupported()) {
									try {
										Desktop.getDesktop().browse(new URI(Settings.JAREXP_HOST_URL + "?l=en"));
									} catch (Exception e) {
										throw new JarexpException("Could not redirect to jar explorer site", e);
									}
								}
							}
						});
					}
				}

				private boolean isNewVersionDeployed(String newVersion) {
					String currentVersion = Version.get();
					try {
						String[] nvTuple = newVersion.split("\\.");
						String[] cvTuple = currentVersion.split("\\.");
						for (int i = 0; i < nvTuple.length; ++i) {
							Integer n = Integer.valueOf(nvTuple[i]);
							Integer c = Integer.valueOf(cvTuple[i]);
							if (n > c) {
								return true;
							}
						}
					} catch (Exception e) {
						log.log(Level.SEVERE, "Unable to compare versions. current " + currentVersion + " new " + newVersion, e);
					}
					return false;
				}

				private void checkDonate(JMenu donate) {
					if (ActionHistory.getLastUpdateCheckDate().getTime() + ONE_DAY < new Date().getTime()) {						
						ActionHistory.loadDonateUrl(getDonateUrl());
					}
					final String donateUrl = ActionHistory.getDonateUrl();
					if (donateUrl == null) {
						return;
					}
					donate.setVisible(true);
					donate.addMouseListener(new MousePressedListener() {
						@Override
						public void mousePressed(MouseEvent event) {
							if (Desktop.isDesktopSupported()) {
								try {
									Desktop.getDesktop().browse(new URI(donateUrl));
								} catch (Exception e) {
									throw new JarexpException("Could not redirect to donate page", e);
								}
							}
						}
					});
				}
			}, 1000L, ONE_DAY);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to init update checker", e);
		}
	}

	private static String checkVersion() {
		return query(VERSION_URL);
	}

	private static String getDonateUrl() {
		return query(DONATE_URL);
	}

	private static String query(String url) {
		InputStream stream = null;
		InputStreamReader reader = null;
		BufferedReader buff = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setReadTimeout(CONN_TIMEOUT);
			int code = connection.getResponseCode();
			if (code < 200 && code >= 300) {
				if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
					log.warning("Code " + code + ". Location header " + connection.getHeaderField("Location"));
				}
				return null;
			}
			stream = connection.getInputStream();
			reader = new InputStreamReader(stream);
			buff = new BufferedReader(reader);
			int c;
			StringBuilder out = new StringBuilder();
			while ((c = buff.read()) != -1) {
				out.append((char) c);
			}
			return out.toString().trim();
		} catch (Exception e) {
			log.log(Level.SEVERE, "An error occurred while checking version", e);
		} finally {
			if (buff != null) {
				try {
					buff.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close buffered reader", e);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream reader", e);
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream", e);
				}
			}
		}
		return null;
	}

	private static abstract class MousePressedListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

}
