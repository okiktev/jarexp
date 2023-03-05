package com.delfin.jarexp.win.exe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class Icon {

	private final byte[] header;

	private final String groupName;

	private List<ImageInfo> infos = new ArrayList<ImageInfo>();

	private List<String> imgNames = new ArrayList<String>();

	Icon(String groupName, byte[] groupInfoData) {
		header = Arrays.copyOfRange(groupInfoData, 0, 6);
		this.groupName = decodeToDec(groupName);
		int left = 6;
		int right = 20;
		while (right <= groupInfoData.length) {
			infos.add(new ImageInfo(Arrays.copyOfRange(groupInfoData, left, right)));
			left = right;
			right = left + 14;
		}
	}

	boolean isContainsImage(String name) {
		return imgNames.contains(decodeToDec(name));
	}

	void addImage(String imgName, byte[] imgData) {
		imgName = decodeToDec(imgName);
		for (ImageInfo info : infos) {
			if (info.name.equals(imgName)) {
				info.imgData = imgData;
				return;
			}
		}
	}

	private class ImageInfo {

		private final String name;
		private final byte[] data;
		private byte[] imgData;

		public ImageInfo(byte[] bytes) {
			data = Arrays.copyOfRange(bytes, 0, 12);
			imgNames.add(name = Integer.toString(((bytes[12] & 0xff) | ((bytes[13] & 0xff) << 8))));
		}

	}

	void dumpTo(File destDir, File peFile) throws IOException {
		File outputFile = new File(destDir, peFile.getName() + '_' + groupName + System.currentTimeMillis() + ".ico");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		try {
			outputStream.write(header);
			Collections.sort(infos, new Comparator<ImageInfo>() {
				public int compare(ImageInfo o1, ImageInfo o2) {
					return Integer.valueOf(o1.name).compareTo(Integer.valueOf(o2.name));
				}
			});
			int imgOffset = infos.size() * 16 + 6;
			for (ImageInfo img : infos) {
				if (img.imgData == null) {
					continue;
				}
				outputStream.write(img.data);
				outputStream.write(decode(Integer.toHexString(imgOffset)));
				imgOffset += img.imgData.length;
			}
			for (ImageInfo img : infos) {
				if (img.imgData == null) {
					continue;
				}
				outputStream.write(img.imgData);
			}
		} finally {
			outputStream.close();
		}
	}

	private static byte[] decode(String hexString) {
		byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
		if (byteArray[0] != 0) {
			return align(byteArray);
		}
		byte[] output = new byte[byteArray.length - 1];
		System.arraycopy(byteArray, 1, output, 0, output.length);
		return align(output);
	}

	private static byte[] align(byte[] output) {
		if (output.length >= 4) {
			return output;
		}
		byte[] out = new byte[4];
		for (int i = 0; i < out.length; ++i) {
			if (i < output.length) {
				out[i] = output[output.length - i - 1];
			} else {
				out[i] = (byte) 0;
			}
		}
		return out;
	}

	private static String decodeToDec(String hex) {
		return Integer.toString(Integer.parseInt(hex, 16));
	}

}
