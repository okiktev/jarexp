package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

abstract class Jar {

	private static final Logger log = Logger.getLogger(Jar.class.getCanonicalName());

	private final File file;

	Jar(File file) {
		this.file = file;
	}

	void bypass() throws IOException {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				process(entries.nextElement());
			}
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Couldn't close jar file " + jarFile.getName(), e);
				}
			}
		}
	}
	
	static void pack(JarNode node, List<File> files) throws IOException {
		List<JarNode> path = node.getPathList();
		JarNode currNode = path.get(0);
		String p = currNode.path.endsWith(currNode.name) ? "" : currNode.path;
		File archive = p.isEmpty() ? currNode.getCurrentArchive() : currNode.archive;
		System.out.println("Adding " + p + " | " + archive + " | " + files);
		Zip.add(p, archive, files);
		
		//Zip.delete(currNode.path, currNode.archive);
		//JarNode prevNode = path.get(path.size() - 1);

		files.clear();
		files.add(archive);

		List<JarNode> archives = node.grabParentArchives();
		System.out.println(archives);
//		do {
//			if (JarTree.isArchive(currNode.name)) {
//				p = currNode.path;
//				break;
//			}
//			currNode = (JarNode) currNode.getParent();
//		} while(currNode != null);
		p = archives.get(0).path;
		//currNode = archives.get(0);
		for (int i = 1; i < archives.size(); ++i) {
			JarNode arc = archives.get(i);
			//p = arc.path.endsWith(arc.name) ? "" : arc.path;
			//archive = p.isEmpty() ? arc.getCurrentArchive() : arc.archive;
			
			System.out.println("Adding " + p + " | " + arc.getCurrentArchive() + " | " + files);
			Zip.add(p, arc.getCurrentArchive(), files);
			// System.out.println("Added " + currNode.path + " | " + arc.getCurrentArchive() + " | " + files);
			files.clear();
			files.add(arc.getCurrentArchive());
			p = arc.path;
		}
		System.out.println("Copying " + path.get(path.size() - 1).archive + " into " + new File(path.get(path.size() - 1).name));
		FileUtils.copy(path.get(path.size() - 1).archive, new File(path.get(path.size() - 1).name));
		
		
		
		
		
//		// System.out.println(node);
//
//		List<JarNode> path = new ArrayList<JarNode>();
//		JarNode n = node;
//		do {
//			path.add(n);
//			n = (JarNode) n.getParent();
//		} while (n != null);
//
//		List<File> files = new ArrayList<File>(droppedFiles.size());
//		for (File f : droppedFiles) {
//			files.add(f);
//		}
//		
//		JarNode i = path.get(0);
//		JarNode prevInfo = path.get(path.size() - 1);
//		for (JarNode info : path) {
//			if (JarTree.isArchive(info.name)) {
//				// String p = i.path.endsWith(i.name) ? "" : i.path;
//				String p = cutName(i.path);
//				// File archive = p.isEmpty() ? info.getCurrentArchive() : prevInfo.archive;
//				File archive = info.getCurrentArchive();
//				System.out.println("Adding " + p + " | " + archive + " | " + files);
//				Zip.add(p, archive, files);
//				System.out.println("Added " + p + " | " + archive + " | " + files);
//				files.clear();
//				files.add(archive);
//				i = info;
//			}
//			prevInfo = info;
//		}

		// Zip.copy(prevInfo.archive, new File(path.get(path.size() - 1).name));
	}

	protected abstract void process(JarEntry entry) throws IOException;

}
