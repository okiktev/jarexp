package com.delfin.jarexp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.JarexpException;

public class FileUtils {

	private static final Logger log = Logger.getLogger(FileUtils.class.getCanonicalName());

	public static String toString(File file) throws IOException {
		return read(new FileInputStream(file));
	}

	public static String toString(URL url) throws IOException {
		return read(url.openStream());
	}

	private static String read(InputStream stream) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
			StringBuilder out = new StringBuilder();
			for (String line; (line = reader.readLine()) != null;) {
				out.append(line).append('\n');
			}
			return out.toString();
		} catch (Exception e) {
			throw new JarexpException("An error occurred while reading file", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close reader of file", e);
				}
			}
		}
	}

}
