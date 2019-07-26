package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

/**
 * 字符串转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class StringConverter extends AbstractConverter<String> {

    @Override
    protected String convertInternal(Object value) {
        return convertToStr(value);
    }

}
