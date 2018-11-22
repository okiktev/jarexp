package com.delfin.jarexp.frame;

import static com.delfin.jarexp.Settings.DLG_TEXT_FONT;
import static com.delfin.jarexp.Settings.DLG_DIM;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NORTH;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;

public class InfoDlg extends JDialog {

	private static final long serialVersionUID = 311846921252768204L;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.00");
	private static final String[] columnNames = { "Type", "Count" };

	private JTable tTypes = new JTable();
	private JTextArea taResult = new JTextArea();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private final JarNode node;

	public InfoDlg(JarNode node) {
		super();

		this.node = node;

		initComponents();
		alignComponents();

		setTitle("Information | " + node.getFullPath());
		setIconImage(Resources.getInstance().getInfoImage());
		setPreferredSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());
		panel.add(taResult, new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTH, HORIZONTAL, insets, 0, 0));
		panel.add(tTypes.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 0, 0, NORTH, HORIZONTAL, insets, 0, 0));
		panel.add(tTypes, new GridBagConstraints(0, 2, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		setLayout(new GridBagLayout());
		add(spResult, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
	}

	protected void initComponents() {
		tTypes.setFont(DLG_TEXT_FONT);
		taResult.setFont(DLG_TEXT_FONT);

		taResult.setEditable(false);

		taResult.append("Name: " + node.name + '\n');
		taResult.append("Path In Jar: " + node.path + '\n');
		taResult.append("Full Path: " + node.getFullPath() + '\n');
		taResult.append("Size: " + getSize(node) + '\n');
		taResult.append("Compressed Size: " + getCompressedSize(node) + '\n');
		taResult.append("CRC: " + format(node.crc) + '\n');
		taResult.append("Method: " + JarNodeTableModel.formatMethod(node.method) + '\n');
		taResult.append("Time: " + JarNodeTableModel.formatTime(node.time) + '\n');
		taResult.append("Creation Time: " + formatTime(node.creationTime) + '\n');
		taResult.append("Last Access Time: " + formatTime(node.lastAccessTime) + '\n');
		taResult.append("Last Modified Time: " + formatLastModifiedTime(node) + '\n');
		taResult.append("Comment: " + format(node.comment) + '\n');
		taResult.append("Atributes: " + (node.attrs == null ? "" : JarNodeTableModel.formatAttributes(node.attrs)) + '\n');
		taResult.append("Certificates: " + (node.certs == null ? "" : Arrays.toString(node.certs)) + '\n');
		taResult.append("Extra: " + (node.extra == null ? "" : JarNodeTableModel.formatExtra(node.extra)) + '\n');
		taResult.append("Code Signers: " + (node.signers == null ? "" : Arrays.toString(node.signers)) + '\n');

		if (node.isArchive()) {
			final Map<String, Integer> types = new HashMap<String, Integer>();
			File dst = new File(Resources.createTmpDir(), node.name);
			if (node.getParent() == null) {
				dst = node.archive;
			} else {
				dst = Zip.unzip(node.getFullPath(), node.path, node.archive, dst);
			}
			new Jar(dst) {
				@Override
				protected void process(JarEntry entry) throws IOException {
					if (entry.isDirectory()) {
						return;
					}
					String ext = "";
					String path = entry.getName();
					int i = path.lastIndexOf('.');
					if (i != -1) {
						ext = path.substring(i);
					}
					Integer count = types.get(ext);
					if (count == null) {
						count = 0;
					}
					types.put(ext, ++count);
				}
			}.bypass();
			taResult.append("File Types:");
			tTypes.setModel(new AbstractTableModel() {

				private static final long serialVersionUID = 776303160021529065L;

				private Object[][] data = new Object[types.size()][2];
				{
					int i = 0;
					for (Entry<String, Integer> entry : types.entrySet()) {
						data[i][0] = entry.getKey();
						data[i][1] = entry.getValue();
						++i;
					}
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 1:
						return Integer.class;
					default:
						return String.class;
					}
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

			});
			tTypes.setAutoCreateRowSorter(true);
		}

	}

	private static String formatLastModifiedTime(JarNode node) {
		if (node.getParent() != null) {
			return formatTime(node.lastModTime);
		}
		return JarNodeTableModel.formatTime(new File(node.getFullPath()).lastModified());
	}

	private static String getSize(JarNode node) {
		long size = node.getParent() == null ? node.archive.length() : node.size;
		long kb = size / 1024;
		if (kb > 0) {
			long mb = kb / 1024;
			if (mb > 0) {
				return size + " (" + mb + ',' + mb % 1024 + "MB)";
			} else {
				return size + " (" + kb + ',' + kb % 1024 + "kB)";
			}
		} else {
			return size + " (" + size + "B)";
		}
	}

	private static String getCompressedSize(JarNode node) {
		boolean isRoot = node.getParent() == null;
		long size = isRoot ? node.archive.length() : node.size;
		long compressedSize = isRoot ? node.archive.length() : node.compSize;
		if (size == 0l) {
			return format(compressedSize);
		}
		double ratio = (compressedSize * 100) / (double) size;
		ratio = Double.valueOf(DECIMAL_FORMAT.format(ratio));
		return format(compressedSize + " (" + ratio + "%)");
	}

	private static String formatTime(Object time) {
		return time == null ? "" : JarNodeTableModel.formatTime(time);
	}

	private static String format(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new InfoDlg(new JarNode());
	}

}
