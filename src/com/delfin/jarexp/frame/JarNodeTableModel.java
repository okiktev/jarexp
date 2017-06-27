package com.delfin.jarexp.frame;

import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

class JarNodeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2259978843988964737L;

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-Y HH:mm");

	private String[] columnNames = {"Name", "Size", "Compressed Size", 
			"Time", "Last Modified Time", "Creation Time", "Last Access Time",
			"Method", "Comment", "Attributes", "Certificates", "Code Signers", "CRC",
    		"Extra"};

    private Object[][] data;

	JarNodeTableModel(JarNode node) {
		data = new Object[node.getChildCount()][];

		int i = 0;
		for (Enumeration<?> children = node.children(); children.hasMoreElements(); ++i) {
			data[i] = parseNode((JarNode) children.nextElement());
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

	private static String formatMethod(int method) {
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

	private static String formatTime(FileTime time) {
		return time == null ? null : formatTime(time.toMillis());
	}

	private static String formatTime(long time) {
		return DATE_FORMAT.format(new Date(time));
	}

	private static String formatExtra(byte[] data) {
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
		if (!node.isLeaf() || node.isDirectory) {
			long ls = 0;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				ls += calcSize(0, (JarNode) children.nextElement());
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
		if (!node.isLeaf() || node.isDirectory) {
			long ls = 0;
			for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
				ls += calcCompSize(0, (JarNode) children.nextElement());
			}
			return ls + s;
		} else {
			return s + node.compSize;
		}
	}

	private static Object[] parseNode(JarNode n) {
		long size = calcSize(0, n);
		long compSize = calcCompSize(0, n);

		return new Object[] {n.name, size, compSize,
				formatTime(n.time), formatTime(n.lastModTime), formatTime(n.creationTime), formatTime(n.lastAccessTime), 
				formatMethod(n.method), n.comment, n.attrs, n.certs, n.signers, Long.toString(n.crc, 16),
				formatExtra(n.extra)};
	}
}


