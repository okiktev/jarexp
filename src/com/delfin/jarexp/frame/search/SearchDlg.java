package com.delfin.jarexp.frame.search;

import static com.delfin.jarexp.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;

public class SearchDlg extends JDialog {

	private static final Color COLOR_RED = new Color(215, 0, 0);

	private static final Color COLOR_GREEN = new Color(0, 215, 0);

	private static final long serialVersionUID = -5182515718022242086L;

	protected JCheckBox cbMatchCase = new JCheckBox();
	protected JCheckBox cbInAllSubArchives = new JCheckBox();
	protected JLabel lbResult = new JLabel("Result");
	private JLabel lbFind = new JLabel("Find what:");
	protected JTextField tfFind = new JTextField();
	private JButton btnFind = new JButton("Find");
	private JRadioButton rbClass = new JRadioButton("Find Class");
	private JRadioButton rbInFiles = new JRadioButton("Find in Files");
	protected JTable tResult = new JTable();
	protected JScrollPane spResult = new JScrollPane(tResult);
	private JLabel lbFileFilter = new JLabel("File Filter:");
	protected JTextField tfFileFilter = new JTextField("!.png,!.jpeg,!.jpg,!.bmp,!.gif,!.ico,!.exe");

	private final int width = 360;
	private final int height = 290;
	protected boolean isFindClass = true;
	protected final File jarFile;
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

	public SearchDlg(File jarFile) throws HeadlessException {
		super();

		this.jarFile = jarFile;

		initComponents();
		alignComponents();

		setTitle("Search");
		setIconImage(Resources.getInstance().getSearchImage());
		setPreferredSize(new Dimension(width, height));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, width, height);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		setLayout(new GridBagLayout());
		Insets insets = new Insets(0, 0, 0, 0);
		add(rbClass, new GridBagConstraints(0, 0, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(rbInFiles, new GridBagConstraints(1, 0, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(cbMatchCase,        new GridBagConstraints(0, 1, 1, 1, 0, 0, NORTH, NONE, insets, 0, 0));
		add(cbInAllSubArchives, new GridBagConstraints(1, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(lbFileFilter,       new GridBagConstraints(0, 2, 1, 1, 0, 0, EAST, NONE, insets, 0, 0));
		add(tfFileFilter,       new GridBagConstraints(1, 2, 2, 1, 0, 0, NORTH, BOTH, new Insets(0, 5, 5, 5), 0, 0));
		add(lbFind, new GridBagConstraints(0, 3, 1, 1, 0, 0, EAST, NONE, insets, 0, 0));
		add(tfFind, new GridBagConstraints(1, 3, 1, 1, 1, 0, NORTH, BOTH, new Insets(0, 5, 0, 0), 0, 0));
		add(btnFind, new GridBagConstraints(2, 3, 1, 1, 0, 0, EAST, NONE, new Insets(0, 5, 0, 5), 0, 0));
		add(lbResult, new GridBagConstraints(0, 4, 3, 1, 1, 0, WEST, HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		add(spResult, new GridBagConstraints(0, 5, 3, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
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

		cbMatchCase.setText("Match case");
		cbMatchCase.setMnemonic(KeyEvent.VK_M);
		cbMatchCase.addChangeListener(setFocusOnInput);

		cbInAllSubArchives.setText("In all sub-archives");
		cbInAllSubArchives.setMnemonic(KeyEvent.VK_A);
		cbInAllSubArchives.addChangeListener(setFocusOnInput);

		btnFind.setMnemonic(KeyEvent.VK_ENTER);
		btnFind.addActionListener(new OnFindBtnClickListener(this));

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
						component.setBackground(Color.WHITE);
					} else if (pos == -1) {
						component.setBackground(COLOR_GREEN);
					} else if (pos == -2) {
						component.setBackground(COLOR_RED);
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
		new SearchDlg(null);
	}

}
