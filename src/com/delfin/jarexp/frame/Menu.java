
package com.delfin.jarexp.frame;

import static com.delfin.jarexp.Version.JAVA_MAJOR_VER;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;

class Menu extends JMenuBar {

	private static final long serialVersionUID = 6283256126265026307L;

	private static final Resources resources = Resources.getInstance();

	Menu(ActionListener openListener, ActionListener searchListener, ActionListener duplicatesListener,
			ActionListener jdCoreListener, ActionListener procyonListener, ActionListener environmentListener, ActionListener aboutListener)
			throws ResourcesException {

		JMenuItem item;

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		item = new JMenuItem("Open", KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		item.addActionListener(openListener);
		item.setIcon(resources.getOpenIcon());
		file.add(item);
		file.addSeparator();
		item = new JMenuItem("Exit", KeyEvent.VK_E);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.runFinalization();
				System.exit(0);
			}
		});
		item.setIcon(resources.getExitIcon());
		file.add(item);
		add(file);

		JMenu tools = new JMenu("Tools");
		tools.setMnemonic(KeyEvent.VK_T);
		item = new JMenuItem("Search", KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		item.addActionListener(searchListener);
		item.setIcon(resources.getSearchIcon());
		tools.add(item);
		item = new JMenuItem("Duplicates", KeyEvent.VK_D);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		item.addActionListener(duplicatesListener);
		item.setIcon(resources.getDuplicatesIcon());
		tools.add(item);
		add(tools);

		JMenu decompilers = new JMenu("Decompilers");
		tools.setMnemonic(KeyEvent.VK_D);
		JRadioButtonMenuItem rbJdCoreItem = new JRadioButtonMenuItem("JD-Core", resources.getJdCoreIcon());
		rbJdCoreItem.setMnemonic(KeyEvent.VK_C);
		rbJdCoreItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		rbJdCoreItem.addActionListener(jdCoreListener);
		JRadioButtonMenuItem rbProcyonItem = new JRadioButtonMenuItem("Procyon", resources.getProcyonIcon());
		rbProcyonItem.setMnemonic(KeyEvent.VK_P);
		rbProcyonItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		rbProcyonItem.addActionListener(procyonListener);
		enableProcyonDecompileOption(rbProcyonItem);
		ButtonGroup group = new ButtonGroup();
		group.add(rbJdCoreItem);
		group.add(rbProcyonItem);
		switch (Settings.getDecompilerType()) {
		case PROCYON:
			rbProcyonItem.setSelected(true);
			break;
		default:
			rbJdCoreItem.setSelected(true);
		}
		decompilers.add(rbJdCoreItem);
		decompilers.add(rbProcyonItem);
		add(decompilers);

		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		item = new JMenuItem("Java environment", KeyEvent.VK_E);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		item.setIcon(resources.getEnvironmentIcon());
		item.addActionListener(environmentListener);
		help.add(item);
		item = new JMenuItem("About", KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		item.setIcon(resources.getInfoIcon());
		item.addActionListener(aboutListener);
		help.add(item);
		add(help);
	}

	private void enableProcyonDecompileOption(JRadioButtonMenuItem rbProcyonItem) {
		rbProcyonItem.setEnabled(JAVA_MAJOR_VER >= 7);
	}

}