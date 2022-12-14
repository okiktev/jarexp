package com.delfin.jarexp.frame.mtool;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.delfin.jarexp.frame.resources.CropIconsBugResolver;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Enumerator;

class RepoTree extends JTree {

	private class RepoTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 8362013055456239893L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof RepoNode) {
                RepoNode node = (RepoNode) value;
                if (node.file == null) {
                	return this;
                }
                if (node.isRoot) {
                	setIcon(Resources.getInstance().getMtoolRepoIcon());
                } else {                	
                	setIcon(node.file.isDirectory() 
                			? Resources.getIconForDir() 
                			: Resources.getIconFor(node.file.getName()));
                }
            }
            return this;
        }
    }

	private static final long serialVersionUID = -1336806276319607354L;

	private RepoNode root;

	private DefaultTreeModel model;

	StatusBar statusBar;

	RepoTree(StatusBar statusBar) {

		this.statusBar = statusBar;

		setDragEnabled(true);

		RepoTreeCellRenderer repoTreeCellRenderer = new RepoTreeCellRenderer();
		setCellRenderer(repoTreeCellRenderer);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		CropIconsBugResolver.getInstance().adaptTree(repoTreeCellRenderer, this);
	}

	void load(List<File> repositories) {
		for (int i = 0; i < repositories.size(); ++i) {
			if (i == 0) {
				load(repositories.get(i));
			} else {
				addRepository(repositories.get(i));
			}
		}
	}

	private void load(File repoDir) {
		if (repoDir == null) {
			return;
		}
		if (root == null) {			
			root = new RepoNode();
		}
		RepoNode repoNode = new RepoNode(repoDir, true);

		root.add(repoNode);

		File[] files = repoDir.listFiles();
		if (files != null) {
			for (File f : files) {
				bypass(f, repoNode);
			}
		}
		setModel(model = new DefaultTreeModel(root, false));
		setRootVisible(false);
		setShowsRootHandles(true);
		expandPath(new TreePath(repoNode.getPath()));
	}

	private static void bypass(File dir, RepoNode parent) {
		if (dir.isFile()) {
			parent.add(new RepoNode(dir));
		} else {
			File[] files = dir.listFiles();
			if (files != null) {
				RepoNode par = new RepoNode(dir);
				parent.add(par);
				if (isAllFiles(files)) {
					par.addFiles(files);
				} else {					
					for (File f : files) {
						bypass(f, par);
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	RepoNode addRepository(File repoDir) {
		final RepoNode repoNode = new RepoNode(repoDir, true);
		root.add(repoNode);

		File[] files = repoDir.listFiles();
		if (files != null) {
			for (File f : files) {
				bypass(f, repoNode);
			}
		}
		update(root);
		new Enumerator(root.children()) {
			@Override
			protected void doAction(Object entity) {
				RepoNode node = ((RepoNode) entity);
				TreePath path = new TreePath(node.getPath());
				expandPath(path);
				if (node != repoNode) {
					collapsePath(path);
				}
			}
		};
		return repoNode;
	}

	private static boolean isAllFiles(File[] files) {
		for (File f : files) {
			if (f.isDirectory()) {
				return false;
			}
		}
		return true;
	}

	void update(TreeNode node) {
		model.reload(node);
	}

	void remove(RepoNode node) {
		model.removeNodeFromParent(node);
	}

}
