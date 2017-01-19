package com.delfin.jarexp.frame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;

class JarNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2854697452587898049L;

	static class JarNodeMenuItem extends JMenuItem {

		private static final long serialVersionUID = 8153268977886693800L;

		TreePath path;

		JarNodeMenuItem(String text, TreePath path) {
			super(text);
			this.path = path;
		}

	}

	String name;

	String path;

	File archive;

	boolean isDirectory;

	JarNode(String name, String path, File archive, boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.archive = archive;
		this.isDirectory = isDirectory;
	}

	public JarNode() {
	}

	@Override
	public String toString() {
		return name;
	}

	File getCurrentArchive() {
		if (getChildCount() <= 0) {
			throw new JarexpException("Unexpected content of jar file " + this.path);
		}
		return ((JarNode) getChildAt(0)).archive;
	}

	List<JarNode> getPathList() {
		List<JarNode> path = new ArrayList<JarNode>();
		JarNode node = this;
		do {
			path.add(node);
			node = (JarNode) node.getParent();
		} while (node != null);
		return path;
	}

	List<JarNode> grabParentArchives() {
		List<JarNode> res = new ArrayList<JarNode>();
		JarNode node = this;
		while (node != null) {
			if (node.isArchive()) {
				res.add(node);
			}
			node = (JarNode) node.getParent();
		}
		return res;
	}

	boolean isArchive() {
		String lowName = name.toLowerCase();
		return lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear") || lowName.endsWith(".zip");
	}

}
