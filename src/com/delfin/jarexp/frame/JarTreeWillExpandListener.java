package com.delfin.jarexp.frame;

import javax.swing.JSplitPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.frame.ContentPanel.TabComponent;


class JarTreeWillExpandListener implements TreeWillExpandListener {

	private JarTreeSelectionListener jarTreeSelectionListener;

	private JarTree jarTree;

	public JarTreeWillExpandListener(JarTreeSelectionListener jarTreeSelectionListener, JarTree jarTree) {
		this.jarTreeSelectionListener = jarTreeSelectionListener;
		this.jarTree = jarTree;
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		TabComponent tab = getSelectedTab();
		if (tab == null || tab.node == null) {
			return;
		}
		Object[] nodes = event.getPath().getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];
		if (isSelectedTabInNodeExpanded(tab, node)) {
			TreeNode[] treeNodes = tab.node.selectedChild == null 
					? tab.node.getPath() 
					: tab.node.selectedChild.getPath();
			jarTree.setSelectionPath(new TreePath(treeNodes));
		}
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		jarTreeSelectionListener.collapsed = event.getPath();
	}

	private static boolean isSelectedTabInNodeExpanded(TabComponent tab, DefaultMutableTreeNode node) {
		TreeNode[] activeTabPath = tab.node.getPath();
		TreeNode[] expandedPath = node.getPath();
		if (expandedPath.length > activeTabPath.length) {
			return false;
		}
		for (int i = 0; i < expandedPath.length; ++i) {
			if (!expandedPath[i].equals(activeTabPath[i])) {
				return false;
			}
		}
		return true;
	}

	private static TabComponent getSelectedTab() {
		JSplitPane pane = Content.getSplitPane();
		ContentPanel contentPanel = (ContentPanel) pane.getRightComponent();
		return contentPanel.getSelectedTabComponent();
	}

}
