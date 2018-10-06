package com.delfin.jarexp.utils;

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

import com.delfin.jarexp.JarexpException;

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

public class Compiler {

	private static final Logger log = Logger.getLogger(Zip.class.getCanonicalName());

	public static class Decompiled {
		public String content;
		public String version;

		Decompiled(String content, String version) {
			this.content = content;
			this.version = version;
		}
	}

	public static String decompile(File file) {
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
				return new String(baos.toByteArray());
			}
		} catch (Exception e) {
			throw new JarexpException("An error while decompiling file " + file, e);
		} finally {
			ps.close();
		}
	}

	@Deprecated
	private static void decompile(final InputStream stream, StringBuilder content, String fileName) {
		content.replace(0, content.length() - 1, "");

		PrintStream ps = null;
		try {
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
			ClassFile classFile = ClassFileDeserializer.Deserialize(loader, fileName);
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
				content.append(baos.toByteArray());
			}
		} catch (Exception e) {
			throw new JarexpException("An error while decompiling file " + fileName, e);
		} finally {
			ps.close();
		}
	}

	public static Decompiled decompile(File archive, String path) {
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
			String fileName = path;
			int i = path.lastIndexOf('/');
			if (i != -1) {
				fileName = path.substring(i + 1);
			}
			ClassFile classFile = ClassFileDeserializer.Deserialize(loader, fileName);
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
				return new Decompiled(new String(baos.toByteArray()), getVersion(classFile));
			}
		} catch (Exception e) {
			throw new JarexpException("Unable to decompile class " + path + " from " + archive);
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

	private static String getVersion(ClassFile classFile) {
		return getVersion(classFile.getMinorVersion(), classFile.getMajorVersion());
	}

	public static String getVersion(int minor, int major) {
		// 1.1 45.3
		// 1.2 46.0
		// 1.3 47.0
		// 1.4 48.0
		// 5 (1.5) 49.0
		// 6 (1.6) 50.0
		// 7 (1.7) 51.0
		// 8 (1.8) 52.0
		// 9 (1.9) 53.0
		// 10 (1.10) 54.0

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
		default:
			log.warning("Unknown version of compiled file. Major " + major + ". Minor " + minor);
			return "~";
		}
	}

}
