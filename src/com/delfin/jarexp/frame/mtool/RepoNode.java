package com.delfin.jarexp.frame.mtool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

class RepoNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2854697452587898049L;

	File file;

	boolean isRoot;

	List<File> files;

	RepoNode(File file) {
		this(file, false);
	}

	RepoNode(File file, boolean isRoot) {
		this.file = file;
		this.isRoot = isRoot;
	}

	RepoNode() {
	}

	@Override
	public String toString() {
		return file == null ? null : isRoot ? file.getAbsolutePath() : file.getName();
	}

	List<RepoNode> getPathList() {
		List<RepoNode> path = new ArrayList<RepoNode>();
		RepoNode node = this;
		do {
			path.add(node);
			node = (RepoNode) node.getParent();
		} while (node != null);
		return path;
	}

	void addFiles(File[] files) {
		this.files = Arrays.asList(files);
	}

}
