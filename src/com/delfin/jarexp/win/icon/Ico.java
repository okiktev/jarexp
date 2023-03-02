
package com.delfin.jarexp.win.icon;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

public class Ico {

	private static final Logger log = Logger.getLogger(Ico.class.getName());

	private static class IconData {

		int iSizeInBytes;

		int iFileOffset;

		IconData(LittleEndianInputStream in) throws IOException {
			in.readUnsignedByte();
			in.readUnsignedByte();
			in.readUnsignedByte();
			in.readByte();
			in.readShortLE();
			in.readShortLE();
			iSizeInBytes = in.readIntLE();
			iFileOffset = in.readIntLE();
		}

	}

	private static final int PNG_MAGIC = 0x89504E47;
	private static final int PNG_MAGIC_LE = 0x474E5089;
	private static final int PNG_MAGIC2 = 0x0D0A1A0A;
	private static final int PNG_MAGIC2_LE = 0x0A1A0A0D;

	private Ico() {
	}

	public static List<BufferedImage> read(File file) throws IOException {
		InputStream fin = new FileInputStream(file);
		try {
			return read(new BufferedInputStream(fin));
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Failed to close file input for file " + file, e);
			}
		}
	}

	public static List<BufferedImage> read(InputStream is) throws IOException {
		return readExt(is);
	}

	private static List<BufferedImage> readExt(InputStream is) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(is);
		in.readShortLE();
		in.readShortLE();
		short sCount = in.readShortLE();
		IconData[] entries = new IconData[sCount];
		for (short s = 0; s < sCount; s++) {
			entries[s] = new IconData(in);
		}

		int i = 0;
		List<BufferedImage> ret = new ArrayList<BufferedImage>(sCount);
		for (i = 0; i < sCount; i++) {
			int fileOffset = in.getCount();
			if (fileOffset != entries[i].iFileOffset) {
				throw new IOException("Cannot read image #" + i + " starting at unexpected file offset.");
			}
			int info = in.readIntLE();
			if (info == 40) {
				InfoHeader infoHeader = new InfoHeader(in, info);
				InfoHeader andHeader = new InfoHeader(infoHeader);
				andHeader.iHeight = (int) (infoHeader.iHeight / 2);
				InfoHeader xorHeader = new InfoHeader(infoHeader);
				xorHeader.iHeight = andHeader.iHeight;
				andHeader.sBitCount = 1;
				andHeader.iNumColors = 2;
				BufferedImage xor = BmpDecoder.read(xorHeader, in);
				BufferedImage img = new BufferedImage(xorHeader.iWidth, xorHeader.iHeight, BufferedImage.TYPE_INT_ARGB);
				if (infoHeader.sBitCount == 32) {
					int size = entries[i].iSizeInBytes;
					int infoHeaderSize = infoHeader.iSize;
					int dataSize = xorHeader.iWidth * xorHeader.iHeight * 4;
					int skip = size - infoHeaderSize - dataSize;
					if (in.skip(skip, false) < skip && i < sCount - 1) {
						throw new EOFException("Unexpected end of input");
					}
					WritableRaster srgb = xor.getRaster();
					WritableRaster salpha = xor.getAlphaRaster();
					WritableRaster rgb = img.getRaster();
					WritableRaster alpha = img.getAlphaRaster();
					for (int y = xorHeader.iHeight - 1; y >= 0; y--) {
						for (int x = 0; x < xorHeader.iWidth; x++) {
							int r = srgb.getSample(x, y, 0);
							int g = srgb.getSample(x, y, 1);
							int b = srgb.getSample(x, y, 2);
							int a = salpha.getSample(x, y, 0);
							rgb.setSample(x, y, 0, r);
							rgb.setSample(x, y, 1, g);
							rgb.setSample(x, y, 2, b);
							alpha.setSample(x, y, 0, a);
						}
					}
				} else {
					BufferedImage and = BmpDecoder.readWithTable(andHeader, in);
					WritableRaster rgb = img.getRaster();
					WritableRaster alpha = img.getAlphaRaster();
					for (int y = 0; y < xorHeader.iHeight; y++) {
						for (int x = 0; x < xorHeader.iWidth; x++) {
							int c = xor.getRGB(x, y);
							int r = (c >> 16) & 0xFF;
							int g = (c >> 8) & 0xFF;
							int b = (c) & 0xFF;
							rgb.setSample(x, y, 0, r);
							rgb.setSample(x, y, 1, g);
							rgb.setSample(x, y, 2, b);
							alpha.setSample(x, y, 0, and.getRGB(x, y));
						}
					}
				}
				ret.add(img);
			} else if (info == PNG_MAGIC_LE) {
				if (in.readIntLE() != PNG_MAGIC2_LE) {
					throw new IOException("Unrecognized icon format for image #" + i);
				}
				byte[] pngData = new byte[entries[i].iSizeInBytes - 8];
				in.readFully(pngData);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dout = new DataOutputStream(bout);
				dout.writeInt(PNG_MAGIC);
				dout.writeInt(PNG_MAGIC2);
				dout.write(pngData);
				ImageReader reader = getPNGImageReader();
				reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bout.toByteArray())));
				ret.add(reader.read(0));
			} else {
				throw new IOException("Unrecognized icon format for image #" + i);
			}
		}

		return ret;
	}

	private static ImageReader getPNGImageReader() {
		Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("png");
		return it.hasNext() ? it.next() : null;
	}

}
