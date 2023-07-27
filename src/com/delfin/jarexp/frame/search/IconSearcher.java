package com.delfin.jarexp.frame.search;

import static java.util.Collections.singleton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableModel;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.win.exe.PE;


class IconSearcher extends AbstractSearcher {

	private static final Logger log = Logger.getLogger(IconSearcher.class.getCanonicalName());

	private Map<String, List<SearchResult>> searchResult = new LinkedHashMap<String, List<SearchResult>>();

	private Map<String, String> errors = new LinkedHashMap<String, String>();

	private List<String> searchResources = new ArrayList<String>(1);

	@Override
	public void search(final SearchDlg searchDlg) {
		super.search(searchDlg);

		if (searchDlg.cbIconsInExe.isSelected()) {
			searchResources.add(".exe");
		}
		if (searchDlg.cbIconsInDll.isSelected()) {
			searchResources.add(".dll");
		}
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
		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		Jar searcher;
		if (searchRoot.isDirectory()) {
			searcher = prepareDirectorySearch(searchRoot, dlg);
		} else {
			searcher = prepareArchiveSearch(parent, searchRoot, dlg, pathInJar);
		}
		searcher.bypass();
	}

	private Jar prepareArchiveSearch(final String parent, final File archive,
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
				String lowPath = path.toLowerCase();
				for(String exe : searchResources) {
					if (lowPath.endsWith(exe)) {
						try {
							List<String> result = getIconsFromPe(jarFile.getInputStream(entry));
							if (!result.isEmpty()) {
								searchResult.put(parent + '/' + path, iconNamesToSearchResults(result));
							}
						} catch (Throwable t) {
							handleError(t, path);
						}
					}
				}
				if (isNeedSearchInArchive(path)) {
					File dst = new File(Resources.createTmpDir(), path);
					String fullPath = getFullPath(parent, path) + '!';
					dst = Zip.unzip(fullPath, path, archive, dst);
					search(fullPath, dst, null, dlg, null);
				} else if (lowPath.endsWith(".ico")) {
					String key = parent.isEmpty() ? "/" : parent;
					List<SearchResult> res = searchResult.get(key);
					if (res == null) {
						res = new ArrayList<SearchResult>();
						searchResult.put(key, res);
					}
					res.addAll(iconNamesToSearchResults(singleton(path)));
				}
			}

			private boolean isNeedSearchInArchive(String path) {
				return fullSearchPath.startsWith(getCurrentFullPath(parent, path, pathInJar)) 
						|| isInAll && Zip.isArchive(path, true);
			}

		};
	}

	private Jar prepareDirectorySearch(final File root, final SearchDlg dlg) {

		return new Directory(root) {
			@Override
			protected void process(File file) throws IOException {
				dlg.lbResult.setText("Searching..." + file);
				String fileName = file.getName().toLowerCase();
				for(String exe : searchResources) {
					if (fileName.endsWith(exe)) {
						try {
							searchResult.put(file.getAbsolutePath(), iconNamesToSearchResults(getIconsFromPe(file)));
						} catch (Throwable t) {
							handleError(t, file.getAbsolutePath());
						}
						break;
					}
				}
				if (isInAll && Zip.isArchive(fileName)) {
					search(file.getAbsolutePath() + '!', file, null, dlg, null);
				}
			}
		};

	}

	private static List<String> getIconsFromPe(File peFile) throws IOException {
		return addIcoExt(PE.getIconNames(peFile));
	}

	private static List<String> getIconsFromPe(InputStream stream) throws IOException {
		return addIcoExt(PE.getIconNames(stream));
	}

	private static List<String> addIcoExt(List<String> names) {
		for (int i = 0; i < names.size(); ++i) {
			names.set(i, names.get(i) + ".ico");
		}
		return names;
	}

	private static List<SearchResult> iconNamesToSearchResults(Collection<String> iconNames) {
		List<SearchResult> res = new ArrayList<SearchResult>();
		for (String icon : iconNames) {
			res.add(new SearchResult(icon, 1));
		}
		return res;
	}

	private void handleError(Throwable e, String fullPath) {
		log.log(Level.SEVERE, "An error while searching icons in " + fullPath, e);
		errors.put(fullPath, "ERROR. Unable to parse a file " + fullPath + ". Cause: " + e.getMessage());
	}

}
