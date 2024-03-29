package com.delfin.jarexp.frame.search;

import java.awt.Color;

public class SearchResult {

	public static final Color COLOR_ERROR = new Color(255, 149, 149);

	public static final Color COLOR_CONTENT = new Color(239, 228, 176);

	public int position;
	public String line;

	public SearchResult(String line, int position) {
		this.position = position;
		this.line = line;
	}

	public SearchResult(String line) {
		this(line, -1);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		if (position != -1) {
			out.append('[').append(position).append("]: ");
		}
		out.append(line);
		return out.toString();
	}

}
