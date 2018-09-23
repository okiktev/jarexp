
package com.delfin.jarexp.frame;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.frame.JarTree.JarTreeClickSelection;
import com.delfin.jarexp.frame.about.AboutDlg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;
import com.delfin.jarexp.frame.search.SearchDlg;
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

		new Executor() {
			@Override
			protected void perform() {
				boolean isNotInitiated = false;
				for (Enumeration<?> childred = node.children(); childred.hasMoreElements();) {
					JarNode child = (JarNode) childred.nextElement();
					if (Settings.NAME_PLACEHOLDER.equals(child.path)) {
						node.removeAllChildren();
						isNotInitiated = true;
						break;
					}
				}
				if (isNotInitiated) {
					statusBar.enableProgress("Loading...");
					File dst = new File(Resources.createTmpDir(), node.name);
					dst = Zip.unzip(node.getFullPath(), node.path, node.archive, dst);
					jarTree.addArchive(dst, node);
					jarTree.update(node);
				}
				if (action != null) {
					action.perform();
				}
			}
			@Override
			protected void doFinally() {
				statusBar.disableProgress();
			}
		}.execute();
	}

	public static void createAndShowGUI(File passedFile) throws ResourcesException {
		// Create and set up the window.
		frame = new JFrame("Jar Explorer " + Settings.getInstance().getVersion());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				final boolean[] isDeleted = { false };
				new Executor() {
					@Override
					protected void perform() {
						statusBar.enableProgress("Exiting...");
						try {
							delete(Settings.getTmpDir());
							isDeleted[0] = true;
						} catch (IOException e) {
							throw new JarexpException(e);
						}
					}

					@Override
					protected void doFinally() {
						statusBar.disableProgress();
					}
				}.execute();
				while (true) {
					if (isDeleted[0]) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new JarexpException(e);
					}
				}
			}

			void delete(File f) throws IOException {
				if (f.isDirectory()) {
					for (File c : f.listFiles()) {
						try {
							delete(c);
						} catch (Exception e) {
							throw new JarexpException("An error occurred while deleting the file " + f.getAbsolutePath());
						}
					}
				}
				if (!f.delete()) {
					throw new JarexpException("Could not delete file " + f.getAbsolutePath());
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
						JOptionPane.showMessageDialog(frame, "Specified file does not exist.", "Wrong input", JOptionPane.ERROR_MESSAGE);
					} else {
						loadJarFile(file = f);
					}
				}
			}
		}, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (file == null) {
					JOptionPane.showMessageDialog(frame, "Archive is not loaded.", "Wrong input", JOptionPane.WARNING_MESSAGE);
					return;
				} else {
					new SearchDlg(file) {
						private static final long serialVersionUID = -838103554183752603L;						
						@Override
						protected void initComponents() {
							super.initComponents();
							tResult.addMouseListener(new MouseAdapter() {
								public void mousePressed(MouseEvent e) {
									if (e.getClickCount() != 2) {
										return;
									}
									int row = tResult.getSelectedRow();
									if (row != -1) {
										String fullPath = (String) tResult.getModel().getValueAt(row, 0);
										JarNode node = jarTree.getRoot();
										String [] items = fullPath.split("/");
										for (int i = 0; i < items.length; ++i) {
											String el = items[i];
											if (el.isEmpty()) {
												continue;
											}
											if (i == items.length - 1) {
												jarTree.expandPath(new TreePath(node.getPath()));
												Enumeration<?> children = node.children();
												while(children.hasMoreElements()) {
													JarNode child = (JarNode) children.nextElement();
													if (child.name.equals(el)) {
														JarTreeClickSelection.setNodes(null);
														TreePath path = new TreePath(child.getPath());
														jarTree.setSelectionPath(path);
														jarTree.scrollPathToVisible(path);
														break;
													}
												}
												break;
											}
											boolean isArchive = false;
											if (el.charAt(el.length() - 1) == '!') {
												el = el.replace("!", "");
												isArchive = true;
											}
											Enumeration<?> children = node.children();
											while(children.hasMoreElements()) {
												JarNode child = (JarNode) children.nextElement();
												if (child.name.equals(el)) {
													if (isArchive) {
														jarTree.expandPath(new TreePath(child.getPath()));
														while (true) {
															try {
																Thread.sleep(50);
															} catch (InterruptedException ex) {
																throw new JarexpException("Error happens while waiting for archive leaf is loaded.", ex);
															}
															if (!Settings.NAME_PLACEHOLDER.equals(((JarNode)child.getLastChild()).name)) {
																break;
															}
														}
													}
													node = child;
													break;
												}
											}
										}
									}
								}
							});
						};
					};
				}
			}
		}, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDlg(frame);
			}
		}));

		// Create and set up the content pane.
		Content content = new Content();
		((JComponent)content).setBorder(Settings.EMPTY_BORDER);
		//newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(content);
		frame.setDropTarget(new DropTarget() {

			private static final long serialVersionUID = -2086424207425075731L;

			public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            @SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
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
				pane.remove(treeView);	
				
				treeView = new JScrollPane(jarTree);
				((JComponent) treeView).setBorder(Settings.EMPTY_BORDER);

				pane.setLeftComponent(treeView);
				JScrollPane contentView = new JScrollPane();
				contentView.setBorder(Settings.EMPTY_BORDER);
				pane.setRightComponent(contentView);

				frame.setTitle((title == null ? title = frame.getTitle() : title) + " | " + file.getName());

				frame.validate();
				frame.repaint();
				jarTreeSelectionListener.setDividerLocation(treeView.getWidth());
				pane.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (!jarTreeSelectionListener.isLocked()) {
							jarTreeSelectionListener.setDividerLocation(pane.getDividerLocation());
						}
					}
				});

				if (jarTree.isSingleFileLoaded()) {
				    jarTree.setSelectionPaths(new TreePath[] {new TreePath(jarTree.getRoot())});
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
				JSplitPane pane = new JSplitPane();
				pane.setRightComponent(new JScrollPane());
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