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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.RegEx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一社会信用代码（GB32100-2015）工具类
 * 标准见：https://www.cods.org.cn/c/2020-10-29/12575.html
 *
 * <pre>
 * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
 * 第二部分：机构类别代码1位 (数字或大写英文字母)
 * 第三部分：登记管理机关行政区划码6位 (数字)
 * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
 * 第五部分：校验码1位 (数字或大写英文字母)
 * </pre>
 */
public class LicenseKit {

    /**
     * 加权因子
     */
    private static final int[] WEIGHT = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
    /**
     * 代码字符集
     */
    private static final char[] BASE_CODE_ARRAY = "0123456789ABCDEFGHJKLMNPQRTUWXY".toCharArray();
    private static final Map<Character, Integer> CODE_INDEX_MAP;

    static {
        CODE_INDEX_MAP = new ConcurrentHashMap<>();
        for (int i = 0; i < BASE_CODE_ARRAY.length; i++) {
            CODE_INDEX_MAP.put(BASE_CODE_ARRAY[i], i);
        }
    }

    /**
     * 正则校验统一社会信用代码（18位）
     *
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     *
     * @param creditCode 统一社会信用代码
     * @return 校验结果
     */
    public static boolean isCreditCodeSimple(CharSequence creditCode) {
        if (StringKit.isBlank(creditCode)) {
            return false;
        }
        return PatternKit.isMatch(RegEx.CREDIT_CODE, creditCode);
    }

    /**
     * 是否是有效的统一社会信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     *
     * @param creditCode 统一社会信用代码
     * @return 校验结果
     */
    public static boolean isCreditCode(CharSequence creditCode) {
        if (false == isCreditCodeSimple(creditCode)) {
            return false;
        }

        final int parityBit = getParityBit(creditCode);
        if (parityBit < 0) {
            return false;
        }

        return creditCode.charAt(17) == BASE_CODE_ARRAY[parityBit];
    }

    /**
     * 获取一个随机的统一社会信用代码
     *
     * @return 统一社会信用代码
     */
    public static String randomCreditCode() {
        final StringBuilder buf = new StringBuilder(18);


        //
        for (int i = 0; i < 2; i++) {
            int num = RandomKit.randomInt(BASE_CODE_ARRAY.length - 1);
            buf.append(Character.toUpperCase(BASE_CODE_ARRAY[num]));
        }
        for (int i = 2; i < 8; i++) {
            int num = RandomKit.randomInt(10);
            buf.append(BASE_CODE_ARRAY[num]);
        }
        for (int i = 8; i < 17; i++) {
            int num = RandomKit.randomInt(BASE_CODE_ARRAY.length - 1);
            buf.append(BASE_CODE_ARRAY[num]);
        }

        final String code = buf.toString();
        return code + BASE_CODE_ARRAY[getParityBit(code)];
    }

    /**
     * 获取校验位的值
     *
     * @param creditCode 统一社会信息代码
     * @return 获取校验位的值，-1表示获取错误
     */
    private static int getParityBit(CharSequence creditCode) {
        int sum = 0;
        Integer codeIndex;
        for (int i = 0; i < 17; i++) {
            codeIndex = CODE_INDEX_MAP.get(creditCode.charAt(i));
            if (null == codeIndex) {
                return -1;
            }
            sum += codeIndex * WEIGHT[i];
        }
        final int result = 31 - sum % 31;
        return result == 31 ? 0 : result;
    }

}
