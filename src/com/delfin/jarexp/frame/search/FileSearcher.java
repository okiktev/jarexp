package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;

class FileSearcher implements Searcher {

	private Pattern pattern;
	
	private final File jarFile;

	private String fileName;

	private boolean isMatchCase;

	private boolean isInAll;

	FileSearcher(File jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public void search(SearchCriteria criteria) {
		final SearchDlg searchDlg = ((FileSearchCriteria) criteria).dlg;
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
		fileName = searchDlg.tfFind.getText();
		if (!isMatchCase) {
			fileName = fileName.toLowerCase();
		}
		pattern = Pattern.compile(fileName.replace(".", "\\.").replace("?", ".?").replace("*", ".*"));
		final List<SearchResult> results = new ArrayList<SearchResult>();
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				search("", jarFile, results, searchDlg);
				long overall = System.currentTimeMillis() - start;
				searchDlg.tResult.setModel(new FileSearchResultTableModel(results));
				searchDlg.lbResult.setText("Result. Found " + results.size() + " results for " + overall + "ms:");
			};
		});
		search.start();
		searchDlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				search.interrupt();
			}
		});
	}

	private void search(final String parent, final File archive, final List<SearchResult> results, final SearchDlg dlg) {

		new Jar(archive) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);
				String fileName = FileUtils.getFileName(path);
				if (isEmptySearch()) {
					if (fileName.lastIndexOf('.') == -1) {
						results.add(new SearchResult(getFullPath(path)));
					}
				} else {
					if (!isMatchCase) {
						fileName = fileName.toLowerCase();
					}
					if (pattern.matcher(fileName).find()) {
						results.add(new SearchResult(getFullPath(path)));
					}
				}
				if (isInAll && Zip.isArchive(path)) {
					File dst = new File(Resources.createTmpDir(), fileName);
					String fullPath = getFullPath(path) + '!';
					dst = Zip.unzip(fullPath, path, archive, dst);
					search(fullPath, dst, results, dlg);
				}
			}

			private String getFullPath(String path) {
				return parent + '/' + path;
			}

		}.bypass();
	}

	private boolean isEmptySearch() {
		return fileName == null || fileName.isEmpty();
	}

}
