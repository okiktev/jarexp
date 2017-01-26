package com.delfin.jarexp.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.Version;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

class JarTreeSelectionListener implements TreeSelectionListener {

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

	private boolean wasEdited;

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
		if (jarTree.isDragging()) {
			return;
		}
		new Executor() {
			@Override
			protected void perform() {
				isLocked = true;
				if (wasEdited) {
					try {
						statusBar.enableProgress("Saving...");
						saveChanges();
					} catch (IOException e) {
						throw new JarexpException("An error occurred while saving changes", e);
					} finally {
						wasEdited = false;
						node = null;
						area = null;
						statusBar.disableProgress();
					}
				}
				node = (JarNode) jarTree.getLastSelectedPathComponent();
				if (node == null || !node.isLeaf() || node.isDirectory) {
					return;
				}
				File file = new File(node.archive.getParent(), node.path);

				Content current = (Content) frame.getContentPane();
				JSplitPane pane = (JSplitPane) current.getComponent(1);
				Component contentView = pane.getRightComponent();

				statusBar.enableProgress("Loading...");
				statusBar.setPath(node.path);
				statusBar.setCompiledVersion("");

				Zip.unzip(node.path, node.archive, file);
				String lowPath = node.path.toLowerCase();
				if (lowPath.endsWith(".class")) {
					statusBar.enableProgress("Decompiling...");
					statusBar.setCompiledVersion(Version.getCompiledJava(file));

					RSyntaxTextArea textArea = new RSyntaxTextArea(com.delfin.jarexp.utils.Compiler.decompile(file));
					textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
					textArea.setBorder(Settings.EMPTY_BORDER);
					textArea.setCodeFoldingEnabled(true);
					textArea.setEditable(false);
					applyTheme(textArea);
					area = textArea;

					Dimension size = contentView.getPreferredSize();
					pane.remove(contentView);
					contentView = new RTextScrollPane(textArea);
					contentView.setPreferredSize(size);
				} else if (isImgFile(lowPath)) {
					try {
						JPanel img = new ImgPanel(ImageIO.read(file));
						img.setBorder(Settings.EMPTY_BORDER);
						pane.remove(contentView);
						contentView = new JScrollPane(img);
						
						// pane.setResizeWeight(1);
						//pane.setDividerSize(0);
					} catch (IOException e) {
						throw new JarexpException("Couldn't read file " + file + " as image", e);
					}
				} else {
					if (!file.isDirectory()) {
						statusBar.enableProgress("Reading...");
						RSyntaxTextArea textArea = new RSyntaxTextArea(FileUtils.toString(file));
						textArea.setSyntaxEditingStyle(getSyntax(lowPath));
						textArea.setBorder(Settings.EMPTY_BORDER);
						textArea.setCodeFoldingEnabled(true);
						textArea.setEditable(true);
						textArea.getDocument().addDocumentListener(new TextAreaDocumentListener());
						applyTheme(textArea);
						KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
						textArea.getInputMap().put(key, new AbstractAction() {
							private static final long serialVersionUID = -3016470783134782605L;

							@Override
							public void actionPerformed(ActionEvent event) {
								if (wasEdited) {
									try {
										statusBar.enableProgress("Saving...");
										saveChanges();
									} catch (IOException e) {
										wasEdited = false;
										node = null;
										area = null;
										throw new JarexpException("An error occurred while saving changes", e);
									} finally {
										statusBar.disableProgress();
									}
								}
							}
						});
						area = textArea;

						Dimension size = contentView.getPreferredSize();
						pane.remove(contentView);
						contentView = new RTextScrollPane(textArea);
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

	private void saveChanges() throws IOException {
		int reply = JOptionPane.showConfirmDialog(frame,
		        "File " + node.path + " was changed. Do you want to keep changes?", "Change confirmation",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (reply == JOptionPane.YES_OPTION) {
			File tmp = File.createTempFile("edit", node.name, Settings.getTmpDir());
			String content = area.getText();
			if (node.path.toLowerCase().endsWith(".mf")) {
				try {
					InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
					new Manifest(is).write(new FileOutputStream(tmp));
				} catch (IOException e) {
					JOptionPane.showConfirmDialog(frame, "Failed to save manifest.\nCause: " + e.getMessage(), "Error",
					        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				FileUtils.toFile(tmp, content);
			}
			Jar.delete(node);
			Jar.pack(node, tmp);

			wasEdited = false;
			node = null;
			area = null;
		}
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
			wasEdited = true;
		}
	}

	boolean isLocked() {
		return isLocked;
	}

	void setDividerLocation(int dividerLocation) {
		this.dividerLocation = dividerLocation;
	}

}
