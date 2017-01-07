package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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
import javax.swing.tree.TreeSelectionModel;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Jar;
import com.delfin.jarexp.utils.Zip;

class JarTree extends JTree {

	private class JarTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 8362013055456239893L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		        boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			JarNode node = (JarNode) value;
			if (!leaf && !isArchive(node.name)) {
				return this;
			}
			setIcon(node.isDirectory ? Resources.getIconForDir() : Resources.getIconFor(node.name));
			return this;
		}

	}

	private class JarTreeTransfer extends TransferHandler {

		private static final long serialVersionUID = -3545320033106894232L;

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			if (!(comp instanceof JarTree)) {
				return false;
			}
			if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}

			JarTree tree = (JarTree) comp;
			JarNode node = (JarNode) tree.getLastSelectedPathComponent();

			try {
				@SuppressWarnings("unchecked")
				List<File> droppedFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

				if (node == null) {
					node = root;
				}
				int reply = JOptionPane.showConfirmDialog(JarTree.this,
				        "Do you want to add files " + droppedFiles + " into " + node.name, "Adding files confirmation",
				        JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					packIntoJar(node, droppedFiles);
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

		private void packIntoJar(JarNode node, List<File> droppedFiles) throws IOException {
			//System.out.println(node);
			
			List<JarNode> path = new ArrayList<JarNode>();
			JarNode n = node;
			do {
				path.add(n);
				n = (JarNode) n.getParent();
			} while (n != null);

			List<File> files = new ArrayList<File>(droppedFiles.size());
			for (File f : droppedFiles) {
				files.add(f);
			}
			JarNode i = path.get(0);
			JarNode prevInfo = path.get(path.size() - 1);
			for (JarNode info : path) {
				if (isArchive(info.name)) {
					//System.out.println("Adding " + i.path + " | " + prevInfo.archive + " | " + files);
					Zip.add(i.path, prevInfo.archive, files);
					//System.out.println("Added  " + i.path + " | " + prevInfo.archive + " | " + files);
					files.clear();
					files.add(prevInfo.archive);
					i = info;
				}
				prevInfo = info;
			}

			Zip.copy(prevInfo.archive, new File(path.get(path.size() - 1).name));

			placeIntoTree(node, droppedFiles);
			update(node);
		}

		private void placeIntoTree(JarNode node, List<File> files) {
			for (File f : files) {
				String path = node.path + f.getName();
				boolean isDir = f.isDirectory();
				if (isDir) {
					path += "/";
				}
				JarNode child = new JarNode(f.getName(), path, node.archive, isDir);
				node.add(child);
				if (isDir) {
					placeIntoTree(child, Arrays.asList(f.listFiles()));
				}
			}
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

	private final JarNode root;

	private DefaultTreeModel treeModel;

	private boolean isDragging;

	JarTree(File file, TreeSelectionListener treeSelectionListener, TreeExpansionListener treeExpansionListener)
	        throws IOException {
		if (file == null) {
			root = new JarNode();
			return;
		}
		File dst = new File(Resources.createTmpDir(), file.getName());
		Zip.copy(file, dst);
		root = new JarNode(file.getAbsolutePath(), "", dst, false);
		new Jar(file) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				addIntoNode(entry, root, dst);
			}
		}.bypass();
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(treeSelectionListener);
		addTreeExpansionListener(treeExpansionListener);
		setCellRenderer(new JarTreeCellRenderer());
		setModel(treeModel = new DefaultTreeModel(root, false));

		setDragEnabled(false);
		setTransferHandler(new JarTreeTransfer());
	}

	void update(DefaultMutableTreeNode node) {
		treeModel.reload(node);
	}

	private static void addIntoNode(JarEntry entry, JarNode node, File archive) throws IOException {
		String path = entry.getName();
		// System.out.println("entry " + entry);
		
		String[] files = path.split("/");
		String name = null;
		for (int i = 0; i < files.length ; ++i) {
			boolean isExist = false;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				JarNode child = (JarNode) children.nextElement();
				if (child.name.equals(files[i])) {
					node = child;
					isExist = true;
					break;
				}
			}
			if (i == files.length - 1) {
				name = isExist ? null : files[i];
				break;
			}
			if (!isExist) {
				String p = "";
				for (int j = 0; j <= i; ++j) {
					p += files[j] + "/";
				}
				JarNode child = new JarNode(files[i], p, archive, true);
				node.add(child);
				node = child;
			}
		}
		if (name != null) {
			JarNode child = new JarNode(name, path, archive, entry.isDirectory());
			// System.out.println("child " + name + " | " + path + " | " + archive);
			if (isArchive(child.name)) {
				child.add(new JarNode("", Settings.NAME_PLACEHOLDER, null, false));
			}
			node.add(child);
		}
	}

	private static boolean isArchive(String name) {
		String lowName = name.toLowerCase();
		return lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear");
	}

	void addArchive(File jar, JarNode node) throws IOException {
		new Jar(jar) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				addIntoNode(entry, node, jar);
			}
		}.bypass();
	}

	boolean isDragging() {
		return isDragging;
	}

}
