package com.delfin.jarexp.win.exe;

import java.util.ArrayList;
import java.util.List;


abstract class HHeader {

    List<TByteDef<?>> headers = new ArrayList<TByteDef<?>>();

    HHeader() {
    }

    protected <T extends TByteDef<?>> T addHeader(T object) {
        headers.add(object);
        return object;
    }

}
