package com.delfin.jarexp.win.exe;


class TSubsystem extends TByteDef<ESubsys> {

    private final UShort value;

    TSubsystem(UShort value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    ESubsys get() {
        return ESubsys.get(value);
    }

    @Override
    void format(StringBuilder b) {
        ESubsys s = get();
        if (s == null) {
            b.append("ERROR, no subsystem description for value: ")
	            .append(value)
	            .append(PE.EOL);
        } else {
        	b.append(getDescriptive())
	        	.append(": ")
	        	.append(s.getDescription())
	        	.append(PE.EOL);
        }
    }
}
