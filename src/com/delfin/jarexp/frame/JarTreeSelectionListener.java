package com.delfin.jarexp.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.analyzer.Analyzer;
import com.delfin.jarexp.analyzer.IJavaItem;
import com.delfin.jarexp.analyzer.IJavaItem.Position;
import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.IDecompiler;
import com.delfin.jarexp.decompiler.IDecompiler.Result;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.Content.PreLoadAction;
import com.delfin.jarexp.frame.ContentPanel.TabComponent;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.icon.Ico;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.TableHeaderCustomizer;
import com.delfin.jarexp.utils.Zip;


class JarTreeSelectionListener implements TreeSelectionListener {

	class FilterAction extends AbstractAction {
		private static final long serialVersionUID = -5642316911788605567L;

		@Override
		public void actionPerformed(ActionEvent event) {
			JSplitPane pane = Content.getSplitPane();
			ContentPanel contentView = (ContentPanel) pane.getRightComponent();
			TabComponent tabComponent = contentView.getSelectedTabComponent();
			if (tabComponent.isDirectory || Zip.isArchive(tabComponent.name, true)) {
				return;
			}
			contentView.showFilterPanel(new FilterPanel());
			pane.validate();
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

	private static final FolderTableCellRenderer FOLDER_TABLE_CELL_RENDERER = new FolderTableCellRenderer();

	private final JarTree jarTree;

	private final StatusBar statusBar;

	private final JFrame frame;

	TreePath collapsed;

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
		if (collapsed != null && collapsed.equals(event.getPath())) {
			jarTree.clearSelection();
			collapsed = null;
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
		final Object obj = jarTree.getLastSelectedPathComponent();
		if (obj == null) {
			return;
		}

		new Executor() {

			@Override
			protected void perform() {
				if (obj instanceof ClassItemNode) {
					ClassItemNode itemNode = (ClassItemNode) obj;
					for (Component comp : ((Content) frame.getContentPane()).getComponents()) {
						if (!(comp instanceof JSplitPane)) {
							continue;
						}
						TreeNode parent = itemNode.getParent();
						while (parent instanceof ClassItemNode) {
							parent = parent.getParent();
						}
						ContentPanel contentPanel = (ContentPanel)((JSplitPane) comp).getRightComponent();
						if (parent != null) {
							String fullPath = ((JarNode) parent).getFullPath();
							TabComponent tabComponent = contentPanel.getSelectedTabComponent();
							if (!fullPath.equals(tabComponent.node.getFullPath())) {
								contentPanel.setSelected(fullPath);
								break;
							}
						}
						JTextArea area = contentPanel.getSelectedComponent();
						Position position = itemNode.getPosition();
						((JarNode)parent).selectedChild = itemNode; 
						FilterPanel.highlight(area, position.position, position.length);
						break;
					}
					return;
				}

				final JarNode node = (JarNode) obj;
				final JSplitPane pane = Content.getSplitPane();
				final ContentPanel contentView = (ContentPanel) pane.getRightComponent();
				final int dividerLocation = pane.getDividerLocation();

				try {
					if (node.isNotClass() && (!node.isLeaf() || node.isDirectory)) {
						statusBar.setPath(node.getFullPath());
						statusBar.setCompiledVersion("");
						Content.preLoadArchive(node, new PreLoadAction() {
							@Override
							public void perform() {
								JTable table = new JTable(new JarNodeTableModel(node, statusBar));
								table.setFillsViewportHeight(true);
								table.setAutoCreateRowSorter(true);
								TableHeaderCustomizer.customize(table);
								table.setDefaultRenderer(Object.class, FOLDER_TABLE_CELL_RENDERER);
								table.setDefaultRenderer(Number.class, FOLDER_TABLE_CELL_RENDERER);
								table.addMouseListener(new FolderTableMouseAdapter(table, node));
								
								JComponent tablePane = new JScrollPane(table);
								tablePane.setBorder(Settings.EMPTY_BORDER);

								contentView.addContent(tablePane, node, statusBar, true);
							}
						});
					} else {
						statusBar.enableProgress("Loading...");
						statusBar.setPath(node.getFullPath());
						statusBar.setCompiledVersion("");
						statusBar.setChildren("");

						File file;
						String lowPath;
						if (node.getParent() == null && (lowPath = node.origArch.getName().toLowerCase()).endsWith(".class")) {
							file = node.origArch;
						} else {
							file = new File(node.origArch.getParent(), node.path);
							lowPath = node.path.toLowerCase();
						}

						if (lowPath.endsWith(".class")) {
							statusBar.enableProgress("Decompiling...");
							Result decompiled = decompile(node);
							statusBar.setCompiledVersion(decompiled.version);
							RSyntaxTextArea textArea = new RSyntaxTextArea(decompiled.content);
							textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
							textArea.setBorder(Settings.EMPTY_BORDER);
							textArea.setCodeFoldingEnabled(true);
							textArea.setEditable(false);
							applyTheme(textArea);

							RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
							textScrollPane.setBorder(Settings.EMPTY_BORDER);

							contentView.addContent(textScrollPane, node, statusBar);

							try {
								fillClassStructure(node, Analyzer.analyze(decompiled.content));
							} catch (Exception e) {
								log.log(Level.SEVERE, "Unable to grab class structure information for " + node.name, e);
							}
						} else if (isImgFile(lowPath)) {
							ZipFile zip = null;
							try {
								zip = new ZipFile(node.getTempArchive());
								ZipEntry entry = zip.getEntry(node.path);
								InputStream stream = zip.getInputStream(entry);
								Image image = ImageIO.read(stream);
								if (image == null) {
									JOptionPane.showConfirmDialog(frame, "Could not read image " + node.path, "Error",
											JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
									return;
								}

								contentView.addContent(new JScrollPane(new ImgPanel(image)), node, statusBar);
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
							try {
								File tmp = File.createTempFile("jarexp", "ico", Settings.getJarexpTmpDir());
								tmp = Zip.unzip(node.getFullPath(), node.path, node.getTempArchive(), tmp);
								JPanel pnl = new JPanel();
								for (BufferedImage icon : Ico.read(tmp)) {
									pnl.add(new ImgPanel(icon));
								}
								contentView.addContent(new JScrollPane(pnl), node, statusBar);
							} catch (IOException e) {
								throw new JarexpException("Unable to render ico " + node.getFullPath(), e);
							}
						} else {
							if (!file.isDirectory()) {
								statusBar.enableProgress("Reading...");
								RSyntaxTextArea textArea = new RSyntaxTextArea(Zip.unzip(node.getTempArchive(), node.path));
								textArea.setSyntaxEditingStyle(getSyntax(lowPath));
								textArea.setBorder(Settings.EMPTY_BORDER);
								textArea.setCodeFoldingEnabled(true);
								textArea.setEditable(true);
								textArea.getDocument().addDocumentListener(new TextAreaDocumentListener(contentView));
								applyTheme(textArea);
								textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK),
										new AbstractAction() {
											private static final long serialVersionUID = -3016470783134782605L;

											@Override
											public void actionPerformed(ActionEvent event) {
												contentView.saveChanges();
											}
										});

								RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
								textScrollPane.setBorder(Settings.EMPTY_BORDER);

								contentView.addContent(textScrollPane, node, statusBar);
							}
						}
					}
				} finally {
					pane.validate();
					if (pane.getDividerLocation() != dividerLocation) {
						log.warning("Divider location was changed. Compensating...");
						pane.setDividerLocation(dividerLocation);
					}
				}
			}

			private void fillClassStructure(JarNode node, List<IJavaItem> classStructure) {
				node.removeAllChildren();
				node.selectedChild = null;
				for (IJavaItem item : classStructure) {
					ClassItemNode child = new ClassItemNode(item);
					node.add(child);
					for (IJavaItem m : item.getChildren()) {
						child.add(new ClassItemNode(m));
					}
				}
				jarTree.update(node);
			}

			private Result decompile(JarNode node) {
				IDecompiler decompiler = Decompiler.get();
				File archive = node.getTempArchive();
				return "".equals(node.path) ? decompiler.decompile(archive) : decompiler.decompile(archive, node.path);
			}

			@Override
			protected void doFinally() {
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

		private ContentPanel contentPanel;

		public TextAreaDocumentListener(ContentPanel contentPanel) {
			this.contentPanel = contentPanel;
		}

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
			TabComponent tabComponent = contentPanel.getSelectedTabComponent();
			if (!tabComponent.isEdited) {
				tabComponent.isEdited = true;
			}
		}
	}

	private class FolderTableMouseAdapter extends MouseAdapter {

		private JTable table;
		private JarNode node;

		FolderTableMouseAdapter(JTable table, JarNode node) {
			this.table = table;
			this.node = node;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			final int row = table.rowAtPoint(e.getPoint());
			final int col = table.columnAtPoint(e.getPoint());
			if (row < 0 || col < 0) {
				table.clearSelection();
				return;
			}
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				String name = (String) table.getModel().getValueAt(row, 0);
				JarTreeClickSelection.setNodes(null);
				jarTree.isNotDraw = true;
				jarTree.clearSelection();
				TreeNode[] nodes = new TreeNode[node.getPath().length + 1];
				for (int i = 0; i < nodes.length; ++i) {
					if (i == nodes.length - 1) {
						@SuppressWarnings("unchecked")
						Enumeration<JarNode> children = node.children();
						while (children.hasMoreElements()) {
							JarNode jarNode = children.nextElement();
							if (name.equals(jarNode.name)) {
								nodes[i] = jarNode;
								break;
							}
						}
					} else {
						nodes[i] = node.getPath()[i];
					}
				}
				jarTree.setSelectionPath(new TreePath(nodes));
			} else if (SwingUtilities.isRightMouseButton(e)) {
				if (!table.isRowSelected(row)) {
					table.setRowSelectionInterval(row, row);
				}
				boolean multiplySelection = table.getSelectedRowCount() > 1;
				Resources resources = Resources.getInstance();
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem copyCell = new JMenuItem("Copy cell");
				copyCell.setIcon(resources.getCopyIcon());
				copyCell.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(new StringSelection(table.getModel().getValueAt(row, col).toString()),
								null);
					}
				});
				copyCell.setEnabled(!multiplySelection);
				JMenuItem copyRow = new JMenuItem(multiplySelection ? "Copy rows" : "Copy row");
				copyRow.setIcon(resources.getCopyIcon());
				copyRow.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringBuilder out = new StringBuilder();
						TableModel model = table.getModel();
						for (int row : table.getSelectedRows()) {
							for (int i = 0; i < model.getColumnCount(); ++i) {
								out.append(model.getValueAt(row, i));
								if (i < model.getColumnCount() - 1) {
									out.append(';');
								}
							}
							out.append('\n');
						}

						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(new StringSelection(out.toString()), null);
					}
				});
				popupMenu.add(copyCell);
				popupMenu.add(copyRow);

				popupMenu.show(table, e.getX(), e.getY());
			}
		}
	}

	private static class FolderTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -3854438137640730745L;

		private static final Color HIGHLIGHTED = new Color(242, 241, 227);
		private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (isSelected) {
				if (hasFocus) {
					FolderTableCellRenderer renderer = (FolderTableCellRenderer) component;
					renderer.setBorder(BORDER);
					return renderer;
				}
				return component;
			}
			if (row % 2 == 1) {
				component.setBackground(HIGHLIGHTED);
			} else {
				component.setBackground(Color.WHITE);
			}
			return component;
		}
	}

	static class ClassItemNode extends DefaultMutableTreeNode {

		private static final long serialVersionUID = 7470246996120563613L;

		IJavaItem javaItem;

		private final String name;

		ClassItemNode(IJavaItem javaItem) {
			this.javaItem = javaItem;
			this.name = javaItem.getName();
		}

		Position getPosition() {
			return javaItem.getPosition();
		}

		@Override
		public String toString() {
			return name;
		}

	} 

}
