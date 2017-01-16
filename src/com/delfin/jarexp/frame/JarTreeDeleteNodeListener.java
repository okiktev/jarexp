package com.delfin.jarexp.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.TreeNode;

import com.delfin.jarexp.frame.JarNode.JarNodeMenuItem;
import com.delfin.jarexp.utils.Zip;

class JarTreeDeleteNodeListener implements ActionListener {

	private final StatusBar statusBar;
	
	private final JarTree jarTree;
	
	JarTreeDeleteNodeListener(JarTree jarTree, StatusBar statusBar) {
		this.jarTree = jarTree;
		this.statusBar = statusBar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JarNodeMenuItem item = (JarNodeMenuItem) e.getSource();
		JarNode node = (JarNode) item.path.getLastPathComponent();
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				statusBar.enableProgress("Removing...");
				try {
					delFromJar(node);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				TreeNode parent = node.getParent();
				jarTree.remove(node);
				jarTree.update(parent);
				statusBar.disableProgress();
				return null;
			}
		}.execute();
	}

	void delFromJar(JarNode node) throws IOException {
		List<JarNode> path = node.getPathList();

		JarNode currNode = path.get(0);
		String delPath = currNode.path;
		Zip.delete(currNode.path, currNode.archive);
		System.out.println("del " + currNode.path + " | " + currNode.archive);


		List<JarNode> archives = node.grabParentArchives();
		System.out.println(archives);
		//if (archives.size() > 2) {
			// JarNode prevNode = path.get(path.size() - 1);

			List<File> files = new ArrayList<File>();
			files.add(currNode.archive);
			currNode = archives.get(0);
			for (int i = 1; i < archives.size(); ++i) {
				JarNode arc = archives.get(i);
				if (!currNode.path.equals(delPath)) {
					System.out.println("Adding " + currNode.path + " | " + arc.getCurrentArchive() + " | " + files);
					Zip.add(currNode.path, arc.getCurrentArchive(), files);
					// System.out.println("Added " + currNode.path + " | " + arc.getCurrentArchive() + " | " + files);
				}
				files.clear();
				files.add(arc.getCurrentArchive());
				currNode = arc;
			}
		//}
		Zip.copy(path.get(path.size() - 1).archive, new File(path.get(path.size() - 1).name));
	}
}
