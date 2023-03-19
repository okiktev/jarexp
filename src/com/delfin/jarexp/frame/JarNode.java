package com.delfin.jarexp.frame;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.analyzer.IJavaItem;
import com.delfin.jarexp.analyzer.IJavaItem.Position;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Enumerator;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.StreamProcessor;
import com.delfin.jarexp.win.exe.PE;
import com.delfin.jarexp.win.icon.Ico;


abstract class  Node extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -8073373592062367648L;

	abstract String getName();
	abstract String getFullPath();
	abstract Icon getIcon(String fullPath, boolean singleFileLoaded);
	abstract Node getSelectedChild();
	abstract void setSelectedChild(Node node);

}

class JarNode extends Node {

	private static final long serialVersionUID = 7129831719595479526L;

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

	private Node selectedChild;

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

	@Override
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

	@Override
	String getName() {
		return name;
	}

	@Override
	Node getSelectedChild() {
		return selectedChild;
	}

	@Override
	void setSelectedChild(Node node) {
		selectedChild = node;
	}

	Icon getIcon(boolean isSingleFileLoaded) {
		return getIcon(null, isSingleFileLoaded);
	}

	@Override
	Icon getIcon(final String fullPath, boolean isSingleFileLoaded) {
		if (isDirectory) {
			Resources.getIconForDir(); 
		}
		try {
			if (StringUtils.endsWith(name, ".exe")) {
				return getFromStore(fullPath == null ? getFullPath() : fullPath, new ResourceLoader(isSingleFileLoaded) {
					@Override
					void loadSingleFileResources(List<BufferedImage> icons) throws IOException {
						InputStream is = new ByteArrayInputStream(PE.getFirstIcon(getTempArchive()));
						try {
							icons.addAll(Ico.read(is));
						} finally {
							is.close();
						}
					}
					@Override
					void loadNonSingleFileResources(final List<BufferedImage> icons) {
						Zip.stream(getTempArchive(), path, new StreamProcessor() {
							@Override
							public void process(InputStream stream) throws IOException {
								InputStream is = new ByteArrayInputStream(PE.getFirstIcon(stream));
								try {
									icons.addAll(Ico.read(is));
								} finally {
									is.close();
								}
							}
						});
					}
				});
			}
			if (StringUtils.endsWith(name, ".ico")) {
				return getFromStore(fullPath == null ? getFullPath() : fullPath, new ResourceLoader(isSingleFileLoaded) {
					@Override
					void loadSingleFileResources(List<BufferedImage> icons) throws IOException {
						icons.addAll(Ico.read(getTempArchive()));
					}
					@Override
					void loadNonSingleFileResources(final List<BufferedImage> icons) {
						Zip.stream(getTempArchive(), path, new StreamProcessor() {
							@Override
							public void process(InputStream stream) throws IOException {
								icons.addAll(Ico.read(stream));
							}
						});
					}
				});
			}
		} catch (IOException e) {
			throw new JarexpException("An error occurred while reading icon from " + getTempArchive(), e);
		}
		return Resources.getIconFor(name);
	}

	private static Icon adaptSize(List<BufferedImage> icons) {
		BufferedImage image = icons.get(0);
		for (int i = 1; i < icons.size(); ++i) {
			BufferedImage img = icons.get(i);
			if (image.getHeight() > img.getHeight()) {
				image = img;
			}
		}
		if (image.getHeight() > 16) {
			image = scale(image, 16, 16);
		}
		return new ImageIcon(image);
	}

	private static BufferedImage scale(BufferedImage src, int w, int h) {
	    Image tmp = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();
	    return dimg;
	}

	private static Icon getFromStore(String fullPath, ResourceLoader resourceLoader) throws IOException {
		Resources resources = Resources.getInstance();
		Icon icon = resources.getPeIcon(fullPath);
		if (icon != null) {
			return icon;
		} else if (resources.isPeIconChecked(fullPath)) {
			return null;
		}
		List<BufferedImage> icons = resourceLoader.load();
		if (icons == null || icons.size() == 0) {
			resources.storePeIcon(fullPath, null);
		}
		return resources.storePeIcon(fullPath, adaptSize(icons));
	}

	static class ClassItemNode extends Node {

		private static final long serialVersionUID = 7470246996120563613L;

		private IJavaItem javaItem;

		protected String name;

		ClassItemNode() {
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
		String getFullPath() {
			return "";
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		String getName() {
			return name;
		}

		@Override
		Icon getIcon(String fullPath, boolean singleFileLoaded) {
			return javaItem.getIcon();
		}

		@Override
		Node getSelectedChild() {
			return null;
		}

		@Override
		void setSelectedChild(Node node) {
		}

	}

	static class PeNode extends ClassItemNode {

		private static final long serialVersionUID = 3944805067597550882L;

		private String fullPath;

		JarNode parent;

		PeNode(JarNode parent, String name) {
			this.parent = parent;
			this.name = name;
			fullPath = parent.getFullPath() + "!/" + name;
		}

		@Override
		String getFullPath() {
			return fullPath;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		Icon getIcon(String fullPath, boolean isSingleFileLoaded) {
			try {
				return getFromStore(getFullPath(), new ResourceLoader(isSingleFileLoaded) {
					@Override
					void loadSingleFileResources(List<BufferedImage> icons) throws IOException {
						InputStream is = new ByteArrayInputStream(
								PE.getIcon(parent.getTempArchive(), name.replace(".ico", "")));
						try {
							icons.addAll(Ico.read(is));
						} finally {
							is.close();
						}
					}
					@Override
					void loadNonSingleFileResources(final List<BufferedImage> icons) {
						Zip.stream(parent.getTempArchive(), parent.path, new StreamProcessor() {
							@Override
							public void process(InputStream stream) throws IOException {
								InputStream is = new ByteArrayInputStream(
										PE.getIcon(stream, name.replace(".ico", "")));
								try {
									icons.addAll(Ico.read(is));
								} finally {
									is.close();
								}
							}
						});
					}
				});
			} catch (IOException e) {
				throw new JarexpException("An error occurred while reading icon from " + parent.getTempArchive(), e);
			}
		}

	}

	private abstract static class ResourceLoader {
		private boolean isSingleFileLoaded;
		ResourceLoader(boolean isSingleFileLoaded) {
			this.isSingleFileLoaded = isSingleFileLoaded;
		}
		List<BufferedImage> load() throws IOException {
			List<BufferedImage> icons = new ArrayList<BufferedImage>();
			if (isSingleFileLoaded) {
				loadSingleFileResources(icons);
			} else {
				loadNonSingleFileResources(icons);
			}
			return icons;
		}
		abstract void loadNonSingleFileResources(List<BufferedImage> icons);
		abstract void loadSingleFileResources(List<BufferedImage> icons) throws IOException;
	}

}
