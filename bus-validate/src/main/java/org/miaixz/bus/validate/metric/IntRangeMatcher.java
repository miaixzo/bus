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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.IntRange;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for the {@link IntRange} annotation, checking if a numeric value is within a specified integer range.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IntRangeMatcher implements Matcher<Object, IntRange> {

    /**
     * A set of supported numeric types for validation.
     */
    private static final Set<Class<?>> NUMBER_TYPES = new HashSet<>();

    static {
        NUMBER_TYPES.add(Integer.class);
        NUMBER_TYPES.add(Long.class);
        NUMBER_TYPES.add(Double.class);
        NUMBER_TYPES.add(Float.class);
        NUMBER_TYPES.add(int.class);
        NUMBER_TYPES.add(long.class);
        NUMBER_TYPES.add(double.class);
        NUMBER_TYPES.add(float.class);
        NUMBER_TYPES.add(BigDecimal.class);
        NUMBER_TYPES.add(BigInteger.class);
    }

    /**
     * Checks if the given object, which can be a {@link Number} or a {@link String} representing a number, falls within
     * the range specified by the {@link IntRange} annotation.
     *
     * @param object     The object to validate.
     * @param annotation The {@link IntRange} annotation instance, providing the min and max range.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object is empty (null) or if its numeric value is within the specified range
     *         (inclusive), {@code false} otherwise.
     * @throws IllegalArgumentException if the object is not a supported number type or a parsable numeric string.
     */
    @Override
    public boolean on(Object object, IntRange annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return true;
        }
        BigDecimal num;
        if (object instanceof String) {
            num = MathKit.toBigDecimal((String) object);
        } else if (NUMBER_TYPES.contains(object.getClass())) {
            String numString = String.valueOf(object);
            num = MathKit.toBigDecimal(numString);
        } else {
            throw new IllegalArgumentException("Unsupported number format: " + object);
        }
        BigDecimal max = BigDecimal.valueOf(annotation.max());
        BigDecimal min = BigDecimal.valueOf(annotation.min());

        return max.compareTo(num) >= 0 && min.compareTo(num) <= 0;
    }

}
