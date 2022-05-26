/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.ObjectKit;
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
 * @since Java 17+
 */
public class NumberFormatter {

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
     * 阿拉伯数字（支持正负整数）四舍五入后转换成中文节权位简洁计数单位，例如 -5_5555 = -5.56万
     *
     * @param amount 数字
     * @return 中文
     */
    public static String format(long amount) {
        if (amount < 1_0000 && amount > -1_0000) {
            return String.valueOf(amount);
        }
        String res;
        if (amount < 1_0000_0000 && amount > -1_0000_0000) {
            res = MathKit.div(amount, 1_0000, 2) + "万";
        } else if (amount < 1_0000_0000_0000L && amount > -1_0000_0000_0000L) {
            res = MathKit.div(amount, 1_0000_0000, 2) + "亿";
        } else {
            res = MathKit.div(amount, 1_0000_0000_0000L, 2) + "万亿";
        }
        return res;
    }

    /**
     * 格式化-999~999之间的数字
     * 这个方法显示10~19以下的数字时使用"十一"而非"一十一"
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @return 中文
     */
    public static String format(int amount, boolean isUseTraditional) {
        Assert.checkBetween(amount, -999, 999, "Number support only: (-999 ~ 999)！");
        final String chinese = toChinese(amount, isUseTraditional);
        if (amount < 20 && amount >= 10) {
            // "十一"而非"一十一"
            return chinese.substring(1);
        }
        return chinese;
    }

