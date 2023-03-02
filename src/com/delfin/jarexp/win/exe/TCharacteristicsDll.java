package com.delfin.jarexp.win.exe;


class TCharacteristicsDll extends TByteDef<ECharacteristicsDll[]> {

    private final UShort value;

    TCharacteristicsDll(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ECharacteristicsDll[] get() {
        return ECharacteristicsDll.get(this.value);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(':')
	        .append(PE.EOL);
        ECharacteristicsDll[] characteristics = get();
        if (characteristics.length > 0) {
            for (ECharacteristicsDll c : characteristics) {
                b.append("\t * ")
	                .append(c.getDescription())
	                .append(PE.EOL);
            }
        } else {
            b.append("\t * none")
            	.append(PE.EOL);
        }
        b.append(PE.EOL);
    }

}
