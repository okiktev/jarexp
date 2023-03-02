package com.delfin.jarexp.frame.mtool;

import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.LibraryManager;
import com.delfin.jarexp.frame.mtool.ContentPanel.PanelContainer;
import com.delfin.jarexp.frame.mtool.ContentPanel.TabComponent;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Cmd;
import com.delfin.jarexp.utils.Cmd.EnvironmentVariable;
import com.delfin.jarexp.utils.Cmd.ErrorReader;
import com.delfin.jarexp.utils.Cmd.OutputReader;
import com.delfin.jarexp.utils.Executor;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.FileUtils.ReadProcessor;
import com.delfin.jarexp.win.icon.Ico;
import com.delfin.jarexp.utils.FolderTableCellRenderer;
import com.delfin.jarexp.utils.ImgPanel;
import com.delfin.jarexp.utils.RstaUtils;
import com.delfin.jarexp.utils.TableHeaderCustomizer;
import com.delfin.jarexp.utils.Zip;


class RepoTreeSelectionListener implements TreeSelectionListener {

	private static final FolderTableCellRenderer FOLDER_TABLE_CELL_RENDERER = new FolderTableCellRenderer();

	private final RepoTree repoTree;

	private final StatusBar statusBar;

	private final JFrame frame;

	TreePath collapsed;

