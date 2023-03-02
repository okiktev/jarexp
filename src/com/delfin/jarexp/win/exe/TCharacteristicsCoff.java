package com.delfin.jarexp.win.exe;


class TCharacteristicsCoff extends TByteDef<ECharacteristics[]> {

    private final UShort value;

    TCharacteristicsCoff(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ECharacteristics[] get() {
        return ECharacteristics.get(value);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(':')
	        .append(PE.EOL);
        ECharacteristics[] characteristics = get();
        if (characteristics.length > 0) {
            for (ECharacteristics c : characteristics) {
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
