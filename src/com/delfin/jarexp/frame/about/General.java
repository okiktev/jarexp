package com.delfin.jarexp.frame.about;

import static com.delfin.jarexp.Settings.DLG_TEXT_FONT;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;


class General extends JPanel {

    private static final long serialVersionUID = 1043794882699118537L;

	General() {
		setLayout(new GridBagLayout());

		JLabel version = new JLabel("Version " + Settings.getInstance().getVersion());
		JLabel author = new JLabel("E-mail dsite@bk.ru");
		final JLabel github = new JLabel("<HTML><FONT color=\"#000099\"><U>Link on GitHub project</U></FONT></HTML>");
		JLabel copyright = new JLabel("Copyright \u00a9 @COPYRIGHT@ delfin. All rights reserved.");

		github.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI("https://github.com/okiktev/jarexp"));
					} catch (Exception ex) {
						throw new JarexpException("Could not redirect to github", ex);
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				github.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				github.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});

		version.setFont(DLG_TEXT_FONT);
		author.setFont(DLG_TEXT_FONT);
		github.setFont(DLG_TEXT_FONT);
		copyright.setFont(DLG_TEXT_FONT);
		
		add(getAppName(), 
				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 10, 0, 0), 0, 0));
		add(new JLabel(new ImageIcon(Resources.getInstance().getLogoImage())), 
				new GridBagConstraints(1, 0, 1, 2, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5, 20, 0, 10), 0, 0));
		add(version, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
		add(getAbout(), new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
		add(author, new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
		add(github, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
		add(copyright, new GridBagConstraints(0, 5, 2, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(20, 10, 0, 0), 0, 0));
	}
	
	private static JLabel getAppName() {
		JLabel appNameLb = new JLabel("Jar Explorer");
		appNameLb.setFont(new Font(DLG_TEXT_FONT.getName(), DLG_TEXT_FONT.getStyle(), 21));
		appNameLb.setForeground(Color.DARK_GRAY);
		return appNameLb;
	}
	
	private static JLabel getAbout() {
		JLabel lb = new JLabel("<html>The Jar Explorer allows to browse content of Java library files known as JARs.");
		lb.setFont(DLG_TEXT_FONT);
		return lb;
	}
	
}
