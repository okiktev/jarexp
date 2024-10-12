package com.delfin.jarexp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.exception.JarexpException;

public abstract class StreamHandler {

	private static final Logger log = Logger.getLogger(StreamHandler.class.getCanonicalName());

	public StreamHandler() {
		InputStream stream = null;
		try {
			stream = stream();
			doAction(stream);
		} catch (Exception e) {
			throw new JarexpException(errorMsg(), e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.log(Level.WARNING, warnMsg(), e);
				}
			}
		}
	}

	protected abstract InputStream stream() throws IOException;

	protected abstract void doAction(InputStream stream) throws IOException;

	protected abstract String errorMsg();

	protected abstract String warnMsg();

}
