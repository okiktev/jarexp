package com.delfin.jarexp.frame.mtool;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTH;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.delfin.jarexp.settings.Settings;

class MtoolPanel extends JPanel {

	private static final long serialVersionUID = -52697160315492022L;

	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

	StatusBar statusBar;
	RepoTree repoTree;
	private MtoolDlg frame;

	MtoolPanel(MtoolDlg frame) {
		this.frame = frame;
		initComponents();
		
		setLayout(new GridBagLayout());
		add(splitPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(statusBar, new GridBagConstraints(0, 1, 1, 1, 1, 0, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}
	
	private void initComponents() {
		statusBar = new StatusBar(this);
		splitPane.setBorder(Settings.EMPTY_BORDER);
		if (!MtoolDlg.repositories.isEmpty()) {
			repoTree = new RepoTree(statusBar);
			repoTree.addTreeSelectionListener(new RepoTreeSelectionListener(repoTree, statusBar, frame));
			repoTree.load(MtoolDlg.repositories);

			JComponent treeView = (JComponent) splitPane.getLeftComponent();
			treeView = new JScrollPane(repoTree);
			treeView.setBorder(Settings.EMPTY_BORDER);
			splitPane.setLeftComponent(treeView);

			JScrollPane contentView = new JScrollPane();
			contentView.setBorder(Settings.EMPTY_BORDER);

			splitPane.setRightComponent(new ContentPanel(contentView, repoTree));
		}
	}

}
