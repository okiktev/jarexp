package com.delfin.jarexp.win.exe;


class TDwordWide extends TByteDef<ULong> {

    private final ULong value;

    TDwordWide(ULong value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ULong get() {
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
