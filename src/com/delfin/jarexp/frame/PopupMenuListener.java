package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        doAction(e);
        JarTreeClickSelection.setNodes(null);
        jarTree.setSelectionPaths(null);
    }

    protected abstract void doAction(ActionEvent e);

}
