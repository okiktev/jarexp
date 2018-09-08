package com.delfin.jarexp.frame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.delfin.jarexp.frame.JarTree.ArchiveLoader;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Zip;

public class JarTreeDeleteNodeListenerUnitTest extends BaseUnitTest {

	private JarTreeDeleteNodeListener listener = new JarTreeDeleteNodeListener(jarTree, bar, null);

	@Test
	public void testDelete_MetaInfFolderFromJstlJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF");
		List<JarNode> assertion = getAssertion(node);

		List<JarNode> nodes = new ArrayList<JarNode>();
		nodes.add(node);
		listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(2, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_ManifestMfFolderFromJstlJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-web.war/WEB-INF/lib/jstl.jar/META-INF/MANIFEST.MF");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(1, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_MetaInfFolderFromRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "META-INF");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(3, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_ManifestMfMetaInfFolderFromRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "META-INF/MANIFEST.MF");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(1, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_BookstoreEjbJarFromRoot() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(2180, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_TldFileFromSpringJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar/META-INF/spring.tld");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(1, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_MetaInfFromSpringJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar/META-INF");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(6, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

	@Test
	public void testDelete_SpringJarFromBookstoreEjbJar() throws IOException {
		List<JarNode> contentBefore = loadBefore();
		JarNode node = getNode(jarTree, "bookstore-ejb.jar/spring.jar");
		List<JarNode> assertion = getAssertion(node);

        List<JarNode> nodes = new ArrayList<JarNode>();
        nodes.add(node);
        listener.delFromJar(nodes);
		List<JarNode> contentAfter = loadAfter();

		xor(contentBefore, contentAfter);

		Assert.assertTrue(contentAfter.isEmpty());
		Assert.assertEquals(2160, contentBefore.size());
		Assert.assertTrue(equals(contentBefore, assertion));
	}

    @Test
    public void testDelete_MultiplyDeleting() throws IOException {
        List<JarNode> contentBefore = loadBefore();

        List<JarNode> deletes = new ArrayList<JarNode>();
        JarNode metaInf = null, ejbJar = null;
        deletes.add(getNode(jarTree, "META-INF/MANIFEST.MF"));
        deletes.add(metaInf = getNode(jarTree, "META-INF"));
        deletes.add(ejbJar = getNode(jarTree, "bookstore-ejb.jar"));
        deletes.add(getNode(jarTree, "bookstore-ejb.jar/bookstore/ejb/LoggerBean.class"));
        deletes.add(getNode(jarTree, "bookstore-ejb.jar/bookstore"));
        List<JarNode> assertion = getAssertion(metaInf);
        assertion.addAll(getAssertion(ejbJar));

        listener.delFromJar(deletes);
        List<JarNode> contentAfter = loadAfter();

        xor(contentBefore, contentAfter);

        Assert.assertTrue(contentAfter.isEmpty());
        Assert.assertEquals(2183, contentBefore.size());
        Assert.assertTrue(equals(contentBefore, assertion));
    }

	private List<JarNode> loadBefore() throws IOException {
		jarTree = new JarTree(null, null, null);
		jarTree.addMouseListener(jarTree.new JarTreeMouseListener(listener, null, null, null));

		clearTmp();
		// copy file into temp
		test = File.createTempFile("test", "del.ear", tmp);
		FileUtils.copy(new File("test/resources/test.ear"), test);

		// load file
		JarTree.archiveLoader = new ArchiveLoader() {
			@Override
			protected void load(JarNode node) {

				File dst = new File(tmp, node.name);
				dst = Zip.unzip(node.getFullPath(), node.path, node.archive, dst);
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

	private List<JarNode> getAssertion(JarNode node) {
		List<JarNode> res = new ArrayList<JarNode>();
		bypass(node, res);
		return res;
	}
	
	@AfterClass
	public static void clearTmp() throws IOException {
		delete(tmp);
		tmp.mkdirs();
	}

}
