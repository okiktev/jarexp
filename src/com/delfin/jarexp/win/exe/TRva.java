package com.delfin.jarexp.win.exe;


class TRva extends TByteDef<UInteger> {

    private final UInteger value;

    TRva(UInteger value, String descriptive) {
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
	        .append(value.longValue())
	        .append(PE.EOL);
    }

}
