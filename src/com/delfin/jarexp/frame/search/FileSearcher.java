package com.delfin.jarexp.frame.search;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;

class FileSearcher extends AbstractSearcher {

	private Pattern pattern;

	private boolean isEmptySearch;

	@Override
	public void search(final SearchDlg searchDlg) {
		super.search(searchDlg);
		String fileName = (String) searchDlg.cbFind.getSelectedItem();
		if (fileName == null || fileName.isEmpty()) {
			showMessageDialog(searchDlg, "Nothing to search.", "Wrong input", ERROR_MESSAGE);
			return;
		}
		if (!isMatchCase) {
			fileName = fileName.toLowerCase();
		}
		isEmptySearch = fileName == null || fileName.isEmpty();
		pattern = Pattern.compile(fileName.replace(".", "\\.").replace("?", ".?").replace("*", ".*"));
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				manageSearchHistory();
				List<SearchResult> results = new ArrayList<SearchResult>();
				long start = System.currentTimeMillis();
				for (SearchEntries entry : searchEntries) {
					fullSearchPath = entry.fullPath;
					isSearchInWhole = entry.archive.getAbsolutePath().equals(fullSearchPath);
					search("", entry.archive, results, searchDlg, entry.path);
				}
				long overall = System.currentTimeMillis() - start;

				TableModel table = new FileSearchResultTableModel(results);
				searchDlg.tResult.setModel(table);
				searchDlg.lbResult.setText("Result. Found " + results.size() + " results for " + overall + "ms:");
				searchDlg.btnResultToClipboard.setVisible(true);
				searchDlg.btnResultToClipboard.setResult(table);
				searchDlg.btnResultToFile.setVisible(true);
				searchDlg.btnResultToFile.setResult(table);
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

	@SuppressWarnings("unchecked")
	@Override
	protected void search(String parent, File searchRoot, Object results, SearchDlg dlg, String pathInJar) {
		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		Jar searcher;
		if (searchRoot.isDirectory()) {
			searcher = prepareDirectorySearch(searchRoot, (List<SearchResult>)results, dlg);
		} else {
			searcher = prepareArchiveSearch(parent, searchRoot, (List<SearchResult>)results, dlg, pathInJar);
		}
		searcher.bypass();
	}

	private Jar prepareArchiveSearch(final String parent, final File archive, final List<SearchResult> results,
			final SearchDlg dlg, final String pathInJar) {

		return new Jar(archive) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				if (isNotAllowedToSearch(parent, path, pathInJar)) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);
				String fileName = FileUtils.getFileName(path);
				if (isEmptySearch) {
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
				if (isNeedSearchInArchive(path)) {
					File dst = new File(Resources.createTmpDir(), fileName);
					String fullPath = getFullPath(parent, path) + '!';
					dst = Zip.unzip(fullPath, path, archive, dst);
					search(fullPath, dst, results, dlg, null);
				}
			}

			private boolean isNeedSearchInArchive(String path) {
				return fullSearchPath.startsWith(getCurrentFullPath(parent, path, pathInJar)) 
						|| isInAll && Zip.isArchive(path, true);
			}

		};
	}

	private Jar prepareDirectorySearch(final File root, final List<SearchResult> results, final SearchDlg dlg) {

		return new Directory(root) {
			@Override
			protected void process(File file) throws IOException {
				dlg.lbResult.setText("Searching..." + file);
				String fileName = file.getName();
				if (isEmptySearch) {
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
					search(file.getAbsolutePath() + '!', file, results, dlg, null);
				}
			}

		};
	}

}
