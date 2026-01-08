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
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.xyz.BooleanKit;

/**
 * Converts an object to an {@link AtomicBoolean}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicBooleanConverter extends AbstractConverter {

    /**
     * Constructs a new AtomicBooleanConverter. Utility class constructor for static access.
     */
    public AtomicBooleanConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final AtomicBooleanConverter INSTANCE = new AtomicBooleanConverter();
    @Serial
    private static final long serialVersionUID = 2852263652262L;

    /**
     * Internally converts the given value to an {@link AtomicBoolean}.
     *
     * @param targetClass The target class, which should be {@link AtomicBoolean}.
     * @param value       The value to be converted.
     * @return The converted {@link AtomicBoolean} object.
     */
    @Override
    protected AtomicBoolean convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Boolean) {
            return new AtomicBoolean((Boolean) value);
        }
        final String values = convertToString(value);
        return new AtomicBoolean(BooleanKit.toBoolean(values));
    }

}
