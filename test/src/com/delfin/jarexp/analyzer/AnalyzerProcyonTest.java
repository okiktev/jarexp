package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.delfin.jarexp.analyzer.JavaMethod.ACCESS;

public class AnalyzerProcyonTest {

	@Test
	public void testMainMethod() {
		String code = "public class Main\r\n" + 
				"{\r\n" + 
				"    private static final Logger log;\r\n" + 
				"    \r\n" + 
				"    public static void main(final String[] args) {\r\n" + 
				"        /*SL:21*/SwingUtilities.invokeLater((Runnable)new Runnable() {\r\n" + 
				"            public void run() {\r\n" + 
				"                try {\r\n" + 
				"                    /*SL:24*/LogManager.getLogManager().readConfiguration(Main.class.getClassLoader().getResourceAsStream(\"logging.properties\"));\r\n" + 
				"                }\r\n" + 
				"                catch (IOException e) {\r\n" + 
				"                    System.err.println(/*EL:26*/\"Could not setup logger configuration.\");\r\n" + 
				"                    /*SL:27*/e.printStackTrace(System.err);\r\n" + 
				"                    /*SL:28*/throw new RuntimeException(\"Unable to initiate logger\", (Throwable)e);\r\n" + 
				"                }\r\n" + 
				"                /*SL:30*/Settings.initLookAndFeel();\r\n" + 
				"                /*SL:31*/Zip.setTempFileCreator(new Zip.TempFileCreator() {\r\n" + 
				"                    public File create(final String prefix, final String suffix) throws IOException {\r\n" + 
				"                        /*SL:34*/return File.createTempFile(prefix, suffix, Settings.getTmpDir());\r\n" + 
				"                    }\r\n" + 
				"                });\r\n" + 
				"                try {\r\n" + 
				"                    /*SL:38*/Content.createAndShowGUI(Main.getPassedFile(args));\r\n" + 
				"                }\r\n" + 
				"                catch (Exception e2) {\r\n" + 
				"                    /*SL:40*/Main.log.log(Level.SEVERE, \"An error occurred while starting application\", (Throwable)e2);\r\n" + 
				"                    /*SL:41*/JOptionPane.showMessageDialog((Component)null, (Object)(\"Something happened: \" + e2.getMessage()), \"Warning\", 2, (Icon)null);\r\n" + 
				"                    /*SL:42*/e2.printStackTrace();\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"        });\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    protected static File getPassedFile(final String[] args) {\r\n" + 
				"        /*SL:49*/if (args != null && args.length != 0) {\r\n" + 
				"            final File file = /*EL:50*/new File(args[0]);\r\n" + 
				"            /*SL:51*/if (file.exists()) {\r\n" + 
				"                /*SL:56*/return file;\r\n" + 
				"            }\r\n" + 
				"            final String msg = new StringBuilder().append(\"Passed file \").append((Object)file).append(\" is not exist\").toString();\r\n" + 
				"            System.err.println(msg);\r\n" + 
				"            Main.log.warning(msg);\r\n" + 
				"        }\r\n" + 
				"        /*SL:59*/return null;\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    static {\r\n" + 
				"        /*SL:18*/log = Logger.getLogger(Main.class.getCanonicalName());\r\n" + 
				"    }\r\n" + 
				"}";


		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("main(String[]) : void", methods.get(0).getName());
		assertEquals("run() : void", methods.get(1).getName());
		assertEquals("create(String, String) : File", methods.get(2).getName());
		assertEquals("getPassedFile(String[]) : File", methods.get(3).getName());
	}

	@Test
	public void testJarexpException() {
		String code = "public class JarexpException extends RuntimeException\r\n" + 
				"{\r\n" + 
				"    private static final long serialVersionUID = 3455357528854519419L;\r\n" + 
				"    \r\n" + 
				"    public JarexpException(final String message) {\r\n" + 
				"        /*SL:8*/super(message);\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public JarexpException(final String message, final Throwable cause) {\r\n" + 
				"        /*SL:12*/super(message, cause);\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public JarexpException(final Throwable e) {\r\n" + 
				"        /*SL:16*/super(e);\r\n" + 
				"    }\r\n" + 
				"}";
		
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("JarexpException(String)", methods.get(0).getName());
		assertEquals(ACCESS.PUBLIC, ((JavaMethod)methods.get(0)).access);
		assertEquals("JarexpException(String, Throwable)", methods.get(1).getName());
		assertEquals("JarexpException(Throwable)", methods.get(2).getName());
	}

	@Test
	public void testFileCopier() {
		String code = "private interface FileCopier\r\n" + 
				"{\r\n" + 
				"    void copy(final File p0, final File p1);\r\n" + 
				"}\r\n" + 
				"";
		
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("copy(File, File) : void", methods.get(0).getName());
		assertEquals(ACCESS.PUBLIC, ((JavaMethod)methods.get(0)).access);
	}

	@Test
	public void testClassFile() {
		String code = "import java.util.List;\r\n" + 
				"import java.util.ArrayList;\r\n" + 
				"\r\n" + 
				"public class ClassFile extends Base\r\n" + 
				"{\r\n" + 
				"    public Accessor getAccessor(final String name, final String descriptor) {\r\n" + 
				"        final Map<String, Accessor> map = /*EL:418*/(Map<String, Accessor>)this.accessors.get((Object)name);\r\n" + 
				"        /*SL:419*/return (map == null) ? null : ((Accessor)map.get((Object)descriptor));\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public Map<Integer, List<Integer>> getSwitchMaps() {\r\n" + 
				"        /*SL:424*/return this.switchMaps;\r\n" + 
				"    }\r\n" + 
				"}";
		
		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();
		
		assertEquals("getAccessor(String, String) : Accessor", methods.get(0).getName());
		assertEquals("getSwitchMaps() : Map<Integer, List<Integer>>", methods.get(1).getName());
		
		assertEquals(JavaMethod.ACCESS.PUBLIC, ((JavaMethod)methods.get(0)).access);
		assertEquals(JavaMethod.ACCESS.PUBLIC, ((JavaMethod)methods.get(1)).access);
	}

	@Test
	public void testLocalVariable() {
		String code = "package jd.core.model.classfile;\r\n" + 
				"\r\n" + 
				"public class LocalVariable implements Comparable<LocalVariable>\r\n" + 
				"{\r\n" + 
				"    public int start_pc;\r\n" + 
				"    public int length;\r\n" + 
				"    public int name_index;\r\n" + 
				"    \r\n" + 
				"    public LocalVariable(final int start_pc, final int length, final int name_index, final int signature_index, final int index) {\r\n" + 
				"        /*SL:27*/this(start_pc, length, name_index, signature_index, index, false, 0);\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public LocalVariable(final int start_pc, final int length, final int name_index, final int signature_index, final int index, final int typesBitSet) {\r\n" + 
				"        /*SL:35*/this(start_pc, length, name_index, signature_index, index, false, typesBitSet);\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public LocalVariable(final int start_pc, final int length, final int name_index, final int signature_index, final int index, final boolean exception) {\r\n" + 
				"        /*SL:42*/this(start_pc, length, name_index, signature_index, index, exception, 0);\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    protected LocalVariable(final int start_pc, final int length, final int name_index, final int signature_index, final int index, final boolean exceptionOrReturnAddress, final int typesBitField) {\r\n" +  
				"        /*SL:53*/this.index = index;\r\n" + 
				"        /*SL:54*/this.exceptionOrReturnAddress = exceptionOrReturnAddress;\r\n" + 
				"        /*SL:55*/this.declarationFlag = exceptionOrReturnAddress;\r\n" + 
				"        /*SL:56*/this.typesBitField = typesBitField;\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public void updateRange(final int offset) {\r\n" + 
				"        /*SL:61*/if (offset < this.start_pc) {\r\n" + 
				"            /*SL:63*/this.length += this.start_pc - offset;\r\n" + 
				"            /*SL:64*/this.start_pc = offset;\r\n" + 
				"        }\r\n" + 
				"        /*SL:67*/if (offset >= this.start_pc + this.length) {\r\n" + 
				"            /*SL:69*/this.length = offset - this.start_pc + 1;\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public void updateSignatureIndex(final int signatureIndex) {\r\n" + 
				"        /*SL:75*/this.signature_index = signatureIndex;\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public String toString() {\r\n" + 
				"        /*SL:80*/return /*EL:81*/new StringBuilder(\"LocalVariable{start_pc=\").append(this.start_pc).append(/*EL:82*/\", length=\").append(this.length).append(/*EL:83*/\", name_index=\").append(this.name_index).append(/*EL:84*/\", signature_index=\").append(this.signature_index).append(/*EL:85*/\", index=\").append(this.index).append(/*EL:86*/\"}\").toString();\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    public int compareTo(final LocalVariable other) {\r\n" + 
				"        /*SL:100*/if (this.start_pc != other.start_pc) {\r\n" + 
				"            /*SL:101*/return this.start_pc - other.start_pc;\r\n" + 
				"        }\r\n" + 
				"        /*SL:103*/return this.index - other.index;\r\n" + 
				"    }\r\n" + 
				"}";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();

		assertEquals("LocalVariable(int, int, int, int, int)", methods.get(0).getName());
		assertEquals("LocalVariable(int, int, int, int, int, int)", methods.get(1).getName());
		assertEquals("LocalVariable(int, int, int, int, int, boolean)", methods.get(2).getName());
		assertEquals("LocalVariable(int, int, int, int, int, boolean, int)", methods.get(3).getName());
		assertEquals("updateRange(int) : void", methods.get(4).getName());
		assertEquals("updateSignatureIndex(int) : void", methods.get(5).getName());
		assertEquals("toString() : String", methods.get(6).getName());
		assertEquals("compareTo(LocalVariable) : int", methods.get(7).getName());
	}

}

