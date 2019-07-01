package com.delfin.jarexp.frame.search;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import com.delfin.jarexp.frame.search.SearchDlg.SearchEntries;

public class SearchEntriesUnitTest {

	private SearchEntries entries = new SearchEntries();

	@Test
	public void testAddedFewEntries() {
		String path1 = "/book.jar!/APP_INF";
		String path2 = "/DIR";

		entries.add(null, null, path1, false);
		entries.add(null, null, path2, false);

		assertEquals(2, entries.size());
		Iterator<SearchEntries> it = entries.iterator();
		assertEquals(path1, it.next().fullPath);
		assertEquals(path2, it.next().fullPath);
	}

	@Test
	public void testReplacedByBeingAdded() {
		String path1 = "/DIR/book.jar!/APP_INF";
		String path2 = "/DIR";

		entries.add(null, null, path1, false);
		entries.add(null, null, path2, false);

		assertEquals(1, entries.size());
		assertEquals(path2, entries.iterator().next().fullPath);
	}

	@Test
	public void testIgnoredBeingAddedBecauseMoreCommonExist() {
		String path1 = "/DIR";
		String path2 = "/DIR/book.jar!/APP_INF";

		entries.add(null, null, path1, false);
		entries.add(null, null, path2, false);

		assertEquals(1, entries.size());
		assertEquals(path1, entries.iterator().next().fullPath);
	}

	@Test
	public void testAddingTwoAntriesWithSameFullPaths() {
		String path1 = "/DIR";
		String path2 = "/DIR";

		entries.add(null, null, path1, false);
		entries.add(null, null, path2, false);

		assertEquals(1, entries.size());
		assertEquals(path2, entries.iterator().next().fullPath);
	}

}
