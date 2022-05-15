package com.delfin.jarexp.frame.duplicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.frame.search.SearchResult;

class DuplicatesFileSearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5853035076323644398L;

	private List<SearchResult> data;

	private boolean isFileSizeRendered;

	DuplicatesFileSearchResultTableModel() {
		data = new ArrayList<SearchResult>();
	}

	DuplicatesFileSearchResultTableModel(Map<String, List<SearchResult>> result) {
		this();
		int i = 1;
		for (Entry<String, List<SearchResult>> entry : result.entrySet()) {
			for (SearchResult res : entry.getValue()) {
				if (!isFileSizeRendered && res instanceof DuplicatesSearchResult) {
					isFileSizeRendered = true;
				}
				res.position = i;
				data.add(res);
			}
			i = i == 1 ? 2 : 1;
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (isFileSizeRendered && columnIndex == 1) {
			return DuplicatesSearchResult.class;
		}
		return Object.class;
	}

	@Override
	public int getColumnCount() {
		return isFileSizeRendered ? 2 : 1;
	}

	@Override
	public String getColumnName(int column) {
		if (isFileSizeRendered) {			
			return column == 0 ? "Path to file" : "File size";
		}
		return null;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex);
	}

	boolean isFileSizeRendered() {
		return isFileSizeRendered;
	}

}
