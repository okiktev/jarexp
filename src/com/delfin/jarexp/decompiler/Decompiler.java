package com.delfin.jarexp.decompiler;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;

public class Decompiler {

	public enum DecompilerType {
		JDCORE, PROCYON, FERNFLOWER
	}

	public static IDecompiler get(DecompilerType type) {
		switch (type) {
		case PROCYON:
			return new ProcyonDecompiler();
		case FERNFLOWER:
			return new FernflowerDecompiler();
		case JDCORE:
			return Version.JAVA_MAJOR_VER >= 8 
				? new JdCoreDecompiler113() 
				: new JdCoreDecompiler071();
		default:
			throw new JarexpException("Unknown decompiler type " + type);
		}
	}

	public static IDecompiler get() {
		return get(Settings.getDecompilerType());
	}

	public static boolean isNotLoaded(DecompilerType type) {
		try {
			switch (type) {
			case PROCYON:
				new ProcyonDecompiler();
				return false;
			case FERNFLOWER:
				new FernflowerDecompiler();
				return false;
			case JDCORE:
				if (Version.JAVA_MAJOR_VER >= 8) {
					new JdCoreDecompiler113();
				} else {
					new JdCoreDecompiler071();
				}
				return false;
			default:
				return true;
			}
		} catch (Throwable t) {
			return true;
		}
	}

}
