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
package org.miaixz.bus.core.data;

import org.miaixz.bus.core.lang.EnumMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数据脱敏（Data Masking）工具类，对某些敏感信息（比如，身份证号、手机号、卡号、姓名、地址、邮箱等 ）屏蔽敏感数据。
 * <p>支持以下类型信息的脱敏自动处理：</p>
 *
 * <ul>
 *     <li>用户ID</li>
 *     <li>中文名</li>
 *     <li>身份证</li>
 *     <li>座机号</li>
 *     <li>手机号</li>
 *     <li>地址</li>
 *     <li>电子邮件</li>
 *     <li>密码</li>
 *     <li>车牌</li>
 *     <li>银行卡号</li>
 *     <li>IPv4</li>
 *     <li>IPv6</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Masking {

    /**
     * 脱敏，使用默认的脱敏策略
     * <pre>
     * Masking.masking("100", Masking.DesensitizedType.USER_ID)) =  "0"
     * Masking.masking("王二小", Masking.DesensitizedType.CHINESE_NAME)) = "王**"
     * Masking.masking("51363620000320711X", Masking.DesensitizedType.ID_CARD)) = "5***************1X"
     * Masking.masking("02167518080", Masking.DesensitizedType.FIXED_PHONE)) = "0216*****80"
     * Masking.masking("13929531666", Masking.DesensitizedType.MOBILE_PHONE)) = "139****1666"
     * Masking.masking("北京市海淀区马连洼街道289号", Masking.DesensitizedType.ADDRESS)) = "北京市海淀区马********"
     * Masking.masking("service@gmail.com", Masking.DesensitizedType.EMAIL)) = "s******@gmail.com"
     * Masking.masking("1234567890", Masking.DesensitizedType.PASSWORD)) = "**********"
     * Masking.masking("沪A50006", Masking.DesensitizedType.CAR_LICENSE)) = "沪A5***6"
     * Masking.masking("11055555000033333350", Masking.DesensitizedType.BANK_CARD)) = "1105 **** **** **** 3350"
     * Masking.masking("192.168.1.1", Masking.DesensitizedType.IPV4)) = "192.*.*.*"
     * </pre>
     *
     * @param text    字符串
     * @param masking 脱敏类型;可以脱敏：用户id、中文名、身份证号、座机号、手机号、地址、电子邮件、密码
     * @return 脱敏之后的字符串
     */
    public static String masking(final CharSequence text, final EnumMap.Masking masking) {
        if (StringKit.isBlank(text)) {
            return Normal.EMPTY;
        }
        String newText = String.valueOf(text);
        switch (masking) {
            case USER_ID:
                newText = String.valueOf(userId());
                break;
            case CHINESE_NAME:
                newText = chineseName(String.valueOf(text));
                break;
            case ID_CARD:
                newText = idCardNum(String.valueOf(text), 1, 2);
                break;
            case FIXED_PHONE:
                newText = fixedPhone(String.valueOf(text));
                break;
            case MOBILE_PHONE:
                newText = mobilePhone(String.valueOf(text));
                break;
            case ADDRESS:
                newText = address(String.valueOf(text), 8);
                break;
            case EMAIL:
                newText = email(String.valueOf(text));
                break;
            case PASSWORD:
                newText = password(String.valueOf(text));
                break;
            case CAR_LICENSE:
                newText = carLicense(String.valueOf(text));
                break;
            case BANK_CARD:
                newText = bankCard(String.valueOf(text));
                break;
            case IPV4:
                newText = ipv4(String.valueOf(text));
                break;
            case IPV6:
                newText = ipv6(String.valueOf(text));
                break;
            case FIRST_MASK:
                newText = firstMask(String.valueOf(text));
                break;
            case CLEAR_TO_EMPTY:
                newText = clear();
                break;
            case CLEAR_TO_NULL:
                newText = clearToNull();
                break;
            default:
        }
        return newText;
    }

    /**
     * 清空为空字符串
     *
     * @return 清空后的值
     */
    public static String clear() {
        return Normal.EMPTY;
    }

    /**
     * 清空为{@code null}
     *
     * @return 清空后的值(null)
     */
    public static String clearToNull() {
        return null;
    }

    /**
     * 【用户id】不对外提供userId
     *
     * @return 脱敏后的主键
     */
    public static Long userId() {
        return 0L;
    }

    /**
     * 定义了一个first_mask的规则，只显示第一个字符。
     * 脱敏前：123456789；脱敏后：1********。
     *
     * @param text 字符串
     * @return 脱敏后的字符串
     */
    public static String firstMask(final String text) {
        if (StringKit.isBlank(text)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(text, 1, text.length());
    }

    /**
     * 【中文姓名】只显示第一个汉字，其他隐藏为2个星号，比如：李**
     *
     * @param fullName 姓名
     * @return 脱敏后的姓名
     */
    public static String chineseName(final String fullName) {
        return firstMask(fullName);
    }

    /**
     * 【身份证号】前1位 和后2位
     *
     * @param idCardNum 身份证
     * @param front     保留：前面的front位数；从1开始
     * @param end       保留：后面的end位数；从1开始
     * @return 脱敏后的身份证
     */
    public static String idCardNum(final String idCardNum, final int front, final int end) {
        //身份证不能为空
        if (StringKit.isBlank(idCardNum)) {
            return Normal.EMPTY;
        }
        //需要截取的长度不能大于身份证号长度
        if ((front + end) > idCardNum.length()) {
            return Normal.EMPTY;
        }
        //需要截取的不能小于0
        if (front < 0 || end < 0) {
            return Normal.EMPTY;
        }
        return StringKit.hide(idCardNum, front, idCardNum.length() - end);
    }

    /**
     * 【固定电话 前四位，后两位
     *
     * @param num 固定电话
     * @return 脱敏后的固定电话；
     */
    public static String fixedPhone(final String num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(num, 4, num.length() - 2);
    }

    /**
     * 【手机号码】前三位，后4位，其他隐藏，比如135****3966
     *
     * @param num 移动电话；
     * @return 脱敏后的移动电话；
     */
    public static String mobilePhone(final String num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(num, 3, num.length() - 4);
    }

    /**
     * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
     *
     * @param address       家庭住址
     * @param sensitiveSize 敏感信息长度
     * @return 脱敏后的家庭地址
     */
    public static String address(final String address, final int sensitiveSize) {
        if (StringKit.isBlank(address)) {
            return Normal.EMPTY;
        }
        final int length = address.length();
        return StringKit.hide(address, length - sensitiveSize, length);
    }

    /**
     * 【电子邮箱】邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@qq.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String email(final String email) {
        if (StringKit.isBlank(email)) {
            return Normal.EMPTY;
        }
        final int index = StringKit.indexOf(email, Symbol.C_AT);
        if (index <= 1) {
            return email;
        }
        return StringKit.hide(email, 1, index);
    }

    /**
     * 【密码】密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(final String password) {
        if (StringKit.isBlank(password)) {
            return Normal.EMPTY;
        }
        return StringKit.repeat(Symbol.C_STAR, password.length());
    }

    /**
     * 【中国车牌】车牌中间用*代替
     * eg1：null       - ""
     * eg1：""         - ""
     * eg3：苏A60000   - 苏A6***0
     * eg4：陕A12345D  - 陕A1****D
     * eg5：京A123     - 京A123     如果是错误的车牌，不处理
     *
     * @param carLicense 完整的车牌号
     * @return 脱敏后的车牌
     */
    public static String carLicense(String carLicense) {
        if (StringKit.isBlank(carLicense)) {
            return Normal.EMPTY;
        }
        // 普通车牌
        if (carLicense.length() == 7) {
            carLicense = StringKit.hide(carLicense, 3, 6);
        } else if (carLicense.length() == 8) {
            // 新能源车牌
            carLicense = StringKit.hide(carLicense, 3, 7);
        }
        return carLicense;
    }

    /**
     * 银行卡号脱敏
     * eg: 1101 **** **** **** 3256
     *
     * @param bankCardNo 银行卡号
     * @return 脱敏之后的银行卡号
     */
    public static String bankCard(String bankCardNo) {
        if (StringKit.isBlank(bankCardNo)) {
            return bankCardNo;
        }
        bankCardNo = StringKit.cleanBlank(bankCardNo);
        if (bankCardNo.length() < 9) {
            return bankCardNo;
        }

        final int length = bankCardNo.length();
        final int endLength = length % 4 == 0 ? 4 : length % 4;
        final int midLength = length - 4 - endLength;
        final StringBuilder buf = new StringBuilder();

        buf.append(bankCardNo, 0, 4);
        for (int i = 0; i < midLength; ++i) {
            if (i % 4 == 0) {
                buf.append(Symbol.C_SPACE);
            }
            buf.append(Symbol.C_STAR);
        }
        buf.append(Symbol.C_SPACE).append(bankCardNo, length - endLength, length);
        return buf.toString();
    }

    /**
     * IPv4脱敏，如：脱敏前：192.0.2.1；脱敏后：192.*.*.*。
     *
     * @param ipv4 IPv4地址
     * @return 脱敏后的地址
     */
    public static String ipv4(final String ipv4) {
        return StringKit.subBefore(ipv4, '.', false) + ".*.*.*";
    }

    /**
     * IPv6脱敏，如：脱敏前：2001:0db8:86a3:08d3:1319:8a2e:0370:7344；脱敏后：2001:*:*:*:*:*:*:*
     *
     * @param ipv6 IPv6地址
     * @return 脱敏后的地址
     */
    public static String ipv6(final String ipv6) {
        return StringKit.subBefore(ipv6, Symbol.C_COLON, false) + ":*:*:*:*:*:*:*";
    }

}
