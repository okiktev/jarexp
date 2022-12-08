
package com.delfin.jarexp.frame.mtool;

import java.awt.Dimension;
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

	private static final Resources resources = Resources.getInstance();

	Menu(ActionListener openListener, ActionListener searchListener, ActionListener duplicatesListener, ActionListener jdCoreListener, 
			ActionListener procyonListener, ActionListener fernflowerListener, ActionListener processesListener, ActionListener environmentListener, ActionListener aboutListener)
			throws ResourcesException {

		JMenuItem item;

		JMenu file = new JMenu("Repository");
		file.setMnemonic(KeyEvent.VK_R);

		item = new JMenuItem("Add", KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		item.addActionListener(openListener);
		item.setIcon(resources.getMtoolAddRepoIcon());
		file.add(item);

		add(file);

		setPreferredSize(new Dimension((int)getPreferredSize().getWidth(), 20));
	}

}