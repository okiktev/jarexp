package com.delfin.jarexp.frame.search;

import java.io.File;

import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;

abstract class AbstractSearcher implements Searcher {

	protected boolean isMatchCase;

	protected boolean isInAll;

	protected String fullSearchPath;

	protected SearchDlg searchDlg;

	protected SearchEntries searchEntries;

	@Override
	public void search(SearchCriteria criteria) {
		searchDlg = extractSearchDlg(criteria);
		searchEntries = searchDlg.searchEntries;
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
	}

	protected String getFullPath(String parent, String path) {
		return parent + '/' + path;
	}

	protected abstract SearchDlg extractSearchDlg(SearchCriteria criteria);

	protected abstract void search(String parent, File searchRoot, Object results, SearchDlg dlg, String pathInJar);

}
