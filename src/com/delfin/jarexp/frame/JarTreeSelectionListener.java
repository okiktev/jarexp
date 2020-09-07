package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.decompiler.IDecompiler.Result;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.IDecompiler;
import com.delfin.jarexp.frame.Content.PreLoadAction;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.icon.Ico;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;


class JarTreeSelectionListener implements TreeSelectionListener {

	class FilterAction extends AbstractAction {
		private static final long serialVersionUID = -5642316911788605567L;
		@Override
		public void actionPerformed(ActionEvent event) {
			if (area == null) {
				return;
			}
			if (node.isDirectory || node.isArchive()) {
				return;
			}
			JarTreeSelectionListener.this.isLocked = true;
			try {
				JSplitPane pane = Content.getSplitPane();
				pane.setRightComponent(new ContentPanel(new FilterPanel(JarTreeSelectionListener.this),
						pane.getRightComponent()));
				pane.validate();
				pane.repaint();
				setDividerLocation(pane);
			} finally {
				JarTreeSelectionListener.this.isLocked = false;
			}
		}
	}
 
	private static final Logger log = Logger.getLogger(JarTreeDropTargetListener.class.getCanonicalName());

	private static Theme theme;
	static {
		try {
			theme = Theme.load(JarTreeSelectionListener.class
			        .getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to apply eclipse theme", e);
		}
	}

	private final JarTree jarTree;

	private final StatusBar statusBar;

	private final JFrame frame;

	private boolean isEdited;

	JTextArea area;

	private JarNode node;

	private int dividerLocation;

	private boolean isLocked;

	JarTreeSelectionListener(JarTree jarTree, StatusBar statusBar, JFrame frame) {
		this.jarTree = jarTree;
		this.statusBar = statusBar;
		this.frame = frame;
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		if (jarTree.isNotDraw) {
			jarTree.isNotDraw = false;
			return;
		}
	    TreePath[] path = JarTreeClickSelection.getNodes();
	    if (path != null) {
	        jarTree.setSelectionPaths(path);
	    }
	    path = jarTree.getSelectionPaths();
	    JarTreeClickSelection.setNodes(path);
	    if (path != null && path.length > 1) {
	        return;
	    }
		if (jarTree.isDragging()) {
			return;
		}
		new Executor() {
			@Override
			protected void perform() {
				isLocked = true;
				if (isEdited()) {
					saveChanges();
				}
				node = (JarNode) jarTree.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				if (!node.isLeaf() || node.isDirectory) {
					statusBar.setPath(node.getFullPath());
					statusBar.setCompiledVersion("");
					Content.preLoadArchive(node, new PreLoadAction() {
						@Override
						public void perform() {
							int divLocation = dividerLocation;
							Content current = (Content) frame.getContentPane();
							JSplitPane pane = (JSplitPane) current.getComponent(1);
							while (true) {
								Component contentView = pane.getRightComponent();
								if (contentView != null) {
									pane.remove(contentView);
									break;
								} else {
									try {
										Thread.sleep(20);
									} catch (InterruptedException e) {
										log.log(Level.SEVERE, "Error while sleeping", e);
									}
								}
							}

						    JTable table = new JTable(new JarNodeTableModel(node, statusBar));
					        table.setFillsViewportHeight(true);
					        table.setAutoCreateRowSorter(true);

					        JComponent contentView = new JScrollPane(table);
							contentView.setBorder(Settings.EMPTY_BORDER);
							pane.setRightComponent(contentView);
							pane.validate();
							pane.repaint();

							dividerLocation = divLocation;
							setDividerLocation(pane);
							isLocked = false;
						}
					});
					return;
				}
				Content current = (Content) frame.getContentPane();
				JSplitPane pane = (JSplitPane) current.getComponent(1);
				Component contentView = pane.getRightComponent();

				try {
					statusBar.enableProgress("Loading...");
					statusBar.setPath(node.getFullPath());
					statusBar.setCompiledVersion("");
					statusBar.setChildren("");

					String archName = null;
					File file;
					String lowPath;
					if (node.getParent() == null && (archName = node.origArch.getName().toLowerCase()).endsWith(".class")) {
	                    file = node.origArch;
	                    lowPath = archName;
					} else {
		                file = new File(node.origArch.getParent(), node.path);
		                lowPath = node.path.toLowerCase();
					}

					if (lowPath.endsWith(".class")) {
						statusBar.enableProgress("Decompiling...");
						Result decompiled = decompile(node);
						statusBar.setCompiledVersion(decompiled.version);
						String content = decompiled.content;
						RSyntaxTextArea textArea = new RSyntaxTextArea(content);
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
						textArea.setBorder(Settings.EMPTY_BORDER);
						textArea.setCodeFoldingEnabled(true);
						textArea.setEditable(false);
						applyTheme(textArea);
						area = textArea;
	
						pane.remove(contentView);
						RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
						textScrollPane.setBorder(Settings.EMPTY_BORDER);
						contentView = new ContentPanel(textScrollPane);
					} else if (isImgFile(lowPath)) {
						ZipFile zip = null;
						try {
							zip = new ZipFile(node.getTempArchive());
							ZipEntry entry = zip.getEntry(node.path);
							InputStream stream = zip.getInputStream(entry);
							Image image = ImageIO.read(stream);
							if (image == null) {
								JOptionPane.showConfirmDialog(frame, "Could not read image " + node.path,
								        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
								return;
							}
							pane.remove(contentView);
							contentView = new JScrollPane(new ImgPanel(image));
						} catch (IOException e) {
							throw new JarexpException("Couldn't read file " + file + " as image", e);
						} finally {
							if (zip != null) {
								try {
									zip.close();
								} catch (IOException e) {
									log.log(Level.WARNING, "Couldn't close zip file " + file, e);
								}
							}
						}
					} else if (lowPath.endsWith(".ico")) {
						file = Zip.unzip(node.getFullPath(), node.path, node.getTempArchive(), file);
						try {
							pane.remove(contentView);
							JPanel pnl = new JPanel();
					        for (BufferedImage icon : Ico.read(file)) {
					        	pnl.add(new ImgPanel(icon));
					        }
							contentView = new JScrollPane(pnl);
						} catch (IOException e) {
							throw new JarexpException("Couldn't read file " + file + " as ico", e);
						}
					} else {
						if (!file.isDirectory()) {
							statusBar.enableProgress("Reading...");
							String content = Zip.unzip(node.getTempArchive(), node.path);
							RSyntaxTextArea textArea = new RSyntaxTextArea(content);
							textArea.setSyntaxEditingStyle(getSyntax(lowPath));
							textArea.setBorder(Settings.EMPTY_BORDER);
							textArea.setCodeFoldingEnabled(true);
							textArea.setEditable(true);
							textArea.getDocument().addDocumentListener(new TextAreaDocumentListener());
							applyTheme(textArea);
							InputMap map = textArea.getInputMap();
							map.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK), new AbstractAction() {
								private static final long serialVersionUID = -3016470783134782605L;

								@Override
								public void actionPerformed(ActionEvent event) {
									saveChanges();
								}
							});
							area = textArea;

							Dimension size = contentView.getPreferredSize();
							pane.remove(contentView);
							RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
							textScrollPane.setBorder(Settings.EMPTY_BORDER);
							contentView = new ContentPanel(textScrollPane);
							contentView.setPreferredSize(size);
						}
					}
				} finally {
					((JComponent) contentView).setBorder(Settings.EMPTY_BORDER);
					pane.setRightComponent(contentView);
					pane.validate();
					pane.repaint();
					setDividerLocation(pane);
					isLocked = false;
				}
			}

			private Result decompile(JarNode node) {
				IDecompiler decompiler = Decompiler.get();
				File archive = node.getTempArchive();
				return "".equals(node.path) ? decompiler.decompile(archive)
						: decompiler.decompile(archive, node.path);
			}

			@Override
			protected void doFinally() {
				isLocked = false;
				statusBar.disableProgress();
			}

			private String getSyntax(String lowPath) {
				String syntax = SyntaxConstants.SYNTAX_STYLE_NONE;
				if (lowPath.endsWith(".html") || lowPath.endsWith(".htm")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_HTML;
				} else if (lowPath.endsWith(".xml") || lowPath.endsWith(".tld")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_XML;
				} else if (lowPath.endsWith(".properties")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
				} else if (lowPath.endsWith(".dtd")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_DTD;
				} else if (lowPath.endsWith(".css")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_CSS;
				} else if (lowPath.endsWith(".jsp")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_JSP;
				} else if (lowPath.endsWith(".js")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
				} else if (lowPath.endsWith(".bat") || lowPath.endsWith(".cmd")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
				} else if (lowPath.endsWith(".sh")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
				} else if (lowPath.endsWith(".java")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_JAVA;
				} else if (lowPath.endsWith(".groovy")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_GROOVY;
				} else if (lowPath.endsWith(".json")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
				} else if (lowPath.endsWith(".yaml")) {
					syntax = SyntaxConstants.SYNTAX_STYLE_YAML;
				}
				return syntax;
			}
		}.execute();
	}

