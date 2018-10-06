package com.delfin.jarexp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.delfin.jarexp.utils.Compiler;

public class Version {

	private static final Logger log = Logger.getLogger(Version.class.getCanonicalName());

	private static String ver = "@VERSION@";

	static String get() {
		return ver;
	}

	@Deprecated
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
			return Compiler.getVersion(minor, major);
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
