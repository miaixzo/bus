package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

/**
 * 泛型枚举转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class GenericEnumConverter<E extends Enum<E>> extends AbstractConverter<E> {

    private Class<E> enumClass;

    /**
     * 构造
     *
     * @param enumClass 转换成的目标Enum类
     */
    public GenericEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    protected E convertInternal(Object value) {
        return Enum.valueOf(enumClass, convertToStr(value));
    }

    @Override
    public Class<E> getTargetType() {
        return this.enumClass;
    }
}
