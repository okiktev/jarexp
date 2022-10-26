package com.delfin.jarexp.frame.about;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;


public class AboutDlg extends JDialog {
	
	private static final long serialVersionUID = -6727320015620479794L;

    public AboutDlg(Component parent) {
	    super();
	    setModal(true);
		setTitle("About Jar Explorer");
		setIconImage(Resources.getInstance().getInfoImage());
		setResizable(false);
		setSize(Settings.DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new GridBagLayout());
		setLocationRelativeTo(parent);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General", null, new General(), "General panel");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_G);
		tabbedPane.addTab("License", null, new License(), "License information");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_N);
		tabbedPane.addTab("RSyntaxTextArea license", null, new RstaLicense(), "RSyntaxTextArea license");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_R);
		tabbedPane.addTab("Procyon license", null, new ProcyonLicense(), "Procyon license");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_P);
		tabbedPane.addTab("Fernflower license", null, new FernflowerLicense(), "Fernflower license");
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_F);
		tabbedPane.addTab("JavaDecompiler license", null, new JavaDecompilerLicense(), "JavaDecompiler license");
		tabbedPane.setMnemonicAt(5, KeyEvent.VK_J);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		add(tabbedPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		setVisible(true);
		pack();
    }
	
	public static void main(String[] args) {
			
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	    new AboutDlg(null);
    }
	
}
