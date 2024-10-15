package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.delfin.jarexp.analyzer.JavaMethod.ACCESS;

public class AnalyzerRecordTest {

	@Test
	public void testRecord() {
		String code = "package org.springframework.aot.generate;\r\n"
		        + "\r\n"
		        + "import org.springframework.javapoet.ClassName;\r\n"
		        + "import org.springframework.lang.Nullable;\r\n"
		        + "\r\n"
		        + "record GeneratedClasses$Owner(String featureNamePrefix, String featureName, ClassName target) {\r\n"
		        + "    private GeneratedClasses$Owner(String featureNamePrefix, String featureName, @Nullable ClassName target) {\r\n"
		        + "        this.featureNamePrefix = featureNamePrefix;\r\n"
		        + "        this.featureName = featureName;\r\n"
		        + "        this.target = target;\r\n"
		        + "    }\r\n"
		        + "\r\n"
		        + "    @Nullable\r\n"
		        + "    public ClassName target() {\r\n"
		        + "        return this.target;// 207\r\n"
		        + "    }\r\n"
		        + "}\r\n"
		        + "";

		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		IJavaItem item = res.get(0);
		assertEquals("GeneratedClasses$Owner(String featureNamePrefix, String featureName, ClassName target)", item.getName());

		List<IJavaItem> methods = item.getChildren();
		assertEquals("GeneratedClasses$Owner(String, String, @Nullable ClassName) : ", methods.get(0).getName());
		assertEquals(ACCESS.PRIVATE, ((JavaMethod)methods.get(0)).access);
		assertEquals("target() : ClassName", methods.get(1).getName());
		assertEquals(ACCESS.PUBLIC, ((JavaMethod)methods.get(1)).access);
	}

}

