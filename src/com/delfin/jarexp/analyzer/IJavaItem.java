package com.delfin.jarexp.analyzer;

import java.util.List;

import javax.swing.Icon;

public interface IJavaItem {

	static class Position {
		public int position;
		public int length;
		Position(int position, int length) {
			this.position = position;
			this.length = length;
		}
	}

	enum TYPE {
		CLASS, INTERFACE, RECORD, METHOD, ENUM;
	};

	TYPE getType();

	Icon getIcon();

	String getName();

	List<IJavaItem> getChildren();

	Position getPosition();

}
