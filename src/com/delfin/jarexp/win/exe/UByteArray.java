package com.delfin.jarexp.win.exe;

import java.io.ByteArrayInputStream;


class UByteArray extends ByteArrayInputStream {

	UByteArray(byte[] bytes) {
		super(bytes);
	}

	String readAsciiString(int length) {
		return new String(copyBytes(length), PE.US_ASCII).trim();
	}

	ULong readULong(int length) {
		pos += length;
		return ULittleEndian.ULong_.from(buf, pos - length, length);
	}

	UInteger readUInt(int length) {
		pos += length;
		return ULittleEndian.UInt_.from(buf, pos - length, length);
	}

	UShort readUShort(int length) {
		pos += length;
		return ULittleEndian.UShort_.from(buf, pos - length, length);
	}

	UByte readUByte() {
		pos++;
		return UByte.valueOf(buf[pos - 1]);
	}

	byte readRaw(int offset) {
		return buf[pos + offset];
	}

	byte[] copyBytes(int length) {
		byte[] data = new byte[length];
		super.read(data, 0, length);
		return data;
	}

	void mark() {
		super.mark(0);
	}

	void seek(int position) {
		pos = position;
	}

	int position() {
		return pos;
	}

	int marked() {
		return mark;
	}

}
