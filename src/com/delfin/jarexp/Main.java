package com.delfin.jarexp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.delfin.jarexp.frame.Content;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.TempFileCreator;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					LogManager.getLogManager().readConfiguration(Main.class.getClassLoader().getResourceAsStream("logging.properties"));
				} catch (IOException e) {
					System.err.println("Could not setup logger configuration.");
					e.printStackTrace(System.err);
					throw new RuntimeException("Unable to initiate logger", e);
				}
				Settings.initLookAndFeel();
				Zip.setTempFileCreator(new TempFileCreator() {
					@Override
					public File create(String prefix, String suffix) throws IOException {
						return File.createTempFile(prefix, suffix, Settings.getTmpDir());
					}
				});
				try {
					Content.createAndShowGUI(getPassedFile(args));
				} catch (Exception e) {
					log.log(Level.SEVERE, "An error occurred while starting application", e);
					JOptionPane.showMessageDialog(null, "Something happened: " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE, null);
					e.printStackTrace();
				}
			}
		});
	}

	protected static File getPassedFile(String[] args) {
		if (args != null && args.length != 0) {
			File file = new File(args[0]);
			if (!file.exists()) {
				String msg = "Passed file " + file + " is not exist";
				System.err.println(msg);
				log.warning(msg);
			} else {
				return file;
			}
		}
		return null;
	}

}