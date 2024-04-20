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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Boolean类型相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanKit {

    /**
     * 给定类是否为Boolean或者boolean
     *
     * @param clazz 类
     * @return 是否为Boolean或者boolean
     */
    public static boolean isBoolean(Class<?> clazz) {
        return (clazz == Boolean.class || clazz == boolean.class);
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
     * @param value 被检查的Boolean值
     * @return 当值为true且非null时返回{@code true}
     */
    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
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
     * @param value 被检查的Boolean值
     * @return 当值为true且非null时返回{@code true}
     */
    public static boolean isFalse(Boolean value) {
        return Boolean.FALSE.equals(value);
    }

    /**
     * 转换字符串为boolean值
     *
     * @param text 字符串
     * @return boolean值
     */
    public static boolean toBoolean(String text) {
        if (StringKit.isNotBlank(text)) {
            text = text.trim().toLowerCase();
            return ArrayKit.contains(Normal.TRUE_ARRAY, text);
        }
        return false;
    }

    /**
     * 转换字符串为boolean值
     * 如果为["true", "yes", "y", "t", "ok", "1", "on", "是", "对", "真", "對", "√"]，返回{@code true}<br>
     * 如果为["false", "no", "n", "f", "0", "off", "否", "错", "假", "錯", "×"]，返回{@code false}<br>
     * 其他情况返回{@code null}
     *
     * @param text 字符串
     * @return boolean值
     */
    public static Boolean toBooleanObject(String text) {
        if (StringKit.isNotBlank(text)) {
            text = text.trim().toLowerCase();
            if (ArrayKit.contains(Normal.TRUE_ARRAY, text)) {
                return Boolean.TRUE;
            } else if (ArrayKit.contains(Normal.FALSE_ARRAY, text)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /**
     * boolean值转为int
     *
     * @param value Boolean值
     * @return int值
     */
    public static int toInt(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * boolean值转为Integer
     *
     * @param value Boolean值
     * @return Integer值
     */
    public static Integer toInteger(boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为char
     *
     * @param value Boolean值
     * @return char值
     */
    public static char toChar(boolean value) {
        return (char) toInt(value);
    }

    /**
     * boolean值转为Character
     *
     * @param value Boolean值
     * @return Character值
     */
    public static Character toCharacter(boolean value) {
        return toChar(value);
    }

    /**
     * boolean值转为byte
     *
     * @param value Boolean值
     * @return byte值
     */
    public static byte toByte(boolean value) {
        return (byte) toInt(value);
    }

    /**
     * boolean值转为Byte
     *
     * @param value Boolean值
     * @return Byte值
     */
    public static Byte toByteObject(boolean value) {
        return toByte(value);
    }

    /**
     * boolean值转为long
     *
     * @param value Boolean值
     * @return long值
     */
    public static long toLong(boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为Long
     *
     * @param value Boolean值
     * @return Long值
     */
    public static Long toLongObject(boolean value) {
        return toLong(value);
    }

    /**
     * boolean值转为short
     *
     * @param value Boolean值
     * @return short值
     */
    public static short toShort(boolean value) {
        return (short) toInt(value);
    }

    /**
     * boolean值转为Short
     *
     * @param value Boolean值
     * @return Short值
     */
    public static Short toShortObject(boolean value) {
        return toShort(value);
    }

    /**
     * boolean值转为float
     *
     * @param value Boolean值
     * @return float值
     */
    public static float toFloat(boolean value) {
        return (float) toInt(value);
    }

    /**
     * boolean值转为Float
     *
     * @param value Boolean值
     * @return float值
     */
    public static Float toFloatObject(boolean value) {
        return toFloat(value);
    }

    /**
     * boolean值转为double
     *
     * @param value Boolean值
     * @return double值
     */
    public static double toDouble(boolean value) {
        return toInt(value);
    }

    /**
     * boolean值转为double
     *
     * @param value Boolean值
     * @return double值
     */
    public static Double toDoubleObject(boolean value) {
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
     * @param value Boolean值
     * @return {@code 'true'}, {@code 'false'}
     */
    public static String toStringTrueFalse(boolean value) {
        return toString(value, "true", "false");
    }

    /**
     * 将boolean转换为字符串 {@code 'on'} 或者 {@code 'off'}.
     *
     * <pre>
     *   BooleanKit.toStringOnOff(true)   = "on"
     *   BooleanKit.toStringOnOff(false)  = "off"
     * </pre>
     *
     * @param value Boolean值
     * @return {@code 'on'}, {@code 'off'}
     */
    public static String toStringOnOff(boolean value) {
        return toString(value, "on", "off");
    }

    /**
     * 将boolean转换为字符串 {@code 'yes'} 或者 {@code 'no'}.
     *
     * <pre>
     *   BooleanKit.toStringYesNo(true)   = "yes"
     *   BooleanKit.toStringYesNo(false)  = "no"
     * </pre>
     *
     * @param value Boolean值
     * @return {@code 'yes'}, {@code 'no'}
     */
    public static String toStringYesNo(boolean value) {
        return toString(value, "yes", "no");
    }

    /**
     * 将boolean转换为字符串
     *
     * <pre>
     *   BooleanKit.toString(true, "true", "false")   = "true"
     *   BooleanKit.toString(false, "true", "false")  = "false"
     * </pre>
     *
     * @param value       Boolean值
     * @param trueString  当值为 {@code true}时返回此字符串, 可能为 {@code null}
     * @param falseString 当值为 {@code false}时返回此字符串, 可能为 {@code null}
     * @return 结果值
     */
    public static String toString(boolean value, String trueString, String falseString) {
        return value ? trueString : falseString;
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
     * 对Boolean数组取与
     *
     * <pre>
     *   BooleanKit.and(true, true)         = true
     *   BooleanKit.and(false, false)       = false
     *   BooleanKit.and(true, false)        = false
     *   BooleanKit.and(true, true, false)  = false
     *   BooleanKit.and(true, true, true)   = true
     * </pre>
     *
     * @param array {@code Boolean}数组
     * @return 取与为真返回{@code true}
     */
    public static boolean and(boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        for (final boolean element : array) {
            if (false == element) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对Boolean数组取与
     *
     * <pre>
     *   BooleanKit.and(Boolean.TRUE, Boolean.TRUE)                 = Boolean.TRUE
     *   BooleanKit.and(Boolean.FALSE, Boolean.FALSE)               = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanKit.and(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE) = Boolean.FALSE
     *   BooleanKit.and(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)  = Boolean.FALSE
     * </pre>
     *
     * @param array {@code Boolean}数组
     * @return 取与为真返回{@code true}
     */
    public static Boolean and(final Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        final boolean[] primitive = Convert.convert(boolean[].class, array);
        return and(primitive);
    }

    /**
     * 对Boolean数组取或
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
     * @param array {@code Boolean}数组
     * @return 取或为真返回{@code true}
     */
    public static boolean or(boolean... array) {
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
     * 对Boolean数组取或
     *
     * <pre>
     *   BooleanKit.or(Boolean.TRUE, Boolean.TRUE)                  = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE)                = Boolean.FALSE
     *   BooleanKit.or(Boolean.TRUE, Boolean.FALSE)                 = Boolean.TRUE
     *   BooleanKit.or(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)    = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE)  = Boolean.TRUE
     *   BooleanKit.or(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)   = Boolean.TRUE
     *   BooleanKit.or(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE) = Boolean.FALSE
     * </pre>
     *
     * @param array {@code Boolean}数组
     * @return 取或为真返回{@code true}
     */
    public static Boolean or(Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        final boolean[] primitive = Convert.convert(boolean[].class, array);
        return Boolean.valueOf(or(primitive));
    }

    /**
     * 对Boolean数组取异或
     *
     * <pre>
     *   BooleanKit.xor(true, true)   = false
     *   BooleanKit.xor(false, false) = false
     *   BooleanKit.xor(true, false)  = true
     *   BooleanKit.xor(true, true)   = false
     *   BooleanKit.xor(false, false) = false
     *   BooleanKit.xor(true, false)  = true
     * </pre>
     *
     * @param array {@code boolean}数组
     * @return 如果异或计算为true返回 {@code true}
     */
    public static boolean xor(boolean... array) {
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
     *   BooleanKit.xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE })   = Boolean.FALSE
     *   BooleanKit.xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE }) = Boolean.FALSE
     *   BooleanKit.xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE })  = Boolean.TRUE
     * </pre>
     *
     * @param array {@code Boolean} 数组
     * @return 异或为真取{@code true}
     */
    public static Boolean xor(Boolean... array) {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException("The Array must not be empty !");
        }
        final boolean[] primitive = Convert.convert(boolean[].class, array);
        return Boolean.valueOf(xor(primitive));
    }

    /**
     * 取相反值
     *
     * @param value Boolean值
     * @return 相反的Boolean值
     */
    public static Boolean negate(Boolean value) {
        if (null == value) {
            return null;
        }
        return value ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * 取相反值
     *
     * @param value Boolean值
     * @return 相反的Boolean值
     */
    public static boolean negate(boolean value) {
        return !value;
    }

    /**
     * 比较两个{@code boolean}值
     *
     * @param x 第一个要比较的{@code boolean}
     * @param y 第二个要比较的{@code boolean}
     * @return 值{@code 0} if {@code x == y};
     * 小于{@code !x && y}的值;如果{@code x && !y}
     */
    public static int compare(boolean x, boolean y) {
        if (x == y) {
            return 0;
        }
        return x ? 1 : -1;
    }

}
