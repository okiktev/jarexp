package com.delfin.jarexp.frame.search;

import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;

import com.delfin.jarexp.ActionHistory;
import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;

public abstract class SearchDlg extends JDialog {

	private static final long serialVersionUID = -2473586208850553553L;

	public static class SearchEntries implements Iterable<SearchEntries> {

		final File archive;
		final String path;
		final String fullPath;
		final boolean isDirectory;

		private List<SearchEntries> entries = new ArrayList<SearchEntries>();

		private SearchEntries(File archive, String path, String fullPath, boolean isDirectory) {
			this.archive = archive;
			this.path = path;
			this.fullPath = fullPath;
			this.isDirectory = isDirectory;
		}

		public SearchEntries() {
			archive = null;
			path = fullPath = null;
			isDirectory = false;
		}

		public void add(File archive, String path, String fullPath, boolean isDirectory) {
			for (Iterator<SearchEntries> it = iterator(); it.hasNext();) {
				String fp = it.next().fullPath;
				if (fullPath.equals(fp) || fullPath.startsWith(fp)) {
					return;
				}
				if (fp.startsWith(fullPath)) {
					it.remove();
					break;
				}
			}
			entries.add(new SearchEntries(archive, path, fullPath, isDirectory));
		}

		@Override
		public Iterator<SearchEntries> iterator() {
			return entries.iterator();
		}

		public int size() {
			return entries.size();
		}
	}

	private JLabel lbSearchIn = new JLabel("Search In:");
	protected JTextField tfSearchIn = new JTextField();
	private JButton btnChangePlace = new JButton("Browse");
	protected JCheckBox cbMatchCase = new JCheckBox("Match case");
	protected JCheckBox cbInAllSubArchives = new JCheckBox("In all sub-archives");
	protected JLabel lbResult = new JLabel("Result");
	private JLabel lbFind = new JLabel("Find what:");
	protected JTextField tfFind = new JTextField();
	private JButton btnFind = new JButton("Find");
	private JRadioButton rbClass = new JRadioButton("Find File");
	private JRadioButton rbInFiles = new JRadioButton("Find in Files");
	protected JTable tResult = new JTable();
	protected JScrollPane spResult = new JScrollPane(tResult);
	private JLabel lbFileFilter = new JLabel("File Filter:");
	protected JTextField tfFileFilter = new JTextField("!.png,!.jpeg,!.jpg,!.bmp,!.gif,!.ico,!.exe");

	protected boolean isFindClass = true;
	protected SearchEntries searchEntries;

