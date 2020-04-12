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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.utils.StringUtils;

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
 * @version 5.8.3
 * @since JDK 1.8+
 */
public class NumberChinese {

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
        final String[] numArray = isUseTraditional ? Normal.TRADITIONAL_DIGITS : Normal.SIMPLE_DIGITS;

        if (amount > 99999999999999.99 || amount < -99999999999999.99) {
            throw new IllegalArgumentException("Number support only: (-99999999999999.99 ～ 99999999999999.99)！");
        }

        boolean negative = false;
        if (amount < 0) {
            negative = true;
            amount = -amount;
        }

        long temp = Math.round(amount * 100);
        int numFen = (int) (temp % 10);
        temp = temp / 10;
        int numJiao = (int) (temp % 10);
        temp = temp / 10;

        //将数字以万为单位分为多份
        int[] parts = new int[20];
        int numParts = 0;
        for (int i = 0; temp != 0; i++) {
            int part = (int) (temp % 10000);
            parts[i] = part;
            numParts++;
            temp = temp / 10000;
        }

        boolean beforeWanIsZero = true; // 标志“万”下面一级是不是 0

        String chineseStr = Normal.EMPTY;
        for (int i = 0; i < numParts; i++) {
            String partChinese = toChinese(parts[i], isUseTraditional);
            if (i % 2 == 0) {
                beforeWanIsZero = StringUtils.isEmpty(partChinese);
            }

            if (i != 0) {
                if (i % 2 == 0) {
                    chineseStr = "亿" + chineseStr;
                } else {
                    if ("".equals(partChinese) && false == beforeWanIsZero) {
                        // 如果“万”对应的 part 为 0,而“万”下面一级不为 0,则不加“万”,而加“零”
                        chineseStr = "零" + chineseStr;
                    } else {
                        if (parts[i - 1] < 1000 && parts[i - 1] > 0) {
                            // 如果"万"的部分不为 0, 而"万"前面的部分小于 1000 大于 0, 则万后面应该跟“零”
                            chineseStr = "零" + chineseStr;
                        }
                        chineseStr = "万" + chineseStr;
                    }
                }
            }
            chineseStr = partChinese + chineseStr;
        }

        // 整数部分为 0, 则表达为"零"
        if (Normal.EMPTY.equals(chineseStr)) {
            chineseStr = numArray[0];
        }
        //负数
        if (negative) { // 整数部分不为 0
            chineseStr = "负" + chineseStr;
        }

        // 小数部分
        if (numFen != 0 || numJiao != 0) {
            if (numFen == 0) {
                chineseStr += (isMoneyMode ? "元" : "点") + numArray[numJiao] + (isMoneyMode ? "角" : "");
            } else { // “分”数不为 0
                if (numJiao == 0) {
                    chineseStr += (isMoneyMode ? "元零" : "点零") + numArray[numFen] + (isMoneyMode ? "分" : "");
                } else {
                    chineseStr += (isMoneyMode ? "元" : "点") + numArray[numJiao] + (isMoneyMode ? "角" : "") + numArray[numFen] + (isMoneyMode ? "分" : "");
                }
            }
        } else if (isMoneyMode) {
            //无小数部分的金额结尾
            chineseStr += "元整";
        }

        return chineseStr;

    }

    /**
     * 把一个 0~9999 之间的整数转换为汉字的字符串,如果是 0 则返回 ""
     *
     * @param amountPart       数字部分
     * @param isUseTraditional 是否使用繁体单位
     * @return 转换后的汉字
     */
    private static String toChinese(int amountPart, boolean isUseTraditional) {
        String[] numArray = isUseTraditional ? Normal.TRADITIONAL_DIGITS : Normal.SIMPLE_DIGITS;
        String[] units = isUseTraditional ? Normal.TRADITIONAL_UNITS : Normal.SIMPLE_UNITS;

        int temp = amountPart;

        String chineseStr = "";
        boolean lastIsZero = true; // 在从低位往高位循环时,记录上一位数字是不是 0
        for (int i = 0; temp > 0; i++) {
            if (temp == 0) {
                // 高位已无数据
                break;
            }
            int digit = temp % 10;
            if (digit == 0) { // 取到的数字为 0
                if (false == lastIsZero) {
                    // 前一个数字不是 0,则在当前汉字串前加“零”字;
                    chineseStr = "零" + chineseStr;
                }
                lastIsZero = true;
            } else { // 取到的数字不是 0
                chineseStr = numArray[digit] + units[i] + chineseStr;
                lastIsZero = false;
            }
            temp = temp / 10;
        }
        return chineseStr;
    }

}
