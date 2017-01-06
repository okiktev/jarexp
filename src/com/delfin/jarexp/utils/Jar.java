package com.delfin.jarexp.utils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Jar {

	private static final Logger log = Logger.getLogger(Jar.class.getCanonicalName());

	private final File file;

	public Jar(File file) {
		this.file = file;
	}

	public void bypass() throws IOException {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				process(entries.nextElement());
			}
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Couldn't close jar file " + jarFile.getName(), e);
				}
			}
		}
	}

	protected abstract void process(JarEntry entry) throws IOException;

}
