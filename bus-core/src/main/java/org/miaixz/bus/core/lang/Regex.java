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
package org.miaixz.bus.core.lang;

import java.util.regex.Pattern;

/**
 * Common regular expression patterns.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Regex {

    /**
     * Matches English letters, numbers, and underscores.
     */
    public static final String GENERAL = "^\\w+$";
    /**
     * Matches numbers.
     */
    public static final String NUMBERS = "\\d+";
    /**
     * Matches letters.
     */
    public static final String WORD = "[a-zA-Z]+";
    /**
     * Matches non-numbers.
     */
    public static final String NOT_NUMBERS = "[^0-9]+";

    /**
     * Matches strings starting with non-numbers.
     */
    public static final String WITH_NOT_NUMBERS = "^[^0-9]*";
    /**
     * Matches one or more whitespace characters.
     */
    public static final String SPACES = "\\s+";
    /**
     * Matches a space, a colon, and another space.
     */
    public static final String SPACES_COLON_SPACE = "\\s+:\\s";
    /**
     * Used to check the validity of a hexadecimal string.
     */
    public static final String VALID_HEX = "[0-9a-fA-F]+";
    /**
     * Matches a single Chinese character. Refer to the Unicode range for Chinese characters on Wikipedia
     * (<a href="https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97">https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97</a>,
     * right side of the page).
     */
    public static final String CHINESE = "[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]";
    /**
     * Matches multiple Chinese characters.
     */
    public static final String CHINESES = CHINESE + Symbol.PLUS;
    /**
     * Matches a group variable, typically used in replacement operations.
     */
    public static final String GROUP_VAR = "\\$(\\d+)";
    /**
     * Quickly distinguishes between IP addresses and hostnames.
     */
    public static final String IP_ADDRESS = "([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)";
    /**
     * Matches an IPv4 address. Uses grouping for easy parsing of each segment of the address.
     */
    public static final String IPV4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$";
    /**
     * Matches an IPv6 address.
     */
    public static final String IPV6 = "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))";
    /**
     * Matches currency values.
     */
    public static final String MONEY = "^(\\d+(?:\\.\\d+)?)$";
    /**
     * Matches email addresses, compliant with RFC 5322. Note that this regex is designed to be more lenient. Regex
     * source: <a href="http://emailregex.com/">http://emailregex.com/</a> References:
     * <ul>
     * <li>https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754</li>
     * <li>https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression</li>
     * </ul>
     */
    public static final String EMAIL = "(?:[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?\\.)+[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

    /**
     * Matches Chinese mainland mobile phone numbers. E.g., +86 180 4953 1399 (2-digit area code + 13 digits).
     */
    public static final String MOBILE = "(?:0|86|\\+86)?1[3-9]\\d{9}";
    /**
     * Pre-compiled pattern for Chinese mainland mobile phone numbers.
     */
    public static final Pattern MOBILE_PATTERN = Pattern.compile(Regex.MOBILE);
    /**
     * Matches Hong Kong mobile phone numbers. E.g., +852 5100 4810 (3-digit area code + 8 digits).
     */
    public static final String MOBILE_HK = "(?:0|852|\\+852)?\\d{8}";
    /**
     * Matches Taiwan mobile phone numbers. E.g., +886 09 60 000000 (3-digit area code + number starting with 09 + 8
     * digits, total 10 digits).
     */
    public static final String MOBILE_TW = "(?:0|886|\\+886)?(?:|-)09\\d{8}";
    /**
     * Matches Macau mobile phone numbers. E.g., +853 68 00000 (3-digit area code + number starting with 6 + 7 digits,
     * total 8 digits).
     */
    public static final String MOBILE_MO = "(?:0|853|\\+853)?(?:|-)6\\d{7}";
    /**
     * Matches fixed-line telephone numbers. E.g., (010|02\d|0[3-9]\d{2})-?(\d{6,8}).
     */
    public static final String TEL = "(010|02\\d|0[3-9]\\d{2})-?(\\d{6,8})";
    /**
     * Matches fixed-line telephone numbers, including 400 and 800 service numbers.
     */
    public static final String TEL_400_800 = "0\\d{2,3}[\\- ]?[0-9]\\d{6,7}|[48]00[\\- ]?[0-9]\\d{2}[\\- ]?\\d{4}";
    /**
     * Matches an 18-digit Chinese Resident Identity Card number.
     */
    public static final String CITIZEN_ID = "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)";
    /**
     * Matches postal codes, compatible with Hong Kong, Macau, and Taiwan.
     */
    public static final String ZIP_CODE = "^(0[1-7]|1[0-356]|2[0-7]|3[0-6]|4[0-7]|5[0-7]|6[0-7]|7[0-5]|8[0-9]|9[0-8])\\d{4}|99907[78]$";
    /**
     * Matches a birthday in various date formats.
     */
    public static final String BIRTHDAY = "^(\\d{2,4})([/\\-.年]?)(\\d{1,2})([/\\-.月]?)(\\d{1,2})日?$";
    /**
     * Matches a URI. Definition from:
     * <a href="https://www.ietf.org/rfc/rfc3986.html#appendix-B">https://www.ietf.org/rfc/rfc3986.html#appendix-B</a>
     */
    public static final String URI = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    /**
     * Matches a URL.
     */
    public static final String URL = "[a-zA-Z]+://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    /**
     * Matches a URL with protocol (HTTP, HTTPS, FTP, File, etc.). Source:
     * <a href="http://urlregex.com/">http://urlregex.com/</a>
     */
    public static final String URL_HTTP = "(https?|ftp|file)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    /**
     * Matches Chinese characters, English letters, numbers, and underscores.
     */
    public static final String GENERAL_WITH_CHINESE = "^[\u4E00-\u9FFF\\w]+$";
    /**
     * Matches a standard UUID format (e.g., XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX).
     */
    public static final String UUID = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    /**
     * Matches a UUID without hyphens.
     */
    public static final String UUID_SIMPLE = "^[0-9a-fA-F]{32}$";
    /**
     * Matches a hexadecimal string.
     */
    public static final String HEX = "^[a-fA-F0-9]+$";
    /**
     * Matches time strings (e.g., 12:30, 12时30分, 12:30:45).
     */
    public static final String TIME = "\\d{1,2}[:时]\\d{1,2}([:分]\\d{1,2})?秒?";
    /**
     * Matches Chinese vehicle license plate numbers, including new energy vehicle plates.
     */
    public static final String PLATE_NUMBER = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[ABCDEFGHJK])|([ABCDEFGHJK]([A-HJ-NP-Z0-9])[0-9]{4})))|"
            + "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]\\d{3}\\d{1,3}[领])|"
            + "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
    /**
     * Matches a Unified Social Credit Code (USCC) for Chinese entities.
     * 
     * <pre>
     * Part 1: Registration management department code (1 digit or uppercase English letter)
     * Part 2: Institution type code (1 digit or uppercase English letter)
     * Part 3: Registration management authority administrative division code (6 digits)
     * Part 4: Main identification code (organization code) (9 digits or uppercase English letter)
     * Part 5: Check code (1 digit or uppercase English letter)
     * </pre>
     */
    public static final String CREDIT_CODE = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$";
    /**
     * Matches a Vehicle Identification Number (VIN). Also known as vehicle identification code, chassis number, or
     * 17-digit code. Standard: GB 16735-2019. Official standard address:
     * https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E2EBF667F8C032B1EDFD6DF9C1114E02 For manufacturers producing
     * 1,000 or more complete and/or incomplete vehicles per year:
     * 
     * <pre>
     *   Part 1: World Manufacturer Identifier (WMI), 3 characters.
     *   Part 2: Vehicle Descriptor Section (VDS), 6 characters.
     *   Part 3: Vehicle Indicator Section (VIS), 8 characters.
     * </pre>
     * 
     * For manufacturers producing fewer than 1,000 complete and/or incomplete vehicles per year:
     * 
     * <pre>
     *   Part 1: World Manufacturer Identifier (WMI), 3 characters.
     *   Part 2: Vehicle Descriptor Section (VDS), 6 characters.
     *   Part 3: The third, fourth, and fifth characters, combined with Part 1, form the WMI. The remaining five characters form the VIS, 8 characters.
     * </pre>
     */
    public static final String CAR_VIN = "^[A-HJ-NPR-Z0-9]{8}[X0-9]([A-HJ-NPR-Z0-9]{3}\\d{5}|[A-HJ-NPR-Z0-9]{5}\\d{3})$";
    /**
     * Matches a Chinese driving license archive number (12-digit numeric string). E.g., 430101758218. Only for Chinese
     * driving license archive numbers.
     */
    public static final String CAR_DRIVING_LICENCE = "^[0-9]{12}$";
    /**
     * Matches a Chinese name. This includes Uyghur names with the middle dot (·). The middle dot is typically entered
     * using the key to the left of '1' on the keyboard in Chinese input methods. Incorrect characters: {@code ．.。．.}
     * Correct Uyghur names examples:
     * <p>
     * Summary for Chinese names: 2-60 characters, can only be Chinese characters and the Uyghur middle dot (·).
     * Broadened Chinese character range: [CJK Unified Ideographs, CJK Unified Ideographs Extension A] for rare names
     * like 刘欣䶮(yǎn). Chinese character range reference: https://www.cnblogs.com/animalize/p/5432864.html
     */
    public static final String CHINESE_NAME = "^[\u3400-\u9FFF·]{2,60}$";
    /**
     * Matches a MAC address. Supports various formats including colon-separated, hyphen-separated, dot-separated, and
     * 12-digit hexadecimal.
     */
    public static String MAC_ADDRESS = "((?:[a-fA-F0-9]{1,2}[:-]){5}[a-fA-F0-9]{1,2})|((?:[a-fA-F0-9]{1,4}[.]){2}[a-fA-F0-9]{1,4})|[a-fA-F0-9]{12}|0x(\\d{12}).+ETHER";

}
