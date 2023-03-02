package com.delfin.jarexp.win.exe;


enum EMachineType {

    NONE("", "No specified machine type"),
    IMG_FILE_MACHINE_UNKNOWN("0", "the contents of this field are assumed to be applicable for any machine type"),
    IMG_FILE_MACHINE_AM33("1d3", "Matsushita AM33"),
    IMG_FILE_MACHINE_AMD64("8664", "x64"),
    IMG_FILE_MACHINE_ARM("1c0", "ARM little endian"),
    IMG_FILE_MACHINE_ARMV7("1c4", "ARMv7 (or higher) Thumb mode only"),
    IMG_FILE_MACHINE_EBC("ebc", "EFI byte code"),
    IMG_FILE_MACHINE_I386("14c", "Intel 386 or later processors and compatible processors"),
    IMG_FILE_MACHINE_IA64("200", "Intel Itanium processor family"),
    IMG_FILE_MACHINE_M32R("9041", "Mitsubishi M32R little endian"),
    IMG_FILE_MACHINE_MIPS16("266", "MIPS16"),
    IMG_FILE_MACHINE_MIPSFPU("366", "MIPS with FPU"),
    IMG_FILE_MACHINE_MIPSFPU16("466", "MIPS16 with FPU"),
    IMG_FILE_MACHINE_POWERPC("1f0", "Power PC little endian"),
    IMG_FILE_MACHINE_POWERPCFP("1f1", "Power PC with floating point support"),
    IMG_FILE_MACHINE_R4000("166", "MIPS little endian"),
    IMG_FILE_MACHINE_SH3("1a2", "Hitachi SH3"),
    IMG_FILE_MACHINE_SH3DSP("1a3", "Hitachi SH3 DSP"),
    IMG_FILE_MACHINE_SH4("1a6", "Hitachi SH4"),
    IMG_FILE_MACHINE_SH5("1a8", "Hitachi SH5"),
    IMG_FILE_MACHINE_THUMB("1c2", "ARM or Thumb (\"interworking\")"),
    IMG_FILE_MACHINE_WCEMIPSV2("169", "MIPS little-endian WCE v2");

    private final String hexValue;
    private final String description;

    EMachineType(String hexValue, String description) {
        this.hexValue = hexValue;
        this.description = description;
    }

    static EMachineType get(UShort value) {
        String key = value.toHexString();
        for (EMachineType mt : values()) {
            if (key.equals(mt.hexValue)) {
                return mt;
            }
        }
        return NONE;
    }

    String getDescription() {
        return this.description;
    }
}
