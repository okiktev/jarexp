
package com.delfin.jarexp.frame;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.Version;
import com.delfin.jarexp.frame.about.Dialog;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.frame.resources.Resources.ResourcesException;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

public class Content extends JPanel {

	private static final Logger log = Logger.getLogger(Content.class.getCanonicalName());

	private static final long serialVersionUID = 2832926850075095267L;
	
	public static final Border emptyBorder = BorderFactory.createEmptyBorder();
	
	private static TreeExpansionListener treeExpansionListener = new TreeExpansionListener() {

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			JarNode node = (JarNode) event.getPath().getLastPathComponent();
			if (node == null) {
				return;
			}
			boolean isNeedToFill = false;
			for (Enumeration<?> childred = node.children(); childred.hasMoreElements();) {
				JarNode child = (JarNode) childred.nextElement();
				String path = child.path;
				//System.out.println(path);
				if (Settings.NAME_PLACEHOLDER.equals(path)) {
					isNeedToFill = true;
					node.removeAllChildren();
					break;
				}
			}
			if (isNeedToFill) {
				
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						statusBar.enableProgress("Loading...");
						File dst = new File(Resources.createTmpDir(), node.name);
						Zip.unzipFrom(node.path, node.archive, dst);
						try {
							jarTree.addArchive(dst, node);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						jarTree.update(node);
						statusBar.disableProgress();
						return null;
					}
				}.execute();
			}
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			// nothing to do
		}
		
	};
	
	
	private static TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {

		@Override
		public void valueChanged(TreeSelectionEvent event) {
			
			if (jarTree.isDragging()) {
				return;
			}
			JarNode node = (JarNode) jarTree.getLastSelectedPathComponent();
			if (node == null || !node.isLeaf()) {
				return;
			}

			File file = new File(node.archive.getParent(), node.path);
			if (node.isDirectory) {
				return;
			}
			
			statusBar.setPath(node.path);
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					
					Content current = (Content) frame.getContentPane();
					JSplitPane pane = (JSplitPane) current.getComponent(1);
					Component contentView = pane.getRightComponent();
					
					statusBar.enableProgress("Loading...");
					
					Zip.unzipFrom(node.path, node.archive, file);
					String content = null;
					String lowPath = node.path.toLowerCase();
					statusBar.setCompiledVersion("");
					if (lowPath.endsWith(".class")) {
						
						statusBar.enableProgress("Decompiling...");
						content = com.delfin.jarexp.utils.Compiler.decompile(file);
						
						statusBar.setCompiledVersion(Version.getCompiledJava(file));
						
					      RSyntaxTextArea textArea = new RSyntaxTextArea();
					      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
					         Theme theme = Theme.load(getClass().getResourceAsStream(
					                 "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));
					           theme.apply(textArea);
					      
					      textArea.setCodeFoldingEnabled(true);
					      textArea.setText(content);
					      textArea.setBorder(emptyBorder);
					      textArea.setEditable(false);
						
					      Dimension size = contentView.getPreferredSize();
					      
					      pane.remove(contentView);
					      contentView = new RTextScrollPane(textArea);
					      contentView.setPreferredSize(size);
						statusBar.disableProgress();
					} else if (isImgFile(lowPath)) {
						try {
							JPanel img = new ImgPanel(ImageIO.read(file));
							img.setBorder(emptyBorder);
							pane.remove(contentView);
							contentView = new JScrollPane(img);
						} catch (IOException e) {
							throw new JarexpException("Couldn't read file " + file + " as image", e);
						}
					} else {
						if (!file.isDirectory()) {
							statusBar.enableProgress("Reading...");
							
							content = FileUtils.toString(file);
							
						      RSyntaxTextArea textArea = new RSyntaxTextArea();
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
						      }
						      
						      textArea.setSyntaxEditingStyle(syntax);
						      textArea.setBorder(emptyBorder);
						      textArea.setCodeFoldingEnabled(true);
						      textArea.setText(content);
						      textArea.setEditable(false);
							
						      Dimension size = contentView.getPreferredSize();
						      
						      pane.remove(contentView);
						      contentView = new RTextScrollPane(textArea);
						      contentView.setPreferredSize(size);
						}
					}
					
					((JComponent)contentView).setBorder(emptyBorder);
					pane.setRightComponent(contentView);

					pane.validate();
					pane.repaint();
					statusBar.disableProgress();
					return null;
				}


			}.execute();
		}

		private boolean isImgFile(String fileName) {
			return fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".jpg")
					|| fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
		}

	};
	
	private static DropTargetListener treeDropTargetListener = new DropTargetListener() {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			jarTree.setDragging(true);
			JarNode node = getNode(dtde);
			if (node.isLeaf()) {
				dtde.rejectDrag();
			} else {
				dtde.acceptDrag(dtde.getDropAction());
			}
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			JarNode node = getNode(dtde);
			if (node.isLeaf() || jarTree.isPacking()) {
				dtde.rejectDrag();
			} else {
				jarTree.setSelectionPath(new TreePath(node.getPath()));
				dtde.acceptDrag(dtde.getDropAction());
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			jarTree.setDragging(false);
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			if (jarTree.isPacking()) {
				dtde.rejectDrop();
				return;
			}
			
			JarNode node = getNode(dtde);
			try {
				final List<File> droppedFiles = new ArrayList<File>();
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				if (flavors.length > 1) {
					log.warning("There are " + flavors.length + " flavors found");
				}
				
				for (int i = 0; i < flavors.length; i++) {
					if (tr.isDataFlavorSupported(flavors[i])) {
						dtde.acceptDrop(dtde.getDropAction());
						Object obj = tr.getTransferData(flavors[i]);
						if (obj instanceof List<?>) {
							droppedFiles.clear();
							for (Object o : (List<?>)obj) {
								if (!(o instanceof File)) {
									continue;
								}
								droppedFiles.add((File)o);
							}
						}
					}
				}
				if (droppedFiles.isEmpty()) {
					JOptionPane.showConfirmDialog(frame,
					        "There is wrong dopped data format. Expected only files list.", 
					        "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					dtde.dropComplete(true);
					return;
				}
				int reply = JOptionPane.showConfirmDialog(frame,
				        "Do you want to add files " + droppedFiles + " into " + node.name, "Adding files confirmation",
				        JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							statusBar.enableProgress("Packing...");
							jarTree.setPacking(true);
							packIntoJar(node, droppedFiles);
							statusBar.disableProgress();
							jarTree.setDragging(false);
							jarTree.setPacking(false);
							dtde.dropComplete(true);
							return null;
						}
					}.execute();
				}
			} catch (Exception e) {
				throw new JarexpException("An error occurred while adding data into jar", e);
			} finally {
				jarTree.setDragging(false);
			}
		}

		private void packIntoJar(JarNode node, List<File> droppedFiles) throws IOException {
			// System.out.println(node);

			List<JarNode> path = new ArrayList<JarNode>();
			JarNode n = node;
			do {
				path.add(n);
				n = (JarNode) n.getParent();
			} while (n != null);

			List<File> files = new ArrayList<File>(droppedFiles.size());
			for (File f : droppedFiles) {
				files.add(f);
			}
			JarNode i = path.get(0);
			JarNode prevInfo = path.get(path.size() - 1);
			for (JarNode info : path) {
				if (JarTree.isArchive(info.name)) {
					// System.out.println("Adding " + i.path + " | " +
					// prevInfo.archive + " | " + files);
					Zip.add(i.path, prevInfo.archive, files);
					// System.out.println("Added " + i.path + " | " +
					// prevInfo.archive + " | " + files);
					files.clear();
					files.add(prevInfo.archive);
					i = info;
				}
				prevInfo = info;
			}

			Zip.copy(prevInfo.archive, new File(path.get(path.size() - 1).name));

			placeIntoTree(node, droppedFiles);
			jarTree.update(node);
		}

		private void placeIntoTree(JarNode node, List<File> files) {
			for (File f : files) {
				String path = node.path + f.getName();
				boolean isDir = f.isDirectory();
				if (isDir) {
					path += "/";
				}
				JarNode child = new JarNode(f.getName(), path, node.archive, isDir);
				node.add(child);
				if (isDir) {
					placeIntoTree(child, Arrays.asList(f.listFiles()));
				}
			}
		}
		

		private JarNode getNode(DropTargetDragEvent dtde) {
			return getNodeByLocation(dtde.getLocation());
		}
		
		private JarNode getNode(DropTargetDropEvent dtde) {
			return getNodeByLocation(dtde.getLocation());
		}
		
		private JarNode getNodeByLocation(Point point) {
			TreePath parentPath = jarTree.getClosestPathForLocation(point.x, point.y);
			return (JarNode) parentPath.getLastPathComponent();
		}

	};

	private static JFrame frame;
	private static JarTree jarTree;
	private static File file;
	private static StatusBar statusBar;

	private Content() {
		super(new BorderLayout());
		JPanel initPanel = new ImgPanel(Resources.getInstance().getDragImage());
		initPanel.setPreferredSize(new Dimension(Settings.getInstance().getFrameWidth(), 400));

		add(initPanel);
		add(statusBar = new StatusBar(this), BorderLayout.SOUTH);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 * @throws ResourcesException 
	 */
	public static void createAndShowGUI() throws ResourcesException {
		// Create and set up the window.
		frame = new JFrame("Jar Explorer " + Settings.getInstance().getVersion());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				try {
					delete(Settings.getInstance().getTmpDir());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			void delete(File f) throws IOException {
				if (f.isDirectory()) {
					for (File c : f.listFiles()) {
						try {
							delete(c);
						} catch (Exception e) {
							
						}
					}
				}
				if (!f.delete()) {
					//throw new FileNotFoundException("Failed to delete file: " + f);
				}
			}
		});

		frame.setJMenuBar(new Menu(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select jar file to open");
				FileFilter filter = new FileNameExtensionFilter("Jar Files (*.jar,*.war,*.ear)", "jar", "war", "ear");
				chooser.addChoosableFileFilter(filter);
				chooser.setFileFilter(filter);
				if (file != null) {
					chooser.setCurrentDirectory(file.getParentFile());
				}

				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (!f.exists()) {
						// TODO show error message;
					}
					loadJarFile(file = f);
				}
			}
		}, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog(frame);
			}
		}));

		// Create and set up the content pane.
		Content newContentPane = new Content();
		((JComponent)newContentPane).setBorder(emptyBorder);
		//newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);
		frame.setDropTarget(new DropTarget() {

			private static final long serialVersionUID = -2086424207425075731L;

			public synchronized void drop(DropTargetDropEvent evt) {
		        try {

		        	
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            @SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            
		        	System.out.println("from content " + droppedFiles);
		            
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
	}
	
	protected static void loadJarFile(File f) {
//		System.out.println("dasds");
//		
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				System.out.println("loaded file " + f);
				statusBar.enableProgress("Loading...");
				
				JSplitPane pane = getSplitPane();
				pane.setBorder(emptyBorder);
				Component treeView = pane.getLeftComponent();
				pane.remove(treeView);
				
				try {
					jarTree = new JarTree(f, treeSelectionListener, treeExpansionListener, treeDropTargetListener);
					jarTree.setBorder(emptyBorder);
					treeView = new JScrollPane(jarTree);
					((JComponent)treeView).setBorder(emptyBorder);
					System.out.println("$$$$$$$$$$$$");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				pane.setLeftComponent(treeView);
				JScrollPane contentView = new JScrollPane();
				contentView.setBorder(emptyBorder);
				pane.setRightComponent(contentView);
		
				frame.validate();
				frame.repaint();		
				statusBar.disableProgress();
				return null;
			}

			private JSplitPane getSplitPane() {
				Content current = (Content) frame.getContentPane();
				for (Component comp : current.getComponents()) {
					if (comp instanceof ImgPanel) {
						frame.remove(comp);
						JSplitPane pane = new JSplitPane();
						pane.setRightComponent(new JScrollPane());
						frame.add(pane);
						return pane;
					} if (comp instanceof JSplitPane) {
						return (JSplitPane) comp;
					}
				}
				throw new JarexpException("Couldn't find split pane on frame");
			}
			
		}.execute();
		

	}

}