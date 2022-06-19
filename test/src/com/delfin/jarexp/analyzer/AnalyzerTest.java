package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class AnalyzerTest {

	@Test
	public void testClasses() {
		String code = "/*   */ import java.io.PrintStream;\r\n" + 
				"/*   */ \r\n" + 
				"/* 5 */ public class Main { public static void main(String[] args) { System.out.println(\"test java 11\"); }\r\n" + 
				"/*   */ }\r\n" + 
				"/* 5 */ private class Main1 { public\r\n" +
				"/*     */ \r\n" +
				"/* 5 */ class Main2 { public \r\n" +
				"/* 5 */ invokeclass Main2 { public \r\n" +
				"/*     */ \r\n" + 
				"/*     */ public class FileH$andle\r\n" + 
				"/*     */   extends Handle\r\n" + 
				"/*     */ {\r\n" +
				"/*    */   {\r\n" +
				"/* 36 */     this.content = content;\r\n" +
				"/*    */     \r\n" +
				"/* 38 */     this.compJava.setToolTipText(\"Version of Java which was class compiled with\");\r\n" +
				"/* 39 */     this.children.setToolTipText(\"Number of all objects placed in current folder/archive\");\r\n" +
				"/*    */     \r\n" +
				"/* 41 */     this.progressBar.setStringPainted(true);\r\n";
			
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(4, res.size());
		assertEquals("Main", res.get(0).getName());
		assertEquals("Main1", res.get(1).getName());
		assertEquals("Main2", res.get(2).getName());
		assertEquals("FileH$andle", res.get(3).getName());
	}

	@Test
	public void testEnums() {
		String code = "/*     */ \r\n" + 
				"/*     */ public enum SftpClient$OpenMode\r\n" + 
				"/*     */ {\r\n" + 
				"/*  73 */   Read, \r\n" + 
				"/*  74 */   Write, \r\n" + 
				"/*  75 */   Append, \r\n" + 
				"/*  76 */   Create, \r\n" + 
				"/*  77 */   Truncate, \r\n" + 
				"/*  78 */   Exclusive;\r\n" + 
				"/*     */ \r\n" + 
				"/*  84 */   public static final Set<OpenOption> SUPPORTED_OPTIONS = Collections.unmodifiableSet(\r\n" + 
				"/*  85 */     EnumSet.of(StandardOpenOption.READ, new StandardOpenOption[] { StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE }));\r\n" + 
				"/*     */ \r\n" + 
				"/*     */   private SftpClient$OpenMode() {}\r\n" + 
				"/*     */ \r\n" + 
				"/*     */   public static Set<OpenMode> fromOpenOptions(Collection<? extends OpenOption> options)\r\n" + 
				"/*     */   {\r\n" + 
				"/* 100 */     if (GenericUtils.isEmpty(options)) {\r\n" + 
				"/* 101 */       return Collections.emptySet();\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/* 104 */     Set<OpenMode> modes = EnumSet.noneOf(OpenMode.class);\r\n" + 
				"/* 105 */     for (OpenOption option : options) {\r\n" + 
				"/* 106 */       if (option == StandardOpenOption.READ) {\r\n" + 
				"/* 107 */         modes.add(Read);\r\n" + 
				"/* 108 */       } else if (option == StandardOpenOption.APPEND) {\r\n" + 
				"/* 109 */         modes.add(Append);\r\n" + 
				"/* 110 */       } else if (option == StandardOpenOption.CREATE) {\r\n" + 
				"/* 111 */         modes.add(Create);\r\n" + 
				"/* 112 */       } else if (option == StandardOpenOption.TRUNCATE_EXISTING) {\r\n" + 
				"/* 113 */         modes.add(Truncate);\r\n" + 
				"/* 114 */       } else if (option == StandardOpenOption.WRITE) {\r\n" + 
				"/* 115 */         modes.add(Write);\r\n" + 
				"/* 116 */       } else if (option == StandardOpenOption.CREATE_NEW) {\r\n" + 
				"/* 117 */         modes.add(Create);\r\n" + 
				"/* 118 */         modes.add(Exclusive);\r\n" + 
				"/* 119 */       } else if (option != StandardOpenOption.SPARSE)\r\n" + 
				"/*     */       {\r\n" + 
				"/*     */ \r\n" + 
				"/* 128 */         throw new IllegalArgumentException(\"Unsupported open option: \" + option);\r\n" + 
				"/*     */       }\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/* 132 */     return modes;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */ }";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		assertEquals("SftpClient$OpenMode", res.get(0).getName());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("SftpClient$OpenMode()", methods.get(0).getName());
		assertEquals("fromOpenOptions(Collection<? extends OpenOption>) : Set<OpenMode>", methods.get(1).getName());
	}

	@Test
	public void testInterfaces() {
		String code = "/*   */ import java.io.PrintStream;\r\n" + 
				"/*   */ \r\n" + 
			    ".interfaces != null && this.interfaces.length > 0) {\r\n" + 
				"/* 5 */ public interface IMain { public static void main(String[] args) { System.out.println(\"test java 11\"); }\r\n" + 
				"/*   */ }\r\n" + 
				"/* 5 */ private interface IMain1 { public\r\n" +
				"/*     */ \r\n" +
				"/*    */ \r\n" +
				"/*    */ \r\n" +
				"/*    */ public abstract interface ExtensionParser<T>\r\n" +
				"/*    */   extends NamedResource, Function<byte[], T>\r\n" +
				"/*    */ {\r\n" +
				"/*    */   public T parse(byte[] input)\r\n" +
				"/*    */   {\r\n" +
				"/* 5 */ interface IMain2 { public \r\n" +
				"/* 5 */ invokeinterface IMain2 { public \r\n" +
				"/*     */ \r\n" + 
				"/*     */ public interface IFileH$andle\r\n" + 
				"/*     */   implements Handle\r\n" + 
				"/*     */ {";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(5, res.size());
		assertEquals("IMain", res.get(0).getName());
		assertEquals("IMain1", res.get(1).getName());
		assertEquals("ExtensionParser", res.get(2).getName());
		assertEquals("IMain2", res.get(3).getName());
		assertEquals("IFileH$andle", res.get(4).getName());
	}

	@Test
	public void testMethods() {
		String code = "/*     */ \r\n" + 
				"/*     */ \r\n" + 
				"/*     */ public class FileH$andle\r\n" + 
				"/*     */   extends Handle\r\n" + 
				"/*     */ {\r\n" + 
				"/*     */   private final int access;\r\n" + 
				"/*     */   private final SeekableByteChannel fileChannel;\r\n" + 
				"/*  51 */   private final List<FileLock> locks = new ArrayList();\r\n" + 
				"/*     */   private final SftpSubsystem subsystem;\r\n" + 
				"/*     */   private final Set<StandardOpenOption> openOptions;\r\n" + 
				"/*     */   private final Collection<FileAttribute<?>> fileAttributes;\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public FileH$andle(SftpSubsystem subsystem, Path file, String handle, int flags, int access, Map<String, Object> attrs) throws IOException {\r\n" + 
				"/*  57 */     super(file, handle);\r\n" + 
				"/*     */     \r\n" + 
				"/*  59 */     this.subsystem = ((SftpSubsystem)Objects.requireNonNull(subsystem, \"No subsystem instance provided\"));\r\n" + 
				"/*  60 */     this.access = access;\r\n" + 
				"/*  61 */     this.openOptions = Collections.unmodifiableSet(getOpenOptions(flags, access));\r\n" + 
				"/*  62 */     this.fileAttributes = Collections.unmodifiableCollection(toFileAttributes(attrs));\r\n" + 
				"/*  63 */     signalHandleOpening(subsystem);\r\n" + 
				"/*     */     \r\n" + 
				"/*     */ \r\n" + 
				"/*     */ \r\n" + 
				"/*  67 */     FileAttribute<?>[] fileAttrs = GenericUtils.isEmpty(this.fileAttributes) ? IoUtils.EMPTY_FILE_ATTRIBUTES : (FileAttribute[])this.fileAttributes.toArray(new FileAttribute[this.fileAttributes.size()]);\r\n" + 
				"/*     */     \r\n" + 
				"/*  69 */     SftpFileSystemAccessor accessor = subsystem.getFileSystemAccessor();\r\n" + 
				"/*  70 */     ServerSession session = subsystem.getServerSession();\r\n" + 
				"/*     */     SeekableByteChannel channel;\r\n" + 
				"/*     */     try {\r\n" + 
				"/*  73 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, fileAttrs);\r\n" + 
				"/*     */     } catch (UnsupportedOperationException e) { SeekableByteChannel channel;\r\n" + 
				"/*  75 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, IoUtils.EMPTY_FILE_ATTRIBUTES);\r\n" + 
				"/*  76 */       subsystem.doSetAttributes(file, attrs);\r\n" + 
				"/*     */     }\r\n" + 
				"/*  78 */     this.fileChannel = channel;\r\n" + 
				"/*     */     try\r\n" + 
				"/*     */     {\r\n" + 
				"/*  81 */       signalHandleOpen(subsystem);\r\n" + 
				"/*     */     } catch (IOException e) {\r\n" + 
				"/*  83 */       close();\r\n" + 
				"/*  84 */       throw e;\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   final Set<StandardOpenOption> getOpenOptions() {\r\n" + 
				"/*  89 */     return this.openOptions;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public final Collection<FileAttribute<?>> getFileAttributes() {\r\n" + 
				"/*  93 */     return this.fileAttributes;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public SeekableByteChannel getFileChannel() {\r\n" + 
				"/*  97 */     return this.fileChannel;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public int getAccessMask() {\r\n" + 
				"/* 101 */     return this.access;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public boolean isOpenAppend() {\r\n" + 
				"/* 105 */     return 4 == (getAccessMask() & 0x4);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public int read(byte[] data, long offset) throws IOException {\r\n" + 
				"/* 109 */     return read(data, 0, data.length, offset);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public int read(byte[] data, int doff, int length, long offset) throws IOException {\r\n" + 
				"/* 113 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 114 */     channel = channel.position(offset);\r\n" + 
				"/* 115 */     return channel.read(ByteBuffer.wrap(data, doff, length));\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void append(byte[] data) throws IOException {\r\n" + 
				"/* 119 */     append(data, 0, data.length);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void append(byte[] data, int doff, int length) throws IOException {\r\n" + 
				"/* 123 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 124 */     write(data, doff, length, channel.size());\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void write(byte[] data, long offset) throws IOException {\r\n" + 
				"/* 128 */     write(data, 0, data.length, offset);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void write(byte[] data, int doff, int length, long offset) throws IOException {\r\n" + 
				"/* 132 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 133 */     channel = channel.position(offset);\r\n" + 
				"/* 134 */     channel.write(ByteBuffer.wrap(data, doff, length));\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void close() throws IOException\r\n" + 
				"/*     */   {\r\n" + 
				"/* 139 */     super.close();\r\n" + 
				"/*     */     \r\n" + 
				"/* 141 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 142 */     if (channel.isOpen()) {\r\n" + 
				"/* 143 */       channel.close();\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void lock(long offset, long length, int mask) throws IOException {\r\n" + 
				"/* 148 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 149 */     long size = length == 0L ? channel.size() - offset : length;\r\n" + 
				"/* 150 */     SftpFileSystemAccessor accessor = this.subsystem.getFileSystemAccessor();\r\n" + 
				"/* 151 */     ServerSession session = this.subsystem.getServerSession();\r\n" + 
				"/* 152 */     FileLock lock = accessor.tryLock(session, this.subsystem, getFile(), getFileHandle(), channel, offset, size, false);\r\n" + 
				"/* 153 */     if (lock == null) {\r\n" + 
				"/* 154 */       throw new SftpException(26, \"Overlapping lock held by another program on range [\" + offset + \"-\" + (offset + length));\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/*     */ \r\n" + 
				"/* 158 */     synchronized (this.locks) {\r\n" + 
				"/* 159 */       this.locks.add(lock);\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public void unlock(long offset, long length) throws IOException {\r\n" + 
				"/* 164 */     SeekableByteChannel channel = getFileChannel();\r\n" + 
				"/* 165 */     long size = length == 0L ? channel.size() - offset : length;\r\n" + 
				"/* 166 */     FileLock lock = null;\r\n" + 
				"/* 167 */     for (Iterator<FileLock> iterator = this.locks.iterator(); iterator.hasNext();) {\r\n" + 
				"/* 168 */       FileLock l = (FileLock)iterator.next();\r\n" + 
				"/* 169 */       if ((l.position() == offset) && (l.size() == size)) {\r\n" + 
				"/* 170 */         iterator.remove();\r\n" + 
				"/* 171 */         lock = l;\r\n" + 
				"/* 172 */         break;\r\n" + 
				"/*     */       }\r\n" + 
				"/*     */     }\r\n" + 
				"/* 175 */     if (lock == null) {\r\n" + 
				"/* 176 */       throw new SftpException(31, \"No matching lock found on range [\" + offset + \"-\" + (offset + length));\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/*     */ \r\n" + 
				"/* 180 */     lock.release();\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public static Collection<FileAttribute<?>> toFileAttributes(Map<String, Object> attrs) {\r\n" + 
				"/* 184 */     if (GenericUtils.isEmpty(attrs)) {\r\n" + 
				"/* 185 */       return Collections.emptyList();\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/* 188 */     Collection<FileAttribute<?>> attributes = null;\r\n" + 
				"/*     */     \r\n" + 
				"/* 190 */     for (Map.Entry<String, Object> attr : attrs.entrySet()) {\r\n" + 
				"/* 191 */       FileAttribute<?> fileAttr = toFileAttribute((String)attr.getKey(), attr.getValue());\r\n" + 
				"/* 192 */       if (fileAttr != null)\r\n" + 
				"/*     */       {\r\n" + 
				"/*     */ \r\n" + 
				"/* 195 */         if (attributes == null) {\r\n" + 
				"/* 196 */           attributes = new LinkedList();\r\n" + 
				"/*     */         }\r\n" + 
				"/* 198 */         attributes.add(fileAttr);\r\n" + 
				"/*     */       }\r\n" + 
				"/*     */     }\r\n" + 
				"/* 201 */     return attributes == null ? Collections.emptyList() : attributes;\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */   public static FileAttribute<?> toFileAttribute(String key, Object val)\r\n" + 
				"/*     */   {\r\n" + 
				"/* 206 */     if (\"isOther\".equals(key)) {\r\n" + 
				"/* 207 */       if (((Boolean)val).booleanValue()) {\r\n" + 
				"/* 208 */         throw new IllegalArgumentException(\"Not allowed to use \" + key + \"=\" + val);\r\n" + 
				"/*     */       }\r\n" + 
				"/* 210 */       return null; }\r\n" + 
				"/* 211 */     if (\"isRegular\".equals(key)) {\r\n" + 
				"/* 212 */       if (!((Boolean)val).booleanValue()) {\r\n" + 
				"/* 213 */         throw new IllegalArgumentException(\"Not allowed to use \" + key + \"=\" + val);\r\n" + 
				"/*     */       }\r\n" + 
				"/* 215 */       return null;\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/* 218 */     return new FileHandle.1(key, val);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/*     */ \r\n" + 
				"/*     */ \r\n" + 
				"/*     */ \r\n" + 
				"/*     */ \r\n" + 
				"/*     */   public static Set<StandardOpenOption> getOpenOptions(int flags, int access)\r\n" + 
				"/*     */   {\r\n" + 
				"/* 239 */     Set<StandardOpenOption> options = EnumSet.noneOf(StandardOpenOption.class);\r\n" + 
				"/* 240 */     if (((access & 0x1) != 0) || ((access & 0x80) != 0)) {\r\n" + 
				"/* 241 */       options.add(StandardOpenOption.READ);\r\n" + 
				"/*     */     }\r\n" + 
				"/* 243 */     if (((access & 0x2) != 0) || ((access & 0x100) != 0)) {\r\n" + 
				"/* 244 */       options.add(StandardOpenOption.WRITE);\r\n" + 
				"/*     */     }";
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("FileH$andle(SftpSubsystem, Path, String, int, int, Map<String, Object>)", methods.get(0).getName());
		assertEquals("getOpenOptions() : Set<StandardOpenOption>", methods.get(1).getName());
		assertEquals("getFileAttributes() : Collection<FileAttribute<?>>", methods.get(2).getName());
		assertEquals("getFileChannel() : SeekableByteChannel", methods.get(3).getName());
		assertEquals("getAccessMask() : int", methods.get(4).getName());
		assertEquals("isOpenAppend() : boolean", methods.get(5).getName());
		assertEquals("read(byte[], long) : int", methods.get(6).getName());
		assertEquals("read(byte[], int, int, long) : int", methods.get(7).getName());
		assertEquals("append(byte[]) : void", methods.get(8).getName());
		assertEquals("append(byte[], int, int) : void", methods.get(9).getName());
		assertEquals("write(byte[], long) : void", methods.get(10).getName());
		assertEquals("write(byte[], int, int, long) : void", methods.get(11).getName());
		assertEquals("close() : void", methods.get(12).getName());
		assertEquals("lock(long, long, int) : void", methods.get(13).getName());
		assertEquals("unlock(long, long) : void", methods.get(14).getName());
		assertEquals("toFileAttributes(Map<String, Object> attr) : Collection<FileAttribute<?>>", methods.get(15).getName());
		assertEquals("toFileAttribute(String, Object) : FileAttribute<?>", methods.get(16).getName());
		assertEquals("getOpenOptions(int, int) : Set<StandardOpenOption>", methods.get(17).getName());
	}
	
	@Test
	public void testMethodsInClass() {
		String code = "/*    */ class ContentPanel\r\n" +
					"/*    */   extends JPanel\r\n" +
					"/*    */ {\r\n" +
					"/*    */   private static final long serialVersionUID = -4853614180239352039L;\r\n" +
					"/*    */   private Component content;\r\n" +
					"/*    */   \r\n" +
					"/*    */   private ContentPanel()\r\n" +
					"/*    */   {\r\n" +
					"/* 17 */     super(new BorderLayout());\r\n" +
					"/* 18 */     setBorder(Settings.EMPTY_BORDER);\r\n" +
					"/*    */   }\r\n" +
					"/*    */   \r\n" +
					"/*    */   ContentPanel(FilterPanel filter, Component content) {\r\n" +
					"/* 22 */     this();\r\n" +
					"/* 23 */     add(filter, \"North\");\r\n" +
					"/* 24 */     add(this.content = content, \"Center\");\r\n" +
					"/*    */   }\r\n" +
					"/*    */   \r\n" +
					"/*    */   ContentPanel(Component content) {\r\n" +
					"/* 28 */     this();\r\n" +
					"/* 248 */     switch (accessDisposition) {\r\n" +
					"/* 29 */     add(this.content = content);\r\n" +
					"/*    */   }\r\n" +
					"/*    */   \r\n" +
					"/*    */   Component getContent() {\r\n" +
					"/* 33 */     return this.content;\r\n" +
					"/*    */   }\r\n" +
					"/*    */ \r\n" +
					"/*     */   JarNode getRoot() {\r\n" +
					"/* 276 */     return this.root;\r\n" +
					"/*     */   }\r\n" +
					"/*     */   \r\n" +
					"/*     */   int getCode() {\r\n" +
					"/* 107 */     return this.code.intValue();\r\n" +
					"/*     */   }\r\n" +
					"/*     */   \r\n" +
					"/*     */ \r\n" +
					"/*     */ <T>  public static Set<StandardOpenOption> getOpenOptions(int flags, int access)\r\n" +
					"/*     */   {\r\n" +
					"/*     */   \r\n" +
					"/*     */   public static   FileAttribute<?>  toFile$Attribute    (String key, Object val)\r\n" +
					"/*     */   {\r\n" +
					"/* 195 */         if (attributes == null) {\r\n" +
					"/* 196 */           attributes = new LinkedList();\r\n" +
					"/*     */         }\r\n" +
					"/*     */ \r\n" +
					"/* 158 */     synchronized (this.locks) {\r\n" +
					"/* 159 */       this.locks.add(lock);\r\n" +
					"/*     */     }\r\n" +
					"/*     */   \r\n" +
					"/*     */   public void append(byte[] data) throws IOException {\r\n" +
					"/* 119 */     append(data, 0, data.length);\r\n" +
					"/*     */   }\r\n" +
					"/*     */   \r\n" +
					"/*     */   final Set<StandardOpenOption> getOpenOptions() {\r\n" +
					"/*  89 */     return this.openOptions;\r\n" +
					"/*     */   }\r\n" +
					"/*  81 */       signalHandleOpen(subsystem);\r\n" +
					"/*     */     } catch (IOException e) {\r\n" +
					"/*  83 */       close();\r\n" +
					"/*  73 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, fileAttrs);\r\n" +
					"/*     */     } catch (UnsupportedOperationException e) { SeekableByteChannel channel;\r\n" +
					"/*  75 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, \r\n" +
					"/*     */   private final SeekableByteChannel fileChannel;\r\n" +
					"/*  51 */   private final List<FileLock> locks = new ArrayList();\r\n" +
					"/*     */   private final SftpSubsystem subsystem;\r\n" +
					"/*     */   private final Set<StandardOpenOption> openOptions;\r\n" +
					"/*     */   private final Collection<FileAttribute<?>> fileAttributes;\r\n" +
					"/*     */   \r\n" +
					"/*     */   public Component(SftpSubsystem subsystem, Path file, String handle, int flags, int access, Map<String, Object> attrs) throws IOException {\r\n" +
					"/*  57 */     super(file, handle);\r\n" +
					"/*     */   \r\n" +
					"/*     */   \r\n" +
					"/*     */   Component(SftpSubsystem subsystem ...) throws IOException {\r\n" +
					"/*  57 */     super(file, handle);\r\n" +
					"/*     */   \r\n" +
					"/*  62 */     this.fileAttributes = Collections.unmodifiableCollection(toFileAttributes(attrs));\r\n" +
					"/*  63 */     signalHandleOpening(subsystem);\r\n" +
					"/*     */     \r\n" +
					"/*  67 */     FileAttribute<?>[] fileAttrs = GenericUtils.isEmpty(this.fileAttributes) ? IoUtils.EMPTY_FILE_ATTRIBUTES : (FileAttribute[])this.fileAttributes.toArray(new FileAttribute[this.fileAttributes.size()]);\r\n" +
					"/*     */     \r\n" +
					"/*  73 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, fileAttrs);\r\n" +
					"/*     */     } catch (UnsupportedOperationException e) { SeekableByteChannel channel;\r\n" +
					"/*  75 */       channel = accessor.openFile(session, subsystem, file, handle, this.openOptions, \r\n" +
					"/*     */     {\r\n" +
					"/*  81 */       signalHandleOpen(subsystem);{\r\n" +
					"/*     */     } catch (IOException e) {{\r\n" +
					"/*  83 */       close();{\r\n" +
					"/*  84 */       throw e;{\r\n" +
					"/* 151 */     ServerSession session = this.subsystem.getServerSession();\r\n" +
					"/* 152 */     FileLock lock = accessor.tryLock(session, this.subsystem, getFile(), getFileHandle(), channel, offset, size, false);\r\n" +
					"/* 153 */     if (lock == null) {\r\n" +
					"/*     */     }{\r\n" +
					"/*  63 */     signalHandleOpening(subsystem);\r\n" +
					"/* 247 */     int accessDisposition = flags & 0x7;\r\n" +
					"/* 248 */     switch (accessDisposition) {\r\n" +
					"/*     */     case 0: \r\n" +
					"/*     */   \r\n" +
					"/*  50 */   JarNode(String name, String path, File archive, boolean isDirectory) { this.name = name;\r\n" +
					"/*  51 */     this.path = path;\r\n" +
					"/*  52 */     this.archive = archive;\r\n" +
					"/*  53 */     this.isDirectory = isDirectory;\r\n" +
					"/*     */   }\r\n" +
					"/*     */   \r\n" +
					"/* 376 */   private static JarNode getNode(ActionEvent e) { JarNode.JarNodeMenuItem item = (JarNode.JarNodeMenuItem)e.getSource();\r\n" +
					"/* 377 */     return (JarNode)item.path.getLastPathComponent();\r\n" +
					"/*     */   }\r\n" +
					"/*     */ \r\n" +
					"/*     */   static Cmd.Result runWithJava6(File workingDir, String... command)\r\n" +
					"/*     */   {\r\n" +
					"/*  80 */     ProcessBuilder processBuilder = new ProcessBuilder(command);\r\n" +
					"/*  81 */     if (workingDir != null) {\r\n" +
					"/* 160 */     return new Object[] { n.name, Long.valueOf(size), Long.valueOf(compSize), \r\n" +
					"/* 161 */       formatTime(n.time), formatTime(n.lastModTime), formatTime(n.creationTime), formatTime(n.lastAccessTime), \r\n" +
					"/* 162 */       formatMethod(n.method), n.comment, formatAttributes(n.attrs), n.certs, n.signers, Long.toString(n.crc, 16), \r\n" +
					"/* 163 */       formatExtra(n.extra) }; \r\n" +
					"/*     */   } \r\n" +
					"/*    */ }\r\n";
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("ContentPanel()", methods.get(0).getName());
		assertEquals("ContentPanel(FilterPanel, Component)", methods.get(1).getName());
		assertEquals("ContentPanel(Component)", methods.get(2).getName());
		assertEquals("getContent() : Component", methods.get(3).getName());
		assertEquals("getRoot() : JarNode", methods.get(4).getName());
		assertEquals("getCode() : int", methods.get(5).getName());
		assertEquals("getOpenOptions(int, int) : <T> Set<StandardOpenOption>", methods.get(6).getName());
		assertEquals("toFile$Attribute(String, Object) : FileAttribute<?>", methods.get(7).getName());
		assertEquals("append(byte[]) : void", methods.get(8).getName());
		assertEquals("getOpenOptions() : Set<StandardOpenOption>", methods.get(9).getName());
		assertEquals("Component(SftpSubsystem, Path, String, int, int, Map<String, Object>) : ", methods.get(10).getName());
		assertEquals("Component(SftpSubsystem...) : ", methods.get(11).getName());
	}

	@Test
	public void testMethodsInClassJarTree$1() {
		String code = "/*     */ class JarTree$1\r\n" + 
				"/*     */   extends Jar\r\n" + 
				"/*     */ {\r\n" + 
				"/*     */   JarTree$1(JarTree this$0, File file, File paramFile1)\r\n" + 
				"/*     */   {\r\n" + 
				"/* 255 */     super(file);\r\n" + 
				"/*     */   }\r\n" + 
				"/*     */   \r\n" + 
				"/* 258 */   protected void process(JarEntry entry) throws IOException { JarTree.access$300(entry, JarTree.access$200(this.this$0), this.val$dst); }\r\n" +
				
				"/*     */   public long skip(long n)\r\n" + 
				"/*     */     throws IOException\r\n" + 
				"/*     */   {\r\n" + 
				"/*  88 */     long newIndex = this.index + n;\r\n" + 
				"/*  89 */     long bufLen = Math.max(0L, this.available);\r\n" + 
				"/*  90 */     long skipLen; if (newIndex > bufLen)\r\n" + 
				"/*     */     {\r\n" + 
				"/*  92 */       long extraLen = newIndex - bufLen;\r\n" + 
				"/*  93 */       this.offset += extraLen;\r\n" + 
				"/*  94 */       long skipLen = Math.max(0L, bufLen - this.index) + extraLen;\r\n" + 
				"/*     */       \r\n" + 
				"/*  96 */       this.index = 0;\r\n" + 
				"/*  97 */       this.available = 0;\r\n" + 
				"/*  98 */     } else if (newIndex < 0L)\r\n" + 
				"/*     */     {\r\n" + 
				"/* 100 */       long startOffset = this.offset - bufLen;\r\n" + 
				"/* 101 */       long newOffset = startOffset + newIndex;\r\n" + 
				"/* 102 */       newOffset = Math.max(0L, newOffset);\r\n" + 
				"/* 103 */       long skipLen = this.index - newIndex;\r\n" + 
				"/* 104 */       this.offset = newOffset;\r\n" + 
				"/*     */       \r\n" + 
				"/* 106 */       this.index = 0;\r\n" + 
				"/* 107 */       this.available = 0;\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     else {\r\n" + 
				"/* 110 */       this.index = ((int)newIndex);\r\n" + 
				"/*     */       \r\n" + 
				"/* 112 */       skipLen = Math.abs(n);\r\n" + 
				"/*     */     }\r\n" + 
				"/*     */     \r\n" + 
				"/* 115 */     return skipLen;\r\n" + 
				"/*     */   }\r\n" + 
				
				
				"/*     */ }";
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals(3, methods.size());
		assertEquals("JarTree$1(JarTree, File, File)", methods.get(0).getName());
		assertEquals("process(JarEntry) : void", methods.get(1).getName());
		assertEquals("skip(long) : long", methods.get(2).getName());
	}

	@Test
	public void testMethodsInClassJarTree$JarTreeClickSelection() {
		String code = "/*    */ class JarTree$JarTreeClickSelection\r\n" + 
				"/*    */ {\r\n" + 
				"/*    */   private static TreePath[] nodes;\r\n" + 
				"/*    */   \r\n" + 
				"/*    */   static TreePath[] getNodes()\r\n" + 
				"/*    */   {\r\n" + 
				"/* 69 */     return nodes;\r\n" + 
				"/*    */   }\r\n" + 
				"/*    */   \r\n" + 
				"/*    */   static synchronized void setNodes(TreePath[] nodes) {\r\n" + 
				"/* 73 */     nodes = nodes;\r\n" + 
				"/*    */   }\r\n" + 
				"/*    */   JarTree$JarTreeClickSelection(JarTree.JarTreeMouseListener this$1) {}\r\n" +
				"/*     */   public static <L extends SftpEventListener> L validateListener(L listener)\r\n" + 
				"/*     */   {\r\n" + 
				"/* 432 */     return (SftpEventListener)SshdEventListener.validateListener(listener, SftpEventListener.class.getSimpleName());\r\n" + 
				"/*     */   }\r\n" + 
				"/*    */ }";
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("getNodes() : TreePath[]", methods.get(0).getName());
		assertEquals("setNodes(TreePath[]) : void", methods.get(1).getName());
		assertEquals("JarTree$JarTreeClickSelection(JarTree.JarTreeMouseListener)", methods.get(2).getName());
		assertEquals("validateListener(L) : <L extends SftpEventListener> L", methods.get(3).getName());
	}

}

