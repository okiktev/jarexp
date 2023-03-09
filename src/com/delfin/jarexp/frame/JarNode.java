package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.analyzer.IJavaItem;
import com.delfin.jarexp.analyzer.IJavaItem.Position;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Enumerator;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
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

	private File tempArch;

	File origArch;

	boolean isDirectory;

	private Boolean isArchive;

	private Boolean isClass;

	ClassItemNode selectedChild;

	JarNode(String name, String path, File tempArch, File origArch, boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.tempArch = tempArch;
		this.origArch = origArch;
		this.isDirectory = isDirectory;
	}

	JarNode() {
	}

	@Override
	public String toString() {
		return name;
	}

	boolean isNotClass() {
		if (isClass == null) {
			isClass = StringUtils.endsWith(name, ".class");
		}
		return !isClass;
	}

	File getTempArchive() {
		if (tempArch != null && !tempArch.exists()) {
			FileUtils.copy(origArch, tempArch);
		}
		return tempArch;
	}

	void setTempArchive(File tempArch) {
		this.tempArch = tempArch;
	}

	File getCurrentArchive() {
		if (getChildCount() <= 0) {
			throw new JarexpException("Unexpected content of jar file " + this.path);
		}
		return ((JarNode) getChildAt(0)).getTempArchive();
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

	String getFullPath() {
		if (getParent() == null) {
			return name;
		}
		List<JarNode> nodes = getPathList();
		Collections.reverse(nodes);
		StringBuilder out = new StringBuilder();
		for (JarNode node : nodes) {
			if (node.getParent() == null) {
				continue;
			}
			out.append('/').append(node.name);
			if (node.isArchive()) {
				out.append('!');
			}
		}
		return out.toString();
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
		if (isArchive == null) {
			isArchive = Zip.isArchive(name);
		}
		return isArchive.booleanValue();
	}

    @SuppressWarnings("unchecked")
	void unzip(final File file) {
		if (getParent() == null) {
			return;
		}
		if (isDirectory) {
			file.mkdir();
			new Enumerator<TreeNode>(children()) {
                @Override
                protected void doAction(TreeNode child) {
            		JarNode node  = (JarNode)child;
            		node.unzip(new File(file, node.name));
                }
            };
		} else {
			Zip.unzip(getFullPath(), path, getTempArchive(), file, false);
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
	byte[] extra;
	int method;
	long size;
	// to support Java 6 (avoiding java.nio.file.attribute.FileTime)
	Object creationTime;
	Object lastAccessTime;
	Object lastModTime;

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
		if (Version.JAVA_MAJOR_VER > 7) {
			creationTime = entry.getCreationTime();
			lastAccessTime = entry.getLastAccessTime();
			lastModTime = entry.getLastModifiedTime();
		}
	}

	static class ClassItemNode extends DefaultMutableTreeNode {

		private static final long serialVersionUID = 7470246996120563613L;

		IJavaItem javaItem;

		private final String name;

		private ClassItemNode() {
			name = null;
		}

		ClassItemNode(IJavaItem javaItem) {
			this.javaItem = javaItem;
			this.name = javaItem.getName();
		}

		Position getPosition() {
			return javaItem.getPosition();
		}

		@Override
		public String toString() {
			return name;
		}

	}

	static class PeNode extends ClassItemNode {

		private static final long serialVersionUID = 3944805067597550882L;

		JarNode parent;

		String name;

		PeNode(JarNode parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		String getFullPath() {
			return parent.getFullPath() + '/' + name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

}
