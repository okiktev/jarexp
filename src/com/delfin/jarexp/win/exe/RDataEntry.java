package com.delfin.jarexp.win.exe;


class RDataEntry extends HHeader {

    private final TDword offsetToData;
    private final TDword size;

    private final HSectionTableEntry section;

    RDataEntry(UByteArray bytes, HSectionTableEntry section) {
        this.section = section;
        this.offsetToData = new TDword(bytes.readUInt(4), "offsetToData");
        this.size = 		new TDword(bytes.readUInt(4), "Size");
        // just do reading to move bytes iterator
        new TDword(bytes.readUInt(4), "CodePage");
        new TDword(bytes.readUInt(4), "Reserved");
    }

    byte[] getData(UByteArray bytes) {
        long dataOffset = section.pointerToRawData.get().longValue() 
        		+ offsetToData.get().longValue() 
        		- section.virtualAddress.get().longValue();

        if (dataOffset > Integer.MAX_VALUE) {
            throw new RuntimeException("Unable to set offset to more than 2Gb.");
        }

        int saved = bytes.position();
        bytes.seek((int) dataOffset);

        long bytesToCopyLong = size.get().longValue();
        if (bytesToCopyLong > Integer.MAX_VALUE) {
            throw new RuntimeException("Unable to copy more than 2Gb.");
        }

        byte[] copyBytes = bytes.copyBytes((int) bytesToCopyLong);
        bytes.seek(saved);
        return copyBytes;
    }

}
