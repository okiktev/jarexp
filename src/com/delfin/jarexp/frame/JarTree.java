package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;

class JarTree extends JTree {

	private class JarTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 8362013055456239893L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		        boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

			if (value instanceof JarNode) {
				JarNode node = (JarNode) value;
				if (!leaf && !node.isArchive()) {
					return this;
				}
				setIcon(node.isDirectory ? Resources.getIconForDir() : Resources.getIconFor(node.name));
			}
			
			return this;
		}

	}

	class JarTreeMouseListener implements MouseListener {

		private final ActionListener deleteActionListener;

		private final ActionListener addActionListener;

		JarTreeMouseListener(ActionListener deleteActionListener, ActionListener addActionListener) {
			this.deleteActionListener = deleteActionListener;
			this.addActionListener = addActionListener;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}
				setSelectionPath(path);
				JPopupMenu popupMenu = new JPopupMenu();
				JarNodeMenuItem deleteNode = new JarNodeMenuItem("Delete", path);
				deleteNode.setIcon(Resources.getInstance().getDelIcon());
				deleteNode.addActionListener(deleteActionListener);
				JarNodeMenuItem addNode = new JarNodeMenuItem("Add", path);
				addNode.setIcon(Resources.getInstance().getAddIcon());
				addNode.addActionListener(addActionListener);
				popupMenu.add(addNode);
				popupMenu.add(deleteNode);
				popupMenu.show(JarTree.this, e.getX(), e.getY());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

	}
	
	static class ArchiveLoader {
		protected void load(JarNode node) {
			node.add(new JarNode("", Settings.NAME_PLACEHOLDER, null, false));
		}
	}

	static ArchiveLoader archiveLoader = new ArchiveLoader();
	
	private static final long serialVersionUID = 8627151048727365096L;

	private JarNode root;

	private DefaultTreeModel model;

	private boolean isDragging;

	private boolean isPacking;

	JarTree(TreeSelectionListener treeSelectionListener, TreeExpansionListener treeExpansionListener, StatusBar statusBar, JFrame frame) {

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(treeSelectionListener);
		addTreeExpansionListener(treeExpansionListener);

		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new JarTreeDropTargetListener(this, statusBar, frame)));
		addMouseListener(new JarTreeMouseListener(new JarTreeDeleteNodeListener(this, statusBar)
				, new JarTreeAddNodeListener(this, statusBar, frame)));
		setCellRenderer(new JarTreeCellRenderer());
	}

	void load(File file) throws IOException {
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
		setModel(model = new DefaultTreeModel(root, false));
	}

	void update(TreeNode node) {
		model.reload(node);
	}

	JarNode getRoot() {
		return root;
	}
	
	static void put(JarNode node, List<File> files) {
		for (File f : files) {
			boolean isArchive = node.isArchive();
			String path = (isArchive ? "" : node.path) + f.getName();
			boolean isDir = f.isDirectory();
			if (isDir) {
				path += "/";
			}
			File archive = isArchive ? node.getCurrentArchive() : node.archive;
			JarNode child = new JarNode(f.getName(), path, archive, isDir);
			if (child.isArchive()) {
				archiveLoader.load(child);
			}
			node.add(child);
			if (isDir) {
				put(child, Arrays.asList(f.listFiles()));
			}
		}
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
			if (child.isArchive()) {
				archiveLoader.load(child);
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

	void remove(JarNode node) {
		model.removeNodeFromParent(node);
	}

}
