package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;

class JarTreeExtractNodeListener extends PopupMenuListener {

	private static final Logger log = Logger.getLogger(JarTreeExtractNodeListener.class.getCanonicalName());

	private File file;

	JarTreeExtractNodeListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
	    super(jarTree, statusBar, frame);
	}

	private static File getTmpFile(JarNode node) {
		return node.getParent() == null ? new File(node.name) : new File(Resources.createTmpDir(), node.name);
	}

	private static String getName(JarNode node) {
		return node.getParent() == null ? new File(node.name).getName() : node.name;
	}

	private void errorDlg(String msg) {
		JOptionPane.showConfirmDialog(frame, msg, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}

    @Override
    protected void doAction(ActionEvent e) {
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
            for (TreePath p : jarTree.getSelectionPaths()) {
                final JarNode node = (JarNode) p.getLastPathComponent();
                final File dst = new File(f, getName(node));
                if (dst.exists()) {
                    int res = JOptionPane.showConfirmDialog(frame, "File " + dst + " already exist. Do you want to replace one?", "Replace or skip file"
                            , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (res != JOptionPane.YES_OPTION) {
                        continue;
                    }
                }
                new Executor() {
                    @Override
                    protected void perform() {
                        statusBar.enableProgress("Extracting...");
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Extracting file " + node.path);
                        }
                        File tmp = getTmpFile(node);
                        node.unzip(tmp);
                        FileUtils.copy(tmp, dst);
                    }

                    @Override
                    protected void doFinally() {
                        statusBar.disableProgress();
                    }
                }.execute();
            }

        }
    }

}
