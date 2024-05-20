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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.Normal;

import java.util.Set;

/**
 * Boolean类型相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanKit {

    /**
     * 表示为真的字符串
     */
    private static final Set<String> TRUE_SET = SetKit.of(Normal.TRUE_ARRAY);
    /**
     * 表示为假的字符串
     */
    private static final Set<String> FALSE_SET = SetKit.of(Normal.FALSE_ARRAY);

    /**
     * 取相反值
     *
     * @param bool Boolean值
     * @return 相反的Boolean值，如果传入 {@code null} 则返回 {@code null}
     */
    public static Boolean negate(final Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * 检查 {@code Boolean} 值是否为 {@code true}
     *
     * <pre>
     *   BooleanKit.isTrue(Boolean.TRUE)  = true
     *   BooleanKit.isTrue(Boolean.FALSE) = false
     *   BooleanKit.isTrue(null)          = false
     * </pre>
     *
     * @param bool 被检查的Boolean值
     * @return 当值非 {@code null} 且为 {@code true} 时返回 {@code true}
     */
    public static boolean isTrue(final Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    /**
     * 检查 {@code Boolean} 值是否为 {@code false}
     *
     * <pre>
     *   BooleanKit.isFalse(Boolean.TRUE)  = false
     *   BooleanKit.isFalse(Boolean.FALSE) = true
     *   BooleanKit.isFalse(null)          = false
     * </pre>
     *
     * @param bool 被检查的Boolean值
     * @return 当值非 {@code null} 且为 {@code false} 时返回 {@code true}
     */
    public static boolean isFalse(final Boolean bool) {
        return Boolean.FALSE.equals(bool);
    }

    /**
     * 取相反值
     *
     * @param bool boolean值
     * @return 相反的boolean值
     */
    public static boolean negate(final boolean bool) {
        return !bool;
    }

    /**
     * 转换字符串为boolean值
     * <p>该字符串 是否在 {@link #TRUE_SET} 中，存在则为 {@code true}，否则为 {@code false}</p>
     *
     * @param valueStr 字符串，不区分大小写，前后可以有空格 {@link String#trim()}
     * @return boolean值
     */
    public static boolean toBoolean(final String valueStr) {
        if (StringKit.isNotBlank(valueStr)) {
            return TRUE_SET.contains(valueStr.trim().toLowerCase());
        }
        return false;
    }

    /**
     * 转换字符串为Boolean值
     * 如果字符串在 {@link #TRUE_SET} 中，返回 {@link Boolean#TRUE}
     * 如果字符串在 {@link #FALSE_SET} 中，返回 {@link Boolean#FALSE}
     * 其他情况返回{@code null}
     *
     * @param valueStr 字符串，不区分大小写，前后可以有空格 {@link String#trim()}
     * @return the boolean
     */
    public static Boolean toBooleanObject(String valueStr) {
        if (StringKit.isNotBlank(valueStr)) {
            valueStr = valueStr.trim().toLowerCase();
            if (TRUE_SET.contains(valueStr)) {
                return Boolean.TRUE;
            } else if (FALSE_SET.contains(valueStr)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /**
     * boolean值转为int
     *
     * @param value boolean值
     * @return int值
     */
    public static int toInt(final boolean value) {
        return value ? 1 : 0;
    }

    /**
     * boolean值转为Integer
     *
     * @param value boolean值
     * @return Integer值
     */
    public static Integer toInteger(final boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为char
     *
     * @param value boolean值
     * @return char值
     */
    public static char toChar(final boolean value) {
        return (char) toInt(value);
    }

    /**
     * boolean值转为Character
     *
     * @param value boolean值
     * @return Character值
     */
    public static Character toCharacter(final boolean value) {
        return toChar(value);
    }

    /**
     * boolean值转为byte
     *
     * @param value boolean值
     * @return byte值
     */
    public static byte toByte(final boolean value) {
        return (byte) toInt(value);
    }

    /**
     * boolean值转为Byte
     *
     * @param value boolean值
     * @return Byte值
     */
    public static Byte toByteObject(final boolean value) {
        return toByte(value);
    }

    /**
     * boolean值转为long
     *
     * @param value boolean值
     * @return long值
     */
    public static long toLong(final boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为Long
     *
     * @param value boolean值
     * @return Long值
     */
    public static Long toLongObject(final boolean value) {
        return toLong(value);
    }

    /**
     * boolean值转为short
     *
     * @param value boolean值
     * @return short值
     */
    public static short toShort(final boolean value) {
        return (short) toInt(value);
    }

    /**
     * boolean值转为Short
     *
     * @param value boolean值
     * @return Short值
     */
    public static Short toShortObject(final boolean value) {
        return toShort(value);
    }

    /**
     * boolean值转为float
     *
     * @param value boolean值
     * @return float值
     */
    public static float toFloat(final boolean value) {
        return (float) toInt(value);
    }

    /**
     * boolean值转为Float
     *
     * @param value boolean值
     * @return Float值
     */
    public static Float toFloatObject(final boolean value) {
        return toFloat(value);
    }

    /**
     * boolean值转为double
     *
     * @param value boolean值
     * @return double值
     */
    public static double toDouble(final boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为Double
     *
     * @param value boolean值
     * @return Double值
     */
    public static Double toDoubleObject(final boolean value) {
        return toDouble(value);
    }

    /**
     * 将boolean转换为字符串 {@code 'true'} 或者 {@code 'false'}.
     *
     * <pre>
     *   BooleanKit.toStringTrueFalse(true)   = "true"
     *   BooleanKit.toStringTrueFalse(false)  = "false"
     * </pre>
     *
     * @param bool boolean值
     * @return {@code 'true'}, {@code 'false'}
     */
    public static String toStringTrueFalse(final boolean bool) {
        return toString(bool, "true", "false");
    }

    /**
     * 将boolean转换为字符串 {@code 'on'} 或者 {@code 'off'}.
     *
     * <pre>
     *   BooleanKit.toStringOnOff(true)   = "on"
     *   BooleanKit.toStringOnOff(false)  = "off"
     * </pre>
     *
     * @param bool boolean值
     * @return {@code 'on'}, {@code 'off'}
     */
    public static String toStringOnOff(final boolean bool) {
        return toString(bool, "on", "off");
    }

    /**
     * 将boolean转换为字符串 {@code 'yes'} 或者 {@code 'no'}.
     *
     * <pre>
     *   BooleanKit.toStringYesNo(true)   = "yes"
     *   BooleanKit.toStringYesNo(false)  = "no"
     * </pre>
     *
     * @param bool boolean值
     * @return {@code 'yes'}, {@code 'no'}
     */
    public static String toStringYesNo(final boolean bool) {
        return toString(bool, "yes", "no");
    }

    /**
     * 将boolean转换为字符串
     *
     * <pre>
     *   BooleanKit.toString(true, "true", "false")   = "true"
     *   BooleanKit.toString(false, "true", "false")  = "false"
     * </pre>
     *
     * @param bool        boolean值
     * @param trueString  当值为 {@code true}时返回此字符串, 可能为 {@code null}
     * @param falseString 当值为 {@code false}时返回此字符串, 可能为 {@code null}
     * @return 结果值
     */
    public static String toString(final boolean bool, final String trueString, final String falseString) {
        return bool ? trueString : falseString;
    }

    /**
     * 将boolean转换为字符串
     *
     * <pre>
     *   BooleanKit.toString(true, "true", "false", null) = "true"
     *   BooleanKit.toString(false, "true", "false", null) = "false"
     *   BooleanKit.toString(null, "true", "false", null) = null
     * </pre>
     *
     * @param bool        Boolean值
     * @param trueString  当值为 {@code true}时返回此字符串, 可能为 {@code null}
     * @param falseString 当值为 {@code false}时返回此字符串, 可能为 {@code null}
     * @param nullString  当值为 {@code null}时返回此字符串, 可能为 {@code null}
     * @return 结果值
     */
    public static String toString(final Boolean bool, final String trueString, final String falseString, final String nullString) {
        if (bool == null) {
            return nullString;
        }
        return bool ? trueString : falseString;
    }

    /**
     * boolean数组所有元素相 与 的结果
     *
     * <pre>
     *   BooleanKit.and(true, true)         = true
     *   BooleanKit.and(false, false)       = false
     *   BooleanKit.and(true, false)        = false
     *   BooleanKit.and(true, true, false)  = false
     *   BooleanKit.and(true, true, true)   = true
     * </pre>
     *
     * @param array {@code boolean}数组
     * @return 数组所有元素相 与 的结果
     * @throws IllegalArgumentException 如果数组为空
     */
    public static boolean and(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        for (final boolean element : array) {
            if (!element) {
                return false;
            }
        }
        return true;
    }

    /**
     * Boolean数组所有元素相 与 的结果
     * <p>注意：{@code null} 元素 被当作 {@code true}</p>
     *
     * <pre>
     *   BooleanKit.and(Boolean.TRUE, Boolean.TRUE)                 = Boolean.TRUE
     *   BooleanKit.and(Boolean.FALSE, Boolean.FALSE)               = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanKit.and(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE) = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)  = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, null)                         = Boolean.TRUE
     * </pre>
     *
     * @param array {@code Boolean}数组
     * @return 数组所有元素相 与 的结果
     * @throws IllegalArgumentException 如果数组为空
     */
    public static Boolean andOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        for (final Boolean b : array) {
            if (!isTrue(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * boolean数组所有元素 或 的结果
     *
     * <pre>
     *   BooleanKit.or(true, true)          = true
     *   BooleanKit.or(false, false)        = false
     *   BooleanKit.or(true, false)         = true
     *   BooleanKit.or(true, true, false)   = true
     *   BooleanKit.or(true, true, true)    = true
     *   BooleanKit.or(false, false, false) = false
     * </pre>
     *
     * @param array {@code boolean}数组
     * @return 数组所有元素 或 的结果
     * @throws IllegalArgumentException 如果数组为空
     */
    public static boolean or(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        for (final boolean element : array) {
            if (element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Boolean数组所有元素 或 的结果
     * <p>注意：{@code null} 元素 被当作 {@code false}</p>
     *
     * <pre>
     *   BooleanKit.or(Boolean.TRUE, Boolean.TRUE)                  = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanKit.or(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     *   BooleanKit.or(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE)  = Boolean.TRUE
     *   BooleanKit.or(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     *   BooleanKit.or(Boolean.FALSE, null)                         = Boolean.FALSE
     * </pre>
     *
     * @param array {@code Boolean}数组
     * @return 数组所有元素 或 的结果
     * @throws IllegalArgumentException 如果数组为空
     */
    public static Boolean orOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        for (final Boolean b : array) {
            if (isTrue(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对boolean数组取异或
     *
     * <pre>
     *   BooleanKit.xor(true, true)   = false
     *   BooleanKit.xor(false, false) = false
     *   BooleanKit.xor(true, false)  = true
     *   BooleanKit.xor(true, true, true)   = true
     *   BooleanKit.xor(false, false, false) = false
     *   BooleanKit.xor(true, true, false)  = false
     *   BooleanKit.xor(true, false, false)  = true
     * </pre>
     *
     * @param array {@code boolean}数组
     * @return 如果异或计算为true返回 {@code true}
     * @throws IllegalArgumentException 如果数组为空
     */
    public static boolean xor(final boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty");
        }

        boolean result = false;
        for (final boolean element : array) {
            result ^= element;
        }

        return result;
    }

    /**
     * 对Boolean数组取异或
     *
     * <pre>
     *   BooleanKit.xor(Boolean.TRUE, Boolean.TRUE)                  = Boolean.FALSE
     *   BooleanKit.xor(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanKit.xor(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     *   BooleanKit.xor(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     *   BooleanKit.xor(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     *   BooleanKit.xor(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE)   = Boolean.FALSE
     *   BooleanKit.xor(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE)  = Boolean.TRUE
     * </pre>
     *
     * @param array {@code Boolean} 数组
     * @return 异或为真取 {@code true}
     * @throws IllegalArgumentException 如果数组为空
     * @see #xor(boolean...)
     */
    public static Boolean xorOfWrap(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }

        boolean result = false;
        for (final Boolean element : array) {
            result ^= element;
        }

        return result;
    }

    /**
     * 给定类是否为Boolean或者boolean
     *
     * @param clazz 类
     * @return 是否为Boolean或者boolean
     */
    public static boolean isBoolean(final Class<?> clazz) {
        return (clazz == Boolean.class || clazz == boolean.class);
    }

}
