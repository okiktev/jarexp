package com.delfin.jarexp.frame.mtool;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Version;

class RepoNodeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7212402470997891769L;

	private static final Logger log = Logger.getLogger(RepoNodeTableModel.class.getCanonicalName());

	static final DateFormat TIME_FORMAT = new SimpleDateFormat(
			isJava6() ? "dd-MMM-yyyy HH:mm:ss.S" : "dd-MMM-Y HH:mm:ss.S");

	private String[] columnNames = { "", "Name", "Size", "Modified"};

	private Object[][] data;

	RepoNodeTableModel(RepoNode node, StatusBar statusBar) {
		int count = node.files.size();
		data = new Object[count][];
		statusBar.setChildren(Integer.toString(count));

		for (int i = 0; i < node.files.size(); ++i) {
			data[i] = parseNode(node.files.get(i));
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
		case 0: return Icon.class;
		case 1: return String.class;
		case 2: return Long.class;
		case 3: return String.class;		
		default:
			throw new JarexpException("Unexpected column index " + columnIndex);
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

	private static String formatTime(long time) {
		return TIME_FORMAT.format(new Date(time));
	}

	private static Object[] parseNode(File f) {
		return new Object[] { Resources.getIconFor(f.getName()), f.getName()
				, f.length(), formatTime(f.lastModified()) };
	}

	private static boolean isJava6() {
		return Version.JAVA_MAJOR_VER == 6;
	}
}
