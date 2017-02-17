package com.delfin.jarexp.frame;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import com.delfin.jarexp.Settings;

class ContentPanel extends JPanel {

	private static final long serialVersionUID = -4853614180239352039L;

	private Component content;

	private ContentPanel() {
		super(new BorderLayout());
		setBorder(Settings.EMPTY_BORDER);
	}

	ContentPanel(FilterPanel filter, Component content) {
		this();
		add(filter, BorderLayout.NORTH);
		add(this.content = content, BorderLayout.CENTER);
	}

	ContentPanel(Component content) {
		this();
		add(this.content = content);
	}

	Component getContent() {
		return content;
	}

}