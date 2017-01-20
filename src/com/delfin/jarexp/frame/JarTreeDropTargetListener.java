package com.delfin.jarexp.frame;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;

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
		if (jarTree.isPacking()) {
			dtde.rejectDrag();
			return;
		}
		JarNode node = getNode(dtde);
		if (node == null) {
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
	public void drop(DropTargetDropEvent dtde) {
		if (jarTree.isPacking()) {
			dtde.rejectDrop();
			return;
		}
		
		JarNode node = getNode(dtde);
		if (node == null) {
			dtde.rejectDrop();
			return;				
		}
		try {
			final List<File> droppedFiles = new ArrayList<File>();
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
						droppedFiles.clear();
						for (Object o : (List<?>)obj) {
							if (!(o instanceof File)) {
								continue;
							}
							droppedFiles.add((File)o);
						}
					}
				}
			}
			if (droppedFiles.isEmpty()) {
				JOptionPane.showConfirmDialog(frame,
				        "There is wrong dopped data format. Expected only files list.", 
				        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				dtde.dropComplete(true);
				return;
			}
			int reply = JOptionPane.showConfirmDialog(frame,
			        "Do you want to add files " + droppedFiles + " into " + node.name, "Adding files confirmation",
			        JOptionPane.YES_NO_OPTION);
			if (reply == JOptionPane.YES_OPTION) {
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						statusBar.enableProgress("Packing...");
						jarTree.setPacking(true);
						packIntoJar(node, droppedFiles);
						jarTree.update(node);
						statusBar.disableProgress();
						jarTree.setDragging(false);
						jarTree.setPacking(false);
						dtde.dropComplete(true);
						return null;
					}
				}.execute();
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while adding data into jar", e);
		} finally {
			jarTree.setDragging(false);
		}
	}

	void packIntoJar(JarNode node, List<File> droppedFiles) throws IOException {
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
