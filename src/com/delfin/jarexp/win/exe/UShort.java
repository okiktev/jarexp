package com.delfin.jarexp.win.exe;

import java.math.BigInteger;


class UShort extends UNumber {

	private static final long serialVersionUID = -8755300420312352959L;

	private final int value;

	private UShort(short value) {
		this.value = (value & 0xFFFF);
	}

	static UShort valueOf(short value) {
		return new UShort(value);
	}

	@Override
	public int intValue() {
		return value;
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
		return value;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(value).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UShort && value == ((UShort) obj).value;
	}

	@Override
	public String toString() {
		return Integer.valueOf(value).toString();
	}

	@Override
	String toHexString() {
		return Integer.toHexString(value);
	}

	@Override
	BigInteger toBigInteger() {
		return BigInteger.valueOf((long) value);
	}

}
