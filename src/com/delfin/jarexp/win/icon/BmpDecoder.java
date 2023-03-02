package com.delfin.jarexp.win.icon;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

class BmpDecoder {

	private static class ColorEntry {

		int bRed;

		int bGreen;

		int bBlue;

		ColorEntry(LittleEndianInputStream in) throws IOException {
			bBlue = in.readUnsignedByte();
			bGreen = in.readUnsignedByte();
			bRed = in.readUnsignedByte();
			in.readUnsignedByte();
		}

		ColorEntry(int r, int g, int b, int a) {
			bBlue = b;
			bGreen = g;
			bRed = r;
		}

	}

	private static final ColorEntry[] andColorTable =  { new ColorEntry(255, 255, 255, 255), new ColorEntry(0, 0, 0, 0) };

	static BufferedImage read(InfoHeader header, LittleEndianInputStream lis) throws IOException {
		return read(header, lis, header.sBitCount <= 8 ? readColorTable(header, lis) : null);
	}

	static BufferedImage readWithTable(InfoHeader header, LittleEndianInputStream lis) throws IOException {
		return read(header, lis, andColorTable);
	}

	private static BufferedImage read(InfoHeader header, LittleEndianInputStream lis, ColorEntry[] colorTable)
	        throws IOException {
		if (header.iCompression != 0) {
			throw new IOException("Unrecognized bitmap format: bit count=" + header.sBitCount + ", compression=" + header.iCompression);
		}
		switch (header.sBitCount) {
		case 1:
			return read1(header, lis, colorTable);
		case 4:
			return read4(header, lis, colorTable);
		case 8:
			return read8(header, lis, colorTable);
		case 24:
			return read24(header, lis);
		case 32:
			return read32(header, lis);
		default:
			throw new IOException("Unrecognized bitmap format: bit count=" + header.sBitCount + ", compression=" + header.iCompression);
		}
	}

	private static int getBit(int bits, int index) {
		return (bits >> (7 - index)) & 1;
	}

	private static int getNibble(int nibbles, int index) {
		return (nibbles >> (4 * (1 - index))) & 0xF;
	}

	private static void getColorTable(ColorEntry[] colorTable, byte[] ar, byte[] ag, byte[] ab) {
		for (int i = 0; i < colorTable.length; i++) {
			ar[i] = (byte) colorTable[i].bRed;
			ag[i] = (byte) colorTable[i].bGreen;
			ab[i] = (byte) colorTable[i].bBlue;
		}
	}

	private static ColorEntry[] readColorTable(InfoHeader infoHeader, LittleEndianInputStream lis) throws IOException {
		ColorEntry[] colorTable = new ColorEntry[infoHeader.iNumColors];
		for (int i = 0; i < infoHeader.iNumColors; i++) {
			colorTable[i] = new ColorEntry(lis);
		}
		return colorTable;
	}

	private static BufferedImage read1(InfoHeader infoHeader, LittleEndianInputStream lis, ColorEntry[] colorTable)
	        throws IOException {
		byte[] ar = new byte[colorTable.length];
		byte[] ag = new byte[colorTable.length];
		byte[] ab = new byte[colorTable.length];
		getColorTable(colorTable, ar, ag, ab);
		IndexColorModel icm = new IndexColorModel(1, 2, ar, ag, ab);
		BufferedImage img = new BufferedImage(infoHeader.iWidth, infoHeader.iHeight, BufferedImage.TYPE_BYTE_BINARY, icm);
		WritableRaster raster = img.getRaster();
		int dataBitsPerLine = infoHeader.iWidth;
		int bitsPerLine = dataBitsPerLine;
		if (bitsPerLine % 32 != 0) {
			bitsPerLine = (bitsPerLine / 32 + 1) * 32;
		}
		int bytesPerLine = (int) (bitsPerLine / 8);
		int[] line = new int[bytesPerLine];
		for (int y = infoHeader.iHeight - 1; y >= 0; y--) {
			for (int i = 0; i < bytesPerLine; i++) {
				line[i] = lis.readUnsignedByte();
			}
			for (int x = 0; x < infoHeader.iWidth; x++) {
				int i = x / 8;
				raster.setSample(x, y, 0, getBit(line[i], x % 8));
			}
		}
		return img;
	}

