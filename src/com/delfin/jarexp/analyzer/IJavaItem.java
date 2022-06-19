package com.delfin.jarexp.analyzer;

import java.util.List;

import javax.swing.Icon;

public interface IJavaItem {

	enum TYPE {
		CLASS, INTERFACE, METHOD, ENUM;
	};

	TYPE getType();

	Icon getIcon();

	String getName();

	List<IJavaItem> getChildren();

}
