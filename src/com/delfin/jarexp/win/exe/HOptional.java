package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


class HOptional extends HHeader {

    List<TImgDataDir> tables = new ArrayList<TImgDataDir>(0);

    final TMagicNum magicNumber;
    final TWord majorLinkerVersion;
    final TWord minorLinkerVersion;
    final TDword sizeOfCode;
    final TDword sizeOfInitData;
    final TDword sizeOfUninitData;
    final TDword entryPointAddress;
    final TDword baseOfCode;
    final TDword baseOfData;
    final TByteDef<?> imageBase;
    final TDword sectionAlignment;
    final TDword fileAlignment;
    final TWord majorOsVer;
    final TWord minorOsVer;
    final TWord majorImgVer;
    final TWord minorImgVer;
    final TWord majorSubsysVer;
    final TWord minorSubsysVer;
    final TDword win32Version;
    final TDword sizeOfImage;
    final TDword sizeOfHeaders;
    final TDword checksum;
    final TSubsystem subsystem;
    final TCharacteristicsDll dllCharacteristics;
    final TByteDef<?> sizeOfStackReserve;
    final TByteDef<?> sizeOfStackCommit;
    final TByteDef<?> sizeOfHeapReserve;
    final TByteDef<?> sizeOfHeapCommit;
    final TDword loaderFlags;
    final TRva numOfRvaAndSizes;
    final TImgDataDir exportTable;
    final TImgDataDir importTable;
    final TImgDataDir resourceTable;
    final TImgDataDir exceptionTable;
    final TImgDataDir certificateTable;
    final TImgDataDir baseRelocationTable;
    final TImgDataDir debug;
    final TImgDataDir copyright;
    final TImgDataDir globalPtr;
    final TImgDataDir tlsTable;
    final TImgDataDir loadConfigTable;
    final TImgDataDirExtra boundImport;
    final TImgDataDirExtra iat;
    final TImgDataDirExtra delayImportDescriptor;
    final TImgDataDirExtra crlRuntimeHeader;

    private boolean is32Bit;

    HOptional(UByteArray bytes) {

        addHeader(new THeaderDef("Standard fields"));
        bytes.mark();

        magicNumber = addHeader(new TMagicNum(bytes.readUShort(2), "Magic number"));
        majorLinkerVersion = addHeader(new TWord(bytes.readUShort(1), "Major linker version"));
        minorLinkerVersion = addHeader(new TWord(bytes.readUShort(1), "Minor linker version"));
        sizeOfCode = addHeader(new TDword(bytes.readUInt(4), "Size of code"));
        sizeOfInitData = addHeader(new TDword(bytes.readUInt(4), "Size of initialized data"));
        sizeOfUninitData = addHeader(new TDword(bytes.readUInt(4), "Size of unitialized data"));
        entryPointAddress = addHeader(new TDword(bytes.readUInt(4), "Address of entry point"));
        baseOfCode = addHeader(new TDword(bytes.readUInt(4), "Address of base of code"));
        baseOfData = addHeader(new TDword(bytes.readUInt(4), "Address of base of data"));

        is32Bit = magicNumber.get() == EMagicNum.PE32;
        bytes.reset();
        bytes.skip(is32Bit ? 28 : 24);
        addHeader(new THeaderDef("Windows specific fields"));
        imageBase = addHeader(is32Bit
        		? new TImgBase(bytes.readUInt(4), "Image base")
        		: new TImgBaseWide(bytes.readULong(8), "Image base"));

        sectionAlignment = addHeader(new TDword(bytes.readUInt(4), "Section alignment in bytes"));
        fileAlignment = addHeader(new TDword(bytes.readUInt(4), "File alignment in bytes"));

        majorOsVer = addHeader(new TWord(bytes.readUShort(2), "Major operating system version"));
        minorOsVer = addHeader(new TWord(bytes.readUShort(2), "Minor operating system version"));
        majorImgVer = addHeader(new TWord(bytes.readUShort(2), "Major image version"));
        minorImgVer = addHeader(new TWord(bytes.readUShort(2), "Minor image version"));
        majorSubsysVer = addHeader(new TWord(bytes.readUShort(2), "Major subsystem version"));
        minorSubsysVer = addHeader(new TWord(bytes.readUShort(2), "Minor subsystem version"));

        win32Version = addHeader(new TDword(bytes.readUInt(4), "Win32 version value (reserved, must be zero)"));
        sizeOfImage = addHeader(new TDword(bytes.readUInt(4), "Size of image in bytes"));
        sizeOfHeaders = addHeader(new TDword(bytes.readUInt(4), "Size of headers (MS DOS stub, PE header, and section headers)"));
        checksum = addHeader(new TDword(bytes.readUInt(4), "Checksum"));
        subsystem = addHeader(new TSubsystem(bytes.readUShort(2), "Subsystem"));
        dllCharacteristics = addHeader(new TCharacteristicsDll(bytes.readUShort(2), "Dll characteristics"));

        if (is32Bit) {
            sizeOfStackReserve = addHeader(new TDword(bytes.readUInt(4), "Size of stack reserve"));
            sizeOfStackCommit = addHeader(new TDword(bytes.readUInt(4), "Size of stack commit"));
            sizeOfHeapReserve = addHeader(new TDword(bytes.readUInt(4), "Size of heap reserve"));
            sizeOfHeapCommit = addHeader(new TDword(bytes.readUInt(4), "Size of heap commit"));
        } else {
            sizeOfStackReserve = addHeader(new TDwordWide(bytes.readULong(8), "Size of stack reserve"));
            sizeOfStackCommit = addHeader(new TDwordWide(bytes.readULong(8), "Size of stack commit"));
            sizeOfHeapReserve = addHeader(new TDwordWide(bytes.readULong(8), "Size of heap reserve"));
            sizeOfHeapCommit = addHeader(new TDwordWide(bytes.readULong(8), "Size of heap commit"));
        }

        loaderFlags = addHeader(new TDword(bytes.readUInt(4), "Loader flags (reserved, must be zero)"));
        numOfRvaAndSizes = addHeader(new TRva(bytes.readUInt(4), "Number of rva and sizes"));

        bytes.reset();
        bytes.skip(is32Bit ? 96 : 112);

        addHeader(new THeaderDef("Data Directories"));
        exportTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.EXPORT)));
        importTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.IMPORT)));
        resourceTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.RESOURCE)));
        exceptionTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.EXCEPTION)));
        certificateTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.SECURITY)));
        baseRelocationTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.BASERELOC)));

        debug = table(addHeader(new TImgDataDir(bytes, EDirEntry.DEBUG)));
        copyright = table(addHeader(new TImgDataDir(bytes, EDirEntry.COPYRIGHT)));
        globalPtr = table(addHeader(new TImgDataDir(bytes, EDirEntry.GLOBALPTR)));
        tlsTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.TLS)));
        loadConfigTable = table(addHeader(new TImgDataDir(bytes, EDirEntry.LOAD_CONFIG)));

        boundImport = addHeader(new TImgDataDirExtra(bytes, "Bound import"));
        iat = addHeader(new TImgDataDirExtra(bytes, "IAT"));
        delayImportDescriptor = addHeader(new TImgDataDirExtra(bytes, "Delay import descriptor"));
        crlRuntimeHeader = addHeader(new TImgDataDirExtra(bytes, "COM+ runtime header"));

        bytes.skip(8);
    }

    private <T extends TImgDataDir> T table(T object) {
        tables.add(object);
        return object;
    }
}
