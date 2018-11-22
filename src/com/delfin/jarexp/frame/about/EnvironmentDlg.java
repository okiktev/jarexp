package com.delfin.jarexp.frame.about;

import static com.delfin.jarexp.Settings.DLG_DIM;
import static com.delfin.jarexp.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NORTH;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;

public class EnvironmentDlg extends JDialog {

	private static final long serialVersionUID = -3568763789661658045L;

	private JTable tEnv = new JTable();
	private JTable tProps = new JTable();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private JLabel lProps = new JLabel("Java System Properties:");
	private JLabel lVarbl = new JLabel("Environment Variables:");

	public EnvironmentDlg(Component parent) {
		super();
		setModal(true);
		setTitle("Java Environment");
		setIconImage(Resources.getInstance().getEnvironmentImage());
		setSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);

		initComponents();
		alignComponents();

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());

		panel.add(lProps, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		panel.add(tProps.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
		panel.add(tProps, new GridBagConstraints(0, 2, 1, 1, 0, 0, NORTH, BOTH, insets, 0, 0));

		panel.add(lVarbl, new GridBagConstraints(0, 3, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		panel.add(tEnv.getTableHeader(), new GridBagConstraints(0, 4, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
		panel.add(tEnv, new GridBagConstraints(0, 5, 1, 1, 0, 0, NORTH, BOTH, insets, 0, 0));

		setLayout(new GridBagLayout());
		add(spResult, new GridBagConstraints(0, 0, 0, 0, 1, 1, NORTH, BOTH, insets, 0, 0));
	}

	private void initComponents() {
		Font font = new Font(DLG_TEXT_FONT.getName(), DLG_TEXT_FONT.getStyle(), 16);
		lVarbl.setFont(font);
		lProps.setFont(font);

		initEnvironments();
		initProperties();
	}

	private void initProperties() {
		Set<Entry<Object, Object>> props = System.getProperties().entrySet();
		tProps.setModel(new EnvironmentTableModel<Object, Object>(props));
		tProps.setAutoCreateRowSorter(true);
	}

	private void initEnvironments() {
		Set<Entry<String, String>> env = System.getenv().entrySet();
		tEnv.setModel(new EnvironmentTableModel<String, String>(env));
		tEnv.setAutoCreateRowSorter(true);
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new EnvironmentDlg(null);
	}

}

class EnvironmentTableModel<K, V> extends AbstractTableModel {

	private static final long serialVersionUID = -399941038361771047L;

	private static final String[] columnNames = { "Name", "Value" };

	private final Object[][] data;

	EnvironmentTableModel(Collection<Entry<K, V>> env) {
		data = new Object[env.size()][2];
		int i = 0;
		for (Entry<K, V> entry : env) {
			data[i][0] = entry.getKey();
			data[i][1] = entry.getValue();
			++i;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

}
