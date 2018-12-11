package com.delfin.jarexp.frame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;

import com.delfin.jarexp.frame.JarTree.ArchiveLoader;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.TempFileCreator;

public abstract class BaseUnitTest {

	@SuppressWarnings("serial")
	protected StatusBar bar = new StatusBar(null) {
		public void enableProgress(String msg) {
		};

		public void disableProgress() {
		};
	};

	protected static File tmp = new File("test/tmp");

	static {
		Zip.setTempFileCreator(new TempFileCreator() {
			@Override
			public File create(String prefix, String suffix) throws IOException {
				return File.createTempFile(prefix, suffix, tmp);
			}
		});
	}

	protected JarTree jarTree;

	protected File test;

	protected List<JarNode> contentBefore;

	@Before
	public void before() throws IOException {
		clearTmp();
		Zip.reset();
		contentBefore = loadBefore();
	}

	@AfterClass
	public static void clearTmp() throws IOException {
		delete(tmp);
		tmp.mkdirs();
	}

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
		return l.eq(r);
	}

	protected static void bypass(JarNode node, List<JarNode> data) {
		data.add(node);
		for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
			bypass((JarNode) children.nextElement(), data);
		}
	}

	protected static List<JarNode> getContent(JarTree tree) {
		List<JarNode> res = new ArrayList<JarNode>();
		bypass(tree.getRoot(), res);
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
			if (f.exists()) {
				throw new FileNotFoundException("Failed to delete file: " + f);
			}
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

	private List<JarNode> loadBefore() throws IOException {
		jarTree = new JarTree(null, null, null);
		initListener();
		// copy file into temp
		test = File.createTempFile("test", "tmp.ear", tmp);
		FileUtils.copy(new File("test/resources/test.ear"), test);

		// load file
		JarTree.archiveLoader = new ArchiveLoader() {
			@Override
			protected void load(JarNode node) {

				File dst = new File(tmp, node.name);
				dst = Zip.unzip(node.getFullPath(), node.path, node.getTempArchive(), dst);
				try {
					jarTree.addArchive(dst, node);
				} catch (Exception e) {
					throw new RuntimeException("Unable add archive into node", e);
				}
			}
		};
		jarTree.load(test);
		jarTree.update(jarTree.getRoot());
		return getContent(jarTree);
	}

	protected List<JarNode> loadAfter() throws IOException {
		JarTree jarTree = new JarTree(null, null, null);
		jarTree.load(test);
		jarTree.update(jarTree.getRoot());
		return getContent(jarTree);
	}

	protected abstract void initListener();

}
