package com.delfin.jarexp.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.delfin.jarexp.analyzer.IJavaItem.TYPE;
import com.delfin.jarexp.analyzer.JavaMethod.ACCESS;

public class Analyzer {

	private final static List<String> KEY_WORDS = new ArrayList<String>();
	static {
		KEY_WORDS.add("public");
		KEY_WORDS.add("private");
		KEY_WORDS.add("protected");
		KEY_WORDS.add("static");
		KEY_WORDS.add("abstract");
		KEY_WORDS.add("synchronized");
		KEY_WORDS.add("final");
	}

	private static final Pattern class_interface_Ptrn = Pattern.compile(
			"(public|private)*[^A-Za-z](class|interface|enum)\\s+([A-Za-z0-9$]+?[ ]?)([\\s]|$|<)+(.*)", Pattern.MULTILINE);

	private static final Pattern method_Ptrn = Pattern.compile(
			"((public|final|protected|abstract|private|static|synchronized|\\s|=|})*)(\\s<([A-Za-z\\s])*>\\s)*([A-Za-z0-9\\.]*(<[\\s\\.,A-Za-z0-9<>\\?\\[\\]]*>)*[\\.\\?\\[\\]]*)\\s+(([$A-Za-z0-9]*)\\s*\\(([A-Za-z$_0-9,\\.<>\\[\\]\\?\\s]*)\\))(.*)", Pattern.MULTILINE);

	public static List<IJavaItem> analyze(String content) {
		List<IJavaItem> res = new ArrayList<IJavaItem>(2);

		Matcher classMatcher = class_interface_Ptrn.matcher(content);
		while (classMatcher.find()) {
			String endOfLine = classMatcher.group(5);
			int s = endOfLine.lastIndexOf(';');
			if (s != -1) {
				int lc = endOfLine.lastIndexOf('{');
				if (lc == -1 || lc > s) {
					continue;
				}
			}
			int position = content.indexOf(classMatcher.group(0));
			if ("class".equals(classMatcher.group(2))) {
				res.add(new JavaClass(classMatcher.group(3), position));
			} else if ("enum".equals(classMatcher.group(2))) {
				res.add(new JavaEnum(classMatcher.group(3), position));				
			} else {
				res.add(new JavaInterface(classMatcher.group(3), position));
			}
		}

		IJavaItem previousHolder = getHolder(res, null, 0, content);
		IJavaItem holder = null;
		List<IJavaItem> methods = new ArrayList<IJavaItem>(5);
		Matcher methodMatcher = method_Ptrn.matcher(content);
		while (methodMatcher.find()) {
			int methodPosition = content.indexOf(methodMatcher.group(0));

			holder = getHolder(res, previousHolder, methodPosition, content);
			if (holder != previousHolder) {
				previousHolder.getChildren().addAll(methods);
				methods.clear();
				previousHolder = holder;
			}
			
			String modifiers = methodMatcher.group(1);
			int nl = modifiers.lastIndexOf('\n');
			if (nl != -1) {
				modifiers = modifiers.substring(nl + 1, modifiers.length());
			}
			if (modifiers.lastIndexOf('=') != -1 || modifiers.lastIndexOf('}') != -1) {
				continue;
			}

			String methodName = methodMatcher.group(8);
			if (methodName == null || methodName.isEmpty()) {
				continue;
			}
			if ("if".equals(methodName) || "while".equals(methodName) 
					|| "return".equals(methodName) || "switch".equals(methodName)) {
				continue;
			}
			String tail = methodMatcher.group(10).trim();
			if (!tail.isEmpty()) {
				char fs = tail.charAt(0);
				if (fs == ';') {
					if (!modifiers.contains("abstract") && holder.getType() != TYPE.INTERFACE) {
						continue;
					}
				}
				if (!(fs == ';' || fs == '{' || fs == 't')) {
					continue;
				}
			}

			String returnType = recognizeReturnType(methodMatcher);
			if (returnType == null || "return".equals(returnType)) {
				continue;
			}

			ACCESS access = holder.getType() == TYPE.INTERFACE ? ACCESS.PUBLIC : ACCESS.DEF;
			if (modifiers.contains("public")) {
				access = ACCESS.PUBLIC;
			} else if (modifiers.contains("private")) {
				access = ACCESS.PRIVATE;
			} else if (modifiers.contains("protected")) {
				access = ACCESS.PROTECTED;
			}
			List<String> params = new ArrayList<String>(4);
			String paramsGroup = methodMatcher.group(9);
			if (isNotParams(paramsGroup)) {
				continue;
			}
			parseMethodParameters(params, paramsGroup.length() - 1, paramsGroup);
			Collections.reverse(params);
			params = removeFinals(params);

			methods.add(new JavaMethod(methodName, access, params
					, returnType.trim()
					, holder.getName().equals(methodName)
					, methodPosition));
		}
		if (holder != null && !methods.isEmpty()) {
			holder.getChildren().addAll(methods);
		}

		return res;
	}

