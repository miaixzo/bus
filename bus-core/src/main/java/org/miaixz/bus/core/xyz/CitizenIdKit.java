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
package org.miaixz.bus.core.xyz;

import java.util.Date;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.data.CIN;
import org.miaixz.bus.core.data.CIN10;

/**
 * Utility class for Chinese Citizen ID numbers, referencing the GB 11643-1999 standard. Standard description can be
 * found at: <a href=
 * "http://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=080D6FBF2BB468F9007657F26D60013E">http://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=080D6FBF2BB468F9007657F26D60013E</a>
 *
 * <p>
 * This utility does not validate the administrative division codes. If needed, please refer to (as of Dec 2020):
 * <a href=
 * "http://www.mca.gov.cn/article/sj/xzqh/2020/20201201.html">http://www.mca.gov.cn/article/sj/xzqh/2020/20201201.html</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CitizenIdKit {

    /**
     * Converts a 15-digit ID card number to an 18-digit one. The 15-digit format follows the GB 11643-1989 standard.
     *
     * @param idCard The 15-digit ID card number.
     * @return The 18-digit ID card number.
     */
    public static String convert15To18(final String idCard) {
        return CIN.convert15To18(idCard);
    }

    /**
     * Converts an 18-digit ID card number to a 15-digit one.
     *
     * @param idCard The 18-digit ID card number.
     * @return The 15-digit ID card number.
     */
    public static String convert18To15(final String idCard) {
        return CIN.convert18To15(idCard);
    }

    /**
     * Checks if the given ID card number is valid, ignoring the case of 'X'. Returns {@code false} if the ID card
     * number contains any whitespace.
     *
     * @param idCard The ID card number, supports 18-digit, 15-digit, and 10-digit (for HK/Macau/Taiwan).
     * @return {@code true} if the ID card number is valid.
     */
    public static boolean isValidCard(final String idCard) {
        if (StringKit.isBlank(idCard)) {
            return false;
        }

        final int length = idCard.length();
        switch (length) {
            case 18: // 18-digit ID
                return isValidCard18(idCard);

            case 15: // 15-digit ID
                try {
                    return isValidCard18(CIN.convert15To18(idCard));
                } catch (final Exception ignore) {
                    return false;
                }
            case 10: // 10-digit ID, for Hong Kong, Macau, Taiwan
                return isValidCard10(idCard);

            default:
                return false;
        }
    }

    /**
     * Validates an 18-digit ID card number. According to the national standard GB11643-1999, the ID number is a feature
     * combination code, consisting of a 17-digit body code and a 1-digit checksum. The structure from left to right is:
     * 6-digit address code, 8-digit date of birth code, 3-digit sequence code, and 1-digit checksum. Sequence code: An
     * odd number is assigned to males, and an even number to females.
     *
     * <ol>
     * <li>Digits 1-2: Province code.</li>
     * <li>Digits 3-4: City code.</li>
     * <li>Digits 5-6: District/County code.</li>
     * <li>Digits 7-14: Year, month, and day of birth.</li>
     * <li>Digits 15-16: Code of the local police station.</li>
     * <li>Digit 17: Gender (odd for male, even for female).</li>
     * <li>Digit 18: Checksum, can be 0-9 or 'X'.</li>
     * </ol>
     * The checksum calculation is as follows:
     * <ol>
     * <li>Multiply the first 17 digits by their respective weights: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2</li>
     * <li>Sum the results of these multiplications.</li>
     * <li>Calculate the remainder of the sum divided by 11.</li>
     * <li>The remainder corresponds to the checksum digit: 1 0 X 9 8 7 6 5 4 3 2 for remainders 0-10 respectively.</li>
     * </ol>
     *
     * @param idcard The ID card number to validate.
     * @return {@code true} if the 18-digit ID is valid, ignoring the case of 'x'.
     */
    public static boolean isValidCard18(final String idcard) {
        return isValidCard18(idcard, true);
    }

    /**
     * Validates an 18-digit ID card number.
     *
     * @param idcard     The ID card number to validate.
     * @param ignoreCase Whether to ignore case. If {@code true}, the case of 'X' is ignored; otherwise, it must be
     *                   uppercase.
     * @return {@code true} if the 18-digit ID is valid.
     */
    public static boolean isValidCard18(final String idcard, final boolean ignoreCase) {
        return CIN.verify(idcard, ignoreCase);
    }

    /**
     * Checks if the 10-digit ID card number is valid. This is generally used for ID cards from Taiwan, Macau, and Hong
     * Kong.
     *
     * @param idcard The 10-digit ID card number.
     * @return {@code true} if the ID card number is valid.
     */
    public static boolean isValidCard10(final String idcard) {
        try {
            return CIN10.of(idcard).isVerified();
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the birthday string (yyyyMMdd) from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idCard The ID card number.
     * @return The birthday string (yyyyMMdd).
     */
    public static String getBirth(final String idCard) {
        return getCIN(idCard).getBirth();
    }

    /**
     * Gets the birth date as a DateTime object from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idCard The ID card number.
     * @return The birth date.
     */
    public static DateTime getBirthDate(final String idCard) {
        return getCIN(idCard).getBirthDate();
    }

    /**
     * Gets the age from the ID card number based on the current date. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The age.
     */
    public static int getAge(final String idcard) {
        return getAge(idcard, DateKit.now());
    }

    /**
     * Gets the age from the ID card number as of a specified date. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard        The ID card number.
     * @param dateToCompare The date to compare against to calculate age.
     * @return The age.
     */
    public static int getAge(final String idcard, final Date dateToCompare) {
        return DateKit.age(getBirthDate(idcard), dateToCompare);
    }

    /**
     * Gets the birth year from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The birth year (yyyy).
     */
    public static Short getBirthYear(final String idcard) {
        return getCIN(idcard).getBirthYear();
    }

    /**
     * Gets the birth month from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The birth month (MM).
     */
    public static Short getBirthMonth(final String idcard) {
        return getCIN(idcard).getBirthMonth();
    }

    /**
     * Gets the birth day from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The birth day (dd).
     */
    public static Short getBirthDay(final String idcard) {
        return getCIN(idcard).getBirthDay();
    }

    /**
     * Gets the gender from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return Gender (1 for male, 0 for female).
     */
    public static int getGender(final String idcard) {
        return getCIN(idcard).getGender();
    }

    /**
     * Gets the province code from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The province code.
     */
    public static String getProvinceCode(final String idcard) {
        return getCIN(idcard).getProvinceCode();
    }

    /**
     * Gets the province name from the ID card number. Only supports 15 or 18-digit ID numbers.
     *
     * @param idcard The ID card number.
     * @return The province name.
     */
    public static String getProvince(final String idcard) {
        return getCIN(idcard).getProvince();
    }

    /**
     * Gets the city-level code from the ID card number. Only supports 15 or 18-digit ID numbers. The code is 4 digits
     * long.
     *
     * @param idcard The ID card number.
     * @return The city-level code.
     */
    public static String getCityCode(final String idcard) {
        return getCIN(idcard).getCityCode();
    }

    /**
     * Gets the district/county-level code from the ID card number. Only supports 15 or 18-digit ID numbers. The code is
     * 6 digits long.
     *
     * @param idcard The ID card number.
     * @return The district/county-level code.
     */
    public static String getDistrictCode(final String idcard) {
        return getCIN(idcard).getDistrictCode();
    }

    /**
     * Hides a portion of the ID card number with asterisks (*).
     *
     * @param idcard       The ID card number.
     * @param startInclude The starting index (inclusive).
     * @param endExclude   The ending index (exclusive).
     * @return The hidden ID card number.
     * @see StringKit#hide(CharSequence, int, int)
     */
    public static String hide(final String idcard, final int startInclude, final int endExclude) {
        return StringKit.hide(idcard, startInclude, endExclude);
    }

    /**
     * Gets the Citizen ID (CIN) information object, which includes province, city code, birthday, gender, etc.
     *
     * @param idcard The 15 or 18-digit ID card number.
     * @return A {@link CIN} object.
     */
    public static CIN getCIN(final String idcard) {
        return CIN.of(idcard);
    }

    /**
     * Validates a Mainland Travel Permit for Hong Kong and Macao Residents (also known as Home Return Permit). The
     * permit number has the following format:
     * <ul>
     * <li>The permit number is 11 digits long.</li>
     * <li>The first character is a letter: 'H' for Hong Kong residents, 'M' for Macao residents.</li>
     * <li>The next 10 digits are numbers. The first 8 are a lifelong ID number, and the last 2 indicate the renewal
     * count (00 for the first issue).</li>
     * </ul>
     * Example: H1234567800, M1234567801
     *
     * <p>
     * Reference: <a href=
     * "https://www.hmo.gov.cn/fwga_new/wldjnd/201711/t20171120_1333.html">https://www.hmo.gov.cn/fwga_new/wldjnd/201711/t20171120_1333.html</a>
     *
     * @param idCard The permit number.
     * @return {@code true} if the permit number is valid.
     */
    public static boolean isValidHkMoHomeReturn(final String idCard) {
        if (StringKit.isEmpty(idCard)) {
            return false;
        }
        // Rule: H/M + 8 or 10 digits
        // Sample: H1234567890
        final String reg = "^[HhMm](\\d{8}|\\d{10})$";
        return idCard.matches(reg);
    }

}
