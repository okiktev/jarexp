package com.delfin.jarexp.frame.search;

import java.io.File;

import javax.swing.JComboBox;

import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.utils.Zip;

abstract class AbstractSearcher implements Searcher {

	protected boolean isMatchCase;

	protected boolean isInAll;

	protected String fullSearchPath;

	private SearchDlg searchDlg;

	protected SearchEntries searchEntries;

	protected boolean isSearchInWhole;

	@Override
	public void search(SearchDlg searchDlg) {
		this.searchDlg = searchDlg;
		searchEntries = searchDlg.searchEntries;
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
	}

	protected boolean isNotAllowedToSearch(String parent, String path, String pathInJar) {
		if (isSearchInWhole) {
			return pathInJar != null && !path.startsWith(pathInJar);
		}
		boolean pathIsArchive = Zip.isArchive(path.toLowerCase(), true);
		String currentFullPath = getCurrentFullPath(parent, path, pathInJar, pathIsArchive);
		if (!currentFullPath.startsWith(fullSearchPath)) {
			boolean isForbidden = true;
			if (pathIsArchive && fullSearchPath.indexOf('!') != -1) {
				for (String pathPart : fullSearchPath.split("!")) {
					if (currentFullPath.startsWith(pathPart)) {
						isForbidden = false;
						break;
					}
				}
			}
			if (isForbidden) {
				return true;
			}
		}
		return false;
	}

	protected String getCurrentFullPath(String parent, String path, String pathInJar) {
		boolean pathIsArchive = Zip.isArchive(path.toLowerCase(), true);
		return getCurrentFullPath(parent, path, pathInJar, pathIsArchive);
	}

	private String getCurrentFullPath(String parent, String path, String pathInJar, boolean pathIsArchive) {
		String currentFullPath = getFullPath(parent, path);
		if (pathInJar != null && pathIsArchive) {
			currentFullPath = currentFullPath + '!';
		}
		return currentFullPath;
	}

	protected String getFullPath(String parent, String path) {
		return parent + '/' + path;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void manageSearchHistory() {
		if (searchDlg.isFindClass == null) {
			return;
		}
		JComboBox cb = searchDlg.cbFind;
		ActionHistory.addSearch((String) cb.getSelectedItem());
		cb.removeAllItems();
		for (String token : ActionHistory.getSearchTokens()) {
			cb.addItem(token);
		}
		cb.setSelectedIndex(0);
	}

	protected abstract void search(String parent, File searchRoot, Object results, SearchDlg dlg, String pathInJar);

}
