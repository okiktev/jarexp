package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.utils.Enumerator;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

abstract class Jar {

	private static final Logger log = Logger.getLogger(Jar.class.getCanonicalName());

	private final File file;

	Jar(File file) {
		this.file = file;
	}

	void bypass() {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				process(entries.nextElement());
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while processing jar file " + file, e);
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

	static void pack(JarNode node, File file) throws IOException {
		List<File> files = new ArrayList<File>(1);
		files.add(file);
		pack(node, files);
	}

	static void pack(JarNode node, List<File> files) {
		try {
    		List<JarNode> path = node.getPathList();
    		JarNode currNode = path.get(0);
    		String p = currNode.path;
    		File archive = currNode.archive;
    		if (node.isDirectory || !node.isLeaf()) {
    			p = currNode.path.endsWith(currNode.name) ? "" : currNode.path;
    			archive = p.isEmpty() ? currNode.getCurrentArchive() : currNode.archive;
    		}
    		Zip.add(p, archive, files);

    		files.clear();
    		files.add(archive);

    		List<JarNode> archives = node.grabParentArchives();
    		p = archives.get(0).path;
    		for (int i = 1; i < archives.size(); ++i) {
    			JarNode arc = archives.get(i);
    			Zip.add(p, arc.getCurrentArchive(), files);
    			files.clear();
    			files.add(arc.getCurrentArchive());
    			p = arc.path;
    		}
    		JarNode root = path.get(path.size() - 1);
    		FileUtils.copy(root.archive, new File(root.name));
		} catch (Exception e) {
			throw new JarexpException("An error occurred while packing node " + node.path, e);
		}
	}

    static void delete(JarNode node, boolean withCopy) {
        List<JarNode> nodes = new ArrayList<JarNode>(1);
        nodes.add(node);
        delete(nodes, withCopy);
    }

	static void delete(List<JarNode> nodes, boolean withCopy) {
		try {
		    List<JarNode> path = null;
		    JarNode root = null;
		    for (JarNode node : nodes) {
	            path = node.getPathList();
	            JarNode currNode = path.get(0);
	            if (currNode.archive == null) {
	                continue;
	            }
	            root = path.get(path.size() - 1);
	            String delPath = currNode.path;
	            Zip.delete(currNode.path, currNode.archive);

	            List<JarNode> archives = node.grabParentArchives();
	            List<File> files = new ArrayList<File>();
	            files.add(currNode.archive);
                if (currNode.isArchive()) {
                    File arch = currNode.archive;
                    clearArchive(currNode);
                    currNode.archive = arch;
                }
	            currNode = archives.get(0);
	            for (int i = 1; i < archives.size(); ++i) {
	                JarNode arc = archives.get(i);
	                if (!currNode.path.equals(delPath)) {
	                    Zip.add(currNode.path, arc.getCurrentArchive(), files);
	                }
	                files.clear();
	                files.add(arc.getCurrentArchive());
	                currNode = arc;
	            }
		    }

			if (withCopy) {
				FileUtils.copy(root.archive, new File(root.name));
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while deleting node " + nodes, e);
		}
	}

	@SuppressWarnings("unchecked")
    private static void clearArchive(JarNode node) {
	    node.archive = null;
	    new Enumerator<JarNode>(node.children()) {
            @Override
            protected void doAction(JarNode entity) {
                clearArchive(entity);
            }
        };
    }

    static void delete(List<JarNode> nodes) {
		delete(nodes, true);
	}

	protected abstract void process(JarEntry entry) throws IOException;

}
