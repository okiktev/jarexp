package com.delfin.jarexp.utils;

import java.util.Enumeration;

public abstract class Enumerator<T> {

    public Enumerator(Enumeration<T> entities) {
        while (entities.hasMoreElements()) {
            doAction(entities.nextElement());
        }
    }

    protected abstract void doAction(T entity);

}
