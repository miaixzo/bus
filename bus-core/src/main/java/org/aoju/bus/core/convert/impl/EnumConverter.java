package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

/**
 * 无泛型检查的枚举转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EnumConverter extends AbstractConverter<Object> {

    private Class enumClass;

    /**
     * 构造
     *
     * @param enumClass 转换成的目标Enum类
     */
    public EnumConverter(Class enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    protected Object convertInternal(Object value) {
        return Enum.valueOf(enumClass, convertToStr(value));
    }

    @Override
    public Class getTargetType() {
        return this.enumClass;
    }
}
