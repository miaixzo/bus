/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 数字工具类
 * 对于精确值计算应该使用 {@link BigDecimal}
 * JDK7中<strong>BigDecimal(double val)</strong>构造方法的结果有一定的不可预知性,例如：
 *
 * <pre>
 * new BigDecimal(0.1)
 * </pre>
 * <p>
 * 表示的不是<strong>0.1</strong>而是<strong>0.1000000000000000055511151231257827021181583404541015625</strong>
 *
 * <p>
 * 这是因为0.1无法准确的表示为double 因此应该使用<strong>new BigDecimal(String)</strong>
 * </p>
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class NumberUtils {

    /**
     * 默认除法运算精度
     */
    private static final int DEFAUT_DIV_SCALE = 10;

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
     * @param values 多个被加值
     * @return 和
     */
    public static BigDecimal add(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被加值
     * @return 和
     */
    public static BigDecimal add(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被加值
     * @return 和
     */
    public static BigDecimal add(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被减值
     * @return 差
     */
    public static BigDecimal sub(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被减值
     * @return 差
     */
    public static BigDecimal sub(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被减值
     * @return 差
     */
    public static BigDecimal sub(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
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
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            result = result.multiply(new BigDecimal(null == value ? Symbol.ZERO : value.toString()));
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
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? Symbol.ZERO : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.multiply(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空,则返回0
     *
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.multiply(value);
            }
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
        return div(v1, v2, DEFAUT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(float v1, double v2) {
        return div(v1, v2, DEFAUT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, float v2) {
        return div(v1, v2, DEFAUT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, DEFAUT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     * @since 3.1.9
     */
    public static BigDecimal div(Number v1, Number v2) {
        return div(v1, v2, DEFAUT_DIV_SCALE);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2) {
        return div(v1, v2, DEFAUT_DIV_SCALE);
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
     * @since 3.1.9
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
     * @since 3.1.9
     */
    public static BigDecimal div(Number v1, Number v2, int scale, RoundingMode roundingMode) {
        return div(v1.toString(), v2.toString(), scale, roundingMode);
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
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =》 123.46
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
     * 例如保留2位小数：123.456789 =》 123.46
     *
     * @param v     值
     * @param scale 保留小数位数
     * @return 新值
     */
    public static String roundStr(double v, int scale) {
        return round(v, scale).toString();
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =》 123.46
     *
     * @param numberStr 数字值的字符串表现形式
     * @param scale     保留小数位数
     * @return 新值
     */
    public static BigDecimal round(String numberStr, int scale) {
        return round(numberStr, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 =》 123.46
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
     * 例如保留2位小数：123.456789 =》 123.46
     *
     * @param numberStr 数字值的字符串表现形式
     * @param scale     保留小数位数
     * @return 新值
     */
    public static String roundStr(String numberStr, int scale) {
        return round(numberStr, scale).toString();
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =》 123.4567
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
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param v            值
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundStr(double v, int scale, RoundingMode roundingMode) {
        return round(v, scale, roundingMode).toString();
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param numberStr    数字值的字符串表现形式
     * @param scale        保留小数位数，如果传入小于0，则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
     * @return 新值
     */
    public static BigDecimal round(String numberStr, int scale, RoundingMode roundingMode) {
        Assert.notBlank(numberStr);
        if (scale < 0) {
            scale = 0;
        }
        return round(toBigDecimal(numberStr), scale, roundingMode);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param number       数字值
     * @param scale        保留小数位数，如果传入小于0，则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
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
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param numberStr    数字值的字符串表现形式
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundStr(String numberStr, int scale, RoundingMode roundingMode) {
        return round(numberStr, scale, roundingMode).toString();
    }

    /**
     * 四舍六入五成双计算法
     * <p>
     * 四舍六入五成双是一种比较精确比较科学的计数保留法，是一种数字修约规则。
     * </p>
     *
     * <pre>
     * 算法规则:
     * 四舍六入五考虑，
     * 五后非零就进一，
     * 五后皆零看奇偶，
     * 五前为偶应舍去，
     * 五前为奇要进一。
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
     *                <li>0 =》 取一位整数</li>
     *                <li>0.00 =》 取一位整数和两位小数</li>
     *                <li>00.000 =》 取两位整数和三位小数</li>
     *                <li># =》 取所有整数部分</li>
     *                <li>#.##% =》 以百分比方式计数,并取两位小数</li>
     *                <li>#.#####E0 =》 显示为科学计数法,并取五位小数</li>
     *                <li>,### =》 每三位以逗号进行分隔,例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =》 将格式嵌入文本</li>
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
     *                <li>0 =》 取一位整数</li>
     *                <li>0.00 =》 取一位整数和两位小数</li>
     *                <li>00.000 =》 取两位整数和三位小数</li>
     *                <li># =》 取所有整数部分</li>
     *                <li>#.##% =》 以百分比方式计数,并取两位小数</li>
     *                <li>#.#####E0 =》 显示为科学计数法,并取五位小数</li>
     *                <li>,### =》 每三位以逗号进行分隔,例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 =》 将格式嵌入文本</li>
     *                </ul>
     * @param value   值
     * @return 格式化后的值
     * @since 3.1.9
     */
    public static String decimalFormat(String pattern, long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度
     *                0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
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
        return new DecimalFormat(pattern).format(value);
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
     * @since 3.2.3
     */
    public static String formatPercent(double number, int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * 是否为数字
     *
     * @param str 字符串值
     * @return 是否为数字
     */
    public static boolean isNumber(CharSequence str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        char[] chars = str.toString().toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == Symbol.C_HYPHEN || chars[0] == Symbol.C_PLUS) ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && (chars[start + 1] == 'x' || chars[start + 1] == 'X')) {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
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
            if (chars[i] >= '0' && chars[i] <= '9') {
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
            } else if (chars[i] == Symbol.C_PLUS || chars[i] == Symbol.C_HYPHEN) {
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
            if (chars[i] >= '0' && chars[i] <= '9') {
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
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return false == allowSigns && foundDigit;
    }

    /**
     * 判断String是否是整数
     *
     * @param s String
     * @return 是否为整数
     */
    public static boolean isInteger(String s) {
        if (StringUtils.isNotBlank(s)) {
            return s.matches("^-?\\d+$");
        } else {
            return false;
        }
    }

    /**
     * 判断字符串是否是Long类型
     *
     * @param s String
     * @return 是否为{@link Long}类型
     */
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否是浮点数
     *
     * @param s String
     * @return 是否为{@link Double}类型
     */
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return s.contains(Symbol.DOT);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 是否是质数
     * 质数表的质数又称素数 指整数在一个大于1的自然数中,除了1和此整数自身外,没法被其他自然数整除的数
     *
     * @param n 数字
     * @return 是否是质数
     */
    public static boolean isPrimes(int n) {
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成不重复随机数 根据给定的最小数字和最大数字,以及随机数的个数,产生指定的不重复的数组
     *
     * @param begin 最小数字（包含该数）
     * @param end   最大数字（不包含该数）
     * @param size  指定产生随机数的个数
     * @return 随机int数组
     */
    public static int[] generateRandomNumber(int begin, int end, int size) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }
        // 加入逻辑判断,确保begin<end并且size不能大于该表示范围
        if ((end - begin) < size) {
            throw new InstrumentException("Size is larger than range between begin and end!");
        }
        // 种子你可以随意生成,但不能重复
        int[] seed = new int[end - begin];

        for (int i = begin; i < end; i++) {
            seed[i - begin] = i;
        }
        int[] ranArr = new int[size];
        Random ran = new Random();
        // 数量你可以自己定义
        for (int i = 0; i < size; i++) {
            // 得到一个位置
            int j = ran.nextInt(seed.length - i);
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
     * @param begin 最小数字（包含该数）
     * @param end   最大数字（不包含该数）
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
            throw new InstrumentException("Size is larger than range between begin and end!");
        }

        Random ran = new Random();
        Set<Integer> set = new HashSet<>();
        while (set.size() < size) {
            set.add(begin + ran.nextInt(end - begin));
        }

        return set.toArray(new Integer[size]);
    }

    /**
     * 从0开始给定范围内的整数列表,步进为1
     *
     * @param stop 结束（包含）
     * @return 整数列表
     * @since 3.3.1
     */
    public static int[] range(int stop) {
        return range(0, stop);
    }

    /**
     * 给定范围内的整数列表,步进为1
     *
     * @param start 开始（包含）
     * @param stop  结束（包含）
     * @return 整数列表
     */
    public static int[] range(int start, int stop) {
        return range(start, stop, 1);
    }

    /**
     * 给定范围内的整数列表
     *
     * @param start 开始（包含）
     * @param stop  结束（包含）
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
     * @param start  开始（包含）
     * @param stop   结束（包含）
     * @param values 集合
     * @return 集合
     */
    public static Collection<Integer> appendRange(int start, int stop, Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /**
     * 将给定范围内的整数添加到已有集合中
     *
     * @param start  开始（包含）
     * @param stop   结束（包含）
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
    public static String getBinaryStr(Number number) {
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
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Character#compare(char, char)
     * @since 3.0.1
     */
    public static int compare(char x, char y) {
        return x - y;
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Double#compare(double, double)
     * @since 3.0.1
     */
    public static int compare(double x, double y) {
        return Double.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Integer#compare(int, int)
     * @since 3.0.1
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
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Long#compare(long, long)
     * @since 3.0.1
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
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     * @see Short#compare(short, short)
     * @since 3.0.1
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
     * @since 3.0.1
     */
    public static int compare(byte x, byte y) {
        return x - y;
    }

    /**
     * 比较大小，值相等 返回true
     * 此方法通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否相等
     */
    public static boolean equals(BigDecimal bigNum1, BigDecimal bigNum2) {
        Assert.notNull(bigNum1);
        Assert.notNull(bigNum2);
        return 0 == bigNum1.compareTo(bigNum2);
    }

    /**
     * 比较两个字符是否相同
     *
     * @param c1         字符1
     * @param c2         字符2
     * @param ignoreCase 是否忽略大小写
     * @return 是否相同
     * @see CharUtils#equals(char, char, boolean)
     */
    public static boolean equals(char c1, char c2, boolean ignoreCase) {
        return CharUtils.equals(c1, c2, ignoreCase);
    }

    /**
     * 取最小值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(Comparable[])
     */
    public static <T extends Comparable<? super T>> T min(T[] numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(long...)
     */
    public static long min(long... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(int...)
     */
    public static int min(int... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(short...)
     */
    public static short min(short... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(double...)
     */
    public static double min(double... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(float...)
     */
    public static float min(float... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @see ArrayUtils#min(Comparable[])
     */
    public static BigDecimal min(BigDecimal... numberArray) {
        return ArrayUtils.min(numberArray);
    }

    /**
     * 取最大值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(Comparable[])
     */
    public static <T extends Comparable<? super T>> T max(T[] numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(long...)
     */
    public static long max(long... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(int...)
     */
    public static int max(int... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(short...)
     */
    public static short max(short... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(double...)
     */
    public static double max(double... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(float...)
     */
    public static float max(float... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @see ArrayUtils#max(Comparable[])
     */
    public static BigDecimal max(BigDecimal... numberArray) {
        return ArrayUtils.max(numberArray);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()},并去除尾小数点儿后多余的0
     *
     * @param number       A Number
     * @param defaultValue 如果number参数为{@code null},返回此默认值
     * @return A String.
     * @since 3.1.9
     */
    public static String toStr(Number number, String defaultValue) {
        return (null == number) ? defaultValue : toStr(number);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()},并去除尾小数点儿后多余的0
     *
     * @param number A Number
     * @return A String.
     */
    public static String toStr(Number number) {
        if (null == number) {
            throw new NullPointerException("Number is null !");
        }

        if (false == ObjectUtils.isValidIfNumber(number)) {
            throw new IllegalArgumentException("Number is non-finite!");
        }

        // 去掉小数点儿后多余的0
        String string = number.toString();
        if (string.indexOf(Symbol.C_DOT) > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
            while (string.endsWith(Symbol.ZERO)) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(Symbol.DOT)) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
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
        return toBigDecimal(number.toString());
    }

    /**
     * 数字转{@link BigDecimal}
     *
     * @param number 数字
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(String number) {
        return (null == number) ? BigDecimal.ZERO : new BigDecimal(number);
    }

    /**
     * 计算等份个数
     *
     * @param total 总数
     * @param part  每份的个数
     * @return 分成了几份
     * @since 3.1.9
     */
    public static int count(int total, int part) {
        return (total % part == 0) ? (total / part) : (total / part + 1);
    }

    /**
     * 空转0
     *
     * @param decimal {@link BigDecimal},可以为{@code null}
     * @return {@link BigDecimal}参数为空时返回0的值
     * @since 3.1.9
     */
    public static BigDecimal null2Zero(BigDecimal decimal) {

        return decimal == null ? BigDecimal.ZERO : decimal;
    }

    /**
     * 如果给定值为0,返回1,否则返回原值
     *
     * @param value 值
     * @return 1或非0值
     * @since 3.1.9
     */
    public static int zero2One(int value) {
        return 0 == value ? 1 : value;
    }

    /**
     * 创建{@link BigInteger},支持16进制、10进制和8进制,如果传入空白串返回null
     * from Apache Common Lang
     *
     * @param str 数字字符串
     * @return {@link BigInteger}
     * @since 5.8.9
     */
    public static BigInteger newBigInteger(String str) {
        if (null == str) {
            return null;
        }

        int pos = 0; // 数字字符串位置
        int radix = 10;
        boolean negate = false; // 负数与否
        if (str.startsWith(Symbol.HYPHEN)) {
            negate = true;
            pos = 1;
        }
        if (str.startsWith("0x", pos) || str.startsWith("0X", pos)) {
            // hex
            radix = 16;
            pos += 2;
        } else if (str.startsWith(Symbol.SHAPE, pos)) {
            // alternative hex (allowed by Long/Integer)
            radix = 16;
            pos++;
        } else if (str.startsWith(Symbol.ZERO, pos) && str.length() > pos + 1) {
            // octal; so long as there are additional digits
            radix = 8;
            pos++;
        } // default is to treat as decimal

        if (pos > 0) {
            str = str.substring(pos);
        }
        final BigInteger value = new BigInteger(str, radix);
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
        if (isPlusOneWhenHasRem && total % partCount == 0) {
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
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的视为8进制数字
     * 3、空串返回0
     * 4、其它情况按照10进制转换
     * </pre>
     *
     * @param number 数字,支持0x开头、0开头和普通十进制
     * @return int
     */
    public static int parseInt(String number) {
        if (StringUtils.isBlank(number)) {
            return 0;
        }

        // 对于带小数转换为整数采取去掉小数的策略
        number = StringUtils.subBefore(number, Symbol.C_DOT, false);
        if (StringUtils.isEmpty(number)) {
            return 0;
        }

        if (StringUtils.startWithIgnoreCase(number, "0x")) {
            // 0x04表示16进制数
            return Integer.parseInt(number.substring(2), 16);
        }

        return Integer.parseInt(removeNumberFlag(number));
    }

    /**
     * 解析转换数字字符串为long型数字,规则如下：
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的视为8进制数字
     * 3、空串返回0
     * 4、其它情况按照10进制转换
     * </pre>
     *
     * @param number 数字,支持0x开头、0开头和普通十进制
     * @return long
     */
    public static long parseLong(String number) {
        if (StringUtils.isBlank(number)) {
            return 0;
        }

        // 对于带小数转换为整数采取去掉小数的策略
        number = StringUtils.subBefore(number, Symbol.C_DOT, false);
        if (StringUtils.isEmpty(number)) {
            return 0;
        }

        if (number.startsWith("0x")) {
            // 0x04表示16进制数
            return Long.parseLong(number.substring(2), 16);
        }

        return Long.parseLong(removeNumberFlag(number));
    }

    /**
     * 将指定字符串转换为{@link Number} 对象
     *
     * @param numberStr Number字符串
     * @return Number对象
     */
    public static Number parseNumber(String numberStr) {
        numberStr = removeNumberFlag(numberStr);
        try {
            return NumberFormat.getInstance().parse(numberStr);
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
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
     * 去掉数字尾部的数字标识,例如12D,44.0F,22L中的最后一个字母
     *
     * @param number 数字字符串
     * @return 去掉标识的字符串
     */
    private static String removeNumberFlag(String number) {
        // 去掉类型标识的结尾
        final int lastPos = number.length() - 1;
        final char lastCharUpper = Character.toUpperCase(number.charAt(lastPos));
        if ('D' == lastCharUpper || 'L' == lastCharUpper || 'F' == lastCharUpper) {
            number = StringUtils.subPre(number, lastPos);
        }
        return number;
    }

    /**
     * 给数字对象按照指定长度在左侧补0.
     * <p>
     * 使用案例: addZero2Str(11,4) 返回 "0011", addZero2Str(-18,6)返回 "-000018"
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
     *   NumberUtils.toInt(null) = 0
     *   NumberUtils.toInt("")   = 0
     *   NumberUtils.toInt("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符串可以为空
     * @return 字符串表示的整型数，如果转换失败则默认值
     */
    public static int toInt(final String str) {
        return toInt(str, 0);
    }

    /**
     * 将字符串转换为int类型，如果转换失败则返回默认值
     *
     * <pre>
     *   NumberUtils.toInt(null, 1) = 1
     *   NumberUtils.toInt("", 1)   = 1
     *   NumberUtils.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的int，如果转换失败则默认值
     */
    public static int toInt(final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为long类型
     *
     * <pre>
     *   NumberUtils.toLong(null) = 0L
     *   NumberUtils.toLong("")   = 0L
     *   NumberUtils.toLong("1")  = 1L
     * </pre>
     *
     * @param str 要转换的字符串可以为空
     * @return 字符串表示的long，如果转换失败则默认值
     */
    public static long toLong(final String str) {
        return toLong(str, 0L);
    }

    /**
     * 将字符串转换为long类型，如果转换失败则返回默认值
     *
     * <pre>
     *   NumberUtils.toInt(null, 1) = 1
     *   NumberUtils.toInt("", 1)   = 1
     *   NumberUtils.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的long，如果转换失败则默认值
     */
    public static long toLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为float类型
     *
     * <pre>
     *   NumberUtils.toFloat(null)   = 0.0f
     *   NumberUtils.toFloat("")     = 0.0f
     *   NumberUtils.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param str 要转换的字符串可以为空
     * @return 字符串表示的float，如果转换失败则默认值
     */
    public static float toFloat(final String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * 将字符串转换为float类型，如果转换失败则返回默认值
     *
     * <pre>
     *   NumberUtils.toFloat(null, 1.1f)   = 1.0f
     *   NumberUtils.toFloat("", 1.1f)     = 1.1f
     *   NumberUtils.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param str          要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的float，如果转换失败则默认值
     */
    public static float toFloat(final String str, final float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符串转换为double类型
     *
     * <pre>
     *   NumberUtils.toDouble(null)   = 0.0d
     *   NumberUtils.toDouble("")     = 0.0d
     *   NumberUtils.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param str 要转换的字符串可以为空
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final String str) {
        return toDouble(str, 0.0d);
    }

    /**
     * 将字符串转换为double类型
     *
     * <pre>
     *   NumberUtils.toDouble(null, 1.1d)   = 1.1d
     *   NumberUtils.toDouble("", 1.1d)     = 1.1d
     *   NumberUtils.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param str          要转换的字符串可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final String str, final double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将BigDecimal转换为double类型
     *
     * <pre>
     *   NumberUtils.toDouble(null)                     = 0.0d
     *   NumberUtils.toDouble(BigDecimal.valudOf(8.5d)) = 8.5d
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
     *   NumberUtils.toDouble(null, 1.1d)                     = 1.1d
     *   NumberUtils.toDouble(BigDecimal.valudOf(8.5d), 1.1d) = 8.5d
     * </pre>
     *
     * @param value        要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的double，如果转换失败则默认值
     */
    public static double toDouble(final BigDecimal value, final double defaultValue) {
        return value == null ? defaultValue : value.doubleValue();
    }

    /**
     * 将字符转换为byte类型
     *
     * <pre>
     *   NumberUtils.toByte(null) = 0
     *   NumberUtils.toByte("")   = 0
     *   NumberUtils.toByte("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符可以为空
     * @return 字符串表示的byte，如果转换失败则默认值
     */
    public static byte toByte(final String str) {
        return toByte(str, (byte) 0);
    }

    /**
     * 将字符转换为byte类型
     *
     * <pre>
     *   NumberUtils.toByte(null, 1) = 1
     *   NumberUtils.toByte("", 1)   = 1
     *   NumberUtils.toByte("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的byte，如果转换失败则默认值
     */
    public static byte toByte(final String str, final byte defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * 将字符转换为short类型
     *
     * <pre>
     *   NumberUtils.toShort(null) = 0
     *   NumberUtils.toShort("")   = 0
     *   NumberUtils.toShort("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符可以为空
     * @return 字符串表示的short，如果转换失败则默认值
     */
    public static short toShort(final String str) {
        return toShort(str, (short) 0);
    }

    /**
     * 将字符转换为short类型
     *
     * <pre>
     *   NumberUtils.toShort(null, 1) = 1
     *   NumberUtils.toShort("", 1)   = 1
     *   NumberUtils.toShort("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符可以为空
     * @param defaultValue 默认值
     * @return 字符串表示的short，如果转换失败则默认值
     */
    public static short toShort(final String str, final short defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
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
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(
                scale,
                (roundingMode == null) ? RoundingMode.HALF_EVEN : roundingMode
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
        if (value == null) {
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
        if (value == null) {
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
        if (value == null) {
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
     * @param str a <code>String</code> to convert, may be null
     * @return 转换后的BigDecimal(如果输入为null ， 则为null)
     * @throws NumberFormatException 如果值不能被转换
     */
    public static BigDecimal createBigDecimal(final String str) {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if (str.trim().startsWith("--")) {
            throw new NumberFormatException(str + " is not a valid number.");
        }
        return new BigDecimal(str);
    }

}
