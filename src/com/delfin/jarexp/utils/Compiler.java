package com.delfin.jarexp.utils;

import java.io.File;
import java.io.IOException;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

public class Compiler {

	public static String decompile(File classFile) throws IOException {
		File dir = classFile.getParentFile();

		String[] args = new String[] { "-dgs=1", classFile.getAbsolutePath(), dir.getAbsolutePath() };
		ConsoleDecompiler.main(args);

		String name = classFile.getName().replace(".class", ".java");
		File dec = new File(dir, name);

		
		return FileUtils.toString(dec);
	}

}
