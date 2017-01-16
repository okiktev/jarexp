package com.delfin.jarexp.frame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

class BaseUnitTest {

	@SuppressWarnings("serial")
	protected StatusBar bar = new StatusBar(null) {
		public void enableProgress(String msg) {

		};

		public void disableProgress() {

		};
	};

	protected static File tmp = new File("test/tmp");

	protected JarTree jarTree;

	protected File test;

	protected boolean equals(List<JarNode> left, List<JarNode> right) {
		if (left.size() != right.size()) {
			return false;
		}
		for (JarNode l : left) {
			boolean isExist = false;
			for (JarNode r : right) {
				if (equals(l, r)) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				return false;
			}
		}
		return true;
	}
	
	private boolean equals(JarNode l, JarNode r) {
		List<JarNode> left = l.getPathList();
		List<JarNode> right = r.getPathList();
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
	
	protected void bypass(JarNode node, List<JarNode> data) {
		data.add(node);
		for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
			bypass((JarNode) children.nextElement(), data);
		}
	}
	
	protected List<JarNode> getContent(JarTree tree) {
		List<JarNode> res = new ArrayList<JarNode>();
		bypass(jarTree.getRoot(), res);
		return res;
	}
	
	protected static JarNode getNode(JarTree tree, String path) {
		JarNode node = tree.getRoot();
		String[] pth = path.split("/");
		if (pth.length == 0) {
			return node;
		}
		for (int i = 0; i < pth.length; ++i) {
			if (node.name.equals(pth[i]) && i == pth.length - 1) {
				return node;
			}
			String nextName = pth[i];
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				JarNode child = (JarNode) children.nextElement();
				if (child.name.equals(nextName)) {
					node = child;
					break;
				}
			}
			if (i == pth.length - 1) {
				return node;
			}
		}
		return null;
	}
	
	protected static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				try {
					delete(c);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!f.delete()) {
			throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}

	protected void xor(List<JarNode> contentBefore, List<JarNode> contentAfter) {
		List<JarNode> res = new ArrayList<JarNode>();
		for (Iterator<JarNode> b = contentBefore.iterator(); b.hasNext();) {
			JarNode nodeB = b.next();
			for (Iterator<JarNode> a = contentAfter.iterator(); a.hasNext();) {
				JarNode nodeA = a.next();
				if (equals(nodeA, nodeB)) {
					res.add(nodeA);
					break;
				}
			}
		}
		for (JarNode n : res) {
			for (JarNode x : contentBefore) {
				if (equals(n, x)) {
					contentBefore.remove(x);
					break;
				}
			}
		}
		for (JarNode n : res) {
			for (JarNode x : contentAfter) {
				if (equals(n, x)) {
					contentAfter.remove(x);
					break;
				}
			}
		}

	}
	
}
