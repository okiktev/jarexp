package com.delfin.jarexp.frame.about;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.delfin.jarexp.frame.Content;
import com.delfin.jarexp.frame.resources.Resources;

class SyntaxLicense extends JPanel {

	private static final long serialVersionUID = 538464150145085703L;

	SyntaxLicense() {

		JTextArea license = new JTextArea();
		license.setEditable(false);
		license.setOpaque(false);
		license.setText(Resources.getInstance().getSyntaxTextLicense());
		license.setFont(Dialog.textFont);
		license.setBorder(Content.emptyBorder);

		JScrollPane scrollPane = new JScrollPane(license);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setBorder(Content.emptyBorder);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				scrollPane.getVerticalScrollBar().setValue(0);
				scrollPane.getHorizontalScrollBar().setValue(0);
			}
		});

		setLayout(new GridBagLayout());

		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}

}
