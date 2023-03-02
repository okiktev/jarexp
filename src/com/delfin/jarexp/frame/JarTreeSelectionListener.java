package com.delfin.jarexp.frame;

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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.analyzer.Analyzer;
import com.delfin.jarexp.analyzer.IJavaItem;
import com.delfin.jarexp.analyzer.IJavaItem.Position;
import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.IDecompiler;
import com.delfin.jarexp.decompiler.IDecompiler.Result;
import com.delfin.jarexp.frame.Content.PreLoadAction;
import com.delfin.jarexp.frame.ContentPanel.TabComponent;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Executor;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.FolderTableCellRenderer;
import com.delfin.jarexp.utils.ImgPanel;
import com.delfin.jarexp.utils.RstaUtils;
import com.delfin.jarexp.utils.TableHeaderCustomizer;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.StreamProcessor;
import com.delfin.jarexp.win.icon.Ico;

import static javax.swing.JOptionPane.*;


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
		if (statusBar.isEnabled) {
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
						JSplitPane split = (JSplitPane) comp;
						ContentPanel contentPanel = (ContentPanel)split.getRightComponent();
						if (parent != null) {
							String fullPath = ((JarNode) parent).getFullPath();
							TabComponent tabComponent = contentPanel.getSelectedTabComponent();
							if (!fullPath.equals(tabComponent.node.getFullPath())) {
								contentPanel.setSelected(fullPath);
							}
						}
						JTextArea area = contentPanel.getSelectedComponent();
						Position position = itemNode.getPosition();
						((JarNode)parent).selectedChild = itemNode; 
						FilterPanel.highlight(area, position.position, position.length);
						split.repaint();
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
							RstaUtils.applyTheme(textArea);

							RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
							textScrollPane.setBorder(Settings.EMPTY_BORDER);

							contentView.addContent(textScrollPane, node, statusBar);

							try {
								fillClassStructure(node, Analyzer.analyze(decompiled.content));
							} catch (Exception e) {
								log.log(Level.SEVERE, "Unable to grab class structure information for " + node.name, e);
							}
						} else if (FileUtils.isImgFile(lowPath)) {
							Zip.stream(node.getTempArchive(), node.path, new StreamProcessor() {
								@Override
								public void process(InputStream stream) throws IOException {
									Image image = ImageIO.read(stream);
									if (image == null) {
										showConfirmDialog(frame, "Could not read image " + node.path, "Error", DEFAULT_OPTION, ERROR_MESSAGE);
									} else {
										contentView.addContent(new JScrollPane(new ImgPanel(image)), node, statusBar);
									}
								}
							});
						} else if (lowPath.endsWith(".ico")) {
							Zip.stream(node.getTempArchive(), node.path, new StreamProcessor() {
								@Override
								public void process(InputStream stream) throws IOException {
									JPanel pnl = new JPanel();
									for (BufferedImage icon : Ico.read(stream)) {
										pnl.add(new ImgPanel(icon));
									}
									contentView.addContent(new JScrollPane(pnl), node, statusBar);
								}
							});
						} else {
							if (!file.isDirectory()) {
								statusBar.enableProgress("Reading...");
								RSyntaxTextArea textArea = new RSyntaxTextArea(Zip.unzip(node.getTempArchive(), node.path));
								textArea.setSyntaxEditingStyle(RstaUtils.getSyntax(lowPath));
								textArea.setBorder(Settings.EMPTY_BORDER);
								textArea.setCodeFoldingEnabled(true);
								textArea.setEditable(true);
								textArea.getDocument().addDocumentListener(new TextAreaDocumentListener(contentView));
								RstaUtils.applyTheme(textArea);
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
					pane.repaint();
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

		}.execute();
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
			final int r = table.rowAtPoint(e.getPoint());
			final int c = table.columnAtPoint(e.getPoint());
			if (r < 0 || c < 0) {
				table.clearSelection();
				return;
			}
			final int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
			final int col = table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint()));
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
				if (!table.isRowSelected(r)) {
					table.setRowSelectionInterval(r, r);
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
						for (int row : getSelectedRows(table)) {
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

					private int[] getSelectedRows(JTable table) {
						int[] rows = table.getSelectedRows();
						int[] res = new int[rows.length];
						for (int i = 0; i < rows.length; ++i) {
							res[i] = table.convertRowIndexToModel(rows[i]);
						}
						return res;
					}
				});
				popupMenu.add(copyCell);
				popupMenu.add(copyRow);

				popupMenu.show(table, e.getX(), e.getY());
			}
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
