package com.delfin.jarexp.frame.search;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

class FileSearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2838930607781355845L;

	private List<SearchResult> data;

	FileSearchResultTableModel() {
		this.data = new ArrayList<SearchResult>();
	}

	FileSearchResultTableModel(List<SearchResult> data) {
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
		return data.get(rowIndex);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (Object result : data) {
			out.append(result).append('\n');
		}
		return out.toString();
	}

}
