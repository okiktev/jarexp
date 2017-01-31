package com.delfin.jarexp.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import com.delfin.jarexp.JarexpException;

import jd.common.loader.BaseLoader;
import jd.common.loader.LoaderManager;
import jd.common.preferences.CommonPreferences;
import jd.common.printer.text.PlainTextPrinter;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

public class Compiler {

	private static LoaderManager loaderManager = new LoaderManager();

	public static String decompile(File file) {
		PrintStream ps = null;
		try {
			BaseLoader loader = loaderManager.getLoader(file.getParent());
			ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
    		ps = new PrintStream(baos);
    		CommonPreferences preferences = new CommonPreferences();
    		PlainTextPrinter printer = new PlainTextPrinter(preferences, ps);
			ClassFile classFile = ClassFileDeserializer.Deserialize(loader, file.getName());
			if (classFile == null) {
				throw new LoaderException("Can not deserialize \'" + file + "\'.");
			} else {
				ReferenceMap referenceMap = new ReferenceMap();
				ClassFileAnalyzer.Analyze(referenceMap, classFile);
				ReferenceAnalyzer.Analyze(referenceMap, classFile);
				ArrayList<LayoutBlock> layoutBlockList = new ArrayList<LayoutBlock>(1024);
				int maxLineNumber = ClassFileLayouter.Layout(preferences, referenceMap, classFile, layoutBlockList);
				ClassFileWriter.Write(loader, printer, referenceMap, maxLineNumber, classFile.getMajorVersion(),
				        classFile.getMinorVersion(), layoutBlockList);
				return new String(baos.toByteArray());
			}
		} catch (Exception e) {
			throw new JarexpException("An error while decompiling file " + file, e);
		} finally {
			ps.close();
		}
	}

}
