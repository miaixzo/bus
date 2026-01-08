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

import org.miaixz.bus.core.xyz.BooleanKit;

/**
 * Converts an object to a {@link Boolean}.
 * <p>
 * The conversion rules are as follows:
 * <ul>
 * <li>A numeric value of 0 is treated as {@code false}, while any other number is {@code true}.</li>
 * <li>For string values, the conversion is delegated to {@link BooleanKit#toBoolean(String)}.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanConverter extends AbstractConverter {

    /**
     * Constructs a new BooleanConverter. Utility class constructor for static access.
     */
    public BooleanConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final BooleanConverter INSTANCE = new BooleanConverter();
    @Serial
    private static final long serialVersionUID = 2852265810501L;

    /**
     * Internally converts the given value to a {@link Boolean}.
     *
     * @param targetClass The target class, which should be {@link Boolean}.
     * @param value       The value to be converted.
     * @return The converted {@link Boolean} object.
     */
    @Override
    protected Boolean convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Number) {
            // A value of 0 is false, other numbers are true.
            return 0 != ((Number) value).doubleValue();
        }
        // For other types, convert to a string and then to a boolean.
        return BooleanKit.toBoolean(convertToString(value));
    }

}
