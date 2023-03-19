package com.delfin.jarexp.frame;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.frame.ContentPanel.TabComponent;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Zip;

class JTabbedContentPane extends JTabbedPane {

	private static final long serialVersionUID = -8241221682221770296L;

	private ActionListener closeTabListener;

	private LinkedList<String> selectionOrder = new LinkedList<String>();

	private List<TabComponent> tabContent = new LinkedList<TabComponent>();

	private final ContentPanel contentPanel;

	//sometimes we don't want to tree rendering. especially if we trigger this event from tree listener.
	private boolean suppressTreeRendering;

	private boolean suppressTreeCenterAlign;

	private final JarTree jarTree;

	JTabbedContentPane(final JarTree jarTree, final ContentPanel contentPanel) {
		super();
		this.jarTree = jarTree;
		this.closeTabListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fullPath = ((JTabbedContentPane.CloseButton) e.getSource()).fullPath;
				String previousSelected = getPreviousSelected();
				for (Iterator<TabComponent> it = tabContent.iterator(); it.hasNext();) {
					TabComponent tab = it.next();
					if (fullPath.equals(tab.fullPath)) {
						if (tab.isEdited) {
							tab.saveChanges();
						}
						if (tab.node.getName().toLowerCase().endsWith(".class")) {
							tab.node.removeAllChildren();
							jarTree.update(tab.node);
						}
						tab.node.setSelectedChild(null);
						it.remove();
						break;
					}
				}
				for (int i = 0; i < getTabCount(); ++i) {
					if (previousSelected.equals(getToolTipTextAt(i))) {
						setSelectedIndex(i);
						break;
					}
				}
				for (int i = 0; i < getTabCount(); ++i) {
					if (fullPath.equals(getToolTipTextAt(i))) {
						remove(i);
						removeFromSelection(fullPath);
						break;
					}
				}
				if (tabContent.isEmpty()) {
					jarTree.statusBar.empty();
					contentPanel.removeAll();
					contentPanel.repaint();
					JarTreeClickSelection.setNodes(null);
					jarTree.clearSelection();
				}
			}
		};
		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectionOrder.size() > Settings.getInstance().getTabCount()) {
					selectionOrder.removeFirst();
				}
				int indx = ((JTabbedContentPane) e.getSource()).getSelectedIndex();
				if (indx >= 0) {
					String fullPath = getToolTipTextAt(indx);
					selectionOrder.add(fullPath);
					TabComponent selectedTabComponent = getSelectedTabComponent(fullPath);
					if (selectedTabComponent != null) {
						if (suppressTreeRendering) {
							suppressTreeRendering = false;
						} else {
							JarTreeClickSelection.setNodes(null);
							jarTree.isNotDraw = true;
							jarTree.clearSelection();
							jarTree.isNotDraw = true;
							TreePath treePath;
							if (selectedTabComponent.node.getSelectedChild() != null) {
								treePath = new TreePath(selectedTabComponent.node.getSelectedChild().getPath());
							} else {
								treePath = new TreePath(selectedTabComponent.node.getPath());
							}
							jarTree.setSelectionPath(treePath);
							if (suppressTreeCenterAlign) {
								suppressTreeCenterAlign = false;
							} else {								
								FilterPanel.doCenterAlign(jarTree, jarTree.getPathBounds(treePath));
							}
						}
						jarTree.statusBar.empty();
						jarTree.statusBar.setChildren(selectedTabComponent.filesCount);
						jarTree.statusBar.setCompiledVersion(selectedTabComponent.compiledVersion);
						jarTree.statusBar.setPath(selectedTabComponent.fullPath);
						if (selectedTabComponent.name.toLowerCase().endsWith(".class")) {
							jarTree.statusBar.setDecompiler(selectedTabComponent.decompilerType);
						} else {
							jarTree.statusBar.setDecompiler(Settings.getDecompilerType());
						}
						setSelectedIndex(indx);
					}
				}
			}
		});
		this.contentPanel = contentPanel;
	}

	TabComponent getSelectedTabComponent(String fullPath) {
		for (TabComponent c : tabContent) {
			if (fullPath.equals(c.fullPath)) {
				return c;
			}
		}
		return null;
	}

	private String getPreviousSelected() {
		return selectionOrder.getLast();
	}

	private void removeFromSelection(String fullPath) {
		for (Iterator<String> it = selectionOrder.iterator(); it.hasNext();) {
			if (it.next().equals(fullPath)) {
				it.remove();
			}
		}
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
		if (tabContent.size() == Settings.getInstance().getTabCount()) {
			String oldestSelFullPath = getOldestSelected();
			for (int i = 0; i < getTabCount(); ++i) {
				if (oldestSelFullPath.equals(getToolTipTextAt(i))) {
					remove(i);
					removeFromSelection(oldestSelFullPath);
					break;
				}
			}
			for (Iterator<TabComponent> it = tabContent.iterator(); it.hasNext();) {
				if (oldestSelFullPath.equals(it.next().fullPath)) {
					it.remove();
					break;
				}
			}
		}

		super.addTab(title, icon, component, tip);
		setTabComponentAt(getTabCount() - 1, new TabHeader(title, icon, tip));
	}

	private String getOldestSelected() {
		List<String> sorted = new ArrayList<String>();
		for (int i = selectionOrder.size() - 1; i >= 0; --i) {
			String fullPath = selectionOrder.get(i);
			if (!sorted.contains(fullPath)) {
				sorted.add(fullPath);
			}
		}
		return sorted.get(sorted.size() - 1);
	}

	public void insertTab(String title, Icon icon, Component component, FilterPanel filter, String tip, int index) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(Settings.EMPTY_BORDER);
		panel.add(filter, BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);

		super.insertTab(title, icon, panel, tip, index);
		setTabComponentAt(index, new TabHeader(title, icon, tip));
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		setTabComponentAt(index, new TabHeader(title, icon, tip));
	}

	void setSelected(String fullPath, boolean suppressTreeRendering) {
		this.suppressTreeRendering = suppressTreeRendering;
		for (int i = 0; i < getTabCount(); ++i) {
			if (fullPath.equals(getToolTipTextAt(i))) {
				setSelectedIndex(i);
				break;
			}
		}
	}

	void add(TabComponent content) {
		suppressTreeCenterAlign = true;
		if (tabContent.isEmpty()) {
			contentPanel.removeAll();
			contentPanel.add(this);
		}
		TabComponent existent = isContentExist(content);
		if (existent != null) {
			for (int i = 0; i < getTabCount(); ++i) {
				if (content.fullPath.equals(getToolTipTextAt(i))) {
					if (!content.equals(existent)) {
						remove(i);
						existent.overwrite(content);
						insertTab(existent.name, getIconFor(existent), existent.content, existent.fullPath, i);
					}
					setSelectedIndex(i);
					break;
				}
			}
			return;
		}
		checkAndAddToTabs(content);
	}

	void addFilterToSelectedTab(FilterPanel filter) {
		int indx = getSelectedIndex();
		TabComponent tab = getSelectedTabComponent(getToolTipTextAt(indx));
		if (tab.isFiltered || tab.isDirectory || Zip.isArchive(tab.name, true)) {
			return;
		}
		remove(indx);
		insertTab(tab.name, getIconFor(tab), tab.content, filter, tab.fullPath, indx);
		setSelectedIndex(indx);
		tab.isFiltered = true;
	}

	void removeFilterFromSelectedTab() {
		int indx = getSelectedIndex();
		TabComponent tab = getSelectedTabComponent(getToolTipTextAt(indx));
		if (!tab.isFiltered || tab.isDirectory || Zip.isArchive(tab.name, true)) {
			return;
		}
		remove(indx);
		insertTab(tab.name, getIconFor(tab), tab.content, tab.fullPath, indx);
		setSelectedIndex(indx);
		tab.isFiltered = false;
	}

	private Icon getIconFor(TabComponent component) {
		return component.node.getIcon(component.fullPath, jarTree.isSingleFileLoaded());
	}

	private void checkAndAddToTabs(TabComponent content) {
		tabContent.add(content);
		addTab(content.name, getIconFor(content), content.content, content.fullPath);
		setSelectedIndex(getTabCount() - 1);
	}

	private TabComponent isContentExist(TabComponent content) {
		for (TabComponent c : tabContent) {
			if (content.fullPath.equals(c.fullPath)) {
				return c;
			}
		}
		return null;
	}

	private class CloseButton extends JButton {

		private static final long serialVersionUID = 7355839246063570520L;

		String fullPath;

		CloseButton(String fullPath) {
			this.fullPath = fullPath;
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("Close");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					((AbstractButton) e.getComponent()).setBorderPainted(true);
				}

				public void mouseExited(MouseEvent e) {
					((AbstractButton) e.getComponent()).setBorderPainted(false);
				}
			});
			setRolloverEnabled(true);
			addActionListener(closeTabListener);
			setMargin(new Insets(0, 0, 0, 0));
		}

		// we don't want to update UI for this button
		@Override
		public void updateUI() {
		}

		// paint the cross
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.DARK_GRAY);
			if (getModel().isRollover()) {
				g2.setColor(Color.RED);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}

	private class TabHeader extends JPanel {

		private static final long serialVersionUID = 3318723051755874818L;

		TabHeader(String title, Icon icon, String fullPath) {
			setOpaque(false);
			setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
			add(new JLabel(title, icon, LEADING));
			add(new CloseButton(fullPath));
		}
	}

}