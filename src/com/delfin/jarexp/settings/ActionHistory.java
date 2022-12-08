package com.delfin.jarexp.settings;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionHistory {

	private static final Logger log = Logger.getLogger(ActionHistory.class.getName());

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
		LAST_DIRS_SEL, SEARCH, LAST_UPDATE_CHECK, NEW_VERSION, DONATE_URL
	};

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMMM-yyyy");

	private static final Map<Key, Collection<Object>> HISTORY = new HashMap<Key, Collection<Object>>();

	@SuppressWarnings("unchecked")
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

	public static String getLastUpdateCheck() {
		return DATE_FORMAT.format(getLastUpdateCheckDate());
	}

	public static Date getLastUpdateCheckDate() {
		Collection<Object> value = HISTORY.get(Key.LAST_UPDATE_CHECK);
		if (value == null || value.isEmpty()) {
			return new Date(0L);
		}
		Date res = (Date) value.iterator().next();
		if (res == null) {
			return new Date(0L);
		}
		return res;
	}

	public static void loadLastUpdateCheck(String date) {
		if (date == null || date.isEmpty()) {
			return;
		}
		try {
			loadLastUpdateCheck(DATE_FORMAT.parse(date));
		} catch (ParseException e) {
			log.log(Level.SEVERE, "Unable to parse date " + date, e);
		}
	}

	public static void loadLastUpdateCheck(Date date) {
		List<Object> value = new ArrayList<Object>(1);
		value.add(date);
		HISTORY.put(Key.LAST_UPDATE_CHECK, value);
	}

	public static String getNewVersion() {
		Collection<Object> value = HISTORY.get(Key.NEW_VERSION);
		if (value == null || value.isEmpty()) {
			return null;
		}
		return (String) value.iterator().next();
	}

	public static void loadNewVersion(String version) {
		if (version == null || version.isEmpty()) {
			return;
		}
		List<Object> value = new ArrayList<Object>(1);
		value.add(version);
		HISTORY.put(Key.NEW_VERSION, value);
	}

	public static void loadDonateUrl(String donateUrl) {
		if (donateUrl == null || donateUrl.isEmpty()) {
			return;
		}
		List<Object> value = new ArrayList<Object>(1);
		value.add(donateUrl);
		HISTORY.put(Key.DONATE_URL, value);
	}

	public static String getDonateUrl() {
		Collection<Object> value = HISTORY.get(Key.DONATE_URL);
		if (value == null || value.isEmpty()) {
			return null;
		}
		return (String) value.iterator().next();
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("rawtypes")
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
			if (size > 1) {
				res.remove(i);
			}
			res.add(i, (T) it.next());
			i--;
		}
		return res;
	}

	public static void main(String[] args) throws ParseException {
		String d = "14-Nov-2021";
		DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMMM-yyyy");
		System.out.println(DATE_FORMAT.format(new Date()));
		System.out.println(DATE_FORMAT.parse(d));
		
	}

}
