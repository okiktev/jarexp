package com.delfin.jarexp.frame.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

class FileContentSearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3746239565967186278L;

	private List<SearchResult> data = new ArrayList<SearchResult>();

	public FileContentSearchResultTableModel(Map<String, List<SearchResult>> result, Map<String, String> errors) {
		for (Entry<String, List<SearchResult>> entry : result.entrySet()) {
			data.add(new SearchResult(entry.getKey()));
			for (SearchResult res : entry.getValue()) {
				data.add(res);
			}
		}
		for (Entry<String, String> entry : errors.entrySet()) {
			data.add(new SearchResult(entry.getKey()));
			data.add(new SearchResult(entry.getValue(), -2));
		}
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

}
