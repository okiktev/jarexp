package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class JarTreeDeleteNodeListener extends PopupMenuListener {

    private static final Logger log = Logger.getLogger(JarTreeDeleteNodeListener.class.getCanonicalName());

    JarTreeDeleteNodeListener(JarTree tree, StatusBar statusBar, JFrame frame) {
        super(tree, statusBar, frame);
    }

	void delFromJar(List<JarNode> nodes) {
		Jar.delete(nodes);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        final TreePath[] paths = jarTree.getSelectionPaths();
        new Executor() {

            @Override
            protected void perform() {
                statusBar.enableProgress("Removing...");
                List<JarNode> nodes = getNodes();
                delFromJar(nodes);
                for (JarNode node : nodes) {
                    TreeNode parent = node.getParent();
                    jarTree.remove(node);
                    jarTree.update(parent);
                }
            }

            private List<JarNode> getNodes() {
                List<JarNode> nodes = new ArrayList<JarNode>(paths.length);
                for (TreePath path : paths) {
                    nodes.add((JarNode)path.getLastPathComponent());
                }
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Deleting files:");
                    for (JarNode n : nodes) {
                        log.fine("\t" + n.path);
                    }
                }
                return nodes;
            }

            @Override
            protected void doFinally() {
                clearNodeSelection();
                statusBar.disableProgress();
            }
        }.execute();
    }
	
	
//    @Override
//    protected void doAction(ActionEvent e) {
//        final TreePath[] paths = jarTree.getSelectionPaths();
//        
//        statusBar.enableProgress("Removing...");
//        
//        new Executor() {
//
//            @Override
//            protected void perform() {
//                statusBar.enableProgress("Removing...");
//                try {
//                    Thread.sleep(1000*10);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                List<JarNode> nodes = getNodes();
//                delFromJar(nodes);
//                for (JarNode node : nodes) {
//                    TreeNode parent = node.getParent();
//                    jarTree.remove(node);
//                    jarTree.update(parent);
//                }
//            }
//
//            private List<JarNode> getNodes() {
//                List<JarNode> nodes = new ArrayList<JarNode>(paths.length);
//                for (TreePath path : paths) {
//                    nodes.add((JarNode)path.getLastPathComponent());
//                }
//                if (log.isLoggable(Level.FINE)) {
//                    log.fine("Deleting files:");
//                    for (JarNode n : nodes) {
//                        log.fine("\t" + n.path);
//                    }
//                }
//                return nodes;
//            }
//
//            @Override
//            protected void doFinally() {
//                statusBar.disableProgress();
//            }
//        }.execute();
//    }

}