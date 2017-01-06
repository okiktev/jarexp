package com.delfin.jarexp.frame.about;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;


class General extends JPanel {

    private static final long serialVersionUID = 1043794882699118537L;

	General() {
		setLayout(new GridBagLayout());

		JLabel version = new JLabel("Version " + Settings.getInstance().getVersion());
		JLabel author = new JLabel("E-mail dsite@bk.ru");
		JLabel copyright = new JLabel("Copyright \u00a9 @COPYRIGHT@ delfin. All rights reserved.");
		version.setFont(Dialog.textFont);
		author.setFont(Dialog.textFont);
		copyright.setFont(Dialog.textFont);
		
		add(getAppName(), 
				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 10, 0, 0), 0, 0));
		add(new JLabel(new ImageIcon(Resources.getInstance().getLogoImage())), 
				new GridBagConstraints(1, 0, 1, 2, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5, 20, 0, 10), 0, 0));
		add(version, 
				new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
		add(getAbout(), 
				new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
		add(author,
				new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
		add(copyright,
				new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
	}
	
	private static JLabel getAppName() {
		JLabel appNameLb = new JLabel("Jar Explorer");
		Font f = Dialog.textFont;
		appNameLb.setFont(new Font(f.getName(), f.getStyle(), 21));
		appNameLb.setForeground(Color.DARK_GRAY);
		return appNameLb;
	}
	
	private static JLabel getAbout() {
		JLabel lb = new JLabel("<html>The Jar Explorer allows to browse content of Java library files known as JARs.");
		lb.setFont(Dialog.textFont);
		return lb;
	}
	
}
