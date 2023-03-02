package com.delfin.jarexp.win.exe;


enum EMagicNum {

    NONE("", "ERROR, unable to recognize magic number"),
    PE32("10B", "PE32, normal executable file"),
    PE32_PLUS("20B", "PE32+ executable"),
    ROM("107", "ROM image");

    private final String hexValue;
    private final String description;

    EMagicNum(String hexValue, String description) {
        this.hexValue = hexValue.toLowerCase();
        this.description = description;
    }

    static EMagicNum get(UShort value) {
        String key = value.toHexString();
        for (EMagicNum mt : values()) {
            if (key.equals(mt.hexValue)) {
                return mt;
            }
        }
        return NONE;
    }

    String getDescription() {
        return description;
    }

}