    /**
     * 阿拉伯数字（支持正负整数）转换成中文
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @return 中文
     */
    public static String format(long amount, boolean isUseTraditional) {
        if (0 == amount) {
            return "零";
        }
        Assert.checkBetween(amount, -99_9999_9999_9999.99, 99_9999_9999_9999.99,
                "Number support only: (-99999999999999.99 ~ 99999999999999.99)！");

        final StringBuilder chineseStr = new StringBuilder();

        // 负数
        if (amount < 0) {
            chineseStr.append("负");
            amount = -amount;
        }

        chineseStr.append(toChinese(amount, isUseTraditional));
        return chineseStr.toString();
    }

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
        return format(amount, isUseTraditional, isMoneyMode, "负", "元");
    }

    /**
     * 阿拉伯数字转换成中文,小数点后四舍五入保留两位. 使用于整数、小数的转换.
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @param isMoneyMode      是否金额模式
     * @param negativeName     负号转换名称 如：负、(负数)
     * @param unitName         单位名称 如：元、圆
     * @return 中文
     */
    public static String format(double amount, boolean isUseTraditional, boolean isMoneyMode, String negativeName, String unitName) {
        if (0 == amount) {
            return "零";
        }
        Assert.checkBetween(amount, -99_9999_9999_9999.99, 99_9999_9999_9999.99,
                "Number support only: (-99999999999999.99 ~ 99999999999999.99)！");

        final StringBuilder chineseStr = new StringBuilder();

        // 负数
        if (amount < 0) {
            chineseStr.append(StringKit.isNullOrUndefined(negativeName) ? "负" : negativeName);
            amount = -amount;
        }

        long yuan = Math.round(amount * 100);
        final int fen = (int) (yuan % 10);
        yuan = yuan / 10;
        final int jiao = (int) (yuan % 10);
        yuan = yuan / 10;

        // 元
        if (false == isMoneyMode || 0 != yuan) {
            // 金额模式下，无需“零元”
            chineseStr.append(toChinese(yuan, isUseTraditional));
            if (isMoneyMode) {
                chineseStr.append(StringKit.isNullOrUndefined(unitName) ? "元" : unitName);
            }
        }

        if (0 == jiao && 0 == fen) {
            // 无小数部分的金额结尾
            if (isMoneyMode) {
                chineseStr.append("整");
            }
            return chineseStr.toString();
        }

        // 小数部分
        if (false == isMoneyMode) {
            chineseStr.append("点");
        }

        // 角
        if (0 == yuan && 0 == jiao) {
            // 元和角都为0时，只有非金额模式下补“零”
            if (false == isMoneyMode) {
                chineseStr.append("零");
            }
        } else {
            chineseStr.append(numberToChinese(jiao, isUseTraditional));
            if (isMoneyMode && 0 != jiao) {
                chineseStr.append("角");
            }
        }

        // 分
        if (0 != fen) {
            chineseStr.append(numberToChinese(fen, isUseTraditional));
            if (isMoneyMode) {
                chineseStr.append("分");
            }
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

        // 将数字以万为单位分为多份
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
        // 在从低位往高位循环时，记录上一位数字是不是 0
        boolean lastIsZero = true;
        for (int i = 0; temp > 0; i++) {
            int digit = temp % 10;
            if (digit == 0) {
                // 取到的数字为 0
                if (false == lastIsZero) {
                    // 前一个数字不是 0，则在当前汉字串前加“零”字;
                    chineseStr.insert(0, "零");
                }
                lastIsZero = true;
            } else {
                // 取到的数字不是 0
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

                // 单位
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
     * 将阿拉伯数字转为英文表达式
     *
     * @param x 阿拉伯数字,可以为{@link Number}对象,也可以是普通对象,最后会使用字符串方式处理
     * @return 英文表达式
     */
    public static String format(Object x) {
        if (null != x) {
            return format(x.toString());
        } else {
            return Normal.EMPTY;
        }
    }

    /**
     * 将阿拉伯数字转为英文表达式
     *
     * @param x 阿拉伯数字字符串
     * @return 英文表达式
     */
    private static String format(String x) {
        // 取小数点位置
        int z = x.indexOf(Symbol.DOT);
        String lstr, rstr = Normal.EMPTY;
        // 看是否有小数,如果有,则分别取左边和右边
        if (z > -1) {
            lstr = x.substring(0, z);
            rstr = x.substring(z + 1);
        } else {
            // 否则就是全部
            lstr = x;
        }

        // 对左边的字串取反
        String lstrrev = StringKit.reverse(lstr);
        // 定义5个字串变量来存放解析出来的叁位一组的字串
        String[] a = new String[5];

        switch (lstrrev.length() % 3) {
            case 1:
                lstrrev += Symbol.ZERO + Symbol.ZERO;
                break;
            case 2:
                lstrrev += Symbol.ZERO;
                break;
        }
        // 用来存放转换後的整数部分
        String lm = Normal.EMPTY;
        for (int i = 0; i < lstrrev.length() / 3; i++) {
            // 截取第一个叁位
            a[i] = StringKit.reverse(lstrrev.substring(3 * i, 3 * i + 3));
            // 用来避免这种情况：1000000 = first million
            if (!ObjectKit.equal(a[i], "000")) {
                // thousand only
                if (i != 0) {
                    lm = transThree(a[i]) + Symbol.SPACE + parseMore(String.valueOf(i)) + Symbol.SPACE + lm; // 加:
                } else {
                    // 防止i=0时, 在多加两个空格.
                    lm = transThree(a[i]);
                }
            } else {
                lm += transThree(a[i]);
            }
        }

        // 用来存放转换後小数部分
        String xs = Normal.EMPTY;
        if (z > -1) {
            // 小数部分存在时转换小数
            xs = "AND CENTS " + transTwo(rstr) + Symbol.SPACE;
        }

        return lm.trim() + Symbol.SPACE + xs + "ONLY";
    }

    /**
     * 将阿拉伯数字转化为简洁计数单位，例如 2100 - 2.1k
     * 范围默认只到w
     *
     * @param value 被格式化的数字
     * @return 格式化后的数字
     */
    public static String formatSimple(long value) {
        return formatSimple(value, true);
    }

    /**
     * 将阿拉伯数字转化为简洁计数单位，例如 2100 - 2.1k
     *
     * @param value 对应数字的值
     * @param isTwo 控制是否为只为k、w，例如当为{@code false}时返回4.38m，{@code true}返回438.43w
     * @return 格式化后的数字
     */
    public static String formatSimple(long value, boolean isTwo) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        int index = -1;
        double res = value;
        while (res > 10 && (false == isTwo || index < 1)) {
            if (res >= 1000) {
                res = res / 1000;
                index++;
            }
            if (res > 10) {
                res = res / 10;
                index++;
            }
        }
        return String.format("%s%s", MathKit.decimalFormat("#.##", res), Normal.NUMBER_SUFFIX[index]);
    }

    private static String parseFirst(String s) {
        return Normal.EN_NUMBER[Integer.parseInt(s.substring(s.length() - 1))];
    }

    private static String parseTeen(String s) {
        return Normal.NUMBER_TEEN[Integer.parseInt(s) - 10];
    }

    private static String parseTen(String s) {
        return Normal.NUMBER_TEN[Integer.parseInt(s.substring(0, 1)) - 1];
    }

    private static String parseMore(String s) {
        return Normal.NUMBER_MORE[Integer.parseInt(s)];
    }

    /**
     * 两位数字信息
     *
     * @param text 数字
     * @return 转换后的数字
     */
    private static String transTwo(String text) {
        String value;
        // 判断位数
        if (text.length() > 2) {
            text = text.substring(0, 2);
        } else if (text.length() < 2) {
            text = Symbol.ZERO + text;
        }

        if (text.startsWith(Symbol.ZERO)) {
            // 07 - seven 是否小於10
            value = parseFirst(text);
        } else if (text.startsWith(Symbol.ONE)) {
            // 17 seventeen 是否在10和20之间
            value = parseTeen(text);
        } else if (text.endsWith(Symbol.ZERO)) {
            // 是否在10与100之间的能被10整除的数
            value = parseTen(text);
        } else {
            value = parseTen(text) + Symbol.SPACE + parseFirst(text);
        }
        return value;
    }

    /**
     * 三位数字信息
     *
     * @param text 数字
     * @return 转换后的数字
     */
    private static String transThree(String text) {
        String value;
        if (text.startsWith(Symbol.ZERO)) {
            // 是否小於100
            value = transTwo(text.substring(1));
        } else if (ObjectKit.equal(text.substring(1), "00")) {
            // 是否被100整除
            value = parseFirst(text.substring(0, 1)) + " HUNDRED";
        } else {
            value = parseFirst(text.substring(0, 1)) + " HUNDRED AND " + transTwo(text.substring(1));
        }
        return value;
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
