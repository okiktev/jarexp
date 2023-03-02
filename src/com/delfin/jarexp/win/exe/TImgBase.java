package com.delfin.jarexp.win.exe;


class TImgBase extends TByteDef<UInteger> {

    private final UInteger value;

    TImgBase(UInteger value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    UInteger get() {
        return value;
    }

    @Override
    void format(StringBuilder b) {
    	EImgBase imageBase = EImgBase.get(value);
        b.append(getDescriptive()).append(": ")
	        .append(value)
	        .append(" (0x")
	        .append(value.toHexString())
	        .append(") (")
	        .append(imageBase == null ? "no image base default" : imageBase.getDescription())
	        .append(')')
	        .append(PE.EOL);
    }

}
