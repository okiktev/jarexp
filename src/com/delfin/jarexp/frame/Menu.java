
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
	
	Menu(ActionListener openListener, ActionListener searchListener, ActionListener duplicatesListener, ActionListener aboutListener) throws ResourcesException {

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
		
		JMenu tools = new JMenu("Tools");
		tools.setMnemonic(KeyEvent.VK_T);
		menuItem = new JMenuItem("Search", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuItem.addActionListener(searchListener);
		menuItem.setIcon(Resources.getInstance().getSearchIcon());
		tools.add(menuItem);
		menuItem = new JMenuItem("Duplicates", KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuItem.addActionListener(duplicatesListener);
		menuItem.setIcon(Resources.getInstance().getDuplicatesIcon());
		tools.add(menuItem);
		add(tools);
		

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