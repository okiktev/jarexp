package com.delfin.jarexp.win.exe;


class THeaderDef extends TByteDef<String> {

    THeaderDef(String descriptive) {
        super(descriptive);
    }

    @Override
    String get() {
        return getDescriptive();
    }

    @Override
    void format(StringBuilder b) {
        b.append(PE.EOL)
	        .append(get())
	        .append(PE.EOL)
	        .append("------------------------")
	        .append(PE.EOL)
	        .append(PE.EOL);
    }

}
