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

import com.delfin.jarexp.exception.JarexpException;

public class Sha1Checksum {

	private static final Logger log = Logger.getLogger(Sha1Checksum.class.getCanonicalName());

	public static String get(File file) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return generate(getBytes(fis));
		} catch (Exception e) {
			throw new JarexpException("Couldn't get SHA-1 checksum", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream to file " + file, e);
				}
			}
		}
	}

	public static String get(File archive, String path) throws ZipException, IOException {
		ZipFile zip = null;
		try {
	        zip = new ZipFile(archive);
	        InputStream stream = zip.getInputStream(zip.getEntry(path));
	        return Sha1Checksum.get(stream);
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
			throw new JarexpException("Couldn't get SHA-1 checksum", e);
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
		MessageDigest complete = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
        	complete.update(buffer, 0, len);
        }
		return complete.digest();
	}

}