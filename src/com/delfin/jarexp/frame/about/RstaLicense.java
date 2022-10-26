package com.delfin.jarexp.frame.about;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;

class RstaLicense extends JPanel {

	private static final long serialVersionUID = 538464150145085703L;

	RstaLicense() {

		JTextArea license = new JTextArea();
		license.setEditable(false);
		license.setOpaque(false);
		license.setText(Resources.getInstance().getRstaLicense());
		license.setFont(Settings.DLG_TEXT_FONT);
		license.setBorder(Settings.EMPTY_BORDER);

		final JScrollPane scrollPane = new JScrollPane(license);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setBorder(Settings.EMPTY_BORDER);
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
