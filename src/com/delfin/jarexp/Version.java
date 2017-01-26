package com.delfin.jarexp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Version {

	private static final Logger log = Logger.getLogger(Version.class.getCanonicalName());

	private static String ver = "@VERSION@";

	static String get() {
		return ver;
	}

	public static String getCompiledJava(File file) {
		InputStream in = null;
		DataInputStream data = null;
		try {
			in = new FileInputStream(file);
			data = new DataInputStream(in);
			if (0xCAFEBABE != data.readInt()) {
				throw new IOException("Invalid header of filr " + file);
			}
			int minor = data.readUnsignedShort();
			int major = data.readUnsignedShort();
			// 1.1 45.3
			// 1.2 46.0
			// 1.3 47.0
			// 1.4 48.0
			// 5 (1.5) 49.0
			// 6 (1.6) 50.0
			// 7 (1.7) 51.0
			// 8 (1.8) 52.0

			switch (major) {
			case 45:
				return "1.1";
			case 46:
				return "1.2";
			case 47:
				return "1.3";
			case 48:
				return "1.4";
			case 49:
				return "5";
			case 50:
				return "6";
			case 51:
				return "7";
			case 52:
				return "8";
			default:
				log.warning("Unknown version of compiled file " + file + ". Major " + major + ". Minor " + minor);
				return "";

			}
		} catch (Exception e) {
			String msg = "An error occurred while trying to identify java version which was compiled file " + file;
			log.log(Level.SEVERE, msg, e);
			throw new JarexpException(msg, e);
		} finally {
			if (data != null) {
				try {
					data.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close data stream for " + file, e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream for " + file, e);
				}
			}
		}
	}

}
