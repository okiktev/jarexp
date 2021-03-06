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

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.Content.SearchResultMouseAdapter;
import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.frame.resources.CropIconsBugResolver;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.settings.Settings;

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

		private final ActionListener searchActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath[] paths = getSelectedPaths();
				SearchEntries entries = new SearchEntries();
				for (TreePath path : paths) {
					JarNode node = ((JarNode) path.getLastPathComponent());
					entries.add(node.origArch, node.path, node.getFullPath(), node.isDirectory);
				}
				new SearchDlg(entries) {
					private static final long serialVersionUID = -2229219000059711983L;

					@Override
					protected void initComponents() {
						super.initComponents();
						tResult.addMouseListener(new SearchResultMouseAdapter(cbFind, tResult));
					};
				};
			}
		};

        private final ActionListener copyPathActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath [] paths = getSelectedPaths();
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(InfoDlg.getComaSeparatedFullPaths(paths)), null);
			}
		};

        private final ActionListener informationActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new InfoDlg(getSelectedPaths());
			}
		};

		private TreePath[] getSelectedPaths() {
			TreePath [] paths = getSelectionPaths();
			if (paths == null) {
				throw new JarexpException("The selection path is null.");
			}
			return paths;
		}

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
				JPopupMenu popupMenu = new JPopupMenu();
				JarNodeMenuItem deleteNode = new JarNodeMenuItem("Delete", path);
				Resources resources = Resources.getInstance();
				deleteNode.setIcon(resources.getDelIcon());
				deleteNode.addActionListener(deleteActionListener);
				JarNodeMenuItem addNode = new JarNodeMenuItem("Add", path);
				addNode.setIcon(resources.getAddIcon());
				addNode.addActionListener(addActionListener);
				JarNodeMenuItem extNode = new JarNodeMenuItem("Extract", path);
				extNode.setIcon(resources.getExtIcon());
				extNode.addActionListener(extractActionListener);
				JarNodeMenuItem unpackNode = new JarNodeMenuItem("Unpack", path);
				unpackNode.setIcon(resources.getUnpackIcon());
				unpackNode.addActionListener(unpackActionListener);

				JarNodeMenuItem search = new JarNodeMenuItem("Search In", path);
				search.setIcon(resources.getSearchIcon());
				search.addActionListener(searchActionListener);

				JarNodeMenuItem copyPath = new JarNodeMenuItem("Copy Path", path);
				copyPath.setIcon(resources.getCopyIcon());
				copyPath.addActionListener(copyPathActionListener);
				JarNodeMenuItem info = new JarNodeMenuItem("Information", path);
				info.setIcon(resources.getInfoIcon());
				info.addActionListener(informationActionListener);

				popupMenu.add(extNode);
				popupMenu.add(addNode);
				popupMenu.add(deleteNode);
				popupMenu.add(unpackNode);
				popupMenu.add(search);
				popupMenu.add(copyPath);
				popupMenu.add(info);
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
                if (paths == null) {
                	paths = new TreePath[] {path};
                }
				if (paths.length > 1) {
					addNode.setEnabled(false);
					unpackNode.setEnabled(false);
				}
				if (paths.length == 1) {
					setSelectionPath(path);
					JarNode node = (JarNode) paths[0].getLastPathComponent();
					if (node.isDirectory) {
						unpackNode.setEnabled(false);
					} else {
						addNode.setEnabled(node.isArchive());
						unpackNode.setEnabled(node.isArchive());
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
			node.add(new JarNode("", Settings.NAME_PLACEHOLDER, null, null, false));
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

	boolean isNotDraw;

	JarTree(TreeExpansionListener treeExpansionListener, StatusBar statusBar, JFrame frame) {
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

	void load(final File origArch) {
		if (origArch == null) {
			root = new JarNode();
			root.origArch = origArch;
			return;
		}
		String fileName = origArch.getName();
		final File tmpArch = new File(Resources.createTmpDir(), fileName);
		root = new JarNode(origArch.getAbsolutePath(), "", tmpArch, origArch, false);

		if (!fileName.toLowerCase().endsWith(".class")) {
	        new Jar(origArch) {
	            @Override
	            protected void process(JarEntry entry) throws IOException {
	            	addIntoNode(entry, root, tmpArch, origArch);
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
			File archive = isArchive ? node.getCurrentArchive() : node.getTempArchive();
			JarNode child = new JarNode(f.getName(), path, archive, node.origArch, isDir);
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
				addIntoNode(entry, node, jar, node.origArch);
			}
		}.bypass();
	}

	boolean isDragging() {
		return isDragging;
	}

	void setDragging(boolean isDragging) {
		this.isDragging = isDragging;
	}

	void expandTreeLeaf(String fullPath) {
		JarNode node = getRoot();
		String [] items = fullPath.split("/");
		for (int i = 0; i < items.length; ++i) {
			String el = items[i];
			if (el.isEmpty()) {
				continue;
			}
			if (i == items.length - 1) {
				expandPath(new TreePath(node.getPath()));
				Enumeration<?> children = node.children();
				while(children.hasMoreElements()) {
					JarNode child = (JarNode) children.nextElement();
					if (child.name.equals(el)) {
						JarTreeClickSelection.setNodes(null);
						TreePath path = new TreePath(child.getPath());
						setSelectionPath(path);
						scrollPathToVisible(path);
						break;
					}
				}
				break;
			}
			boolean isArchive = false;
			if (el.charAt(el.length() - 1) == '!') {
				el = el.replace("!", "");
				isArchive = true;
			}
			Enumeration<?> children = node.children();
			while(children.hasMoreElements()) {
				JarNode child = (JarNode) children.nextElement();
				if (child.name.equals(el)) {
					if (isArchive) {
						expandPath(new TreePath(child.getPath()));
						while (true) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								throw new JarexpException("Error happens while waiting for archive leaf is loaded.", e);
							}
							if (child.children().hasMoreElements() && !Settings.NAME_PLACEHOLDER.equals(((JarNode)child.getLastChild()).name)) {
								break;
							}
						}
					}
					node = child;
					break;
				}
			}
		}
	}

	private static boolean isExist(JarNode node, File file) {
		for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
			if (((JarNode) children.nextElement()).name.equals(file.getName())) {
				return true;
			}
		}
		return false;
	}

	private static void addIntoNode(JarEntry entry, JarNode node, File tempArch, File origArch) throws IOException {
		String path = entry.getName();
		String[] pathToken = path.split("/");
		String name = null;
		for (int i = 0; i < pathToken.length; ++i) {
			boolean isChildExist = false;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				JarNode child = (JarNode) children.nextElement();
				if (child.name.equals(pathToken[i])) {
					node = child;
					isChildExist = true;
					break;
				}
			}
			if (i == pathToken.length - 1) {
				name = isChildExist ? null : pathToken[i];
				break;
			}
			if (!isChildExist) {
				JarNode child = new JarNode(pathToken[i], calcPath(pathToken, i), tempArch, origArch, true);
				child.grab(entry);
				node.add(child);
				node = child;
			}
		}
		if (name != null) {
			JarNode child = new JarNode(name, path, tempArch, origArch, entry.isDirectory());
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
