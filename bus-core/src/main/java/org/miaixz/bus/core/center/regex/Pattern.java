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
package org.miaixz.bus.core.center.regex;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Regex;

/**
 * A collection of commonly used regular expressions, providing pre-compiled {@link java.util.regex.Pattern} instances
 * for various validation and parsing tasks. This class aims to centralize and optimize the usage of regular expressions
 * by caching compiled patterns. More regular expressions can be found at:
 * <a href="https://any86.github.io/any-rule/">https://any86.github.io/any-rule/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pattern {

    /**
     * Regular expression for English letters, numbers, and underscores.
     */
    public static final java.util.regex.Pattern GENERAL_PATTERN = java.util.regex.Pattern.compile(Regex.GENERAL);
    /**
     * Regular expression for numbers.
     */
    public static final java.util.regex.Pattern NUMBERS_PATTERN = java.util.regex.Pattern.compile(Regex.NUMBERS);
    /**
     * Regular expression for letters.
     */
    public static final java.util.regex.Pattern WORD_PATTERN = java.util.regex.Pattern.compile(Regex.WORD);
    /**
     * Regular expression for non-numbers.
     */
    public static final java.util.regex.Pattern NOT_NUMBERS_PATTERN = java.util.regex.Pattern
            .compile(Regex.NOT_NUMBERS);
    /**
     * Regular expression for strings starting with non-numbers.
     */
    public static final java.util.regex.Pattern WITH_NOT_NUMBERS_PATTERN = java.util.regex.Pattern
            .compile(Regex.WITH_NOT_NUMBERS);
    /**
     * Regular expression for spaces.
     */
    public static final java.util.regex.Pattern SPACES_PATTERN = java.util.regex.Pattern.compile(Regex.SPACES);
    /**
     * Regular expression for space-colon-space sequence.
     */
    public static final java.util.regex.Pattern SPACES_COLON_SPACE_PATTERN = java.util.regex.Pattern
            .compile(Regex.SPACES_COLON_SPACE);
    /**
     * Regular expression for validating hexadecimal strings.
     */
    public static final java.util.regex.Pattern VALID_HEX_PATTERN = java.util.regex.Pattern.compile(Regex.VALID_HEX);
    /**
     * Regular expression for a single Chinese character, referencing Unicode ranges from Wikipedia (<a href=
     * "https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97">https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97</a>).
     */
    public static final java.util.regex.Pattern CHINESE_PATTERN = java.util.regex.Pattern.compile(Regex.CHINESE);
    /**
     * Regular expression for multiple Chinese characters.
     */
    public static final java.util.regex.Pattern CHINESES_PATTERN = java.util.regex.Pattern.compile(Regex.CHINESES);
    /**
     * Regular expression for capturing groups.
     */
    public static final java.util.regex.Pattern GROUP_VAR_PATTERN = java.util.regex.Pattern.compile(Regex.GROUP_VAR);
    /**
     * Regular expression to quickly distinguish between IP addresses and hostnames.
     */
    public static final java.util.regex.Pattern IP_ADDRESS_PATTERN = java.util.regex.Pattern.compile(Regex.IP_ADDRESS);
    /**
     * Regular expression for IPv4 addresses, using groups for easy parsing of each segment.
     */
    public static final java.util.regex.Pattern IPV4_PATTERN = java.util.regex.Pattern.compile(Regex.IPV4);
    /**
     * Regular expression for IPv6 addresses.
     */
    public static final java.util.regex.Pattern IPV6_PATTERN = java.util.regex.Pattern.compile(Regex.IPV6);
    /**
     * Regular expression for currency values.
     */
    public static final java.util.regex.Pattern MONEY_PATTERN = java.util.regex.Pattern.compile(Regex.MONEY);
    /**
     * Regular expression for email addresses, compliant with RFC 5322. Note that this regex is more lenient. Regex
     * source: <a href="http://emailregex.com/">http://emailregex.com/</a> References:
     * <ul>
     * <li>https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754</li>
     * <li>https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression</li>
     * </ul>
     */
    public static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern
            .compile(Regex.EMAIL, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * Regular expression for mainland China mobile phone numbers. Example: +86 180 5690 2500, 2-digit area code + 13
     * digits.
     */
    public static final java.util.regex.Pattern MOBILE_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE);
    /**
     * Regular expression for Hong Kong mobile phone numbers. Example: +852 5100 6590, 3-digit area code + 10 digits,
     * Hong Kong mobile numbers are 8 digits.
     */
    public static final java.util.regex.Pattern MOBILE_HK_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_HK);
    /**
     * Regular expression for Taiwan mobile phone numbers. Example: +886 09 60 000000, 3-digit area code + number
     * starting with 09 + 8 digits, Taiwan mobile numbers are 10 digits.
     */
    public static final java.util.regex.Pattern MOBILE_TW_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_TW);
    /**
     * Regular expression for Macau mobile phone numbers. Example: +853 68 00000, 3-digit area code + number starting
     * with 6 + 7 digits, Macau mobile numbers are 8 digits.
     */
    public static final java.util.regex.Pattern MOBILE_MO_PATTERN = java.util.regex.Pattern.compile(Regex.MOBILE_MO);
    /**
     * Regular expression for landline phone numbers.
     */
    public static final java.util.regex.Pattern TEL_PATTERN = java.util.regex.Pattern.compile(Regex.TEL);
    /**
     * Regular expression for landline phone numbers, including 400 and 800 service numbers.
     */
    public static final java.util.regex.Pattern TEL_400_800_PATTERN = java.util.regex.Pattern
            .compile(Regex.TEL_400_800);
    /**
     * Regular expression for 18-digit Chinese Resident Identity Card numbers.
     */
    public static final java.util.regex.Pattern CITIZEN_ID_PATTERN = java.util.regex.Pattern.compile(Regex.CITIZEN_ID);
    /**
     * Regular expression for postal codes, compatible with Hong Kong, Macau, and Taiwan.
     */
    public static final java.util.regex.Pattern ZIP_CODE_PATTERN = java.util.regex.Pattern.compile(Regex.ZIP_CODE);
    /**
     * Regular expression for birth dates.
     */
    public static final java.util.regex.Pattern BIRTHDAY_PATTERN = java.util.regex.Pattern.compile(Regex.BIRTHDAY);
    /**
     * Regular expression for URIs. Definition can be found at:
     * <a href="https://www.ietf.org/rfc/rfc3986.html#appendix-B">https://www.ietf.org/rfc/rfc3986.html#appendix-B</a>
     */
    public static final java.util.regex.Pattern URI_PATTERN = java.util.regex.Pattern.compile(Regex.URI);
    /**
     * Regular expression for URLs.
     */
    public static final java.util.regex.Pattern URL_PATTERN = java.util.regex.Pattern.compile(Regex.URL);
    /**
     * Regular expression for protocol-based URLs (from: <a href="http://urlregex.com/">http://urlregex.com/</a>). This
     * regex supports URLs with protocols like FTP, File, etc.
     */
    public static final java.util.regex.Pattern URL_HTTP_PATTERN = java.util.regex.Pattern
            .compile(Regex.URL_HTTP, java.util.regex.Pattern.CASE_INSENSITIVE);
    /**
     * Regular expression for Chinese characters, English letters, numbers, and underscores.
     */
    public static final java.util.regex.Pattern GENERAL_WITH_CHINESE_PATTERN = java.util.regex.Pattern
            .compile(Regex.GENERAL_WITH_CHINESE);
    /**
     * Regular expression for UUIDs.
     */
    public static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern.compile(Regex.UUID);
    /**
     * Regular expression for UUIDs without hyphens.
     */
    public static final java.util.regex.Pattern UUID_SIMPLE_PATTERN = java.util.regex.Pattern
            .compile(Regex.UUID_SIMPLE);
    /**
     * Regular expression for MAC addresses.
     */
    public static final java.util.regex.Pattern MAC_ADDRESS_PATTERN = java.util.regex.Pattern
            .compile(Regex.MAC_ADDRESS);
    /**
     * Regular expression for hexadecimal strings.
     */
    public static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile(Regex.HEX);
    /**
     * Regular expression for time formats.
     */
    public static final java.util.regex.Pattern TIME_PATTERN = java.util.regex.Pattern.compile(Regex.TIME);
    /**
     * Regular expression for Chinese vehicle license plate numbers (compatible with new energy vehicle plates).
     */
    public static final java.util.regex.Pattern PLATE_NUMBER_PATTERN = java.util.regex.Pattern
            .compile(Regex.PLATE_NUMBER);

    /**
     * Regular expression for Uniform Social Credit Code (USCC).
     * 
     * <pre>
     * Part 1: Registration and administration department code (1 digit, number or uppercase English letter)
     * Part 2: Institutional category code (1 digit, number or uppercase English letter)
     * Part 3: Registration and administration authority administrative division code (6 digits, numbers)
     * Part 4: Main body identification code (organization code) (9 digits, number or uppercase English letter)
     * Part 5: Check code (1 digit, number or uppercase English letter)
     * </pre>
     */
    public static final java.util.regex.Pattern CREDIT_CODE_PATTERN = java.util.regex.Pattern
            .compile(Regex.CREDIT_CODE);
    /**
     * Regular expression for Vehicle Identification Number (VIN). Also known as: Vehicle Identification Code, Chassis
     * Number, Seventeen-digit Code. Standard: GB 16735-2019. Official standard address:
     * https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E2EBF667F8C032B1EDFD6DF9C1114E02 For manufacturers of complete
     * and/or incomplete vehicles with an annual production of 1,000 or more:
     * 
     * <pre>
     *   Part 1: World Manufacturer Identifier (WMI), 3 characters;
     *   Part 2: Vehicle Descriptor Section (VDS), 6 characters;
     *   Part 3: Vehicle Indicator Section (VIS), 8 characters.
     * </pre>
     * <p>
     * For manufacturers of complete and/or incomplete vehicles with an annual production of less than 1,000:
     * 
     * <pre>
     *   Part 1: World Manufacturer Identifier (WMI), 3 characters;
     *   Part 2: Vehicle Descriptor Section (VDS), 6 characters;
     *   The third, fourth, and fifth characters of Part 3, combined with the three characters of Part 1, form the World Manufacturer Identifier (WMI);
     *   The remaining five characters are the Vehicle Indicator Section (VIS), 8 characters in total.
     * </pre>
     *
     * <pre>
     *   e.g.: LDC210P23A1306189
     *   e.g.: LSJA24U61JG269201
     *   e.g.: LBV5S3102ESJ20935
     * </pre>
     */
    public static final java.util.regex.Pattern CAR_VIN_PATTERN = java.util.regex.Pattern.compile(Regex.CAR_VIN);

    /**
     * Regular expression for Chinese driving license archive numbers. Alias: Driving License Archive Number, Vehicle
     * Registration Number. e.g.: 530201950258 (12-digit numeric string). Limited to: Chinese driving license archive
     * numbers.
     */
    public static final java.util.regex.Pattern CAR_DRIVING_LICENCE_PATTERN = java.util.regex.Pattern
            .compile(Regex.CAR_DRIVING_LICENCE);
    /**
     * Regular expression for Chinese names. Uyghur names use a middle dot (·). The correct middle dot is the one found
     * on the top-left of the keyboard (before '1') in Chinese input method. Incorrect characters: {@code ．.。．.} Correct
     * Uyghur names examples:
     * 
     * <pre>
     * 霍加阿卜杜拉·麦提喀斯木
     * 玛合萨提别克·哈斯木别克
     * 阿布都热依木江·艾斯卡尔
     * 阿卜杜尼亚孜·毛力尼亚孜
     * </pre>
     * 
     * <pre>
     * ----------
     * Incorrect examples:
     *   大  小                reason: contains spaces
     *   乐逍遥0               reason: contains numbers
     *   依帕古丽-艾则孜        reason: contains special symbols
     *   牙力空.买提萨力        reason: incorrect middle dot for Xinjiang names
     *   王二小2002-3-2        reason: contains numbers, special symbols
     *   霍金(科学家）          reason: contains parentheses
     *   寒冷:冬天             reason: contains special symbols
     *   大                   reason: less than 2 characters
     * ----------
     * </pre>
     * 
     * Summary for Chinese names: 2-60 characters, can only be Chinese characters and the Uyghur middle dot (·).
     * Broadened range for Chinese characters to include rare characters.
     */
    public static final java.util.regex.Pattern CHINESE_NAME_PATTERN = java.util.regex.Pattern
            .compile(Regex.CHINESE_NAME);
    /**
     * Regular expression for invalid characters in Windows filenames.
     */
    public static final java.util.regex.Pattern FILE_NAME_INVALID_PATTERN_WIN = java.util.regex.Pattern
            .compile("[\\\\/:*?\"<>|\r\n]");

    /**
     * A cache pool for compiled {@link java.util.regex.Pattern} objects. Uses {@link WeakConcurrentMap} to allow
     * patterns to be garbage collected if no longer strongly referenced.
     */
    private static final WeakConcurrentMap<RegexWithFlag, java.util.regex.Pattern> POOL = new WeakConcurrentMap<>();

    /**
     * Retrieves a compiled {@link java.util.regex.Pattern} from the pool based on the regular expression string. If the
     * pattern is not found in the pool, it will be compiled and added to the pool.
     *
     * @param regex The regular expression string.
     * @return A compiled {@link java.util.regex.Pattern} instance.
     */
    public static java.util.regex.Pattern get(final String regex) {
        return get(regex, 0);
    }

    /**
     * Retrieves a compiled {@link java.util.regex.Pattern} from the pool based on the regular expression string and
     * flags. If the pattern is not found in the pool, it will be compiled with the specified flags and added to the
     * pool.
     *
     * @param regex The regular expression string.
     * @param flags The match flags, a bit mask that may include {@link java.util.regex.Pattern#CASE_INSENSITIVE},
     *              {@link java.util.regex.Pattern#MULTILINE}, etc.
     * @return A compiled {@link java.util.regex.Pattern} instance.
     */
    public static java.util.regex.Pattern get(final String regex, final int flags) {
        final RegexWithFlag regexWithFlag = new RegexWithFlag(regex, flags);
        return POOL.computeIfAbsent(regexWithFlag, (key) -> java.util.regex.Pattern.compile(regex, flags));
    }

    /**
     * Removes a compiled {@link java.util.regex.Pattern} from the cache pool.
     *
     * @param regex The regular expression string.
     * @param flags The match flags used when the pattern was compiled.
     * @return The removed {@link java.util.regex.Pattern} instance, or {@code null} if not found.
     */
    public static java.util.regex.Pattern remove(final String regex, final int flags) {
        return POOL.remove(new RegexWithFlag(regex, flags));
    }

    /**
     * Clears all cached {@link java.util.regex.Pattern} instances from the pool.
     */
    public static void clear() {
        POOL.clear();
    }

}
