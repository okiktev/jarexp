package com.delfin.jarexp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.delfin.jarexp.JarexpException;

public class Md5Checksum {

	private static final Logger log = Logger.getLogger(Md5Checksum.class.getCanonicalName());

	static String get(File file) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return generate(getBytes(fis));
		} catch (Exception e) {
			throw new JarexpException("Couldn't get MD5 checksum", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream for file " + file, e);
				}
			}
		}
	}

	public static String get(File archive, String path) throws ZipException, IOException {
		ZipFile zip = null;
		try {
	        zip = new ZipFile(archive);
	        InputStream stream = zip.getInputStream(zip.getEntry(path));
	        return Md5Checksum.get(stream);
		} finally {
			if (zip != null) {
				zip.close();
			}
		}
	}

	private static String get(InputStream stream) {
		try {
			return generate(getBytes(stream));
		} catch (Exception e) {
			throw new JarexpException("Couldn't get MD5 checksum", e);
		}
	}

	private static String generate(byte[] bytes) {
		StringBuilder out = new StringBuilder(bytes.length * 2);
		for (byte bt : bytes) {
			out.append(Integer.toString((bt & 0xff) + 0x100, 16).substring(1));
		}
		return out.toString();
	}

	private static byte[] getBytes(InputStream stream) throws NoSuchAlgorithmException, IOException {
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = stream.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		return complete.digest();
	}

}