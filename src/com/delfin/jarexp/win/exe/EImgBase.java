package com.delfin.jarexp.win.exe;


enum EImgBase {

    IMAGE_BASE_DEFAULT(0x10000000L, "DLL default"),
    IMAGE_BASE_WIN_CE(0x00010000L, "Default for Windows CE EXEs"),
    IMAGE_BASE_WIN(0x00400000L, "Default for Windows NT, 2000, XP, 95, 98 and Me");

    private final long value;
    private final String description;

    EImgBase(long value, String description) {
        this.value = value;
        this.description = description;
    }

    static EImgBase get(UInteger value) {
        for (EImgBase v : values()) {
            if (value.longValue() == v.value) {
                return v;
            }
        }
        return null;
    }

    static EImgBase get(ULong key) {
    	for (EImgBase v : values()) {
    		if (key.longValue() == v.value) {
    			return v;
    		}
    	}
    	return null;
    }

    String getDescription() {
        return description;
    }
}
