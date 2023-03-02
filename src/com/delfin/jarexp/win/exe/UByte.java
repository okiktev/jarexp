package com.delfin.jarexp.win.exe;

import java.math.BigInteger;


class UByte extends UNumber {

	private static final long serialVersionUID = -7919692259820991759L;

	private static final UByte[] VALUES = mkValues();

	private final short value;

	private UByte(byte value) {
		this.value = (short) (value & 0xFF);
	}

	private static UByte[] mkValues() {
		UByte[] ret = new UByte[256];
		for (int i = -128; i <= 127; ++i) {
			ret[i & 0xFF] = new UByte((byte) i);
		}
		return ret;
	}

	public static UByte valueOf(byte value) {
		return valueOfUnchecked((short) (value & 0xFF));
	}

	private static UByte valueOfUnchecked(short value) throws NumberFormatException {
		return VALUES[value & 0xFF];
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
		return value;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Short.valueOf(value).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof UByte && value == ((UByte) obj).value);
	}

	@Override
	public String toString() {
		return Short.valueOf(value).toString();
	}

	@Override
	String toHexString() {
		return Integer.toHexString((int) value);
	}

	@Override
	BigInteger toBigInteger() {
		return BigInteger.valueOf((long) value);
	}

}
