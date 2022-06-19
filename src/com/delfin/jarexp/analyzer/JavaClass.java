package com.delfin.jarexp.analyzer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.delfin.jarexp.frame.resources.Resources;

public class JavaClass implements IJavaItem {

	private String name;

	private List<IJavaItem> children = new ArrayList<IJavaItem>();

	JavaClass(String name) {
		this.name = name;
	}

	@Override
	public Icon getIcon() {
		return Resources.getInstance().getJavaClassIcon();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<IJavaItem> getChildren() {
		return children;
	}

	public void setChildren(List<IJavaItem> children) {
		this.children = children;
	}

	@Override
	public TYPE getType() {
		return TYPE.CLASS;
	}

}
