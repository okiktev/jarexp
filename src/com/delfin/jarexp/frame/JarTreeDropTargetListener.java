package com.delfin.jarexp.frame;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.utils.Zip;

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
		List<File> files = new ArrayList<File>();
		for (File f : droppedFiles) {
			files.add(f);
		}
		List<JarNode> path = node.getPathList();
		JarNode currNode = path.get(0);
		String p = currNode.path.endsWith(currNode.name) ? "" : currNode.path;
		File archive = p.isEmpty() ? currNode.getCurrentArchive() : currNode.archive;
		System.out.println("Adding " + p + " | " + archive + " | " + files);
		Zip.add(p, archive, files);
		
		//Zip.delete(currNode.path, currNode.archive);
		//JarNode prevNode = path.get(path.size() - 1);

		files.clear();
		files.add(archive);

		List<JarNode> archives = node.grabParentArchives();
		System.out.println(archives);
//		do {
//			if (JarTree.isArchive(currNode.name)) {
//				p = currNode.path;
//				break;
//			}
//			currNode = (JarNode) currNode.getParent();
//		} while(currNode != null);
		p = archives.get(0).path;
		//currNode = archives.get(0);
		for (int i = 1; i < archives.size(); ++i) {
			JarNode arc = archives.get(i);
			//p = arc.path.endsWith(arc.name) ? "" : arc.path;
			//archive = p.isEmpty() ? arc.getCurrentArchive() : arc.archive;
			
			System.out.println("Adding " + p + " | " + arc.getCurrentArchive() + " | " + files);
			Zip.add(p, arc.getCurrentArchive(), files);
			// System.out.println("Added " + currNode.path + " | " + arc.getCurrentArchive() + " | " + files);
			files.clear();
			files.add(arc.getCurrentArchive());
			p = arc.path;
		}
		System.out.println("Copying " + path.get(path.size() - 1).archive + " into " + new File(path.get(path.size() - 1).name));
		Zip.copy(path.get(path.size() - 1).archive, new File(path.get(path.size() - 1).name));
		
		
		
		
		
//		// System.out.println(node);
//
//		List<JarNode> path = new ArrayList<JarNode>();
//		JarNode n = node;
//		do {
//			path.add(n);
//			n = (JarNode) n.getParent();
//		} while (n != null);
//
//		List<File> files = new ArrayList<File>(droppedFiles.size());
//		for (File f : droppedFiles) {
//			files.add(f);
//		}
//		
//		JarNode i = path.get(0);
//		JarNode prevInfo = path.get(path.size() - 1);
//		for (JarNode info : path) {
//			if (JarTree.isArchive(info.name)) {
//				// String p = i.path.endsWith(i.name) ? "" : i.path;
//				String p = cutName(i.path);
//				// File archive = p.isEmpty() ? info.getCurrentArchive() : prevInfo.archive;
//				File archive = info.getCurrentArchive();
//				System.out.println("Adding " + p + " | " + archive + " | " + files);
//				Zip.add(p, archive, files);
//				System.out.println("Added " + p + " | " + archive + " | " + files);
//				files.clear();
//				files.add(archive);
//				i = info;
//			}
//			prevInfo = info;
//		}

		// Zip.copy(prevInfo.archive, new File(path.get(path.size() - 1).name));

		placeIntoTree(node, droppedFiles);
	}

	private void placeIntoTree(JarNode node, List<File> files) {
		for (File f : files) {
			boolean isArchive = JarTree.isArchive(node.path);
			String path = (isArchive ? "" : node.path) + f.getName();
			boolean isDir = f.isDirectory();
			if (isDir) {
				path += "/";
			}
			File archive = isArchive ? node.getCurrentArchive() : node.archive;
			JarNode child = new JarNode(f.getName(), path, archive, isDir);
			node.add(child);
			if (isDir) {
				placeIntoTree(child, Arrays.asList(f.listFiles()));
			}
		}
	}
	

	private JarNode getNode(DropTargetDragEvent dtde) {
		return getNodeByLocation(dtde.getLocation());
	}
	
	private JarNode getNode(DropTargetDropEvent dtde) {
		return getNodeByLocation(dtde.getLocation());
	}
	
	private JarNode getNodeByLocation(Point point) {
		TreePath parentPath = jarTree.getPathForLocation(point.x, point.y);
		if (parentPath == null) {
			return null;
		}
		return (JarNode) parentPath.getLastPathComponent();
	}
	
}
