package com.delfin.jarexp.win.exe;


class HSectionTableEntry extends HHeader {

    final static int ENTRY_SIZE = 40;

    final TAsciiString name;
    final TDword virtualSize;
    final TDword virtualAddress;
    final TDword sizeOfRawData;
    final TDword pointerToRawData;
    final TDword pointerToRelocations;
    final TDword pointerToLineMembers;
    final TWord numOfRelocations;
    final TWord numOfLineNumbers;
    final TCharacteristicsSection characteristics;

    HSectionTableEntry(UByteArray bytes, int entryNumber, int offset, int size) {
        addHeader(new THeaderDef("Section table entry: " + entryNumber));
        name = addHeader(new TAsciiString(bytes, 8, "Name"));
        virtualSize = addHeader(new TDword(bytes.readUInt(4), "Virtual size"));
        virtualAddress = addHeader(new TDword(bytes.readUInt(4), "Virtual address"));
        sizeOfRawData = addHeader(new TDword(bytes.readUInt(4), "Size of raw data"));
        pointerToRawData = addHeader(new TDword(bytes.readUInt(4), "Pointer to raw data"));
        pointerToRelocations = addHeader(new TDword(bytes.readUInt(4), "Pointer to relocations"));
        pointerToLineMembers = addHeader(new TDword(bytes.readUInt(4), "Pointer to line numbers"));
        numOfRelocations = addHeader(new TWord(bytes.readUShort(2), "Number of relocations"));
        numOfLineNumbers = addHeader(new TWord(bytes.readUShort(2), "Number of line numbers"));
        characteristics = addHeader(new TCharacteristicsSection(bytes.readUInt(4), "Characteristics"));
    }
    
}
