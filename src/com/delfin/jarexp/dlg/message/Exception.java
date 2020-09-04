package com.delfin.jarexp.dlg.message;

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

import com.delfin.jarexp.frame.resources.Resources;

class Exception extends JDialog {

	private static final long serialVersionUID = -4584183009890746807L;

	private final int width = 500;

	private final int height = 140;

	private final int stHeight = 200;

	private boolean isShown;

	protected Exception() {
	}

	Exception(String errMsg, Throwable e) {
		setIconImage(Resources.getInstance().getLogoImage());

		setModal(true);

		setTitle("Error");
		setSize(width, height);
		Msg.centerDlg(this, width, height);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JTextArea area = new JTextArea(toString(e));
		area.setFont(Msg.defaultFont);
		
		EmptyBorder emptyBoder = new EmptyBorder(new Insets(0, 0, 0, 0));
		area.setBorder(emptyBoder);
		area.setOpaque(false);
		final JScrollPane pane = new JScrollPane(area);
		pane.setPreferredSize(new Dimension(width, stHeight));
		pane.setBorder(emptyBoder);

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(getIconLabel(), BorderLayout.WEST);
		topPanel.add(new JLabel("<html>" + errMsg + "</html>"));

		add(topPanel);

		JButton okButton = new JButton("OK");
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
					Exception.this.setSize(width, height);
					isShown = false;
				} else {
					viewButton.setText("Hide Error");
					topPanel.add(pane, BorderLayout.SOUTH);
					Exception.this.setSize(width, height + stHeight);
					isShown = true;
				}
				topPanel.revalidate();
			}
		});

		setVisible(true);
		pack();
	}

	protected static Component getIconLabel() {
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
		iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		return iconLabel;
	}

	protected static String toString(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
