package com.delfin.jarexp.win.exe;


class TInt extends TByteDef<UInteger> {

    private final UInteger value;

    TInt(UInteger value, String descriptive) {
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
	        .append(value.toBigInteger().toString(16))
	        .append(')')
	        .append(PE.EOL);
    }

}
