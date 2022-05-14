package com.delfin.jarexp.frame.duplicates;

import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.utils.Zip;

public abstract class DuplicatesDlg extends JDialog {

	private static final Logger log = Logger.getLogger(DuplicatesDlg.class.getName());

	private static final long serialVersionUID = 7971966785040198828L;

	private JLabel lbSearchIn = new JLabel("Search In:");
	JTextField tfSearchIn = new JTextField();
	private JButton btnChangePlace = new JButton("Browse");
	
	private JRadioButton rbUseMd5 = new JRadioButton("MD5 check");
	private JRadioButton rbUsePath = new JRadioButton("Path in archive");
	protected JCheckBox cbInAllSubArchives = new JCheckBox("In sub-archives");
	protected JLabel lbResult = new JLabel("Result");
	private JButton btnFind = new JButton("Find");
	protected JDuplicatesScrollTable spResult = new JDuplicatesScrollTable();

	protected boolean isUseMd5 = true;

	protected SearchEntries searchEntries;

	public DuplicatesDlg(SearchEntries searchEntries) throws HeadlessException {
		super((JDialog) null);

		this.searchEntries = searchEntries;
		initLocation();

		initComponents();
		alignComponents();
		
		setTitle("Find duplicates");
		setIconImage(Resources.getInstance().getDuplicatesImage());
		setPreferredSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		setLayout(new GridBagLayout());
		Insets insets = new Insets(0, 0, 0, 0);

		add(lbSearchIn, 	new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 0, 0), 0, 0));
		add(tfSearchIn, 	new GridBagConstraints(1, 0, 1, 1, 1, 0, NORTH, BOTH, new Insets(5, 5, 0, 0), 0, 0));
		add(btnChangePlace, new GridBagConstraints(2, 0, 1, 1, 0, 0, WEST, NONE, new Insets(5, 5, 0, 5), 0, 0));

		add(rbUseMd5, new GridBagConstraints( 0, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(rbUsePath, new GridBagConstraints(1, 1, 1, 1, 1, 0, WEST, NONE, insets, 0, 0));

		add(cbInAllSubArchives, new GridBagConstraints(0, 2, 1, 1, 0, 0, NORTH, NONE, insets, 0, 0));
		add(btnFind, new GridBagConstraints(           2, 2, 1, 1, 0, 0, WEST, NONE, new Insets(0, 5, 0, 5), 0, 0));

		add(lbResult, new GridBagConstraints(0, 3, 3, 1, 1, 0, WEST, NONE, new Insets(5, 5, 5, 0), 0, 0));

 		add(spResult, new GridBagConstraints(0, 4, 3, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
	}

	protected void initComponents() {
		rbUseMd5.setFont(DLG_TEXT_FONT);
		rbUsePath.setFont(DLG_TEXT_FONT);
		cbInAllSubArchives.setFont(DLG_TEXT_FONT);
		btnFind.setFont(DLG_TEXT_FONT);
		lbResult.setFont(DLG_TEXT_FONT);
		tfSearchIn.setFont(DLG_TEXT_FONT);

		tfSearchIn.setEditable(false);		

		btnChangePlace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setDialogTitle("Select archive or directory to search duplicates");
				FileFilter filter = new FileNameExtensionFilter("Jar Files (*.jar,*.war,*.ear,*.zip,*.apk)", "jar", "war", "ear", "zip", "apk");
				chooser.addChoosableFileFilter(filter);
				chooser.setFileFilter(filter);

				File openIn = getOpenIn();
				if (openIn != null) {
					chooser.setCurrentDirectory(openIn);
				}

				if (chooser.showOpenDialog(DuplicatesDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (!f.exists()) {
						showMessageDialog(DuplicatesDlg.this, "Specified file does not exist.", "Wrong input", ERROR_MESSAGE);
					}
					if (!f.isDirectory() && !Zip.isArchive(f.getName())) {
						showMessageDialog(DuplicatesDlg.this, "Specified file is not archive.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						DuplicatesDlg.this.searchEntries.replace(f, null, f.getAbsolutePath(), f.isDirectory());
						initLocation();
						makeVisibleHide();
					}
				}
			}
		});

		rbUseMd5.setMnemonic(KeyEvent.VK_M);
		rbUseMd5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isUseMd5 = true;
				cbInAllSubArchives.setEnabled(true);
			}
		});
		rbUseMd5.setSelected(isUseMd5);

		rbUsePath.setMnemonic(KeyEvent.VK_P);
		rbUsePath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isUseMd5 = false;
				cbInAllSubArchives.setSelected(true);
				cbInAllSubArchives.setEnabled(false);
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(rbUseMd5);
		group.add(rbUsePath);

		cbInAllSubArchives.setMnemonic(KeyEvent.VK_A);

		btnFind.setMnemonic(KeyEvent.VK_ENTER);
		btnFind.addActionListener(new OnFindBtnClickListener(this));

		getRootPane().setDefaultButton(btnFind);
	}

	private void makeVisibleHide() {
		File folderToFind = new File(tfSearchIn.getText());
		if (folderToFind.exists() && folderToFind.isDirectory()) {
			cbInAllSubArchives.setEnabled(false);
			rbUseMd5.setSelected(true);
			rbUsePath.setSelected(false);
			rbUsePath.setEnabled(false);
		}
	}

	private File getOpenIn() {
		try {
			File place = new File(tfSearchIn.getText());
			if(place.exists()) {
				return place.isDirectory() ? place : place.getParentFile();
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to select default path to open directory", e);
		}
		List<File> dirs = ActionHistory.getLastDirSelected();
		return dirs.isEmpty() ? null : dirs.get(0);
	}

	private void initLocation() {
		StringBuilder fullNames = new StringBuilder();
		for (Iterator<SearchEntries> it = searchEntries.iterator();it.hasNext();) {
			fullNames.append(it.next().fullPath);
			if (it.hasNext()) {
				fullNames.append(',').append(' ');
			}
		}
		tfSearchIn.setText(fullNames.toString());
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new DuplicatesDlg(null) {
			private static final long serialVersionUID = 8313293992221834194L;
		};
	}

}