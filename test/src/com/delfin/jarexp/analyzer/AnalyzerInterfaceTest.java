package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;


public class AnalyzerInterfaceTest {

	@Test
	public void testSftpClient() {
		String code = "/*      */ package org.apache.sshd.client.subsystem.sftp;\r\n" + 
				"/*      */ \r\n" + 
				"/*      */ import java.io.IOException;\r\n" + 
				"/*      */ import java.io.InputStream;\r\n" + 
				"/*      */ public abstract interface SftpClient\r\n" + 
				"/*      */   extends SubsystemClient\r\n" + 
				"/*      */ {\r\n" + 
				"/*      */   public int read(SftpClient.Handle handle, long fileOffset, byte[] dst)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  673 */     return read(handle, fileOffset, dst, null);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public int read(SftpClient.Handle handle, long fileOffset, byte[] dst, AtomicReference<Boolean> eofSignalled)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  691 */     return read(handle, fileOffset, dst, 0, dst.length, eofSignalled);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public int read(SftpClient.Handle handle, long fileOffset, byte[] dst, int dstOffset, int len) throws IOException {\r\n" + 
				"/*  695 */     return read(handle, fileOffset, dst, dstOffset, len, null);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract int read(SftpClient.Handle paramHandle, long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2, AtomicReference<Boolean> paramAtomicReference)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public void write(SftpClient.Handle handle, long fileOffset, byte[] src)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  716 */     write(handle, fileOffset, src, 0, src.length);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void write(SftpClient.Handle paramHandle, long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void mkdir(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void rmdir(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract SftpClient.CloseableHandle openDir(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public List<SftpClient.DirEntry> readDir(SftpClient.Handle handle)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  767 */     return readDir(handle, null);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract List<SftpClient.DirEntry> readDir(SftpClient.Handle paramHandle, AtomicReference<Boolean> paramAtomicReference)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public Iterable<SftpClient.DirEntry> listDir(SftpClient.Handle handle)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  792 */     if (!isOpen()) {\r\n" + 
				"/*  793 */       throw new IOException(\"listDir(\" + handle + \") client is closed\");\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/*  796 */     return new StfpIterableDirHandle(this, handle);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract String canonicalPath(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract SftpClient.Attributes stat(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract SftpClient.Attributes lstat(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract SftpClient.Attributes stat(SftpClient.Handle paramHandle)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void setStat(String paramString, SftpClient.Attributes paramAttributes)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void setStat(SftpClient.Handle paramHandle, SftpClient.Attributes paramAttributes)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract String readLink(String paramString)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public void symLink(String linkPath, String targetPath)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  873 */     link(linkPath, targetPath, true);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void link(String paramString1, String paramString2, boolean paramBoolean)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void lock(SftpClient.Handle paramHandle, long paramLong1, long paramLong2, int paramInt)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract void unlock(SftpClient.Handle paramHandle, long paramLong1, long paramLong2)\r\n" + 
				"/*      */     throws IOException;\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public SftpRemotePathChannel openRemotePathChannel(String path, OpenOption... options)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  896 */     return openRemotePathChannel(path, GenericUtils.isEmpty(options) ? Collections.emptyList() : Arrays.asList(options));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public SftpRemotePathChannel openRemotePathChannel(String path, Collection<? extends OpenOption> options) throws IOException {\r\n" + 
				"/*  900 */     return openRemoteFileChannel(path, SftpClient.OpenMode.fromOpenOptions(options));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public SftpRemotePathChannel openRemoteFileChannel(String path, SftpClient.OpenMode... modes) throws IOException {\r\n" + 
				"/*  904 */     return openRemoteFileChannel(path, GenericUtils.isEmpty(modes) ? Collections.emptyList() : Arrays.asList(modes));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public SftpRemotePathChannel openRemoteFileChannel(String path, Collection<SftpClient.OpenMode> modes)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  920 */     return new SftpRemotePathChannel(path, this, false, GenericUtils.isEmpty(modes) ? DEFAULT_CHANNEL_MODES : modes);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public Iterable<SftpClient.DirEntry> readDir(String path)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  931 */     if (!isOpen()) {\r\n" + 
				"/*  932 */       throw new IOException(\"readDir(\" + path + \") client is closed\");\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/*  935 */     return new SftpIterableDirEntry(this, path);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path) throws IOException {\r\n" + 
				"/*  939 */     return read(path, 32768);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path, int bufferSize) throws IOException {\r\n" + 
				"/*  943 */     return read(path, bufferSize, EnumSet.of(SftpClient.OpenMode.Read));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path, SftpClient.OpenMode... mode) throws IOException {\r\n" + 
				"/*  947 */     return read(path, 32768, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path, int bufferSize, SftpClient.OpenMode... mode) throws IOException {\r\n" + 
				"/*  951 */     return read(path, bufferSize, GenericUtils.of(mode));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path, Collection<SftpClient.OpenMode> mode) throws IOException {\r\n" + 
				"/*  955 */     return read(path, 32768, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public InputStream read(String path, int bufferSize, Collection<SftpClient.OpenMode> mode)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/*  968 */     if (bufferSize < 127) {\r\n" + 
				"/*  969 */       throw new IllegalArgumentException(\"Insufficient read buffer size: \" + bufferSize + \", min.=\" + 127);\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/*  972 */     if (!isOpen()) {\r\n" + 
				"/*  973 */       throw new IOException(\"read(\" + path + \")[\" + mode + \"] size=\" + bufferSize + \": client is closed\");\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/*  976 */     return new SftpInputStreamWithChannel(this, bufferSize, path, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path) throws IOException {\r\n" + 
				"/*  980 */     return write(path, 32768);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path, int bufferSize) throws IOException {\r\n" + 
				"/*  984 */     return write(path, bufferSize, EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create, SftpClient.OpenMode.Truncate));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path, SftpClient.OpenMode... mode) throws IOException {\r\n" + 
				"/*  988 */     return write(path, 32768, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path, int bufferSize, SftpClient.OpenMode... mode) throws IOException {\r\n" + 
				"/*  992 */     return write(path, bufferSize, GenericUtils.of(mode));\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path, Collection<SftpClient.OpenMode> mode) throws IOException {\r\n" + 
				"/*  996 */     return write(path, 32768, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public OutputStream write(String path, int bufferSize, Collection<SftpClient.OpenMode> mode)\r\n" + 
				"/*      */     throws IOException\r\n" + 
				"/*      */   {\r\n" + 
				"/* 1009 */     if (bufferSize < 127) {\r\n" + 
				"/* 1010 */       throw new IllegalArgumentException(\"Insufficient write buffer size: \" + bufferSize + \", min.=\" + 127);\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/* 1013 */     if (!isOpen()) {\r\n" + 
				"/* 1014 */       throw new IOException(\"write(\" + path + \")[\" + mode + \"] size=\" + bufferSize + \": client is closed\");\r\n" + 
				"/*      */     }\r\n" + 
				"/*      */     \r\n" + 
				"/* 1017 */     return new SftpOutputStreamWithChannel(this, bufferSize, path, mode);\r\n" + 
				"/*      */   }\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract <E extends SftpClientExtension> E getExtension(Class<? extends E> paramClass);\r\n" + 
				"/*      */   \r\n" + 
				"/*      */   public abstract SftpClientExtension getExtension(String paramString);\r\n" + 
				"/*      */ }\r\n" + 
				"";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("read(SftpClient.Handle, long, byte[]) : int", methods.get(0).getName());
		assertEquals("read(SftpClient.Handle, long, byte[], AtomicReference<Boolean>) : int", methods.get(1).getName());
		assertEquals("read(SftpClient.Handle, long, byte[], int, int) : int", methods.get(2).getName());
		assertEquals("read(SftpClient.Handle, long, byte[], int, int, AtomicReference<Boolean>) : int", methods.get(3).getName());
		assertEquals("write(SftpClient.Handle, long, byte[]) : void", methods.get(4).getName());
		assertEquals("write(SftpClient.Handle, long, byte[], int, int) : void", methods.get(5).getName());
		assertEquals("mkdir(String) : void", methods.get(6).getName());
		assertEquals("rmdir(String) : void", methods.get(7).getName());
		assertEquals("openDir(String) : CloseableHandle", methods.get(8).getName());
		assertEquals("readDir(SftpClient.Handle) : List<SftpClient.DirEntry>", methods.get(9).getName());
		assertEquals("readDir(SftpClient.Handle, AtomicReference<Boolean>) : List<SftpClient.DirEntry>", methods.get(10).getName());
		assertEquals("listDir(SftpClient.Handle) : Iterable<SftpClient.DirEntry>", methods.get(11).getName());
		assertEquals("canonicalPath(String) : String", methods.get(12).getName());
		assertEquals("stat(String) : Attributes", methods.get(13).getName());
		assertEquals("lstat(String) : Attributes", methods.get(14).getName());
		assertEquals("stat(SftpClient.Handle) : Attributes", methods.get(15).getName());
		assertEquals("setStat(String, SftpClient.Attributes) : void", methods.get(16).getName());
		assertEquals("setStat(SftpClient.Handle, SftpClient.Attributes) : void", methods.get(17).getName());
		assertEquals("readLink(String) : String", methods.get(18).getName());
		assertEquals("symLink(String, String) : void", methods.get(19).getName());
		assertEquals("link(String, String, boolean) : void", methods.get(20).getName());
		assertEquals("lock(SftpClient.Handle, long, long, int) : void", methods.get(21).getName());
		assertEquals("unlock(SftpClient.Handle, long, long) : void", methods.get(22).getName());
		assertEquals("openRemotePathChannel(String, OpenOption...) : SftpRemotePathChannel", methods.get(23).getName());
		assertEquals("openRemotePathChannel(String, Collection<? extends OpenOption>) : SftpRemotePathChannel", methods.get(24).getName());
		assertEquals("openRemoteFileChannel(String, SftpClient.OpenMode...) : SftpRemotePathChannel", methods.get(25).getName());
		assertEquals("openRemoteFileChannel(String, Collection<SftpClient.OpenMode>) : SftpRemotePathChannel", methods.get(26).getName());
		assertEquals("readDir(String) : Iterable<SftpClient.DirEntry>", methods.get(27).getName());
		assertEquals("read(String) : InputStream", methods.get(28).getName());
		assertEquals("read(String, int) : InputStream", methods.get(29).getName());
		assertEquals("read(String, SftpClient.OpenMode...) : InputStream", methods.get(30).getName());
		assertEquals("read(String, int, SftpClient.OpenMode...) : InputStream", methods.get(31).getName());
		assertEquals("read(String, Collection<SftpClient.OpenMode>) : InputStream", methods.get(32).getName());
		assertEquals("read(String, int, Collection<SftpClient.OpenMode>) : InputStream", methods.get(33).getName());
		assertEquals("write(String) : OutputStream", methods.get(34).getName());
		assertEquals("write(String, int) : OutputStream", methods.get(35).getName());
		assertEquals("write(String, SftpClient.OpenMode...) : OutputStream", methods.get(36).getName());
		assertEquals("write(String, int, SftpClient.OpenMode...) : OutputStream", methods.get(37).getName());
		assertEquals("write(String, Collection<SftpClient.OpenMode>) : OutputStream", methods.get(38).getName());
		assertEquals("write(String, int, Collection<SftpClient.OpenMode>) : OutputStream", methods.get(39).getName());
		assertEquals("getExtension(Class<? extends E>) : <E extends SftpClientExtension> E", methods.get(40).getName());
		assertEquals("getExtension(String) : SftpClientExtension", methods.get(41).getName());
		
	}

