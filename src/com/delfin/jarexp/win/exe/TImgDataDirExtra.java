package com.delfin.jarexp.win.exe;


class TImgDataDirExtra extends TByteDef<UInteger> {

    private TInt virtualAddress;
    private TInt size;

    TImgDataDirExtra(UByteArray bytes, String description) {
        super(description);
        virtualAddress = new TInt(bytes.readUInt(4), "Virtual Address");
        size = new TInt(bytes.readUInt(4), "Size");
    }

    @Override
    UInteger get() {
        return virtualAddress.get();
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
	        .append(": ")
	        .append(PE.EOL)
	        .append('\t')
	        .append("address: ")
	        .append(virtualAddress)
	        .append(" (0x")
	        .append(virtualAddress.get().toHexString())
	        .append(")")
	        .append(PE.EOL)
	        .append('\t')
	        .append("size: ")
	        .append(size.get())
	        .append(" (0x")
	        .append(size.get().toHexString())
	        .append(')')
	        .append(PE.EOL);
    }
}
