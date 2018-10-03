package com.delfin.jarexp.frame.duplicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.frame.search.SearchResult;

class DuplicatesFileSearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5853035076323644398L;

	private List<SearchResult> data = new ArrayList<SearchResult>();

	DuplicatesFileSearchResultTableModel(Map<String, List<SearchResult>> result) {
		int i = 1;
		for (Entry<String, List<SearchResult>> entry : result.entrySet()) {
			for (SearchResult res : entry.getValue()) {
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
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex);
	}

}
