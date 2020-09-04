package com.delfin.jarexp.decompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpDecompilerException;
import com.delfin.jarexp.exception.JarexpException;
import com.strobel.assembler.ir.ConstantPool;
import com.strobel.assembler.ir.ConstantPool.TypeInfoEntry;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.DeobfuscationUtilities;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class ProcyonDecompiler implements IDecompiler {

	private static final Logger log = Logger.getLogger(ProcyonDecompiler.class.getCanonicalName());

	@Override
	public Result decompile(File archive, String path) {
		String internalName = path.substring(0, path.lastIndexOf('.'));
		try {
			return decompile(internalName, new JarTypeLoader(new JarFile(archive)));
		} catch (Exception e) {
			throw new JarexpDecompilerException("An error occurred while decompiling " + path + " from " + archive, e);
		}
	}

	@Override
	public Result decompile(final File file) {
		try {
			return decompile(lookupClassInFile(file), new ITypeLoader() {
				private String className;

				@Override
				public boolean tryLoadType(String internalName, Buffer buffer) {
					if (className != null && !className.equals(internalName)) {
						return false;
					}
					if (!readFile(file, buffer)) {
						return false;
					}
					if (className == null) {
						className = internalName;
					}
					return true;
				}
			});
		} catch (Exception e) {
			throw new JarexpDecompilerException("An error occurred while decompiling " + file, e);
		}
	}

	private static Result decompile(String internalName, ITypeLoader typeLoader) {
		DecompilerSettings settings = DecompilerSettings.javaDefaults();
		settings.setShowDebugLineNumbers(true);
		settings.setForceExplicitImports(true);
		StringWriter writer = new StringWriter();
		PlainTextOutput output = new PlainTextOutput(writer);
		MetadataSystem metadataSystem = new MetadataSystem(typeLoader);

		TypeReference type;
		if (internalName.length() == 1) {
			MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
			TypeReference reference = parser.parseTypeDescriptor(internalName);
			type = (TypeReference) metadataSystem.resolve(reference);
		} else {
			type = metadataSystem.lookupType(internalName);
		}
		TypeDefinition resolvedType;
		if (type == null || (resolvedType = type.resolve()) == null) {
			throw new JarexpException("Failed to load class " + internalName);
		}
		DeobfuscationUtilities.processType(resolvedType);
		DecompilationOptions options = new DecompilationOptions();
		options.setSettings(settings);
		options.setFullDecompilation(true);
		settings.getLanguage().decompileType(resolvedType, output, options);
		return new Result(writer.toString(), Version.getCompiledJava(resolvedType.getCompilerMinorVersion(),
				resolvedType.getCompilerMajorVersion()));
	}

	private static String lookupClassInFile(final File file) {
		final List<String> classNames = new ArrayList<String>();
		new ITypeLoader() {
			@Override
			public boolean tryLoadType(String paramString, Buffer buffer) {
				if (!classNames.isEmpty()) {
					return false;
				}
				if (!readFile(file, buffer)) {
					return false;
				}
				classNames.add(getInternalNameFromClassFile(buffer));
				return true;
			}

			private String getInternalNameFromClassFile(final Buffer buffer) {
				long magic = buffer.readInt() & 0xFFFFFFFFL;
				if (magic != 0xCAFEBABEL) {
					return null;
				}
				buffer.readUnsignedShort();
				buffer.readUnsignedShort();
				ConstantPool constantPool = ConstantPool.read(buffer);
				buffer.readUnsignedShort();
				TypeInfoEntry thisClass = (TypeInfoEntry) constantPool.getEntry(buffer.readUnsignedShort());
				buffer.position(0);
				return thisClass.getName();
			}

		}.tryLoadType(null, new Buffer(0));

		return classNames.get(0);
	}

	private static boolean readFile(File file, Buffer buffer) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			int remainingBytes = stream.available();
			buffer.reset(remainingBytes);
			while (remainingBytes > 0) {
				int bytesRead = stream.read(buffer.array(), buffer.position(), remainingBytes);
				if (bytesRead < 0) {
					break;
				}
				buffer.position(buffer.position() + bytesRead);
				remainingBytes -= bytesRead;
			}
			buffer.position(0);
			return true;
		} catch (IOException e) {
			log.log(Level.SEVERE, "An error occurred while reading file " + file, e);
			return false;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while cloasing stream to file.", e);
				}
			}
		}
	}

}
