package com.delfin.jarexp.frame.duplicates;

import com.delfin.jarexp.frame.search.SearchResult;

public class DuplicatesSearchResult extends SearchResult implements Comparable<DuplicatesSearchResult> {

	public long size;

	DuplicatesSearchResult(String line, long size) {
		super(line);
		this.size = size;
	}

	@Override
	public int compareTo(DuplicatesSearchResult o) {
		if (o.size > size) {
			return 1;
		}
		if (o.size == size) {
			return 0;
		}
		return -1;
	}

}
