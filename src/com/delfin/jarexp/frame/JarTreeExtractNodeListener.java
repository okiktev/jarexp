package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.frame.JarNode.PeNode;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Executor;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.StreamProcessor;
import com.delfin.jarexp.win.exe.PE;

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
            	Object obj = p.getLastPathComponent();
            	if (obj instanceof PeNode) {
            		 final PeNode pe = (PeNode) obj;
            		 final File dst = new File(f, pe.getName());
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
                                 log.fine("Extracting file " + pe.getFullPath());
                             }
                             final byte[][] ico = new byte[1][1];
                             FileOutputStream outStream = null;
                             try {
                                 if (jarTree.isSingleFileLoaded()) {
                                     ico[0] = PE.getIcon(pe.parent.getTempArchive(), pe.name.replace(".ico", ""));
                                 } else {
                                     Zip.stream(pe.parent.getTempArchive(), pe.parent.path, new StreamProcessor() {
                                         @Override
                                         public void process(InputStream stream) throws IOException {
                                             ico[0] = PE.getIcon(stream, pe.name.replace(".ico", ""));
                                         }
                                     });
                                 }
                                 File tmp = new File(Settings.getJarexpTmpDir(), "ico_" + System.currentTimeMillis() + ".ico");
                                 outStream = new FileOutputStream(tmp);
                                 outStream.write(ico[0]);
                                 FileUtils.copy(tmp, dst);
                             } catch (Exception e) {
                                 log.log(Level.SEVERE, "Couldn't extract icon " + pe.name, e);
                                 showConfirmDialog(frame, "Unable to extract icon " + pe.name + ". Cause: " + e.getMessage(), "Error while extracting icon"
                                         , OK_OPTION, ERROR_MESSAGE);
                             } finally {
                                 if (outStream != null) {                                     
                                     try {
                                        outStream.close();
                                    } catch (IOException e) {
                                        log.log(Level.WARNING, "An error occurred while closing output stream of " + pe.name, e);
                                    }
                                 }
                             }
                         }

                         @Override
                         protected void doFinally() {
                             clearNodeSelection();
                             statusBar.disableProgress();
                         }
                     }.execute();
            	} else {
                    final JarNode node = (JarNode) obj;
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

}
