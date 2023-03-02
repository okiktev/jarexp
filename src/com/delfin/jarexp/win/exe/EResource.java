package com.delfin.jarexp.win.exe;


enum EResource {

    N_1("???_0"),
    CURSOR("Cursor"),
    BITMAP("Bitmap"),
    ICON("Icon"),
    MENU("Menu"),
    DLG_BOX("Dialog Box"),
    STRING_TABLE_ENTRY("String"),
    FONT_DIR("Font Directory"),
    FONT("Font"),
    ACCEL_TABLE("Accelerators"),
    RAW_DATA("application defined resource (raw data)"),
    MESSAGE_TABLE_ENTRY("Message entry"),
    GROUP_CURSOR("Group Cursor"),
    N_13("???_13"),
    GROUP_ICON("Group Icon"),
    N_15("???_15"),
    VER_INFO("Version"),
    DLG_INCLUDE("dlginclude"),
    N_18("???_18"),
    PNP_RESOURCE("Plug and Play Resource"),
    VXD("VXD"),
    ANIM_CURSOR("Animated Cursor"),
    ANIM_ICON("Animated Icon"),
    HTML("HTML"),
    MANIFEST("Manifest");

    private final String detailedInfo;

    EResource(String detailedInfo) {
        this.detailedInfo = detailedInfo;
    }

    String getDetailedInfo() {
        return detailedInfo;
    }

    static EResource get(UInteger value) {
        int valueAsInt = value.intValue();
        for (EResource v : values()) {
            if (valueAsInt == v.ordinal()) {
                return v;
            }
        }
        return null;
    }

}