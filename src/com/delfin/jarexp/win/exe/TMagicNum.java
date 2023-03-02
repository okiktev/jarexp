package com.delfin.jarexp.win.exe;


class TMagicNum extends TByteDef<EMagicNum> {

    private final UShort value;

    TMagicNum(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    EMagicNum get() {
        return EMagicNum.get(value);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(value)
	        .append(" --> ")
	        .append(get().getDescription())
	        .append(PE.EOL);
    }
}
