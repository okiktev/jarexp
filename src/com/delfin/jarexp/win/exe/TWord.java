package com.delfin.jarexp.win.exe;


class TWord extends TByteDef<UShort> {

    private final UShort value;

    TWord(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    UShort get() {
        return value;
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(value)
	        .append(" (0x")
	        .append(value.toHexString())
	        .append(')')
	        .append(PE.EOL);
    }
}
