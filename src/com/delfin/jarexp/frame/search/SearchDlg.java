package com.delfin.jarexp.frame.search;

import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.decompiler.IDecompiler;
import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.LibraryManager;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;


public abstract class SearchDlg extends JFrame {

	private static final Logger log = Logger.getLogger(SearchDlg.class.getName());

	private static final long serialVersionUID = -2473586208850553553L;

	private static final Insets CHECKBOX_MARGIN = new Insets(-1, 0, -3 ,0);
	private static final Insets ZERO_PADDING = new Insets(0, 0, 0, 0);

	public static class SearchEntries implements Iterable<SearchEntries> {

		public final String fullPath;

		final File archive;
		final String path;
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

		public String getSearchPath() {
			return entries.iterator().next().fullPath;
		}

		public void replace(File archive, String path, String fullPath, boolean isDirectory) {
			entries.clear();
			entries.add(new SearchEntries(archive, path, fullPath, isDirectory));
		}

	}

	private static final Dimension DLG_DIM = new Dimension(Settings.DLG_DIM.width + 100, Settings.DLG_DIM.height);

	public SearchEntries searchEntries;

	private JLabel lbSearchIn = new JLabel("Search In:");
	protected JTextField tfSearchIn = new JTextField();
	private JButton btnChangePlace = new JButton("Browse");
	protected JCheckBox cbMatchCase = new CheckBox("Match case");
	protected JCheckBox cbInAllSubArchives = new CheckBox("In all sub-archives");
	protected JLabel lbResult = new JLabel("Result");
	private JLabel lbFind = new JLabel("Find what:");
	private JLabel lbIconsFind = new JLabel("Find in:");
	@SuppressWarnings("rawtypes")
	protected JComboBox cbFind = new JComboBox();
	JCheckBox cbIconsInExe = new CheckBox(".EXE");
	JCheckBox cbIconsInDll = new CheckBox(".DLL");
	private JButton btnFind = new JButton("Find");
	private JRadioButton rbClass = new RadioButton("Find File");
	private JRadioButton rbInFiles = new RadioButton("Find in Files");
	private JRadioButton rbIcons = new RadioButton("Find icons");
	protected JTable tResult = new JTable();
	protected ImgBtn btnResultToFile = new ImgButton("Save search result to file", Resources.getInstance().getFloppyIcon());
	protected ImgBtn btnResultToClipboard = new ImgButton("Copy search result to clipboard", Resources.getInstance().getCopyIcon());
	protected JScrollPane spResult = new JScrollPane(tResult);
	private JLabel lbFileFilter = new JLabel("File Filter:");
	protected JTextField tfFileFilter = new JTextField("!.png,!.jpeg,!.jpg,!.bmp,!.gif,!.ico,!.exe");

	@SuppressWarnings("rawtypes")
	JComboBox cbDecompiler = new ComboBox();
	IDecompiler decompiler;

	protected Boolean isFindClass = true;

	private JPanel pIconsIn = group(cbIconsInExe, cbIconsInDll);
	{
		cbIconsInExe.setMargin(CHECKBOX_MARGIN);
		cbIconsInDll.setMargin(CHECKBOX_MARGIN);
		lbIconsFind.setVisible(false);
		cbIconsInExe.setSelected(true);
		pIconsIn.setVisible(false);
	}

