package com.delfin.jarexp.frame;

import java.awt.event.ActionListener;

import javax.swing.JFrame;

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

}
