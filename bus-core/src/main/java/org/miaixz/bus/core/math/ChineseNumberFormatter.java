/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.math;

import java.math.BigDecimal;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A class for converting numbers to Chinese numerals. It includes:
 * 
 * <pre>
 * 1. Converting numbers to standard Chinese characters (e.g., 一百二十一).
 * 2. Converting numbers to financial Chinese characters (e.g., 壹佰贰拾壹).
 * 3. Formatting numbers into a financial string (e.g., 壹佰贰拾壹整).
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChineseNumberFormatter {

    /**
     * Chinese numeral characters. Odd indices are simplified, even indices are traditional (for accounting). '零' (zero)
     * is shared.
     */
    static final char[] DIGITS = { '零', '一', '壹', '二', '贰', '三', '叁', '四', '肆', '五', '伍', '六', '陆', '七', '柒', '八', '捌',
            '九', '玖' };

    /**
     * Whether to use traditional characters (financial format).
     */
    private boolean useTraditional;
    /**
     * Whether to use money mode (adds currency units like Yuan, Jiao, Fen).
     */
    private boolean moneyMode;
    /**
     * Whether to use colloquial mode (e.g., '十一' instead of '一十一').
     */
    private boolean colloquialMode;
    /**
     * The character or string to represent negative numbers.
     */
    private String negativeName = "负";
    /**
     * The name of the main currency unit (e.g., '元' or '圆').
     */
    private String unitName = "元";

    /**
     * Converts an Arabic numeral (integer, positive or negative) to a concise Chinese representation with section units
     * after rounding. For example, -55,555 becomes -5.56万.
     *
     * @param amount The number to format.
     * @return The formatted Chinese string.
     */
    public static String formatSimple(final long amount) {
        if (amount < 1_0000 && amount > -1_0000) {
            return String.valueOf(amount);
        }
        final String res;
        if (amount < 1_0000_0000 && amount > -1_0000_0000) {
            res = MathKit.div(amount, 1_0000, 2) + "万";
        } else if (amount < 1_0000_0000_0000L && amount > -1_0000_0000_0000L) {
            res = MathKit.div(amount, 1_0000_0000, 2) + Symbol.L_ONE_HUNDRED_MILLION;
        } else {
            res = MathKit.div(amount, 1_0000_0000_0000L, 2) + Symbol.L_TEN_THOUSAND + Symbol.L_ONE_HUNDRED_MILLION;
        }
        return res;
    }

    /**
     * Converts a numeric character to its Chinese numeral equivalent. Non-numeric characters are returned as is.
     *
     * @param c                The numeric character ('0'-'9').
     * @param isUseTraditional Whether to use traditional characters.
     * @return The Chinese numeral character.
     */
    public static char formatChar(final char c, final boolean isUseTraditional) {
        if (c < '0' || c > '9') {
            return c;
        }
        return singleNumberToChinese(c - Symbol.C_ZERO, isUseTraditional);
    }

    /**
     * Gets the default instance of {@link ChineseNumberFormatter}.
     *
     * @return A new {@link ChineseNumberFormatter} instance.
     */
    public static ChineseNumberFormatter of() {
        return new ChineseNumberFormatter();
    }

    /**
     * Converts a single digit to its Chinese character representation.
     *
     * @param number           The digit (0-9).
     * @param isUseTraditional Whether to use traditional characters.
     * @return The Chinese character.
     */
    private static char singleNumberToChinese(final int number, final boolean isUseTraditional) {
        if (0 == number) {
            return DIGITS[0];
        }
        // Simplified characters are at odd indices, traditional at even (except for zero).
        return DIGITS[number * 2 - (isUseTraditional ? 0 : 1)];
    }

    /**
     * Adds a '零' (zero) character to the beginning of the string if it's needed to bridge gaps between different place
     * value sections (e.g., between thousands and tens in 1010).
     *
     * @param chineseStr The string builder to modify.
     */
    private static void addPreZero(final StringBuilder chineseStr) {
        if (StringKit.isEmpty(chineseStr)) {
            return;
        }
        if (Symbol.C_UL_ZERO != chineseStr.charAt(0)) {
            chineseStr.insert(0, Symbol.C_UL_ZERO);
        }
    }

    /**
     * Sets whether to use traditional characters (financial format), e.g., 壹拾贰圆叁角贰分.
     *
     * @param useTraditional {@code true} to use traditional characters.
     * @return this instance for chaining.
     */
    public ChineseNumberFormatter setUseTraditional(final boolean useTraditional) {
        this.useTraditional = useTraditional;
        return this;
    }

    /**
     * Sets whether to use money mode, which adds currency units like 元(Yuan), 角(Jiao), 分(Fen).
     *
     * @param moneyMode {@code true} to use money mode.
     * @return this instance for chaining.
     */
    public ChineseNumberFormatter setMoneyMode(final boolean moneyMode) {
        this.moneyMode = moneyMode;
        return this;
    }

    /**
     * Sets whether to use colloquial mode, which simplifies numbers. For example, "一十一" (yī shí yī) becomes "十一" (shí
     * yī) for 11.
     *
     * @param colloquialMode {@code true} to use colloquial mode.
     * @return this instance for chaining.
     */
    public ChineseNumberFormatter setColloquialMode(final boolean colloquialMode) {
        this.colloquialMode = colloquialMode;
        return this;
    }

    /**
     * Sets the name for representing negative numbers (default is "负").
     *
     * @param negativeName The name for negative numbers (must not be null).
     * @return this instance for chaining.
     */
    public ChineseNumberFormatter setNegativeName(final String negativeName) {
        this.negativeName = Assert.notNull(negativeName);
        return this;
    }

    /**
     * Sets the currency unit name (default is "元"). Can be set to "圆" for formal contexts.
     *
     * @param unitName The currency unit name.
     * @return this instance for chaining.
     */
    public ChineseNumberFormatter setUnitName(final String unitName) {
        this.unitName = Assert.notNull(unitName);
        return this;
    }

    /**
     * Converts a {@link BigDecimal} to its Chinese representation. Supports integers and decimals.
     *
     * @param amount The number to format.
     * @return The formatted Chinese string.
     */
    public String format(final BigDecimal amount) {
        String formatAmount;
        if (amount.scale() <= 0) {
            formatAmount = format(amount.longValue());
        } else {
            final List<String> numberList = CharsBacker.split(amount.toPlainString(), Symbol.DOT);
            final StringBuilder decimalPartStr = new StringBuilder();
            for (final char decimalChar : numberList.get(1).toCharArray()) {
                decimalPartStr.append(formatChar(decimalChar, this.useTraditional));
            }
            formatAmount = format(amount.longValue()) + "点" + decimalPartStr;
        }

        return formatAmount;
    }

    /**
     * Converts a double to its Chinese representation, often used for financial amounts. For example, -12.32 could be
     * formatted as "(负数)壹拾贰圆叁角贰分".
     *
     * @param amount The number to format.
     * @return The formatted string.
     */
    public String format(double amount) {
        if (0 == amount) {
            return this.moneyMode ? "零" + unitName + "整" : String.valueOf(DIGITS[0]);
        }
        Assert.checkBetween(
                amount,
                -99_9999_9999_9999.99,
                99_9999_9999_9999.99,
                "Number is out of range: (-99999999999999.99 ~ 99999999999999.99)");

        final StringBuilder chineseStr = new StringBuilder();

        if (amount < 0) {
            chineseStr.append(this.negativeName);
            amount = -amount;
        }

        long yuan = Math.round(amount * 100);
        final int fen = (int) (yuan % 10);
        yuan /= 10;
        final int jiao = (int) (yuan % 10);
        yuan /= 10;

        final boolean isMoneyMode = this.moneyMode;
        if (!isMoneyMode || 0 != yuan) {
            chineseStr.append(longToChinese(yuan));
            if (isMoneyMode) {
                chineseStr.append(this.unitName);
            }
        }

        if (0 == jiao && 0 == fen) {
            if (isMoneyMode) {
                chineseStr.append(Symbol.CNY_ZHENG);
            }
            return chineseStr.toString();
        }

        if (!isMoneyMode) {
            chineseStr.append("点");
        }

        if (0 == yuan && 0 == jiao) {
            if (!isMoneyMode) {
                chineseStr.append(DIGITS[0]);
            }
        } else {
            chineseStr.append(singleNumberToChinese(jiao, this.useTraditional));
            if (isMoneyMode && 0 != jiao) {
                chineseStr.append(Symbol.CNY_JIAO);
            }
        }

        if (0 != fen) {
            chineseStr.append(singleNumberToChinese(fen, this.useTraditional));
            if (isMoneyMode) {
                chineseStr.append(Symbol.CNY_FEN);
            }
        }
        return chineseStr.toString();
    }

    /**
     * Converts the integer part of a positive number to its Chinese numeral string.
     *
     * @param amount The number to convert.
     * @return The Chinese numeral string.
     */
    private String longToChinese(long amount) {
        if (0 == amount) {
            return String.valueOf(DIGITS[0]);
        }

        if (amount >= 10 && amount < 20 && this.colloquialMode) {
            return thousandToChinese((int) amount).substring(1);
        }

        final int[] parts = new int[4];
        for (int i = 0; amount != 0; i++) {
            parts[i] = (int) (amount % 10000);
            amount /= 10000;
        }

        final StringBuilder chineseStr = new StringBuilder();
        int partValue;
        String partChinese;

        partValue = parts[0];
        if (partValue > 0) {
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese);
            if (partValue < 1000) {
                addPreZero(chineseStr);
            }
        }

        partValue = parts[1];
        if (partValue > 0) {
            if ((partValue % 10 == 0 && parts[0] > 0)) {
                addPreZero(chineseStr);
            }
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_TEN_THOUSAND);
            if (partValue < 1000) {
                addPreZero(chineseStr);
            }
        } else {
            addPreZero(chineseStr);
        }

        partValue = parts[2];
        if (partValue > 0) {
            if ((partValue % 10 == 0 && parts[1] > 0)) {
                addPreZero(chineseStr);
            }
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_ONE_HUNDRED_MILLION);
            if (partValue < 1000) {
                addPreZero(chineseStr);
            }
        } else {
            addPreZero(chineseStr);
        }

        partValue = parts[3];
        if (partValue > 0) {
            if (parts[2] == 0) {
                chineseStr.insert(0, Symbol.L_ONE_HUNDRED_MILLION);
            }
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_TEN_THOUSAND);
        }

        if (!chineseStr.isEmpty() && Symbol.C_UL_ZERO == chineseStr.charAt(0)) {
            return chineseStr.substring(1);
        }
        return chineseStr.toString();
    }

    /**
     * Converts an integer between 0 and 9999 to its Chinese numeral string representation.
     *
     * @param amountPart The numeric part to convert (must be < 10000).
     * @return The converted Chinese numeral string.
     */
    private String thousandToChinese(final int amountPart) {
        if (amountPart == 0) {
            return String.valueOf(DIGITS[0]);
        }

        int temp = amountPart;
        final StringBuilder chineseStr = new StringBuilder();
        boolean lastIsZero = true;
        for (int i = 0; temp > 0; i++) {
            final int digit = temp % 10;
            if (digit == 0) {
                if (!lastIsZero) {
                    chineseStr.insert(0, Symbol.UL_ZERO);
                }
                lastIsZero = true;
            } else {
                chineseStr.insert(
                        0,
                        singleNumberToChinese(digit, this.useTraditional)
                                + ChineseNumberParser.getUnitName(i, this.useTraditional));
                lastIsZero = false;
            }
            temp /= 10;
        }
        return chineseStr.toString();
    }

}
