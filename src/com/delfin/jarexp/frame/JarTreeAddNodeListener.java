package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.settings.ActionHistory;

class JarTreeAddNodeListener extends PopupMenuListener {

	private static final Logger log = Logger.getLogger(JarTreeAddNodeListener.class.getCanonicalName());

	JarTreeAddNodeListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
		super(jarTree, statusBar, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select files/folders to add");
		initCurrentDir(chooser);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			final File f = chooser.getSelectedFile();
			if (!f.exists()) {
				showConfirmDialog(frame, "File " + f + " is not exist", "Error", DEFAULT_OPTION, ERROR_MESSAGE);
				return;
			}
			ActionHistory.addLastDirSelected(f.isFile() ? f.getParentFile() : f);
			JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
			final JarNode node = (JarNode) item.path.getLastPathComponent();
			new Executor() {

				@Override
				protected void perform() {
					statusBar.enableProgress("Packing...");
					jarTree.setPacking(true);
					if (log.isLoggable(Level.FINE)) {
						log.fine("Adding file " + node.path);
					}
					List<File> files = new ArrayList<File>(1);
					files.add(f);
					Jar.pack(node, files);
					files.clear();
					files.add(f);
					JarTree.put(node, files);
					jarTree.update(node);
				}

				@Override
				protected void doFinally() {
					clearNodeSelection();
					jarTree.setPacking(false);
					statusBar.disableProgress();
				};

			}.execute();
		}
	}

}
