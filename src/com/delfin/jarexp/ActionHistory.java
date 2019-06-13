package com.delfin.jarexp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ActionHistory {

	private static enum Key {
		LAST_DIRS_SEL
	};

	private static final Map<Key, Collection<Object>> HISTORY = new HashMap<Key, Collection<Object>>();

	public static List<File> getLastDirSelected() {
		return getParameterizedAndRevertedList(Key.LAST_DIRS_SEL);
	}

	public static void addLastDirSelected(File dir) {
		getList(Key.LAST_DIRS_SEL).add(dir);
	}

	private static Collection<Object> getList(Key key) {
		Collection<Object> res = HISTORY.get(key);
		if (res == null) {
			switch (key) {
			case LAST_DIRS_SEL:
				res = new LinkedHashSet<Object>();
				HISTORY.put(key, res);
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
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
			res.add(i, (T) it.next());
			i--;
		}
		return res;
	}

}
