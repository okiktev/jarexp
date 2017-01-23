package com.delfin.jarexp.frame.about;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;


class Notice extends JPanel {

	private static final long serialVersionUID = 538464150145085703L;

	Notice() {
		JTextArea license = new JTextArea();
		license.setEditable(false);
		license.setOpaque(false);
		license.setText(Resources.getInstance().getNoticeText());
		license.setFont(Dialog.textFont);
		license.setBorder(Settings.EMPTY_BORDER);

		JScrollPane scrollPane = new JScrollPane(license);
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
