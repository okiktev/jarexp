package com.delfin.jarexp.decompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.gui.util.decompiler.StringBuilderPrinter;

import com.delfin.jarexp.exception.JarexpDecompilerException;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.utils.FileUtils;


public class JdCoreDecompiler113 implements IDecompiler {

	private static final Logger log = Logger.getLogger(JdCoreDecompiler113.class.getCanonicalName());

	@Override
	public Result decompile(File archive, String path) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(archive);
			return decompile(zip.getInputStream(zip.getEntry(path)),
					"Unable to decompile class " + path + " from " + archive);
		} catch (IOException e) {
			throw new JarexpException("Unable to open entry " + path + " in archive " + archive, e);
		} finally {
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
		InputStream stream = null;
		try {
			return decompile(stream = new FileInputStream(file), "An error while decompiling file " + file);
		} catch (FileNotFoundException e) {
			throw new JarexpException("Unable to open file " + file, e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream to file " + file, e);
				}
			}
		}
	}

	private static Result decompile(final InputStream stream, String errMsg) {
		try {
			Loader loader = new Loader() {
				@Override
				public boolean canLoad(String internalName) {
					return false;
				}
				@Override
				public byte[] load(String internalName) throws LoaderException {
					try {
						return FileUtils.toBytes(stream);
					} catch (IOException e) {
						throw new LoaderException(e);
					}
				}
			};
			StringBuilderPrinter printer = new StringBuilderPrinter();
			printer.setRealignmentLineNumber(true);
			new ClassFileToJavaSourceDecompiler().decompile(loader, printer, "",
					Collections.singletonMap("realignLineNumbers", (Object) "true"));
			return new Result(printer.getStringBuffer().toString(), getVersion(printer));
		} catch (Exception e) {
			throw new JarexpDecompilerException(errMsg, e);
		}
	}

	private static String getVersion(StringBuilderPrinter printer) {
		return Version.getCompiledJava(printer.getMinorVersion(), printer.getMajorVersion());
	}

}
