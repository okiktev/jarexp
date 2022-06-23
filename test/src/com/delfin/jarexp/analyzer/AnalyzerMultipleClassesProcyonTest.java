package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;


public class AnalyzerMultipleClassesProcyonTest {

	@Test
	public void testRPrintUtilities() {
		String code = "package org.fife.print;\r\n" + 
				"\r\n" + 
				"import javax.swing.text.TabExpander;\r\n" + 
				"import javax.swing.text.Element;\r\n" + 
				"\r\n" + 
				"public abstract class RPrintUtilities\r\n" + 
				"{\r\n" + 
				"    private static FontMetrics fm;\r\n" + 
				"    \r\n" + 
				"    private static int getLineBreakPoint(final String line, final int maxCharsPerLine) {\r\n" + 
				"        int breakPoint = /*EL:78*/-1;\r\n" + 
				"        /*SL:79*/for (int i = 0; i < RPrintUtilities.BREAK_CHARS.length; ++i) {\r\n" + 
				"            final int breakCharPos = /*EL:80*/line.lastIndexOf((int)RPrintUtilities.BREAK_CHARS[i], maxCharsPerLine - 1);\r\n" + 
				"            /*SL:81*/if (breakCharPos > breakPoint) {\r\n" + 
				"                /*SL:82*/breakPoint = breakCharPos;\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"        /*SL:86*/return (breakPoint == -1) ? (maxCharsPerLine - 1) : breakPoint;\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    private static Segment removeEndingWhitespace(final Segment segment) {\r\n" + 
				"        int toTrim = /*EL:524*/0;\r\n" + 
				"        /*SL:526*/for (char currentChar = segment.setIndex(segment.getEndIndex() - 1); (currentChar == ' ' || currentChar == '\\t') && currentChar != '\\uffff'; /*SL:528*/currentChar = segment.previous()) {\r\n" + 
				"            ++toTrim;\r\n" + 
				"        }\r\n" + 
				"        final String stringVal = /*EL:530*/segment.toString();\r\n" + 
				"        final String newStringVal = /*EL:531*/stringVal.substring(0, stringVal.length() - toTrim);\r\n" + 
				"        /*SL:532*/return new Segment(newStringVal.toCharArray(), 0, newStringVal.length());\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    static {\r\n" + 
				"        /*SL:42*/BREAK_CHARS = new char[] { ' ', '\\t', ',', '.', ';', '?', '!' };\r\n" + 
				"    }\r\n" + 
				"    \r\n" + 
				"    private static class RPrintTabExpander implements TabExpander\r\n" + 
				"    {\r\n" + 
				"        RPrintTabExpander() {\r\n" + 
				"        }\r\n" + 
				"        \r\n" + 
				"        public float nextTabStop(final float x, final int tabOffset) {\r\n" + 
				"            /*SL:547*/if (RPrintUtilities.tabSizeInSpaces == 0) {\r\n" + 
				"                /*SL:548*/return x;\r\n" + 
				"            }\r\n" + 
				"            final int tabSizeInPixels = /*EL:550*/RPrintUtilities.tabSizeInSpaces * RPrintUtilities.fm.charWidth(' ');\r\n" + 
				"            final int ntabs = /*EL:551*/((int)x - RPrintUtilities.xOffset) / tabSizeInPixels;\r\n" + 
				"            /*SL:552*/return RPrintUtilities.xOffset + (ntabs + 1.0f) * tabSizeInPixels;\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"}\r\n" + 
				"";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(2, res.size());
		List<IJavaItem> methods1 = res.get(0).getChildren();
		assertEquals("getLineBreakPoint(String, int) : int", methods1.get(0).getName());
		assertEquals("removeEndingWhitespace(Segment) : Segment", methods1.get(1).getName());
		
		
		List<IJavaItem> methods2 = res.get(1).getChildren();
		assertEquals("RPrintTabExpander()", methods2.get(0).getName());
		assertEquals("nextTabStop(float, int) : float", methods2.get(1).getName());
	}

}

