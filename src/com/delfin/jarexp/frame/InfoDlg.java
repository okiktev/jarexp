package com.delfin.jarexp.frame;

import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHEAST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

public class InfoDlg extends JDialog {

	private static final long serialVersionUID = 311846921252768204L;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.00");
	private static final DecimalFormat DECIMAL_FORMAT_WITH_COMMA = new DecimalFormat("##,00");
	private static final String[] columnNames = { "Type", "Count" };

	private JTable tTypes = new JTable();
	private JTextArea taResult = new JTextArea();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private final TreePath[] paths;
	private JButton btnResultToFile = new JButton();
	private JButton btnResultToClipboard = new JButton();
	private Map<String, Integer> types;

	InfoDlg(TreePath[] paths) {
		super((JDialog) null);

		this.paths = paths;

		try {
			initComponents();
		} catch (IOException e) {
			Msg.showException("An error occurred while grabbing file data.", e);
		}
		alignComponents();

		setTitle("Information | " + getComaSeparatedFullPaths(paths));
		setIconImage(Resources.getInstance().getInfoImage());
		setPreferredSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setVisible(true);
		pack();
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(taResult.getText()).append('\n');
		if (!types.isEmpty()) {
			String delim = "\t\t|\t\t";
			out.append(columnNames[0]).append(delim).append(columnNames[1]).append('\n');
			for(Entry<String, Integer> entry : types.entrySet()) {
				out.append(entry.getKey()).append(delim).append(entry.getValue()).append('\n');
			}
		}
		return out.toString();
	}

	static String getComaSeparatedFullPaths(TreePath[] paths) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < paths.length; ++i) {
			String fullPath = ((JarNode) paths[i].getLastPathComponent()).getFullPath();
			result.append(fullPath);
			if (i != paths.length - 1) {
				result.append(',');
			}
		}
		return result.toString();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());
		panel.add(taResult, new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTH, HORIZONTAL, insets, 0, 0));
		panel.add(btnResultToFile,      new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTHEAST, NONE, new Insets(5, 5, 5, 25), 0, 0));
		panel.add(btnResultToClipboard, new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTHEAST, NONE, new Insets(5, 0, 5, 5), 0, 0));
		panel.add(tTypes.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 0, 0, NORTH, HORIZONTAL, insets, 0, 0));
		panel.add(tTypes, new GridBagConstraints(0, 2, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		setLayout(new GridBagLayout());
		add(spResult, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		panel.setComponentZOrder(btnResultToClipboard, 0);
		panel.setComponentZOrder(btnResultToFile, 0);
	}

	protected void initComponents() throws IOException {
		tTypes.setFont(DLG_TEXT_FONT);
		taResult.setFont(DLG_TEXT_FONT);

		taResult.setEditable(false);

		if (paths.length == 1) {
			JarNode node = (JarNode) paths[0].getLastPathComponent();
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
		}

		types = grabTypesInside(paths);
		if (!types.isEmpty()) {
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

		btnResultToClipboard.setFont(DLG_TEXT_FONT);
		btnResultToClipboard.setBorder(Settings.EMPTY_BORDER);
		btnResultToClipboard.setIcon(Resources.getInstance().getCopyIcon());
		btnResultToClipboard.setToolTipText("Copy file info to clipboard");
		btnResultToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(InfoDlg.this.toString()), null);			
			}
		});

		btnResultToFile.setFont(DLG_TEXT_FONT);
		btnResultToFile.setBorder(Settings.EMPTY_BORDER);
		btnResultToFile.setIcon(Resources.getInstance().getFloppyIcon());
		btnResultToFile.setToolTipText("Save file info on disk");
		btnResultToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select file to save info");
				List<File> dirs = ActionHistory.getLastDirSelected();
				if (!dirs.isEmpty()) {
					chooser.setCurrentDirectory(dirs.get(0));
				}
				if (chooser.showOpenDialog(InfoDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.exists()) {
						showMessageDialog(InfoDlg.this, "Specified file exists.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (f.isDirectory()) {
						showMessageDialog(InfoDlg.this, "Specified file is a directory.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						try {
							FileUtils.toFile(f, InfoDlg.this.toString());
						} catch (IOException ex) {
							Msg.showException("Could not dump info into the file " + f, ex);
						}
					}
				}
			}
		});
	}

	private static Map<String, Integer> grabTypesInside(TreePath[] paths) throws IOException {
		final Map<String, Integer> types = new HashMap<String, Integer>();
		for (TreePath path : paths) {
			final JarNode node = (JarNode) path.getLastPathComponent();
			if (node.isArchive()) {
				File dst = new File(Resources.createTmpDir(), node.name);
				if (node.getParent() == null) {
					dst = node.origArch;
				} else {
					dst = Zip.unzip(node.getFullPath(), node.path, node.getTempArchive(), dst);
				}
				new Jar(dst) {
					@Override
					protected void process(JarEntry entry) throws IOException {
						if (entry.isDirectory()) {
							return;
						}
						increaseCount(types, entry.getName());
					}
				}.bypass();
			} else if (node.isDirectory) {
				new Jar(node.getTempArchive()) {
					@Override
					protected void process(JarEntry entry) throws IOException {
						if (entry.isDirectory()) {
							return;
						}
						String path = entry.getName();
						if (!path.startsWith(node.path)) {
							return;
						}
						increaseCount(types, path);
					}
				}.bypass();
			}
		}
		return types;
	}

	private static void increaseCount(Map<String, Integer> types, String path) {
		String ext = "";
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

	private static String formatLastModifiedTime(JarNode node) {
		if (node.getParent() != null) {
			return formatTime(node.lastModTime);
		}
		return JarNodeTableModel.formatTime(new File(node.getFullPath()).lastModified());
	}

	private static String getSize(JarNode node) {
		long size = node.getParent() == null ? node.origArch.length() : node.size;
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
		long size = isRoot ? node.origArch.length() : node.size;
		long compressedSize = isRoot ? node.origArch.length() : node.compSize;
		if (size == 0l) {
			return format(compressedSize);
		}
		double ratio = (compressedSize * 100) / (double) size;
		try {
			ratio = Double.valueOf(DECIMAL_FORMAT.format(ratio));
		} catch (NumberFormatException e) {
			try {
				ratio = Double.valueOf(DECIMAL_FORMAT_WITH_COMMA.format(ratio));
			} catch (NumberFormatException ex) {
				ratio = Double.valueOf(ratio);
			}
		}
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
		new InfoDlg(new TreePath[] {});
	}

}
