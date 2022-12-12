package com.delfin.jarexp.frame.about;

import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;

public class EnvironmentDlg extends JDialog {

	private static final long serialVersionUID = -3568763789661658045L;

	private JTable tEnv = new JTable();
	private JTable tProps = new JTable();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private JLabel lProps = new JLabel("Java System Properties:");
	private JLabel lVarbl = new JLabel("Environment Variables:");
	private JButton btnResultToFile = new JButton();
	private JButton btnResultToClipboard = new JButton();
	private Set<Entry<Object, Object>> props = System.getProperties().entrySet();
	private Set<Entry<String, String>> env = System.getenv().entrySet();

	public EnvironmentDlg(Component parent) {
		super((JDialog) null);
		setTitle("Java Environment");
		setIconImage(Resources.getInstance().getEnvironmentImage());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setSize(DLG_DIM);
		setPreferredSize(DLG_DIM);

		initComponents();
		alignComponents();

		Msg.centerDlg(this, DLG_DIM);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());

		panel.add(lProps, new GridBagConstraints(0, 0, 1, 1, 1, 1, WEST, NONE, insets, 0, 0));
		panel.add(btnResultToFile,      new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 5, 5, 25), 0, 0));
		panel.add(btnResultToClipboard, new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 5, 5), 0, 0));

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

		btnResultToClipboard.setFont(DLG_TEXT_FONT);
		btnResultToClipboard.setBorder(Settings.EMPTY_BORDER);
		btnResultToClipboard.setIcon(Resources.getInstance().getCopyIcon());
		btnResultToClipboard.setToolTipText("Copy Java environment to clipboard");
		btnResultToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(EnvironmentDlg.this.toString()), null);			
			}
		});

		btnResultToFile.setFont(DLG_TEXT_FONT);
		btnResultToFile.setBorder(Settings.EMPTY_BORDER);
		btnResultToFile.setIcon(Resources.getInstance().getFloppyIcon());
		btnResultToFile.setToolTipText("Save Java environment to file");
		btnResultToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select file to save Java environment");
				List<File> dirs = ActionHistory.getLastDirSelected();
				if (!dirs.isEmpty()) {
					chooser.setCurrentDirectory(dirs.get(0));
				}
				if (chooser.showOpenDialog(EnvironmentDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.exists()) {
						showMessageDialog(EnvironmentDlg.this, "Specified file exists.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (f.isDirectory()) {
						showMessageDialog(EnvironmentDlg.this, "Specified file is a directory.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						try {
							FileUtils.toFile(f, EnvironmentDlg.this.toString());
						} catch (IOException ex) {
							Msg.showException("Could not dump Java environment into the file " + f, ex);
						}
					}
				}
			}
		});

		initEnvironments();
		initProperties();
	}

	private void initProperties() {
		tProps.setModel(new EnvironmentTableModel<Object, Object>(props));
		tProps.setAutoCreateRowSorter(true);
	}

	private void initEnvironments() {
		tEnv.setModel(new EnvironmentTableModel<String, String>(env));
		tEnv.setAutoCreateRowSorter(true);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder(lProps.getText()).append('\n');
		for (Entry<Object, Object> entry : props) {
			out.append(entry.getKey()).append(':').append(' ').append(entry.getValue()).append('\n');
		}
		out.append(lVarbl.getText()).append('\n');
		for (Entry<String, String> entry : env) {
			out.append(entry.getKey()).append(':').append(' ').append(entry.getValue()).append('\n');
		}
		return out.toString();
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
