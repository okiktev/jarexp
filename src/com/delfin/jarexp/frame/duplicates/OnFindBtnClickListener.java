package com.delfin.jarexp.frame.duplicates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;

import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.search.SearchResult;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Md5Checksum;
import com.delfin.jarexp.utils.StringUtils;
import com.delfin.jarexp.utils.Zip;

class OnFindBtnClickListener implements ActionListener {

	private static class UnpackResult {
		String fullPath;
		File dst;

		UnpackResult(String fullPath, File dst) {
			this.fullPath = fullPath;
			this.dst = dst;
		}
	}

	private Map<String, List<SearchResult>> searchResult = new LinkedHashMap<String, List<SearchResult>>();

	private DuplicatesDlg dlg;

	private boolean isInAll;

	private boolean isUseMd5;

	OnFindBtnClickListener(DuplicatesDlg dlg) {
		this.dlg = dlg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		searchResult.clear();
		isInAll = dlg.cbInAllSubArchives.isSelected();
		isUseMd5 = dlg.isUseMd5;
		final Thread search = new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				search("", dlg.jarFile, dlg);
				long overall = System.currentTimeMillis() - start;
				for (Iterator<Entry<String, List<SearchResult>>> it = searchResult.entrySet().iterator(); it
						.hasNext();) {
					Entry<String, List<SearchResult>> entry = it.next();
					if (entry.getValue().size() < 2) {
						it.remove();
					}
				}
				dlg.tResult.setModel(new DuplicatesFileSearchResultTableModel(searchResult));
				String label = "Found " + searchResult.size() + " results for " + overall + "ms:";
				dlg.lbResult.setText(label);
			};
		});
		search.start();
		dlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				search.interrupt();
			}
		});
	}

	private void search(final String parent, final File archive, final DuplicatesDlg dlg) {

		new Jar(archive) {
			@Override
			protected void process(JarEntry entry) throws IOException {
				String path = entry.getName();
				if (StringUtils.isLast(path, '/')) {
					return;
				}
				dlg.lbResult.setText("Searching..." + path);
				String key;
				if (isUseMd5) {
			        key = Md5Checksum.get(archive, path);
				} else {
					key = entry.getName();
				}
				boolean isArchive = Zip.isArchive(path);
				if ((isArchive && !isInAll) || !isArchive) {
					List<SearchResult> results = searchResult.get(key);
					if (results == null) {
						results = new ArrayList<SearchResult>();
						searchResult.put(key, results);
					}
					results.add(new SearchResult(getFullPath(path)));
				}
				if (isArchive && isInAll) {
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

			private String getFullPath(String path) {
				return parent + '/' + path;
			}

		}.bypass();
	}

}
