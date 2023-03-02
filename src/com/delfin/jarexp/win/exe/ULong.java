package com.delfin.jarexp.win.exe;

import java.math.BigInteger;


class ULong extends UNumber {

	private static final long serialVersionUID = 3708246220322595671L;

	private static final BigInteger MAX_VALUE_LONG = new BigInteger("9223372036854775808");

	private final long value;

	private ULong(long value) {
		this.value = value;
	}

	static ULong valueOf(long value) {
		return new ULong(value);
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
		if (value < 0L) {
			return (value & Long.MAX_VALUE) + 9.223372E18f;
		}
		return (float) value;
	}

	@Override
	public double doubleValue() {
		if (value < 0L) {
			return (value & Long.MAX_VALUE) + 9.223372036854776E18;
		}
		return (double) value;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(value).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ULong && value == ((ULong) obj).value;
	}

	@Override
	public String toString() {
		if (value >= 0L) {
			return Long.toString(value);
		}
		return BigInteger.valueOf(value & Long.MAX_VALUE).add(MAX_VALUE_LONG).toString();
	}

	@Override
	String toHexString() {
		return Long.toHexString(value);
	}

	@Override
	BigInteger toBigInteger() {
		return new BigInteger(toString());
	}

}
