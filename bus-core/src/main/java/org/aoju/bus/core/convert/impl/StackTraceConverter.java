package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.util.Map;

/**
 * {@link StackTraceElement} 转换器
 * 只支持Map方式转换
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class StackTraceConverter extends AbstractConverter<StackTraceElement> {

    @Override
    protected StackTraceElement convertInternal(Object value) {
        if (value instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value;

            final String declaringClass = MapUtils.getString(map, "className");
            final String methodName = MapUtils.getString(map, "methodName");
            final String fileName = MapUtils.getString(map, "fileName");
            final Integer lineNumber = MapUtils.getInt(map, "lineNumber");

            return new StackTraceElement(declaringClass, methodName, fileName, ObjectUtils.defaultIfNull(lineNumber, 0));
        }
        return null;
    }

}
