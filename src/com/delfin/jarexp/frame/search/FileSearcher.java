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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.table.TableModel;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.Jar.JarBypassErrorAction;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.BypassAction;
import com.delfin.jarexp.utils.Zip.BypassRecursivelyAction;

class FileSearcher extends AbstractSearcher {

	private Pattern pattern;

	private boolean isEmptySearch;

	private Progress progress;

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
		progress = new Progress(searchDlg);
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
			    searchDlg.enableUxComponent(false);
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
				searchDlg.enableUxComponent(true);
			};
		});
		search.setName("file_search");
		search.start();
		searchDlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				search.interrupt();
				progress.interrupt();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void search(String parent, final File searchRoot, final Object results, SearchDlg dlg, String pathInJar) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
		Jar searcher;
		if (searchRoot.isDirectory()) {
			searcher = prepareDirectorySearch(searchRoot, (List<SearchResult>) results, dlg, progress);
		} else {
			searcher = prepareArchiveSearch(parent, searchRoot, (List<SearchResult>) results, dlg, pathInJar, progress);
		}
		progress.run();
		searcher.bypass(new JarBypassErrorAction() {
            @Override
            public RuntimeException apply(Exception e) {
                ((List<SearchResult>) results).add(new SearchResult("ERROR! " + e.getMessage() + "; " + searchRoot, -2));
                return null;
            }
		});
	}

	private Jar prepareArchiveSearch(final String parent, final File archive, final List<SearchResult> results,
			final SearchDlg dlg, final String pathInJar, Progress progress) {

	    progress.dir = archive;

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
                    if (pattern.matcher(isMatchCase ? fileName : fileName.toLowerCase()).find()) {
                        results.add(new SearchResult(getFullPath(parent, path)));
                    }
                }
                if (isNeedSearchInArchive(path)) {
                    if (dlg.cbToDisk.isEnabled()) {
                        File dst = new File(Resources.createTmpDir(), FileUtils.getFileName(path));
                        String fullPath = getFullPath(parent, path) + '!';
                        dst = Zip.unzip(fullPath, path, archive, dst);
                        search(fullPath, dst, results, dlg, null);
                    } else {
                        bypassArchiveRecursively(jarFile, results, dlg, entry, "/");
                    }
                }
			}

			private boolean isNeedSearchInArchive(String path) {
				return fullSearchPath.startsWith(getCurrentFullPath(parent, path, pathInJar)) 
						|| isInAll && Zip.isArchive(path, true);
			}

		};
	}

	private Jar prepareDirectorySearch(File root, final List<SearchResult> results, final SearchDlg dlg, Progress progress) {

	    progress.dir = root;

		return new Directory(root) {
			@Override
			protected void process(final File file) throws IOException {
			    String fileName = file.getName();
			    processEntry(fileName, file.getAbsolutePath(), file, dlg, results);
				if (isInAll && Zip.isArchive(fileName)) {
				    if (dlg.cbToDisk.isEnabled()) {
				        search(file.getAbsolutePath() + '!', file, results, dlg, null);
				    } else {
	                    final String archPath = file.getAbsolutePath() + '!' + '/';
	                    try {
	                        Zip.bypass(file, new BypassAction() {
	                            @Override
	                            public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
	                                processEntry(getZipEntryName(zipEntry), archPath, null, dlg, results);
	                                if (Zip.isArchive(zipEntry.getName())) {
	                                    bypassArchiveRecursively(zipFile, results, dlg, zipEntry, archPath);
	                                }
	                            }
	                        });
	                    } catch (Exception e) {

	                    }
				    }
				}
			}
		};
	}

    private void bypassArchiveRecursively(ZipFile zipFile, final List<SearchResult> results, final SearchDlg dlg, ZipEntry entry, String rootPath) throws IOException {
        String fullPath = rootPath + entry.getName() + '!' + '/';
        ZipInputStream stream = new ZipInputStream(zipFile.getInputStream(entry));
        Zip.bypass(stream, fullPath, new BypassRecursivelyAction () {
            @Override
            public void process(ZipEntry zipEntry, String fullPath) throws IOException {
                processEntry(getZipEntryName(zipEntry), fullPath, null, dlg, results);
            }
            @Override
            public void error(ZipEntry zipEntry, Exception error) {
                results.add(new SearchResult(error.getMessage(), -2));
            }
        });
    }

    private static String getZipEntryName(ZipEntry zipEntry) {
        String name = zipEntry.getName();
        int i = name.lastIndexOf('/');
        return i < 0 ? name : name.substring(i + 1);
    }

    private void processEntry(String fileName, String fullPath, File file, SearchDlg dlg, List<SearchResult> results) {
        dlg.lbResult.setText("Searching..." + (file == null ? fullPath + fileName : file));
        if (isEmptySearch) {
            if (fileName.lastIndexOf('.') == -1) {
                results.add(new SearchResult(getResultPath(fileName, fullPath, file)));
            }
        } else {
            if (pattern.matcher(isMatchCase ? fileName : fileName.toLowerCase()).find()) {
                results.add(new SearchResult(getResultPath(fileName, fullPath, file)));
            }
        }
        progress.increaseProcessed();
    }

    private static String getResultPath(String fileName, String fullPath, File file) {
        if (fullPath != null && fullPath.charAt(fullPath.length() - 1) != '/') {
            fullPath += '/';
        }
        return file == null ? fullPath + fileName : file.getAbsolutePath();
    }

}
