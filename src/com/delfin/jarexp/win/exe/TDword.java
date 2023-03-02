package com.delfin.jarexp.win.exe;


class TDword extends TByteDef<UInteger> {

    private final UInteger value;

    TDword(UInteger value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    UInteger get() {
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