	private static boolean isNotParams(String paramsGroup) {
		paramsGroup = paramsGroup.trim();
		if (paramsGroup.isEmpty()) {
			return false;
		}
		if (paramsGroup.indexOf('<') != -1) {
			return false;
		}
		int c = paramsGroup.indexOf(',');
		if (c != -1) {			
			paramsGroup = paramsGroup.substring(0, c).trim();
		}
		return paramsGroup.indexOf(' ') == -1;
	}

	private static IJavaItem getHolder(List<IJavaItem> res, IJavaItem previousHolder, int methodPosition, String content) {
		if (res.size() == 0 || previousHolder == null) {
			return res.get(0);
		}
		for (int i = 0; i < res.size(); ++i) {
			if (i == res.size() - 1) {
				return res.get(i);
			}
			if (methodPosition > res.get(i).getPosition() && methodPosition < res.get(i + 1).getPosition()) {
				return res.get(i);
			}
		}
		throw new RuntimeException("Unexpected end of method. No holder was found");
	}

	private static String recognizeReturnType(Matcher methodMatcher) {
		String returnType = methodMatcher.group(3);
		if (returnType == null) {
			returnType = methodMatcher.group(5).trim();
		} else {
			returnType += methodMatcher.group(5);
		}
		if ("new".equals(returnType)) {
			return null;
		}
		int dt = returnType.lastIndexOf('.');
		if (dt != -1 && returnType.lastIndexOf('<') == -1) {
			returnType = returnType.substring(dt + 1, returnType.length());
		}
		for (String key : KEY_WORDS) {
			if (returnType.contains(key)) {
				returnType = returnType.replaceFirst(key, "");
			}
		}
		while (returnType.indexOf("  ") != -1) {
			returnType = returnType.replaceAll("  ", " ");
		}
		return returnType;
	}

	private static List<String> removeFinals(List<String> params) {
		List<String> ret = new ArrayList<String>(params.size());
		for (String p : params) {
			ret.add(p.replaceAll("[^A-Za-z]*final[^A-Za-z]*", ""));
		}
		return ret;
	}

	private static void parseMethodParameters(List<String> res, int endIdx, String group) {
		if (endIdx < 0) {
			return;
		}
		int c = group.lastIndexOf(',', endIdx);
		int d = group.lastIndexOf('.', endIdx);
		if (c == -1 && d == -1) {
			int s = group.lastIndexOf(' ', endIdx);
			res.add(group.substring(0, s).trim());				
		} if (c != -1) {
			int rq = group.lastIndexOf('>', endIdx);
			if (rq > c) {
				int lq = group.lastIndexOf('<', rq);
				c = group.lastIndexOf(',', lq);
				if (c == -1) {
					res.add(group.substring(0, endIdx).trim());
				} else {
					int s = group.lastIndexOf(' ', endIdx);
					res.add(group.substring(c + 1, s).trim());
					parseMethodParameters(res, c - 1, group);
				}
			} else {
				int s = group.lastIndexOf(' ', endIdx);
				res.add(group.substring(c + 1, s).trim());
				parseMethodParameters(res, c - 1, group);
			}
		} else if (c < d) {
			String strParam = group.substring(c + 1, group.indexOf(' ', c)).trim();
			if (strParam.indexOf('.') == -1) {
				strParam += "...";
			}
			res.add(strParam);
			parseMethodParameters(res, c - 1, group);
		}
	}

}
