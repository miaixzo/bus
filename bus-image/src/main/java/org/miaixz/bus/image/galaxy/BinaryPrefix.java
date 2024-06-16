/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.core.lang.Symbol;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BinaryPrefix {
    K(1000, 1),
    k(1000, 1),
    M(1000, 2),
    G(1000, 3),
    T(1000, 4),
    P(1000, 5),
    Ki(1024, 1),
    Mi(1024, 2),
    Gi(1024, 3),
    Ti(1024, 4),
    Pi(1024, 5);
    private final int base;
    private final int exponent;

    BinaryPrefix(int base, int exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public static long parse(String s) {
        int unitEnd = s.length();
        if (unitEnd > 0 && s.charAt(0) != Symbol.C_MINUS)
            try {
                if (s.charAt(unitEnd - 1) == 'B')
                    unitEnd--;
                int unitStart = unitEnd;
                while (unitStart > 0 && !Character.isDigit(s.charAt(unitStart - 1)))
                    unitStart--;
                String val = s.substring(0, unitStart);
                long unitSize = unitStart < unitEnd ? valueOf(s.substring(unitStart, unitEnd)).size() : 1L;
                return (s.indexOf(Symbol.C_DOT) >= 0)
                        ? (long) (Double.parseDouble(val) * unitSize)
                        : Long.parseLong(val) * unitSize;
            } catch (IllegalArgumentException e) {
            }
        throw new IllegalArgumentException(s);
    }

    public static String formatBinary(long size) {
        return format(size, Ki);
    }

    public static String formatDecimal(long size) {
        return format(size, k);
    }

    private static long size(int base, int exponent) {
        if (exponent == 0)
            return 1;
        long size = base;
        int i = exponent;
        while (--i > 0)
            size *= base;
        return size;
    }

    private static String format(long size, BinaryPrefix kiloPrefix) {
        if (size < 0)
            throw new IllegalArgumentException("size must be positive");
        StringBuilder sb = new StringBuilder();
        int base = kiloPrefix.base;
        long val = size;
        int exp = 0;
        while (val >= base && exp < 5) {
            val /= base;
            exp++;
        }
        long unitSize = size(base, exp);
        if (val * unitSize == size)
            sb.append(val);
        else
            sb.append(((double) size / unitSize));
        if (exp != 0)
            sb.append(BinaryPrefix.values()[kiloPrefix.ordinal() - 1 + exp]).append('B');
        return sb.toString();
    }

    public int getBase() {
        return base;
    }

    public int getExponent() {
        return exponent;
    }

    public long size() {
        return size(base, exponent);
    }

}
