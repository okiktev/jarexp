package com.delfin.jarexp.win.exe;

import java.util.Date;


class TTimeDate extends TByteDef<Date> {

    private final UInteger value;

    TTimeDate(UInteger value, String descriptive) {
        super(descriptive);
        this.value = value;
    }

    @Override
    Date get() {
        return new Date(value.longValue() * 1000);
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(get().toString())
	        .append(PE.EOL);
    }

}
