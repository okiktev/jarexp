package com.delfin.jarexp.dlg.message;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.DecompilierMenuItems;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;

class DecompilerException extends Exception {

	private static final long serialVersionUID = -7017111429925382606L;
	
	private final int width = 500;

	private final int height = 250;

	private final int stHeight = 200;

	private boolean isShown;

	DecompilerException(String errMsg, Throwable e) {
		setIconImage(Resources.getInstance().getLogoImage());

		setModal(true);

		setTitle("Error");
		setSize(width, height);
		Msg.centerDlg(this, width, height);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		Insets zeroInsets = new Insets(0, 0, 0, 0);

		JTextArea area = new JTextArea(toString(e));
		area.setFont(Msg.defaultFont);
		EmptyBorder emptyBoder = new EmptyBorder(zeroInsets);
		area.setBorder(emptyBoder);
		area.setOpaque(false);
		final JScrollPane pane = new JScrollPane(area);
		pane.setPreferredSize(new Dimension(width, stHeight));
		pane.setBorder(emptyBoder);

		final ButtonGroup group = new ButtonGroup();	
		Resources res = Resources.getInstance();
		final JIconRadioButton rbJdCore = new JIconRadioButton(res.getJdCoreIcon(),"JD-Core", group);
		final JIconRadioButton rbProcyon = new JIconRadioButton(res.getProcyonIcon(),"Procyon", group);
		final JIconRadioButton rbFernflower = new JIconRadioButton(res.getFernflowerIcon(),"Fernflower", group);
		switch (Settings.getDecompilerType()) {
		case PROCYON:
			rbProcyon.setEnabled(false);
			rbFernflower.setSelected(true);
			break;
		case FERNFLOWER:
			rbFernflower.setEnabled(false);
			rbJdCore.setSelected(true);
			break;
		default:
			rbJdCore.setEnabled(false);
			rbProcyon.setSelected(true);
			break;
		}

		JButton okButton = new JButton("OK");
		final JButton viewButton = new JButton("View Error");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		buttonPanel.add(viewButton);

		JLabel lbErrorMsg = new JLabel("<html>" + errMsg + "</html>");
		JLabel lbOptional = new JLabel("Also you can try one of the other decompilers:");

		setLayout(new GridBagLayout());

		add(getIconLabel(), new GridBagConstraints(0, 0, 1, 1, 0.01, 0, WEST, NONE, zeroInsets, 0, 0));
		add(lbErrorMsg, 	new GridBagConstraints(1, 0, 1, 1, 1, 0, WEST, HORIZONTAL, zeroInsets, 0, 0));

		add(lbOptional, 	new GridBagConstraints(0, 1, 2, 1, 0, 0, WEST, HORIZONTAL, new Insets(-10, 100, 0, 0), 0, 0));

		add(rbJdCore,       new GridBagConstraints(0, 2, 2, 1, 0, 0, WEST, NONE, new Insets(0, 100, 0, 0), 0, 0));

		add(rbProcyon,      new GridBagConstraints(0, 3, 2, 1, 0, 0, WEST, NONE, new Insets(-7, 100, 0, 0), 0, 0));

		add(rbFernflower,   new GridBagConstraints(0, 4, 2, 1, 0, 0, WEST, NONE, new Insets(-7, 100, 0, 0), 0, 0));

		add(pane,           new GridBagConstraints(0, 5, 2, 1, 1, 1, WEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));

		add(buttonPanel,    new GridBagConstraints(0, 6, 2, 1, 0, 0.01, SOUTHEAST, NONE, zeroInsets, 0, 0));

		pane.setVisible(false);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							throw new JarexpException(e);
						}
						if (rbJdCore.isSelected()) {
							DecompilierMenuItems.jdCore.doClick();
						} else if (rbProcyon.isSelected()) {
							DecompilierMenuItems.procyon.doClick();
						} else if (rbFernflower.isSelected()) {
							DecompilierMenuItems.fernflower.doClick();
						}
					}
				}).start();
			}
		});

		viewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isShown) {
					viewButton.setText("View Error");
					pane.setVisible(false);
					DecompilerException.this.setSize(width, height);
					isShown = false;
				} else {
					viewButton.setText("Hide Error");
					pane.setVisible(true);
					DecompilerException.this.setSize(width, height + stHeight);
					isShown = true;
				}
			}
		});

		setVisible(true);
		pack();
	}

	private static class JIconRadioButton extends JPanel {

		private static final long serialVersionUID = -3559387566975198517L;

		private JRadioButton radio = new JRadioButton();
		private JLabel image;
		private JLabel text;
		private boolean isEnabled = true;

		public JIconRadioButton(Icon icon, String text, ButtonGroup group) {
		    add(radio);
		    add(image = new JLabel(icon));
		    add(this.text = new JLabel(text));
		    addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				@Override
				public void mousePressed(MouseEvent e) {
					if (isEnabled) {
						radio.setSelected(true);
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
				}
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
		    group.add(radio);
		}

		public boolean isSelected() {
			return radio.isSelected();
		}

		public void setSelected(boolean b) {
			radio.setSelected(true);
		}

		@Override
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
			if (!isEnabled) {
				radio.setSelected(false);
			}
			radio.setEnabled(isEnabled);
			image.setEnabled(isEnabled);
			text.setEnabled(isEnabled);
		}

	}

}
