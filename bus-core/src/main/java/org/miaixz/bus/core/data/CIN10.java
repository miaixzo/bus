/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * 10-digit Citizen Identification Number (CIN) for Taiwan, Hong Kong, and Macau.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CIN10 {

    /**
     * Regex for Taiwan ID card number.
     */
    private static final Pattern PATTERN_TW = Pattern.compile("^[a-zA-Z][0-9]{9}$");
    /**
     * Regex for Macau ID card number.
     */
    private static final Pattern PATTERN_MC = Pattern.compile("^[157][0-9]{6}\\(?[0-9A-Z]\\)?$");
    /**
     * Regex for Hong Kong ID card number.
     */
    private static final Pattern PATTERN_HK = Pattern.compile("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$");
    /**
     * Mapping of the first letter of a Taiwan ID card to a number.
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

    /**
     * The ID card number.
     */
    private final String code;
    /**
     * The province.
     */
    private final String province;
    /**
     * The gender.
     */
    private final Gender gender;
    /**
     * Whether the ID card number has been verified.
     */
    private final boolean verified;

    /**
     * Constructor.
     *
     * @param code The ID card number.
     * @throws IllegalArgumentException if the ID card format is not supported.
     */
    public CIN10(String code) throws IllegalArgumentException {
        this.code = code;
        if (StringKit.isNotBlank(code)) {
            // Replace Chinese parentheses with English ones.
            code = StringKit.replace(code, "（", Symbol.PARENTHESE_LEFT);
            code = StringKit.replace(code, "）", Symbol.PARENTHESE_RIGHT);
            // Taiwan
            if (PatternKit.isMatch(PATTERN_TW, code)) {
                this.province = "Taiwan";
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
                // Macau
            } else if (PatternKit.isMatch(PATTERN_MC, code)) {
                this.province = "Macau";
                this.gender = Gender.UNKNOWN;
                this.verified = true;
                return;
                // Hong Kong
            } else if (PatternKit.isMatch(PATTERN_HK, code)) {
                this.province = "Hong Kong";
                this.gender = Gender.UNKNOWN;
                this.verified = verfyHKCard(code);
                return;
            }
        }

        throw new IllegalArgumentException("Invalid CIN10 code!");
    }

    /**
     * Creates and validates a Taiwan, Hong Kong, or Macau ID card number.
     *
     * @param code The Taiwan, Hong Kong, or Macau ID card number.
     * @return A CIN10 object.
     */
    public static CIN10 of(final String code) {
        return new CIN10(code);
    }

    /**
     * Verifies a Taiwan ID card number.
     *
     * @param code The ID card number.
     * @return {@code true} if the checksum is valid, {@code false} otherwise.
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
     * Verifies a Hong Kong ID card number (currently has a bug, some special ID cards cannot be checked). The first 2
     * digits of the ID card are English characters. If only one English character appears, it means the first digit is
     * a space, corresponding to the number 58. The first 2 English characters A-Z correspond to the numbers 10-35. The
     * last check digit is a number from 0-9 plus the character "A", where "A" represents 10. Convert the entire ID card
     * number to numbers, multiply by 9-1 respectively, sum them up, and if the sum is divisible by 11, the ID card
     * number is valid.
     *
     * @param code The ID card number.
     * @return {@code true} if the checksum is valid, {@code false} otherwise.
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

        // The first letter A-Z, A represents 1, and so on.
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
     * Gets the CIN10 code.
     *
     * @return The CIN10 code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the province.
     *
     * @return The province.
     */
    public String getProvince() {
        return province;
    }

    /**
     * Gets the gender.
     *
     * @return The gender.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Checks if the ID card number has been verified.
     *
     * @return {@code true} if the ID card number has been verified, {@code false} otherwise.
     */
    public boolean isVerified() {
        return verified;
    }

}
