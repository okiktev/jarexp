
package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;

class Menu extends JMenuBar {

	private static final long serialVersionUID = 6283256126265026307L;

	Menu(ActionListener openListener, ActionListener aboutListener) throws ResourcesException {

		JMenuItem menuItem;

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menuItem = new JMenuItem("Open", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(openListener);
		menuItem.setIcon(Resources.getInstance().getOpenIcon());
		file.add(menuItem);
		file.addSeparator();
		menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.runFinalization();
				System.exit(0);
			}
		});
		menuItem.setIcon(Resources.getInstance().getExitIcon());
		file.add(menuItem);
		add(file);

		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		menuItem = new JMenuItem("About", KeyEvent.VK_A);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuItem.setIcon(Resources.getInstance().getInfoIcon());
		menuItem.addActionListener(aboutListener);
		help.add(menuItem);
		add(help);

	}

}