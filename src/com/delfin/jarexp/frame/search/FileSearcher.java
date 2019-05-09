package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;

class FileSearcher implements Searcher {

	private Pattern pattern;

	private final File searchRoot;

	private String fileName;

	private boolean isMatchCase;

	private boolean isInAll;

	FileSearcher(File searchRoot) {
		this.searchRoot = searchRoot;
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

	private void search(final String parent, final File searchRoot, final List<SearchResult> results,
			final SearchDlg dlg) {

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
					String fullPath = file.getAbsolutePath() + '!';
					search(fullPath, file, results, dlg);
				}
			}

		};
	}

	private boolean isEmptySearch() {
		return fileName == null || fileName.isEmpty();
	}

}

abstract class Directory extends Jar {

	public Directory(File file) {
		super(file);
	}

	@Override
	protected void process(JarEntry entry) throws IOException {

	}

	@Override
	public void bypass() {
		try {
			bypass(file);
		} catch (Exception e) {
			throw new JarexpException("An error occurred while bypassing directory " + file, e);
		}
	}

	private void bypass(File file) throws IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					bypass(f);
				}
			}
		} else {
			process(file);
		}
	}

	protected abstract void process(File file) throws IOException;
}