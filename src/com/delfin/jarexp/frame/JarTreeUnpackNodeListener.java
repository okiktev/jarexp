package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;

class JarTreeUnpackNodeListener extends PopupMenuListener {

	private static final Logger log = Logger.getLogger(JarTreeUnpackNodeListener.class.getCanonicalName());

	private File file;

	JarTreeUnpackNodeListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
	    super(jarTree, statusBar, frame);
	}

	private static File getTmpFile(JarNode node) {
		return node.getParent() == null ? new File(node.name) : new File(Resources.createTmpDir(), node.name);
	}

	private void errorDlg(String msg) {
		JOptionPane.showConfirmDialog(frame, msg, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}

    @Override
    protected void doAction(ActionEvent e) {
        JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
        final JarNode node = (JarNode) item.path.getLastPathComponent();
        new Executor() {
            @Override
            protected void perform() {
                final JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select directory for unpack");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (file != null) {
                    chooser.setCurrentDirectory(file);
                }
                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    final File f = file = chooser.getSelectedFile();
                    if (!f.isDirectory()) {
                        errorDlg("File " + f + " is not a folder");
                        return;
                    }
                    if (!f.exists()) {
                        f.mkdirs();
                    } else {
                        int res = JOptionPane.showConfirmDialog(frame,
                                "Folder " + f + " already exist. Do you want to replace all data inside?",
                                "Replace or skip data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (res != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    statusBar.enableProgress("Unpacking...");
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("Unpacking file " + node.path);
                    }
                    File tmp = getTmpFile(node);
                    node.unzip(tmp);
                    Zip.unzip(tmp, f);
                }
            }

            @Override
            protected void doFinally() {
                statusBar.disableProgress();
            }
        }.execute();
    }

}