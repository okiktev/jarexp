package com.delfin.jarexp.win.exe;

import java.nio.charset.Charset;


class TResDirName extends TByteDef<String> {

    private static final int NAME_IS_STRING_MASK = 0x80000000;
    private static final int NAME_OFFSET_MASK = 0x7FFFFFFF;

    private final String value;
    private final int level;

    TResDirName(UInteger intValue, String descriptive, UByteArray bytes, int level) {
        super(descriptive);
        this.level = level;
        long valueInt = intValue.longValue();
        boolean isString = 0 != (valueInt & NAME_IS_STRING_MASK);
        if (isString) {
            int savedPosition = bytes.position();
            long offset = valueInt & NAME_OFFSET_MASK;
            if (offset > Integer.MAX_VALUE) {
                throw new RuntimeException("Unable to set offset to more than 2gb!");
            }
            bytes.seek(bytes.marked() + (int) offset);
            int length = bytes.readUShort(2).intValue();
            byte[] buff = new byte[length * 2];
            for (int i = 0; i < buff.length; i++) {
                buff[i] = bytes.readUByte().byteValue();
            }
            bytes.seek(savedPosition);
            // StandardCharsets.UTF-16LE
            value = new String(buff, Charset.forName("UTF-16LE")).trim();
        } else {
            switch (level) {
                case 1: value = EResource.get(intValue).getDetailedInfo();
                    break;
                case 2:
                case 3:
                default: value = intValue.toHexString();
                    break;
            }
        }
    }

    @Override
    String get() {
        return value;
    }

    @Override
    void format(StringBuilder b) {
        b.append(getDescriptive())
        	.append(": ");
        switch (this.level) {
            case 1: break;
            case 2: b.append("name: "); break;
            case 3: b.append("Language: " ); break;
            default: b.append("??: "); break;
        }
        b.append(value)
        	.append(PE.EOL);
    }
}
