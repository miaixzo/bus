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
 * INT RANGE 校验
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IntRangeMatcher implements Matcher<Object, IntRange> {

    private static Set<Class<?>> NumberTypes = new HashSet<>();

    static {
        NumberTypes.add(Integer.class);
        NumberTypes.add(Long.class);
        NumberTypes.add(Double.class);
        NumberTypes.add(Float.class);
        NumberTypes.add(int.class);
        NumberTypes.add(long.class);
        NumberTypes.add(double.class);
        NumberTypes.add(float.class);
        NumberTypes.add(BigDecimal.class);
        NumberTypes.add(BigInteger.class);
    }

    @Override
    public boolean on(Object object, IntRange annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return true;
        }
        BigDecimal num;
        if (object instanceof String) {
            num = MathKit.add((String) object);
        } else if (NumberTypes.contains(object.getClass())) {
            String numString = String.valueOf(object);
            num = MathKit.add(numString);
        } else {
            throw new IllegalArgumentException("不支持的数字格式:" + object.toString());
        }
        BigDecimal max = new BigDecimal(annotation.max());
        BigDecimal min = new BigDecimal(annotation.min());

        return max.compareTo(num) >= 0 && min.compareTo(num) <= 0;
    }

}
