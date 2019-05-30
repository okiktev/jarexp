package com.delfin.jarexp.frame.search;

import java.io.File;

import com.delfin.jarexp.utils.StringUtils;

abstract class AbstractSearcher implements Searcher {

	protected final File searchRoot;

	protected boolean isMatchCase;

	protected boolean isInAll;

	protected String pathInJar;

	protected String fullSearchPath;

	protected SearchDlg searchDlg;

	AbstractSearcher(File searchRoot) {
		this.searchRoot = searchRoot;
	}

	@Override
	public void search(SearchCriteria criteria) {
		searchDlg = extractSearchDlg(criteria);
		initPathInJar(searchDlg);
		initFullSearchPath(searchDlg);
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
	}

	private void initPathInJar(SearchDlg searchDlg) {
		pathInJar = searchDlg.pathInJar;
		if (StringUtils.isLast(searchDlg.tfSearchIn.getText(), '!')) {
			pathInJar = "";
		}
	}

	private void initFullSearchPath(SearchDlg searchDlg) {
		if (pathInJar == null) {
			return;
		}
		fullSearchPath = searchDlg.tfSearchIn.getText();
		int i = fullSearchPath.lastIndexOf('!');
		if (i != -1) {
			fullSearchPath = fullSearchPath.substring(0, i + 1);
		}
	}

	protected String getFullPath(String parent, String path) {
		String result = "";
		if (pathInJar == null) {
			result = parent + '/' + path;
		} else if (pathInJar.isEmpty()) {
			if (!parent.isEmpty()) {
				result = parent + '/' + path;
			} else {
				result = fullSearchPath + '/' + path;
			}
		} else {
			result = fullSearchPath + '/' + path;
		}
		return result;
	}

	protected abstract SearchDlg extractSearchDlg(SearchCriteria criteria);
}
