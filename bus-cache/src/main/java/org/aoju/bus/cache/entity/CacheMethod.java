package org.aoju.bus.cache.entity;

import lombok.Data;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class CacheMethod {

    private Class<?> innerReturnType;

    private Class<?> returnType;

    private boolean collection;

    public CacheMethod(boolean collection) {
        this.collection = collection;
    }

    public boolean isCollection() {
        return collection;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Class<?> getInnerReturnType() {
        return innerReturnType;
    }

    public void setInnerReturnType(Class<?> innerReturnType) {
        this.innerReturnType = innerReturnType;
    }
}
