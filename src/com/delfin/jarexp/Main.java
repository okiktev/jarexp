package com.delfin.jarexp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.delfin.jarexp.frame.Content;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.TempFileCreator;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());

	private static Executor executor = Executors.newSingleThreadExecutor();

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					LogManager.getLogManager().readConfiguration(Main.class.getClassLoader().getResourceAsStream("logging.properties"));
				} catch (Exception e) {
					System.err.println("Could not setup logger configuration.");
					e.printStackTrace(System.err);
					throw new RuntimeException("Unable to initiate logger", e);
				}
				executor.execute(new TempFoldersRemover());
				Settings.initLookAndFeel();
				Zip.setTempFileCreator(new TempFileCreator() {
					@Override
					public File create(String prefix, String suffix) throws IOException {
						return File.createTempFile(prefix, suffix, Settings.getJarexpTmpDir());
					}
				});
				try {
					Content.createAndShowGUI(fileToOpen(args), pathToOpen(args));
				} catch (Exception e) {
					log.log(Level.SEVERE, "An error occurred while starting application", e);
					JOptionPane.showMessageDialog(null, "Something happened: " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE, null);
					e.printStackTrace();
				}
			}
		});
	}

	protected static String pathToOpen(String[] args) {
		if (args == null || args.length < 3) {
			return null;
		}
		String option = args[1];
		if ("-o".equalsIgnoreCase(option)) {
			String path = args[2];
			return path == null || path.isEmpty() ? null : path;
		}
		StringBuilder params = new StringBuilder();
		for (String s : args) {
			params.append(s).append(' ');
		}
		String msg = "Unknown passed arguments: " + params.toString();
		System.err.println(msg);
		log.warning(msg);
		return null;
	}

	private static File fileToOpen(String[] args) {
		if (args == null || args.length == 0) {
			return null;
		}
		File file = new File(args[0]);
		if (file.exists()) {
			return file;
		}
		String msg = "Passed file " + file + " is not exist";
		System.err.println(msg);
		log.warning(msg);
		return null;
	}

	private static class TempFoldersRemover implements Runnable {

		private final Pattern tmpDirPattern = Pattern.compile("\\d{13}");

		@Override
		public void run() {
			File[] files = Settings.getAppDir().listFiles();
			if (files == null) {
				return;
			}
			for (File d : files) {
				if (d.isDirectory() && tmpDirPattern.matcher(d.getName()).find()) {
					File[] contents = d.listFiles();
					if (contents != null) {
						boolean canBeDeleted = false;
						for (File f : contents) {
							if (Settings.LOCKER_FILE_NAME.equals(f.getName())) {
								canBeDeleted = true;
								break;
							}
						}
						if (canBeDeleted) {
							FileUtils.delete(d);
							log.info("Folder '" + d.getName() + "' has been removed.");
						}
					}
				}
			}
		}
	}

}