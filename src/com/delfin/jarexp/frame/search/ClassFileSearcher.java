package com.delfin.jarexp.frame.search;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;

import javax.swing.JOptionPane;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.utils.Zip;

class ClassFileSearcher implements Searcher {

	private final File jarFile;

	private String className;

	private boolean isMatchCase;

	private boolean isInAll;

	ClassFileSearcher(File jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public void search(SearchCriteria criteria) {
		final SearchDlg searchDlg = ((ClassFileSearchCriteria) criteria).dlg;
		isMatchCase = searchDlg.cbMatchCase.isSelected();
		isInAll = searchDlg.cbInAllSubArchives.isSelected();
		className = searchDlg.tfFind.getText();
		if (className == null || className.isEmpty()) {
			JOptionPane.showMessageDialog(searchDlg, "Class name should not be empty.", "Wrong input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!isMatchCase) {
			className = className.toLowerCase();
		}
		final List<SearchResult> results = new ArrayList<SearchResult>();
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				search("", jarFile, results, searchDlg);
				long overall = System.currentTimeMillis() - start;
				searchDlg.tResult.setModel(new ClassFileSearchResultTableModel(results));
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
				if (path.charAt(path.length() - 1) == '/') {
					return;
				}
				dlg.lbResult.setText("Searching..." + entry.getName());
				String fileName = path;
				int i = path.lastIndexOf('/');
				if (i != -1) {
					fileName = path.substring(i + 1);
				}
				if (!isArchive(path.toLowerCase())) {
					fileName = fileName.substring(0, fileName.lastIndexOf('.'));
					if (!isMatchCase) {
						fileName = fileName.toLowerCase();
					}
					if (fileName.contains(className)) {
						results.add(new SearchResult(getFullPath(path)));
					}
				} else if (isInAll) {
					File dst = new File(Resources.createTmpDir(), fileName);
					String fullPath = getFullPath(path) + '!';
					dst = Zip.unzip(fullPath, path, archive, dst);
					search(fullPath, dst, results, dlg);
				}
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
