package com.delfin.jarexp.frame.search;

public class SearchResult {

	public int position;
	public String line;

	SearchResult(String line, int position) {
		this.position = position;
		this.line = line;
	}

	SearchResult(String line) {
		this(line, -1);
	}

}
