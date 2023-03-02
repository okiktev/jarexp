package com.delfin.jarexp.win.exe;


class TAsciiString extends TByteDef<String> {

    private final String value;

    TAsciiString(UByteArray bytes, int byteLength, String descriptive) {
        super(descriptive);

        value = new String(bytes.copyBytes(byteLength), PE.US_ASCII).trim();
    }

    @Override
    String get() {
        return value;
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
		    .append(value)
		    .append(PE.EOL);
    }
}
