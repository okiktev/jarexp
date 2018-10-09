package com.delfin.jarexp.frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class JarTreeDeleteNodeListenerUnitTest extends BaseUnitTest {

	private JarTreeDeleteNodeListener listener = new JarTreeDeleteNodeListener(jarTree, bar, null);

	@Override
	protected void initListener() {
		jarTree.addMouseListener(jarTree.new JarTreeMouseListener(listener, null, null, null));
	}

	@Test
	public void testDelete_MetaInfFolderFromJstlJar() throws IOException {
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

	private List<JarNode> getAssertion(JarNode node) {
		List<JarNode> res = new ArrayList<JarNode>();
		bypass(node, res);
		return res;
	}

}
