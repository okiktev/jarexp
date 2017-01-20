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
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;

import com.delfin.jarexp.frame.resources.Resources;

class JarTreeNodeTransferHandler extends TransferHandler {

	private static final Logger log = Logger.getLogger(JarTreeNodeTransferHandler.class.getCanonicalName());

	private static final long serialVersionUID = 912067810455805511L;

	private JarNode node;

	private final StatusBar statusBar;

	private class JarNodeTransferable implements Transferable {

		private File file;

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
			List<File> files = new ArrayList<File>(1);
			if (node != null) {
				if (file == null) {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							statusBar.enableProgress("Extracting...");
							if (log.isLoggable(Level.FINE)) {
								log.fine("Extracting file " + node.path);
							}
							node.unzip(file = new File(Resources.createTmpDir(), node.name));
							statusBar.disableProgress();
							return null;
						}
					}.execute();
				}
				files.add(file);
			}
			return files;
		}

	}

	JarTreeNodeTransferHandler(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		node = null;
		super.exportDone(source, data, action);
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		node = (JarNode) ((JarTree) c).getLastSelectedPathComponent();
		return new JarNodeTransferable();
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

}
