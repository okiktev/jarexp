package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;


class FileContentSearcher implements Searcher {
	
	private static final Logger log = Logger.getLogger(FileContentSearcher.class.getCanonicalName());

	private static class UnpackResult {
		String fullPath;
		File dst;
		UnpackResult(String fullPath, File dst) {
			this.fullPath = fullPath;
			this.dst = dst;
		}
	}

	private Map<String, List<SearchResult>> searchResult = new LinkedHashMap<String, List<SearchResult>>();

	private List<String> ignores = new ArrayList<String>();

	private List<String> nonIgnores = new ArrayList<String>();

	private Map<String, String> errors = new LinkedHashMap<String, String>();

	private final File jarFile;

	private String statement;

	private boolean isMatchCase;

	private boolean isInAll;

	FileContentSearcher(File jarFile) {
		this.jarFile = jarFile;
	}
	
	@Override
	public void search(SearchCriteria criteria) {
		final SearchDlg searchDlg = ((FileContentSearchCriteria) criteria).dlg;
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
		statement = searchDlg.tfFind.getText();
		if (statement == null || statement.isEmpty()) {
			JOptionPane.showMessageDialog(searchDlg, "Statement for search should not be empty.", "Wrong input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!isMatchCase) {
			statement = statement.toLowerCase();
		}
		initFileFilters(searchDlg.tfFileFilter.getText());
		// final List<SearchResult> results = new ArrayList<SearchResult>();
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				search("", jarFile, searchDlg);
				long overall = System.currentTimeMillis() - start;
				searchDlg.tResult.setModel(new FileContentSearchResultTableModel(searchResult, errors));
				String label = "Found " + searchResult.size() + " results for " + overall + "ms";
				int size = errors.size();
				if (size != 0) {
					label += " with " + size + " errors";
				}
				label += ':';
				searchDlg.lbResult.setText(label);
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

	private void initFileFilters(String fileFilter) {
		if (fileFilter == null || fileFilter.isEmpty()) {
			return;
		}
		for (String t : fileFilter.toLowerCase().split(",")) {
			String token = t.trim();
			if (token.trim().isEmpty()) {
				continue;
			}
			if (token.charAt(0) == '!') {
				ignores.add(token.substring(1));
			} else {
				nonIgnores.add(token);
			}
		}
	}

	private void search(final String parent, final File archive, final SearchDlg dlg) {

		new Jar(archive) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				String fileName = path;
				int i = path.lastIndexOf('/');
				if (i != -1) {
					fileName = path.substring(i + 1);
				}
				String ext = path.toLowerCase();
				boolean isArchive = isArchive(ext);
				if (!isArchive && isForSearch(ext) && !ext.endsWith("/")) {
					UnpackResult res = unpack(fileName, path, archive);
					Scanner scanner;
					if (ext.endsWith(".class")) {
						try {
							scanner = new Scanner(com.delfin.jarexp.utils.Compiler.decompile(res.dst));
						} catch (Exception e) {
							log.log(Level.SEVERE, "An error occurred while decompiling file " + res.dst, e);
							errors.put(res.fullPath, "ERROR. Unable to search in the class. Cause: " + e.getMessage());
							return;
						}
					} else {
						scanner = new Scanner(res.dst);
					}
					try {
						int j = 0;
						while(scanner.hasNextLine()) {
							String origLine = scanner.nextLine();
							String line = isMatchCase ? origLine : origLine.toLowerCase();
							int k = line.indexOf(statement);
							if (k != -1) {
								List<SearchResult> findings = searchResult.get(res.fullPath);
								if (findings == null) {
									findings = new ArrayList<SearchResult>();
								}
								findings.add(new SearchResult(line, j + k));
								searchResult.put(res.fullPath, findings);
							}
							j += line.length() + 1;
						}
					} finally {
						scanner.close();
					}
				} else if (isInAll && isArchive) {
					UnpackResult res = unpack(fileName, path, archive);
					search(res.fullPath + '!', res.dst, dlg);
				}
				dlg.lbResult.setText("Searching..." + entry.getName());
			}

			private UnpackResult unpack(String fileName, String path, File archive) {
				File dst = new File(Resources.createTmpDir(), fileName);
				String fullPath = getFullPath(path);
				dst = Zip.unzip(fullPath, path, archive, dst);
				return new UnpackResult(fullPath, dst);
			}

			private boolean isForSearch(String fileName) {
				if (ignores.isEmpty() && nonIgnores.isEmpty()) {
					return true;
				}
				for (String ext : nonIgnores) {
					if (fileName.endsWith(ext)) {
						return true;
					}
				}
				for (String ext : ignores) {
					if (fileName.endsWith(ext)) {
						return true;
					}
				}
				return nonIgnores.isEmpty();
			}

			private boolean isArchive(String ext) {
				return ext.endsWith(".jar") || ext.endsWith(".war") || ext.endsWith(".ear") || ext.endsWith(".zip")
						|| ext.endsWith(".apk");
			}

			private String getFullPath(String path) {
				return parent + '/' + path;
			}

		}.bypass();
	}

}
