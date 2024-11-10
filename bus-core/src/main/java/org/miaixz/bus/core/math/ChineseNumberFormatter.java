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
package org.miaixz.bus.core.math;

import java.math.BigDecimal;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数字转中文类 包括：
 * 
 * <pre>
 * 1. 数字转中文大写形式，比如一百二十一
 * 2. 数字转金额用的大写形式，比如：壹佰贰拾壹
 * 3. 转金额形式，比如：壹佰贰拾壹整
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChineseNumberFormatter {

    /**
     * 中文形式，奇数位置是简体，偶数位置是记账繁体，0共用 使用混合数组提高效率和数组复用
     */
    static final char[] DIGITS = { '零', '一', '壹', '二', '贰', '三', '叁', '四', '肆', '五', '伍', '六', '陆', '七', '柒', '八', '捌',
            '九', '玖' };
    private boolean useTraditional;
    private boolean moneyMode;
    private boolean colloquialMode;
    private String negativeName = "负";
    private String unitName = "元";

    /**
     * 阿拉伯数字（支持正负整数）四舍五入后转换成中文节权位简洁计数单位，例如 -5_5555 = -5.56万
     *
     * @param amount 数字
     * @return 中文
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
     * 数字字符转中文，非数字字符原样返回
     *
     * @param c                数字字符
     * @param isUseTraditional 是否繁体
     * @return 中文字符
     */
    public static char formatChar(final char c, final boolean isUseTraditional) {
        if (c < '0' || c > '9') {
            return c;
        }
        return singleNumberToChinese(c - Symbol.C_ZERO, isUseTraditional);
    }

    /**
     * 获取 NumberChineseFormatter 默认对象
     *
     * @return NumberChineseFormatter
     */
    public static ChineseNumberFormatter of() {
        return new ChineseNumberFormatter();
    }

    /**
     * 单个数字转汉字
     *
     * @param number           数字
     * @param isUseTraditional 是否使用繁体
     * @return 汉字
     */
    private static char singleNumberToChinese(final int number, final boolean isUseTraditional) {
        if (0 == number) {
            return DIGITS[0];
        }
        return DIGITS[number * 2 - (isUseTraditional ? 0 : 1)];
    }

    private static void addPreZero(final StringBuilder chineseStr) {
        if (StringKit.isEmpty(chineseStr)) {
            return;
        }
        if (Symbol.C_UL_ZERO != chineseStr.charAt(0)) {
            chineseStr.insert(0, Symbol.C_UL_ZERO);
        }
    }

    /**
     * 是否使用繁体，即金额表示模式，如：壹拾贰圆叁角贰分
     *
     * @param useTraditional 是否使用繁体
     * @return this
     */
    public ChineseNumberFormatter setUseTraditional(final boolean useTraditional) {
        this.useTraditional = useTraditional;
        return this;
    }

    /**
     * 是否使用金额模式，，如：壹拾贰圆
     *
     * @param moneyMode 是否使用金额模式
     * @return this
     */
    public ChineseNumberFormatter setMoneyMode(final boolean moneyMode) {
        this.moneyMode = moneyMode;
        return this;
    }

    /**
     * 是否使用口语模式，此模式下的数字更加简化，如“一十一”会表示为“十一”
     *
     * @param colloquialMode 是否口语模式
     * @return this
     */
    public ChineseNumberFormatter setColloquialMode(final boolean colloquialMode) {
        this.colloquialMode = colloquialMode;
        return this;
    }

    /**
     * 设置负数的表示名称，如"负"
     *
     * @param negativeName 负数表示名称，非空
     * @return this
     */
    public ChineseNumberFormatter setNegativeName(final String negativeName) {
        this.negativeName = Assert.notNull(negativeName);
        return this;
    }

    /**
     * 设置金额单位名称，如：“元”或“圆”
     *
     * @param unitName 金额单位名称
     * @return this
     */
    public ChineseNumberFormatter setUnitName(final String unitName) {
        this.unitName = Assert.notNull(unitName);
        return this;
    }

    /**
     * 阿拉伯数字转换成中文. 使用于整数、小数的转换. 支持多位小数
     *
     * @param amount 数字
     * @return 中文
     */
    public String format(final BigDecimal amount) {
        final long longValue = amount.longValue();

        String formatAmount;
        if (amount.scale() <= 0) {
            formatAmount = format(longValue);
        } else {
            final List<String> numberList = CharsBacker.split(amount.toPlainString(), Symbol.DOT);
            // 小数部分逐个数字转换为汉字
            final StringBuilder decimalPartStr = new StringBuilder();
            for (final char decimalChar : numberList.get(1).toCharArray()) {
                decimalPartStr.append(formatChar(decimalChar, this.useTraditional));
            }
            formatAmount = format(longValue) + "点" + decimalPartStr;
        }

        return formatAmount;
    }

    /**
     * 阿拉伯数字转换成中文
     *
     * <p>
     * 主要是对发票票面金额转换的扩展
     * <p>
     * 如：-10.10
     * <p>
     * 发票票面转换为：(负数)壹拾贰圆叁角贰分
     * <p>
     * 而非：负壹拾贰元叁角贰分
     * <p>
     * 共两点不同：1、(负数) 而非 负；2、圆 而非 元
     *
     * @param amount 数字
     * @return 格式化后的字符串
     */
    public String format(double amount) {
        if (0 == amount) {
            return this.moneyMode ? "零元整" : Symbol.UL_ZERO;
        }
        Assert.checkBetween(amount, -99_9999_9999_9999.99, 99_9999_9999_9999.99,
                "Number support only: (-99999999999999.99 ~ 99999999999999.99)！");

        final StringBuilder chineseStr = new StringBuilder();

        // 负数
        if (amount < 0) {
            chineseStr.append(this.negativeName);
            amount = -amount;
        }

        long yuan = Math.round(amount * 100);
        final int fen = (int) (yuan % 10);
        yuan = yuan / 10;
        final int jiao = (int) (yuan % 10);
        yuan = yuan / 10;

        final boolean isMoneyMode = this.moneyMode;
        // 元
        if (!isMoneyMode || 0 != yuan) {
            // 金额模式下，无需“零元”
            chineseStr.append(longToChinese(yuan));
            if (isMoneyMode) {
                chineseStr.append(this.unitName);
            }
        }

        if (0 == jiao && 0 == fen) {
            // 无小数部分的金额结尾
            if (isMoneyMode) {
                chineseStr.append(Symbol.CNY_ZHENG);
            }
            return chineseStr.toString();
        }

        // 小数部分
        if (!isMoneyMode) {
            chineseStr.append("点");
        }

        // 角
        if (0 == yuan && 0 == jiao) {
            // 元和角都为0时，只有非金额模式下补“零”
            if (!isMoneyMode) {
                chineseStr.append(Symbol.UL_ZERO);
            }
        } else {
            chineseStr.append(singleNumberToChinese(jiao, this.useTraditional));
            if (isMoneyMode && 0 != jiao) {
                chineseStr.append(Symbol.CNY_JIAO);
            }
        }

        // 分
        if (0 != fen) {
            chineseStr.append(singleNumberToChinese(fen, this.useTraditional));
            if (isMoneyMode) {
                chineseStr.append(Symbol.CNY_FEN);
            }
        }

        return chineseStr.toString();
    }

    /**
     * 阿拉伯数字整数部分转换成中文，只支持正数
     *
     * @param amount 数字
     * @return 中文
     */
    private String longToChinese(long amount) {
        if (0 == amount) {
            return Symbol.UL_ZERO;
        }

        // 对于10~20，可选口语模式，如一十一，口语模式下为十一
        if (amount < 20 && amount >= 10) {
            final String chinese = thousandToChinese((int) amount);
            // "十一"而非"一十一"
            return this.colloquialMode ? chinese.substring(1) : chinese;
        }

        // 将数字以万为单位分为多份
        final int[] parts = new int[4];
        for (int i = 0; amount != 0; i++) {
            parts[i] = (int) (amount % 10000);
            amount = amount / 10000;
        }

        final StringBuilder chineseStr = new StringBuilder();
        int partValue;
        String partChinese;

        // 千
        partValue = parts[0];
        if (partValue > 0) {
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese);

            if (partValue < 1000) {
                // 和万位之间空0，则补零，如一万零三百
                addPreZero(chineseStr);
            }
        }

        // 万
        partValue = parts[1];
        if (partValue > 0) {
            if ((partValue % 10 == 0 && parts[0] > 0)) {
                // 如果"万"的个位是0，则补零，如十万零八千
                addPreZero(chineseStr);
            }
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_TEN_THOUSAND);

            if (partValue < 1000) {
                // 和亿位之间空0，则补零，如一亿零三百万
                addPreZero(chineseStr);
            }
        } else {
            addPreZero(chineseStr);
        }

        // 亿
        partValue = parts[2];
        if (partValue > 0) {
            if ((partValue % 10 == 0 && parts[1] > 0)) {
                // 如果"万"的个位是0，则补零，如十万零八千
                addPreZero(chineseStr);
            }

            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_ONE_HUNDRED_MILLION);

            if (partValue < 1000) {
                // 和万亿位之间空0，则补零，如一万亿零三百亿
                addPreZero(chineseStr);
            }
        } else {
            addPreZero(chineseStr);
        }

        // 万亿
        partValue = parts[3];
        if (partValue > 0) {
            if (parts[2] == 0) {
                chineseStr.insert(0, Symbol.L_ONE_HUNDRED_MILLION);
            }
            partChinese = thousandToChinese(partValue);
            chineseStr.insert(0, partChinese + Symbol.L_TEN_THOUSAND);
        }

        if (StringKit.isNotEmpty(chineseStr) && Symbol.C_UL_ZERO == chineseStr.charAt(0)) {
            return chineseStr.substring(1);
        }

        return chineseStr.toString();
    }

    /**
     * 把一个 0~9999 之间的整数转换为汉字的字符串，如果是 0 则返回 ""
     *
     * @param amountPart 数字部分
     * @return 转换后的汉字
     */
    private String thousandToChinese(final int amountPart) {
        if (amountPart == 0) {
            return String.valueOf(DIGITS[0]);
        }

        int temp = amountPart;

        final StringBuilder chineseStr = new StringBuilder();
        boolean lastIsZero = true; // 在从低位往高位循环时，记录上一位数字是不是 0
        for (int i = 0; temp > 0; i++) {
            final int digit = temp % 10;
            if (digit == 0) { // 取到的数字为 0
                if (!lastIsZero) {
                    // 前一个数字不是 0，则在当前汉字串前加“零”字;
                    chineseStr.insert(0, Symbol.UL_ZERO);
                }
                lastIsZero = true;
            } else { // 取到的数字不是 0
                final boolean isUseTraditional = this.useTraditional;
                chineseStr.insert(0, singleNumberToChinese(digit, isUseTraditional)
                        + ChineseNumberParser.getUnitName(i, isUseTraditional));
                lastIsZero = false;
            }
            temp = temp / 10;
        }
        return chineseStr.toString();
    }

}
