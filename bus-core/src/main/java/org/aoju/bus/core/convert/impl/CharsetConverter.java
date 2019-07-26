package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.CharsetUtils;

import java.nio.charset.Charset;

/**
 * 编码对象转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CharsetConverter extends AbstractConverter<Charset> {

    @Override
    protected Charset convertInternal(Object value) {
        return CharsetUtils.charset(convertToStr(value));
    }

}
