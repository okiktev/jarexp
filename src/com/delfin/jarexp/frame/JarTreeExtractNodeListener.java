package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;

class JarTreeExtractNodeListener implements ActionListener {

	private static final Logger log = Logger.getLogger(JarTreeExtractNodeListener.class.getCanonicalName());

	private final StatusBar statusBar;

	private final JFrame frame;

	private File file;

	JarTreeExtractNodeListener(StatusBar statusBar, JFrame frame) {
		this.statusBar = statusBar;
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select directory for extract");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (file != null) {
			chooser.setCurrentDirectory(file);
		}
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File f = file = chooser.getSelectedFile();
			if (!f.exists()) {
				errorDlg("File " + f + " is not exist");
				return;
			}
			if (!f.isDirectory()) {
				errorDlg("File " + f + " is not a folder");
				return;
			}
			JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
			JarNode node = (JarNode) item.path.getLastPathComponent();
			File dst = new File(f, node.name);
			if (dst.exists()) {
				int res = JOptionPane.showConfirmDialog(frame, "File " + dst + " already exist. Do you want to replace one?", "Replace or skip file"
						, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (res == JOptionPane.YES_OPTION) {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							statusBar.enableProgress("Extracting...");
							if (log.isLoggable(Level.FINE)) {
								log.fine("Extracting file " + node.path);
							}
							File tmp = new File(Resources.createTmpDir(), node.name);
							node.unzip(tmp);
							FileUtils.copy(tmp, dst);
							statusBar.disableProgress();
							return null;
						}
					}.execute();
				}
			}
		}
	}

	private void errorDlg(String msg) {
		JOptionPane.showConfirmDialog(frame, msg, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}

}
