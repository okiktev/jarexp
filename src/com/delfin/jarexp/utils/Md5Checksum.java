package com.delfin.jarexp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.JarexpException;

public class Md5Checksum {

	private static final Logger log = Logger.getLogger(Md5Checksum.class.getCanonicalName());

	public static String get(File file) {
		byte[] bytes = getBytes(file);
		StringBuilder out = new StringBuilder(bytes.length * 2);
		for (byte bt : bytes) {
			out.append(Integer.toString((bt & 0xff) + 0x100, 16).substring(1));
		}
		return out.toString();
	}

	private static byte[] getBytes(File file) {
		byte[] buffer = new byte[1024];
		InputStream fis = null;
		try {
    		fis = new FileInputStream(file);
    		MessageDigest complete = MessageDigest.getInstance("MD5");
    		int numRead;
    		do {
    			numRead = fis.read(buffer);
    			if (numRead > 0) {
    				complete.update(buffer, 0, numRead);
    			}
    		} while (numRead != -1);
    		return complete.digest();
		} catch (Exception e) {
			throw new JarexpException("Couldn't get bytes for generating MD5 checksum", e);
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
}