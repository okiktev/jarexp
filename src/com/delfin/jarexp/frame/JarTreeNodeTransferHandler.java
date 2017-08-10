package com.delfin.jarexp.frame;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.frame.resources.Resources;

class JarTreeNodeTransferHandler extends TransferHandler {

	private class Pair {
		Pair(JarNode node, File file) {
			this.node = node;
			this.file = file;
		}

		JarNode node;
		File file;
	}

	private List<Pair> map = new ArrayList<Pair>();

	private static final Logger log = Logger.getLogger(JarTreeNodeTransferHandler.class.getCanonicalName());

	private static final long serialVersionUID = 912067810455805511L;

	private List<JarNode> nodes = new ArrayList<JarNode>();

	private final StatusBar statusBar;

	private class JarNodeTransferable implements Transferable {

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.javaFileListFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			List<File> files = new ArrayList<File>();
			if (nodes != null) {
				for (JarNode node : nodes) {
					File file = getFile(node);
					if (file == null) {
						final File f = file = node.getParent() == null 
						        ? new File(node.name) 
						        : new File(Resources.createTmpDir(), node.name);
						final JarNode n = node;
						new Executor() {
							@Override
							protected void perform() {
								statusBar.enableProgress("Extracting...");
								if (log.isLoggable(Level.FINE)) {
									log.fine("Extracting file " + n.path);
								}
								n.unzip(f);
							}

							@Override
							protected void doFinally() {
								statusBar.disableProgress();
							};
						}.execute();
					}
					map.add(new Pair(node, file));
					files.add(file);
				}
			}
			return files;
		}

		private File getFile(JarNode node) {
			for (Pair p : map) {
				if (p.node.eq(node)) {
					return p.file;
				}
			}
			return null;
		}
	}

	JarTreeNodeTransferHandler(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		nodes.clear();
		map.clear();
		super.exportDone(source, data, action);
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		for (TreePath path : ((JarTree) c).getSelectionPaths()) {
			nodes.add((JarNode) path.getLastPathComponent());
		}
		return new JarNodeTransferable();
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

}
