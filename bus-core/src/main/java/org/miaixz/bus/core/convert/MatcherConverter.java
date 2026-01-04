/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.lang.reflect.Type;

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A converter with matching capability. Determines whether the target object meets the conditions. If met, converts;
 * otherwise skips. Implementing this interface can also directly convert without determining assertions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MatcherConverter extends Converter {

    /**
     * Determines whether the object to be converted matches the current converter. If met, converts; otherwise skips.
     *
     * @param targetType the target type of the conversion, cannot be {@code null}
     * @param rawType    the target raw type, consistent with this parameter when targetType is Class, cannot be
     *                   {@code null}
     * @param value      the value to be converted
     * @return whether it matches
     */
    boolean match(Type targetType, Class<?> rawType, Object value);

    /**
     * Determines whether the object to be converted matches the current converter. If met, converts; otherwise skips.
     *
     * @param targetType the target type of the conversion
     * @param value      the value to be converted
     * @return whether it matches
     */
    default boolean match(final Type targetType, final Object value) {
        return match(targetType, TypeKit.getClass(targetType), value);
    }

}
