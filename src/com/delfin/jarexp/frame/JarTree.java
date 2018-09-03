package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.frame.resources.CropIconsBugResolver;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;


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

    static class JarTreeClickSelection {
        private static TreePath[] nodes;

        static TreePath[] getNodes() {
            return nodes;
        }

        static synchronized void setNodes(TreePath[] nodes) {
            JarTreeClickSelection.nodes = nodes;
        }
    }

	class JarTreeMouseListener implements MouseListener {

        private final ActionListener deleteActionListener;

        private final ActionListener addActionListener;

        private final ActionListener extractActionListener;

        private final ActionListener unpackActionListener;

        private final ActionListener copyPathActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		        JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
		        JarNode node = (JarNode) item.path.getLastPathComponent();
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(node.getFullPath()), null);
			}
		};

		JarTreeMouseListener(ActionListener deleteActionListener, ActionListener addActionListener
				, ActionListener extractActionListener, ActionListener unpackActionListener) {
			this.deleteActionListener = deleteActionListener;
			this.addActionListener = addActionListener;
			this.extractActionListener = extractActionListener;
			this.unpackActionListener = unpackActionListener;
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
				JarNodeMenuItem extNode = new JarNodeMenuItem("Extract", path);
				extNode.setIcon(Resources.getInstance().getExtIcon());
				extNode.addActionListener(extractActionListener);
				JarNodeMenuItem unpackNode = new JarNodeMenuItem("Unpack", path);
				unpackNode.setIcon(Resources.getInstance().getUnpackIcon());
				unpackNode.addActionListener(unpackActionListener);
				JarNodeMenuItem copyPath = new JarNodeMenuItem("Copy Path", path);
				copyPath.setIcon(Resources.getInstance().getCopyIcon());
				copyPath.addActionListener(copyPathActionListener);

				popupMenu.add(extNode);
				popupMenu.add(addNode);
				popupMenu.add(deleteNode);
				popupMenu.add(unpackNode);
				popupMenu.add(copyPath);
				popupMenu.show(JarTree.this, e.getX(), e.getY());

                popupMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }
                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    }
                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                        setSelectionPaths(null);
                    }
                });

                TreePath [] paths = getSelectionPaths();
                if (paths.length > 1) {
                    addNode.setEnabled(false);
                    unpackNode.setEnabled(false);
                } if (paths.length == 1) {
                    JarNode node = (JarNode)paths[0].getLastPathComponent();
                    if (!node.isDirectory) {
                        addNode.setEnabled(node.isArchive());
                        unpackNode.setEnabled(node.isArchive());
                    } else {
                        unpackNode.setEnabled(false);
                    }
                }
                deleteNode.setEnabled(!isSingleFileLoaded());
            } 
            JarTreeClickSelection.setNodes(null);
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

	private boolean isSingleFileLoaded;

	StatusBar statusBar;

	JFrame frame;

	JarTree(TreeExpansionListener treeExpansionListener, StatusBar statusBar, JFrame frame) {
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//addTreeSelectionListener(treeSelectionListener);
		addTreeExpansionListener(treeExpansionListener);

		this.statusBar = statusBar;
		this.frame = frame;

		setDragEnabled(true);
		setTransferHandler(new JarTreeNodeTransferHandler(statusBar));
		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new JarTreeDropTargetListener(this, statusBar, frame)));

		addMouseListener(new JarTreeMouseListener(
				new JarTreeDeleteNodeListener(this, statusBar, frame),
				new JarTreeAddNodeListener(this, statusBar, frame), 
				new JarTreeExtractNodeListener(this, statusBar, frame),
				new JarTreeUnpackNodeListener(this, statusBar, frame)
				));
		JarTreeCellRenderer jarTreeCellRenderer = new JarTreeCellRenderer();
		setCellRenderer(jarTreeCellRenderer);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		CropIconsBugResolver.getInstance().adaptTree(jarTreeCellRenderer, this);
	}

	void load(File file) {
		if (file == null) {
			root = new JarNode();
			return;
		}
		final File dst = new File(Resources.createTmpDir(), file.getName());
		FileUtils.copy(file, dst);
		root = new JarNode(file.getAbsolutePath(), "", dst, false);
		
		if (!file.getName().toLowerCase().endsWith(".class")) {
	        new Jar(file) {
	            @Override
	            protected void process(JarEntry entry) throws IOException {
	                addIntoNode(entry, root, dst);
	            }
	        }.bypass();
		} else {
		    isSingleFileLoaded = true;
		}
		setModel(model = new DefaultTreeModel(root, false));
	}

	boolean isSingleFileLoaded() {
	    return isSingleFileLoaded;
	}

	void update(TreeNode node) {
		model.reload(node);
	}

	JarNode getRoot() {
		return root;
	}

	JarNode getNodeByLocation(Point point) {
		TreePath path = getPathForLocation(point.x, point.y);
		return path == null ? null : (JarNode) path.getLastPathComponent();
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
			if (!isExist(node, f)) {
				node.add(child);
			}
			if (isDir) {
				put(child, Arrays.asList(f.listFiles()));
			}
		}
	}

	void addArchive(final File jar, final JarNode node) {
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

	private static boolean isExist(JarNode node, File file) {
		for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
			if (((JarNode) children.nextElement()).name.equals(file.getName())) {
				return true;
			}
		}
		return false;
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
					//child.grab(entry);
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
				child.grab(entry);
				node.add(child);
				node = child;
			}
		}
		if (name != null) {
			JarNode child = new JarNode(name, path, archive, entry.isDirectory());
			child.grab(entry);
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
