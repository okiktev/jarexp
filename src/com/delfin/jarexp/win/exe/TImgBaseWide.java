package com.delfin.jarexp.win.exe;


class TImgBaseWide extends TByteDef<ULong> {

    private final ULong value;

    TImgBaseWide(ULong value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ULong get() {
        return value;
    }

    @Override
    void format(StringBuilder b) {
        EImgBase imageBase = EImgBase.get(ULong.valueOf(value.longValue()));
        b.append(getDescriptive())
			.append(": ")
			.append(value)
			.append(" (0x")
			.append(value.toHexString())
			.append(") (")
        	.append(imageBase == null ? "no image base default" : imageBase.getDescription())
        	.append(')')
        	.append(PE.EOL);
    }
}
