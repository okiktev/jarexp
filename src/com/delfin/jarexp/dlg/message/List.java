package com.delfin.jarexp.dlg.message;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.delfin.jarexp.frame.resources.Resources;

class List extends JDialog {

	private static final long serialVersionUID = 4206299762749212388L;

	private final int width = 500;

	private final int height = 140;

	private final int stHeight = 140;
	
	private Integer code;

	List(String title, String msg, java.util.List<File> list) {
		setIconImage(Resources.getInstance().getLogoImage());
		setModal(true);

		setTitle(title);
		setSize(width, height + stHeight);
		Msg.centerDlg(this, new Dimension(width, height + stHeight));

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JTextArea area = new JTextArea(toString(list));
		final JScrollPane pane = new JScrollPane(area);
		pane.setPreferredSize(new Dimension(width, stHeight));

		EmptyBorder brd = new EmptyBorder(new Insets(0, 20, 10, 20));
		pane.setBorder(brd);
		area.setBorder(brd);
		area.setOpaque(false);

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(getIconLabel(), BorderLayout.WEST);

		JTextArea label = new JTextArea(msg);
		label.setBorder(new EmptyBorder(new Insets(10, 0, 0, 0)));
		label.setOpaque(false);

		area.setFont(Msg.defaultFont);
		label.setFont(Msg.defaultFont);

		topPanel.add(label);
		topPanel.add(pane, BorderLayout.SOUTH);

		add(topPanel);

		JButton yesBtn = new JButton("Yes");
		JButton noBtn = new JButton("No");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(yesBtn);
		buttonPanel.add(noBtn);
		add(buttonPanel, BorderLayout.SOUTH);

		noBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				code = JOptionPane.NO_OPTION;
				dispose();
			}
		});
		yesBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				code = JOptionPane.YES_OPTION;
				dispose();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				code = JOptionPane.CANCEL_OPTION;
			}
		});

		setVisible(true);
		pack();
	}

	int getCode() {
		return code;
	}

	private static Component getIconLabel() {
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
		iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		return iconLabel;
	}

	private static String toString(java.util.List<File> list) {
		StringBuilder out = new StringBuilder();
		for (Iterator<File> it = list.iterator();it.hasNext();) {
			out.append(it.next().getAbsolutePath());
			if (it.hasNext()) {
				out.append('\n');
			}
		}
		return out.toString();
	}

}
