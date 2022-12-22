package com.delfin.jarexp.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;


public class AnalyzerGroovyCompiledTest {

	@Test
	public void testClassName() {
		String code = "package io.xh.hoist.configuration;\r\n" + 
				"\r\n" + 
				"import groovy.lang.Closure;\r\n" + 
				"import groovy.transform.Generated;\r\n" + 
				"import java.util.List;\r\n" + 
				"import java.util.Map;\r\n" + 
				"import org.codehaus.groovy.runtime.GeneratedClosure;\r\n" + 
				"import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;\r\n" + 
				"import org.codehaus.groovy.runtime.callsite.CallSite;\r\n" + 
				"\r\n" + 
				"public final class ApplicationConfig$_defaultConfig_closure1$_closure5$_closure8 extends Closure implements GeneratedClosure {\r\n" + 
				"    public ApplicationConfig$_defaultConfig_closure1$_closure5$_closure8(Object _outerInstance, Object _thisObject) {\r\n" + 
				"        CallSite[] var3 = $getCallSiteArray();\r\n" + 
				"        super(_outerInstance, _thisObject);\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    public Object doCall(Object it) {\r\n" + 
				"        CallSite[] var2 = $getCallSiteArray();\r\n" + 
				"        List var3 = ScriptBytecodeAdapter.createList(new Object[]{\"Gecko\", \"WebKit\", \"Presto\", \"Trident\"});// 39\r\n" + 
				"        ScriptBytecodeAdapter.setProperty(var3, (Class)null, var2[0].callGetProperty(var2[1].callGetProperty(var2[2].callGroovyObjectGetProperty(this))), (String)\"userAgents\");\r\n" + 
				"        Map var4 = ScriptBytecodeAdapter.createMap(new Object[]{\"all\", \"*/*\", \"atom\", \"application/atom+xml\", \"css\", \"text/css\", \"csv\", \"text/csv\", \"form\", \"application/x-www-form-urlencoded\", \"html\", ScriptBytecodeAdapter.createList(new Object[]{\"text/html\", \"application/xhtml+xml\"}), \"js\", \"text/javascript\", \"json\", ScriptBytecodeAdapter.createList(new Object[]{\"application/json\", \"text/json\"}), \"multipartForm\", \"multipart/form-data\", \"rss\", \"application/rss+xml\", \"text\", \"text/plain\", \"hal\", ScriptBytecodeAdapter.createList(new Object[]{\"application/hal+json\", \"application/hal+xml\"}), \"xml\", ScriptBytecodeAdapter.createList(new Object[]{\"text/xml\", \"application/xml\"}), \"excel\", \"application/vnd.ms-excel\"});// 40 46 48 52 53\r\n" + 
				"        ScriptBytecodeAdapter.setGroovyObjectProperty(var4, ApplicationConfig$_defaultConfig_closure1$_closure5$_closure8.class, this, (String)\"types\");\r\n" + 
				"        return var4;\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    @Generated\r\n" + 
				"    public Object doCall() {\r\n" + 
				"        CallSite[] var1 = $getCallSiteArray();\r\n" + 
				"        return this.doCall((Object)null);\r\n" + 
				"    }\r\n" + 
				"}";


		List<IJavaItem> res = Analyzer.analyze(code);
		assertEquals(1, res.size());
		List<IJavaItem> methods = res.get(0).getChildren();

		assertEquals("doCall(Object) : Object", methods.get(0).getName());
		assertEquals("doCall() : Object", methods.get(1).getName());
	}

}

