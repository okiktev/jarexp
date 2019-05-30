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

class FileSearcher extends AbstractSearcher {

	FileSearcher(File searchRoot) {
		super(searchRoot);
	}

	private Pattern pattern;

	private String fileName;

	@Override
	public void search(SearchCriteria criteria) {
		super.search(criteria);
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
				search("", searchRoot, results, searchDlg);
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

	private void search(String parent, File searchRoot, List<SearchResult> results, SearchDlg dlg) {
		Jar seacher;
		if (searchRoot.isDirectory()) {
			seacher = prepareDirectorySearch(searchRoot, results, dlg);
		} else {
			seacher = prepareArchiveSearch(parent, searchRoot, results, dlg);
		}
		seacher.bypass();
	}

	private Jar prepareArchiveSearch(final String parent, final File archive, final List<SearchResult> results,
			final SearchDlg dlg) {

		return new Jar(archive) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				if (pathInJar != null && !path.startsWith(pathInJar)) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);
				String fileName = FileUtils.getFileName(path);
				if (isEmptySearch()) {
					if (fileName.lastIndexOf('.') == -1) {
						results.add(new SearchResult(getFullPath(parent, path)));
					}
				} else {
					if (!isMatchCase) {
						fileName = fileName.toLowerCase();
					}
					if (pattern.matcher(fileName).find()) {
						results.add(new SearchResult(getFullPath(parent, path)));
					}
				}
				if (isInAll && Zip.isArchive(path)) {
					File dst = new File(Resources.createTmpDir(), fileName);
					String fullPath = getFullPath(parent, path) + '!';
					dst = Zip.unzip(fullPath, path, archive, dst);
					search(fullPath, dst, results, dlg);
				}
			}

		};
	}

	private Jar prepareDirectorySearch(final File root, final List<SearchResult> results, final SearchDlg dlg) {

		return new Directory(root) {
			@Override
			protected void process(File file) throws IOException {
				dlg.lbResult.setText("Searching..." + file);
				String fileName = file.getName();
				if (isEmptySearch()) {
					if (fileName.lastIndexOf('.') == -1) {
						results.add(new SearchResult(file.getAbsolutePath()));
					}
				} else {
					if (!isMatchCase) {
						fileName = fileName.toLowerCase();
					}
					if (pattern.matcher(fileName).find()) {
						results.add(new SearchResult(file.getAbsolutePath()));
					}
				}
				if (isInAll && Zip.isArchive(fileName)) {
					search(file.getAbsolutePath() + '!', file, results, dlg);
				}
			}

		};
	}

	private boolean isEmptySearch() {
		return fileName == null || fileName.isEmpty();
	}

	@Override
	protected SearchDlg extractSearchDlg(SearchCriteria criteria) {
		return ((FileSearchCriteria) criteria).dlg;
	}

}
