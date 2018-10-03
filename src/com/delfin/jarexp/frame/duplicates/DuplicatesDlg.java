package com.delfin.jarexp.frame.duplicates;

import static com.delfin.jarexp.Settings.DLG_DIM;
import static com.delfin.jarexp.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchResult;

public abstract class DuplicatesDlg extends JDialog {

	private static final long serialVersionUID = 7971966785040198828L;

	private JRadioButton rbUseMd5 = new JRadioButton("MD5 check");
	private JRadioButton rbUsePath = new JRadioButton("Path in archive");
	protected JCheckBox cbInAllSubArchives = new JCheckBox("In all sub-archives");
	protected JLabel lbResult = new JLabel("Result");
	private JButton btnFind = new JButton("Find");
	protected JTable tResult = new JTable();
	protected JScrollPane spResult = new JScrollPane(tResult);

	protected boolean isUseMd5 = true;

	protected final File jarFile;

	public DuplicatesDlg(File jarFile) throws HeadlessException {
		super();

		this.jarFile = jarFile;

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
		add(rbUseMd5, new GridBagConstraints(0, 0, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(rbUsePath, new GridBagConstraints(1, 0, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(cbInAllSubArchives, new GridBagConstraints(0, 1, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		add(btnFind, new GridBagConstraints(2, 1, 1, 1, 0, 0, EAST, NONE, new Insets(0, 5, 0, 5), 0, 0));
		add(lbResult, new GridBagConstraints(0, 2, 3, 1, 1, 0, WEST, HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		add(spResult, new GridBagConstraints(0, 3, 3, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
	}

	protected void initComponents() {
		rbUseMd5.setFont(DLG_TEXT_FONT);
		rbUsePath.setFont(DLG_TEXT_FONT);
		cbInAllSubArchives.setFont(DLG_TEXT_FONT);
		btnFind.setFont(DLG_TEXT_FONT);
		lbResult.setFont(DLG_TEXT_FONT);

		rbUseMd5.setMnemonic(KeyEvent.VK_M);
		rbUseMd5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isUseMd5 = true;
			}
		});
		rbUseMd5.setSelected(isUseMd5);

		rbUsePath.setMnemonic(KeyEvent.VK_P);
		rbUsePath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isUseMd5 = false;
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(rbUseMd5);
		group.add(rbUsePath);

		cbInAllSubArchives.setMnemonic(KeyEvent.VK_A);

		btnFind.setMnemonic(KeyEvent.VK_ENTER);
		btnFind.addActionListener(new OnFindBtnClickListener(this));

		tResult.setTableHeader(null);
		tResult.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 914300240645750561L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				SearchResult searchResult = (SearchResult) value;
				if (!isSelected) {
					int pos = searchResult.position;
					if (pos == 1) {
						component.setBackground(Color.WHITE);
					} else if (pos == 2) {
						component.setBackground(SearchResult.COLOR_CONTENT);
					}
				}
				setValue(searchResult.line);
				return component;
			}
		});

		getRootPane().setDefaultButton(btnFind);
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