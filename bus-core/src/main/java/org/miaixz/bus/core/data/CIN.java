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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.xyz.*;

/**
 * Citizen Identification Number (CIN), according to the GB11643-1999 standard.
 * <ul>
 * <li>Digits 1-2: 2-digit province code.</li>
 * <li>Digits 3-4: 2-digit city code.</li>
 * <li>Digits 5-6: 2-digit district/county code.</li>
 * <li>Digits 7-14: 8-digit date of birth code.</li>
 * <li>Digits 15-17: 3-digit sequence code. The 17th digit indicates gender: odd for male, even for female.</li>
 * <li>Digit 18: 1-digit checksum. The checksum can be a digit from 0-9, or sometimes 'X'.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CIN {

    /**
     * Minimum length of a Chinese citizen ID number.
     */
    public static final int CHINA_ID_MIN_LENGTH = 15;
    /**
     * Maximum length of a Chinese citizen ID number.
     */
    public static final int CHINA_ID_MAX_LENGTH = 18;

    /**
     * Weighting factor for each digit.
     */
    private static final int[] POWER = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
    /**
     * Province and city code table.
     */
    private static final Map<String, String> CITY_CODES = new HashMap<>();

    static {
        CITY_CODES.put("11", "Beijing");
        CITY_CODES.put("12", "Tianjin");
        CITY_CODES.put("13", "Hebei");
        CITY_CODES.put("14", "Shanxi");
        CITY_CODES.put("15", "Inner Mongolia");
        CITY_CODES.put("21", "Liaoning");
        CITY_CODES.put("22", "Jilin");
        CITY_CODES.put("23", "Heilongjiang");
        CITY_CODES.put("31", "Shanghai");
        CITY_CODES.put("32", "Jiangsu");
        CITY_CODES.put("33", "Zhejiang");
        CITY_CODES.put("34", "Anhui");
        CITY_CODES.put("35", "Fujian");
        CITY_CODES.put("36", "Jiangxi");
        CITY_CODES.put("37", "Shandong");
        CITY_CODES.put("41", "Henan");
        CITY_CODES.put("42", "Hubei");
        CITY_CODES.put("43", "Hunan");
        CITY_CODES.put("44", "Guangdong");
        CITY_CODES.put("45", "Guangxi");
        CITY_CODES.put("46", "Hainan");
        CITY_CODES.put("50", "Chongqing");
        CITY_CODES.put("51", "Sichuan");
        CITY_CODES.put("52", "Guizhou");
        CITY_CODES.put("53", "Yunnan");
        CITY_CODES.put("54", "Tibet");
        CITY_CODES.put("61", "Shaanxi");
        CITY_CODES.put("62", "Gansu");
        CITY_CODES.put("63", "Qinghai");
        CITY_CODES.put("64", "Ningxia");
        CITY_CODES.put("65", "Xinjiang");
        CITY_CODES.put("71", "Taiwan");
        CITY_CODES.put("81", "Hong Kong");
        CITY_CODES.put("82", "Macau");
        // Taiwan ID cards start with 83, but the administrative division is 71.
        CITY_CODES.put("83", "Taiwan");
        CITY_CODES.put("91", "Overseas");
    }

    /**
     * The ID card number.
     */
    private final String code;

    /**
     * Constructor.
     *
     * @param code The ID card number.
     */
    public CIN(String code) {
        final int length = code.length();
        Assert.isTrue(length == CHINA_ID_MIN_LENGTH || length == CHINA_ID_MAX_LENGTH, "CIN length must be 15 or 18!");
        if (length == CHINA_ID_MIN_LENGTH) {
            // Convert 15-digit ID card number to 18-digit.
            code = convert15To18(code);
        }
        Assert.isTrue(verify(code, true), "Invalid CIN code!");
        this.code = code;
    }

    /**
     * Creates a CIN object.
     *
     * @param code The ID card number.
     * @return A CIN object.
     */
    public static CIN of(final String code) {
        return new CIN(code);
    }

    /**
     * Converts a 15-digit ID card number to an 18-digit one. The 15-digit ID card number follows the GB 11643-1989
     * standard.
     *
     * @param idCard The 15-digit ID card number.
     * @return The 18-digit ID card number.
     */
    public static String convert15To18(final String idCard) {
        final StringBuilder idCard18;
        if (idCard.length() != CIN.CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (PatternKit.isMatch(Pattern.NUMBERS_PATTERN, idCard)) {
            // Get the date of birth.
            final String birthday = idCard.substring(6, 12);
            final Date birthDate;
            try {
                birthDate = DateKit.parse(birthday, "yyMMdd");
            } catch (final Exception ignore) {
                throw new IllegalArgumentException("Invalid birthday: " + birthday);
            }
            // Get the full year of birth (e.g., 2010).
            int sYear = DateKit.year(birthDate);
            if (sYear > 2000) {
                // There are no 15-digit ID cards after 2000. This is to fix this issue.
                sYear -= 100;
            }
            idCard18 = StringKit.builder().append(idCard, 0, 6).append(sYear).append(idCard.substring(8));
            // Get the checksum.
            final char sVal = getVerifyCode18(idCard18.toString());
            idCard18.append(sVal);
        } else {
            return null;
        }
        return idCard18.toString();
    }

    /**
     * Converts an 18-digit ID card number to a 15-digit one.
     *
     * @param idCard The 18-digit ID card number.
     * @return The 15-digit ID card number.
     */
    public static String convert18To15(final String idCard) {
        if (StringKit.isNotBlank(idCard) && CitizenIdKit.isValidCard18(idCard)) {
            return idCard.substring(0, 6) + idCard.substring(8, idCard.length() - 1);
        }
        return idCard;
    }

    /**
     * Validates an 18-digit ID card number. The calculation method for the 18th digit (checksum) is as follows:
     * <ol>
     * <li>Multiply the first 17 digits of the ID card number by different coefficients. The coefficients from the first
     * to the 17th digit are: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2.</li>
     * <li>Sum the results of the multiplication of these 17 digits and their coefficients.</li>
     * <li>Divide the sum by 11 and get the remainder.</li>
     * <li>The remainder can only be one of these 11 numbers: 0 1 2 3 4 5 6 7 8 9 10. The corresponding last digit of
     * the ID card is 1 0 X 9 8 7 6 5 4 3 2.</li>
     * <li>From the above, if the remainder is 2, the 18th digit of the ID card will be the Roman numeral X. If the
     * remainder is 10, the last digit of the ID card is 2.</li>
     * </ol>
     *
     * @param idCard     The ID card to be validated.
     * @param ignoreCase Whether to ignore case. If {@code true}, ignore the case of 'X'; otherwise, strictly match the
     *                   uppercase 'X'.
     * @return {@code true} if the 18-digit ID card is valid, {@code false} otherwise.
     */
    public static boolean verify(final String idCard, final boolean ignoreCase) {
        if (StringKit.isBlank(idCard) || CHINA_ID_MAX_LENGTH != idCard.length()) {
            return false;
        }

        // Intercept the province code. The new version of the foreigner's permanent residence ID card starts with 9,
        // and the second and third digits are the acceptance place code.
        final String proCode = StringKit.startWith(idCard, '9') ? idCard.substring(1, 3) : idCard.substring(0, 2);
        if (null == CITY_CODES.get(proCode)) {
            return false;
        }

        // Validate the birthday.
        if (!Validator.isBirthday(idCard.substring(6, 14))) {
            return false;
        }

        // The first 17 digits.
        final String code17 = idCard.substring(0, 17);
        if (PatternKit.isMatch(Pattern.NUMBERS_PATTERN, code17)) {
            // Get the checksum.
            final char val = getVerifyCode18(code17);
            // The 18th digit.
            return CharKit.equals(val, idCard.charAt(17), ignoreCase);
        }
        return false;
    }

    /**
     * Gets the 18-digit ID card checksum.
     *
     * @param code17 The first 17 digits of the 18-digit ID card number.
     * @return The 18th digit.
     */
    private static char getVerifyCode18(final String code17) {
        final int sum = getPowerSum(code17.toCharArray());
        return getVerifyCode18(sum);
    }

    /**
     * Calculates the checksum character based on the weighted sum modulo 11.
     *
     * @param iSum The weighted sum.
     * @return The checksum character.
     */
    private static char getVerifyCode18(final int iSum) {
        switch (iSum % 11) {
            case 10:
                return '2';

            case 9:
                return '3';

            case 8:
                return '4';

            case 7:
                return '5';

            case 6:
                return '6';

            case 5:
                return '7';

            case 4:
                return '8';

            case 3:
                return '9';

            case 2:
                return Symbol.C_X;

            case 1:
                return '0';

            case 0:
                return '1';

            default:
                return Symbol.C_SPACE;
        }
    }

    /**
     * Calculates the weighted sum of the ID card digits.
     *
     * @param iArr The array of ID card digits.
     * @return The weighted sum.
     */
    private static int getPowerSum(final char[] iArr) {
        int iSum = 0;
        if (POWER.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                iSum += Integer.parseInt(String.valueOf(iArr[i])) * POWER[i];
            }
        }
        return iSum;
    }

    /**
     * Gets the province code from the ID number.
     *
     * @return The province code.
     */
    public String getProvinceCode() {
        return this.code.substring(0, 2);
    }

    /**
     * Gets the province name from the ID number.
     *
     * @return The province name.
     */
    public String getProvince() {
        final String code = getProvinceCode();
        if (StringKit.isNotBlank(code)) {
            return CITY_CODES.get(code);
        }
        return null;
    }

    /**
     * Gets the city-level code (4 digits) from the ID number.
     *
     * @return The city-level code.
     */
    public String getCityCode() {
        return this.code.substring(0, 4);
    }

    /**
     * Gets the district/county-level code (6 digits) from the ID number.
     *
     * @return The district/county-level code.
     */
    public String getDistrictCode() {
        return this.code.substring(0, 6);
    }

    /**
     * Gets the date of birth (yyyyMMdd) from the ID number.
     *
     * @return The date of birth.
     */
    public String getBirth() {
        return this.code.substring(6, 14);
    }

    /**
     * Gets the date of birth as a DateTime object from the ID number.
     *
     * @return The date of birth.
     */
    public DateTime getBirthDate() {
        final String birth = getBirth();
        return DateKit.parse(birth, Formatter.PURE_DATE_FORMAT);
    }

    /**
     * Gets the age based on the ID number.
     *
     * @return The age.
     */
    public int getAge() {
        return getAge(DateKit.now());
    }

    /**
     * Calculates the age (in full years) as of a specified date, based on the ID number. According to the second
     * article of the Interpretation of the Supreme People's Court on Several Issues Concerning the Specific Application
     * of Law in the Trial of Criminal Cases of Minors, a 'full year' as stipulated in Article 17 of the Criminal Law is
     * calculated according to the Gregorian calendar year, month, and day, starting from the day after the birthday.
     * <ul>
     * <li>Born on 2022-03-01, the age is 0 on 2023-03-01, and 1 on 2023-03-02.</li>
     * <li>Born on 1999-02-28, the age is 1 on 2000-02-29.</li>
     * </ul>
     *
     * @param dateToCompare The date to compare with to calculate the age.
     * @return The age.
     */
    public int getAge(final Date dateToCompare) {
        return DateKit.age(getBirthDate(), dateToCompare);
    }

    /**
     * Gets the year of birth (yyyy) from the ID number.
     *
     * @return The year of birth.
     */
    public Short getBirthYear() {
        return Short.valueOf(this.code.substring(6, 10));
    }

    /**
     * Gets the month of birth (MM) from the ID number.
     *
     * @return The month of birth.
     */
    public Short getBirthMonth() {
        return Short.valueOf(this.code.substring(10, 12));
    }

    /**
     * Gets the day of birth (dd) from the ID number.
     *
     * @return The day of birth.
     */
    public Short getBirthDay() {
        return Short.valueOf(this.code.substring(12, 14));
    }

    /**
     * Gets the gender from the ID number.
     *
     * @return Gender (1 for male, 0 for female).
     */
    public int getGender() {
        final char sCardChar = this.code.charAt(16);
        return (sCardChar % 2 != 0) ? 1 : 0;
    }

}
