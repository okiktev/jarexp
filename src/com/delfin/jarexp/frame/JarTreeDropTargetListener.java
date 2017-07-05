package com.delfin.jarexp.frame;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.dlg.message.Msg;

class JarTreeDropTargetListener implements DropTargetListener {

	private static final Logger log = Logger.getLogger(JarTreeDropTargetListener.class.getCanonicalName());

	private final JarTree jarTree;

	private final StatusBar statusBar;

	private final JFrame frame;

	JarTreeDropTargetListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
		this.jarTree = jarTree;
		this.statusBar = statusBar;
		this.frame = frame;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		jarTree.setDragging(true);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		JarNode node = null;
		if (jarTree.isPacking() || (node = getNode(dtde)) == null) {
			dtde.rejectDrag();
			return;
		}
		if (node.isLeaf()) {
			dtde.rejectDrag();
		} else {
			jarTree.setSelectionPath(new TreePath(node.getPath()));
			dtde.acceptDrag(dtde.getDropAction());
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		jarTree.setDragging(false);
	}

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		if (jarTree.isPacking()) {
			dtde.rejectDrop();
			return;
		}
		final JarNode node = getNode(dtde);
		if (node == null) {
			dtde.rejectDrop();
			return;
		}
		final List<File> droppedFiles = getDroppedFiles(dtde);
		if (droppedFiles.isEmpty()) {
			JOptionPane.showConfirmDialog(frame, "There is wrong dopped data format. Expected only files list.",
			        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			dtde.dropComplete(true);
			return;
		}
		int reply = Msg.showList("Adding files confirmation", 
				"Do you want to add into\n" + node.name + "\nfollowing files:", droppedFiles);
		if (reply == JOptionPane.YES_OPTION) {
			new Executor() {

				@Override
				protected void perform() {
					statusBar.enableProgress("Packing...");
					jarTree.setPacking(true);
					packIntoJar(node, droppedFiles);
					jarTree.update(node);
				}

				@Override
				protected void doFinally() {
					dtde.dropComplete(true);
					jarTree.setDragging(false);
					jarTree.setPacking(false);
					statusBar.disableProgress();
				};
			}.execute();
		} else {
			dtde.dropComplete(true);
			jarTree.setDragging(false);
			jarTree.setPacking(false);
		}
	}

	private static List<File> getDroppedFiles(DropTargetDropEvent dtde) {
		try {
			List<File> res = new ArrayList<File>();
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			if (flavors.length > 1) {
				log.warning("There are " + flavors.length + " flavors found");
			}
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					dtde.acceptDrop(dtde.getDropAction());
					Object obj = tr.getTransferData(flavors[i]);
					if (obj instanceof List<?>) {
						res.clear();
						for (Object o : (List<?>) obj) {
							if (o instanceof File) {
								res.add((File) o);
							}
						}
					}
				}
			}
			return res;
		} catch (Exception e) {
			throw new JarexpException("An error occurred while retrieving dropped files", e);
		}
	}

	void packIntoJar(JarNode node, List<File> droppedFiles) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Dropping files " + droppedFiles + " into " + node.getPathList());
		}
		List<File> files = new ArrayList<File>();
		for (File f : droppedFiles) {
			files.add(f);
		}
		Jar.pack(node, files);
		files.clear();
		for (File f : droppedFiles) {
			files.add(f);
		}
		JarTree.put(node, files);
	}

	private JarNode getNode(DropTargetDragEvent dtde) {
		return jarTree.getNodeByLocation(dtde.getLocation());
	}

	private JarNode getNode(DropTargetDropEvent dtde) {
		return jarTree.getNodeByLocation(dtde.getLocation());
	}

}
