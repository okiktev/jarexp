package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


enum ECharacteristicsSection {

    IMG_SCN_TYPE_NO_PAD("8", "The section should not be padded to the next boundary. DEPRECATED"),
    IMG_SCN_CNT_CODE("20", "The section contains executable code."),
    IMG_SCN_CNT_INITIALIZED_DATA("40", "The section contains initialized data."),
    IMG_SCN_CNT_UNINITIALIZED_DATA("80", "The section contains uninitialized data."),
    IMG_SCN_LNK_INFO("200", "The section contains comments or other information. Valid for object files only."),
    IMG_SCN_LNK_REMOVE("800", "The section will not become part of the image. Valid for object files only."),
    IMG_SCN_LNK_COMDAT("1000", "The section contains COMDAT data."),
    IMG_SCN_GPREL("8000", "The section contains data referenced through the global pointer (GP)."),
    IMG_SCN_MEM_16BIT("20000", "For ARM machine types, the section contains Thumb code."),
    IMG_SCN_ALIGN_1BYTES("100000", "Align data on a 1-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_2BYTES("200000", "Align data on a 2-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_4BYTES("300000", "Align data on a 4-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_8BYTES("400000", "Align data on a 8-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_16BYTES("500000", "Align data on a 16-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_32BYTES("600000", "Align data on a 32-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_64BYTES("700000", "Align data on a 64-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_128BYTES("800000", "Align data on a 128-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_256BYTES("900000", "Align data on a 256-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_512BYTES("A00000", "Align data on a 512-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_1024BYTES("B00000", "Align data on a 1024-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_2048BYTES("C00000", "Align data on a 2048-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_4096BYTES("D00000", "Align data on a 4096-byte boundary. Valid only for object files."),
    IMG_SCN_ALIGN_8192BYTES("E00000", "Align data on a 8192-byte boundary. Valid only for object files."),
    IMG_SCN_LNK_NRELOC_OVFL("1000000", "The section contains extended relocations."),
    IMG_SCN_MEM_DISCARDABLE("2000000", "The section can be discarded as needed."),
    IMG_SCN_MEM_NOT_CACHED("4000000", "The section cannot be cached."),
    IMG_SCN_MEM_NOT_PAGED("8000000", "The section is not pageable."),
    IMG_SCN_MEM_SHARED("10000000", "The section can be shared in memory."),
    IMG_SCN_MEM_EXECUTE("20000000", "The section can be executed as code."),
    IMG_SCN_MEM_READ("80000000", "The section can be read."),
    IMG_SCN_MEM_WRITE("80000000", "The section can be written to.");

    private final String hexValue;
    private final String description;

    ECharacteristicsSection(String hexValue, String description) {
        this.hexValue = hexValue;
        this.description = description;
    }

    static ECharacteristicsSection[] get(UInteger value) {
        List<ECharacteristicsSection> chars = new ArrayList<ECharacteristicsSection>(0);
        long keyAsLong = value.longValue();
        for (ECharacteristicsSection c : values()) {
            long mask = Long.parseLong(c.hexValue, 16);
            if ((keyAsLong & mask) != 0) {
                chars.add(c);
            }
        }
        return chars.toArray(new ECharacteristicsSection[0]);
    }

    String getDescription() {
        return description;
    }

}
