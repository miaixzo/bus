package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BooleanUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link AtomicBoolean}转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AtomicBooleanConverter extends AbstractConverter<AtomicBoolean> {

    @Override
    protected AtomicBoolean convertInternal(Object value) {
        if (boolean.class == value.getClass()) {
            return new AtomicBoolean((boolean) value);
        }
        if (value instanceof Boolean) {
            return new AtomicBoolean((Boolean) value);
        }
        final String valueStr = convertToStr(value);
        return new AtomicBoolean(BooleanUtils.toBoolean(valueStr));
    }

}
