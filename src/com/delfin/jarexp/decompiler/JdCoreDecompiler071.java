package com.delfin.jarexp.decompiler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.delfin.jarexp.decompiler.IDecompiler.Result;
import com.delfin.jarexp.exception.JarexpDecompilerException;
import com.delfin.jarexp.utils.FileUtils;

import jd.common.loader.BaseLoader;
import jd.common.loader.LoaderManager;
import jd.common.preferences.CommonPreferences;
import jd.common.printer.text.PlainTextPrinter;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

public class JdCoreDecompiler071 implements IDecompiler {

	private static final Logger log = Logger.getLogger(JdCoreDecompiler071.class.getCanonicalName());

	@Override
	public Result decompile(File archive, String path) {
		PrintStream ps = null;
		ZipFile zip = null;
		try {
			zip = new ZipFile(archive);
			ZipEntry entry = zip.getEntry(path);
			final InputStream stream = zip.getInputStream(entry);
			Loader loader = new Loader() {
				@Override
				public DataInputStream load(String internalPath) throws LoaderException {
					return new DataInputStream(stream);
				}

				@Override
				public boolean canLoad(String internalPath) {
					return true;
				}
			};
			ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
			ps = new PrintStream(baos);
			CommonPreferences preferences = new CommonPreferences();
			PlainTextPrinter printer = new PlainTextPrinter(preferences, ps);
			ClassFile classFile = ClassFileDeserializer.Deserialize(loader, FileUtils.getFileName(path));
			if (classFile == null) {
				throw new LoaderException("Can not deserialize from stream.");
			} else {
				ReferenceMap referenceMap = new ReferenceMap();
				ClassFileAnalyzer.Analyze(referenceMap, classFile);
				ReferenceAnalyzer.Analyze(referenceMap, classFile);
				ArrayList<LayoutBlock> layoutBlockList = new ArrayList<LayoutBlock>(1024);
				int maxLineNumber = ClassFileLayouter.Layout(preferences, referenceMap, classFile, layoutBlockList);
				ClassFileWriter.Write(loader, printer, referenceMap, maxLineNumber, classFile.getMajorVersion(),
						classFile.getMinorVersion(), layoutBlockList);
				return new Result(new String(baos.toByteArray()), getVersion(classFile));
			}
		} catch (Exception e) {
			throw new JarexpDecompilerException("Unable to decompile class " + path + " from " + archive, e);
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream to archive " + archive, e);
				}
			}
		}
	}

	@Override
	public Result decompile(File file) {
		PrintStream ps = null;
		try {
			BaseLoader loader = new LoaderManager().getLoader(file.getParent());
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
				return new Result(new String(baos.toByteArray()), getVersion(classFile));
			}
		} catch (Exception e) {
			throw new JarexpDecompilerException("An error while decompiling file " + file, e);
		} finally {
			ps.close();
		}
	}

	private static String getVersion(ClassFile classFile) {
		return Version.getCompiledJava(classFile.getMinorVersion(), classFile.getMajorVersion());
	}

}
