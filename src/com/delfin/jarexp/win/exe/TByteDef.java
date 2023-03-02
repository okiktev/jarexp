package com.delfin.jarexp.win.exe;


abstract class TByteDef<T> {

    private final String descriptive;

    TByteDef(String descriptive) {
        this.descriptive = descriptive;
    }

    String getDescriptive() {
        return descriptive;
    }

    abstract T get();
    abstract void format(StringBuilder b);

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        format(b);
        return b.toString();
    }
}
