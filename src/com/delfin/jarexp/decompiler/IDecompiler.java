package com.delfin.jarexp.decompiler;

import java.io.File;

public interface IDecompiler {

	class Result {
		public String content;
		public String version;

		Result(String content, String version) {
			this.content = content;
			this.version = version;
		}
	}

	Result decompile(File archive, String path);

	Result decompile(File classFile);

}
