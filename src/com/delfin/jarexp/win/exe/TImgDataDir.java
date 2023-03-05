package com.delfin.jarexp.win.exe;


class TImgDataDir extends TByteDef<UInteger> {

    private final EDirEntry entry;
    private final TInt virtualAddress;
    private final TInt size;

    private HSectionTableEntry section;
    HHeader data;

    TImgDataDir(UByteArray bytes, EDirEntry entry) {
        super(entry.getDescription());

        this.entry = entry;
        virtualAddress = new TInt(bytes.readUInt(4), "Virtual Address");
        size = new TInt(bytes.readUInt(4), "Size");
    }

    EDirEntry getType() {
        return entry;
    }

    TInt getSize() {
    	return size;
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
	        .append(')')
	        .append(PE.EOL)
	        .append('\t')
	        .append("size: ")
	        .append(size.get())
	        .append(" (0x")
	        .append(size.get().toHexString())
	        .append(')')
	        .append(PE.EOL);
    }

    void setSection(HSectionTableEntry section) {
        this.section = section;
    }

    HSectionTableEntry getSection() {
        return section;
    }
}
