package com.delfin.jarexp.win.exe;


enum ESubsys {

    IMG_SUBSYSTEM_UNKNOWN(0, "unknown subsystem"),
    IMG_SUBSYSTEM_NATIVE(1, "Device drivers and native Windows processes"),
    IMG_SUBSYSTEM_WINDOWS_GUI(2, "The Windows graphical user interface (GUI) subsystem"),
    IMG_SUBSYSTEM_WINDOWS_CUI(3, "The Windows character subsystem"),
    IMG_SUBSYSTEM_POSIX_CUI(7, "The Posix character subsystem"),
    IMG_SUBSYSTEM_WINDOWS_CE_GUI(9, "Windows CE"),
    IMG_SUBSYSTEM_EFI_APPLICATION(10, "An Extensible Firmware Interface (EFI) application"),
    IMG_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER(11, "An EFI driver with boot services"),
    IMG_SUBSYSTEM_EFI_RUNTIME_DRIVER(12, "An EFI driver with run-time services"),
    IMG_SUBSYSTEM_EFI_ROM(13, "An EFI ROM image"),
    IMG_SUBSYSTEM_XBOX(14, "XBOX");

    private final int intValue;
    private final String description;

    ESubsys(int intValue, String description) {
        this.intValue = intValue;
        this.description = description;
    }

    static ESubsys get(UShort value) {
        for (ESubsys v : values()) {
            if (v.intValue == value.intValue()) {
                return v;
            }
        }
        return null;
    }

    String getDescription() {
        return description;
    }

}