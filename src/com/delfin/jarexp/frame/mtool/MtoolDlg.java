package com.delfin.jarexp.frame.mtool;

import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;

public class MtoolDlg extends JFrame {

	private static final long serialVersionUID = -7465733990511794369L;

	static List<File> repositories = new ArrayList<File>();
	static {
		File f = new File(Settings.getUserHome(), ".m2/repository");
		if (f.exists()) {			
			repositories.add(f);
		}
	}

	public MtoolDlg() {
		super();

		final MtoolPanel content = new MtoolPanel(this);
		setContentPane(content);

		setTitle("Maven Tool");
		setIconImage(Resources.getInstance().getMtoolLogoImage());
		setPreferredSize(new Dimension(1000, 700));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setJMenuBar(new Menu(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Select path to maven repository");

				File openIn = repositories.isEmpty() ? Settings.getUserHome() : repositories.get(repositories.size() - 1);
				if (openIn != null) {
					chooser.setCurrentDirectory(openIn);
				}

				if (chooser.showOpenDialog(MtoolDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (!f.exists()) {
						showMessageDialog(MtoolDlg.this, "Specified location does not exist.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (!f.isDirectory()) {
						showMessageDialog(MtoolDlg.this, "Specified location is not directory.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					RepoNode root = content.repoTree.addRepository(f);
					content.repoTree.update(root);
					MtoolDlg.this.revalidate();
				}
			}
		}, null, null, null, null, null, null, null, null));

		setVisible(true);
		pack();
	}

	public static void main(String[] args) throws Exception {
		Settings.initLookAndFeel();
		new MtoolDlg().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}