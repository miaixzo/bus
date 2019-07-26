package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.utils.ArrayUtils;

/**
 * byte 类型数组转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ByteArrayConverter extends AbstractConverter<byte[]> {

    @Override
    protected byte[] convertInternal(Object value) {
        final Byte[] result = ConverterRegistry.getInstance().convert(Byte[].class, value);
        return ArrayUtils.unWrap(result);
    }

}
