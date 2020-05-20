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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.utils.CollUtils;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
public class RegEx {

    /**
     * 正则表达式匹配中文汉字
     */
    public static final String CHINESE_PATTERN = "[\u4E00-\u9FFF]";
    public static final Pattern CHINESE = Pattern.compile(CHINESE_PATTERN);
    /**
     * 正则表达式匹配中文字符串
     */
    public final static String CHINESES_PATTERN = CHINESE_PATTERN + "+";
    public static final Pattern CHINESES = Pattern.compile(CHINESES_PATTERN);
    /**
     * 在XML中无效的字符 正则
     */
    public static final String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";
    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final String GENERAL_PATTERN = "^[\u4E00-\u9FFF\\w]+$";
    public static final Pattern GENERAL = Pattern.compile(GENERAL_PATTERN);
    /**
     * 字母
     */
    public static final String WORD_PATTERN = "[a-zA-Z]+";
    public static final Pattern WORD = Pattern.compile(WORD_PATTERN);
    /**
     * 数字
     */
    public static final String NUMBERS_PATTERN = "\\d+";
    public static final Pattern NUMBERS = Pattern.compile(NUMBERS_PATTERN);
    /**
     * 分组
     */
    public static final String GROUP_VAR_PATTERN = "\\$(\\d+)";
    public static final Pattern GROUP_VAR = Pattern.compile(GROUP_VAR_PATTERN);
    /**
     * IP v4
     */
    public static final String IPV4_PATTERN = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
    public static final Pattern IPV4 = Pattern.compile(IPV4_PATTERN);
    /**
     * IP v6
     */
    public static final String IPV6_PATTERN = "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))";
    public static final Pattern IPV6 = Pattern.compile(IPV6_PATTERN);
    /**
     * 货币
     */
    public static final String MONEY_PATTERN = "^(\\d+(?:\\.\\d+)?)$";
    public static final Pattern MONEY = Pattern.compile(MONEY_PATTERN);

    /**
     * 邮件,符合RFC 5322规范
     */
    public static final String EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
    public static final Pattern EMAIL = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
    /**
     * 固定电话
     */
    public static final String PHONE_PATTERN = "^0[1-9](\\\\d{1,2}\\\\-?)\\\\d{7,8}";
    public static final Pattern PHONE = Pattern.compile(PHONE_PATTERN);
    /**
     * 移动电话
     */
    public static final String MOBILE_PATTERN = "(?:0|86|\\+86)?1[3456789]\\d{9}";
    public static final Pattern MOBILE = Pattern.compile(MOBILE_PATTERN);
    /**
     * 18位身份证号码
     */
    public static final String CITIZEN_ID_PATTERN = "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)";
    public static final Pattern CITIZEN_ID = Pattern.compile(CITIZEN_ID_PATTERN);
    /**
     * 邮编
     */
    public static final String ZIP_CODE_PATTERN = "[1-9]\\d{5}(?!\\d)";
    public static final Pattern ZIP_CODE = Pattern.compile(ZIP_CODE_PATTERN);
    /**
     * 生日
     */
    public static final String BIRTHDAY_PATTERN = "^(\\d{2,4})([/\\-.年]?)(\\d{1,2})([/\\-.月]?)(\\d{1,2})日?$";
    public static final Pattern BIRTHDAY = Pattern.compile(BIRTHDAY_PATTERN);
    /**
     * URL
     */
    public static final String URL_PATTERN = "[a-zA-z]+://[^\\s]*";
    public static final Pattern URL = Pattern.compile(URL_PATTERN);
    /**
     * Http URL
     */
    public static final String URL_HTTP_PATTERN = "(https://|http://)?([\\w-]+\\.)+[\\w-]+(:\\d+)*(/[\\w- ./?%&=]*)?";
    public static final Pattern URL_HTTP = Pattern.compile(URL_HTTP_PATTERN);
    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final String GENERAL_WITH_CHINESE_PATTERN = "^[\u4E00-\u9FFF\\w]+$";
    public static final Pattern GENERAL_WITH_CHINESE = Pattern.compile(GENERAL_WITH_CHINESE_PATTERN);
    /**
     * UUID
     */
    public static final String UUID_PATTERN = "^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$";
    public static final Pattern UUID = Pattern.compile(UUID_PATTERN);
    /**
     * 不带横线的UUID
     */
    public static final String UUID_SIMPLE_PATTERN = "^[0-9a-z]{32}$";
    public static final Pattern UUID_SIMPLE = Pattern.compile(UUID_SIMPLE_PATTERN);
    /**
     * 中国车牌号码
     */
    public static final String PLATE_NUMBER_PATTERN = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[ABCDEFGHJK])|([ABCDEFGHJK]([A-HJ-NP-Z0-9])[0-9]{4})))|" +
            "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
    public static final Pattern PLATE_NUMBER = Pattern.compile(PLATE_NUMBER_PATTERN);
    /**
     * MAC地址正则
     */
    public static final String MAC_ADDRESS_PATTERN = "((?:[A-F0-9]{1,2}[:-]){5}[A-F0-9]{1,2})|(?:0x)(\\d{12})(?:.+ETHER)";
    public static final Pattern MAC_ADDRESS = Pattern.compile(MAC_ADDRESS_PATTERN, Pattern.CASE_INSENSITIVE);
    /**
     * 16进制字符串
     */
    public static final String HEX_PATTERN = "^[a-f0-9]+$";
    public static final Pattern HEX = Pattern.compile(HEX_PATTERN, Pattern.CASE_INSENSITIVE);
    /**
     * 时间正则
     */
    public static final String TIME_PATTERN = "\\d{1,2}:\\d{1,2}(:\\d{1,2})?";
    public static final Pattern TIME = Pattern.compile(TIME_PATTERN);
    /**
     * 密码规则 数字,英文,符号至少两种,最小长度8
     */
    public static final String PASSWORD_WEAK_PATTERN = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,}$";
    public static final Pattern PASSWORD_WEAK = Pattern.compile(PASSWORD_WEAK_PATTERN);
    /**
     * 密码规则 数字,英文,符号全部包含,最小长度8
     */
    public static final String PASSWORD_STRONG_PATTERN = "^(?![0-9]+$)(?![^0-9]+$)(?![a-zA-Z]+$)(?![^a-zA-Z]+$)(?![a-zA-Z0-9]+$)[a-zA-Z0-9\\S]{8,}$";
    public static final Pattern PASSWORD_STRONG = Pattern.compile(PASSWORD_STRONG_PATTERN);
    /**
     * 正则中需要被转义的关键字
     */
    public static final Set<Character> RE_KEYS = CollUtils.newHashSet(Symbol.C_DOLLAR, Symbol.C_PARENTHESE_LEFT, Symbol.C_PARENTHESE_RIGHT, Symbol.C_STAR, Symbol.C_PLUS, Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT, Symbol.C_QUESTION_MARK, Symbol.C_BACKSLASH, Symbol.C_CARET, Symbol.C_BRACE_LEFT, Symbol.C_BRACE_RIGHT, Symbol.C_OR);

}
