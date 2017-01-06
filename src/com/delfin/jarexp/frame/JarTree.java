package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Jar;
import com.delfin.jarexp.utils.Zip;


class JarTree extends JTree {

	// private static final Logger log = Logger.getLogger(JarTree.class.getCanonicalName());

	private class JarTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 8362013055456239893L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		        boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			if (obj instanceof FileInfo) {
				FileInfo info = (FileInfo) node.getUserObject();
				if (!leaf) {
					String lowName = info.name.toLowerCase();
					if (lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear")) {
					} else { 
						return this;
					}
				}
				setIcon(info.isDir ? Resources.getIconForDir() : Resources.getIconFor(info.name));
			}
			// System.out.println(obj);
			return this;
		}

	}

	private class JarTreeTransfer extends TransferHandler {
		
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			//System.out.println("dasdas");
			if (!(comp instanceof JarTree)) {
				return false;
			}
			if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}

			JarTree tree = (JarTree) comp;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			
			try {
				@SuppressWarnings("unchecked")
				List<File> droppedFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				
				if (node == null) {
					node = root;
				}
                int reply = JOptionPane.showConfirmDialog(JarTree.this, "Do you want to add files " + droppedFiles + " into " + node.getUserObject(), "Adding files confirmation", JOptionPane.YES_NO_OPTION);
                if(reply == JOptionPane.YES_OPTION) {
                	addFilesInto(node, droppedFiles);
                	return true;
                }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				isDragging = false;
			}

			return false;
		}
		
		private void addFilesInto(DefaultMutableTreeNode node, List<File> droppedFiles) throws IOException {

			List<FileInfo> path = new ArrayList<FileInfo>();
			DefaultMutableTreeNode n = node;
			do {
				path.add((FileInfo) n.getUserObject());
				n = (DefaultMutableTreeNode) n.getParent();
			} while (n != null);

			List<File> files = new ArrayList<File>(droppedFiles.size());
			for (File f : droppedFiles) {
				files.add(f);
			}
			FileInfo i = path.get(0);
			FileInfo prevInfo = path.get(path.size() - 1);
			for (FileInfo info : path) {
				if (isArchive(info.name)) {
					//String p = removeName(i.path);
					System.out.println("going to add " + i.path + " : " + prevInfo.parentArc + " : " + files);
					Zip.add(i.path, prevInfo.parentArc, files);
					System.out.println("add " + i.path + " : " + prevInfo.parentArc + " : " + files);
					files.clear();
					files.add(prevInfo.parentArc);
					i = info;
				}
				prevInfo = info;
			}
			
			Zip.copy(prevInfo.parentArc, new File(path.get(path.size() - 1).name));

			addFilesTo(node, droppedFiles);
			update(node);
			

			
			
			
			//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
			
			
//			class FileRecord {
//				File file;
//				String path;
//				FileRecord(File file, String path) {
//					this.file = file;
//					this.path = path;
//				}
//			}
//			
//			TreeNode[] path = node.getPath();
//			File orig = new File(node.getPath()[0].toString());
//			
////			new Jar(orig) {
////				
////				@Override
////				protected void process(JarEntry entry) throws IOException {
////					System.out.println("$$$" + entry);
////				}
////			}.bypass();
//			
//			List<FileRecord> archives = new ArrayList<FileRecord>();
//			String pathInArchive = "";
//			
//			// create new temp dir
//			File tmpDir = Resources.createTmpDir();
//			// copy there main jar file
//			Zip.copy(orig, tmpDir);
//			// go inside path and unpack into new temp folder each zip file
//			File parent = new File(tmpDir, orig.getName());
//			final File tempArchive = parent;
//			archives.add(new FileRecord(parent, null));
//			for (int i = 1; i < path.length; ++i) {
//				String p = path[i].toString();
//				if (isArchive(p)) {
//					tmpDir = Resources.createTmpDir();
//					File dst = new File(tmpDir, p);
//					Zip.unzipFrom(pathInArchive + p, parent, dst);
//					parent = dst;
//					archives.add(new FileRecord(dst, pathInArchive));
//					pathInArchive = "";
//					continue;
//				}
//				pathInArchive += p + "/";
//    		}
//			// get last jar file and add into new files
//			File lastArc = archives.get(archives.size() - 1).file;
//			// Zip.add(pathInArchive, archives.size() == 1 ? orig : lastArc, droppedFiles);
//			Zip.add(pathInArchive, lastArc, droppedFiles);
//			// update all parent archive files
//			List<File> files = new ArrayList<File>();
//			files.add(lastArc);
//			for (int i = archives.size() - 1; i > 0; --i) {
//				// File file = i == 1 ? orig : archives.get(i - 1).file;
//				File file = archives.get(i - 1).file;
//				Zip.add(archives.get(i).path, file, files);
//				files.clear();
//				files.add(file);
//			}
//
//			Zip.copy(tempArchive, orig);
//			
//			addFilesTo(node, droppedFiles);
//			update(node);
			
		}

		private String removeName(String path) {
			int i = path.lastIndexOf('/');
			if (i != -1) {
				return path.substring(i);
			}
			return path;
		}

		private void addFilesTo(DefaultMutableTreeNode node, List<File> files) {
			FileInfo info = (FileInfo)node.getUserObject();
			for (File f : files) {
				String path = info.path + f.getName();
				boolean isDir = f.isDirectory();
				if (isDir) {
					path += "/";
				}
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileInfo(f.getName(), path, info.parentArc, isDir));
				node.add(child);
				if (isDir) {
					addFilesTo(child, Arrays.asList(f.listFiles()));
				}
			}
		}

		private boolean isArchive(String name) {
			String lowName = name.toLowerCase();
			return lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear");
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			isDragging = true;
			if (!(comp instanceof JarTree)) {
				return false;
			}
			for (DataFlavor data : transferFlavors) {
				if (!DataFlavor.javaFileListFlavor.equals(data)) {
					return false;
				}
			}
			return true;
		}

	}
	
	private static final long serialVersionUID = 8627151048727365096L;

	private final DefaultMutableTreeNode root;
	
	private DefaultTreeModel treeModel;

	private boolean isDragging;

	JarTree(File file, TreeSelectionListener treeSelectionListener, TreeExpansionListener treeExpansionListener) throws IOException {
		if (file == null) {
			root = new DefaultMutableTreeNode();
			return;
		}
		File dst = new File(Resources.createTmpDir(), file.getName());
		Zip.copy(file, dst);
		root = new DefaultMutableTreeNode(new FileInfo(file.getAbsolutePath(), "", dst, false));
		new Jar(file){
			@Override
			protected void process(JarEntry entry) throws IOException {
				addIntoNode(entry, root, dst);
			}
		}.bypass();
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(treeSelectionListener);
		addTreeExpansionListener(treeExpansionListener);
		setCellRenderer(new JarTreeCellRenderer());
		// addMouseListener(new JarTreeMouseListener());
		setModel(treeModel = new DefaultTreeModel(root, false));
		
		setDragEnabled(false);
		setTransferHandler(new JarTreeTransfer());
	}
	
	void update(DefaultMutableTreeNode node) {
		treeModel.reload(node);
	}

	private static void addIntoNode(JarEntry entry, DefaultMutableTreeNode root, File parentArc) throws IOException {
		String name = entry.getName();
		DefaultMutableTreeNode node = root;
		for (String p : name.split("/")) {
			FileInfo info = (FileInfo) node.getUserObject();
			if (!info.name.equals(p)) {
				boolean isExist = false;
				for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
					FileInfo fileInfo = (FileInfo) child.getUserObject();
					if (fileInfo.name.equals(p)) {
						node = child;
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileInfo(p, name, parentArc, entry.isDirectory()));
					checkIsArchive(child);
					node.add(child);
					node = child;
				}
			}
		}
	}

	private static void checkIsArchive(DefaultMutableTreeNode node) throws IOException {
		String lowName = ((FileInfo)node.getUserObject()).name.toLowerCase();
		if (lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear")) {
			node.add(new DefaultMutableTreeNode(new FileInfo("", Settings.NAME_PLACEHOLDER, null, false)));
		}
	}

	public void addArchive(File jar, DefaultMutableTreeNode node) throws IOException {
		new Jar(jar){
			@Override
			protected void process(JarEntry entry) throws IOException {
				addIntoNode(entry, node, jar);
			}
		}.bypass();
	}

	public boolean isDragging() {
		return isDragging;
	}
	
}


