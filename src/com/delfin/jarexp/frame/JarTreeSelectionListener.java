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

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.Version;
import com.delfin.jarexp.frame.Content.PreLoadAction;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.icon.Ico;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;


class JarTreeSelectionListener implements TreeSelectionListener {

	class FilterAction extends AbstractAction {
		private static final long serialVersionUID = 2090916851645693410L;
		private RSyntaxTextArea textArea;
		FilterAction(RSyntaxTextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			JSplitPane pane = Content.getSplitPane();
			pane.setRightComponent(new ContentPanel(
					new FilterPanel(JarTreeSelectionListener.this, textArea),
					((ContentPanel) pane.getRightComponent()).getContent()));
			pane.setDividerLocation(dividerLocation);
			pane.validate();
			pane.repaint();
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

	private JTextArea area;

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
	    TreePath[] path = JarTreeClickSelection.getNodes();
	    if (path != null) {
	        jarTree.setSelectionPaths(path);
	    }
	    path = jarTree.getSelectionPaths();
	    JarTreeClickSelection.setNodes(path);
	    if (path != null && path.length > 1) {
	        return;
	    }
	    try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new JarexpException(e);
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
							Content current = (Content) frame.getContentPane();
							JSplitPane pane = (JSplitPane) current.getComponent(1);
							Component contentView = pane.getRightComponent();

						    JTable table = new JTable(new JarNodeTableModel(node, statusBar));
					        table.setFillsViewportHeight(true);
					        table.setAutoCreateRowSorter(true);

					        contentView = new JScrollPane(table);

							((JComponent) contentView).setBorder(Settings.EMPTY_BORDER);
							pane.setRightComponent(contentView);
							pane.setDividerLocation(dividerLocation);
							pane.validate();
							pane.repaint();
						}
					});
					return;
				}
				Content current = (Content) frame.getContentPane();
				JSplitPane pane = (JSplitPane) current.getComponent(1);
				Component contentView = pane.getRightComponent();

				statusBar.enableProgress("Loading...");
				statusBar.setPath(node.getFullPath());
				statusBar.setCompiledVersion("");
				statusBar.setChildren("");

				String archName = null;
				File file;
				String lowPath;
				if (node.getParent() == null && (archName = node.archive.getName().toLowerCase()).endsWith(".class")) {
                    file = node.archive;
                    lowPath = archName;
				} else {
	                file = new File(node.archive.getParent(), node.path);
	                file = Zip.unzip(node.getFullPath(), node.path, node.archive, file);
	                lowPath = node.path.toLowerCase();
				}

				if (lowPath.endsWith(".class")) {
					statusBar.enableProgress("Decompiling...");
					statusBar.setCompiledVersion(Version.getCompiledJava(file));

					RSyntaxTextArea textArea = new RSyntaxTextArea(com.delfin.jarexp.utils.Compiler.decompile(file));
					textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
					textArea.setBorder(Settings.EMPTY_BORDER);
					textArea.setCodeFoldingEnabled(true);
					textArea.setEditable(false);
					textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), new FilterAction(textArea));
					applyTheme(textArea);
					area = textArea;

					Dimension size = contentView.getPreferredSize();
					pane.remove(contentView);
					RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
					textScrollPane.setBorder(Settings.EMPTY_BORDER);
					contentView = new ContentPanel(textScrollPane);
					contentView.setPreferredSize(size);
				} else if (isImgFile(lowPath)) {
					try {
						Image image = ImageIO.read(file);
						if (image == null) {
							JOptionPane.showConfirmDialog(frame, "Could not read image " + node.path,
							        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							return;
						}
						pane.remove(contentView);
						contentView = new JScrollPane(new ImgPanel(image));
					} catch (IOException e) {
						throw new JarexpException("Couldn't read file " + file + " as image", e);
					}
				} else if (lowPath.endsWith(".ico")) {
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
						final RSyntaxTextArea textArea = new RSyntaxTextArea(FileUtils.toString(file));
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
						map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), new FilterAction(textArea));
						area = textArea;

						Dimension size = contentView.getPreferredSize();
						pane.remove(contentView);
						RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
						textScrollPane.setBorder(Settings.EMPTY_BORDER);
						contentView = new ContentPanel(textScrollPane);
						contentView.setPreferredSize(size);
					}
				}

				((JComponent) contentView).setBorder(Settings.EMPTY_BORDER);
				pane.setRightComponent(contentView);
				pane.setDividerLocation(dividerLocation);
				pane.validate();
				pane.repaint();
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
				} else if (lowPath.endsWith(".xml")) {
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
