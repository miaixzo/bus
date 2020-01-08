/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang;

import org.aoju.bus.core.utils.CollUtils;

import java.util.Set;

/**
 * 正则表达式
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class RegEx {

    /**
     * 正则表达式匹配中文汉字
     */
    public static final String CHINESE_PATTERN = "[\u4E00-\u9FFF]";
    public static final java.util.regex.Pattern CHINESE = java.util.regex.Pattern.compile(CHINESE_PATTERN);
    /**
     * 在XML中无效的字符 正则
     */
    public static final String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";
    /**
     * 英文字母 、数字和下划线
     */
    public static final String GENERAL_PATTERN = "^\\w+$";
    public static final java.util.regex.Pattern GENERAL = java.util.regex.Pattern.compile(GENERAL_PATTERN);
    /**
     * 字母
     */
    public static final String WORD_PATTERN = "[a-zA-Z]+";
    public static final java.util.regex.Pattern WORD = java.util.regex.Pattern.compile(WORD_PATTERN);
    /**
     * 数字
     */
    public static final String NUMBERS_PATTERN = "\\d+";
    public static final java.util.regex.Pattern NUMBERS = java.util.regex.Pattern.compile(NUMBERS_PATTERN);
    /**
     * 分组
     */
    public static final String GROUP_VAR_PATTERN = "\\$(\\d+)";
    public static final java.util.regex.Pattern GROUP_VAR = java.util.regex.Pattern.compile(GROUP_VAR_PATTERN);
    /**
     * IP v4
     */
    public static final String IPV4_PATTERN = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
    public static final java.util.regex.Pattern IPV4 = java.util.regex.Pattern.compile(IPV4_PATTERN);
    /**
     * IP v6
     */
    public static final String IPV6_PATTERN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    public static final java.util.regex.Pattern IPV6 = java.util.regex.Pattern.compile(IPV6_PATTERN);
    /**
     * 货币
     */
    public static final String MONEY_PATTERN = "^(\\d+(?:\\.\\d+)?)$";
    public static final java.util.regex.Pattern MONEY = java.util.regex.Pattern.compile(MONEY_PATTERN);

    /**
     * 邮件,符合RFC 5322规范
     */
    public static final String EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    public static final java.util.regex.Pattern EMAIL = java.util.regex.Pattern.compile(EMAIL_PATTERN, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 固定电话
     */
    public static final String PHONE_PATTERN = "^0[1-9](\\\\d{1,2}\\\\-?)\\\\d{7,8}";
    public static final java.util.regex.Pattern PHONE = java.util.regex.Pattern.compile(PHONE_PATTERN);
    /**
     * 移动电话
     */
    public static final String MOBILE_PATTERN = "(?:0|86|\\+86)?1[3456789]\\d{9}";
    public static final java.util.regex.Pattern MOBILE = java.util.regex.Pattern.compile(MOBILE_PATTERN);
    /**
     * 18位身份证号码
     */
    public static final String CITIZEN_ID_PATTERN = "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}(\\d|X|x)";
    public static final java.util.regex.Pattern CITIZEN_ID = java.util.regex.Pattern.compile(CITIZEN_ID_PATTERN);
    /**
     * 邮编
     */
    public static final String ZIP_CODE_PATTERN = "[1-9]\\d{5}(?!\\d)";
    public static final java.util.regex.Pattern ZIP_CODE = java.util.regex.Pattern.compile(ZIP_CODE_PATTERN);
    /**
     * 生日
     */
    public static final String BIRTHDAY_PATTERN = "^(\\d{2,4})([/\\-\\.年]?)(\\d{1,2})([/\\-\\.月]?)(\\d{1,2})日?$";
    public static final java.util.regex.Pattern BIRTHDAY = java.util.regex.Pattern.compile(BIRTHDAY_PATTERN);
    /**
     * URL
     */
    public static final String URL_PATTERN = "[a-zA-z]+://[^\\s]*";
    public static final java.util.regex.Pattern URL = java.util.regex.Pattern.compile("[a-zA-z]+://[^\\s]*");
    /**
     * Http URL
     */
    public static final String URL_HTTP_PATTERN = "(https://|http://)?([\\w-]+\\.)+[\\w-]+(:\\d+)*(/[\\w- ./?%&=]*)?";
    public static final java.util.regex.Pattern URL_HTTP = java.util.regex.Pattern.compile(URL_HTTP_PATTERN);
    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final String GENERAL_WITH_CHINESE_PATTERN = "^[\u4E00-\u9FFF\\w]+$";
    public static final java.util.regex.Pattern GENERAL_WITH_CHINESE = java.util.regex.Pattern.compile(GENERAL_WITH_CHINESE_PATTERN);
    /**
     * UUID
     */
    public static final String UUID_PATTERN = "^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$";
    public static final java.util.regex.Pattern UUID = java.util.regex.Pattern.compile(UUID_PATTERN);
    /**
     * 不带横线的UUID
     */
    public static final String UUID_SIMPLE_PATTERN = "^[0-9a-z]{32}$";
    public static final java.util.regex.Pattern UUID_SIMPLE = java.util.regex.Pattern.compile(UUID_SIMPLE_PATTERN);
    /**
     * 中国车牌号码
     */
    public static final String PLATE_NUMBER_PATTERN = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
    public static final java.util.regex.Pattern PLATE_NUMBER = java.util.regex.Pattern.compile(PLATE_NUMBER_PATTERN);
    /**
     * MAC地址正则
     */
    public static final String MAC_ADDRESS_PATTERN = "((?:[A-F0-9]{1,2}[:-]){5}[A-F0-9]{1,2})|(?:0x)(\\d{12})(?:.+ETHER)";
    public static final java.util.regex.Pattern MAC_ADDRESS = java.util.regex.Pattern.compile(MAC_ADDRESS_PATTERN, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 16进制字符串
     */
    public static final String HEX_PATTERN = "^[a-f0-9]+$";
    public static final java.util.regex.Pattern HEX = java.util.regex.Pattern.compile(HEX_PATTERN, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * 时间正则
     */
    public static final String TIME_PATTERN = "\\d{1,2}:\\d{1,2}(:\\d{1,2})?";
    public static final java.util.regex.Pattern TIME = java.util.regex.Pattern.compile(TIME_PATTERN);
    /**
     * 密码强度
     */
    public static final String PASSWD_PATTERN = "(?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W])[\\da-zA-Z\\W]{8,}$";
    public static final java.util.regex.Pattern PASSWD = java.util.regex.Pattern.compile(PASSWD_PATTERN);

    /**
     * 正则中需要被转义的关键字
     */
    public static final Set<Character> RE_KEYS = CollUtils.newHashSet(Symbol.C_DOLLAR, Symbol.C_PARENTHESE_LEFT, Symbol.C_PARENTHESE_RIGHT, Symbol.C_STAR, Symbol.C_PLUS, Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT, Symbol.C_QUESTION_MARK, Symbol.C_BACKSLASH, Symbol.C_CARET, Symbol.C_BRACE_LEFT, Symbol.C_BRACE_RIGHT, Symbol.C_OR);

}
