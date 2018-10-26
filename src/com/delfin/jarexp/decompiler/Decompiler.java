package com.delfin.jarexp.decompiler;

import com.delfin.jarexp.Settings;

public class Decompiler {

	public enum DecompilerType {
		JDCORE, PROCYON
	}

	public static IDecompiler get() {
		switch (Settings.getDecompilerType()) {
		case PROCYON:
			return new ProcyonDecompiler();
		default:
			return new JdCoreDecompiler();
		}
	}

}
