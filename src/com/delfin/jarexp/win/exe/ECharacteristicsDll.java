package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


enum ECharacteristicsDll {

    IMG_DLL_CHARACTERISTICS_DYNAMIC_BASE("40", "DLL can be relocated at load time."),
    IMG_DLL_CHARACTERISTICS_FORCE_INTEGRITY("80", "Code Integrity checks are enforced."),
    IMG_DLL_CHARACTERISTICS_NX_COMPAT("100", "Image is NX compatible."),
    IMG_DLL_CHARACTERISTICS_ISOLATION("200", "Isolation aware, but do not isolate the image."),
    IMG_DLLCHARACTERISTICS_NO_SEH("400", "Does not use structured exception (SE) handling. No SE handler may be called in this image."),
    IMG_DLLCHARACTERISTICS_NO_BIND("800", "Do not bind the image."),
    IMG_DLLCHARACTERISTICS_WDM_DRIVER("2000", "A WDM driver."),
    IMG_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE("8000", "Terminal Server aware.");

    private final String hexValue;
    private final String description;

    ECharacteristicsDll(String hexValue, String description) {
        this.hexValue = hexValue;
        this.description = description;
    }

    static ECharacteristicsDll[] get(UShort value) {
        List<ECharacteristicsDll> chars = new ArrayList<ECharacteristicsDll>(0);
        for (ECharacteristicsDll c : values()) {
            long mask = Long.parseLong(c.hexValue, 16);
            if ((value.intValue() & mask) != 0) {
                chars.add(c);
            }
        }
        return chars.toArray(new ECharacteristicsDll[0]);
    }

    String getDescription() {
        return description;
    }
}
