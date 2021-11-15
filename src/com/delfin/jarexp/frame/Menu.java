
package com.delfin.jarexp.frame;

import static com.delfin.jarexp.settings.Version.JAVA_MAJOR_VER;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Updater;
import com.delfin.jarexp.settings.Version;


class Menu extends JMenuBar {

	private static final long serialVersionUID = 6283256126265026307L;

	private static final Resources resources = Resources.getInstance();

	Menu(ActionListener openListener, ActionListener searchListener, ActionListener duplicatesListener, ActionListener jdCoreListener, 
			ActionListener procyonListener, ActionListener fernflowerListener, ActionListener processesListener, ActionListener environmentListener, ActionListener aboutListener)
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
		if (Version.IS_WINDOWS) {
			item = new JMenuItem("Process Cmd", KeyEvent.VK_R);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			item.addActionListener(processesListener);
			item.setIcon(resources.getProcessesIcon());
			tools.add(item);
		}
		add(tools);

		JMenu decompilers = new JMenu("Decompilers");
		tools.setMnemonic(KeyEvent.VK_D);
		JRadioButtonMenuItem rbJdCoreItem = new JRadioButtonMenuItem("JD-Core", resources.getJdCoreIcon());
		rbJdCoreItem.setMnemonic(KeyEvent.VK_C);
		rbJdCoreItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		rbJdCoreItem.addActionListener(jdCoreListener);
		DecompilierMenuItems.jdCore = rbJdCoreItem;
		JRadioButtonMenuItem rbProcyonItem = new JRadioButtonMenuItem("Procyon", resources.getProcyonIcon());
		rbProcyonItem.setMnemonic(KeyEvent.VK_P);
		rbProcyonItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		rbProcyonItem.addActionListener(procyonListener);
		boolean isProcyonSupported = isProcyonSupported();
		rbProcyonItem.setEnabled(isProcyonSupported);
		rbProcyonItem.setToolTipText(isProcyonSupported ? null : "Unfortunately Procyon decompiler supports Java 7 and higher.");
		DecompilierMenuItems.procyon = rbProcyonItem;
		JRadioButtonMenuItem rbFernflowerItem = new JRadioButtonMenuItem("Fernflower", resources.getFernflowerIcon());
		rbFernflowerItem.setMnemonic(KeyEvent.VK_F);
		rbFernflowerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		rbFernflowerItem.addActionListener(fernflowerListener);
		boolean isFernflowerSupported = isFernflowerSupported();
		rbFernflowerItem.setEnabled(isFernflowerSupported);
		rbFernflowerItem.setToolTipText(isFernflowerSupported ? null : "Unfortunately Fernflower decompiler supports Java 8 and higher.");
		DecompilierMenuItems.fernflower = rbFernflowerItem;
		ButtonGroup group = new ButtonGroup();
		group.add(rbJdCoreItem);
		group.add(rbProcyonItem);
		group.add(rbFernflowerItem);
		switch (Settings.getDecompilerType()) {
		case PROCYON:
			rbProcyonItem.setSelected(true);
			break;
		case FERNFLOWER:
			rbFernflowerItem.setSelected(true);
			break;
		default:
			rbJdCoreItem.setSelected(true);
		}
		decompilers.add(rbJdCoreItem);
		decompilers.add(rbProcyonItem);
		decompilers.add(rbFernflowerItem);
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

		JMenu donate = new JMenu("Donate");
		donate.setIcon(resources.getDonateIcon());
		donate.setToolTipText("Donate to the \"Jar Explorer\" development");
		donate.setVisible(false);
		add(Box.createHorizontalGlue());
		add(donate);
		JMenu update = new JMenu("Update");
		update.setIcon(resources.getUpdateIcon());
		update.setVisible(false);
		add(update);
		new Updater(update, donate);

		setPreferredSize(new Dimension((int)getPreferredSize().getWidth(), 20));
	}

	private boolean isFernflowerSupported() {
		return JAVA_MAJOR_VER >= 8;
	}

	private static boolean isProcyonSupported() {
		return JAVA_MAJOR_VER >= 7;
	}

}