package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.utils.Enumerator;
import com.delfin.jarexp.utils.Zip;

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
		return lowName.endsWith(".jar") || lowName.endsWith(".war") || lowName.endsWith(".ear") || lowName.endsWith(".zip") || lowName.endsWith(".apk");
	}

	@SuppressWarnings("unchecked")
    void unzip(final File file) {
		if (getParent() == null) {
			return;
		}
		if (isDirectory) {
			file.mkdir();
			new Enumerator<JarNode>(children()) {
                @Override
                protected void doAction(JarNode child) {
                    child.unzip(new File(file, child.name));
                }
            };
		} else {
			Zip.unzip(path, archive, file);
		}
	}

	boolean eq(JarNode node) {
		if (node == null) {
			return false;
		}
		if (this == node) {
			return true;
		}
		List<JarNode> left = getPathList();
		List<JarNode> right = node.getPathList();
		if (left.size() != right.size()) {
			return false;
		}
		for (int i = 0; i < left.size(); ++i) {
			if (!left.get(i).path.equals(right.get(i).path)) {
				return false;
			}
		}
		return true;
	}

	Attributes attrs;
	long time;
	Certificate[] certs;
	CodeSigner[] signers;
	String comment;
	long compSize;
	long crc;
	FileTime creationTime;
	byte[] extra;
	FileTime lastAccessTime;
	FileTime lastModTime;
	int method;
	long size;

	void grab(JarEntry entry) throws IOException {
		attrs = entry.getAttributes();
		time = entry.getTime();
		certs = entry.getCertificates();
		signers = entry.getCodeSigners();
		comment = entry.getComment();
		compSize = entry.getCompressedSize();
		crc = entry.getCrc();
		extra = entry.getExtra();
		method = entry.getMethod();
		size = entry.getSize();
		creationTime = entry.getCreationTime();
		lastAccessTime = entry.getLastAccessTime();
		lastModTime = entry.getLastModifiedTime();
	}

}
