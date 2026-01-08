/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converts an object to an {@link AtomicReference}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicReferenceConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852265266938L;

    /**
     * The converter used to convert the object wrapped by the {@link AtomicReference}.
     */
    private final Converter converter;

    /**
     * Constructs a new {@code AtomicReferenceConverter}.
     *
     * @param converter The converter for the referenced object.
     */
    public AtomicReferenceConverter(final Converter converter) {
        this.converter = converter;
    }

    /**
     * Internally converts the given value to an {@link AtomicReference}.
     *
     * @param targetClass The target class, which should be {@link AtomicReference}.
     * @param value       The value to be converted.
     * @return The converted {@link AtomicReference} object.
     */
    @Override
    protected AtomicReference<?> convertInternal(final Class<?> targetClass, final Object value) {
        // Attempt to convert the value to the generic type of the AtomicReference.
        Object targetValue = null;
        final Type paramType = TypeKit.getTypeArgument(AtomicReference.class);
        if (null != paramType && !TypeKit.isUnknown(paramType)) {
            targetValue = converter.convert(paramType, value);
        }

        // If conversion is not possible or not needed, use the original value.
        if (null == targetValue) {
            targetValue = value;
        }

        return new AtomicReference<>(targetValue);
    }

}
