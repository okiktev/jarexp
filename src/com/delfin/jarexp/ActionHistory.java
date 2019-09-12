package com.delfin.jarexp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.delfin.jarexp.settings.Settings;

public class ActionHistory {

	private static abstract class CommaSeparatedParcer<T> {
		private Collection<T> collection;

		CommaSeparatedParcer(Collection<T> collection) {
			this.collection = collection;
		}

		void parse(String tokens) {
			if (tokens == null) {
				return;
			}
			int i = 0;
			for (String token : tokens.split(",")) {
				if (i == Settings.HISTORY_BUFFER_SIZE) {
					break;
				}
				++i;
				collection.add(doParse(token));
			}
		}

		String convert() {
			StringBuilder out = new StringBuilder();
			for (Iterator<T> it = collection.iterator(); it.hasNext();) {
				out.append(doConvert(it.next()));
				if (it.hasNext()) {
					out.append(',');
				}
			}
			return out.toString();
		}

		abstract String doConvert(T next);

		abstract T doParse(String token);

	}

	private static enum Key {
		LAST_DIRS_SEL, SEARCH
	};

	private static final Map<Key, Collection<Object>> HISTORY = new HashMap<Key, Collection<Object>>();

	private static CommaSeparatedParcer<String> searchParcer = new CommaSeparatedParcer<String>(getList(Key.SEARCH)) {
		@Override
		String doConvert(String token) {
			return token;
		}

		@Override
		String doParse(String token) {
			return token;
		}
	};

	public static List<File> getLastDirSelected() {
		return getParameterizedAndRevertedList(Key.LAST_DIRS_SEL);
	}

	public static void addLastDirSelected(File dir) {
		addToHistory(dir, Key.LAST_DIRS_SEL);
	}

	public static void addSearch(String token) {
		addToHistory(token, Key.SEARCH);
	}

	public static String getSearchHistory() {
		return searchParcer.convert();
	}

	public static List<String> getSearchTokens() {
		return getParameterizedAndRevertedList(Key.SEARCH);
	}

	public static void loadSearchHistory(String tokens) {
		searchParcer.parse(tokens);
	}

	private static void addToHistory(Object value, Key key) {
		Collection<Object> collection = getList(key);
		for (Iterator<Object> it = collection.iterator();it.hasNext();) {
			if (value.equals(it.next())) {
				it.remove();
			}
		}
		collection.add(value);
		if (collection.size() == Settings.HISTORY_BUFFER_SIZE + 1) {
			for (Iterator<Object> it = collection.iterator(); it.hasNext();) {
				it.next();
				it.remove();
				break;
			}
		}
	}

	private static Collection getList(Key key) {
		Collection<Object> res = HISTORY.get(key);
		if (res == null) {
			switch (key) {
			case LAST_DIRS_SEL:
			case SEARCH:
				res = new LinkedHashSet<Object>();
				HISTORY.put(key, res);
			default:
				break;
			}
		}
		return res;
	}

	private static <T> List<T> getParameterizedAndRevertedList(Key key) {
		Collection<Object> collection = getList(key);
		int size = collection.size();
		List<T> res = new ArrayList<T>(size);
		if (size > 1) {
			for (int i = 0; i < size; i++) {
				res.add(null);
			}
		}
		int i = size - 1;
		for (Iterator<Object> it = collection.iterator(); it.hasNext();) {
			if (size > 1) {
				res.remove(i);
			}
			res.add(i, (T) it.next());
			i--;
		}
		return res;
	}

}
