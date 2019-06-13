package com.delfin.jarexp.frame;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.delfin.jarexp.ActionHistory;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;

abstract class PopupMenuListener implements ActionListener {

    protected final JarTree jarTree;

    protected final StatusBar statusBar;

    protected final JFrame frame;

    PopupMenuListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
        this.jarTree = jarTree;
        this.statusBar = statusBar;
        this.frame = frame;
    }

    protected void clearNodeSelection() {
        JarTreeClickSelection.setNodes(null);
        jarTree.setSelectionPaths(null);
    }

	protected void initCurrentDir(JFileChooser chooser) {
		List<File> dirs = ActionHistory.getLastDirSelected();
		File file = dirs.isEmpty() ? jarTree.getRoot().origArch.getParentFile() : dirs.get(0);
		chooser.setCurrentDirectory(file);
	}

}
