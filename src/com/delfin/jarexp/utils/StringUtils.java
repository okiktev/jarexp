package com.delfin.jarexp.utils;

public final class StringUtils {

	public static int indexOf(String text, String token) {
		return indexOf(text, token, 0);
	}

	public static int indexOf(String text, String token, int offset) {
		for (int i = offset; i < text.length(); ++i) {
			if (seek(i, token, text)) {
				return i;
			}
		}
		return -1;
	}

	public static int lastIndexOf(String text, String token) {
		return lastIndexOf(text, token, text.length() - 1);
	}

	public static int lastIndexOf(String text, String token, int offset) {
		for (int i = offset; i >= 0; --i) {
			if (seek(i, token, text)) {
				return i;
			}
		}
		return -1;
	}

	private static boolean seek(int i, String token, String text) {
		int j = 0;
		int tokenSize = token.length();
		int textSize = text.length();
		while (j < tokenSize && i < textSize) {
			char ch = text.charAt(i);
			char cht = token.charAt(j);
			if (charEquals(ch, cht)) {
				if (j == tokenSize - 1) {
					return true;
				}
			} else {
				break;
			}
			++i;
			++j;
		}
		return false;
	}

	private static boolean charEquals(char ch, char cht) {
		if (Character.isLetter(ch) && Character.isLetter(cht)) {
			boolean isFirstUpper = Character.isUpperCase(ch);
			if (isFirstUpper && Character.isUpperCase(cht)) {
				return ch == cht;
			} else if (isFirstUpper) {
				return ch == Character.toUpperCase(cht);
			} else {
				return ch == Character.toLowerCase(cht);
			}
		} else {
			return ch == cht;
		}
	}

}
