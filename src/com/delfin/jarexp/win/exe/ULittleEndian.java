package com.delfin.jarexp.win.exe;


class ULittleEndian {

	static final class UShort_ {
		static UShort from(byte[] bytes, int offset, int bytenum) {
			short number = 0;
			switch (bytenum) {
			case 2:
				number |= (short) ((bytes[offset + 1] & 0xFF) << 8);
			case 1:
				number |= (short) ((bytes[offset + 0] & 0xFF) << 0);
				break;
			}
			return UShort.valueOf(number);
		}
	}

	static final class UInt_ {
		static UInteger from(byte[] bytes, int offset, int bytenum) {
			int number = 0;
			switch (bytenum) {
			case 4:
				number |= (bytes[offset + 3] & 0xFF) << 24;
			case 3:
				number |= (bytes[offset + 2] & 0xFF) << 16;
			case 2:
				number |= (bytes[offset + 1] & 0xFF) << 8;
			case 1:
				number |= (bytes[offset + 0] & 0xFF) << 0;
				break;
			}
			return UInteger.valueOf(number);
		}
	}

	static final class ULong_ {
		static ULong from(byte[] bytes, int offset, int bytenum) {
			long number = 0L;
			switch (bytenum) {
			case 8:
				number |= (long) (bytes[offset + 7] & 0xFF) << 56;
			case 7:
				number |= (long) (bytes[offset + 6] & 0xFF) << 48;
			case 6:
				number |= (long) (bytes[offset + 5] & 0xFF) << 40;
			case 5:
				number |= (long) (bytes[offset + 4] & 0xFF) << 32;
			case 4:
				number |= (long) (bytes[offset + 3] & 0xFF) << 24;
			case 3:
				number |= (long) (bytes[offset + 2] & 0xFF) << 16;
			case 2:
				number |= (long) (bytes[offset + 1] & 0xFF) << 8;
			case 1:
				number |= (long) (bytes[offset + 0] & 0xFF) << 0;
				break;
			}
			return ULong.valueOf(number);
		}
	}
}
