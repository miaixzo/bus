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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.toolkit.CollKit;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegEx {

    /**
     * 正则表达式匹配中文汉字
     */
    public static final String CHINESE_PATTERN = "[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]";
    public static final Pattern CHINESE = Pattern.compile(CHINESE_PATTERN + "+");

    /**
     * 正则表达式匹配中文字符串
     */
    public final static String CHINESES_PATTERN = CHINESE_PATTERN + "+";
    public static final Pattern CHINESES = Pattern.compile(CHINESES_PATTERN);

    /**
     * 用于检查十六进制字符串的有效性
     */
    public static final String VALID_HEX_PATTERN = "[0-9a-fA-F]+";
    public static final Pattern VALID_HEX = Pattern.compile(VALID_HEX_PATTERN);

    /**
     * XML中无效的字符
     */
    public static final String VALID_XML_PATTERN = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";
    public static final Pattern VALID_XML = Pattern.compile(VALID_XML_PATTERN);

    /**
     * XML中注释的内容
     */
    public static final String COMMENT_XML_PATTERN = "(?s)<!--.+?-->";
    public static final Pattern COMMENT_XML = Pattern.compile(COMMENT_XML_PATTERN);

    /**
     * 英文字母、数字和下划线
     */
    public static final String GENERAL_PATTERN = "^\\w+$";
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
     * 非数字
     */
    public static final String NOT_NUMBERS_PATTERN = "[^0-9]+";
    public static final Pattern NOT_NUMBERS = Pattern.compile(NOT_NUMBERS_PATTERN);

    /**
     * 从非数字开始
     */
    public static final String WITH_NOT_NUMBERS_PATTERN = "^[^0-9]*";
    public static final Pattern WITH_NOT_NUMBERS = Pattern.compile(WITH_NOT_NUMBERS_PATTERN);

    /**
     * 空格
     */
    public static final String SPACES_PATTERN = "\\s+";
    public static final Pattern SPACES = Pattern.compile(SPACES_PATTERN);

    /**
     * 空格冒号空格
     */
    public static final String SPACES_COLON_SPACE_PATTERN = "\\s+:\\s";
    public static final Pattern SPACES_COLON_SPACE = Pattern.compile(SPACES_COLON_SPACE_PATTERN);

    /**
     * 分组
     */
    public static final String GROUP_VAR_PATTERN = "\\$(\\d+)";
    public static final Pattern GROUP_VAR = Pattern.compile(GROUP_VAR_PATTERN);

    /**
     * 快速区分IP地址和主机名
     */
    public static final String IP_ADDRESS_PATTERN = "([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)";
    public static final Pattern IP_ADDRESS = Pattern.compile(IP_ADDRESS_PATTERN);

    /**
     * IP v4
     */
    public static final String IPV4_PATTERN = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$";
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
    public static final String PHONE_PATTERN = "(010|02\\d|0[3-9]\\d{2})-?(\\d{6,8})";
    public static final Pattern PHONE = Pattern.compile(PHONE_PATTERN);

    /**
     * 移动电话
     * eg: 中国大陆： +86  139 1111 2222，2位区域码标示+13位数字
     */
    public static final String MOBILE_PATTERN = "(?:0|86|\\+86)?1[3-9]\\d{9}";
    public static final Pattern MOBILE = Pattern.compile(MOBILE_PATTERN);

    /**
     * 中国香港移动电话
     * eg: 中国香港： +852 5200 8810， 三位区域码+10位数字, 中国香港手机号码8位数
     */
    public final static String MOBILE_HK_PATTERN = "(?:0|852|\\+852)?\\d{8}";
    public final static Pattern MOBILE_HK = Pattern.compile(MOBILE_HK_PATTERN);

    /**
     * 中国澳门移动电话
     * eg: 中国台湾： +886 09 60 000000， 三位区域码+号码以数字09开头 + 8位数字, 中国台湾手机号码10位数
     */
    public final static String MOBILE_MO_PATTERN = "(?:0|853|\\+853)?(?:|-)6\\d{7}";
    public final static Pattern MOBILE_MO = Pattern.compile(MOBILE_MO_PATTERN);

    /**
     * 中国台湾移动电话
     * eg: 中国澳门： +853 68 00000， 三位区域码 +号码以数字6开头 + 7位数字, 中国台湾手机号码8位数
     */
    public final static String MOBILE_TW_PATTERN = "(?:0|886|\\+886)?(?:|-)09\\d{8}";
    public final static Pattern MOBILE_TW = Pattern.compile(MOBILE_TW_PATTERN);

    /**
     * 座机号码+400+800电话
     */
    public final static String PHONE_400_800_PATTERN = "0\\d{2,3}[\\- ]?[1-9]\\d{6,7}|[48]00[\\- ]?[1-9]\\d{2}[\\- ]?\\d{4}";
    public final static Pattern PHONE_400_800 = Pattern.compile(PHONE_400_800_PATTERN);

    /**
     * 18位身份证号码
     */
    public static final String CITIZEN_ID_PATTERN = "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)";
    public static final Pattern CITIZEN_ID = Pattern.compile(CITIZEN_ID_PATTERN);

    /**
     * 邮编，兼容港澳台
     */
    public static final String ZIP_CODE_PATTERN = "^(0[1-7]|1[0-356]|2[0-7]|3[0-6]|4[0-7]|5[0-7]|6[0-7]|7[0-5]|8[0-9]|9[0-8])\\d{4}|99907[78]$";
    public static final Pattern ZIP_CODE = Pattern.compile(ZIP_CODE_PATTERN);

    /**
     * 生日
     */
    public static final String BIRTHDAY_PATTERN = "^(\\d{2,4})([/\\-.年]?)(\\d{1,2})([/\\-.月]?)(\\d{1,2})日?$";
    public static final Pattern BIRTHDAY = Pattern.compile(BIRTHDAY_PATTERN);

    /**
     * URI
     * 定义见：https://www.ietf.org/rfc/rfc3986.html#appendix-B
     */
    public static final String URI_PATTERN = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    public static final Pattern URI = Pattern.compile(URI_PATTERN);

    /**
     * URL
     * 定义见：https://www.ietf.org/rfc/rfc3986.html#appendix-B
     */
    public static final String URL_PATTERN = "[a-zA-Z]+://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    public static final Pattern URL = Pattern.compile(URL_PATTERN);

    /**
     * Http URL
     */
    public static final String URL_HTTP_PATTERN = "(https?|ftp|file)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    public static final Pattern URL_HTTP = Pattern.compile(URL_HTTP_PATTERN);

    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final String GENERAL_WITH_CHINESE_PATTERN = "^[\u4E00-\u9FFF\\w]+$";
    public static final Pattern GENERAL_WITH_CHINESE = Pattern.compile(GENERAL_WITH_CHINESE_PATTERN);

    /**
     * UUID
     */
    public static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    public static final Pattern UUID = Pattern.compile(UUID_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * 不带横线的UUID
     */
    public static final String UUID_SIMPLE_PATTERN = "^[0-9a-fA-F]{32}$";
    public static final Pattern UUID_SIMPLE = Pattern.compile(UUID_SIMPLE_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * 中国车牌号码
     */
    public static final String PLATE_NUMBER_PATTERN = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[ABCDEFGHJK])"
            + "|([ABCDEFGHJK]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]\\d{3}\\d{1,3}[领])|([京津沪渝"
            + "冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
    public static final Pattern PLATE_NUMBER = Pattern.compile(PLATE_NUMBER_PATTERN);

    /**
     * MAC地址正则
     */
    public static final String MAC_ADDRESS_PATTERN = "((?:[a-fA-F0-9]{1,2}[:-]){5}[a-fA-F0-9]{1,2})|0x(\\d{12}).+ETHER";
    public static final Pattern MAC_ADDRESS = Pattern.compile(MAC_ADDRESS_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * 16进制字符串
     */
    public static final String HEX_PATTERN = "^[a-fA-F0-9]+$";
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
     * 中文姓名
     * 总结中文姓名：2-60位，只能是中文和维吾尔族的点·
     * 放宽汉字范围：如生僻姓名 刘欣䶮yǎn
     */
    public static final String CHINESE_NAME_PATTERN = "^[\u2E80-\u9FFF·]{2,60}$";
    public static final Pattern CHINESE_NAME = Pattern.compile(CHINESE_NAME_PATTERN);

    /**
     * 驾驶证  别名：驾驶证档案编号、行驶证编号
     * eg:430101758218
     * 12位数字字符串
     * 仅限：中国驾驶证档案编号
     */
    public static final String CAR_DRIVING_LICENCE_PATTERN = "^[0-9]{12}$";
    public static final Pattern CAR_DRIVING_LICENCE = Pattern.compile(CAR_DRIVING_LICENCE_PATTERN);

    /**
     * 车架号
     * 别名：车辆识别代号 车辆识别码
     * eg:LDC613P23A1305189
     * eg:LSJA24U62JG269225
     * 十七位码、车架号
     * 车辆的唯一标示
     */
    public static final String CAR_VIN_PATTERN = "^[A-HJ-NPR-Z0-9]{8}[0-9X][A-HJ-NPR-Z0-9]{2}\\d{6}$";
    public static final Pattern CAR_VIN = Pattern.compile(CAR_VIN_PATTERN);

    /**
     * 统一社会信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     */
    public static final String CREDIT_CODE_PATTERN = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$";
    public static final Pattern CREDIT_CODE = Pattern.compile(CREDIT_CODE_PATTERN);

    /**
     * 正则中需要被转义的关键字
     */
    public static final Set<Character> RE_KEYS = CollKit.newHashSet(Symbol.C_DOLLAR, Symbol.C_PARENTHESE_LEFT, Symbol.C_PARENTHESE_RIGHT, Symbol.C_STAR, Symbol.C_PLUS, Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT, Symbol.C_QUESTION_MARK, Symbol.C_BACKSLASH, Symbol.C_CARET, Symbol.C_BRACE_LEFT, Symbol.C_BRACE_RIGHT, Symbol.C_OR);

}