	private void saveChanges() {
		try {
			statusBar.enableProgress("Saving...");
			if (!isEdited()) {
				return;
			}
			int reply = JOptionPane.showConfirmDialog(frame,
			        "File " + node.path + " was changed. Do you want to keep changes?", "Change confirmation",
			        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (reply == JOptionPane.YES_OPTION) {

				List<JarNode> path = node.getPathList();
				JarNode root = path.get(path.size() - 1);
				File f = new File(root.name);
				if (!FileUtils.isUnlocked(f)) {
					JOptionPane.showConfirmDialog(frame,
					        "Cannot save the file " + f + " because it is being used by another process.",
					        "Error saving...", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				File tmp = File.createTempFile("edit", node.name, Settings.getTmpDir());
				String content = area.getText();
				if (node.path.toLowerCase().endsWith(".mf")) {
					try {
						InputStream is = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
						new Manifest(is).write(new FileOutputStream(tmp));
					} catch (IOException e) {
						JOptionPane.showConfirmDialog(frame, "Failed to save manifest.\nCause: " + e.getMessage(),
						        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					FileUtils.toFile(tmp, content);
				}
				Jar.delete(node, false);
				Jar.pack(node, tmp);
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while saving changes", e);
		} finally {
			setEdited(false);
			node = null;
			area = null;
			statusBar.disableProgress();
		}
	}
	
	boolean isEdited() {
		return isEdited;
	}
	
	void setEdited(boolean isEdited) {
		this.isEdited = isEdited;
	}

	private static void applyTheme(RSyntaxTextArea textArea) {
		if (theme != null) {
			theme.apply(textArea);
		}
	}

	private static boolean isImgFile(String fileName) {
		return fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".jpg")
		        || fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
	}

	private class TextAreaDocumentListener implements DocumentListener {
		@Override
		public void removeUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (!isEdited()) {
				setEdited(true);
			}
		}
	}

	private void setDividerLocation(JSplitPane pane) {
		try {
			Thread.sleep(50);
			pane.setDividerLocation(dividerLocation);
			// wait something till changing divider event will being fired.
			Thread.sleep(50);
		} catch (InterruptedException e) {
			throw new JarexpException(e);
		}
	}

	boolean isLocked() {
		return isLocked;
	}

	void setDividerLocation(int dividerLocation) {
		this.dividerLocation = dividerLocation;
	}

	int getDividerLocation() {
		return dividerLocation;
	}

}
