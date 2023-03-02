package com.delfin.jarexp.win.exe;


class RDirEntry extends HHeader {

    static final int HEADER_SIZE = 8;

    private static final int DATA_IS_DIRECTORY_MASK = 0x80000000;
    private static final int ENTRY_OFFSET_MASK = 0x7FFFFFFF;

    final TResDirName name;
    final boolean isDirectory;
    RDirHeader directory;
    RDataEntry resourceDataEntry;

    private final TDword dataOffset;

    RDirEntry(UByteArray bytes, HSectionTableEntry section, int level) {
        name =       addHeader(new TResDirName(bytes.readUInt(4), "name", bytes, level));
        dataOffset = addHeader(new TDword(bytes.readUInt(4), "data offset"));
        long dataOffset = ENTRY_OFFSET_MASK & this.dataOffset.get().longValue();
        if (dataOffset == 0L) {
            isDirectory = false;
            return;
        }
        if (dataOffset > Integer.MAX_VALUE) {
            throw new RuntimeException("Unable to set offset to more than 2Gb.");
        }
        isDirectory = 0L != (DATA_IS_DIRECTORY_MASK & this.dataOffset.get().longValue());
        int saved = bytes.position();
        bytes.seek(bytes.marked() + (int) dataOffset);
        if (isDirectory) {
            directory = new RDirHeader(bytes, section, level);
        } else {
            resourceDataEntry = new RDataEntry(bytes, section);
        }
        bytes.seek(saved);
    }
}
