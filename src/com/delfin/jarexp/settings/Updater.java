package com.delfin.jarexp.settings;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;

import com.delfin.jarexp.ActionHistory;
import com.delfin.jarexp.exception.JarexpException;

public class Updater {

	private static final Logger log = Logger.getLogger(Updater.class.getName());

	private static final long ONE_DAY = 86400000L;

	private static final int CONN_TIMEOUT = 30000;

	private static final String VERSION_URL = Settings.JAREXP_HOST_URL + "/version";

	private static final String DONATE_URL = Settings.JAREXP_HOST_URL + "/config/donate";

	private static Timer checker;

	public Updater(final JMenu update, final JMenu donate) {
		try {
			checker = new Timer("UpdateChecker");
			checker.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					final String donateUrl = getDonateUrl();
					if (donateUrl != null) {
						donate.setVisible(true);
						donate.addMouseListener(new MouseListener() {
							@Override
							public void mouseReleased(MouseEvent e) {
							}
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
							@Override
							public void mouseExited(MouseEvent e) {
							}
							@Override
							public void mouseEntered(MouseEvent e) {
							}
							@Override
							public void mouseClicked(MouseEvent e) {
							}
						});
					}

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
					update.setVisible(true);
					update.setToolTipText(
							"<html>\"Jar Explorer\" update<br/>v<b>" + newVersion + "</b> is available</html>");
					update.addMouseListener(new MouseListener() {
						@Override
						public void mouseReleased(MouseEvent e) {
						}
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
						@Override
						public void mouseExited(MouseEvent e) {
						}
						@Override
						public void mouseEntered(MouseEvent e) {
						}
						@Override
						public void mouseClicked(MouseEvent e) {
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
			URLConnection connection = new URL(url).openConnection();
			connection.setReadTimeout(CONN_TIMEOUT);
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

}
