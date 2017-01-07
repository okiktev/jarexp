package com.delfin.jarexp.frame;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

class JarNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2854697452587898049L;

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
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return name;
	}

}
