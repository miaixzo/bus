package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BooleanUtils;

/**
 * 波尔转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BooleanConverter extends AbstractConverter<Boolean> {

    @Override
    protected Boolean convertInternal(Object value) {
        if (boolean.class == value.getClass()) {
            return Boolean.valueOf((boolean) value);
        }
        String valueStr = convertToStr(value);
        return Boolean.valueOf(BooleanUtils.toBoolean(valueStr));
    }

}