	private static BufferedImage read4(InfoHeader infoHeader, LittleEndianInputStream lis, ColorEntry[] colorTable)
	        throws IOException {
		byte[] ar = new byte[colorTable.length];
		byte[] ag = new byte[colorTable.length];
		byte[] ab = new byte[colorTable.length];
		getColorTable(colorTable, ar, ag, ab);
		IndexColorModel icm = new IndexColorModel(4, infoHeader.iNumColors, ar, ag, ab);
		BufferedImage img = new BufferedImage(infoHeader.iWidth, infoHeader.iHeight, BufferedImage.TYPE_BYTE_BINARY, icm);
		WritableRaster raster = img.getRaster();
		int bitsPerLine = infoHeader.iWidth * 4;
		if (bitsPerLine % 32 != 0) {
			bitsPerLine = (bitsPerLine / 32 + 1) * 32;
		}
		int bytesPerLine = (int) (bitsPerLine / 8);
		int[] line = new int[bytesPerLine];
		for (int y = infoHeader.iHeight - 1; y >= 0; y--) {
			for (int i = 0; i < bytesPerLine; i++) {
				int b = lis.readUnsignedByte();
				line[i] = b;
			}
			for (int x = 0; x < infoHeader.iWidth; x++) {
				int b = x / 2;
				raster.setSample(x, y, 0, getNibble(line[b], x % 2));
			}
		}
		return img;
	}

	private static BufferedImage read8(InfoHeader infoHeader, LittleEndianInputStream lis, ColorEntry[] colorTable)
	        throws IOException {
		byte[] ar = new byte[colorTable.length];
		byte[] ag = new byte[colorTable.length];
		byte[] ab = new byte[colorTable.length];
		getColorTable(colorTable, ar, ag, ab);
		IndexColorModel icm = new IndexColorModel(8, infoHeader.iNumColors, ar, ag, ab);
		BufferedImage img = new BufferedImage(infoHeader.iWidth, infoHeader.iHeight, BufferedImage.TYPE_BYTE_INDEXED, icm);
		WritableRaster raster = img.getRaster();
		int dataPerLine = infoHeader.iWidth;
		int bytesPerLine = dataPerLine;
		if (bytesPerLine % 4 != 0) {
			bytesPerLine = (bytesPerLine / 4 + 1) * 4;
		}
		int padBytesPerLine = bytesPerLine - dataPerLine;
		for (int y = infoHeader.iHeight - 1; y >= 0; y--) {
			for (int x = 0; x < infoHeader.iWidth; x++) {
				int b = lis.readUnsignedByte();
				raster.setSample(x, y, 0, b);
			}
			lis.skip(padBytesPerLine);
		}
		return img;
	}

	private static BufferedImage read24(InfoHeader infoHeader, LittleEndianInputStream lis) throws IOException {
		BufferedImage img = new BufferedImage(infoHeader.iWidth, infoHeader.iHeight, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = img.getRaster();
		int dataPerLine = infoHeader.iWidth * 3;
		int bytesPerLine = dataPerLine;
		if (bytesPerLine % 4 != 0) {
			bytesPerLine = (bytesPerLine / 4 + 1) * 4;
		}
		int padBytesPerLine = bytesPerLine - dataPerLine;
		for (int y = infoHeader.iHeight - 1; y >= 0; y--) {
			for (int x = 0; x < infoHeader.iWidth; x++) {
				int b = lis.readUnsignedByte();
				int g = lis.readUnsignedByte();
				int r = lis.readUnsignedByte();
				raster.setSample(x, y, 0, r);
				raster.setSample(x, y, 1, g);
				raster.setSample(x, y, 2, b);
			}
			lis.skip(padBytesPerLine);
		}
		return img;
	}

	private static BufferedImage read32(InfoHeader infoHeader, LittleEndianInputStream lis) throws IOException {
		BufferedImage img = new BufferedImage(infoHeader.iWidth, infoHeader.iHeight, BufferedImage.TYPE_INT_ARGB);
		WritableRaster rgb = img.getRaster();
		WritableRaster alpha = img.getAlphaRaster();
		for (int y = infoHeader.iHeight - 1; y >= 0; y--) {
			for (int x = 0; x < infoHeader.iWidth; x++) {
				int b = lis.readUnsignedByte();
				int g = lis.readUnsignedByte();
				int r = lis.readUnsignedByte();
				int a = lis.readUnsignedByte();
				rgb.setSample(x, y, 0, r);
				rgb.setSample(x, y, 1, g);
				rgb.setSample(x, y, 2, b);
				alpha.setSample(x, y, 0, a);
			}
		}
		return img;
	}

}
