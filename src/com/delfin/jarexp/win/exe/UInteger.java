package com.delfin.jarexp.win.exe;

import java.math.BigInteger;


class UInteger extends UNumber {

	private static final long serialVersionUID = 8672874700386858946L;

	private static final Class<UInteger> CLASS = UInteger.class;
	private static final String CLASS_NAME = CLASS.getName();
	private static final String PRECACHE_PROPERTY = CLASS_NAME + ".precacheSize";
	private static final UInteger[] VALUES = mkValues();

	private final long value;

	private static final int getPrecacheSize() {
		String prop = null;
		try {
			prop = System.getProperty(PRECACHE_PROPERTY);
		} catch (SecurityException e) {
			return 256;
		}
		if (prop == null || prop.length() <= 0) {
			return 256;
		}
		long propParsed;
		try {
			propParsed = Long.parseLong(prop);
		} catch (NumberFormatException e2) {
			return 256;
		}
		if (propParsed < 0L) {
			return 0;
		}
		if (propParsed > 2147483647L) {
			return Integer.MAX_VALUE;
		}
		return (int) propParsed;
	}

	private static UInteger[] mkValues() {
		int precacheSize = getPrecacheSize();
		if (precacheSize <= 0) {
			return null;
		}
		UInteger[] ret = new UInteger[precacheSize];
		for (int i = 0; i < precacheSize; ++i) {
			ret[i] = new UInteger(i);
		}
		return ret;
	}

	private UInteger(long value, boolean unused) {
		this.value = value;
	}

	private UInteger(int value) {
		this.value = ((long) value & 0xFFFFFFFFL);
	}

	private static UInteger getCached(long value) {
		if (VALUES != null && value < VALUES.length) {
			return VALUES[(int) value];
		}
		return null;
	}

	private static UInteger valueOfUnchecked(long value) {
		UInteger cached = getCached(value);
		return cached == null ? new UInteger(value, true) : cached;
	}

	static UInteger valueOf(int value) {
		return valueOfUnchecked((long) value & 0xFFFFFFFFL);
	}

	@Override
	public int intValue() {
		return (int) value;
	}

	@Override
	public long longValue() {
		return value;
	}

	@Override
	public float floatValue() {
		return (float) value;
	}

	@Override
	public double doubleValue() {
		return (double) value;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(value).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj instanceof UInteger && value == ((UInteger) obj).value);
	}

	@Override
	public String toString() {
		return Long.valueOf(value).toString();
	}

	@Override
	String toHexString() {
		return Long.toHexString(value);
	}

	@Override
	BigInteger toBigInteger() {
		return BigInteger.valueOf(value);
	}

}
