package com.delfin.jarexp.decompiler;

import com.delfin.jarexp.settings.Settings;

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
		default:
			return new JdCoreDecompiler();
		}
	}

	public static IDecompiler get() {
		return get(Settings.getDecompilerType());
	}

}
