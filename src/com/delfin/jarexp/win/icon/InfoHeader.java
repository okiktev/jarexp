
package com.delfin.jarexp.win.icon;

import java.io.IOException;

class InfoHeader {

	int iSize;

	int iWidth;

	int iHeight;

	short sBitCount;

	int iCompression;

	int iNumColors;

	InfoHeader(LittleEndianInputStream in, int infoSize) throws IOException {
		this.iSize = infoSize;
		iWidth = in.readIntLE();
		iHeight = in.readIntLE();
		in.readShortLE();
		sBitCount = in.readShortLE();
		iNumColors = (int) Math.pow(2, sBitCount);
		iCompression = in.readIntLE();
		in.readIntLE();
		in.readIntLE();
		in.readIntLE();
		in.readIntLE();
		in.readIntLE();
	}

	InfoHeader(InfoHeader source) {
		iCompression = source.iCompression;
		iHeight = source.iHeight;
		iWidth = source.iWidth;
		iNumColors = source.iNumColors;
		iSize = source.iSize;
		sBitCount = source.sBitCount;
	}

}
