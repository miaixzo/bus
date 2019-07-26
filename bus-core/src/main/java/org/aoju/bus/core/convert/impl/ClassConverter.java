package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;

/**
 * 类转换器<br>
 * 将类名转换为类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ClassConverter extends AbstractConverter<Class<?>> {

    @Override
    protected Class<?> convertInternal(Object value) {
        String valueStr = convertToStr(value);
        try {
            return ClassUtils.getClassLoader().loadClass(valueStr);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

}
