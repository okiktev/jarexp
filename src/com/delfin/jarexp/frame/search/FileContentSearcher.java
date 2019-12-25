package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import javax.swing.table.TableModel;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;

class FileContentSearcher extends AbstractSearcher {

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

	private String statement;

	@Override
	public void search(SearchCriteria criteria) {
		super.search(criteria);
		statement = (String) searchDlg.cbFind.getSelectedItem();
		if (statement == null || statement.isEmpty()) {
			showMessageDialog(searchDlg, "Statement for search should not be empty.", "Wrong input", WARNING_MESSAGE);
			return;
		}
		if (!isMatchCase) {
			statement = statement.toLowerCase();
		}
		initFileFilters(searchDlg.tfFileFilter.getText());
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				manageSearchHistory();
				long start = System.currentTimeMillis();
				for (SearchEntries entry : searchEntries) {
					fullSearchPath = entry.fullPath;
					isSearchInWhole = entry.archive.getAbsolutePath().equals(fullSearchPath);
					search("", entry.archive, null, searchDlg, entry.path);
				}
				long overall = System.currentTimeMillis() - start;
				searchResult = new TreeMap<String, List<SearchResult>>(searchResult);
				TableModel table = new FileContentSearchResultTableModel(searchResult, errors);
				searchDlg.tResult.setModel(table);
				String label = "Found " + searchResult.size() + " results with " + getHits(searchResult) + " hits for " + overall + "ms";
				int size = errors.size();
				if (size != 0) {
					label += " with " + size + " errors";
				}
				label += ':';
				searchDlg.lbResult.setText(label);
				searchDlg.btnResultToClipboard.setVisible(true);
				searchDlg.btnResultToClipboard.setResult(table);
				searchDlg.btnResultToFile.setVisible(true);
				searchDlg.btnResultToFile.setResult(table);
			}

			private int getHits(Map<String, List<SearchResult>> result) {
				int res = 0;
				for (List<SearchResult> hits : result.values()) {
					res += hits.size();
				}
				return res;
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

	@Override
	protected void search(String parent, File searchRoot, Object results, SearchDlg dlg, String pathInJar) {
		Jar seacher;
		if (searchRoot.isDirectory()) {
			seacher = prepareDirectorySearch(searchRoot, dlg);
		} else {
			seacher = prepareArchiveSearch(parent, searchRoot, dlg, pathInJar);
		}
		seacher.bypass();
	}

	private Jar prepareArchiveSearch(final String parent, final File searchRoot, final SearchDlg dlg, final String pathInJar) {
		return new Jar(searchRoot) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				if (isNotAllowedToSearch(parent, path, pathInJar)) {
					return;
				}
				String ext = path.toLowerCase();
				boolean isArchive = Zip.isArchive(ext, false);
				if (!isArchive && !isForSearch(ext)) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);

				if (!isArchive) {
					String fullPath = getFullPath(parent, path);
					Scanner scanner;
					if (ext.endsWith(".class")) {
						try {
							scanner = new Scanner(Decompiler.get().decompile(searchRoot, path).content);
						} catch (Exception e) {
							handleError(e, fullPath);
							return;
						}
					} else {
						@SuppressWarnings("resource")
						ZipFile zip = new ZipFile(searchRoot);
						InputStream stream = zip.getInputStream(zip.getEntry(path));
						scanner = new Scanner(stream);
					}
					doSearchInFile(scanner, fullPath);
				} else if (isInAll || fullSearchPath.startsWith(getCurrentFullPath(parent, path, pathInJar))) {
					UnpackResult res = unpack(FileUtils.getFileName(path), path, searchRoot, parent);
					search(res.fullPath + '!', res.dst, null, dlg, null);
				}
			}

			private UnpackResult unpack(String fileName, String path, File archive, String parent) {
				File dst = new File(Resources.createTmpDir(), fileName);
				String fullPath = getFullPath(parent, path);
				dst = Zip.unzip(fullPath, path, archive, dst);
				return new UnpackResult(fullPath, dst);
			}

		};
	}

	private Jar prepareDirectorySearch(File searchRoot, final SearchDlg dlg) {
		return new Directory(searchRoot) {
			@Override
			protected void process(File file) throws IOException {
				String path = file.getName();
				String ext = path.toLowerCase();
				boolean isArchive = Zip.isArchive(ext, false);
				if (!isArchive && !isForSearch(ext)) {
					return;
				}
				dlg.lbResult.setText("Searching..." + file);
				if (!isArchive) {
					String fullPath = file.getAbsolutePath();
					Scanner scanner;
					if (ext.endsWith(".class")) {
						try {
							scanner = new Scanner(Decompiler.get().decompile(file).content);
						} catch (Exception e) {
							handleError(e, fullPath);
							return;
						}
					} else {
						scanner = new Scanner(new FileInputStream(file));
					}
					doSearchInFile(scanner, fullPath);
				} else if (isInAll) {
					search(file.getAbsolutePath() + '!', file, null, dlg, null);
				}
			}
		};
	}

	private void doSearchInFile(Scanner scanner, String fullPath) {
		try {
			int j = 0;
			while (scanner.hasNextLine()) {
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
	}

	private void handleError(Exception e, String fullPath) {
		log.log(Level.SEVERE, "An error occurred while decompiling file " + fullPath, e);
		errors.put(fullPath, "ERROR. Unable to search in the class. Cause: " + e.getMessage());
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

	@Override
	protected SearchDlg extractSearchDlg(SearchCriteria criteria) {
		return ((FileContentSearchCriteria) criteria).dlg;
	}

}
