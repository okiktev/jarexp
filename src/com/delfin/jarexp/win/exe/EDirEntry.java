package com.delfin.jarexp.win.exe;


enum EDirEntry {

    EXPORT("Export Directory"),
    IMPORT("Import Directory"),
    RESOURCE("Resource Directory"),
    EXCEPTION("Exception Directory"),
    SECURITY("Security Directory"),
    BASERELOC("Base Relocation Table"),
    DEBUG("Debug Directory"),
    COPYRIGHT("Description String"),
    GLOBALPTR("Machine Value (MIPS GP)"),
    TLS("TLS Directory"),
    LOAD_CONFIG("Load Configuration Directory");

    private final String description;

    EDirEntry(String description) {
        this.description = description;
    }

    String getDescription() {
        return description;
    }
}
