package com.delfin.jarexp.win.exe;


class HCoffFile extends HHeader {

    static final int HEADER_SIZE = 20;

    final TMachineType machine;
    final TWord numOfSections;
    final TDword numOfSymbols;
    final TTimeDate timeDateStamp;
    final TDword pointerToSymbolTable;
    final TWord sizeOfOptionalHeader;
    final TCharacteristicsCoff characteristics;

    HCoffFile(UByteArray bytes) {
        machine    			 = addHeader(new TMachineType(bytes.readUShort(2), "Machine type"));
        numOfSections 		 = addHeader(new TWord(bytes.readUShort(2), "Sections number"));
        timeDateStamp  		 = addHeader(new TTimeDate(bytes.readUInt(4), "TimeDate stamp"));
        pointerToSymbolTable = addHeader(new TDword(bytes.readUInt(4), "Pointer to symbol table"));
        numOfSymbols      	 = addHeader(new TDword(bytes.readUInt(4), "Symbols number"));
        sizeOfOptionalHeader = addHeader(new TWord(bytes.readUShort(2), "Optional header size"));
        characteristics    	 = addHeader(new TCharacteristicsCoff(bytes.readUShort(2), "Characteristics"));
    }

}
