package com.delfin.jarexp.win.exe;


class RDirHeader extends HHeader {

    RDirEntry[] entries;

    RDirHeader(UByteArray bytes, HSectionTableEntry section, int level) {
    	// just do reading to reach entries
        new TDword(bytes.readUInt(4), "Resource Characteristics");
        new TTimeDate(bytes.readUInt(4), "Date");
        new TWord(bytes.readUShort(2), "Major Version");
        new TWord(bytes.readUShort(2), "Minor Version");
        // reading entries
        int numOfNamedEntires = new TWord(bytes.readUShort(2), "Number of Name Entries").get().intValue();
        int numOfIDEntires = new TWord(bytes.readUShort(2), "Number of ID Entries").get().intValue();
        int numOfEntries = numOfNamedEntires + numOfIDEntires;
        entries = new RDirEntry[numOfEntries];
        for (int i = 0; i < numOfEntries; ++i) {
            entries[i] = new RDirEntry(bytes, section, level + 1);
        }
    }
}
