package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link AtomicReference}转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AtomicReferenceConverter extends AbstractConverter<AtomicReference> {

    @Override
    protected AtomicReference<?> convertInternal(Object value) {

        //尝试将值转换为Reference泛型的类型
        Object targetValue = null;
        final Type paramType = TypeUtils.getTypeArgument(AtomicReference.class);
        if (null != paramType) {
            targetValue = ConverterRegistry.getInstance().convert(paramType, value);
        }
        if (null == targetValue) {
            targetValue = value;
        }

        return new AtomicReference<>(targetValue);
    }

}
