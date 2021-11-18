package com.delfin.jarexp.decompiler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import com.delfin.jarexp.exception.JarexpException;

class Version {

	private static final Logger log = Logger.getLogger(Version.class.getCanonicalName());

	static String getCompiledJava(File archive, String path) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(archive);
			return getCompiledJava(zip.getInputStream(zip.getEntry(path)));
		} catch (Exception e) {
			String msg = "An error occurred while trying to identify java version which was compiled file " + path;
			log.log(Level.SEVERE, msg, e);
			throw new JarexpException(msg, e);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close zip file " + archive, e);
				}
			}
		}
	}

	static String getCompiledJava(InputStream stream) {
		DataInputStream data = null;
		try {
			data = new DataInputStream(stream);
			if (0xCAFEBABE != data.readInt()) {
				throw new IOException("Invalid header of file class file.");
			}
			int minor = data.readUnsignedShort();
			int major = data.readUnsignedShort();
			return getCompiledJava(minor, major);
		} catch (Exception e) {
			String msg = "An error occurred while trying to identify java version which was compiled file.";
			log.log(Level.SEVERE, msg, e);
			throw new JarexpException(msg, e);
		} finally {
			if (data != null) {
				try {
					data.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close data stream.", e);
				}
			}
		}
	}

	static String getCompiledJava(File file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return getCompiledJava(in);
		} catch (Exception e) {
			String msg = "An error occurred while trying to identify java version which was compiled file " + file;
			log.log(Level.SEVERE, msg, e);
			throw new JarexpException(msg, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close stream for " + file, e);
				}
			}
		}
	}

	static String getCompiledJava(int minor, int major) {
		// 1.1 45.3
		// 1.2 46.0
		// 1.3 47.0
		// 1.4 48.0
		// 5 (1.5) 49.0
		// 6 (1.6) 50.0
		// 7 (1.7) 51.0
		// 8 (1.8) 52.0
		// 9 (9) 53.0
		// 10 (10) 54.0
		// 11 (11) 55.0
		// 12 (12) 56.0
		// 13 (13) 57.0

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
		case 53:
			return "9";
		case 54:
			return "10";
		case 55:
			return "11";
		case 56:
			return "12";
		case 57:
			return "13";
		case 58:
			return "14";
		case 59:
			return "15";
		case 60:
			return "16";
		case 61:
			return "17";
		case 62:
			return "18";
		default:
			log.warning("Unknown version of compiled file. Major " + major + ". Minor " + minor);
			return "~";
		}
	}

}
