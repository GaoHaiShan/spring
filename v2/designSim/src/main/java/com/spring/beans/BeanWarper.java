package com.spring.beans;

public class BeanWarper {
    private Object o;
    private Class<?> wapperClass;
    public BeanWarper(Object o) {
        this.o = o;
        wapperClass = o.getClass();
    }

    public Object getWapperInstance(){
        return o;
    }
    public Class<?> getWapperClass(){
        return wapperClass;
    }
}
