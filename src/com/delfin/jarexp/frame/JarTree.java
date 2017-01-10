package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;

import javax.swing.JTree;
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

	private static final long serialVersionUID = 8627151048727365096L;

	private final JarNode root;

	private DefaultTreeModel treeModel;

	private boolean isDragging;

	private boolean isPacking;

	JarTree(File file, TreeSelectionListener treeSelectionListener, TreeExpansionListener treeExpansionListener,
	        DropTargetListener treeDropTargetListener) throws IOException {
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
		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, treeDropTargetListener));

		setCellRenderer(new JarTreeCellRenderer());
		setModel(treeModel = new DefaultTreeModel(root, false));

	}

	void update(DefaultMutableTreeNode node) {
		treeModel.reload(node);
	}

	JarNode getRoot() {
		return root;
	}

	static boolean isArchive(String name) {
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

	void setDragging(boolean isDragging) {
		this.isDragging = isDragging;
	}

	private static void addIntoNode(JarEntry entry, JarNode node, File archive) throws IOException {
		String path = entry.getName();
		String[] files = path.split("/");
		String name = null;
		for (int i = 0; i < files.length; ++i) {
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
				JarNode child = new JarNode(files[i], calcPath(files, i), archive, true);
				node.add(child);
				node = child;
			}
		}
		if (name != null) {
			JarNode child = new JarNode(name, path, archive, entry.isDirectory());
			if (isArchive(child.name)) {
				child.add(new JarNode("", Settings.NAME_PLACEHOLDER, null, false));
			}
			node.add(child);
		}
	}

	private static String calcPath(String[] names, int i) {
		StringBuilder out = new StringBuilder();
		for (int j = 0; j <= i; ++j) {
			out.append(names[j]).append('/');
		}
		return out.toString();
	}

	void setPacking(boolean isPacking) {
		this.isPacking = isPacking;
	}

	boolean isPacking() {
		return isPacking;
	}

}
