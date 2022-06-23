package com.delfin.jarexp.analyzer;

import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import com.delfin.jarexp.frame.resources.Resources;

public class JavaMethod implements IJavaItem {

	public enum ACCESS {DEF, PUBLIC, PRIVATE, PROTECTED}

	String name;

	ACCESS access;

	private List<String> params;

	private String returnType;

	private boolean isConstuctor;

	private int position;

	JavaMethod(String name, ACCESS access, List<String> params, String returnType, boolean isConstuctor, int position) {
		this.name = name;
		this.access = access;
		this.params = params;
		this.returnType = returnType;
		this.isConstuctor = isConstuctor;
		this.position = position;
	}

	@Override
	public Icon getIcon() {
		switch (access) {
		case PRIVATE: return Resources.getInstance().getJavaPrivateMethodIcon();
		case PROTECTED: return Resources.getInstance().getJavaProtectedMethodIcon();
		case PUBLIC: return Resources.getInstance().getJavaPublicMethodIcon();
		default:
			return Resources.getInstance().getJavaDefaultMethodIcon();
		}
	}

	@Override
	public String getName() {
		StringBuilder out = new StringBuilder(name);
		out.append('(');
		for(int i = 0; i < params.size(); ++i) {
			out.append(params.get(i));
			if (i != params.size() - 1) {
				out.append(',').append(' ');
			}
		}
		out.append(')');
		if (!isConstuctor) {
			out.append(' ').append(':').append(' ').append(returnType);
		}
		return out.toString();
	}

	@Override
	public List<IJavaItem> getChildren() {
		return Collections.emptyList();
	}

	public List<String> getParams() {
		return params;
	}

	@Override
	public TYPE getType() {
		return TYPE.CLASS;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(access).append(' ');
		out.append(isConstuctor ? "constructor" : returnType).append(' ');
		out.append(name).append('(');
		out.append(params).append(')');
		out.append(':').append(position);
		return out.toString();
	}

}
