package com.delfin.jarexp.frame;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.settings.Version;

class JarNodeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2259978843988964737L;

	private static final Logger log = Logger.getLogger(JarNodeTableModel.class.getCanonicalName());

	static final DateFormat TIME_FORMAT = new SimpleDateFormat(
			isJava6() ? "dd-MMM-yyyy HH:mm:ss.S" : "dd-MMM-Y HH:mm:ss.S");

	private String[] columnNames = { "Name", "Size", "Compressed Size", "Time", "Last Modified Time", "Creation Time",
			"Last Access Time", "Method", "Comment", "Attributes", "Certificates", "Code Signers", "CRC", "Extra" };

	private Object[][] data;

	JarNodeTableModel(JarNode node, StatusBar statusBar) {
		int count = node.getChildCount();
		data = new Object[count][];
		statusBar.setChildren(Integer.toString(count));

		int i = 0;
		for (Enumeration<?> children = node.children(); children.hasMoreElements(); ++i) {
			data[i] = parseNode((JarNode) children.nextElement());
		}
		optimize();
	}

	private void optimize() {
		try {
			List<Integer> columnsToRemove = new ArrayList<Integer>();
			for (int i = 0; i < columnNames.length; ++i) {
				int j = 0;
				while (j < data.length && data[j][i] == null) {
					++j;
				}
				if (j == data.length) {
					columnsToRemove.add(i);
				}
			}
			int newLength = columnNames.length - columnsToRemove.size();
			String[] newColumnNames = new String[newLength];
			Object[][] newData = new Object[data.length][newLength];
			int i = 0;
			for (int j = 0; j < columnNames.length; ++j) {
				if (columnsToRemove.contains(j)) {
					continue;
				}
				for (int k = 0; k < data.length; ++k) {
					newData[k][i] = data[k][j];
				}
				newColumnNames[i] = columnNames[j];
				++i;
			}
			columnNames = newColumnNames;
			data = newData;
		} catch (Exception e) {
			log.log(Level.SEVERE, "An error while optimizing directory table.", e);
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 1:
		case 2:
			return Long.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getRowCount() {
		return data.length;
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
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {

	}

	@Override
	public void removeTableModelListener(TableModelListener l) {

	}

	static String formatMethod(int method) {
		switch (method) {
		case 0:
			return "Store";
		case 8:
			return "Deflate";
		case -1:
			return null;
		default:
			return Integer.toString(method);
		}
	}

	static String formatTime(Object time) {
		return isJava6() || time == null ? null : formatTime(((java.nio.file.attribute.FileTime) time).toMillis());
	}

	static String formatTime(long time) {
		return TIME_FORMAT.format(new Date(time));
	}

	static String formatExtra(byte[] data) {
		if (data == null) {
			return null;
		}
		StringBuilder out = new StringBuilder(data.length * 2);
		for (int i = 0; i < data.length; ++i) {
			out.append(Byte.toString(data[i]));
			if (i != data.length - 1) {
				out.append(',');
			}
		}
		return out.toString();
	}

	private static long calcSize(long s, JarNode node) {
		if (node.isArchive()) {
			return node.size;
		}
		if (node.isNotClass() && (!node.isLeaf() || node.isDirectory)) {
			long ls = 0;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				Object child = children.nextElement();
				if (!(child instanceof JarNode)) {
					continue;
				}
				ls += calcSize(0, (JarNode) child);
			}
			return ls + s;
		} else {
			return s + node.size;
		}
	}

	private static long calcCompSize(long s, JarNode node) {
		if (node.isArchive()) {
			return node.compSize;
		}
		if (node.isNotClass() && (!node.isLeaf() || node.isDirectory)) {
			long ls = 0;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				Object child = children.nextElement();
				if (!(child instanceof JarNode)) {
					continue;
				}
				ls += calcCompSize(0, (JarNode) child);
			}
			return ls + s;
		} else {
			return s + node.compSize;
		}
	}

	private static Object[] parseNode(JarNode n) {
		long size = calcSize(0, n);
		long compSize = calcCompSize(0, n);

		return new Object[] { n.name, size, compSize, formatTime(n.time), formatTime(n.lastModTime),
				formatTime(n.creationTime), formatTime(n.lastAccessTime), formatMethod(n.method), n.comment,
				formatAttributes(n.attrs), n.certs, n.signers, Long.toString(n.crc, 16), formatExtra(n.extra) };
	}

	static String formatAttributes(Attributes attrs) {
		if (attrs == null) {
			return null;
		}
		Set<Entry<Object, Object>> entries = attrs.entrySet();
		StringBuilder out = new StringBuilder();
		int i = 0;
		int size = entries.size();
		for (Entry<Object, Object> entry : entries) {
			out.append(entry.getKey()).append('=').append(entry.getValue());
			if (i != size - 1) {
				out.append(',');
			}
			++i;
		}
		return out.toString();
	}

	private static boolean isJava6() {
		return Version.JAVA_MAJOR_VER == 6;
	}
}
