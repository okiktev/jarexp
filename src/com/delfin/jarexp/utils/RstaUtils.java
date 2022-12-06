package com.delfin.jarexp.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

public class RstaUtils {

	private static final Logger log = Logger.getLogger(RstaUtils.class.getCanonicalName());

	private static Theme theme;
	static {
		try {
			theme = Theme.load(RstaUtils.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to apply eclipse theme", e);
		}
	}

	public static void applyTheme(RSyntaxTextArea textArea) {
		theme.apply(textArea);
	}

	public static String getSyntax(String lowPath) {
		String syntax = SyntaxConstants.SYNTAX_STYLE_NONE;
		if (lowPath.endsWith(".html") || lowPath.endsWith(".htm")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_HTML;
		} else if (lowPath.endsWith(".xml") || lowPath.endsWith(".tld") || lowPath.endsWith(".pom")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_XML;
		} else if (lowPath.endsWith(".properties")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
		} else if (lowPath.endsWith(".dtd")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_DTD;
		} else if (lowPath.endsWith(".css")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_CSS;
		} else if (lowPath.endsWith(".jsp")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_JSP;
		} else if (lowPath.endsWith(".js")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		} else if (lowPath.endsWith(".bat") || lowPath.endsWith(".cmd")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
		} else if (lowPath.endsWith(".sh")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
		} else if (lowPath.endsWith(".java")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_JAVA;
		} else if (lowPath.endsWith(".groovy")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_GROOVY;
		} else if (lowPath.endsWith(".json")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
		} else if (lowPath.endsWith(".yaml")) {
			syntax = SyntaxConstants.SYNTAX_STYLE_YAML;
		}
		return syntax;
	}

}