	private ChangeListener setFocusOnInput = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					cbFind.grabFocus();
					cbFind.requestFocus();
				}
			});
		}
	};

	public SearchDlg(SearchEntries searchEntries) {
		super();

		this.searchEntries = searchEntries;
		initLocation();

		initComponents();
		alignComponents();

		setTitle("Search");
		setIconImage(Resources.getInstance().getSearchImage());
		setPreferredSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		setLayout(new GridBagLayout());
		Insets insets = new Insets(0, 0, 0, 0);

		add(lbSearchIn, 	new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 0, 0), 0, 0));
		add(tfSearchIn, 	new GridBagConstraints(1, 0, 1, 1, 1, 0, NORTH, BOTH, new Insets(5, 5, 0, 0), 0, 0));
		add(btnChangePlace, new GridBagConstraints(2, 0, 1, 1, 0, 0, WEST, NONE, new Insets(5, 5, 0, 5), 0, 0));

		add(group(rbClass),      new GridBagConstraints(0, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));

		add(group(Version.JAVA_MAJOR_VER >= 7 
				? new JComponent[] {rbInFiles, cbDecompiler, rbIcons} 
				: new JComponent[] {rbInFiles, rbIcons} )
			, new GridBagConstraints(1, 1, 2, 1, 0, 0, WEST, NONE, insets, 0, 0));


		add(group(cbMatchCase, cbInAllSubArchives),      new GridBagConstraints(0, 2, 3, 1, 0, 0, WEST, NONE, insets, 0, 0));

		add(lbFileFilter,       new GridBagConstraints(0, 3, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 0, 0), 0, 0));
		add(tfFileFilter,       new GridBagConstraints(1, 3, 2, 1, 0, 0, NORTH, BOTH, new Insets(0, 5, 5, 5), 0, 0));

		add(lbFind, new GridBagConstraints(0, 4, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 0, 0), 0, 0));		
		add(lbIconsFind, new GridBagConstraints(0, 4, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 0, 0), 0, 0));		
		add(cbFind, new GridBagConstraints(1, 4, 1, 1, 1, 0, NORTH, BOTH, new Insets(0, 5, 0, 0), 0, 0));
		add(pIconsIn, new GridBagConstraints(1, 4, 1, 1, 1, 0, WEST, NONE, insets, 0, 0));
		add(btnFind, new GridBagConstraints(2, 4, 1, 1, 0, 0, WEST, NONE, new Insets(0, 5, 0, 5), 0, 0));

		add(lbResult,             new GridBagConstraints(0, 5, 2, 1, 1, 0, WEST, NONE, new Insets(5, 5, 5, 0), 0, 0));
		add(group(btnResultToFile, btnResultToClipboard),
				new GridBagConstraints(2, 5, 1, 0, 0, 0, NORTH, NONE, ZERO_PADDING, 0, 0));

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
		tfSearchIn.setToolTipText(tfSearchIn.getText());
	}

	@SuppressWarnings("unchecked")
	protected void initComponents() {
		rbClass.setFont(DLG_TEXT_FONT);
		rbInFiles.setFont(DLG_TEXT_FONT);
		rbIcons.setFont(DLG_TEXT_FONT);
		cbDecompiler.setFont(DLG_TEXT_FONT);
		cbMatchCase.setFont(DLG_TEXT_FONT);
		cbInAllSubArchives.setFont(DLG_TEXT_FONT);
		lbFileFilter.setFont(DLG_TEXT_FONT);
		tfFileFilter.setFont(DLG_TEXT_FONT);
		lbFind.setFont(DLG_TEXT_FONT);
		cbFind.setFont(DLG_TEXT_FONT);
		btnFind.setFont(DLG_TEXT_FONT);
		lbResult.setFont(DLG_TEXT_FONT);
		btnResultToFile.setFont(DLG_TEXT_FONT);
		btnResultToClipboard.setFont(DLG_TEXT_FONT);
		lbSearchIn.setFont(DLG_TEXT_FONT);
		tfSearchIn.setFont(DLG_TEXT_FONT);
		btnChangePlace.setFont(DLG_TEXT_FONT);
		cbIconsInDll.setFont(DLG_TEXT_FONT);
		cbIconsInExe.setFont(DLG_TEXT_FONT);
		lbIconsFind.setFont(DLG_TEXT_FONT);

		tfSearchIn.setEditable(false);
		tfSearchIn.setToolTipText(tfSearchIn.getText());
		for (String token : ActionHistory.getSearchTokens()) {
			cbFind.addItem(token);
		}
		cbFind.setEditable(true);
		cbFind.setSelectedIndex(-1);

		btnResultToFile.setContentAreaFilled(false);
		btnResultToClipboard.setContentAreaFilled(false);

		btnChangePlace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setDialogTitle("Select archive or directory to search");
				FileFilter filter = new FileNameExtensionFilter("Jar Files (*.jar,*.war,*.ear,*.zip,*.apk)", "jar", "war", "ear", "zip", "apk");
				chooser.addChoosableFileFilter(filter);
				chooser.setFileFilter(filter);

				File openIn = getOpenIn();
				if (openIn != null) {
					chooser.setCurrentDirectory(openIn);
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
						searchEntries.replace(f, null, f.getAbsolutePath(), f.isDirectory());
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

		rbIcons.setMnemonic(KeyEvent.VK_I);
		rbIcons.addChangeListener(setFocusOnInput);
		rbIcons.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isFindClass = null;
				makeVisibleHide();
				repaint();
			}
		});

		initDecompilerComboBox();

		ButtonGroup group = new ButtonGroup();
		group.add(rbClass);
		group.add(rbInFiles);
		group.add(rbIcons);

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

		btnResultToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select file to save search result");
				File openIn = getOpenIn();
				if (openIn != null) {
					chooser.setCurrentDirectory(openIn);
				}
				if (chooser.showOpenDialog(SearchDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.exists()) {
						showMessageDialog(SearchDlg.this, "Specified file exists.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (f.isDirectory()) {
						showMessageDialog(SearchDlg.this, "Specified file is a directory.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						try {
							FileUtils.toFile(f, btnResultToFile.result.toString());
						} catch (IOException ex) {
							Msg.showException("Could not dump search results into the file " + f, ex);
						}
					}
				}
			}
		});

		btnResultToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(btnResultToClipboard.result.toString()), null);			
			}
		});

		makeVisibleHide();
		getRootPane().setDefaultButton(btnFind);
	}

	@SuppressWarnings("unchecked")
	private void initDecompilerComboBox() {
		cbDecompiler.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (cbDecompiler.getSelectedIndex()) {
				case 0:
					try {
						LibraryManager.prepareBinariesFor(DecompilerType.JDCORE);
						decompiler = Decompiler.get(DecompilerType.JDCORE);
					} catch (Exception ex) {
						Msg.showException("Unable to toggle decompiler", ex);
					}
				break;
				case 1:
					try {
						LibraryManager.prepareBinariesFor(DecompilerType.PROCYON);
						decompiler = Decompiler.get(DecompilerType.PROCYON);
					} catch (Exception ex) {
						Msg.showException("Unable to toggle decompiler", ex);
					}
				break;
				case 2:
					try {
						LibraryManager.prepareBinariesFor(DecompilerType.FERNFLOWER);
						decompiler = Decompiler.get(DecompilerType.FERNFLOWER);
					} catch (Exception ex) {
						Msg.showException("Unable to toggle decompiler", ex);
					}
				break;
				}
			}
		});
		cbDecompiler.setRenderer(new IconListRenderer());
		cbDecompiler.addItem("JdCore");
		cbDecompiler.addItem("Procyon");
		if (Version.JAVA_MAJOR_VER >= 8) {			
			cbDecompiler.addItem("Fernflower");
		}
		cbDecompiler.setVisible(false);
		switch (Settings.getDecompilerType()) {
		case JDCORE: cbDecompiler.setSelectedIndex(0); break;
		case PROCYON: cbDecompiler.setSelectedIndex(1); break;
		case FERNFLOWER: cbDecompiler.setSelectedIndex(2); break;
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

	private void makeVisibleHide() {
		rbClass.setEnabled(true);
		cbInAllSubArchives.setEnabled(true);
		cbIconsInExe.setEnabled(true);
		cbIconsInDll.setEnabled(true);
		rbInFiles.setEnabled(true);
		if (searchEntries.size() == 1) {
			SearchEntries entry = searchEntries.iterator().next();
			String epath = entry.path;
			String path = (epath == null || epath.isEmpty() ? entry.archive.getName() : epath).toLowerCase();
			if (!entry.isDirectory && !Zip.isArchive(path, true)) {
				rbClass.setEnabled(false);
				cbInAllSubArchives.setEnabled(false);
				rbInFiles.setSelected(true);
				isFindClass = false;
			}
			if (path.endsWith(".exe") || path.endsWith(".dll")) {
				isFindClass = null;
				rbInFiles.setSelected(false);
				rbInFiles.setEnabled(false);
				rbIcons.setSelected(true);
				cbInAllSubArchives.setEnabled(false);
				cbInAllSubArchives.setSelected(true);
				boolean isExe = path.endsWith(".exe");
				cbIconsInExe.setEnabled(false);
				cbIconsInExe.setSelected(isExe);
				cbIconsInDll.setEnabled(false);
				cbIconsInDll.setSelected(!isExe);
			}
		}
		lbFileFilter.setVisible(isFindClass != null && !isFindClass);
		tfFileFilter.setVisible(isFindClass != null && !isFindClass);
		cbDecompiler.setVisible(isFindClass != null && !isFindClass);
		cbMatchCase.setVisible(isFindClass != null);
		cbFind.setVisible(isFindClass != null);
		pIconsIn.setVisible(isFindClass == null);
		lbIconsFind.setVisible(isFindClass == null);
		lbFind.setVisible(isFindClass != null);
	}

	private static JPanel group(JComponent...comps) {
		JPanel panel = new JPanel(new FlowLayout(0, 10, 0));
		for (JComponent c : comps) {
			c.getInsets().set(0, 0, 0, 0);
			panel.add(c);
		}
		return panel;
	}

	private static class IconListRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -363503412229224693L;

		private static final Map<Object, Icon> ICONS = new HashMap<Object, Icon>(3);
		static {
			ICONS.put("JdCore", Resources.getInstance().getJdCoreIcon());
			ICONS.put("Procyon", Resources.getInstance().getProcyonIcon());
			ICONS.put("Fernflower", Resources.getInstance().getFernflowerIcon());
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setIcon(ICONS.get(value));
			return label;
		}
	}
	
	private static class CheckBox extends JCheckBox {
		private static final long serialVersionUID = -167729086880469021L;
		CheckBox(String text) {
			super(text);
		}
		@Override
		public Insets getInsets() {
			return ZERO_PADDING;
		}
	}

	private static class RadioButton extends JRadioButton {
		private static final long serialVersionUID = 5356552474097721574L;
		RadioButton(String text) {
			super(text);
		}
		@Override
		public Insets getInsets() {
			return ZERO_PADDING;
		}
	}

	private static class ComboBox extends JComboBox<Object> {
		private static final long serialVersionUID = 6230485454343520493L;
		@Override
		public Insets getInsets() {
			return ZERO_PADDING;
		}
	}
	
	private static class ImgButton extends ImgBtn {
		private static final long serialVersionUID = 6049029429225279535L;
		ImgButton(String text, Icon icon) {
			super(text, icon);
		}
		@Override
		public Insets getInsets() {
			return ZERO_PADDING;
		}
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

class ImgBtn extends JButton {

	private static final long serialVersionUID = 1615780487137910986L;

	Object result;

	ImgBtn(String alt, Icon icon) {
		setIcon(icon);
		setBorder(Settings.EMPTY_BORDER);
		setToolTipText(alt);
		setVisible(false);
	}

	void setResult(Object result) {
		this.result = result;
	}
}