	private static class ConsoleDlg extends JFrame {
		private static final long serialVersionUID = -2568920673863824935L;
		private static final Font FONT = new Font("Consolas", Font.PLAIN, 12);
		private static final Dimension DIM = new Dimension(550, 300);
		JTextArea area = new JTextArea();
		ConsoleDlg(String fileName) {
			setTitle("Console | " + fileName);
			setIconImage(Resources.getMtoolConsoleImage());
			setPreferredSize(DIM);
			area.setFont(FONT);
			area.setBackground(Color.BLACK);
			((DefaultCaret)area.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			add(new JScrollPane(area));
			Msg.centerDlg(this, DIM, MtoolDlg.MTOOL_TITLE);
			setVisible(true);
			pack();
		}
		void out(String line) {
			area.setForeground(Color.GREEN);
			area.append(line + '\n');
		}
		void error(String line) {
			area.setForeground(Color.RED);
			area.append(line + '\n');
		}
	}

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
				final RepoNode node = (RepoNode) obj;
				if (node.file.isFile()) {
					new PanelContainer(frame) {
						@Override
						protected void receive(ContentPanel contentPanel) {
							try {
								statusBar.enableProgress("Reading...");
								renderContent(contentPanel, node);
							} finally {
								statusBar.disableProgress();
							}
						}
					};
					return;
				}
				if (node.files == null || node.files.isEmpty()) {
					return;
				}
				new PanelContainer(frame) {
					@Override
					protected void receive(ContentPanel contentPanel) {
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
						contentPanel.addContent(splitPane, node, statusBar, node.file.isDirectory());
					}
				};
			}
			@Override
			protected void doFinally() {
				statusBar.disableProgress();
			}
		}.execute();
	}

	private void renderContent(final ContentPanel contentPanel, final RepoNode node) {
		String lowPath = node.file.getName().toLowerCase();
		if (FileUtils.isImgFile(lowPath)) {
			FileUtils.read(node.file, new ReadProcessor() {
				@Override
				public void process(InputStream stream) throws IOException {
					Image image = ImageIO.read(stream);
					if (image == null) {
						showConfirmDialog(frame, "Could not read an image " + node.file, "Error", DEFAULT_OPTION, ERROR_MESSAGE);
					} else {
						contentPanel.addContent(new JScrollPane(new ImgPanel(image)), node, statusBar);
					}
				}
			});
		} else if (lowPath.endsWith(".ico")) {
			FileUtils.read(node.file, new ReadProcessor() {
				@Override
				public void process(InputStream stream) throws IOException {
					JPanel pnl = new JPanel();
					for (BufferedImage icon : Ico.read(stream)) {
						pnl.add(new ImgPanel(icon));
					}
					contentPanel.addContent(new JScrollPane(pnl), node, statusBar);
				}
			});
		} else {
			RSyntaxTextArea textArea = new RSyntaxTextArea(FileUtils.toString(node.file));
			textArea.setSyntaxEditingStyle(RstaUtils.getSyntax(lowPath));
			textArea.setBorder(Settings.EMPTY_BORDER);
			textArea.setCodeFoldingEnabled(true);
			textArea.setEditable(true);
			RstaUtils.applyTheme(textArea);

			RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
			textScrollPane.setBorder(Settings.EMPTY_BORDER);

			contentPanel.addContent(textScrollPane, node, statusBar);
		}
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
			final int r = table.rowAtPoint(e.getPoint());
			final int c = table.columnAtPoint(e.getPoint());
			if (r < 0 || c < 0) {
				table.clearSelection();
				return;
			}
			final int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
			final int col = table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint()));

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
						final File file = f;
						new PanelContainer(frame) {
							@Override
							protected void receive(ContentPanel contentPanel) {
								try {
									statusBar.enableProgress("Reading...");
									previewContent(contentPanel, file);
								} finally {
									statusBar.disableProgress();
								}
							}
						};
						break;
					}
				}
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

				final File selFile = getSelectedFileByName((String) table.getModel().getValueAt(row, 1));
				JMenuItem tree = new JMenuItem("Tree");
				tree.setIcon(resources.getTreeIcon());
				tree.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						LibraryManager.prepareMaven();						
						final String[] command = compile(selFile);
						final ConsoleDlg consoleDlg = new ConsoleDlg(selFile.getName());
						Executors.newSingleThreadExecutor().execute(new Runnable() {
							@Override
							public void run() {
								Cmd.run(command, null, getEnvironmentVariables()
										, new OutputReader() {
											@Override
											protected void processLine(String line) {
												consoleDlg.out(line);
											};
										}
										, new ErrorReader() {
											@Override
											protected void processLine(String line) {
												consoleDlg.error(line);
											};
										});
							}
							
						});
					}

					private String[] compile(File selFile) {
						File mvnDir = new File(Settings.getAppDir(), "maven");
						if (Version.IS_WINDOWS) {
							File mvn = new File(mvnDir, "/bin/mvn.cmd");
							return new String[] { "cmd.exe", "/c", mvn.getAbsolutePath()
									, "-f", selFile.getAbsolutePath()
									, "org.apache.maven.plugins:maven-dependency-plugin:3.4.0:tree" };
						} else {
							File mvn = new File(mvnDir, "/bin/mvn");
							return new String[] { "sh", mvn.getAbsolutePath()
									, "-f", selFile.getAbsolutePath()
									, "org.apache.maven.plugins:maven-dependency-plugin:3.4.0:tree" };

						}
					}

					private EnvironmentVariable[] getEnvironmentVariables() {
						List<EnvironmentVariable> list = new ArrayList<EnvironmentVariable>();
						boolean isJavaHomeAdded = false;
						for (Entry<String, String> e : System.getenv().entrySet()) {
							if ("JAVA_HOME".equals(e.getKey())) {
								list.add(new EnvironmentVariable("JAVA_HOME", System.getProperty("java.home")));
								isJavaHomeAdded = true;
							} else {
								list.add(new EnvironmentVariable(e.getKey(), e.getValue()));
							}
						}
						if (!isJavaHomeAdded) {
							list.add(new EnvironmentVariable("JAVA_HOME", System.getProperty("java.home")));
						}
						return list.toArray(new EnvironmentVariable[list.size()]);
					}
				});

				popupMenu.add(copyCell);
				popupMenu.add(copyRow);
				if (selFile.getName().toLowerCase().endsWith(".pom")) {
					popupMenu.add(tree);
				}

				popupMenu.show(table, e.getX(), e.getY());
			}
		}

		private File getSelectedFileByName(String name) {
			for(File f : node.files) {
				if (f.getName().equals(name)) {
					return f;
				}
			}
			return null;
		}

		private void previewContent(final ContentPanel contentPanel, final File f) {
			TabComponent tab = contentPanel.getSelectedTabComponent();
			final JSplitPane dir = ((JSplitPane)tab.content);
			String lowPath = f.getName().toLowerCase();
			if (FileUtils.isImgFile(lowPath)) {
				FileUtils.read(f, new ReadProcessor() {
					@Override
					public void process(InputStream stream) throws IOException {
						Image image = ImageIO.read(stream);
						if (image == null) {
							showConfirmDialog(frame, "Could not read an image " + f, "Error", DEFAULT_OPTION, ERROR_MESSAGE);
						} else {
							dir.setBottomComponent(new JScrollPane(new ImgPanel(image)));
						}
					}
				});
			} else if (lowPath.endsWith(".ico")) {
				FileUtils.read(f, new ReadProcessor() {
					@Override
					public void process(InputStream stream) throws IOException {
						JPanel pnl = new JPanel();
						for (BufferedImage icon : Ico.read(stream)) {
							pnl.add(new ImgPanel(icon));
						}
						dir.setBottomComponent(new JScrollPane(pnl));
					}
				});
			} else {
				RSyntaxTextArea textArea = new RSyntaxTextArea(FileUtils.toString(f));
				textArea.setSyntaxEditingStyle(RstaUtils.getSyntax(lowPath));
				textArea.setBorder(Settings.EMPTY_BORDER);
				textArea.setCodeFoldingEnabled(true);
				textArea.setEditable(true);
				RstaUtils.applyTheme(textArea);

				RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
				textScrollPane.setBorder(Settings.EMPTY_BORDER);

				dir.setBottomComponent(textScrollPane);
			}
			
			contentPanel.repaint();
		}
	}

}
