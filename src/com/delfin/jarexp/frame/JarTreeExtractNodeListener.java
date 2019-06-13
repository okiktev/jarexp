package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.ActionHistory;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;

class JarTreeExtractNodeListener extends PopupMenuListener {

	private static final Logger log = Logger.getLogger(JarTreeExtractNodeListener.class.getCanonicalName());

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
		showConfirmDialog(frame, msg, "Error", DEFAULT_OPTION, ERROR_MESSAGE);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select directory for extract");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        initCurrentDir(chooser);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.exists()) {
                errorDlg("File " + f + " is not exist");
                return;
            }
            if (!f.isDirectory()) {
                errorDlg("File " + f + " is not a folder");
                return;
            }
            ActionHistory.addLastDirSelected(f);
            for (TreePath p : jarTree.getSelectionPaths()) {
                final JarNode node = (JarNode) p.getLastPathComponent();
                final File dst = new File(f, getName(node));
                if (dst.exists()) {
                    int res = showConfirmDialog(frame, "File " + dst + " already exist. Do you want to replace one?", "Replace or skip file"
                            , YES_NO_OPTION, QUESTION_MESSAGE);
                    if (res != YES_OPTION) {
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
                        clearNodeSelection();
                        statusBar.disableProgress();
                    }
                }.execute();
            }

        }
    }

}
