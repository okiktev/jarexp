package com.delfin.jarexp.win.icon;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class LittleEndianInputStream extends DataInputStream implements DataInput {

	private static class CountingInputStream extends FilterInputStream {

		private int count;

		CountingInputStream(InputStream src) {
			super(src);
		}

		int getCount() {
			return count;
		}

		@Override
		public int read() throws IOException {
			int b = super.read();
			if (b != -1) {
				count++;
			}
			return b;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int r = super.read(b, off, len);
			if (r > 0) {
				count += r;
			}
			return r;
		}
	}

	LittleEndianInputStream(InputStream in) {
		super(new CountingInputStream(in));
	}

	int getCount() {
		return ((CountingInputStream) in).getCount();
	}

	int skip(int count, boolean strict) throws IOException {
		int skipped = 0;
		while (skipped < count) {
			int b = in.read();
			if (b == -1) {
				break;
			}
			skipped++;
		}
		if (skipped < count && strict) {
			throw new EOFException("Failed to skip " + count + " bytes in input");
		}
		return skipped;
	}

	short readShortLE() throws IOException {
		int b1 = read();
		int b2 = read();
		if (b1 < 0 || b2 < 0) {
			throw new EOFException();
		}
		return (short) ((b2 << 8) + (b1 << 0));
	}

	int readIntLE() throws IOException {
		int b1 = read();
		int b2 = read();
		int b3 = read();
		int b4 = read();
		if (b1 < -1 || b2 < -1 || b3 < -1 || b4 < -1) {
			throw new EOFException();
		}
		return (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
	}

	long readLongLE() throws IOException {
		int i1 = readIntLE();
		int i2 = readIntLE();
		return  ((long) (i1) << 32) + (i2 & 0xFFFFFFFFL);
	}

}
