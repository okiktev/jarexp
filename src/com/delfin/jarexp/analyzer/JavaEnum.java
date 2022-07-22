package com.delfin.jarexp.analyzer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.delfin.jarexp.frame.resources.Resources;

public class JavaEnum implements IJavaItem {

	private final String name;
	private final Position position; 

	private List<IJavaItem> children = new ArrayList<IJavaItem>(5);

	public JavaEnum(String name, Position position) {
		this.name = name;
		this.position = position;
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

	@Override
	public Position getPosition() {
		return position;
	}

}
