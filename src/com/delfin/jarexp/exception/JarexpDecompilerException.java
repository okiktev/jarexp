package com.delfin.jarexp.exception;

public class JarexpDecompilerException extends JarexpException {

	private static final long serialVersionUID = -1451230522562809059L;

	public JarexpDecompilerException(String message) {
		super(message);
	}

	public JarexpDecompilerException(String message, Throwable cause) {
		super(message, cause);
	}

	public JarexpDecompilerException(Throwable e) {
		super(e);
	}

}
