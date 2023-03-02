package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


class HSectionTable extends HHeader {

	List<HSectionTableEntry> sections;

	HSectionTable(UByteArray bytes, int entriesNumber) {
		sections = new ArrayList<HSectionTableEntry>(entriesNumber);
		bytes.mark();
		for (int i = 0; i < entriesNumber; i++) {
			int offset = i * HSectionTableEntry.ENTRY_SIZE;
			bytes.skip(offset);
			sections.add(new HSectionTableEntry(bytes, i + 1, offset, HSectionTableEntry.ENTRY_SIZE));
			bytes.reset();
		}
	}
}
