package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;


class FileContentSearcher implements Searcher {

	private static class UnpackResult {
		String fullPath;
		File dst;
		UnpackResult(String fullPath, File dst) {
			this.fullPath = fullPath;
			this.dst = dst;
		}
	}

	private static final Logger log = Logger.getLogger(FileContentSearcher.class.getCanonicalName());

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
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				String ext = path.toLowerCase();
				boolean isArchive = Zip.isArchive(ext, false);
				if (!isArchive && !isForSearch(ext)) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);
				if (!isArchive) {
			        String fullPath = getFullPath(path);
					Scanner scanner;
					if (ext.endsWith(".class")) {
						try {
							scanner = new Scanner(Decompiler.get().decompile(archive, path).content);
						} catch (Exception e) {
							log.log(Level.SEVERE, "An error occurred while decompiling file " + fullPath, e);
							errors.put(fullPath, "ERROR. Unable to search in the class. Cause: " + e.getMessage());
							return;
						}
					} else {
				        ZipFile zip = new ZipFile(archive);
				        InputStream stream = zip.getInputStream(zip.getEntry(path));
						scanner = new Scanner(stream);
						zip.close();
					}
					try {
						int j = 0;
						while(scanner.hasNextLine()) {
							String line = scanner.nextLine();
							int k = isMatchCase ? line.indexOf(statement) : StringUtils.indexOf(line, statement);
							if (k != -1) {
								List<SearchResult> findings = searchResult.get(fullPath);
								if (findings == null) {
									findings = new ArrayList<SearchResult>();
								}
								findings.add(new SearchResult(line, j + k));
								searchResult.put(fullPath, findings);
							}
							j += line.length() + 1;
						}
					} finally {
						scanner.close();
					}
				} else if (isInAll) {
					UnpackResult res = unpack(FileUtils.getFileName(path), path, archive);
					search(res.fullPath + '!', res.dst, dlg);
				}
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

			private String getFullPath(String path) {
				return parent + '/' + path;
			}

		}.bypass();
	}

}
