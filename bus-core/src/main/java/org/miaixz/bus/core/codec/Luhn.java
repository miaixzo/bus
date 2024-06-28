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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Luhn算法，也称为“模10”算法，是一种简单的校验和（Checksum）算法，在ISO/IEC 7812-1中定义，校验步骤如下：
 * <ol>
 *     <li>从右边第1个数字（校验数字）开始偶数位乘以2，如果小于10，直接返回，否则将个位数和十位数相加</li>
 *     <li>把步骤1种获得的乘积的各位数字与原号码中未乘2的各位数字相加</li>
 *     <li>如果步骤2得到的总和模10为0，则校验通过</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Luhn {

    /**
     * 校验字符串
     *
     * @param text 含校验数字的字符串
     * @return true - 校验通过，false-校验不通过
     * @throws IllegalArgumentException 如果字符串为空或不是8~19位的数字
     */
    public static boolean check(final String text) {
        if (StringKit.isBlank(text)) {
            return false;
        }
        if (!PatternKit.isMatch(Pattern.NUMBERS_PATTERN, text)) {
            // 必须为全数字
            return false;
        }
        return sum(text) % 10 == 0;
    }

    /**
     * 计算校验位数字
     * 忽略已有的校验位数字，根据前N位计算最后一位校验位数字
     *
     * @param str            被检查的数字
     * @param withCheckDigit 是否含有校验位
     * @return 校验位数字
     */
    public static int getCheckDigit(String str, final boolean withCheckDigit) {
        if (withCheckDigit) {
            str = str.substring(0, str.length() - 1);
        }
        return 10 - (sum(str + "0") % 10);
    }

    /**
     * 根据Luhn算法计算字符串各位数字之和
     *
     * @param text 需要校验的数字字符串
     * @return 数字之和
     */
    private static int sum(final String text) {
        final char[] strArray = text.toCharArray();
        final int n = strArray.length;
        int sum = strArray[n - 1] - '0';
        ;
        for (int i = 2; i <= n; i++) {
            int a = strArray[n - i] - '0';
            // 偶数位乘以2
            if ((i & 1) == 0) {
                a *= 2;
            }
            // 十位数和个位数相加，如果不是偶数位，不乘以2，则十位数为0
            sum += a / 10 + a % 10;
        }
        return sum;
    }

}
