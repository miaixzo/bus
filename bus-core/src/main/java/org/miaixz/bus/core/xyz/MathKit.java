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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.math.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 数字工具类
 * 对于精确值计算应该使用 {@link BigDecimal}
 * JDK7中<strong>BigDecimal(double val)</strong>构造方法的结果有一定的不可预知性，例如：
 *
 * <pre>
 * new BigDecimal(0.1)和 BigDecimal.valueOf(0.1)
 * </pre>
 * <p>
 * 表示的不是<strong>0.1</strong>而是<strong>0.1000000000000000055511151231257827021181583404541015625</strong>
 *
 * <p>
 * 这是因为0.1无法准确的表示为double。因此应该使用<strong>new BigDecimal(String)</strong>。
 * </p>
 * 相关介绍：
 * <ul>
 * <li><a href="https://github.com/venusdrogon/feilong-core/wiki/one-jdk7-bug-thinking">one-jdk7-bug-thinking</a></li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MathKit extends NumberValidator {

    /**
     * 0-20对应的阶乘，超过20的阶乘会超过Long.MAX_VALUE
     */
    private static final long[] FACTORIALS = new long[]{
            1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L, 479001600L, 6227020800L,
            87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L,
            2432902008176640000L};

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空，则返回0
     *
     * @param values 多个被加值
     * @return 和
     */
    public static BigDecimal add(final Number... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的加法运算
     * 如果传入多个值为null或者空，则返回
     *
     * <p>
     * 需要注意的是，在不同Locale下，数字的表示形式也是不同的，例如：
     * 德国、荷兰、比利时、丹麦、意大利、罗马尼亚和欧洲大多地区使用`,`区分小数
     * 也就是说，在这些国家地区，1.20表示120，而非1.2。
     * </p>
     *
     * @param values 多个被加值
     * @return 和
     */
    public static BigDecimal add(final String... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringKit.isNotBlank(value)) {
                result = result.add(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空，则返回0
     *
     * @param values 多个被减值
     * @return 差
     */
    public static BigDecimal sub(final Number... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的减法运算
     * 如果传入多个值为null或者空，则返回0
     *
     * @param values 多个被减值
     * @return 差
     */
    public static BigDecimal sub(final String... values) {
        if (ArrayKit.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (StringKit.isNotBlank(value)) {
                result = result.subtract(toBigDecimal(value));
            }
        }
        return result;
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空，则返回0
     *
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(final Number... values) {
        if (ArrayKit.isEmpty(values) || ArrayKit.hasNull(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        if (isZero(value)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (isZero(value)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(toBigDecimal(value));
        }
        return result;
    }

    /**
     * 提供精确的乘法运算
     * 如果传入多个值为null或者空，则返回0
     *
     * @param values 多个被乘值
     * @return 积
     */
    public static BigDecimal mul(final String... values) {
        if (ArrayKit.isEmpty(values) || ArrayKit.hasNull(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = toBigDecimal(values[0]);
        if (isZero(result)) {
            return BigDecimal.ZERO;
        }

        BigDecimal ele;
        for (int i = 1; i < values.length; i++) {
            ele = toBigDecimal(values[i]);
            if (isZero(ele)) {
                return BigDecimal.ZERO;
            }
            result = result.multiply(ele);
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
    public static BigDecimal div(final Number v1, final Number v2) {
        return div(v1, v2, Normal._10);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况的时候,精确到小数点后10位,后面的四舍五入
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(final String v1, final String v2) {
        return div(v1, v2, Normal._10);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度，如果为负值，取绝对值
     * @return 两个参数的商
     */
    public static BigDecimal div(final Number v1, final Number v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度,后面的四舍五入
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 精确度，如果为负值，取绝对值
     * @return 两个参数的商
     */
    public static BigDecimal div(final String v1, final String v2, final int scale) {
        return div(v1, v2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度，如果为负值，取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static BigDecimal div(final String v1, final String v2, final int scale, final RoundingMode roundingMode) {
        return div(toBigDecimal(v1), toBigDecimal(v2), scale, roundingMode);
    }

    /**
     * 提供(相对)精确的除法运算,当发生除不尽的情况时,由scale指定精确度
     *
     * @param v1           被除数
     * @param v2           除数
     * @param scale        精确度，如果为负值，取绝对值
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 两个参数的商
     */
    public static BigDecimal div(final Number v1, final Number v2, int scale, final RoundingMode roundingMode) {
        Assert.notNull(v2, "Divisor must be not null !");
        if (null == v1 || isZero(v1)) {
            return BigDecimal.ZERO;
        }

        if (scale < 0) {
            scale = -scale;
        }
        return toBigDecimal(v1).divide(toBigDecimal(v2), scale, roundingMode);
    }

    /**
     * 补充Math.ceilDiv() JDK8中添加了和 {@link Math#floorDiv(int, int)} 但却没有ceilDiv()
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static int ceilDiv(final int v1, final int v2) {
        return (int) Math.ceil((double) v1 / v2);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 = 123.46
     *
     * @param v     值
     * @param scale 保留小数位数
     * @return 新值
     */
    public static BigDecimal round(final double v, final int scale) {
        return round(v, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 = 123.46
     *
     * @param v     值
     * @param scale 保留小数位数
     * @return 新值
     */
    public static String roundString(final double v, final int scale) {
        return round(v, scale).toPlainString();
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 = 123.46
     *
     * @param number 数字值
     * @param scale  保留小数位数
     * @return 新值
     */
    public static BigDecimal round(final BigDecimal number, final int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 采用四舍五入策略 {@link RoundingMode#HALF_UP}
     * 例如保留2位小数：123.456789 = 123.46
     *
     * @param numberStr 数字值的字符串表现形式
     * @param scale     保留小数位数
     * @return 新值
     */
    public static String roundString(final String numberStr, final int scale) {
        return roundString(numberStr, scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 = 123.4567
     *
     * @param v            值
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static BigDecimal round(final double v, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(v), scale, roundingMode);
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 = 123.4567
     *
     * @param v            值
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundString(final double v, final int scale, final RoundingMode roundingMode) {
        return round(v, scale, roundingMode).toPlainString();
    }

    /**
     * 保留固定位数小数
     * 例如保留四位小数：123.456789 = 123.4567
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
     * 例如保留四位小数：123.456789 = 123.4567
     *
     * @param numberStr    数字值的字符串表现形式
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 新值
     */
    public static String roundString(final String numberStr, final int scale, final RoundingMode roundingMode) {
        return round(toBigDecimal(numberStr), scale, roundingMode).toPlainString();
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
    public static BigDecimal roundHalfEven(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.HALF_EVEN);
    }

    /**
     * 保留固定小数位数，舍去多余位数
     *
     * @param number 需要科学计算的数据
     * @param scale  保留的小数位
     * @return 结果
     */
    public static BigDecimal roundDown(final Number number, final int scale) {
        return round(toBigDecimal(number), scale, RoundingMode.DOWN);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
     *                <ul>
     *                <li>0 = 取一位整数</li>
     *                <li>0.00 = 取一位整数和两位小数</li>
     *                <li>00.000 = 取两位整数和三位小数</li>
     *                <li># = 取所有整数部分</li>
     *                <li>#.##% = 以百分比方式计数，并取两位小数</li>
     *                <li>#.#####E0 = 显示为科学计数法，并取五位小数</li>
     *                <li>,### = 每三位以逗号进行分隔，例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 = 将格式嵌入文本</li>
     *                </ul>
     * @param value   值
     * @return 格式化后的值
     */
    public static String format(final String pattern, final double value) {
        Assert.isTrue(isValid(value), "value is NaN or Infinite!");
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
     *                <ul>
     *                <li>0 = 取一位整数</li>
     *                <li>0.00 = 取一位整数和两位小数</li>
     *                <li>00.000 = 取两位整数和三位小数</li>
     *                <li># = 取所有整数部分</li>
     *                <li>#.##% = 以百分比方式计数，并取两位小数</li>
     *                <li>#.#####E0 = 显示为科学计数法，并取五位小数</li>
     *                <li>,### = 每三位以逗号进行分隔，例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 = 将格式嵌入文本</li>
     *                </ul>
     * @param value   值
     * @return 格式化后的值
     */
    public static String format(final String pattern, final long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern 格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
     *                <ul>
     *                <li>0 = 取一位整数</li>
     *                <li>0.00 = 取一位整数和两位小数</li>
     *                <li>00.000 = 取两位整数和三位小数</li>
     *                <li># = 取所有整数部分</li>
     *                <li>#.##% = 以百分比方式计数，并取两位小数</li>
     *                <li>#.#####E0 = 显示为科学计数法，并取五位小数</li>
     *                <li>,### = 每三位以逗号进行分隔，例如：299,792,458</li>
     *                <li>光速大小为每秒,###米 = 将格式嵌入文本</li>
     *                </ul>
     * @param value   值，支持BigDecimal、BigInteger、Number等类型
     * @return 格式化后的值
     */
    public static String format(final String pattern, final Object value) {
        return format(pattern, value, null);
    }

    /**
     * 格式化double
     * 对 {@link DecimalFormat} 做封装
     *
     * @param pattern      格式 格式中主要以 # 和 0 两种占位符号来指定数字长度。0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
     *                     <ul>
     *                     <li>0 = 取一位整数</li>
     *                     <li>0.00 = 取一位整数和两位小数</li>
     *                     <li>00.000 = 取两位整数和三位小数</li>
     *                     <li># = 取所有整数部分</li>
     *                     <li>#.##% = 以百分比方式计数，并取两位小数</li>
     *                     <li>#.#####E0 = 显示为科学计数法，并取五位小数</li>
     *                     <li>,### = 每三位以逗号进行分隔，例如：299,792,458</li>
     *                     <li>光速大小为每秒,###米 = 将格式嵌入文本</li>
     *                     </ul>
     * @param value        值，支持BigDecimal、BigInteger、Number等类型
     * @param roundingMode 保留小数的方式枚举
     * @return 格式化后的值
     */
    public static String format(final String pattern, final Object value, final RoundingMode roundingMode) {
        if (value instanceof Number) {
            Assert.isTrue(isValidNumber((Number) value), "value is NaN or Infinite!");
        }
        final DecimalFormat decimalFormat = new DecimalFormat(pattern);
        if (null != roundingMode) {
            decimalFormat.setRoundingMode(roundingMode);
        }
        return decimalFormat.format(value);
    }

    /**
     * 格式化金额输出，每三位用逗号分隔
     *
     * @param value 金额
     * @return 格式化后的值
     */
    public static String formatMoney(final double value) {
        return format(",##0.00", value);
    }

    /**
     * 格式化百分比，小数采用四舍五入方式
     *
     * @param number 值
     * @param scale  保留小数位数
     * @return 百分比
     */
    public static String formatPercent(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * 格式化千分位表示方式，小数采用四舍五入方式
     *
     * @param number 值
     * @param scale  保留小数位数
     * @return 千分位数字
     */
    public static String formatThousands(final double number, final int scale) {
        final NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    /**
     * 生成一个从0开始的数字列表
     *
     * @param stopIncluded 结束的数字（不包含）
     * @return 数字列表
     */
    public static int[] range(final int stopIncluded) {
        return range(0, stopIncluded, 1);
    }

    /**
     * 生成一个数字列表
     * 自动判定正序反序
     *
     * @param startInclude 开始的数字（包含）
     * @param stopIncluded 结束的数字（包含）
     * @return 数字列表
     */
    public static int[] range(final int startInclude, final int stopIncluded) {
        return range(startInclude, stopIncluded, 1);
    }

    /**
     * 生成一个数字列表
     * 自动判定正序反序
     *
     * @param startInclude 开始的数字（包含）
     * @param stopIncluded 结束的数字（不包含）
     * @param step         步进
     * @return 数字列表
     */
    public static int[] range(int startInclude, int stopIncluded, int step) {
        if (startInclude > stopIncluded) {
            final int tmp = startInclude;
            startInclude = stopIncluded;
            stopIncluded = tmp;
        }

        if (step <= 0) {
            step = 1;
        }

        final int deviation = stopIncluded + 1 - startInclude;
        int length = deviation / step;
        if (deviation % step != 0) {
            length += 1;
        }
        final int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = startInclude;
            startInclude += step;
        }
        return range;
    }

    /**
     * 将给定范围内的整数添加到已有集合中，步进为1
     *
     * @param start  开始（包含）
     * @param stop   结束（包含）
     * @param values 集合
     * @return 集合
     */
    public static Collection<Integer> appendRange(final int start, final int stop, final Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /**
     * 将给定范围内的整数添加到已有集合中
     *
     * @param startInclude 开始（包含）
     * @param stopInclude  结束（包含）
     * @param step         步进
     * @param values       集合
     * @return 集合
     */
    public static Collection<Integer> appendRange(final int startInclude, final int stopInclude, int step, final Collection<Integer> values) {
        if (startInclude < stopInclude) {
            step = Math.abs(step);
        } else if (startInclude > stopInclude) {
            step = -Math.abs(step);
        } else {// start == end
            values.add(startInclude);
            return values;
        }

        for (int i = startInclude; (step > 0) ? i <= stopInclude : i >= stopInclude; i += step) {
            values.add(i);
        }
        return values;
    }

    /**
     * 获得数字对应的二进制字符串
     *
     * @param number 数字
     * @return 二进制字符串
     */
    public static String getBinaryString(final Number number) {
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
    public static int binaryToInt(final String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    /**
     * 二进制转long
     *
     * @param binaryStr 二进制字符串
     * @return long
     */
    public static long binaryToLong(final String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    /**
     * 比较数字值是否相等，相等返回{@code true}
     * 需要注意的是{@link BigDecimal}需要特殊处理
     * BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等，
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * <ul>
     *     <li>如果用户提供两个Number都是{@link BigDecimal}，则通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等</li>
     *     <li>其他情况调用{@link Number#equals(Object)}比较</li>
     * </ul>
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相等
     * @see CompareKit#equals(Comparable, Comparable)
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(final Number number1, final Number number2) {
        if (number1 instanceof BigDecimal && number2 instanceof BigDecimal) {
            // BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等
            return CompareKit.equals((BigDecimal) number1, (BigDecimal) number2);
        }
        return Objects.equals(number1, number2);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()}，并去除尾小数点儿后多余的0
     *
     * @param number       A Number
     * @param defaultValue 如果number参数为{@code null}，返回此默认值
     * @return A String.
     */
    public static String toString(final Number number, final String defaultValue) {
        return (null == number) ? defaultValue : toString(number);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()}或 {@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     *
     * @param number A Number
     * @return A String.
     */
    public static String toString(final Number number) {
        return toString(number, true);
    }

    /**
     * 数字转字符串
     * 调用{@link Number#toString()}或 {@link BigDecimal#toPlainString()}，并去除尾小数点儿后多余的0
     *
     * @param number               A Number
     * @param isStripTrailingZeros 是否去除末尾多余0，例如5.0返回5
     * @return A String.
     */
    public static String toString(final Number number, final boolean isStripTrailingZeros) {
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
    public static String toString(final BigDecimal bigDecimal) {
        return toString(bigDecimal, true);
    }

    /**
     * {@link BigDecimal}数字转字符串
     * 调用{@link BigDecimal#toPlainString()}，可选去除尾小数点儿后多余的0
     *
     * @param bigDecimal           A {@link BigDecimal}
     * @param isStripTrailingZeros 是否去除末尾多余0，例如5.0返回5
     * @return A String.
     */
    public static String toString(BigDecimal bigDecimal, final boolean isStripTrailingZeros) {
        Assert.notNull(bigDecimal, "BigDecimal is null !");
        if (isStripTrailingZeros) {
            bigDecimal = bigDecimal.stripTrailingZeros();
        }
        return bigDecimal.toPlainString();
    }

    /**
     * 数字转{@link BigDecimal}
     * Float、Double等有精度问题，转换为字符串后再转换
     * null转换为0
     *
     * @param number 数字
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(final Number number) {
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

        // Float、Double等有精度问题，转换为字符串后再转换
        return new BigDecimal(number.toString());
    }

    /**
     * 数字转{@link BigDecimal}
     * null或""或空白符抛出{@link IllegalArgumentException}异常
     * "NaN"转为{@link BigDecimal#ZERO}
     *
     * @param numberStr 数字字符串
     * @return {@link BigDecimal}
     * @throws IllegalArgumentException null或""或"NaN"或空白符抛出此异常
     */
    public static BigDecimal toBigDecimal(final String numberStr) throws IllegalArgumentException {
        // 统一规则，不再转换带有歧义的null、""和空格
        Assert.notBlank(numberStr, "Number text must be not blank!");

        // 优先调用构造解析
        try {
            return new BigDecimal(numberStr);
        } catch (final Exception ignore) {
            // 忽略解析错误
        }

        // 支持类似于 1,234.55 格式的数字
        return toBigDecimal(parseNumber(numberStr));
    }

    /**
     * 数字转{@link BigInteger}
     * null或"NaN"转换为0
     *
     * @param number 数字
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(final Number number) {
        // 统一规则，不再转换带有歧义的null
        Assert.notNull(number, "Number must be not null!");

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof Long) {
            return BigInteger.valueOf((Long) number);
        }

        return toBigInteger(number.longValue());
    }

    /**
     * 数字转{@link BigInteger}
     * null或""或空白符转换为0
     *
     * @param numberStr 数字字符串
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(final String numberStr) {
        // 统一规则，不再转换带有歧义的null、""和空格
        Assert.notBlank(numberStr, "Number text must be not blank!");

        try {
            return new BigInteger(numberStr);
        } catch (final Exception ignore) {
            // 忽略解析错误
        }

        return parseBigInteger(numberStr);
    }

    /**
     * 计算等份个数
     * <pre>
     *     (每份2)12   34  57
     *     (每份3)123  456 7
     *     (每份4)1234 567
     * </pre>
     *
     * @param total    总数
     * @param pageSize 每份的个数
     * @return 分成了几份
     */
    public static int count(final int total, final int pageSize) {
        // 因为总条数除以页大小的最大余数是页大小数-1，
        // 因此加一个最大余数，保证舍弃的余数与最大余数凑1.x，就是一旦有余数则+1页
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 如果给定值为0，返回1，否则返回原值
     *
     * @param value 值
     * @return 1或非0值
     */
    public static int zeroToOne(final int value) {
        return 0 == value ? 1 : value;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static int nullToZero(final Integer number) {
        return number == null ? 0 : number;
    }

    /**
     * 如果给定值为0，返回1，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static long nullToZero(final Long number) {
        return number == null ? 0L : number;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static double nullToZero(final Double number) {
        return number == null ? 0.0 : number;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static float nullToZero(final Float number) {
        return number == null ? 0.0f : number;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static short nullToZero(final Short number) {
        return number == null ? (short) 0 : number;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static byte nullToZero(final Byte number) {
        return number == null ? (byte) 0 : number;
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param number 值
     * @return 0或非0值
     */
    public static BigInteger nullToZero(final BigInteger number) {
        return ObjectKit.defaultIfNull(number, BigInteger.ZERO);
    }

    /**
     * 如果给定值为{@code null}，返回0，否则返回原值
     *
     * @param decimal {@link BigDecimal}，可以为{@code null}
     * @return {@link BigDecimal}参数为空时返回0的值
     */
    public static BigDecimal nullToZero(final BigDecimal decimal) {
        return ObjectKit.defaultIfNull(decimal, BigDecimal.ZERO);
    }

    /**
     * 创建{@link BigInteger}，支持16进制、10进制和8进制，如果传入空白串返回null
     * from Apache Common Lang
     *
     * @param numberStr 数字字符串
     * @return {@link BigInteger}
     */
    public static BigInteger parseBigInteger(final String numberStr) {
        return NumberParser.INSTANCE.parseBigInteger(numberStr);
    }

    /**
     * 判断两个数字是否相邻，例如1和2相邻，1和3不相邻
     * 判断方法为做差取绝对值判断是否为1
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相邻
     */
    public static boolean isBeside(final long number1, final long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 判断两个数字是否相邻，例如1和2相邻，1和3不相邻
     * 判断方法为做差取绝对值判断是否为1
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相邻
     */
    public static boolean isBeside(final int number1, final int number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * 把给定的总数平均分成N份，返回每份的个数
     * 当除以分数有余数时每份+1
     *
     * @param total     总数
     * @param partCount 份数
     * @return 每份的个数
     */
    public static int partValue(final int total, final int partCount) {
        return partValue(total, partCount, true);
    }

    /**
     * 把给定的总数平均分成N份，返回每份的个数
     * 如果isPlusOneWhenHasRem为true，则当除以分数有余数时每份+1，否则丢弃余数部分
     *
     * @param total               总数
     * @param partCount           份数
     * @param isPlusOneWhenHasRem 在有余数时是否每份+1
     * @return 每份的个数
     */
    public static int partValue(final int total, final int partCount, final boolean isPlusOneWhenHasRem) {
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
    public static BigDecimal pow(final Number number, final int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     * 提供精确的幂运算
     *
     * @param number 底数
     * @param n      指数
     * @return 幂的积
     */
    public static BigDecimal pow(final BigDecimal number, final int n) {
        return number.pow(n);
    }

    /**
     * 判断一个整数是否是2的幂
     *
     * @param n 待验证的整数
     * @return 如果n是2的幂返回true, 反之返回false
     */
    public static boolean isPowerOfTwo(final long n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    /**
     * 解析转换数字字符串为 {@link java.lang.Integer } 规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、其它情况按照10进制转换
     * 4、空串返回0
     * 5、.123形式返回0（按照小于0的小数对待）
     * 6、123.56截取小数点之前的数字，忽略小数部分
     * 7、解析失败返回默认值
     * </pre>
     *
     * @param numberStr    数字字符串，支持0x开头、0开头和普通十进制
     * @param defaultValue 如果解析失败, 将返回defaultValue, 允许null
     * @return Integer
     */
    public static Integer parseInt(final String numberStr, final Integer defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseInt(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * 解析转换数字字符串为int型数字，规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、其它情况按照10进制转换
     * 4、空串返回0
     * 5、.123形式返回0（按照小于0的小数对待）
     * 6、123.56截取小数点之前的数字，忽略小数部分
     * 7、科学计数法抛出NumberFormatException异常
     * </pre>
     *
     * @param numberStr 数字，支持0x开头、0开头和普通十进制
     * @return int
     * @throws NumberFormatException 数字格式异常
     */
    public static int parseInt(final String numberStr) throws NumberFormatException {
        return NumberParser.INSTANCE.parseInt(numberStr);
    }

    /**
     * 解析转换数字字符串为 {@link java.lang.Long } 规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、其它情况按照10进制转换
     * 4、空串返回0
     * 5、.123形式返回0（按照小于0的小数对待）
     * 6、123.56截取小数点之前的数字，忽略小数部分
     * 7、解析失败返回默认值
     * </pre>
     *
     * @param numberStr    数字字符串，支持0x开头、0开头和普通十进制
     * @param defaultValue 如果解析失败, 将返回defaultValue, 允许null
     * @return Long
     */
    public static Long parseLong(final String numberStr, final Long defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseLong(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }

        return defaultValue;
    }

    /**
     * 解析转换数字字符串为long型数字，规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、空串返回0
     * 4、其它情况按照10进制转换
     * 5、.123形式返回0（按照小于0的小数对待）
     * 6、123.56截取小数点之前的数字，忽略小数部分
     * </pre>
     *
     * @param numberStr 数字，支持0x开头、0开头和普通十进制
     * @return long
     */
    public static long parseLong(final String numberStr) {
        return NumberParser.INSTANCE.parseLong(numberStr);
    }

    /**
     * 解析转换数字字符串为 {@link java.lang.Float } 规则如下：
     *
     * <pre>
     * 1、0开头的忽略开头的0
     * 2、空串返回0
     * 3、其它情况按照10进制转换
     * 4、.123形式返回0.123（按照小于0的小数对待）
     * </pre>
     *
     * @param numberStr    数字字符串，支持0x开头、0开头和普通十进制
     * @param defaultValue 如果解析失败, 将返回defaultValue, 允许null
     * @return Float
     */
    public static Float parseFloat(final String numberStr, final Float defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseFloat(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }

        return defaultValue;
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
     * @param numberStr 数字，支持0x开头、0开头和普通十进制
     * @return long
     */
    public static float parseFloat(final String numberStr) {
        return NumberParser.INSTANCE.parseFloat(numberStr);
    }

    /**
     * 解析转换数字字符串为 {@link java.lang.Double } 规则如下：
     *
     * <pre>
     * 1、0开头的忽略开头的0
     * 2、空串返回0
     * 3、其它情况按照10进制转换
     * 4、.123形式返回0.123（按照小于0的小数对待）
     * </pre>
     *
     * @param numberStr    数字字符串，支持0x开头、0开头和普通十进制
     * @param defaultValue 如果解析失败, 将返回defaultValue, 允许null
     * @return Double
     */
    public static Double parseDouble(final String numberStr, final Double defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseDouble(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * 解析转换数字字符串为long型数字，规则如下：
     *
     * <pre>
     * 1、0开头的忽略开头的0
     * 2、空串返回0
     * 3、其它情况按照10进制转换
     * 4、.123形式返回0.123（按照小于0的小数对待）
     * 5、NaN返回0
     * </pre>
     *
     * @param numberStr 数字，支持0x开头、0开头和普通十进制
     * @return double
     */
    public static double parseDouble(final String numberStr) {
        return NumberParser.INSTANCE.parseDouble(numberStr);
    }

    /**
     * 将指定字符串转换为{@link Number }
     * 此方法不支持科学计数法
     *
     * @param numberStr    Number字符串
     * @param defaultValue 如果解析失败, 将返回defaultValue, 允许null
     * @return Number对象
     */
    public static Number parseNumber(final String numberStr, final Number defaultValue) {
        if (StringKit.isNotBlank(numberStr)) {
            try {
                return parseNumber(numberStr);
            } catch (final NumberFormatException ignore) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * 将指定字符串转换为{@link Number} 对象
     * 此方法不支持科学计数法
     *
     * <p>
     * 需要注意的是，在不同Locale下，数字的表示形式也是不同的，例如：
     * 德国、荷兰、比利时、丹麦、意大利、罗马尼亚和欧洲大多地区使用`,`区分小数
     * 也就是说，在这些国家地区，1.20表示120，而非1.2。
     * </p>
     *
     * @param numberStr Number字符串
     * @return Number对象
     * @throws NumberFormatException 包装了{@link ParseException}，当给定的数字字符串无法解析时抛出
     */
    public static Number parseNumber(final String numberStr) throws NumberFormatException {
        return NumberParser.INSTANCE.parseNumber(numberStr);
    }

    /**
     * 将指定字符串转换为{@link Number} 对象
     * 此方法不支持科学计数法
     *
     * <p>
     * 需要注意的是，在不同Locale下，数字的表示形式也是不同的，例如：
     * 德国、荷兰、比利时、丹麦、意大利、罗马尼亚和欧洲大多地区使用`,`区分小数
     * 也就是说，在这些国家地区，1.20表示120，而非1.2。
     * </p>
     *
     * @param numberStr Number字符串
     * @param locale    地区，不同地区数字表示方式不同
     * @return Number对象
     * @throws NumberFormatException 包装了{@link ParseException}，当给定的数字字符串无法解析时抛出
     */
    public static Number parseNumber(final String numberStr, final Locale locale) throws NumberFormatException {
        return NumberParser.of(locale).parseNumber(numberStr);
    }

    /**
     * 检查是否为有效的数字
     * 检查Double和Float是否为无限大，或者Not a Number
     * 非数字类型和{@code null}将返回{@code false}
     *
     * @param number 被检查类型
     * @return 检查结果，非数字类型和Null将返回true
     */
    public static boolean isValidNumber(final Number number) {
        if (null == number) {
            return false;
        }
        if (number instanceof Double) {
            return (!((Double) number).isInfinite()) && (!((Double) number).isNaN());
        } else if (number instanceof Float) {
            return (!((Float) number).isInfinite()) && (!((Float) number).isNaN());
        }
        return true;
    }

    /**
     * 检查是否为有效的数字
     * 检查double否为无限大，或者Not a Number（NaN）
     *
     * @param number 被检查double
     * @return 检查结果
     */
    public static boolean isValid(final double number) {
        return !(Double.isNaN(number) || Double.isInfinite(number));
    }

    /**
     * 检查是否为有效的数字
     * 检查double否为无限大，或者Not a Number（NaN）
     *
     * @param number 被检查double
     * @return 检查结果
     */
    public static boolean isValid(final float number) {
        return !(Float.isNaN(number) || Float.isInfinite(number));
    }

    /**
     * 计算数学表达式的值，只支持加减乘除和取余
     * 如：
     * <pre class="code">
     *   calculate("(0*1--3)-5/-4-(3*(-2.13))") - 10.64
     * </pre>
     *
     * @param expression 数学表达式
     * @return 结果
     */
    public static double calculate(final String expression) {
        return Calculator.conversion(expression);
    }

    /**
     * Number值转换为double
     * float强制转换存在精度问题，此方法避免精度丢失
     *
     * @param value 被转换的float值
     * @return double值
     */
    public static double toDouble(final Number value) {
        if (value instanceof Float) {
            return Double.parseDouble(value.toString());
        } else {
            return value.doubleValue();
        }
    }

    /**
     * 检查是否为奇数
     *
     * @param num 被判断的数值
     * @return 是否是奇数
     */
    public static boolean isOdd(final int num) {
        return (num & 1) == 1;
    }

    /**
     * 检查是否为偶数
     *
     * @param num 被判断的数值
     * @return 是否是偶数
     */
    public static boolean isEven(final int num) {
        return !isOdd(num);
    }

    /**
     * 判断给定数字是否为0
     * <ul>
     *     <li>如果是{@link Byte}、{@link Short}、{@link Integer}、{@link Long}，直接转为long和0L比较</li>
     *     <li>如果是{@link BigInteger}，使用{@link BigInteger#equals(Object)}</li>
     *     <li>如果是{@link Float}，转为float与0f比较</li>
     *     <li>如果是{@link Double}，转为double与0d比较</li>
     *     <li>其它情况转为{@link BigDecimal}与{@link BigDecimal#ZERO}比较大小（使用compare）</li>
     * </ul>
     *
     * @param n 数字
     * @return 是否为0
     */
    public static boolean isZero(final Number n) {
        Assert.notNull(n);

        if (n instanceof Byte ||
                n instanceof Short ||
                n instanceof Integer ||
                n instanceof Long) {
            return 0L == n.longValue();
        } else if (n instanceof BigInteger) {
            return equals(BigInteger.ZERO, n);
        } else if (n instanceof Float) {
            return 0f == n.floatValue();
        } else if (n instanceof Double) {
            return 0d == n.doubleValue();
        }
        return equals(toBigDecimal(n), BigDecimal.ZERO);
    }

    /**
     * 整数转罗马数字
     * 限制：[1,3999]的正整数
     * <ul>
     *     <li>I 1</li>
     *     <li>V 5</li>
     *     <li>X 10</li>
     *     <li>L 50</li>
     *     <li>C 100</li>
     *     <li>D 500</li>
     *     <li>M 1000</li>
     * </ul>
     *
     * @param num [1,3999]的正整数
     * @return 罗马数字
     */
    public static String intToRoman(final int num) {
        return RomanNumberFormatter.intToRoman(num);
    }

    /**
     * 罗马数字转整数
     *
     * @param roman 罗马字符
     * @return 整数
     * @throws IllegalArgumentException 如果传入非罗马字符串，抛出异常
     */
    public static int romanToInt(final String roman) {
        return RomanNumberFormatter.romanToInt(roman);
    }

    /**
     * 计算排列数，即A(n, m) = n!/(n-m)!
     *
     * @param n 总数
     * @param m 选择的个数
     * @return 排列数
     */
    public static long arrangementCount(final int n, final int m) {
        return Arrangement.count(n, m);
    }

    /**
     * 计算排列数，即A(n, n) = n!
     *
     * @param n 总数
     * @return 排列数
     */
    public static long arrangementCount(final int n) {
        return Arrangement.count(n);
    }

    /**
     * 排列选择（从列表中选择n个排列）
     *
     * @param datas 待选列表
     * @param m     选择个数
     * @return 所有排列列表
     */
    public static List<String[]> arrangementSelect(final String[] datas, final int m) {
        return new Arrangement(datas).select(m);
    }

    /**
     * 全排列选择（列表全部参与排列）
     *
     * @param datas 待选列表
     * @return 所有排列列表
     */
    public static List<String[]> arrangementSelect(final String[] datas) {
        return new Arrangement(datas).select();
    }

    /**
     * 计算组合数，即C(n, m) = n!/((n-m)!* m!)
     *
     * @param n 总数
     * @param m 选择的个数
     * @return 组合数
     */
    public static long combinationCount(final int n, final int m) {
        return Combination.count(n, m);
    }

    /**
     * 组合选择（从列表中选择n个组合）
     *
     * @param datas 待选列表
     * @param m     选择个数
     * @return 所有组合列表
     */
    public static List<String[]> combinationSelect(final String[] datas, final int m) {
        return new Combination(datas).select(m);
    }

    /**
     * 金额元转换为分
     *
     * @param yuan 金额，单位元
     * @return 金额，单位分
     */
    public static long yuanToCent(final double yuan) {
        return new Money(yuan).getCent();
    }

    /**
     * 金额分转换为元
     *
     * @param cent 金额，单位分
     * @return 金额，单位元
     */
    public static double centToYuan(final long cent) {
        final long yuan = cent / 100;
        final int centPart = (int) (cent % 100);
        return new Money(yuan, centPart).getAmount().doubleValue();
    }

    /**
     * 计算阶乘
     * <p>
     * n!= n * (n-1) * ... * 2 * 1
     * </p>
     *
     * @param n 阶乘起始
     * @return 结果
     */
    public static BigInteger factorial(final BigInteger n) {
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
        Assert.notNull(start, "Factorial start must be not null!");
        Assert.notNull(end, "Factorial end must be not null!");
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
     * 计算范围阶乘
     * <p>
     * factorial(start, end) = start * (start - 1) * ... * (end + 1)
     * </p>
     *
     * @param start 阶乘起始（包含）
     * @param end   阶乘结束，必须小于起始（不包括）
     * @return 结果
     */
    public static long factorial(final long start, final long end) {
        // 负数没有阶乘
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException(StringKit.format("Factorial start and end both must be >= 0, but got start={}, end={}", start, end));
        }
        if (0L == start || start == end) {
            return 1L;
        }
        if (start < end) {
            return 0L;
        }
        return factorialMultiplyAndCheck(start, factorial(start - 1, end));
    }

    /**
     * 计算范围阶乘中校验中间的计算是否存在溢出，factorial提前做了负数和0的校验，因此这里没有校验数字的正负
     *
     * @param a 乘数
     * @param b 被乘数
     * @return 如果 a * b的结果没有溢出直接返回，否则抛出异常
     */
    private static long factorialMultiplyAndCheck(final long a, final long b) {
        if (a <= Long.MAX_VALUE / b) {
            return a * b;
        }
        throw new IllegalArgumentException(StringKit.format("Overflow in multiplication: {} * {}", a, b));
    }

    /**
     * 计算阶乘
     * <p>
     * n!= n * (n-1) * ... * 2 * 1
     * </p>
     *
     * @param n 阶乘起始
     * @return 结果
     */
    public static long factorial(final long n) {
        if (n < 0 || n > 20) {
            throw new IllegalArgumentException(StringKit.format("Factorial must have n >= 0 and n <= 20 for n!, but got n = {}", n));
        }
        return FACTORIALS[(int) n];
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
    public static int processMultiple(final int selectNum, final int minNum) {
        final int result;
        result = mathSubNode(selectNum, minNum) / mathNode(selectNum - minNum);
        return result;
    }

    /**
     * 最大公约数
     * 见：https://stackoverflow.com/questions/4009198/java-get-greatest-common-divisor
     * 来自Guava的IntMath.gcd
     *
     * @param a 第一个值
     * @param b 第二个值
     * @return 最大公约数
     */
    public static int gcd(int a, int b) {
        /*
         * The reason we require both arguments to be >= 0 is because otherwise, what do you return on
         * gcd(0, Integer.MIN_VALUE)? BigInteger.gcd would return positive 2^31, but positive 2^31
         * isn't an int.
         */
        Assert.isTrue(a >= 0, "a must be >= 0");
        Assert.isTrue(b >= 0, "b must be >= 0");
        if (a == 0) {
            // 0 % b == 0, so b divides a, but the converse doesn't hold.
            // BigInteger.gcd is consistent with this decision.
            return b;
        } else if (b == 0) {
            return a; // similar logic
        }
        /*
         * Uses the binary GCD algorithm; see http://en.wikipedia.org/wiki/Binary_GCD_algorithm.
         * This is >40% faster than the Euclidean algorithm in benchmarks.
         */
        final int aTwos = Integer.numberOfTrailingZeros(a);
        a >>= aTwos; // divide out all 2s
        final int bTwos = Integer.numberOfTrailingZeros(b);
        b >>= bTwos; // divide out all 2s
        while (a != b) { // both a, b are odd
            // The data to the binary GCD algorithm is as follows:
            // Both a and b are odd.  Assume a > b; then gcd(a - b, b) = gcd(a, b).
            // But in gcd(a - b, b), a - b is even and b is odd, so we can divide out powers of two.

            // We bend over backwards to avoid branching, adapting a technique from
            // http://graphics.stanford.edu/~seander/bithacks.html#IntegerMinOrMax

            final int delta = a - b;

            final int minDeltaOrZero = delta & (delta >> (Integer.SIZE - 1));
            // equivalent to Math.min(delta, 0)

            a = delta - minDeltaOrZero - minDeltaOrZero; // sets a to Math.abs(a - b)
            // a is now nonnegative and even

            b += minDeltaOrZero; // sets b to min(old a, b)
            a >>= Integer.numberOfTrailingZeros(a); // divide out all 2s, since 2 doesn't divide b
        }
        return a << Math.min(aTwos, bTwos);
    }

    /**
     * 最小公倍数
     *
     * @param m 第一个值
     * @param n 第二个值
     * @return 最小公倍数
     */
    public static int multiple(final int m, final int n) {
        return m * n / gcd(m, n);
    }

    private static int mathSubNode(final int selectNum, final int minNum) {
        if (selectNum == minNum) {
            return 1;
        } else {
            return selectNum * mathSubNode(selectNum - 1, minNum);
        }
    }

    private static int mathNode(final int selectNum) {
        if (selectNum == 0) {
            return 1;
        } else {
            return selectNum * mathNode(selectNum - 1);
        }
    }

}
