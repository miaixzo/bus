/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;

/**
 * 数字转中文类
 * 包括：
 * <pre>
 * 1. 数字转中文大写形式,比如一百二十一
 * 2. 数字转金额用的大写形式,比如：壹佰贰拾壹
 * 3. 转金额形式,比如：壹佰贰拾壹整
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public class NumberChinese {

    /**
     * 中文形式，奇数位置是简体，偶数位置是记账繁体，0共用
     * 使用混合数组提高效率和数组复用
     */
    private static final char[] CHINESE_DIGITS_VALUE = {
            Symbol.C_UL_ZERO, Symbol.C_L_ONE, Symbol.C_U_ONE, Symbol.C_L_TWO, Symbol.C_U_TWO, Symbol.C_L_THREE, Symbol.C_U_THREE,
            Symbol.C_L_FOUR, Symbol.C_U_FOUR, Symbol.C_L_FIVE, Symbol.C_U_FIVE, Symbol.C_L_SIX, Symbol.C_U_SIX, Symbol.C_L_SEVEN,
            Symbol.C_U_SEVEN, Symbol.C_L_EIGHT, Symbol.C_U_EIGHT, Symbol.C_L_NINE, Symbol.C_U_NINE
    };

    /**
     * 汉字转阿拉伯数字的
     */
    private static final NameValue[] CHINESE_NAME_VALUE = {
            new NameValue(Symbol.C_SPACE, 1, false),
            new NameValue(Symbol.C_L_TEN, 10, false),
            new NameValue(Symbol.C_U_TEN, 10, false),
            new NameValue('百', 100, false),
            new NameValue('佰', 100, false),
            new NameValue('千', 1000, false),
            new NameValue('仟', 1000, false),
            new NameValue('万', 1_0000, true),
            new NameValue('亿', 1_0000_0000, true),
    };

    /**
     * 阿拉伯数字转换成中文,小数点后四舍五入保留两位. 使用于整数、小数的转换.
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @return 中文
     */
    public static String format(double amount, boolean isUseTraditional) {
        return format(amount, isUseTraditional, false);
    }

    /**
     * 阿拉伯数字转换成中文,小数点后四舍五入保留两位. 使用于整数、小数的转换.
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @param isMoneyMode      是否为金额模式
     * @return 中文
     */
    public static String format(double amount, boolean isUseTraditional, boolean isMoneyMode) {
        if (amount > 99_9999_9999_9999.99 || amount < -99999999999999.99) {
            throw new IllegalArgumentException("Number support only: (-99999999999999.99 ～ 99999999999999.99)！");
        }

        // 负数
        boolean negative = false;
        if (amount < 0) {
            negative = true;
            amount = -amount;
        }

        // 分和角
        long temp = Math.round(amount * 100);

        final int numFen = (int) (temp % 10);
        temp = temp / 10;
        final int numJiao = (int) (temp % 10);
        temp = temp / 10;

        final StringBuilder chineseStr = new StringBuilder(toChinese(temp, isUseTraditional));
        //负数
        if (negative) { // 整数部分不为 0
            chineseStr.insert(0, "负");
        }

        // 小数部分
        if (numFen != 0 || numJiao != 0) {
            if (numFen == 0) {
                chineseStr.append(isMoneyMode ? "元" : "点").append(numberToChinese(numJiao, isUseTraditional)).append(isMoneyMode ? "角" : "");
            } else { // “分”数不为 0
                if (numJiao == 0) {
                    chineseStr.append(isMoneyMode ? "元零" : "点零").append(numberToChinese(numFen, isUseTraditional)).append(isMoneyMode ? "分" : "");
                } else {
                    chineseStr.append(isMoneyMode ? "元" : "点").append(numberToChinese(numJiao, isUseTraditional)).append(isMoneyMode ? "角" : "").append(numberToChinese(numFen, isUseTraditional)).append(isMoneyMode ? "分" : "");
                }
            }
        } else if (isMoneyMode) {
            //无小数部分的金额结尾
            chineseStr.append("元整");
        }

        return chineseStr.toString();
    }

    /**
     * 数字字符转中文，非数字字符原样返回
     *
     * @param c                数字字符
     * @param isUseTraditional 是否繁体
     * @return 中文字符
     */
    public static String toChinese(char c, boolean isUseTraditional) {
        if (c < '0' || c > '9') {
            return String.valueOf(c);
        }
        return String.valueOf(numberToChinese(c - '0', isUseTraditional));
    }

    /**
     * 阿拉伯数字整数部分转换成中文，只支持正数
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @return 中文
     */
    private static String toChinese(long amount, boolean isUseTraditional) {
        if (0 == amount) {
            return "零";
        }

        //将数字以万为单位分为多份
        int[] parts = new int[4];
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
            partChinese = toChinese(partValue, isUseTraditional);
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
            partChinese = toChinese(partValue, isUseTraditional);
            chineseStr.insert(0, partChinese + "万");

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

            partChinese = toChinese(partValue, isUseTraditional);
            chineseStr.insert(0, partChinese + "亿");

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
                chineseStr.insert(0, "亿");
            }
            partChinese = toChinese(partValue, isUseTraditional);
            chineseStr.insert(0, partChinese + "万");
        }

        if (StringKit.isNotEmpty(chineseStr) && '零' == chineseStr.charAt(0)) {
            return chineseStr.substring(1);
        }

        return chineseStr.toString();
    }

    /**
     * 把一个 0~9999 之间的整数转换为汉字的字符串，如果是 0 则返回 ""
     *
     * @param amountPart       数字部分
     * @param isUseTraditional 是否使用繁体单位
     * @return 转换后的汉字
     */
    private static String toChinese(int amountPart, boolean isUseTraditional) {
        int temp = amountPart;

        StringBuilder chineseStr = new StringBuilder();
        boolean lastIsZero = true; // 在从低位往高位循环时，记录上一位数字是不是 0
        for (int i = 0; temp > 0; i++) {
            int digit = temp % 10;
            if (digit == 0) { // 取到的数字为 0
                if (false == lastIsZero) {
                    // 前一个数字不是 0，则在当前汉字串前加“零”字;
                    chineseStr.insert(0, "零");
                }
                lastIsZero = true;
            } else { // 取到的数字不是 0
                chineseStr.insert(0, numberToChinese(digit, isUseTraditional) + getUnitName(i, isUseTraditional));
                lastIsZero = false;
            }
            temp = temp / 10;
        }
        return chineseStr.toString();
    }

    /**
     * 把中文转换为数字 如 二百二十 220
     * 见：https://www.d5.nz/read/sfdlq/text-part0000_split_030.html
     * <ul>
     *     <li>一百一十二 -》 112</li>
     *     <li>一千零一十二 -》 1012</li>
     * </ul>
     *
     * @param chinese 中文字符
     * @return 数字
     */
    public static int chineseToNumber(String chinese) {
        final int length = chinese.length();
        int result = 0;

        // 节总和
        int section = 0;
        int number = 0;
        NameValue unit = null;
        char c;
        for (int i = 0; i < length; i++) {
            c = chinese.charAt(i);
            final int num = chineseToNumber(c);
            if (num >= 0) {
                if (num == 0) {
                    // 遇到零时节结束，权位失效，比如两万二零一十
                    if (number > 0 && null != unit) {
                        section += number * (unit.value / 10);
                    }
                    unit = null;
                } else if (number > 0) {
                    // 多个数字同时出现，报错
                    throw new IllegalArgumentException(StringKit.format("Bad number '{}{}' at: {}", chinese.charAt(i - 1), c, i));
                }
                // 普通数字
                number = num;
            } else {
                unit = chineseToUnit(c);
                if (null == unit) {
                    // 出现非法字符
                    throw new IllegalArgumentException(StringKit.format("Unknown unit '{}' at: {}", c, i));
                }

                //单位
                if (unit.unit) {
                    // 节单位，按照节求和
                    section = (section + number) * unit.value;
                    result += section;
                    section = 0;
                } else {
                    // 非节单位，和单位前的单数字组合为值
                    int unitNumber = number;
                    if (0 == number && 0 == i) {
                        // issue#1726，对于单位开头的数组，默认赋予1
                        // 十二 -> 一十二
                        // 百二 -> 一百二
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
     * 查找对应的权对象
     *
     * @param chinese 中文权位名
     * @return 权对象
     */
    private static NameValue chineseToUnit(char chinese) {
        for (NameValue chineseNameValue : CHINESE_NAME_VALUE) {
            if (chineseNameValue.name == chinese) {
                return chineseNameValue;
            }
        }
        return null;
    }

    /**
     * 将汉字单个数字转换为int类型数字
     *
     * @param chinese 汉字数字，支持简体和繁体
     * @return 数字，-1表示未找到
     */
    private static int chineseToNumber(char chinese) {
        if ('两' == chinese) {
            // 口语纠正
            chinese = '二';
        }
        final int i = ArrayKit.indexOf(CHINESE_DIGITS_VALUE, chinese);
        if (i > 0) {
            return (i + 1) / 2;
        }
        return i;
    }

    /**
     * 单个数字转汉字
     *
     * @param number           数字
     * @param isUseTraditional 是否使用繁体
     * @return 汉字
     */
    private static char numberToChinese(int number, boolean isUseTraditional) {
        if (0 == number) {
            return CHINESE_DIGITS_VALUE[0];
        }
        return CHINESE_DIGITS_VALUE[number * 2 - (isUseTraditional ? 0 : 1)];
    }

    /**
     * 获取对应级别的单位
     *
     * @param index            级别，0表示各位，1表示十位，2表示百位，以此类推
     * @param isUseTraditional 是否使用繁体
     * @return 单位
     */
    private static String getUnitName(int index, boolean isUseTraditional) {
        if (0 == index) {
            return Normal.EMPTY;
        }
        return String.valueOf(CHINESE_NAME_VALUE[index * 2 - (isUseTraditional ? 0 : 1)].name);
    }

    private static void addPreZero(StringBuilder chineseStr) {
        if (StringKit.isEmpty(chineseStr)) {
            return;
        }
        final char c = chineseStr.charAt(0);
        if (Symbol.C_UL_ZERO != c) {
            chineseStr.insert(0, Symbol.C_UL_ZERO);
        }
    }

    /**
     * 权位
     */
    private static class NameValue {
        /**
         * 中文权名称
         */
        private final char name;
        /**
         * 10的倍数值
         */
        private final int value;
        /**
         * 是否为节权位，它不是与之相邻的数字的倍数，而是整个小节的倍数
         * 例如二十三万，万是节权位，与三无关，而和二十三关联
         */
        private final boolean unit;

        /**
         * 构造
         *
         * @param name  名称
         * @param value 值，即10的倍数
         * @param unit  是否为节权位
         */
        public NameValue(char name, int value, boolean unit) {
            this.name = name;
            this.value = value;
            this.unit = unit;
        }
    }

}
