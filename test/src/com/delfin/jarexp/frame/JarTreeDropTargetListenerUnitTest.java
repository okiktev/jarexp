package com.delfin.jarexp.frame;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.delfin.jarexp.frame.JarTree.ArchiveLoader;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

public class JarTreeDropTargetListenerUnitTest extends BaseUnitTest {

	private JarTreeDropTargetListener listener = new JarTreeDropTargetListener(jarTree, bar, null);

	@Test
	public void testAdd_FileIntoJstlJarMetaInf() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir/file")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(1, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/file"));
	}

	@Test
	public void testAdd_DirIntoJstlJarMetaInf() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(2, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/dir"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/dir/file"));
	}
	
	@Test
	public void testAdd_FileIntoJstlJarRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar");
		//List<JarNode> assertion = getAssertion(node);

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir/file")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(1, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/file"));
	}

	@Test
	public void testAdd_DirIntoJstlJarRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(2, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/dir"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-web.war/WEB-INF/lib/jstl.jar/dir/file"));
	}
	
	@Test
	public void testAdd_JarIntoJstlJarRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/slf4j-api.jar")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(3, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/slf4j-api.jar"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-web.war/WEB-INF/lib/jstl.jar/slf4j-api.jar/META-INF"));
		Assert.assertTrue(isPath(contentAfter.get(2), "bookstore-web.war/WEB-INF/lib/jstl.jar/slf4j-api.jar/META-INF/MANIFEST.MF"));
	}
	
	@Test
	public void testAdd_JarIntoJstlJarMetaInf() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/slf4j-api.jar")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(3, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/slf4j-api.jar"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/slf4j-api.jar/META-INF"));
		Assert.assertTrue(isPath(contentAfter.get(2), "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/slf4j-api.jar/META-INF/MANIFEST.MF"));
	}

	@Test
	public void testAdd_JarIntoRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "/");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/slf4j-api.jar")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(3, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "slf4j-api.jar"));
		Assert.assertTrue(isPath(contentAfter.get(1), "slf4j-api.jar/META-INF"));
		Assert.assertTrue(isPath(contentAfter.get(2), "slf4j-api.jar/META-INF/MANIFEST.MF"));
	}
	
	@Test
	public void testAdd_FileIntoRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "/");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir/file")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(1, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "file"));
	}
	
	@Test
	public void testAdd_DirIntoRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "/");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(2, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "dir"));
		Assert.assertTrue(isPath(contentAfter.get(1), "dir/file"));
	}
	
	@Test
	public void testAdd_JarIntoJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/slf4j-api.jar")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(3, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-ejb.jar/spring.jar/slf4j-api.jar"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-ejb.jar/spring.jar/slf4j-api.jar/META-INF"));
		Assert.assertTrue(isPath(contentAfter.get(2), "bookstore-ejb.jar/spring.jar/slf4j-api.jar/META-INF/MANIFEST.MF"));
	}
	
	@Test
	public void testAdd_FileIntoJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir/file")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(1, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-ejb.jar/spring.jar/file"));
	}
	
	@Test
	public void testAdd_DirIntoJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar");

		listener.packIntoJar(node, getDroppedFiles(new File("test/resources/dir")));
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentBefore.isEmpty());
		Assert.assertEquals(2, contentAfter.size());
		Assert.assertTrue(isPath(contentAfter.get(0), "bookstore-ejb.jar/spring.jar/dir"));
		Assert.assertTrue(isPath(contentAfter.get(1), "bookstore-ejb.jar/spring.jar/dir/file"));
	}
	
	
	private static boolean isPath(JarNode node, String path) {
		List<JarNode> list = node.getPathList();
		Collections.reverse(list);
		String [] paths = path.split("/");
		if (list.size() == paths.length + 1) {
			
		}
		for (int i = 0; i < paths.length; ++i) {
			if (!paths[i].equals(list.get(i + 1).name)) {
				return false;
			}
		}
		return true;
	}

	private List<File> getDroppedFiles(File file) {
		List<File> res = new ArrayList<File>();
		res.add(file);
		return res;
	}

	private List<JarNode> loadBefore() throws IOException {
		jarTree = new JarTree(null, null, null);
		jarTree.setDropTarget(new DropTarget(jarTree, DnDConstants.ACTION_COPY, listener));

		clearTmp();
		// copy file into temp
		test = File.createTempFile("test", "add.ear", tmp);
		FileUtils.copy(new File("test/resources/test.ear"), test);

		// load file
		JarTree.archiveLoader = new ArchiveLoader() {
			@Override
			protected void load(JarNode node) {

				File dst = new File(tmp, node.name);
				Zip.unzip(node.path, node.archive, dst);
				try {
					jarTree.addArchive(dst, node);
				} catch (Exception e) {
					throw new RuntimeException("Unable add archive into node", e);
				}
			}
		};
		jarTree.load(test);
		jarTree.update(jarTree.getRoot());
		return getContent(jarTree);
	}

	private List<JarNode> loadAfter() throws IOException {
		// reload file
		jarTree.load(test);
		jarTree.update(jarTree.getRoot());
		return getContent(jarTree);
	}

	@AfterClass
	public static void clearTmp() throws IOException {
		delete(tmp);
		tmp.mkdirs();
	}

}