	@Test
	public void testDefaultMethods() {
		String code = "public @interface DynamicParameter\r\n" + 
				"{\r\n" + 
				"  String[] names() default {};\r\n" + 
				"  \r\n" + 
				"  boolean required() default false;\r\n" + 
				"  \r\n" + 
				"  String description() default \"\";\r\n" + 
				"  \r\n" + 
				"  String descriptionKey() default \"\";\r\n" + 
				"  \r\n" + 
				"  boolean hidden() default false;\r\n" + 
				"  \r\n" + 
				"  Class<? extends IParameterValidator>[] validateWith() default {NoValidator.class};\r\n" + 
				"  \r\n" + 
				"  String assignment() default \"=\";\r\n" + 
				"  \r\n" + 
				"  Class<? extends IValueValidator>[] validateValueWith() default {NoValueValidator.class};\r\n" + 
				"  \r\n" + 
				"  int order() default -1;\r\n" + 
				"}";
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		assertEquals("names() : String[]", methods.get(0).getName());
		assertEquals("required() : boolean", methods.get(1).getName());
		assertEquals("description() : String", methods.get(2).getName());
		assertEquals("descriptionKey() : String", methods.get(3).getName());
		assertEquals("hidden() : boolean", methods.get(4).getName());
		assertEquals("validateWith() : Class<? extends IParameterValidator>[]", methods.get(5).getName());
		assertEquals("assignment() : String", methods.get(6).getName());
		assertEquals("validateValueWith() : Class<? extends IValueValidator>[]", methods.get(7).getName());
		assertEquals("order() : int", methods.get(8).getName());
		
	}

}

