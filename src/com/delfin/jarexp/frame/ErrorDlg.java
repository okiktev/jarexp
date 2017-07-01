package com.delfin.jarexp.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

class ErrorDlg extends JDialog {

	private static final long serialVersionUID = -4584183009890746807L;

	private final int width = 500;

	private final int height = 140;

	private final int stHeight = 200;

	private boolean isShown;

	static void showException(String errMsg, Throwable e) {
		new ErrorDlg(errMsg, e);
	}

	private ErrorDlg(String errMsg, Throwable e) {
		setModal(true);

		setTitle("Error");
		setSize(width, height);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final JScrollPane pane = new JScrollPane(new JTextArea(toString(e)));
		pane.setPreferredSize(new Dimension(width - 50, stHeight));

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(getIconLabel(), BorderLayout.WEST);
		topPanel.add(new JLabel(errMsg));

		add(topPanel);

		final JButton okButton = new JButton("OK");
		final JButton viewButton = new JButton("View Error");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		buttonPanel.add(viewButton);
		add(buttonPanel, BorderLayout.SOUTH);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		viewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isShown) {
					viewButton.setText("View Error");
					topPanel.remove(pane);
					ErrorDlg.this.setSize(width, height);
					isShown = false;
				} else {
					viewButton.setText("Hide Error");
					topPanel.add(pane, BorderLayout.SOUTH);
					ErrorDlg.this.setSize(width, height + stHeight);
					isShown = true;
				}
				topPanel.revalidate();
			}
		});

		setVisible(true);
		pack();
	}

	private static Component getIconLabel() {
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
		iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		return iconLabel;
	}

	private static String toString(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
