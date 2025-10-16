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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Unified Social Credit Code (GB32100-2015) utility class. Standard see:
 * <a href="https://www.cods.org.cn/c/2020-10-29/12575.html">GB 32100-2015</a> After the policy of "three certificates
 * in one, one license one code", the taxpayer identification number == unified social credit code. Policy see the State
 * Administration of Taxation: <a href="https://www.chinatax.gov.cn/n810219/n810724/c1838941/content.html">What are the
 * changes to the taxpayer identification number after "three certificates in one"?</a> Rules:
 * 
 * <pre>
 * Part 1: Registration management department code, 1 digit (number or uppercase English letter)
 * Part 2: Institution category code, 1 digit (number or uppercase English letter)
 * Part 3: Registration management authority administrative division code, 6 digits (number)
 * Part 4: Main body identification code (organization code), 9 digits (number or uppercase English letter)
 * Part 5: Check code, 1 digit (number or uppercase English letter)
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CreditCode {

    /**
     * Unified social credit code regex.
     */
    public static final java.util.regex.Pattern CREDIT_CODE_PATTERN = Pattern.CREDIT_CODE_PATTERN;

    /**
     * Weighting factor.
     */
    private static final int[] WEIGHT = { 1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28 };
    /**
     * Code character set.
     */
    private static final char[] BASE_CODE_ARRAY = "0123456789ABCDEFGHJKLMNPQRTUWXY".toCharArray();
    /**
     * Map from code characters to their index.
     */
    private static final Map<Character, Integer> CODE_INDEX_MAP;

    static {
        CODE_INDEX_MAP = new ConcurrentHashMap<>();
        for (int i = 0; i < BASE_CODE_ARRAY.length; i++) {
            CODE_INDEX_MAP.put(BASE_CODE_ARRAY[i], i);
        }
    }

    /**
     * Validates the unified social credit code (18 digits) using a regular expression. Note: This method is a
     * simplified version and does not strictly check if the check code conforms to the rules. For strict validation,
     * refer to {@link #isCreditCode(CharSequence)}.
     *
     * <b>Rules:</b>
     * 
     * <pre>
     * Part 1: Registration management department code, 1 digit (number or uppercase English letter)
     * Part 2: Institution category code, 1 digit (number or uppercase English letter)
     * Part 3: Registration management authority administrative division code, 6 digits (number)
     * Part 4: Main body identification code (organization code), 9 digits (number or uppercase English letter)
     * Part 5: Check code, 1 digit (number or uppercase English letter)
     * </pre>
     *
     * @param creditCode The unified social credit code.
     * @return The validation result.
     */
    public static boolean isCreditCodeSimple(final CharSequence creditCode) {
        if (StringKit.isBlank(creditCode)) {
            return false;
        }
        return PatternKit.isMatch(CREDIT_CODE_PATTERN, creditCode);
    }

    /**
     * Checks if it is a valid unified social credit code.
     * 
     * <pre>
     * Part 1: Registration management department code, 1 digit (number or uppercase English letter)
     * Part 2: Institution category code, 1 digit (number or uppercase English letter)
     * Part 3: Registration management authority administrative division code, 6 digits (number)
     * Part 4: Main body identification code (organization code), 9 digits (number or uppercase English letter)
     * Part 5: Check code, 1 digit (number or uppercase English letter)
     * </pre>
     *
     * @param creditCode The unified social credit code.
     * @return The validation result.
     */
    public static boolean isCreditCode(final CharSequence creditCode) {
        if (!isCreditCodeSimple(creditCode)) {
            return false;
        }

        final int parityBit = getParityBit(creditCode);
        if (parityBit < 0) {
            return false;
        }

        return creditCode.charAt(17) == BASE_CODE_ARRAY[parityBit];
    }

    /**
     * Gets a random unified social credit code.
     *
     * @return A unified social credit code.
     */
    public static String randomCreditCode() {
        final StringBuilder buf = new StringBuilder(18);

        //
        for (int i = 0; i < 2; i++) {
            final int num = RandomKit.randomInt(BASE_CODE_ARRAY.length - 1);
            buf.append(Character.toUpperCase(BASE_CODE_ARRAY[num]));
        }
        for (int i = 2; i < 8; i++) {
            final int num = RandomKit.randomInt(10);
            buf.append(BASE_CODE_ARRAY[num]);
        }
        for (int i = 8; i < 17; i++) {
            final int num = RandomKit.randomInt(BASE_CODE_ARRAY.length - 1);
            buf.append(BASE_CODE_ARRAY[num]);
        }

        final String code = buf.toString();
        return code + BASE_CODE_ARRAY[getParityBit(code)];
    }

    /**
     * Gets the value of the check digit.
     *
     * @param creditCode The unified social credit code.
     * @return The value of the check digit, or -1 if an error occurs.
     */
    private static int getParityBit(final CharSequence creditCode) {
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
