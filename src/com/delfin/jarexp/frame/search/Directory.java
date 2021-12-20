package com.delfin.jarexp.frame.search;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.Jar;

public abstract class Directory extends Jar {

	public Directory(File file) {
		super(file);
	}

	@Override
	protected void process(JarEntry entry) throws IOException {

	}

	@Override
	public void bypass() {
		try {
			bypass(file);
		} catch (Exception e) {
			throw new JarexpException("An error occurred while bypassing directory " + file, e);
		}
	}

	private void bypass(File file) throws IOException {
		if (file.isFile()) {
			process(file);
		} else {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					bypass(f);
				}
			}
		}
	}

	protected abstract void process(File file) throws IOException;
}