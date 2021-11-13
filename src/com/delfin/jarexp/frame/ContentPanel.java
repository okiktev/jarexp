package com.delfin.jarexp.frame;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import com.delfin.jarexp.settings.Settings;

class ContentPanel extends JPanel {

	private static final long serialVersionUID = -4853614180239352039L;

	private Component content;

	private ContentPanel() {
		super(new BorderLayout());
		setBorder(Settings.EMPTY_BORDER);
	}

	ContentPanel(Component content) {
		this();
		add(this.content = content);
	}

	public void replaceContentBy(Component content) {
		removeAll();
		add(this.content = content);
	}

	public void showFilterPanel(FilterPanel filter) {
		removeAll();
		add(filter, BorderLayout.NORTH);
		add(this.content, BorderLayout.CENTER);
	}

	public void removeFilter() {
		removeAll();
		add(this.content);
	}

}