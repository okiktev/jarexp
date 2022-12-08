package com.delfin.jarexp.frame.mtool;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.mtool.ContentPanel.TabComponent;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.Executor;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.FolderTableCellRenderer;
import com.delfin.jarexp.utils.RstaUtils;
import com.delfin.jarexp.utils.TableHeaderCustomizer;
import com.delfin.jarexp.utils.Zip;


class RepoTreeSelectionListener implements TreeSelectionListener {

	private static final FolderTableCellRenderer FOLDER_TABLE_CELL_RENDERER = new FolderTableCellRenderer();

	private final RepoTree repoTree;

	private final StatusBar statusBar;

	private final JFrame frame;

	TreePath collapsed;

	RepoTreeSelectionListener(RepoTree repoTree, StatusBar statusBar, JFrame frame) {
		this.repoTree = repoTree;
		this.statusBar = statusBar;
		this.frame = frame;
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		final Object obj = repoTree.getLastSelectedPathComponent();
		if (obj == null) {
			return;
		}
		new Executor() {
			@Override
			protected void perform() {
				statusBar.enableProgress("Checking...");
				RepoNode node = (RepoNode) obj;
				if (node.files == null || node.files.isEmpty()) {
					return;
				}
				for (Component comp : ((MtoolPanel) frame.getContentPane()).getComponents()) {
					if (!(comp instanceof JSplitPane)) {
						continue;
					}
					
					JTable table = new JTable(new RepoNodeTableModel(node, statusBar));
					table.setFillsViewportHeight(true);
					table.setAutoCreateRowSorter(true);
					TableHeaderCustomizer.customize(table);
					table.setDefaultRenderer(Object.class, FOLDER_TABLE_CELL_RENDERER);
					table.setDefaultRenderer(Number.class, FOLDER_TABLE_CELL_RENDERER);
					table.addMouseListener(new FolderTableMouseAdapter(table, node));

					JPanel pnl = new JPanel();
					pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));
					pnl.add(table.getTableHeader());
					pnl.add(table);

					JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
					splitPane.setTopComponent(pnl);

					ContentPanel contentPanel = (ContentPanel)((JSplitPane) comp).getRightComponent();
					contentPanel.addContent(splitPane, node, statusBar, node.file.isDirectory());
					break;
				}
			}
			@Override
			protected void doFinally() {
				statusBar.disableProgress();
			}
		}.execute();
	}
		
	private class FolderTableMouseAdapter extends MouseAdapter {

		private JTable table;
		private RepoNode node;

		FolderTableMouseAdapter(JTable table, RepoNode node) {
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
			
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				String name = (String) table.getModel().getValueAt(row, 1);
				for(File f : node.files) {
					if (f.getName().equals(name)) {
						if (Zip.isArchive(name, true)) {
							if (Desktop.isDesktopSupported()) {
								try {
									Desktop.getDesktop().open(f);
								} catch (IOException ex) {
									Msg.showException("Unable to open file " + f, ex);
								}
							} else {
								showMessageDialog(frame, "Unable to open path to " + f, "Information", WARNING_MESSAGE);
							}
							break;
						}

						statusBar.enableProgress("Reading...");

						RSyntaxTextArea textArea = new RSyntaxTextArea(FileUtils.toString(f));
						textArea.setSyntaxEditingStyle(RstaUtils.getSyntax(name.toLowerCase()));
						textArea.setBorder(Settings.EMPTY_BORDER);
						textArea.setCodeFoldingEnabled(true);
						textArea.setEditable(true);
						RstaUtils.applyTheme(textArea);

						RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
						textScrollPane.setBorder(Settings.EMPTY_BORDER);

						for (Component comp : ((MtoolPanel) frame.getContentPane()).getComponents()) {
							if (!(comp instanceof JSplitPane)) {
								continue;
							}
							ContentPanel contentPanel = (ContentPanel)((JSplitPane) comp).getRightComponent();
							TabComponent tab = contentPanel.getSelectedTabComponent();
							JSplitPane dir = ((JSplitPane)tab.content);
							dir.setBottomComponent(textScrollPane);
							
							contentPanel.repaint();
							break;
						}
						statusBar.disableProgress();
						break;
					}
				}
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
						clipboard.setContents(new StringSelection(table.getModel().getValueAt(row, col).toString()), null);
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

}
