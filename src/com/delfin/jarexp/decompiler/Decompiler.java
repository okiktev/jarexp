package com.delfin.jarexp.decompiler;

import java.io.File;
import java.io.IOException;

import com.delfin.jarexp.Settings;
import com.delfin.jarexp.utils.FileUtils;

public class Decompiler {

	private static final String DECOMPILERS_STORE_URL = Settings.JAREXP_HOST_URL + "/downloads/decompilers/";

	private static final String PROCYON_FILE_NAME = "@PROCYON_FILE_NAME@";
	
	private static final String FERNFLOWER_FILE_NAME = "@FERNFLOWER_FILE_NAME@";

	public enum DecompilerType {
		JDCORE, PROCYON, FERNFLOWER
	}

	public static IDecompiler get() {
		switch (Settings.getDecompilerType()) {
		case PROCYON:
			return new ProcyonDecompiler();
		case FERNFLOWER:
			return new FernflowerDecompiler();
		default:
			return new JdCoreDecompiler();
		}
	}

	public static void prepareBinariesFor(DecompilerType type) throws IOException {
		String fileName;
		switch (type) {
		case PROCYON:
			fileName = PROCYON_FILE_NAME;
			break;
		case FERNFLOWER:
			fileName = FERNFLOWER_FILE_NAME;
			break;
		default:
			throw new IOException("Unable to identify type of decompiler " + type);
		}
		try {
			File lib = new File(Settings.getInstance().getExecutiveJar().getParentFile(), "lib");
			if (!lib.exists()) {
				lib.mkdirs();
			}
			File jar = new File(lib, fileName);
			if (!jar.exists()) {
				FileUtils.download(DECOMPILERS_STORE_URL + fileName, jar);
				FileUtils.addJarToClasspath(jar);
			}

		} catch (Exception e) {
			throw new IOException("Unable to preapare binaries for " + type + " decompiler", e);
		}
	}

}
