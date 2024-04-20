/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.toolkit.MapKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.util.Map;

/**
 * {@link StackTraceElement} 转换器
 * 只支持Map方式转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StackTraceConverter extends AbstractConverter {

    private static final long serialVersionUID = 1L;

    @Override
    protected StackTraceElement convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value;

            final String declaringClass = MapKit.getString(map, "className");
            final String methodName = MapKit.getString(map, "methodName");
            final String fileName = MapKit.getString(map, "fileName");
            final Integer lineNumber = MapKit.getInt(map, "lineNumber");

            return new StackTraceElement(declaringClass, methodName, fileName, ObjectKit.defaultIfNull(lineNumber, 0));
        }
        return null;
    }

}
