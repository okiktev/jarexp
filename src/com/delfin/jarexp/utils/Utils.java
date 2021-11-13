package com.delfin.jarexp.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpException;

public class Utils {

	private static final Logger log = Logger.getLogger(Utils.class.getCanonicalName());

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Sleep was unexpectedly interrupted", e);
			Thread.currentThread().interrupt();
			throw new JarexpException(e);
		}
	}

}
