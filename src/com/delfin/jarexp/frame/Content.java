package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;

import com.delfin.jarexp.decompiler.Decompiler;
import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.frame.about.AboutDlg;
import com.delfin.jarexp.frame.about.EnvironmentDlg;
import com.delfin.jarexp.frame.about.ProcessesDlg;
import com.delfin.jarexp.frame.duplicates.DuplicatesDlg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;
import com.delfin.jarexp.frame.search.SearchDlg;
import com.delfin.jarexp.frame.search.SearchResult;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Utils;
import com.delfin.jarexp.utils.Zip;

public class Content extends JPanel {

	private static final Logger log = Logger.getLogger(Content.class.getCanonicalName());

	private static final long serialVersionUID = 2832926850075095267L;

	private static TreeExpansionListener treeExpansionListener = new TreeExpansionListener() {

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			preLoadArchive((JarNode) event.getPath().getLastPathComponent(), null);
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			// nothing to do
		}

	};

	static class SearchResultMouseAdapter extends MouseAdapter {

		private final JComboBox<String> cbFind;

		private final JTable tResult;

		private final SearchEntries searchEntries;

		SearchResultMouseAdapter(JComboBox<String> cbFind, JTable tResult, SearchEntries searchEntries) {
			this.searchEntries = searchEntries;
			this.cbFind = cbFind;
			this.tResult = tResult;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() != 2) {
				return;
			}
			int row = tResult.getSelectedRow();
			if (row == -1) {
				return;
			}
			TableModel tableModel = tResult.getModel();
			final SearchResult searchResult = (SearchResult) tableModel.getValueAt(row, 0);
			if (jarTree != null && searchEntries.getSearchPath().equals(jarTree.getRoot().name)) {
				switch (searchResult.position) {
				case -1: jarTree.expandTreeLeaf(searchResult.line); break;
				case -2: showMessageDialog(frame, searchResult.line, "Information", INFORMATION_MESSAGE); break;
				default:
					if (searchResult.position < -1) {
						showMessageDialog(frame, "Unknown result code: " + searchResult.line, "Error", ERROR_MESSAGE);
						break;
					}
					String fullPath = null;
					while (row != -1) {
						SearchResult fileSearch = (SearchResult) tableModel.getValueAt(--row, 0);
						if (fileSearch.position == -1) {
							jarTree.expandTreeLeaf(fullPath = fileSearch.line);
							break;
						}
					}
					ContentPanel contentPanel = (ContentPanel) getSplitPane().getRightComponent();
					while (!fullPath.equals(contentPanel.getSelectedTabComponent().fullPath)) {
						Utils.sleep(50);
					}
					JTextArea area = contentPanel.getSelectedComponent();
					try {
						int position = searchResult.position;
						area.scrollRectToVisible(area.modelToView(position));
						Highlighter hilit = new RSyntaxTextAreaHighlighter();
						area.setHighlighter(hilit);
						hilit.addHighlight(position, position + ((String) cbFind.getSelectedItem()).length()
								, FilterPanel.DEFAULT_HIGHLIGHT_PAINTER);
					} catch (BadLocationException ex) {
						throw new JarexpException("Could not scroll to found index.", ex);
					}
				}
			} else if (Desktop.isDesktopSupported()) {
					SearchResult sr = searchResult;
					while (sr.position != -1) {
						sr = (SearchResult) tableModel.getValueAt(--row, 0);
					}
					String pathToFile = sr.line;
					int idx = pathToFile.indexOf('!');
					if (idx != -1) {
						pathToFile = pathToFile.substring(0, idx);
					}
					File fileToOpen = new File(pathToFile);
					if (!fileToOpen.exists()) {
						fileToOpen = new File(searchEntries.getSearchPath());
					}
					try {
						Desktop.getDesktop().open(fileToOpen);
					} catch (IOException ex) {
						Msg.showException("Unable to open file " + fileToOpen, ex);
					}
				} else {
					showMessageDialog(frame, "Unable to open path to " + searchResult.line, "Information", WARNING_MESSAGE);
				}
			}
	}

	static interface PreLoadAction {
		void perform();
	}

	private static String title;
	private static JFrame frame;
	private static JarTree jarTree;
	private static File file;
	private static StatusBar statusBar;
	private static JarTreeSelectionListener jarTreeSelectionListener;

	private Content() {
		super(new BorderLayout());

		JPanel initPanel = new ImgPanel(Resources.getInstance().getDragImage());
		initPanel.setPreferredSize(new Dimension(Settings.getInstance().getFrameWidth(), 400));
		add(initPanel);
		add(statusBar = new StatusBar(this), BorderLayout.SOUTH);
	}

	static void preLoadArchive(final JarNode node, final PreLoadAction action) {
		if (node == null) {
			return;
		}
		try {
			for (Enumeration<?> childred = node.children(); childred.hasMoreElements();) {
				JarNode child = (JarNode) childred.nextElement();
				if (!Settings.NAME_PLACEHOLDER.equals(child.path)) {
					continue;
				}
				node.removeAllChildren();
				statusBar.enableProgress("Loading...");
				File dst = new File(Resources.createTmpDir(), node.name);
				dst = Zip.unzip(node.getFullPath(), node.path, node.getTempArchive(), dst);
				jarTree.addArchive(dst, node);
				jarTree.update(node);
				break;
			}
			if (action != null) {
				action.perform();
			}
		} finally {
			statusBar.disableProgress();
		}
	}

	public static void createAndShowGUI(File passedFile) throws ResourcesException {
		// Create and set up the window.
		frame = new JFrame("Jar Explorer " + Settings.getInstance().getVersion());
		if (Settings.X != 0 && Settings.Y != 0) {
			frame.setLocation(Settings.X, Settings.Y);
		}
		frame.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				Settings.X = frame.getX();
				Settings.Y = frame.getY();
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				final boolean[] isDeleted = { false };
				new Executor() {
					@Override
					protected void perform() {
						statusBar.enableProgress("Exiting...");
						FileUtils.delete(Settings.getTmpDir());
						isDeleted[0] = true;
					}

					@Override
					protected void doFinally() {
						statusBar.disableProgress();
					}
				}.execute();
				while (!isDeleted[0]) {
					Utils.sleep(100);
				}
			}
		});

		frame.setJMenuBar(new Menu(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
				chooser.setDialogTitle("Select jar file to open");
				FileFilter filter = new FileNameExtensionFilter("Jar Files (*.jar,*.war,*.ear,*.zip,*.apk)", "jar", "war", "ear", "zip", "apk");
				chooser.addChoosableFileFilter(filter);
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("Class Files (*.class)", "class"));
				chooser.setFileFilter(filter);
				if (file != null) {
					chooser.setCurrentDirectory(file.getParentFile());
				}
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (!f.exists()) {
						showMessageDialog(frame, "Specified file does not exist.", "Wrong input", ERROR_MESSAGE);
					} else {
						loadJarFile(file = f);
					}
				}
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SearchEntries searchEntries = new SearchEntries();
				File f = file;
				if (file == null) {
					List<File> files = ActionHistory.getLastDirSelected();
					f = files.isEmpty() ? Settings.getUserHome() : files.get(0);
				}
				searchEntries.add(f, null, f.getAbsolutePath(), f.isDirectory());
				new SearchDlg(searchEntries) {
					private static final long serialVersionUID = -838103554183752603L;
					@Override
					protected void initComponents() {
						super.initComponents();
						tResult.addMouseListener(new SearchResultMouseAdapter(cbFind, tResult, searchEntries));
					};
				};

			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SearchEntries searchEntries = new SearchEntries();
				File f = file;
				if (file == null) {
					List<File> files = ActionHistory.getLastDirSelected();
					f = files.isEmpty() ? Settings.getUserHome() : files.get(0);
				}
				searchEntries.add(f, null, f.getAbsolutePath(), f.isDirectory());
				new DuplicatesDlg(searchEntries) {
					private static final long serialVersionUID = 7499714177978424203L;
					@Override
					protected void initComponents() {
						super.initComponents();
						spResult.addMouseListener(new MouseAdapter() {
							public void mousePressed(MouseEvent e) {
								if (e.getClickCount() != 2) {
									return;
								}
								SearchResult searchResult = spResult.getSelectedSearchResult();
								if (searchResult == null) {
									return;
								}
								if (searchEntries.getSearchPath().equals(jarTree.getRoot().name)) {
									jarTree.expandTreeLeaf(searchResult.line);
								} else if (Desktop.isDesktopSupported()) {
									String pathToFile = searchResult.line;
									int idx = pathToFile.indexOf('!');
									if (idx != -1) {
										pathToFile = pathToFile.substring(0, idx);
									}
									File fileToOpen = new File(pathToFile);
									if (!fileToOpen.exists()) {
										fileToOpen = new File(searchEntries.getSearchPath());
									}
									try {
										Desktop.getDesktop().open(fileToOpen);
									} catch (IOException ex) {
										Msg.showException("Unable to open file " + fileToOpen, ex);
									}
								} else {
									showMessageDialog(frame, "Unable to open file " + searchResult.line, "Information", WARNING_MESSAGE);
								}
							}
						});
					}
				};
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeDecompiler(DecompilerType.JDCORE);
				statusBar.setDecompiler(DecompilerType.JDCORE);
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Executor() {
					@Override
					protected void perform() {
						try {
							statusBar.enableProgress("Downloading...");
							Decompiler.prepareBinariesFor(DecompilerType.PROCYON);
							changeDecompiler(DecompilerType.PROCYON);
							statusBar.setDecompiler(DecompilerType.PROCYON);
						} catch (Exception ex) {
							Msg.showException("Unable to toggle decompiler", ex);
						} finally {
							statusBar.disableProgress();
						}
					}
				}.execute();
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Executor() {
					@Override
					protected void perform() {
						try {
							statusBar.enableProgress("Downloading...");
							Decompiler.prepareBinariesFor(DecompilerType.FERNFLOWER);
							changeDecompiler(DecompilerType.FERNFLOWER);
							statusBar.setDecompiler(DecompilerType.FERNFLOWER);
						} catch (Exception ex) {
							Msg.showException("Unable to toggle decompiler", ex);
						} finally {
							statusBar.disableProgress();
						}
					}
				}.execute();
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ProcessesDlg(frame);
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EnvironmentDlg(frame);
			}
		}
		, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDlg(frame);
			}
		}));

		// Create and set up the content pane.
		Content content = new Content();
		((JComponent) content).setBorder(Settings.EMPTY_BORDER);
		// newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(content);
		frame.setDropTarget(new DropTarget() {

			private static final long serialVersionUID = -2086424207425075731L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) evt.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
					for (File f : droppedFiles) {
						loadJarFile(file = f);
						break;
					}
				} catch (Exception e) {
					throw new JarexpException("Unable to receive list of dropped files", e);
				}
			}
		});
		frame.setIconImage(Resources.getInstance().getLogoImage());
		frame.pack();
		frame.setVisible(true);

		if (passedFile != null) {
			loadJarFile(file = passedFile);
		}
	}

	protected static void changeDecompiler(DecompilerType type) {
		Settings.setDecompilerType(type);
		if (jarTree == null) {
			return;
		}
		JarTreeClickSelection.setNodes(null);
		JarNode node = (JarNode) jarTree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		jarTree.isNotDraw = true;
		jarTree.clearSelection();
		jarTree.setSelectionPath(new TreePath(node.getPath()));
	}

	protected static void loadJarFile(final File f) {
		log.fine("Loading file " + f);

		new Executor() {
			@Override
			protected void perform() {
				statusBar.enableProgress("Loading...");

				jarTree = new JarTree(treeExpansionListener, statusBar, frame);
				jarTree.addTreeSelectionListener(jarTreeSelectionListener = new JarTreeSelectionListener(jarTree, statusBar, frame));
				jarTree.load(f);
				jarTree.setBorder(Settings.EMPTY_BORDER);

				final JSplitPane pane = getSplitPane();
				pane.setBorder(Settings.EMPTY_BORDER);

				Component treeView = pane.getLeftComponent();
				treeView = new JScrollPane(jarTree);
				((JComponent) treeView).setBorder(Settings.EMPTY_BORDER);
				pane.setLeftComponent(treeView);

				JScrollPane contentView = new JScrollPane();
				contentView.setBorder(Settings.EMPTY_BORDER);
				pane.setRightComponent(new ContentPanel(contentView, jarTree));

				frame.setTitle((title == null ? title = frame.getTitle() : title) + " | " + file.getName());

				frame.validate();
				frame.repaint();

				frame.getRootPane().registerKeyboardAction(jarTreeSelectionListener.new FilterAction(),
						KeyStroke.getKeyStroke("ctrl F"), JComponent.WHEN_IN_FOCUSED_WINDOW);

				if (jarTree.isSingleFileLoaded()) {
					JarTreeClickSelection.setNodes(null);
					jarTree.setSelectionPath(new TreePath(jarTree.getRoot()));
				}
			}

			@Override
			protected void doFinally() {
				statusBar.disableProgress();
			}

		}.execute();
	}

	static JSplitPane getSplitPane() {
		Content current = (Content) frame.getContentPane();
		for (Component comp : current.getComponents()) {
			if (comp instanceof ImgPanel) {
				frame.remove(comp);
				JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
				frame.add(pane);
				return pane;
			}
			if (comp instanceof JSplitPane) {
				return (JSplitPane) comp;
			}
		}
		throw new JarexpException("Couldn't find split pane on frame");
	}

}