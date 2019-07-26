package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 强转转换器
 *
 * @param <T> 强制转换到的类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CastConverter<T> extends AbstractConverter<T> {

    private Class<T> targetType;

    @Override
    protected T convertInternal(Object value) {
        // 由于在AbstractConverter中已经有类型判断并强制转换，因此当在上一步强制转换失败时直接抛出异常
        throw new CommonException("Can not cast value to [{}]", this.targetType);
    }

    @Override
    public Class<T> getTargetType() {
        return this.targetType;
    }
}
