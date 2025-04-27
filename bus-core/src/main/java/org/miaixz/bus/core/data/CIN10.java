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
package org.miaixz.bus.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 10位公民身份号码（Citizen Identification Number）用于台湾、香港、澳门
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CIN10 {

    private static final Pattern PATTERN_TW = Pattern.compile("^[a-zA-Z][0-9]{9}$");
    private static final Pattern PATTERN_MC = Pattern.compile("^[157][0-9]{6}\\(?[0-9A-Z]\\)?$");
    private static final Pattern PATTERN_HK = Pattern.compile("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$");
    /**
     * 台湾身份首字母对应数字
     */
    private static final Map<Character, Integer> TW_FIRST_CODE = new HashMap<>();

    static {
        TW_FIRST_CODE.put('A', 10);
        TW_FIRST_CODE.put('B', 11);
        TW_FIRST_CODE.put('C', 12);
        TW_FIRST_CODE.put('D', 13);
        TW_FIRST_CODE.put('E', 14);
        TW_FIRST_CODE.put('F', 15);
        TW_FIRST_CODE.put('G', 16);
        TW_FIRST_CODE.put('H', 17);
        TW_FIRST_CODE.put('J', 18);
        TW_FIRST_CODE.put('K', 19);
        TW_FIRST_CODE.put('L', 20);
        TW_FIRST_CODE.put('M', 21);
        TW_FIRST_CODE.put('N', 22);
        TW_FIRST_CODE.put('P', 23);
        TW_FIRST_CODE.put('Q', 24);
        TW_FIRST_CODE.put('R', 25);
        TW_FIRST_CODE.put('S', 26);
        TW_FIRST_CODE.put('T', 27);
        TW_FIRST_CODE.put('U', 28);
        TW_FIRST_CODE.put('V', 29);
        TW_FIRST_CODE.put('X', 30);
        TW_FIRST_CODE.put('Y', 31);
        TW_FIRST_CODE.put('W', 32);
        TW_FIRST_CODE.put('Z', 33);
        TW_FIRST_CODE.put('I', 34);
        TW_FIRST_CODE.put('O', 35);
    }

    private final String code;
    private final String province;
    private final Gender gender;
    private final boolean verified;

    /**
     * 构造
     *
     * @param code 身份证号码
     * @throws IllegalArgumentException 身份证格式不支持
     */
    public CIN10(String code) throws IllegalArgumentException {
        this.code = code;
        if (StringKit.isNotBlank(code)) {
            // 中文空格替换为英文
            code = StringKit.replace(code, "（", Symbol.PARENTHESE_LEFT);
            code = StringKit.replace(code, "）", Symbol.PARENTHESE_RIGHT);
            // 台湾
            if (PatternKit.isMatch(PATTERN_TW, code)) {
                this.province = "台湾";
                final char char2 = code.charAt(1);
                if ('1' == char2) {
                    this.gender = Gender.MALE;
                } else if ('2' == char2) {
                    this.gender = Gender.FEMALE;
                } else {
                    this.gender = Gender.UNKNOWN;
                }
                this.verified = verifyTWCard(code);
                return;
                // 澳门
            } else if (PatternKit.isMatch(PATTERN_MC, code)) {
                this.province = "澳门";
                this.gender = Gender.UNKNOWN;
                this.verified = true;
                return;
                // 香港
            } else if (PatternKit.isMatch(PATTERN_HK, code)) {
                this.province = "香港";
                this.gender = Gender.UNKNOWN;
                this.verified = verfyHKCard(code);
                return;
            }
        }

        throw new IllegalArgumentException("Invalid CIN10 code!");
    }

    /**
     * 创建并验证台湾、香港、澳门身份证号码
     *
     * @param code 台湾、香港、澳门身份证号码
     * @return CIN10
     */
    public static CIN10 of(final String code) {
        return new CIN10(code);
    }

    /**
     * 验证台湾身份证号码
     *
     * @param code 身份证号码
     * @return 验证码是否符合
     */
    private static boolean verifyTWCard(final String code) {
        final Integer iStart = TW_FIRST_CODE.get(code.charAt(0));
        if (null == iStart) {
            return false;
        }
        int sum = iStart / 10 + (iStart % 10) * 9;

        final String mid = code.substring(1, 9);
        final char[] chars = mid.toCharArray();
        int iflag = 8;
        for (final char c : chars) {
            sum += Integer.parseInt(String.valueOf(c)) * iflag;
            iflag--;
        }

        final String end = code.substring(9, 10);
        return (sum % 10 == 0 ? 0 : (10 - sum % 10)) == Integer.parseInt(end);
    }

    /**
     * 验证香港身份证号码(目前存在Bug，部份特殊身份证无法检查) 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35
     * 最后一位校验码为0-9的数字加上字符"A"，"A"代表10 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
     *
     * @param code 身份证号码
     * @return 验证码是否符合
     */
    private static boolean verfyHKCard(final String code) {
        String card = code.replaceAll("[()]", Normal.EMPTY);
        int sum;
        if (card.length() == 9) {
            sum = (Character.toUpperCase(card.charAt(0)) - 55) * 9 + (Character.toUpperCase(card.charAt(1)) - 55) * 8;
            card = card.substring(1, 9);
        } else {
            sum = 522 + (Character.toUpperCase(card.charAt(0)) - 55) * 8;
        }

        // 首字母A-Z，A表示1，以此类推
        final String mid = card.substring(1, 7);
        final String end = card.substring(7, 8);
        final char[] chars = mid.toCharArray();
        int iflag = 7;
        for (final char c : chars) {
            sum = sum + Integer.parseInt(String.valueOf(c)) * iflag;
            iflag--;
        }
        if ("A".equalsIgnoreCase(end)) {
            sum += 10;
        } else {
            sum += Integer.parseInt(end);
        }
        return sum % 11 == 0;
    }

    /**
     * 获取CIN10码
     *
     * @return CIN10码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取省份
     *
     * @return 省份
     */
    public String getProvince() {
        return province;
    }

    /**
     * 获取性别
     *
     * @return 性别
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * 是否验证通过
     *
     * @return 是否验证通过
     */
    public boolean isVerified() {
        return verified;
    }

}
