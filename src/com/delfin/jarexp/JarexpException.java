package com.delfin.jarexp;

public class JarexpException extends RuntimeException {

	private static final long serialVersionUID = 3455357528854519419L;

	public JarexpException(String message) {
		super(message);
	}

	public JarexpException(String message, Throwable cause) {
		super(message, cause);
	}

	public JarexpException(Throwable e) {
		super(e);
	}

}
