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

import javax.swing.tree.TreeNode;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.utils.Enumerator;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

public abstract class Jar {

	private static final Logger log = Logger.getLogger(Jar.class.getCanonicalName());

	protected final File file;
	protected JarFile jarFile;

	public Jar(File file) {
		this.file = file;
	}

	public void bypass() {
		bypass(null);
	}

	public void bypass(JarBypassErrorAction errorAction) {
	    if (file == null || file.length() == 0) {
	        return;
	    }
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				process(entries.nextElement());
			}
		} catch (Exception e) {
			if (errorAction == null) {
				throw new JarexpException("An error occurred while processing jar file " + file, e);
			}
			RuntimeException toThrow = errorAction.apply(e);
			if (toThrow != null) {
				throw toThrow;
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
    		File archive = currNode.getTempArchive();
    		if (node.isDirectory || !node.isLeaf()) {
    			p = currNode.path.endsWith(currNode.name) ? "" : currNode.path;
    			archive = p.isEmpty() ? currNode.getCurrentArchive() : currNode.getTempArchive();
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
    		FileUtils.copy(root.getTempArchive(), new File(root.name));
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
	            if (currNode.getTempArchive() == null) {
	                continue;
	            }
	            root = path.get(path.size() - 1);
	            String delPath = currNode.path;
	            Zip.delete(currNode.path, currNode.getTempArchive());

	            List<JarNode> archives = node.grabParentArchives();
	            List<File> files = new ArrayList<File>();
	            files.add(currNode.getTempArchive());
                if (currNode.isArchive()) {
                    File arch = currNode.getTempArchive();
                    clearArchive(currNode);
                    currNode.setTempArchive(arch);
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
	            node.closeTab();
		    }

			if (withCopy) {
				FileUtils.copy(root.getTempArchive(), new File(root.name));
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while deleting node " + nodes, e);
		}
	}

    @SuppressWarnings("unchecked")
	private static void clearArchive(JarNode node) {
	    node.setTempArchive(null);
	    new Enumerator<TreeNode>(node.children()) {
            @Override
            protected void doAction(TreeNode entity) {
        		JarNode node  = (JarNode)entity;
                clearArchive(node);
            }
        };
    }

    static void delete(List<JarNode> nodes) {
		delete(nodes, true);
	}

	protected abstract void process(JarEntry entry) throws IOException;

	public static abstract class JarBypassErrorAction {

		public abstract RuntimeException apply(Exception e);

	}

}
