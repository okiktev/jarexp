package com.delfin.jarexp.win.exe;


class TCharacteristicsSection extends TByteDef<ECharacteristicsSection[]> {

    private final UInteger value;

    TCharacteristicsSection(UInteger value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ECharacteristicsSection[] get() {
        return ECharacteristicsSection.get(value);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(PE.EOL);
        ECharacteristicsSection[] characteristics = get();
        if (characteristics.length > 0) {
            for (ECharacteristicsSection c : characteristics) {
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
