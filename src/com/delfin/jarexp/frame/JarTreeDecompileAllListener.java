package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.IDecompiler;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Executor;
import com.delfin.jarexp.utils.FileUtils;

class JarTreeDecompileAllListener extends PopupMenuListener {

    private abstract static class BypassAction {

		abstract void process(File arch, String path, File currDir);

	}

	private static final Logger log = Logger.getLogger(JarTreeDecompileAllListener.class.getCanonicalName());

	private static final String DEC_HEADER = "// decompiled by 'Jar Explorer' (" + Settings.JAREXP_HOST_URL + ')' + '\n';

	private final TreePath path;

	JarTreeDecompileAllListener(JarTree jarTree, StatusBar statusBar, JFrame frame, TreePath path) {
		super(jarTree, statusBar, frame);
		this.path = path;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final IDecompiler decompiler = Decompiler.get(Settings.getDecompilerType());
		final JarNode node = (JarNode) path.getLastPathComponent();
		if (!(node.isDirectory || node.isArchive()) && !node.name.toLowerCase().endsWith(".class")) {
			showMessageDialog(frame, "Node is not a directory or archive.", "Wrong input", ERROR_MESSAGE);
			return;
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select directory to dump decompiled source");
		File openIn = Settings.getUserHome();
		if (openIn != null) {
			chooser.setCurrentDirectory(openIn);
		}
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File f = chooser.getSelectedFile();
		if (!f.exists()) {
			showMessageDialog(frame, "Specified location does not exist.", "Wrong input", ERROR_MESSAGE);
			return;
		}
		if (!f.isDirectory()) {
			showMessageDialog(frame, "Specified location is not directory.", "Wrong input", ERROR_MESSAGE);
			return;
		}
		File[] files = f.listFiles();
		if (files != null && files.length > 0) {
			showMessageDialog(frame, "Specified folder is not empty.", "Wrong input", ERROR_MESSAGE);
			return;
		}
		new Executor() {
			@Override
			protected void perform() {
				statusBar.enableProgress("Decompiling...");
				final Map<String, ZipFile> zipFiles = new HashMap<String, ZipFile>();
				final Map<String, Integer> result = new HashMap<String, Integer>();
				result.put("dumped", 0);
				result.put("errors", 0);				
				try {
					File dir = f;
					if (node.isDirectory) {
						dir = new File(f, node.name);
						dir.mkdirs();
					}
					bypass(node, dir, new BypassAction() {
						@Override
						void process(File arch, String path, File toFile) {
							try {
								if (path.toLowerCase().endsWith(".class")) {
									String content = DEC_HEADER + "// " + new Date() + "; decompiler is " + Settings.getDecompilerType() + '\n';
									content += decompiler.decompile(arch, path).content;
									String fileName = getFileName(path, toFile);
									fileName = fileName.substring(0, fileName.length() - ".class".length());
									FileUtils.toFile(new File(toFile.isDirectory() ? toFile : toFile.getParentFile(), fileName + ".java"), content);
								} else {
									ZipFile zip = zipFiles.get(arch.getAbsolutePath());
									if (zip == null) {											
										zip = new ZipFile(arch);
										zipFiles.put(arch.getAbsolutePath(), zip);
									}
									InputStream stream = zip.getInputStream(zip.getEntry(path));
									FileUtils.toFile(toFile, stream);
								}
								result.put("dumped", result.get("dumped") + 1);
							} catch (Throwable e) {
								result.put("errors", result.get("errors") + 1);
							}
						}

						private String getFileName(String path, File toFile) {
							if (toFile.isDirectory()) {
								int i = path.lastIndexOf('/');
								if (i == -1) {
									return path;
								}
								return path.substring(i, path.length());
							} else {
								return toFile.getName();
							}
						}
					});
				} finally {
					for (Entry<String, ZipFile> en : zipFiles.entrySet()) {
						try {
							en.getValue().close();
						} catch (IOException ex) {
							log.log(Level.WARNING, "Unable to close zip file " + en.getKey(), ex);
						}
					}
					statusBar.disableProgress();
				}
				int errorsNum = result.get("errors");
				showMessageDialog(frame
						, "There " + result.get("dumped") + " files were dumped with " + errorsNum + " errors."
						, "Dumping result", errorsNum > 0 ? WARNING_MESSAGE : INFORMATION_MESSAGE);
			}
		}.execute();
	}

	private void bypass(JarNode node, File f, BypassAction action) {
		if (!node.isDirectory && !node.isArchive()) {
			File arch = node.getTempArchive();
			if (arch == null) {
				node = (JarNode) node.getParent();
				arch = node.getTempArchive();
			}
			action.process(arch, node.path, f);
		} else {
			Enumeration<?> entities = node.children();
	        while (entities.hasMoreElements()) {
	        	JarNode n = (JarNode) entities.nextElement();
				File dir = new File(f, n.name);
				if (n.isDirectory && !dir.exists()) {
					dir.mkdirs();
				}
				bypass(n, dir, action);
	        }
		}
	}

}

