package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;

class JarTreeDeleteNodeListener implements ActionListener {

	private static final Logger log = Logger.getLogger(JarTreeDeleteNodeListener.class.getCanonicalName());

	private final StatusBar statusBar;
	
	private final JarTree jarTree;
	
	JarTreeDeleteNodeListener(JarTree jarTree, StatusBar statusBar) {
		this.jarTree = jarTree;
		this.statusBar = statusBar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
		final JarNode node = (JarNode) item.path.getLastPathComponent();
		new Executor() {

			@Override
			protected void perform() {
				statusBar.enableProgress("Removing...");
				if (log.isLoggable(Level.FINE)) {
					log.fine("Deleting file " + node.path);
				}
				delFromJar(node);
				TreeNode parent = node.getParent();
				jarTree.remove(node);
				jarTree.update(parent);
			}

			@Override
			protected void doFinally() {
				statusBar.disableProgress();
			}
		}.execute();

	}

	void delFromJar(JarNode node) {
		Jar.delete(node);
	}

}
