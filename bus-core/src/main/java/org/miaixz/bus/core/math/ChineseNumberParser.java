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
import java.math.RoundingMode;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A parser for Chinese numbers or currency amounts.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChineseNumberParser {

    /**
     * Constructs a new ChineseNumberParser. Utility class constructor for static access.
     */
    private ChineseNumberParser() {
    }

    /**
     * Mapping of Chinese characters to their numeric values and unit properties.
     */
    private static final ChineseUnit[] CHINESE_NAME_VALUE = { new ChineseUnit(Symbol.C_SPACE, 1, false),
            new ChineseUnit('十', 10, false), new ChineseUnit('拾', 10, false), new ChineseUnit('百', 100, false),
            new ChineseUnit('佰', 100, false), new ChineseUnit('千', 1000, false), new ChineseUnit('仟', 1000, false),
            new ChineseUnit('万', 1_0000, true), new ChineseUnit('亿', 1_0000_0000, true), };

    /**
     * Converts a Chinese numeral string to a BigDecimal. For example, "二百二十" becomes 220.
     * <ul>
     * <li>"一百一十二" -> 112</li>
     * <li>"一千零一十二" -> 1012</li>
     * </ul>
     *
     * @param chinese The Chinese numeral string.
     * @return The corresponding BigDecimal.
     */
    public static BigDecimal parseFromChinese(final String chinese) {
        if (StringKit.containsAny(chinese, '元', '圆', '角', '分')) {
            return parseFromChineseMoney(chinese);
        }

        return parseFromChineseNumber(chinese);
    }

    /**
     * Converts a Chinese numeral string (including decimals) to a BigDecimal.
     * <ul>
     * <li>"一百一十二" -> 112</li>
     * <li>"一千零一十二" -> 1012</li>
     * <li>"十二点二三" -> 12.23</li>
     * <li>"三点一四一五九二六五四" -> 3.141592654</li>
     * </ul>
     *
     * @param chinese The Chinese numeral string.
     * @return The corresponding BigDecimal.
     */
    public static BigDecimal parseFromChineseNumber(final String chinese) {
        Assert.notBlank(chinese, "Chinese number is blank!");
        final int dotIndex = chinese.indexOf('点');

        // Integer part
        final char[] charArray = chinese.toCharArray();
        BigDecimal result = MathKit
                .toBigDecimal(parseLongFromChineseNumber(charArray, 0, dotIndex > 0 ? dotIndex : charArray.length));

        // Decimal part
        if (dotIndex > 0) {
            final int length = chinese.length();
            for (int i = dotIndex + 1; i < length; i++) {
                // The number of decimal places depends on the actual number of digits.
                // result = result + (numberChar / 10^(i-dotIndex))
                result = result.add(
                        MathKit.div(
                                chineseToNumber(chinese.charAt(i)),
                                BigDecimal.TEN.pow(i - dotIndex),
                                (length - dotIndex + 1)));
            }
        }

        return result.stripTrailingZeros();
    }

    /**
     * Converts a Chinese currency string to a BigDecimal, with the result in Yuan. For example: "陆万柒仟伍佰伍拾陆元叁角贰分"
     * returns "67556.32", "叁角贰分" returns "0.32".
     *
     * @param chineseMoneyAmount The Chinese currency string.
     * @return A BigDecimal representing the amount in Yuan.
     */
    public static BigDecimal parseFromChineseMoney(final String chineseMoneyAmount) {
        if (StringKit.isBlank(chineseMoneyAmount)) {
            return null;
        }

        final char[] charArray = chineseMoneyAmount.toCharArray();
        int yEnd = ArrayKit.indexOf(charArray, '元');
        if (yEnd < 0) {
            yEnd = ArrayKit.indexOf(charArray, '圆');
        }

        // First, find the number part for Yuan.
        long y = 0;
        if (yEnd > 0) {
            y = parseLongFromChineseNumber(charArray, 0, yEnd);
        }

        // Then, find the number part for Jiao.
        long j = 0;
        final int jEnd = ArrayKit.indexOf(charArray, '角');
        if (jEnd > 0) {
            if (yEnd >= 0) {
                // If Yuan exists, Jiao must come after it.
                if (jEnd > yEnd) {
                    j = parseLongFromChineseNumber(charArray, yEnd + 1, jEnd);
                }
            } else {
                // No Yuan, only Jiao.
                j = parseLongFromChineseNumber(charArray, 0, jEnd);
            }
        }

        // Then, find the number part for Fen.
        long f = 0;
        final int fEnd = ArrayKit.indexOf(charArray, '分');
        if (fEnd > 0) {
            if (jEnd >= 0) {
                // If Jiao exists, Fen must come after it.
                if (fEnd > jEnd) {
                    f = parseLongFromChineseNumber(charArray, jEnd + 1, fEnd);
                }
            } else if (yEnd > 0) {
                // No Jiao, but Yuan exists, so search after Yuan.
                if (fEnd > yEnd) {
                    f = parseLongFromChineseNumber(charArray, yEnd + 1, fEnd);
                }
            } else {
                // No Yuan or Jiao, only Fen.
                f = parseLongFromChineseNumber(charArray, 0, fEnd);
            }
        }

        BigDecimal amount = new BigDecimal(y);
        amount = amount.add(BigDecimal.valueOf(j).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP));
        amount = amount.add(BigDecimal.valueOf(f).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return amount;
    }

    /**
     * Converts a Chinese integer string to a long. For example, "二百二十" -> 220.
     * <ul>
     * <li>"一百一十二" -> 112</li>
     * <li>"一千零一十二" -> 1012</li>
     * </ul>
     *
     * @param chinese    The char array of the Chinese numeral string.
     * @param beginIndex The start index.
     * @param toIndex    The end index (exclusive). For an integer, this is the length; for a decimal, this is the
     *                   position of the decimal point.
     * @return The parsed long.
     */
    public static long parseLongFromChineseNumber(final char[] chinese, final int beginIndex, final int toIndex) {
        long result = 0;

        // Section total.
        long section = 0;
        long number = 0;
        ChineseUnit unit = null;
        char c;
        for (int i = beginIndex; i < toIndex; i++) {
            c = chinese[i];
            final int num = chineseToNumber(c);
            if (num >= 0) {
                if (num == 0) {
                    // When a zero is encountered, the section ends, and the place value becomes invalid.
                    if (number > 0 && null != unit) {
                        section += number * (unit.value / 10);
                    }
                    unit = null;
                } else if (number > 0) {
                    // If multiple digits appear consecutively, throw an error.
                    throw new IllegalArgumentException(
                            StringKit.format("Bad number '{}{}' at: {}", chinese[i - 1], c, i));
                }
                // Normal digit.
                number = num;
            } else {
                unit = chineseToUnit(c);
                if (null == unit) {
                    // Illegal character found.
                    throw new IllegalArgumentException(StringKit.format("Unknown unit '{}' at: {}", c, i));
                }

                // Unit.
                if (unit.secUnit) {
                    // Section unit, sum by section.
                    section = (section + number) * unit.value;
                    result += section;
                    section = 0;
                } else {
                    // Not a section unit, combine with the single digit before the unit.
                    long unitNumber = number;
                    if (0 == number && 0 == i) {
                        // For a string starting with a unit, default to 1.
                        // e.g., "十二" -> "一十二" (12), "百二" -> "一百二" (120)
                        unitNumber = 1;
                    }
                    section += (unitNumber * unit.value);
                }
                number = 0;
            }
        }

        if (number > 0 && null != unit) {
            number = number * (unit.value / 10);
        }

        return result + section + number;
    }

    /**
     * Finds the corresponding unit object for a Chinese unit character.
     *
     * @param chinese The Chinese unit character.
     * @return The ChineseUnit object, or null if not found.
     */
    private static ChineseUnit chineseToUnit(final char chinese) {
        for (final ChineseUnit chineseNameValue : CHINESE_NAME_VALUE) {
            if (chineseNameValue.name == chinese) {
                return chineseNameValue;
            }
        }
        return null;
    }

    /**
     * Converts a single Chinese digit character to an int. Supports both simplified and traditional characters.
     *
     * @param chinese The Chinese digit character.
     * @return The integer value, or -1 if not a valid digit.
     */
    private static int chineseToNumber(char chinese) {
        if ('两' == chinese) {
            // Colloquial correction for '两' (liǎng) to '二' (èr).
            chinese = '二';
        }
        final int i = ArrayKit.indexOf(ChineseNumberFormatter.DIGITS, chinese);
        if (i > 0) {
            return (i + 1) / 2;
        }
        return i;
    }

    /**
     * Gets the unit name for the corresponding level.
     *
     * @param index            The level: 0 for ones, 1 for tens, 2 for hundreds, and so on.
     * @param isUseTraditional Whether to use traditional characters.
     * @return The unit name.
     */
    static String getUnitName(final int index, final boolean isUseTraditional) {
        if (0 == index) {
            return Normal.EMPTY;
        }
        return String.valueOf(CHINESE_NAME_VALUE[index * 2 - (isUseTraditional ? 0 : 1)].name);
    }

    /**
     * Represents a unit in the Chinese number system.
     */
    private static class ChineseUnit {

        /**
         * The Chinese character for the unit.
         */
        private final char name;
        /**
         * The numeric value of the unit (a power of 10).
         */
        private final int value;
        /**
         * Indicates if it is a section unit (e.g., 万, 亿). It's not a multiplier for the adjacent digit, but for the
         * entire section. For example, in "二十三万" (230,000), "万" (10,000) is a section unit related to "二十三" (23), not
         * just "三" (3).
         */
        private final boolean secUnit;

        /**
         * Constructor.
         *
         * @param name    The name of the unit.
         * @param value   The value, a multiple of 10.
         * @param secUnit Whether it is a section unit.
         */
        public ChineseUnit(final char name, final int value, final boolean secUnit) {
            this.name = name;
            this.value = value;
            this.secUnit = secUnit;
        }
    }

}
