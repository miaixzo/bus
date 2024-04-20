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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.math.Arrange;
import org.miaixz.bus.core.math.Combine;
import org.miaixz.bus.core.math.Formula;
import org.miaixz.bus.core.math.Money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数学计算
 * 计量标准
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MathKit {

    /**
     * 默认除法运算精度
     */
    private static final int DEFAULT_DIV_SCALE = 10;
    /**
     * bytes 长度
     */
    private static long bytes;

    /**
     * 构造函数
     *
     * @param bytes 长度
     */
    private MathKit(long bytes) {
        MathKit.bytes = bytes;
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 和
     */
    public static double add(float v1, float v2) {
        return add(Float.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 和
     */
    public static double add(float v1, double v2) {
        return add(Float.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 和
     */
    public static double add(double v1, float v2) {
        return add(Double.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 和
     */
    public static double add(double v1, double v2) {
        return add(Double.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 和
     */
    public static BigDecimal add(Number v1, Number v2) {
        return add(new Number[]{v1, v2});
    }

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被加值
     * @return 和
     */
    public static BigDecimal add(Number... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        Number value = args[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.add(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被加值
     * @return 和
     */
    public static BigDecimal add(String... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        String value = args[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : new BigDecimal(value);
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.add(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被加值
     * @return 和
     */
    public static BigDecimal add(BigDecimal... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = args[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.add(value);
            }
        }
        return result;
    }

    /**
     * 提供精确的减法运算
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 差
     */
    public static double sub(float v1, float v2) {
        return sub(Float.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的减法运算
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 差
     */
    public static double sub(float v1, double v2) {
        return sub(Float.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的减法运算
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 差
     */
    public static double sub(double v1, float v2) {
        return sub(Double.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的减法运算
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 差
     */
    public static double sub(double v1, double v2) {
        return sub(Double.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 差
     */
    public static BigDecimal sub(Number v1, Number v2) {
        return sub(new Number[]{v1, v2});
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被减值
     * @return 差
     */
    public static BigDecimal sub(Number... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        Number value = args[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.subtract(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被减值
     * @return 差
     */
    public static BigDecimal sub(String... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        String value = args[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value);
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.subtract(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被减值
     * @return 差
     */
    public static BigDecimal sub(BigDecimal... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = args[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.subtract(value);
            }
        }
        return result;
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static double mul(float v1, float v2) {
        return mul(Float.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static double mul(float v1, double v2) {
        return mul(Float.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static double mul(double v1, float v2) {
        return mul(Double.toString(v1), Float.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static double mul(double v1, double v2) {
        return mul(Double.toString(v1), Double.toString(v2)).doubleValue();
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static BigDecimal mul(Number v1, Number v2) {
        return mul(new Number[]{v1, v2});
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(Number... args) {
        if (ArrayKit.isEmpty(args)) {
            return BigDecimal.ZERO;
        }

        Number value = args[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < args.length; i++) {
            value = args[i];
            if (null != value) {
                result = result.multiply(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * 提供精确的乘法运算
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 积
     */
    public static BigDecimal mul(String v1, String v2) {
        return mul(new BigDecimal(v1), new BigDecimal(v2));
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(String... args) {
        if (ArrayKit.isEmpty(args) || ArrayKit.hasNull(args)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = new BigDecimal(args[0]);
        for (int i = 1; i < args.length; i++) {
            result = result.multiply(new BigDecimal(args[i]));
        }

        return result;
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param args 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(BigDecimal... args) {
        if (ArrayKit.isEmpty(args) || ArrayKit.hasNull(args)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = args[0];
        for (int i = 1; i < args.length; i++) {
            result = result.multiply(args[i]);
        }
        return result;
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(float v1, float v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(float v1, double v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, float v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(Number v1, Number v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2) {
        return div(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static double div(float v1, float v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static double div(float v1, double v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static double div(double v1, float v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static BigDecimal div(Number v1, Number v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度,如果为负值,取绝对值
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2, int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static double div(float v1, float v2, int scale, RoundingMode roundingMode) {
        return div(Float.toString(v1), Float.toString(v2), scale, roundingMode).doubleValue();
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static double div(float v1, double v2, int scale, RoundingMode roundingMode) {
        return div(Float.toString(v1), Double.toString(v2), scale, roundingMode).doubleValue();
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static double div(double v1, float v2, int scale, RoundingMode roundingMode) {
        return div(Double.toString(v1), Float.toString(v2), scale, roundingMode).doubleValue();
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale, RoundingMode roundingMode) {
        return div(Double.toString(v1), Double.toString(v2), scale, roundingMode).doubleValue();
    }


    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static BigDecimal div(Number v1, Number v2, int scale, RoundingMode roundingMode) {
        if (v1 instanceof BigDecimal && v2 instanceof BigDecimal) {
            return div((BigDecimal) v1, (BigDecimal) v2, scale, roundingMode);
        }
        return div(StringKit.toStringOrNull(v1), StringKit.toStringOrNull(v2), scale, roundingMode);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2, int scale, RoundingMode roundingMode) {
        return div(new BigDecimal(v1), new BigDecimal(v2), scale, roundingMode);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度,如果为负值,取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale, RoundingMode roundingMode) {
        Assert.notNull(v2, "Divisor must be not null !");
        if (null == v1) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = -scale;
        }
        return v1.divide(v2, scale, roundingMode);
    }

    /**
     * 补充Math.ceilDiv() JDK8中添加了和Math.floorDiv()但却没有ceilDiv()
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static int ceilDiv(int v1, int v2) {
        return (int) Math.ceil((double) v1 / v2);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =  123.46
     *
     * @param v     值
     * @param scale 保留小数位数
     * @return 新值
     */
    public static BigDecimal round(double v, int scale) {
        return round(v, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =  123.46
     *
     * @param v     值
     * @param scale 保留小数位数
     * @return 新值
     */
    public static String roundString(double v, int scale) {
        return round(v, scale).toPlainString();
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =  123.46
     *
     * @param number 数字值的字符串表现形式
     * @param scale  保留小数位数
     * @return 新值
     */
    public static BigDecimal round(String number, int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =  123.46
     *
     * @param number 数字值
     * @param scale  保留小数位数
     * @return 新值
     */
    public static BigDecimal round(BigDecimal number, int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =  123.46
     *
     * @param number 数字值的字符串表现形式
     * @param scale  保留小数位数
     * @return 新值
     */
    public static String roundString(String number, int scale) {
        return round(number, scale).toPlainString();
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =  123.4567
     *
     * @param v            值
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static BigDecimal round(double v, int scale, RoundingMode roundingMode) {
        return round(Double.toString(v), scale, roundingMode);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =  123.4567
     *
     * @param v            值
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundString(double v, int scale, RoundingMode roundingMode) {
        return round(v, scale, roundingMode).toPlainString();
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =  123.4567
     *
     * @param number       数字值的字符串表现形式
     * @param scale        保留小数位数,如果传入小于0,则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode},如果传入null则默认四舍五入
     * @return 新值
     */
    public static BigDecimal round(String number, int scale, RoundingMode roundingMode) {
        Assert.notBlank(number);
        if (scale < 0) {
            scale = 0;
        }
        return round(toBigDecimal(number), scale, roundingMode);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =  123.4567
     *
     * @param number       数字值
     * @param scale        保留小数位数,如果传入小于0,则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode},如果传入null则默认四舍五入
     * @return 新值
     */
    public static BigDecimal round(BigDecimal number, int scale, RoundingMode roundingMode) {
        if (null == number) {
            number = BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = 0;
        }
        if (null == roundingMode) {
            roundingMode = RoundingMode.HALF_UP;
        }

        return number.setScale(scale, roundingMode);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =  123.4567
     *
     * @param number       数字值的字符串表现形式
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundString(String number, int scale, RoundingMode roundingMode) {
        return round(number, scale, roundingMode).toPlainString();
    }

    /**
     * 四舍六入五成双计算法
     * <p>
     * 四舍六入五成双是一种比较精确比较科学的计数保留法,是一种数字修约规则
     * </p>
     *
     * <pre>
     * 算法规则:
     * 四舍六入五考虑,
     * 五后非零就进一,
     * 五后皆零看奇偶,
     * 五前为偶应舍去,
     * 五前为奇要进一
     * </pre>
     *
     * @param number 需要科学计算的数据
     * @param scale  保留的小数位
     * @return 结果
     */
    public static BigDecimal roundHalfEven(Number number, int scale) {
        return roundHalfEven(toBigDecimal(number), scale);
    }

    /**
     * 四舍六入五成双计算法
     * <p>
     * 四舍六入五成双是一种比较精确比较科学的计数保留法,是一种数字修约规则
     * </p>
     *
     * <pre>
     * 算法规则:
     * 四舍六入五考虑,
     * 五后非零就进一,
     * 五后皆零看奇偶,
     * 五前为偶应舍去,
     * 五前为奇要进一
     * </pre>
     *
     * @param value 需要科学计算的数据
     * @param scale 保留的小数位
     * @return 结果
     */
    public static BigDecimal roundHalfEven(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.HALF_EVEN);
    }

    /**
     * 保留固定小数位数,舍去多余位数
     *
     * @param number 需要科学计算的数据
     * @param scale  保留的小数位
     * @return 结果
     */
    public static BigDecimal roundDown(Number number, int scale) {
        return roundDown(toBigDecimal(number), scale);
    }

    /**
     * 保留固定小数位数,舍去多余位数
     *
     * @param value 需要科学计算的数据
     * @param scale 保留的小数位
     * @return 结果
     */
    public static BigDecimal roundDown(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.DOWN);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度 0 表示如果位数不足则以 0 填充,# 表示只要有可能就把数字拉上这个位置
     *                <ul>
     *                <li>0 =  取一位整数</li>
     *                <li>0.00 =  取一位整数和两位小数</li>
     *                <li>00.000 =  取两位整数和三位小数</li>
     *                <li># =  取所有整数部分</li>
     *                <li>#.##% =  以百分比方式计数,并取两位小数</li>
     *                <li>#.#####E0 =  显示为科学计数法,并取五位小数</li>
     *                <li>,### =  每三位以逗号进行分隔,例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =  将格式嵌入文本</li>
     *                </ul>
     * @param value   值
     * @return 格式化后的值
     */
    public static String decimalFormat(String pattern, double value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度 0 表示如果位数不足则以 0 填充,# 表示只要有可能就把数字拉上这个位置
     *                <ul>
     *                <li>0 =  取一位整数</li>
     *                <li>0.00 =  取一位整数和两位小数</li>
     *                <li>00.000 =  取两位整数和三位小数</li>
     *                <li># =  取所有整数部分</li>
     *                <li>#.##% =  以百分比方式计数,并取两位小数</li>
     *                <li>#.#####E0 =  显示为科学计数法,并取五位小数</li>
     *                <li>,### =  每三位以逗号进行分隔,例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =  将格式嵌入文本</li>
     *                </ul>
     * @param value   值
     * @return 格式化后的值
     */
    public static String decimalFormat(String pattern, long value) {
        Assert.isTrue(isValid(value), "value is NaN or Infinite!");
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度
     *                0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置
     *                <ul>
     *                <li>0 =》 取一位整数</li>
     *                <li>0.00 =》 取一位整数和两位小数</li>
     *                <li>00.000 =》 取两位整数和三位小数</li>
     *                <li># =》 取所有整数部分</li>
     *                <li>#.##% =》 以百分比方式计数，并取两位小数</li>
     *                <li>#.#####E0 =》 显示为科学计数法，并取五位小数</li>
     *                <li>,### =》 每三位以逗号进行分隔，例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =》 将格式嵌入文本</li>
     *                </ul>
     * @param value   值，支持BigDecimal、BigInteger、Number等类型
     * @return 格式化后的值
     */
    public static String decimalFormat(String pattern, Object value) {
        return decimalFormat(pattern, value, null);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern      格式 格式中主要以 # 和 0 两种占位符号来指定数字长度
     *                     0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置
     *                     <ul>
     *                     <li>0 =》 取一位整数</li>
     *                     <li>0.00 =》 取一位整数和两位小数</li>
     *                     <li>00.000 =》 取两位整数和三位小数</li>
     *                     <li># =》 取所有整数部分</li>
     *                     <li>#.##% =》 以百分比方式计数，并取两位小数</li>
     *                     <li>#.#####E0 =》 显示为科学计数法，并取五位小数</li>
     *                     <li>,### =》 每三位以逗号进行分隔，例如：299,792,458</li>
     *                     <li>光速大小为每秒,###米 =》 将格式嵌入文本</li>
     *                     </ul>
     * @param value        值，支持BigDecimal、BigInteger、Number等类型
     * @param roundingMode 保留小数的方式枚举
     * @return 格式化后的值
     */
    public static String decimalFormat(String pattern, Object value, RoundingMode roundingMode) {
        final DecimalFormat decimalFormat = new DecimalFormat(pattern);
        if (null != roundingMode) {
            decimalFormat.setRoundingMode(roundingMode);
        }
        return decimalFormat.format(value);
    }

    /**
     * 格式化金额输出,每三位用逗号分隔
     *
     * @param value 金额
     * @return 格式化后的值
     */
    public static String decimalFormatMoney(double value) {
        return decimalFormat(",##0.00", value);
    }

    /**
     * 格式化百分比,小数采用四舍五入方式
     *
     * @param number 值
     * @param scale  保留小数位数
     * @return 百分比
     */
    public static String formatPercent(double number, int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * 求百分比(带精度)(带百分号后缀)  (3,10,0) - 30%
     *
     * @param num   当前num
     * @param total 总长度
     * @param scale 精度(保留小数点后几位)
     * @return String 百分比(带百分号后缀)
     */
    public static String formatPercent(Number num, Number total, int scale) {
        return formatPercent(num.doubleValue() / total.doubleValue(), scale);
    }

    /**
     * 是否为数字
     *
     * @param text 字符串值
     * @return 是否为数字
     */
    public static boolean isNumber(CharSequence text) {
        if (StringKit.isBlank(text)) {
            return false;
        }
        final char[] chars = text.toString().toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = (chars[0] == Symbol.C_MINUS || chars[0] == Symbol.C_PLUS) ? 1 : 0;
        if (sz > start + 1 && chars[start] == Symbol.C_ZERO && !StringKit.contains(text, Symbol.C_DOT)) { // leading 0, skip if is a decimal number
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // text == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < Symbol.C_ZERO || chars[i] > Symbol.C_NINE) && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
            if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < Symbol.C_ZERO || chars[i] > Symbol.C_SEVEN) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= Symbol.C_ZERO && chars[i] <= Symbol.C_NINE) {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == Symbol.C_DOT) {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (false == foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == Symbol.C_PLUS || chars[i] == Symbol.C_MINUS) {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= Symbol.C_ZERO && chars[i] <= Symbol.C_NINE) {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == Symbol.C_DOT) {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return false == allowSigns && foundDigit;
    }

    /**
     * 判断字符串是否是整数
     * <ol>
     * 		<li>10进制, 不能包含前导零</li>
     * 		<li>8进制(以0开头)</li>
     * 		<li>16进制(以0x或者0X开头)</li>
     * </ol>
     *
     * @param text 校验的字符串, 只能含有 正负号、数字字符 和 {@literal X/x}
     * @return 是否为 {@link Integer}类型
     */
    public static boolean isInteger(String text) {
        if (false == isNumber(text)) {
            return false;
        }
        try {
            Integer.decode(text);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否是Long类型
     * <ol>
     * 		<li>10进制, 不能包含前导零</li>
     * 		<li>8进制(以0开头)</li>
     * 		<li>16进制(以0x或者0X开头)</li>
     * </ol>
     *
     * @param text 校验的字符串, 只能含有 正负号、数字字符、{@literal X/x} 和 后缀{@literal L/l}
     * @return 是否为 {@link Long}类型
     */
    public static boolean isLong(String text) {
        if (false == isNumber(text)) {
            return false;
        }
        final char lastChar = text.charAt(text.length() - 1);
        if (lastChar == 'l' || lastChar == 'L') {
            return true;
        }
        try {
            Long.decode(text);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否是浮点数
     *
     * @param text String
     * @return 是否为 {@link Double}类型
     */
    public static boolean isDouble(String text) {
        if (StringKit.isBlank(text)) {
            return false;
        }
        try {
            Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return text.contains(Symbol.DOT);
    }

    /**
     * 是否是质数(素数)
     * 质数表的质数又称素数 指整数在一个大于1的自然数中,除了1和此整数自身外,没法被其他自然数整除的数
     *
     * @param n 数字
     * @return 是否是质数
     */
    public static boolean isPrimes(int n) {
        Assert.isTrue(n > 1, "The number must be > 1");
        if (n <= 3) {
            return true;
        } else if ((n & 1) == 0) {
            // 快速排除偶数
            return false;
        }
        final int end = (int) Math.sqrt(n);
        for (int i = 3; i <= end; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成不重复随机数 根据给定的最小数字和最大数字,以及随机数的个数,产生指定的不重复的数组
     *
     * @param begin 最小数字(包含该数)
     * @param end   最大数字(不包含该数)
     * @param size  指定产生随机数的个数
     * @return 随机int数组
     */
    public static int[] generateRandomNumber(int begin, int end, int size) {
        return generateRandomNumber(begin, end, size, ArrayKit.range(begin, end));
    }

    /**
     * 生成不重复随机数 根据给定的最小数字和最大数字，以及随机数的个数，产生指定的不重复的数组
     *
     * @param begin 最小数字（包含该数）
     * @param end   最大数字（不包含该数）
     * @param size  指定产生随机数的个数
     * @param seed  种子，用于取随机数的int池
     * @return 随机int数组
     */
    public static int[] generateRandomNumber(int begin, int end, int size, int[] seed) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        // 加入逻辑判断，确保begin<end并且size不能大于该表示范围
        Assert.isTrue((end - begin) >= size, "Size is larger than range between begin and end!");
        Assert.isTrue(seed.length >= size, "Size is larger than seed size!");

        final int[] ranArr = new int[size];
        // 数量你可以自己定义。
        for (int i = 0; i < size; i++) {
            // 得到一个位置
            int j = RandomKit.randomInt(seed.length - i);
            // 得到那个位置的数值
            ranArr[i] = seed[j];
            // 将最后一个未用的数字放到这里
            seed[j] = seed[seed.length - 1 - i];
        }
        return ranArr;
    }

    /**
     * 生成不重复随机数 根据给定的最小数字和最大数字,以及随机数的个数,产生指定的不重复的数组
     *
     * @param begin 最小数字(包含该数)
     * @param end   最大数字(不包含该数)
     * @param size  指定产生随机数的个数
     * @return 随机int数组
     */
    public static Integer[] generateBySet(int begin, int end, int size) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        // 加入逻辑判断,确保begin<end并且size不能大于该表示范围
        if ((end - begin) < size) {
            throw new InternalException("Size is larger than range between begin and end!");
        }

        Set<Integer> set = new HashSet<>(size, 1);
        while (set.size() < size) {
            set.add(begin + RandomKit.randomInt(end - begin));
        }

        return set.toArray(new Integer[0]);
    }

    /**
     * 从0开始给定范围内的整数列表,步进为1
     *
     * @param stop 结束(包含)
     * @return 整数列表
     */
    public static int[] range(int stop) {
        return range(0, stop);
    }

    /**
     * 给定范围内的整数列表,步进为1
     *
     * @param start 开始(包含)
     * @param stop  结束(包含)
     * @return 整数列表
     */
    public static int[] range(int start, int stop) {
        return range(start, stop, 1);
    }

    /**
     * 给定范围内的整数列表
     *
     * @param start 开始(包含)
     * @param stop  结束(包含)
     * @param step  步进
     * @return 整数列表
     */
    public static int[] range(int start, int stop, int step) {
        if (start < stop) {
            step = Math.abs(step);
        } else if (start > stop) {
            step = -Math.abs(step);
        } else {// start == end
            return new int[]{start};
        }

        int size = Math.abs((stop - start) / step) + 1;
        int[] values = new int[size];
        int index = 0;
        for (int i = start; (step > 0) ? i <= stop : i >= stop; i += step) {
            values[index] = i;
            index++;
        }
        return values;
    }

    /**
     * 将给定范围内的整数添加到已有集合中,步进为1
     *
     * @param start  开始(包含)
     * @param stop   结束(包含)
     * @param values 集合
     * @return 集合
     */
    public static Collection<Integer> appendRange(int start, int stop, Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /**
     * 将给定范围内的整数添加到已有集合中
     *
     * @param start  开始(包含)
     * @param stop   结束(包含)
     * @param step   步进
     * @param values 集合
     * @return 集合
     */
    public static Collection<Integer> appendRange(int start, int stop, int step, Collection<Integer> values) {
        if (start < stop) {
            step = Math.abs(step);
        } else if (start > stop) {
            step = -Math.abs(step);
        } else {// start == end
            values.add(start);
            return values;
        }

        for (int i = start; (step > 0) ? i <= stop : i >= stop; i += step) {
            values.add(i);
        }
        return values;
    }

    /**
     * 计算阶乘
     * <p>
     * n! = n * (n-1) * ... * 2 * 1
     * </p>
     *
     * @param n 阶乘起始
     * @return 结果
     */
    public static BigInteger factorial(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }
        return factorial(n, BigInteger.ZERO);
    }

    /**
     * 计算范围阶乘
     * <p>
     * factorial(start, end) = start * (start - 1) * ... * (end + 1)
     * </p>
     *
     * @param start 阶乘起始（包含）
     * @param end   阶乘结束，必须小于起始（不包括）
     * @return 结果
     */
    public static BigInteger factorial(BigInteger start, BigInteger end) {
        if (start.compareTo(BigInteger.ZERO) < 0 || end.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException(StringKit.format("Factorial start and end both must be > 0, but got start={}, end={}", start, end));
        }

        if (start.equals(BigInteger.ZERO)) {
            start = BigInteger.ONE;
        }

        if (end.compareTo(BigInteger.ONE) < 0) {
            end = BigInteger.ONE;
        }

        BigInteger result = start;
        end = end.add(BigInteger.ONE);
        while (start.compareTo(end) > 0) {
            start = start.subtract(BigInteger.ONE);
            result = result.multiply(start);
        }
        return result;
    }

    /**
     * 计算阶乘
     * <p>
     * n! = n * (n-1) * ... * end
     * </p>
     *
     * @param start 阶乘起始
     * @param end   阶乘结束
     * @return 结果
     */
    public static long factorial(long start, long end) {
        if (start < end) {
            return 0L;
        }
        if (start == end) {
            return 1L;
        }
        return start * factorial(start - 1, end);
    }

    /**
     * 计算阶乘
     * <p>
     * n! = n * (n-1) * ... * 2 * 1
     * </p>
     *
     * @param n 阶乘起始
     * @return 结果
     */
    public static long factorial(long n) {
        return factorial(n, 1);
    }

    /**
     * 平方根算法
     * 推荐使用 {@link Math#sqrt(double)}
     *
     * @param x 值
     * @return 平方根
     */
    public static long sqrt(long x) {
        long y = 0;
        long b = (~Long.MAX_VALUE) >>> 1;
        while (b > 0) {
            if (x >= y + b) {
                x -= y + b;
                y >>= 1;
                y += b;
            } else {
                y >>= 1;
            }
            b >>= 2;
        }
        return y;
    }

    /**
     * 可以用于计算双色球、大乐透注数的方法
     * 比如大乐透35选5可以这样调用processMultiple(7,5); 就是数学中的：C75=7*6/2*1
     *
     * @param selectNum 选中小球个数
     * @param minNum    最少要选中多少个小球
     * @return 注数
     */
    public static int processMultiple(int selectNum, int minNum) {
        int result;
        result = mathSubnode(selectNum, minNum) / mathNode(selectNum - minNum);
        return result;
    }

    /**
     * 最大公约数
     *
     * @param m 第一个值
     * @param n 第二个值
     * @return 最大公约数
     */
    public static int divisor(int m, int n) {
        while (m % n != 0) {
            int temp = m % n;
            m = n;
            n = temp;
        }
        return n;
    }

    /**
     * 最小公倍数
     *
     * @param m 第一个值
     * @param n 第二个值
     * @return 最小公倍数
     */
    public static int multiple(int m, int n) {
        return m * n / divisor(m, n);
    }

    /**
     * 获得数字对应的二进制字符串
     *
     * @param number 数字
     * @return 二进制字符串
     */
    public static String getBinaryString(Number number) {
        if (number instanceof Long) {
            return Long.toBinaryString((Long) number);
        } else if (number instanceof Integer) {
            return Integer.toBinaryString((Integer) number);
        } else {
            return Long.toBinaryString(number.longValue());
        }
    }

    /**
     * 二进制转int
     *
     * @param binaryStr 二进制字符串
     * @return int
     */
    public static int binaryToInt(String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    /**
     * 二进制转long
     *
     * @param binaryStr 二进制字符串
     * @return long
     */
    public static long binaryToLong(String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Character#compare(char, char)
     */
    public static int compare(char x, char y) {
        return x - y;
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Double#compare(double, double)
     */
    public static int compare(double x, double y) {
        return Double.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Integer#compare(int, int)
     */
    public static int compare(int x, int y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Long#compare(long, long)
     */
    public static int compare(long x, long y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Short#compare(short, short)
     */
    public static int compare(short x, short y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Byte#compare(byte, byte)
     */
    public static int compare(byte x, byte y) {
        return x - y;
    }

    /**
     * 比较大小,参数1 &gt; 参数2 返回true
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否大于
     */
    public static boolean isGreater(BigDecimal bigNum1, BigDecimal bigNum2) {
        Assert.notNull(bigNum1);
        Assert.notNull(bigNum2);
        return bigNum1.compareTo(bigNum2) > 0;
    }

    /**
     * 比较大小,参数1 &gt;= 参数2 返回true
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否大于等于
     */
    public static boolean isGreaterOrEqual(BigDecimal bigNum1, BigDecimal bigNum2) {
        Assert.notNull(bigNum1);
        Assert.notNull(bigNum2);
        return bigNum1.compareTo(bigNum2) >= 0;
    }

    /**
     * 比较大小,参数1 &lt; 参数2 返回true
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否小于
     */
    public static boolean isLess(BigDecimal bigNum1, BigDecimal bigNum2) {
        Assert.notNull(bigNum1);
        Assert.notNull(bigNum2);
        return bigNum1.compareTo(bigNum2) < 0;
    }

    /**
     * 比较大小,参数1&lt;=参数2 返回true
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否小于等于
     */
    public static boolean isLessOrEqual(BigDecimal bigNum1, BigDecimal bigNum2) {
        Assert.notNull(bigNum1);
        Assert.notNull(bigNum2);
        return bigNum1.compareTo(bigNum2) <= 0;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value      值
     * @param minInclude 最小值（包含）
     * @param maxInclude 最大值（包含）
     * @return 经过检查后的值
     */
    public static boolean isIn(final BigDecimal value, final BigDecimal minInclude, final BigDecimal maxInclude) {
        Assert.notNull(value);
        Assert.notNull(minInclude);
        Assert.notNull(maxInclude);
        return isGreaterOrEqual(value, minInclude) && isLessOrEqual(value, maxInclude);
    }

    /**
     * 比较大小,值相等 返回true
     * 此方法通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等
     * 此方法判断值相等时忽略精度的,既0.00 == 0
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否相等
     */
    public static boolean equals(BigDecimal bigNum1, BigDecimal bigNum2) {
        return ObjectKit.equals(bigNum1, bigNum2);
    }

    /**
     * 比较大小，值相等 返回true
     * 此方法通过调用{@link Double#doubleToLongBits(double)}方法来判断是否相等
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return 是否相等
     */
    public static boolean equals(double num1, double num2) {
        return Double.doubleToLongBits(num1) == Double.doubleToLongBits(num2);
    }

    /**
     * 比较大小，值相等 返回true
     * 此方法通过调用{@link Float#floatToIntBits(float)}方法来判断是否相等
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return 是否相等
     */
    public static boolean equals(float num1, float num2) {
        return Float.floatToIntBits(num1) == Float.floatToIntBits(num2);
    }

    /**
     * 比较大小，值相等 返回true
     * 此方法修复传入long型数据由于没有本类型重载方法,导致数据精度丢失
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return 是否相等
     */
    public static boolean equals(long num1, long num2) {
        return num1 == num2;
    }

    /**
     * 比较两个字符是否相同
     *
     * @param c1         字符1
     * @param c2         字符2
     * @param ignoreCase 是否忽略大小写
     * @return 是否相同
     * @see CharsKit#equals(char, char, boolean)
     */
    public static boolean equals(char c1, char c2, boolean ignoreCase) {
        return CharsKit.equals(c1, c2, ignoreCase);
    }

    /**
     * 取最小值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayKit#min(Comparable[])
     */
    public static <T extends Comparable<? super T>> T min(T[] numberArray) {
        return ArrayKit.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param args 数字数组
     * @return 最小值
     * @see ArrayKit#min(long...)
     */
    public static long min(long... args) {
        return ArrayKit.min(args);
    }

    /**
     * 取最小值
     *
     * @param args 数字数组
     * @return 最小值
     * @see ArrayKit#min(int...)
     */
    public static int min(int... args) {
        return ArrayKit.min(args);
    }

    /**
     * 取最小值
     *
     * @param args 数字数组
     * @return 最小值
     * @see ArrayKit#min(short...)
     */
    public static short min(short... args) {
        return ArrayKit.min(args);
    }

    /**
     * 取最小值
     *
     * @param args 数字数组
     * @return 最小值
     * @see ArrayKit#min(double...)
     */
    public static double min(double... args) {
        return ArrayKit.min(args);
    }

    /**
     * 取最小值
     *
     * @param args 数字数组
     * @return 最小值
     * @see ArrayKit#min(float...)
     */
    public static float min(float... args) {
        return ArrayKit.min(args);
    }

    /**
     * 取最大值
     *
     * @param <T>  元素类型
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(Comparable[])
     */
    public static <T extends Comparable<? super T>> T max(T[] args) {
        return ArrayKit.max(args);
    }

    /**
     * 取最大值
     *
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(long...)
     */
    public static long max(long... args) {
        return ArrayKit.max(args);
    }

    /**
     * 取最大值
     *
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(int...)
     */
    public static int max(int... args) {
        return ArrayKit.max(args);
    }

    /**
     * 取最大值
     *
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(short...)
     */
    public static short max(short... args) {
        return ArrayKit.max(args);
    }

    /**
     * 取最大值
     *
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(double...)
     */
    public static double max(double... args) {
        return ArrayKit.max(args);
    }

    /**
     * 取最大值
     *
     * @param args 数字数组
     * @return 最大值
     * @see ArrayKit#max(float...)
     */
    public static float max(float... args) {
        return ArrayKit.max(args);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()},并去除尾小数点儿后多余的0
     *
     * @param number       A Number
     * @param defaultValue 如果number参数为{@code null},返回此默认值
     * @return A String.
     */
    public static String toString(Number number, String defaultValue) {
        return (null == number) ? defaultValue : toString(number);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()},并去除尾小数点儿后多余的0
     *
     * @param number A Number
     * @return A String.
     */
    public static String toString(Number number) {
        return toString(number, true);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()}或 {@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     *
     * @param number               A Number
     * @param isStripTrailingZeros 是否去除末尾多余0，例如5.0返回5
     * @return A String
     */
    public static String toString(Number number, boolean isStripTrailingZeros) {
        Assert.notNull(number, "Number is null !");

        // BigDecimal单独处理，使用非科学计数法
        if (number instanceof BigDecimal) {
            return toString((BigDecimal) number, isStripTrailingZeros);
        }

        Assert.isTrue(isValidNumber(number), "Number is non-finite!");
        // 去掉小数点儿后多余的0
        String string = number.toString();
        if (isStripTrailingZeros) {
            if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
                while (string.endsWith("0")) {
                    string = string.substring(0, string.length() - 1);
                }
                if (string.endsWith(".")) {
                    string = string.substring(0, string.length() - 1);
                }
            }
        }
        return string;
    }

    /**
     * {@link BigDecimal}数字转字符串
     * 调用{@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     *
     * @param bigDecimal A {@link BigDecimal}
     * @return A String.
     */
    public static String toString(BigDecimal bigDecimal) {
        return toString(bigDecimal, true);
    }

    /**
     * {@link BigDecimal} 数字转字符串
     * 调用{@link BigDecimal#toPlainString()}，可选去除尾小数点儿后多余的0
     *
     * @param bigDecimal           A {@link BigDecimal}
     * @param isStripTrailingZeros 是否去除末尾多余0，例如5.0返回5
     * @return A String
     */
    public static String toString(BigDecimal bigDecimal, boolean isStripTrailingZeros) {
        Assert.notNull(bigDecimal, "BigDecimal is null !");
        if (isStripTrailingZeros) {
            bigDecimal = bigDecimal.stripTrailingZeros();
        }
        return bigDecimal.toPlainString();
    }

    /**
     * 数字转{@link BigDecimal}
     *
     * @param number 数字
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(Number number) {
        if (null == number) {
            return BigDecimal.ZERO;
        }

        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof Long) {
            return new BigDecimal((Long) number);
        } else if (number instanceof Integer) {
            return new BigDecimal((Integer) number);
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        }

        return toBigDecimal(number.toString());
    }

    /**
     * 数字转{@link BigDecimal}
     *
     * @param number 数字
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(String number) {
        if (StringKit.isBlank(number)) {
            return BigDecimal.ZERO;
        }

        try {
            // 支持类似于 1,234.55 格式的数字
            final Number parseNumber = parseNumber(number);
            if (parseNumber instanceof BigDecimal) {
                return (BigDecimal) parseNumber;
            } else {
                return new BigDecimal(parseNumber.toString());
            }
        } catch (Exception ignore) {
            // 忽略解析错误
        }

        return new BigDecimal(number);
    }

    /**
     * 数字转{@link BigInteger}
     *
     * @param number 数字
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(Number number) {
        if (null == number) {
            return BigInteger.ZERO;
        }

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof Long) {
            return BigInteger.valueOf((Long) number);
        }

        return toBigInteger(number.longValue());
    }

    /**
     * 数字转{@link BigInteger}
     *
     * @param number 数字
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(String number) {
        return (null == number) ? BigInteger.ZERO : new BigInteger(number);
    }

    /**
     * 计算等份个数
     *
     * @param total 总数
     * @param part  每份的个数
     * @return 分成了几份
     */
    public static int count(int total, int part) {
        return (total % part == 0) ? (total / part) : (total / part + 1);
    }

    /**
     * 空转0
     *
     * @param decimal {@link BigDecimal},可以为{@code null}
     * @return {@link BigDecimal}参数为空时返回0的值
     */
    public static BigDecimal null2Zero(BigDecimal decimal) {
        return null == decimal ? BigDecimal.ZERO : decimal;
    }

    /**
     * 如果给定值为0,返回1,否则返回原值
     *
     * @param value 值
     * @return 1或非0值
     */
    public static int zero2One(int value) {
        return 0 == value ? 1 : value;
    }

    /**
     * 创建{@link BigInteger},支持16进制、10进制和8进制,如果传入空白串返回null
     * from Apache Common Lang
     *
     * @param text 数字字符串
     * @return {@link BigInteger}
     */
    public static BigInteger newBigInteger(String text) {
        text = StringKit.trimToNull(text);
        if (null == text) {
            return null;
        }

        int pos = 0; // 数字字符串位置
        int radix = 10;
        boolean negate = false; // 负数与否
        if (text.startsWith(Symbol.MINUS)) {
            negate = true;
            pos = 1;
        }
        if (text.startsWith("0x", pos) || text.startsWith("0X", pos)) {
            // hex
            radix = Normal._16;
            pos += 2;
        } else if (text.startsWith(Symbol.SHAPE, pos)) {
            // alternative hex (allowed by Long/Integer)
            radix = Normal._16;
            pos++;
        } else if (text.startsWith(Symbol.ZERO, pos) && text.length() > pos + 1) {
            // octal; so long as there are additional digits
            radix = 8;
            pos++;
        } // default is to treat as decimal

        if (pos > 0) {
            text = text.substring(pos);
        }
        final BigInteger value = new BigInteger(text, radix);
        return negate ? value.negate() : value;
    }

    /**
     * 判断两个数字是否相邻,例如1和2相邻,1和3不相邻
     * 判断方法为做差取绝对值判断是否为1
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相邻
     */
    public static boolean isBeside(long number1, long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 判断两个数字是否相邻,例如1和2相邻,1和3不相邻
     * 判断方法为做差取绝对值判断是否为1
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相邻
     */
    public static boolean isBeside(int number1, int number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 把给定的总数平均分成N份,返回每份的个数
     * 当除以分数有余数时每份+1
     *
     * @param total     总数
     * @param partCount 份数
     * @return 每份的个数
     */
    public static int partValue(int total, int partCount) {
        return partValue(total, partCount, true);
    }

    /**
     * 把给定的总数平均分成N份,返回每份的个数
     * 如果isPlusOneWhenHasRem为true,则当除以分数有余数时每份+1,否则丢弃余数部分
     *
     * @param total               总数
     * @param partCount           份数
     * @param isPlusOneWhenHasRem 在有余数时是否每份+1
     * @return 每份的个数
     */
    public static int partValue(int total, int partCount, boolean isPlusOneWhenHasRem) {
        int partValue = total / partCount;
        if (isPlusOneWhenHasRem && total % partCount > 0) {
            partValue++;
        }
        return partValue;
    }

    /**
     * 提供精确的幂运算
     *
     * @param number 底数
     * @param n      指数
     * @return 幂的积
     */
    public static BigDecimal pow(Number number, int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     * 提供精确的幂运算
     *
     * @param number 底数
     * @param n      指数
     * @return 幂的积
     */
    public static BigDecimal pow(BigDecimal number, int n) {
        return number.pow(n);
    }

    /**
     * 解析转换数字字符串为int型数字,规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、其它情况按照10进制转换
     * 4、空串返回0
     * 5、.123形式返回0(按照小于0的小数对待)
     * 6、123.56截取小数点之前的数字,忽略小数部分
     * </pre>
     *
     * @param number 数字,支持0x开头、0开头和普通十进制
     * @return int
     * @throws NumberFormatException 数字格式异常
     */
    public static int parseInt(String number) throws NumberFormatException {
        if (StringKit.isBlank(number)) {
            return 0;
        }

        if (StringKit.containsIgnoreCase(number, "E")) {
            // 科学计数法忽略支持，科学计数法一般用于表示非常小和非常大的数字，这类数字转换为int后精度丢失，没有意义
            throw new NumberFormatException(StringKit.format("Unsupported int format: [{}]", number));
        }

        if (StringKit.startWithIgnoreCase(number, "0x")) {
            // 0x04表示16进制数
            return Integer.parseInt(number.substring(2), Normal._16);
        }

        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return parseNumber(number).intValue();
        }
    }

    /**
     * 解析转换数字字符串为long型数字,规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、空串返回0
     * 4、其它情况按照10进制转换
     * </pre>
     *
     * @param number 数字,支持0x开头、0开头和普通十进制
     * @return long
     */
    public static long parseLong(String number) {
        if (StringKit.isBlank(number)) {
            return 0L;
        }

        if (number.startsWith("0x")) {
            // 0x04表示16进制数
            return Long.parseLong(number.substring(2), Normal._16);
        }

        try {
            return Long.parseLong(number);
        } catch (NumberFormatException e) {
            return parseNumber(number).longValue();
        }
    }

    /**
     * 解析转换数字字符串为long型数字，规则如下：
     *
     * <pre>
     * 1、0开头的忽略开头的0
     * 2、空串返回0
     * 3、其它情况按照10进制转换
     * 4、.123形式返回0.123（按照小于0的小数对待）
     * </pre>
     *
     * @param number 数字，支持0x开头、0开头和普通十进制
     * @return long
     */
    public static float parseFloat(String number) {
        if (StringKit.isBlank(number)) {
            return 0f;
        }

        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException e) {
            return parseNumber(number).floatValue();
        }
    }

    /**
     * 解析转换数字字符串为long型数字，规则如下：
     *
     * <pre>
     * 1、0开头的忽略开头的0
     * 2、空串返回0
     * 3、其它情况按照10进制转换
     * 4、.123形式返回0.123（按照小于0的小数对待）
     * </pre>
     *
     * @param number 数字，支持0x开头、0开头和普通十进制
     * @return long
     */
    public static double parseDouble(String number) {
        if (StringKit.isBlank(number)) {
            return 0D;
        }

        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return parseNumber(number).doubleValue();
        }
    }

    /**
     * 将指定字符串转换为{@link Number} 对象
     * 此方法不支持科学计数法
     *
     * @param number Number字符串
     * @return Number对象
     * @throws NumberFormatException 包装了{@link ParseException}，当给定的数字字符串无法解析时抛出
     */
    public static Number parseNumber(String number) throws NumberFormatException {
        if (StringKit.startWithIgnoreCase(number, "0x")) {
            // 0x04表示16进制数
            return Long.parseLong(number.substring(2), 16);
        }

        try {
            final NumberFormat format = NumberFormat.getInstance();
            if (format instanceof DecimalFormat) {
                // 当字符串数字超出double的长度时，会导致截断，此处使用BigDecimal接收
                ((DecimalFormat) format).setParseBigDecimal(true);
            }
            return format.parse(number);
        } catch (ParseException e) {
            final NumberFormatException nfe = new NumberFormatException(e.getMessage());
            nfe.initCause(e);
            throw nfe;
        }
    }

    /**
     * int值转byte数组,使用大端字节序(高位字节在前,低位字节在后)
     *
     * @param value 值
     * @return byte数组
     */
    public static byte[] toBytes(int value) {
        final byte[] result = new byte[4];

        result[0] = (byte) (value >> 24);
        result[1] = (byte) (value >> Normal._16);
        result[2] = (byte) (value >> 8);
        result[3] = (byte) (value /* >> 0 */);

        return result;
    }

    /**
     * byte数组转int,使用大端字节序(高位字节在前,低位字节在后)
     *
     * @param bytes 字节
     * @return int
     */
    public static int toInt(byte[] bytes) {
        return (bytes[0] & 0xff) << 24
                | (bytes[1] & 0xff) << Normal._16
                | (bytes[2] & 0xff) << 8
                | (bytes[3] & 0xff);
    }

    /**
     * 以无符号字节数组的形式返回传入值
     *
     * @param value 需要转换的值
     * @return 无符号bytes
     */
    public static byte[] toUnsignedByteArray(BigInteger value) {
        byte[] bytes = value.toByteArray();

        if (bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);

            return tmp;
        }

        return bytes;
    }

    /**
     * 以无符号字节数组的形式返回传入值
     *
     * @param length bytes长度
     * @param value  需要转换的值
     * @return 无符号bytes
     */
    public static byte[] toUnsignedByteArray(int length, BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }

        int start = bytes[0] == 0 ? 1 : 0;
        int count = bytes.length - start;

        if (count > length) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }

        byte[] tmp = new byte[length];
        System.arraycopy(bytes, start, tmp, tmp.length - count, count);
        return tmp;
    }

    /**
     * 无符号bytes转{@link BigInteger}
     *
     * @param buf buf 无符号bytes
     * @return {@link BigInteger}
     */
    public static BigInteger fromUnsignedByteArray(byte[] buf) {
        return new BigInteger(1, buf);
    }

    /**
     * 无符号bytes转{@link BigInteger}
     *
     * @param buf    无符号bytes
     * @param off    起始位置
     * @param length 长度
     * @return {@link BigInteger}
     */
    public static BigInteger fromUnsignedByteArray(byte[] buf, int off, int length) {
        byte[] mag = buf;
        if (off != 0 || length != buf.length) {
            mag = new byte[length];
            System.arraycopy(buf, off, mag, 0, length);
        }
        return new BigInteger(1, mag);
    }

    /**
     * 检查是否为有效的数字
     * 检查double否为无限大，或者Not a Number（NaN）
     *
     * @param number 被检查double
     * @return 检查结果
     */
    public static boolean isValid(double number) {
        return false == (Double.isNaN(number) || Double.isInfinite(number));
    }

    /**
     * 检查是否为有效的数字
     * 检查double否为无限大，或者Not a Number（NaN）
     *
     * @param number 被检查double
     * @return 检查结果
     */
    public static boolean isValid(float number) {
        return false == (Float.isNaN(number) || Float.isInfinite(number));
    }

    /**
     * 计算数学表达式的值，只支持加减乘除和取余
     *
     * <pre class="code">
     *   calculate("(0*1--3)-5/-4-(3*(-2.13))") -》 10.64
     * </pre>
     *
     * @param expression 数学表达式
     * @return 结果
     */
    public static double calculate(String expression) {
        return Formula.conversion(expression);
    }

    private static int mathSubnode(int selectNum, int minNum) {
        if (selectNum == minNum) {
            return 1;
        } else {
            return selectNum * mathSubnode(selectNum - 1, minNum);
        }
    }

    private static int mathNode(int selectNum) {
        if (selectNum == 0) {
            return 1;
        } else {
            return selectNum * mathNode(selectNum - 1);
        }
    }

    /**
     * 给数字对象按照指定长度在左侧补0.
     * <p>
     * 使用案例: addZero2String(11,4) 返回 "0011", addZero2String(-18,6)返回 "-000018"
     *
     * @param numObj 数字对象
     * @param length 指定的长度
     * @return the string
     */
    public static String addZero(Number numObj, int length) {
        NumberFormat nf = NumberFormat.getInstance();
        // 设置是否使用分组
        nf.setGroupingUsed(false);
        // 设置最大整数位数
        nf.setMaximumIntegerDigits(length);
        // 设置最小整数位数
        nf.setMinimumIntegerDigits(length);
        return nf.format(numObj);
    }

    /**
     * 将字符串转换为int类型
     *
     * <pre>
     *   MathKit.toInt(null) = 0
     *   MathKit.toInt("")   = 0
     *   MathKit.toInt("1")  = 1
     * </pre>
     *
     * @param text 要转换的字符串可以为空
     * @return 字符串表示的整型数，如果转换失败则默认值
     */
    public static int toInt(final String text) {
        return toInt(text, 0);
    }

    /**
     * 将字符串转换为int类型，如果转换失败则返回默认值
     *
     * <pre>
     *   MathKit.toInt(null, 1) = 1
     *   MathKit.toInt("", 1)   = 1
     *   MathKit.toInt("1", 0)  = 1
     * </pre>
     *
     * @param text         要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的int，如果转换失败则默认值
     */
    public static int toInt(final String text, final int defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为long类型
     *
     * <pre>
     *   MathKit.toLong(null) = 0L
     *   MathKit.toLong("")   = 0L
     *   MathKit.toLong("1")  = 1L
     * </pre>
     *
     * @param text 要转换的字符串可以为空
     * @return 字符串表示的long，如果转换失败则默认值
     */
    public static long toLong(final String text) {
        return toLong(text, 0L);
    }

    /**
     * 将字符串转换为long类型，如果转换失败则返回默认值
     *
     * <pre>
     *   MathKit.toInt(null, 1) = 1
     *   MathKit.toInt("", 1)   = 1
     *   MathKit.toInt("1", 0)  = 1
     * </pre>
     *
     * @param text         要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的long，如果转换失败则默认值
     */
    public static long toLong(final String text, final long defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为float类型
     *
     * <pre>
     *   MathKit.toFloat(null)   = 0.0f
     *   MathKit.toFloat("")     = 0.0f
     *   MathKit.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param text 要转换的字符串可以为空
     * @return 字符串表示的float，如果转换失败则默认值
     */
    public static float toFloat(final String text) {
        return toFloat(text, 0.0f);
    }

    /**
     * 将字符串转换为float类型，如果转换失败则返回默认值
     *
     * <pre>
     *   MathKit.toFloat(null, 1.1f)   = 1.0f
     *   MathKit.toFloat("", 1.1f)     = 1.1f
     *   MathKit.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param text         要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的float，如果转换失败则默认值
     */
    public static float toFloat(final String text, final float defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为double类型
     *
     * <pre>
     *   MathKit.toDouble(null)   = 0.0d
     *   MathKit.toDouble("")     = 0.0d
     *   MathKit.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param text 要转换的字符串可以为空
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final String text) {
        return toDouble(text, 0.0d);
    }

    /**
     * 将字符串转换为double类型
     *
     * <pre>
     *   MathKit.toDouble(null, 1.1d)   = 1.1d
     *   MathKit.toDouble("", 1.1d)     = 1.1d
     *   MathKit.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param text         要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final String text, final double defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将BigDecimal转换为double类型
     *
     * <pre>
     *   MathKit.toDouble(null)                     = 0.0d
     *   MathKit.toDouble(BigDecimal.valudOf(8.5d)) = 8.5d
     * </pre>
     *
     * @param value 要转换的字符可以为空
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final BigDecimal value) {
        return toDouble(value, 0.0d);
    }

    /**
     * 将BigDecimal转换为double类型
     *
     * <pre>
     *   MathKit.toDouble(null, 1.1d)                     = 1.1d
     *   MathKit.toDouble(BigDecimal.valudOf(8.5d), 1.1d) = 8.5d
     * </pre>
     *
     * @param value        要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final BigDecimal value, final double defaultValue) {
        return null == value ? defaultValue : value.doubleValue();
    }

    /**
     * Number值转换为double
     * float强制转换存在精度问题，此方法避免精度丢失
     *
     * @param value 被转换的float值
     * @return double值
     */
    public static double toDouble(Number value) {
        if (value instanceof Float) {
            return Double.parseDouble(value.toString());
        } else {
            return value.doubleValue();
        }
    }

    /**
     * 将字符转换为byte类型
     *
     * <pre>
     *   MathKit.toByte(null) = 0
     *   MathKit.toByte("")   = 0
     *   MathKit.toByte("1")  = 1
     * </pre>
     *
     * @param text 要转换的字符可以为空
     * @return 字符串表示的byte，如果转换失败则默认值
     */
    public static byte toByte(final String text) {
        return toByte(text, (byte) 0);
    }

    /**
     * 将字符转换为byte类型
     *
     * <pre>
     *   MathKit.toByte(null, 1) = 1
     *   MathKit.toByte("", 1)   = 1
     *   MathKit.toByte("1", 0)  = 1
     * </pre>
     *
     * @param text         要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的byte，如果转换失败则默认值
     */
    public static byte toByte(final String text, final byte defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符转换为short类型
     *
     * <pre>
     *   MathKit.toShort(null) = 0
     *   MathKit.toShort("")   = 0
     *   MathKit.toShort("1")  = 1
     * </pre>
     *
     * @param text 要转换的字符可以为空
     * @return 字符串表示的short，如果转换失败则默认值
     */
    public static short toShort(final String text) {
        return toShort(text, (short) 0);
    }

    /**
     * 将字符转换为short类型
     *
     * <pre>
     *   MathKit.toShort(null, 1) = 1
     *   MathKit.toShort("", 1)   = 1
     *   MathKit.toShort("1", 0)  = 1
     * </pre>
     *
     * @param text         要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的short，如果转换失败则默认值
     */
    public static short toShort(final String text, final short defaultValue) {
        if (null == text) {
            return defaultValue;
        }
        try {
            return Short.parseShort(text);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将一个BigDecimal转换为一个BigDecimal，两个刻度已经使用RoundingMode.HALF_EVEN 四舍五入.
     * 如果提供的值为null，则BigDecimal返回0
     *
     * @param value 要转换的字符可以为空
     * @return 字符串表示的BigDecimal，如果转换失败则默认值
     */
    public static BigDecimal toScaledBigDecimal(final BigDecimal value) {
        return toScaledBigDecimal(value, Normal.INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * 将一个BigDecimal转换为一个BigDecimal，两个刻度已经使用RoundingMode.HALF_EVEN 四舍五入.
     * * 如果提供的值为null，则BigDecimal返回0.
     *
     * @param value        要转换的BigDecimal，可以为null.
     * @param scale        小数点右边的位数.
     * @param roundingMode 能够放弃精度的数值运算的舍入行为.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final BigDecimal value, final int scale, final RoundingMode roundingMode) {
        if (null == value) {
            return BigDecimal.ZERO;
        }
        return value.setScale(
                scale,
                null == roundingMode ? RoundingMode.HALF_EVEN : roundingMode
        );
    }

    /**
     * 将一个Float转换为一个BigDecimal，两个刻度已经用RoundingMode.HALF_EVEN四舍五入了.
     * 如果提供的值为null，则BigDecimal 返回0.
     *
     * @param value 要转换的Float，可以为null.
     * @return 按比例取适当的四舍五入
     */
    public static BigDecimal toScaledBigDecimal(final Float value) {
        return toScaledBigDecimal(value, Normal.INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * 将一个Float转换为一个BigDecimal，其比例尺为指定值，并应用RoundingMode.
     * 如果输入值是null，我们只需返回BigDecimal.ZERO.
     *
     * @param value        要转换的Float，可以为null.
     * @param scale        小数点右边的位数.
     * @param roundingMode 能够放弃精度的数值运算的舍入行为.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final Float value, final int scale, final RoundingMode roundingMode) {
        if (null == value) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                BigDecimal.valueOf(value),
                scale,
                roundingMode
        );
    }

    /**
     * 将一个Double转换为一个BigDecimal，其比例尺为指定值，并应用RoundingMode.
     * 如果输入值是null，我们只需返回BigDecimal.ZERO.
     *
     * @param value 要转换的Double，可以为null.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final Double value) {
        return toScaledBigDecimal(value, Normal.INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * 将一个Double转换为一个BigDecimal，其比例尺为指定值，并应用RoundingMode.
     * 如果输入值是null，我们只需返回BigDecimal.ZERO..
     *
     * @param value        转换的Double，可以为null.
     * @param scale        小数点右边的位数.
     * @param roundingMode 能够放弃精度的数值运算的舍入行为.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final Double value, final int scale, final RoundingMode roundingMode) {
        if (null == value) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                BigDecimal.valueOf(value),
                scale,
                roundingMode
        );
    }

    /**
     * 将一个String转换为一个BigDecimal，其比例尺为指定值，并应用RoundingMode.
     * 如果输入值是null，我们只需返回BigDecimal.ZERO.
     *
     * @param value 转换的String，可以为null.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final String value) {
        return toScaledBigDecimal(value, Normal.INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * 将一个String转换为一个BigDecimal，其比例尺为指定值，并应用RoundingMode.
     * 如果输入值是null，我们只需返回BigDecimal.ZERO.
     *
     * @param value        转换的String，可以为null.
     * @param scale        小数点右边的位数.
     * @param roundingMode 能够放弃精度的数值运算的舍入行为.
     * @return 按比例取适当的四舍五入.
     */
    public static BigDecimal toScaledBigDecimal(final String value, final int scale, final RoundingMode roundingMode) {
        if (null == value) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                createBigDecimal(value),
                scale,
                roundingMode
        );
    }

    /**
     * 将一个String转换为一个BigDecimal
     *
     * @param text a <code>String</code> to convert, may be null
     * @return 转换后的BigDecimal(如果输入为null ， 则为null)
     * @throws NumberFormatException 如果值不能被转换
     */
    public static BigDecimal createBigDecimal(final String text) {
        if (null == text) {
            return null;
        }
        if (StringKit.isBlank(text)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if (text.trim().startsWith(Symbol.MINUS + Symbol.MINUS)) {
            throw new NumberFormatException(text + " is not a valid number.");
        }
        return new BigDecimal(text);
    }

    /**
     * 计算排列数，即A(n, m) = n!/(n-m)!
     *
     * @param n 总数
     * @param m 选择的个数
     * @return 排列数
     */
    public static long arrangeCount(int n, int m) {
        return Arrange.count(n, m);
    }

    /**
     * 计算排列数，即A(n, n) = n!
     *
     * @param n 总数
     * @return 排列数
     */
    public static long arrangeCount(int n) {
        return Arrange.count(n);
    }

    /**
     * 排列选择（从列表中选择n个排列）
     *
     * @param datas 待选列表
     * @param m     选择个数
     * @return 所有排列列表
     */
    public static List<String[]> arrangeSelect(String[] datas, int m) {
        return new Arrange(datas).select(m);
    }

    /**
     * 全排列选择（列表全部参与排列）
     *
     * @param datas 待选列表
     * @return 所有排列列表
     */
    public static List<String[]> arrangeSelect(String[] datas) {
        return new Arrange(datas).select();
    }

    /**
     * 计算组合数，即C(n, m) = n!/((n-m)! * m!)
     *
     * @param n 总数
     * @param m 选择的个数
     * @return 组合数
     */
    public static long combineCount(int n, int m) {
        return Combine.count(n, m);
    }

    /**
     * 组合选择（从列表中选择n个组合）
     *
     * @param datas 待选列表
     * @param m     选择个数
     * @return 所有组合列表
     */
    public static List<String[]> combineSelect(String[] datas, int m) {
        return new Combine(datas).select(m);
    }


    /**
     * 获得对应bytes的{@link MathKit}
     *
     * @param bytes bytes大小，可正可负
     * @return a {@link MathKit}
     */
    public static MathKit ofBytes(long bytes) {
        return new MathKit(bytes);
    }

    /**
     * 获得对应kilobytes的{@link MathKit}
     *
     * @param kilobytes kilobytes大小，可正可负
     * @return a {@link MathKit}
     */
    public static MathKit ofKilobytes(long kilobytes) {
        return new MathKit(Math.multiplyExact(kilobytes, Normal.BYTES_PER_KB));
    }

    /**
     * 获得对应megabytes的{@link MathKit}
     *
     * @param megabytes megabytes大小，可正可负
     * @return a {@link MathKit}
     */
    public static MathKit ofMegabytes(long megabytes) {
        return new MathKit(Math.multiplyExact(megabytes, Normal.BYTES_PER_MB));
    }

    /**
     * 获得对应gigabytes的{@link MathKit}
     *
     * @param gigabytes gigabytes大小，可正可负
     * @return a {@link MathKit}
     */
    public static MathKit ofGigabytes(long gigabytes) {
        return new MathKit(Math.multiplyExact(gigabytes, Normal.BYTES_PER_GB));
    }

    /**
     * 获得对应terabytes的{@link MathKit}
     *
     * @param terabytes terabytes大小，可正可负
     * @return a {@link MathKit}
     */
    public static MathKit ofTerabytes(long terabytes) {
        return new MathKit(Math.multiplyExact(terabytes, Normal.BYTES_PER_TB));
    }

    /**
     * 可读的文件大小
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String format(long size) {
        if (size <= 0) {
            return Symbol.ZERO;
        }
        int digitGroups = Math.min(Normal.CAPACITY_NAMES.length - 1, (int) (Math.log10(size) / Math.log10(Normal._1024)));
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(Normal._1024, digitGroups)) + Symbol.SPACE + Normal.CAPACITY_NAMES[digitGroups];
    }

    /**
     * 解析数据大小字符串，转换为bytes大小
     *
     * @param text 数据大小字符串，类似于：12KB, 5MB等
     * @return bytes大小
     */
    public static long parse(String text) {
        return MathKit.parse(text, null).toBytes();
    }

    /**
     * 获取指定数据大小文本对应的{@link MathKit}对象，如果无单位指定，默认获取{@link Normal#CAPACITY_NAMES}
     * <p>
     * 例如：
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 bytes"
     * </pre>
     *
     * @param text the text to parse
     * @return the parsed {@link MathKit}
     */
    public static MathKit parse(CharSequence text) {
        return parse(text, null);
    }

    /**
     * Obtain a {@link MathKit} from a text string such as {@code 12MB} using
     * the specified default {@link Normal#CAPACITY_NAMES} if no unit is specified.
     * <p>
     * Examples:
     * <pre>
     * "12KB" -- parses as "12 kilobytes"
     * "5MB"  -- parses as "5 megabytes"
     * "20"   -- parses as "20 kilobytes" (where the {@code defaultUnit} is {@link Normal#CAPACITY_NAMES})
     * </pre>
     *
     * @param text        the text to parse
     * @param defaultUnit the default
     * @return the parsed {@link MathKit}
     */
    public static MathKit parse(CharSequence text, String defaultUnit) {
        Assert.notNull(text, "Text must not be null");
        try {
            Matcher matcher = Pattern.compile("^([+\\-]?\\d+)([a-zA-Z]{0,2})$").matcher(text);
            Assert.state(matcher.matches(), "Does not match data size pattern");
            String unit = determineDataUnit(matcher.group(2), defaultUnit);
            long amount = Long.parseLong(matcher.group(1));
            return new MathKit(Math.multiplyExact(amount, bytes));
        } catch (Exception ex) {
            throw new IllegalArgumentException("'" + text + "' is not a valid data size", ex);
        }
    }

    /**
     * 决定数据单位，后缀不识别时使用默认单位
     *
     * @param suffix      后缀
     * @param defaultUnit 默认单位
     * @return {@link String}
     */
    private static String determineDataUnit(String suffix, String defaultUnit) {
        String defaultUnitToUse = (null != defaultUnit ? defaultUnit : Normal.CAPACITY_NAMES[0]);
        return (StringKit.isNotEmpty(suffix) ? getCapacity(suffix) : defaultUnitToUse);
    }

    /**
     * 检查是否为有效的数字
     * 检查Double和Float是否为无限大，或者Not a Number
     * 非数字类型和Null将返回true
     *
     * @param number 被检查类型
     * @return 检查结果，非数字类型和Null将返回true
     */
    public static boolean isValidNumber(Number number) {
        if (null == number) {
            return false;
        }
        if (number instanceof Double) {
            return (false == ((Double) number).isInfinite()) && (false == ((Double) number).isNaN());
        } else if (number instanceof Float) {
            return (false == ((Float) number).isInfinite()) && (false == ((Float) number).isNaN());
        }
        return true;
    }

    /**
     * 检查value是否在[min,max]范围内
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 被检查值
     * @return 检查结果，范围内将返回true，否则返回false
     */
    public static boolean isBetween(int min, int max, int value) {
        return value >= min && value <= max;
    }

    /**
     * 检查value是否在[min,max]范围内
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 被检查值
     * @return 检查结果，范围内将返回true，否则返回false
     */
    public static boolean isBetween(long min, long max, long value) {
        return value >= min && value <= max;
    }

    /**
     * 检查value是否在[min,max]范围内
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 被检查值
     * @return 检查结果，范围内将返回true，否则返回false
     */
    public static boolean isBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    /**
     * 检查value是否在[min,max]范围内
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 被检查值
     * @return 检查结果，范围内将返回true，否则返回false
     */
    public static boolean isBetween(double min, double max, double value) {
        return value >= min && value <= max;
    }

    /**
     * 返回标准容量后缀
     * 支持类似于3MB，3M，3m等写法
     *
     * @param suffix 单位后缀
     * @return 匹配到的容量信息，无法匹配则返回原始信息
     */
    public static String getCapacity(String suffix) {
        for (String candidate : Normal.CAPACITY_NAMES) {
            if (StringKit.startWithIgnoreCase(candidate, suffix)) {
                return candidate;
            }
        }
        return suffix;
    }

    /**
     * 检查是否为奇数
     *
     * @param num 被判断的数值
     * @return 是否是奇数
     */
    public static boolean isOdd(int num) {
        return (num & 1) == 1;
    }

    /**
     * 检查是否为偶数
     *
     * @param num 被判断的数值
     * @return 是否是偶数
     */
    public static boolean isEven(int num) {
        return false == isOdd(num);
    }

    /**
     * 金额元转换为分
     *
     * @param yuan 金额，单位元
     * @return 金额，单位分
     */
    public static long yuanToCent(double yuan) {
        return new Money(yuan).getCent();
    }

    /**
     * 金额分转换为元
     *
     * @param cent 金额，单位分
     * @return 金额，单位元
     */
    public static double centToYuan(long cent) {
        long yuan = cent / 100;
        int centPart = (int) (cent % 100);
        return new Money(yuan, centPart).getAmount().doubleValue();
    }

    /**
     * 是否为负数，不包括0
     *
     * @return 负数返回true，否则false
     */
    public boolean isNegative() {
        return bytes < 0;
    }

    /**
     * 返回bytes大小
     *
     * @return bytes大小
     */
    public long toBytes() {
        return bytes;
    }

    /**
     * 返回KB大小
     *
     * @return KB大小
     */
    public long toKilobytes() {
        return bytes / Normal.BYTES_PER_KB;
    }

    /**
     * 返回MB大小
     *
     * @return MB大小
     */
    public long toMegabytes() {
        return bytes / Normal.BYTES_PER_MB;
    }

    /**
     * 返回GB大小
     *
     * @return GB大小
     */
    public long toGigabytes() {
        return bytes / Normal.BYTES_PER_GB;
    }

    /**
     * 返回TB大小
     *
     * @return TB大小
     */
    public long toTerabytes() {
        return bytes / Normal.BYTES_PER_TB;
    }

}