	private ChangeListener setFocusOnInput = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					tfFind.grabFocus();
					tfFind.requestFocus();
				}
			});
		}
	};

	public SearchDlg(SearchEntries searchEntries) {
		super();
		this.searchEntries = searchEntries;

		initComponents();
		alignComponents();

		setTitle("Search");
		initLocation();
		setIconImage(Resources.getInstance().getSearchImage());
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

		add(rbClass,   new GridBagConstraints(0, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(rbInFiles, new GridBagConstraints(1, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));

		add(cbMatchCase,        new GridBagConstraints(0, 2, 1, 1, 0, 0, NORTH, NONE, insets, 0, 0));
		add(cbInAllSubArchives, new GridBagConstraints(1, 2, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));

		add(lbFileFilter,       new GridBagConstraints(0, 3, 1, 1, 0, 0, EAST, NONE, insets, 0, 0));
		add(tfFileFilter,       new GridBagConstraints(1, 3, 2, 1, 0, 0, NORTH, BOTH, new Insets(0, 5, 5, 5), 0, 0));

		add(lbFind, new GridBagConstraints(0, 4, 1, 1, 0, 0, EAST, NONE, insets, 0, 0));
		add(tfFind, new GridBagConstraints(1, 4, 1, 1, 1, 0, NORTH, BOTH, new Insets(0, 5, 0, 0), 0, 0));
		add(btnFind, new GridBagConstraints(2, 4, 1, 1, 0, 0, EAST, NONE, new Insets(0, 5, 0, 5), 0, 0));

		add(lbResult, new GridBagConstraints(0, 5, 3, 1, 1, 0, WEST, HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		add(spResult, new GridBagConstraints(0, 6, 3, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
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

	protected void initComponents() {
		rbClass.setFont(DLG_TEXT_FONT);
		rbInFiles.setFont(DLG_TEXT_FONT);
		cbMatchCase.setFont(DLG_TEXT_FONT);
		cbInAllSubArchives.setFont(DLG_TEXT_FONT);
		lbFileFilter.setFont(DLG_TEXT_FONT);
		tfFileFilter.setFont(DLG_TEXT_FONT);
		lbFind.setFont(DLG_TEXT_FONT);
		tfFind.setFont(DLG_TEXT_FONT);
		btnFind.setFont(DLG_TEXT_FONT);
		lbResult.setFont(DLG_TEXT_FONT);
		lbSearchIn.setFont(DLG_TEXT_FONT);
		tfSearchIn.setFont(DLG_TEXT_FONT);
		btnChangePlace.setFont(DLG_TEXT_FONT);

		tfSearchIn.setEditable(false);

		btnChangePlace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setDialogTitle("Select archive or directory to search");
				FileFilter filter = new FileNameExtensionFilter("Jar Files (*.jar,*.war,*.ear,*.zip,*.apk)", "jar", "war", "ear", "zip", "apk");
				chooser.addChoosableFileFilter(filter);
				chooser.setFileFilter(filter);

				List<File> dirs = ActionHistory.getLastDirSelected();
				if (!dirs.isEmpty()) {
					chooser.setCurrentDirectory(dirs.get(0));
				}

				if (chooser.showOpenDialog(SearchDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (!f.exists()) {
						showMessageDialog(SearchDlg.this, "Specified file does not exist.", "Wrong input", ERROR_MESSAGE);
					}
					if (!f.isDirectory() && !Zip.isArchive(f.getName())) {
						showMessageDialog(SearchDlg.this, "Specified file is not archive.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						SearchDlg.this.searchEntries = new SearchEntries();
						SearchDlg.this.searchEntries.add(f, null, f.getAbsolutePath(), f.isDirectory());
						initLocation();
						makeVisibleHide();
					}
				}
			}
		});

		cbMatchCase.setMnemonic(KeyEvent.VK_M);
		cbMatchCase.addChangeListener(setFocusOnInput);

		cbInAllSubArchives.setMnemonic(KeyEvent.VK_A);
		cbInAllSubArchives.addChangeListener(setFocusOnInput);

		btnFind.setMnemonic(KeyEvent.VK_ENTER);
		btnFind.addActionListener(new OnFindBtnClickListener(this));
		btnFind.setMinimumSize(btnChangePlace.getPreferredSize());
		
		rbClass.setMnemonic(KeyEvent.VK_C);
		rbClass.addChangeListener(setFocusOnInput);
		rbClass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isFindClass = true;
				makeVisibleHide();
				repaint();
			}
		});
		rbClass.setSelected(true);

		rbInFiles.setMnemonic(KeyEvent.VK_F);
		rbInFiles.addChangeListener(setFocusOnInput);
		rbInFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isFindClass = false;
				makeVisibleHide();
				repaint();
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(rbClass);
		group.add(rbInFiles);

		tResult.setTableHeader(null);
		tResult.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 5962009671528344075L;
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				SearchResult searchResult = (SearchResult) value;
				if (!isSelected) {
					int pos = searchResult.position;
					if (pos >= 0) {
						component.setBackground(SearchResult.COLOR_CONTENT);
					} else if (pos == -1) {
						component.setBackground(Color.WHITE);
					} else if (pos == -2) {
						component.setBackground(SearchResult.COLOR_ERROR);
					}
				}
				setValue(searchResult.line);
				return component;
			}
		});

		makeVisibleHide();
		getRootPane().setDefaultButton(btnFind);
	}

	private void makeVisibleHide() {
		rbClass.setEnabled(true);
		cbInAllSubArchives.setEnabled(true);
		if (searchEntries.size() == 1) {
			SearchEntries entry = searchEntries.iterator().next();
			String path = entry.path == null || entry.path.isEmpty() ? entry.archive.getName() : entry.path;
			if (!entry.isDirectory && !Zip.isArchive(path.toLowerCase(), true)) {
				rbClass.setEnabled(false);
				cbInAllSubArchives.setEnabled(false);
				rbInFiles.setSelected(true);
				isFindClass = false;
			}
		}
		lbFileFilter.setVisible(!isFindClass);
		tfFileFilter.setVisible(!isFindClass);
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new SearchDlg(new SearchEntries()) {
			private static final long serialVersionUID = -2883424465981010979L;
		};
	}

}
