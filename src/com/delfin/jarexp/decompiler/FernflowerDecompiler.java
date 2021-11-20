package com.delfin.jarexp.decompiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import com.delfin.jarexp.exception.JarexpDecompilerException;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.utils.FileUtils;

public class FernflowerDecompiler implements IDecompiler {

	private static final Logger log = Logger.getLogger(FernflowerDecompiler.class.getCanonicalName());

	private static abstract class AbstractBytecodeProvider implements IBytecodeProvider {
		protected StructClass structClass;
		@Override
		public abstract byte[] getBytecode(String externalPath, String internalPath) throws IOException;
	}

	private static class AbstractResultSaver implements IResultSaver {
		protected String content;
		@Override
		public void saveFolder(String path) {}
		@Override
		public void copyFile(String source, String path, String entryName) {}
		@Override
		public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {}
		@Override
		public void createArchive(String path, String archiveName, Manifest manifest) {}
		@Override
		public void saveDirEntry(String path, String archiveName, String entryName) {}
		@Override
		public void copyEntry(String source, String path, String archiveName, String entry) {}
		@Override
		public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {}
		@Override
		public void closeArchive(String path, String archiveName) {}
	}

	private static IFernflowerLogger logger = new IFernflowerLogger() {
		@Override
		public void writeMessage(String message, Severity severity) {
			writeMessage(message, severity, null);
		}
		@Override
		public void writeMessage(String message, Severity severity, Throwable t) {
			Level level;
			switch (severity) {
			case INFO:
				level = Level.INFO;
				break;
			case WARN:
				level = Level.WARNING;
				break;
			case ERROR:
				level = Level.SEVERE;
				break;
			case TRACE:
				level = Level.FINEST;
				break;
			default:
				throw new JarexpException("Unknown logger severity: " + severity);
			}
			if (t == null) {
				log.log(level, message);
			} else {
				log.log(level, message, t);
			}
		}
	};

	private static Map<String, Object> DECOMPILE_OPTIONS = new HashMap<String, Object>(8);
	static {
		DECOMPILE_OPTIONS.put("ind", "    ");
		DECOMPILE_OPTIONS.put("rsy", "1");
		DECOMPILE_OPTIONS.put("dgs", "1");
		DECOMPILE_OPTIONS.put("din", "1");
		DECOMPILE_OPTIONS.put("den", "1");
		DECOMPILE_OPTIONS.put("asc", "1");
		DECOMPILE_OPTIONS.put("__dump_original_lines__", "1");
		DECOMPILE_OPTIONS.put("bsm", "1");
	}

	private static final Field FIELD_MAJOR_VER;
	static {
		try {			
			FIELD_MAJOR_VER = StructClass.class.getDeclaredField("majorVersion");
			FIELD_MAJOR_VER.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Unable to reflect majorVersion field", e);
		}
	}

	@Override
	public Result decompile(File archive, String path) {
		AbstractBytecodeProvider provider = new AbstractBytecodeProvider() {
			@Override
			public byte[] getBytecode(String externalPath, String internalPath) throws IOException {			
				ZipFile zip = null;
				InputStream stream = null;
				try {
			        zip = new ZipFile(externalPath);
			        stream = zip.getInputStream(zip.getEntry(internalPath));
			        byte[] bytes = FileUtils.toBytes(stream);
			        structClass = StructClass.create(new DataInputFullStream(bytes), false, null);
			        return bytes;
				} finally {
					close(stream);
					close(zip);
				}
			}
		};
		AbstractResultSaver saver = new AbstractResultSaver() {
			@Override
			public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
				this.content = content;
			}
		};
		Fernflower engine = new Fernflower(provider, saver, DECOMPILE_OPTIONS, logger);

		FernflowerStructContext structContext = new FernflowerStructContext(saver, engine, new LazyLoader(provider));
		ClassesProcessor classProcessor = new ClassesProcessor(structContext);
		replace(engine, "structContext", structContext);
		replace(engine, "classProcessor", classProcessor);
	    Map<String, Object> properties = new HashMap<String, Object>(IFernflowerPreferences.DEFAULTS);
	    properties.putAll(DECOMPILE_OPTIONS);
		DecompilerContext context = new DecompilerContext(properties, logger, structContext, classProcessor, DecompilerContext.getPoolInterceptor());
		DecompilerContext.setCurrentContext(context);

		structContext.addSource(archive, path);
		try {
			return decompile(engine, provider, saver);
		} catch (Exception e) {
			throw new JarexpDecompilerException("Unable to decompile class " + path + " from " + archive, e);
		}
	}

	@Override
	public Result decompile(File file) {
		AbstractBytecodeProvider provider = new AbstractBytecodeProvider() {
			@Override
			public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
				byte[] bytes = FileUtils.toBytes(new File(externalPath));
				structClass = StructClass.create(new DataInputFullStream(bytes), false, null);
				return bytes;
			}
		};
		AbstractResultSaver saver = new AbstractResultSaver() {
			@Override
			public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
				this.content = content;
			}
		};
		Fernflower engine = new Fernflower(provider, saver, DECOMPILE_OPTIONS, logger);
		engine.addSource(file);
		try {
			return decompile(engine, provider, saver);
		} catch (Exception e) {
			throw new JarexpDecompilerException("An error while decompiling file " + file, e);
		}
	}

	private static Result decompile(Fernflower engine, AbstractBytecodeProvider provider, AbstractResultSaver saver) {
		try {
			engine.decompileContext();
		} finally {
			engine.clearContext();
		}
		int majorVersion = -1;
		try {
			majorVersion = (Integer) FIELD_MAJOR_VER.get(provider.structClass);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to identify bytecode version", e);
		}
		return new Result(saver.content, Version.getCompiledJava(0, majorVersion));
	}

	private static void replace(Fernflower engine, String fieldName, Object fieldObject) {
		try {
			Field field = engine.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(engine, fieldObject);
		} catch (Exception e) {
			throw new JarexpException("Unable to replace private field " + fieldName, e);
		}
	}

	private static void close(InputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Unable to close stream.", e);
			}
		}
	}

	private static void close(ZipFile zip) {
		if (zip != null) {
			try {
				zip.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Unable to close zip " + zip.getName(), e);
			}
		}
	}

}
