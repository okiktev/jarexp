package com.delfin.jarexp.frame.search;

import java.util.List;

import javax.swing.table.AbstractTableModel;

class ClassFileSearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2838930607781355845L;

	private List<ClassFileSearcher.SearchResult> data;

	ClassFileSearchResultTableModel(List<ClassFileSearcher.SearchResult> data) {
		this.data = data;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).fullPath;
	}

}
