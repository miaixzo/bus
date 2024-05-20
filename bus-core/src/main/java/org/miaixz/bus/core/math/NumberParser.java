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
package org.miaixz.bus.core.math;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * 数字解析器
 * 用于将字符串解析为对应的数字类型，支持包括：
 * <ul>
 *     <li>0开头的忽略开头的0</li>
 *     <li>空串返回0</li>
 *     <li>NaN返回0</li>
 *     <li>其它情况按照10进制转换</li>
 *     <li>.123形式返回0.123（按照小于0的小数对待）</li>
 * </ul>
 *
 * <p>
 *     构造时可选是否将NaN转为0，默认为true。
 *     参考：https://stackoverflow.com/questions/5876369/why-does-casting-double-nan-to-int-not-throw-an-exception-in-java
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumberParser {

    /**
     * 单例
     */
    public static final NumberParser INSTANCE = of(null);
    private static final String NaN = "NaN";
    private final Locale locale;
    private final boolean zeroIfNaN;

    /**
     * 构造
     *
     * @param locale    地域，{@code null}表示默认本地
     * @param zeroIfNaN 如果用户传入NaN，是否转为0，如果为{@code false}，则抛出{@link NumberFormatException}
     */
    public NumberParser(final Locale locale, final boolean zeroIfNaN) {
        this.locale = locale;
        this.zeroIfNaN = zeroIfNaN;
    }

    /**
     * 构建NumberParser
     *
     * @param locale 地域，{@code null}表示默认本地
     * @return NumberParser
     */
    public static NumberParser of(final Locale locale) {
        return of(locale, true);
    }

    /**
     * 构建NumberParser
     *
     * @param locale    地域，{@code null}表示默认本地
     * @param zeroIfNaN 如果用户传入NaN，是否转为0，如果为{@code false}，则抛出{@link NumberFormatException}
     * @return NumberParser
     */
    public static NumberParser of(final Locale locale, final boolean zeroIfNaN) {
        return new NumberParser(locale, zeroIfNaN);
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
     * @param numberStr 数字字符串
     * @return the int
     * @throws NumberFormatException 数字格式异常
     */
    public int parseInt(final String numberStr) throws NumberFormatException {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // 0x04表示16进制数
            return Integer.parseInt(numberStr.substring(2), 16);
        }

        if (StringKit.containsIgnoreCase(numberStr, "E")) {
            // 科学计数法忽略支持，科学计数法一般用于表示非常小和非常大的数字，这类数字转换为int后精度丢失，没有意义。
            throw new NumberFormatException(StringKit.format("Unsupported int format: [{}]", numberStr));
        }

        try {
            return Integer.parseInt(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).intValue();
        }
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
     * @param numberStr 数字字符串
     * @return the long
     */
    public long parseLong(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // 0x04表示16进制数
            return Long.parseLong(numberStr.substring(2), 16);
        }

        try {
            return Long.parseLong(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).longValue();
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
     * @param numberStr 数字字符串
     * @return the long
     */
    public float parseFloat(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        try {
            return Float.parseFloat(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).floatValue();
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
     * 5、NaN返回0
     * </pre>
     *
     * @param numberStr 数字字符串
     * @return the double
     */
    public double parseDouble(final String numberStr) {
        if (isBlankOrNaN(numberStr)) {
            return 0;
        }

        try {
            return Double.parseDouble(numberStr);
        } catch (final NumberFormatException e) {
            return doParse(numberStr).doubleValue();
        }
    }

    /**
     * 解析为{@link BigInteger}，支持16进制、10进制和8进制，如果传入空白串返回null
     *
     * @param text 数字字符串
     * @return {@link BigInteger}
     */
    public BigInteger parseBigInteger(String text) {
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
            radix = 16;
            pos += 2;
        } else if (text.startsWith(Symbol.SHAPE, pos)) {
            // alternative hex (allowed by Long/Integer)
            radix = 16;
            pos++;
        } else if (text.startsWith("0", pos) && text.length() > pos + 1) {
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
     * 将指定字符串转换为{@link Number} 对象
     * 此方法不支持科学计数法
     *
     * <ul>
     *     <li>空白符和NaN转换为0</li>
     *     <li>0x开头使用16进制解析为Long类型</li>
     * </ul>
     *
     * <p>
     * 需要注意的是，在不同Locale下，数字的表示形式也是不同的，例如：
     * 德国、荷兰、比利时、丹麦、意大利、罗马尼亚和欧洲大多地区使用`,`区分小数
     * 也就是说，在这些国家地区，1.20表示120，而非1.2。
     * </p>
     *
     * @param numberStr 数字字符串
     * @return Number对象
     * @throws NumberFormatException 包装了{@link ParseException}，当给定的数字字符串无法解析时抛出
     */
    public Number parseNumber(final String numberStr) throws NumberFormatException {
        if (isBlankOrNaN(numberStr)) {
            // 在JDK9+中，NaN的处理逻辑是返回0，此处保持一致
            return 0;
        }

        // 16进制
        if (StringKit.startWithIgnoreCase(numberStr, "0x")) {
            // 0x04表示16进制数
            return Long.parseLong(numberStr.substring(2), 16);
        }

        return doParse(numberStr);
    }

    /**
     * 使用{@link NumberFormat} 完成数字解析
     * 如果为{@link DecimalFormat}
     *
     * @return 数字
     */
    private Number doParse(String numberStr) {
        Locale locale = this.locale;
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        if (StringKit.startWith(numberStr, Symbol.C_PLUS)) {
            numberStr = StringKit.subSuf(numberStr, 1);
        }

        try {
            final NumberFormat format = NumberFormat.getInstance(locale);
            if (format instanceof DecimalFormat) {
                // 当字符串数字超出double的长度时，会导致截断，此处使用BigDecimal接收
                ((DecimalFormat) format).setParseBigDecimal(true);
            }
            return format.parse(numberStr);
        } catch (final ParseException e) {
            final NumberFormatException nfe = new NumberFormatException(e.getMessage());
            nfe.initCause(e);
            throw nfe;
        }
    }

    /**
     * 是否空白串或者NaN
     * 如果{@link #zeroIfNaN}为{@code false}，则抛出{@link NumberFormatException}
     *
     * @return 是否空白串或者NaN
     */
    private boolean isBlankOrNaN(final String numberStr) throws NumberFormatException {
        if (StringKit.isBlank(numberStr)) {
            return true;
        }

        if (NaN.equals(numberStr)) {
            if (zeroIfNaN) {
                return true;
            } else {
                throw new NumberFormatException("Can not parse NaN when 'zeroIfNaN' is false!");
            }
        }

        return false;
    }

}
