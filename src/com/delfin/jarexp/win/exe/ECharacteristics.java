package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


enum ECharacteristics {

    IMG_FILE_RELOCS_STRIPPED("1", "Resource information is stripped from the file"),
    IMG_FILE_EXECUTABLE_IMAGE("2", "The file is executable (no unresoled external references"),
    IMG_FILE_LINE_NUMS_STRIPPED("4", "COFF line numbers are stripped from the file (DEPRECATED)"),
    IMG_FILE_LOCAL_SYMS_STRIPPED("8", "COFF local symbols are stripped form the file (DEPRECATED)"),
    IMG_FILE_AGGRESSIVE_WS_TRIM("10", "Aggressively trim working set (DEPRECATED for Windows 2000 and later)"),
    IMG_FILE_LARGE_ADDRESS_AWARE("20", "Application can handle larger than 2 GB addresses."),
    IMG_FILE_RESERVED("40", "Use of this flag is reserved."),
    IMG_FILE_BYTES_REVERSED_LO("80", "Bytes of the word are reversed (REVERSED LO)"),
    IMG_FILE_32BIT_MACHINE("100", "Machine is based on a 32-bit-word architecture."),
    IMG_FILE_DEBUG_STRIPPED("200", "Debugging is removed from the file."),
    IMG_FILE_REMOVABLE_RUN_FROM_SWAP("400", "If the image is on removable media, fully load it and copy it to the swap file."),
    IMG_FILE_NET_RUN_FROM_SWAP("800", "If the image is on network media, fully load it and copy it to the swap file."),
    IMG_FILE_SYSTEM("1000", "The image file is a system file, (such as a driver) and not a user program."),
    IMG_FILE_DLL("2000", "The image file is a dynamic-link library (DLL). Such files are considered executable files for almost all purposes, although they cannot be directly run."),
    IMG_FILE_UP_SYSTEM_ONLY("4000", "The file should be run only on a uniprocessor machine."),
    IMG_FILE_BYTES_REVERSED_HI("8000", "Bytes of the word are reversed (REVERSED HI)");

    private final String hexValue;
    private final String description;

    ECharacteristics(String hexValue, String description) {
        this.hexValue = hexValue;
        this.description = description;
    }

    static ECharacteristics[] get(UShort value) {
        List<ECharacteristics> chars = new ArrayList<ECharacteristics>(0);
        for (ECharacteristics c : values()) {
            long mask = Long.parseLong(c.hexValue, 16);
            if ((value.intValue() & mask) != 0) {
                chars.add(c);
            }
        }
        return chars.toArray(new ECharacteristics[0]);
    }

    String getDescription() {
        return description;
    }

}
