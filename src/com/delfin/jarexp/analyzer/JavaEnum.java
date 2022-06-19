package com.delfin.jarexp.analyzer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.delfin.jarexp.frame.resources.Resources;

public class JavaEnum implements IJavaItem {

	private String name;

	private List<IJavaItem> children = new ArrayList<IJavaItem>();

	public JavaEnum(String name) {
		this.name = name;
	}

	@Override
	public TYPE getType() {
		return TYPE.ENUM;
	}

	@Override
	public Icon getIcon() {
		return Resources.getInstance().getJavaEnumIcon();
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

}
