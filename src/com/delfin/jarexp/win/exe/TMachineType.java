package com.delfin.jarexp.win.exe;


class TMachineType extends TByteDef<EMachineType> {

    private final UShort value;

    TMachineType(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    EMachineType get() {
        return EMachineType.get(value);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(get().getDescription())
	        .append(PE.EOL);
    }

}
