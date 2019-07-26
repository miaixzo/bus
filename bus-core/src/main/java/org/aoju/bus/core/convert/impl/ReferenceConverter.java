package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.TypeUtils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

/**
 * {@link Reference}转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ReferenceConverter extends AbstractConverter<Reference> {

    private Class<? extends Reference> targetType;

    /**
     * 构造
     *
     * @param targetType {@link Reference}实现类型
     */
    public ReferenceConverter(Class<? extends Reference> targetType) {
        this.targetType = targetType;
    }

    @Override
    protected Reference<?> convertInternal(Object value) {
        //尝试将值转换为Reference泛型的类型
        Object targetValue = null;
        final Type paramType = TypeUtils.getTypeArgument(targetType);
        if (null != paramType) {
            targetValue = ConverterRegistry.getInstance().convert(paramType, value);
        }
        if (null == targetValue) {
            targetValue = value;
        }

        if (this.targetType == WeakReference.class) {
            return new WeakReference(targetValue);
        } else if (this.targetType == SoftReference.class) {
            return new SoftReference(targetValue);
        }
        throw new UnsupportedOperationException(StringUtils.format("Unsupport Reference type: {}", this.targetType.getName()));
    }

}
