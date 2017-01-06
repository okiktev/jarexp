package com.delfin.jarexp.frame;

import java.io.File;

class FileInfo {

	String name;

	String path;
	
	File parentArc;
	
	boolean isDir;

	FileInfo(String name, String path, File parentArc, boolean isDir) {
		this.name = name;
		this.path = path;
		this.parentArc = parentArc;
		this.isDir = isDir;
	}

	@Override
	public String toString() {
		return name;
	}
}
