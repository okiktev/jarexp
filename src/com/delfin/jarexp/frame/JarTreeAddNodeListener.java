package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;

class JarTreeAddNodeListener implements ActionListener {

	private final StatusBar statusBar;

	private final JarTree jarTree;

	private final JFrame frame;

	private File file;

	JarTreeAddNodeListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
		this.jarTree = jarTree;
		this.statusBar = statusBar;
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select files/folders to add");
		if (file != null) {
			chooser.setCurrentDirectory(file.getParentFile());
		}
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.exists()) {
				JOptionPane.showConfirmDialog(frame, "File " + f.getAbsolutePath() + " is not exist", "Error",
				        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				return;
			}
			JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
			JarNode node = (JarNode) item.path.getLastPathComponent();
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					statusBar.enableProgress("Packing...");
					jarTree.setPacking(true);
					List<File> files = new ArrayList<File>(1);
					files.add(f);
					Jar.pack(node, files);
					files.clear();
					files.add(f);
					JarTree.put(node, files);
					jarTree.update(node);
					statusBar.disableProgress();
					jarTree.setPacking(false);
					return null;
				}
			}.execute();
		}
	}
}
