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

import java.net.MalformedURLException;
import java.util.Date;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.data.CreditCode;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.*;

/**
 * Field validator for checking various data formats and constraints.
 *
 * <p>
 * This class provides two types of validation methods:
 * <ul>
 * <li>isXXX methods: Return boolean values to check if a value meets a specific format.</li>
 * <li>validateXXX methods: Throw {@link ValidateException} if a value does not meet a specific format.</li>
 * </ul>
 *
 * <p>
 * These methods can be used to validate fields for non-null values, specific formats (such as Email, phone numbers),
 * and other constraints.
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * // Check if a value is a valid email
 * boolean isValid = Validator.isEmail("user@example.com");
 *
 * // Validate a phone number, throwing an exception if invalid
 * try {
 *     Validator.validateMobile("13800138000", "Invalid mobile number");
 * } catch (ValidateException e) {
 *     // Handle validation error
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Validator {

    /**
     * Checks if the given value is {@code true}.
     *
     * @param value the value to check
     * @return true if the value is {@code true}
     */
    public static boolean isTrue(final boolean value) {
        return value;
    }

    /**
     * Checks if the given value is not {@code false}.
     *
     * @param value the value to check
     * @return true if the value is not {@code false}
     */
    public static boolean isFalse(final boolean value) {
        return !value;
    }

    /**
     * Validates that the specified value is {@code true}.
     *
     * @param value            the value to validate
     * @param errorMsgTemplate error message template (variables represented by {})
     * @param args             template variable replacement values
     * @return the validated value
     * @throws ValidateException if the condition is not met
     */
    public static boolean validateTrue(final boolean value, final String errorMsgTemplate, final Object... args)
            throws ValidateException {
        if (isFalse(value)) {
            throw new ValidateException(errorMsgTemplate, args);
        }
        return true;
    }

    /**
     * Validates that the specified value is {@code false}.
     *
     * @param value            the value to validate
     * @param errorMsgTemplate error message template (variables represented by {})
     * @param args             template variable replacement values
     * @return the validated value
     * @throws ValidateException if the condition is not met
     */
    public static boolean validateFalse(final boolean value, final String errorMsgTemplate, final Object... args)
            throws ValidateException {
        if (isTrue(value)) {
            throw new ValidateException(errorMsgTemplate, args);
        }
        return false;
    }

    /**
     * Checks if the given value is {@code null}.
     *
     * @param value the value to check
     * @return true if the value is {@code null}
     */
    public static boolean isNull(final Object value) {
        return null == value;
    }

    /**
     * Checks if the given value is not {@code null}.
     *
     * @param value the value to check
     * @return true if the value is not {@code null}
     */
    public static boolean isNotNull(final Object value) {
        return null != value;
    }

    /**
     * Validates that the specified value is {@code null}.
     *
     * @param <T>              the type of the object being checked
     * @param value            the value to validate
     * @param errorMsgTemplate error message template (variables represented by {})
     * @param args             template variable replacement values
     * @return the validated value
     * @throws ValidateException if the condition is not met
     */
    public static <T> T validateNull(final T value, final String errorMsgTemplate, final Object... args)
            throws ValidateException {
        if (isNotNull(value)) {
            throw new ValidateException(errorMsgTemplate, args);
        }
        return null;
    }

    /**
     * Validates that the specified value is not {@code null}.
     *
     * @param <T>              the type of the object being checked
     * @param value            the value to validate
     * @param errorMsgTemplate error message template (variables represented by {})
     * @param args             template variable replacement values
     * @return the validated value
     * @throws ValidateException if the condition is not met
     */
    public static <T> T validateNotNull(final T value, final String errorMsgTemplate, final Object... args)
            throws ValidateException {
        if (isNull(value)) {
            throw new ValidateException(errorMsgTemplate, args);
        }
        return value;
    }

    /**
     * Validates if the value is empty. For String types, checks if it's empty (null or "").
     *
     * @param value the value to check
     * @return true if the value is empty
     * @see ObjectKit#isEmpty(Object)
     */
    public static boolean isEmpty(final Object value) {
        return ObjectKit.isEmpty(value);
    }

    /**
     * Validates if the value is not empty. For String types, checks if it's not empty (null or "").
     *
     * @param value the value to check
     * @return true if the value is not empty
     * @see ObjectKit#isNotEmpty(Object)
     */
    public static boolean isNotEmpty(final Object value) {
        return ObjectKit.isNotEmpty(value);
    }

    /**
     * Validates that the value is empty, throwing an exception if not. For String types, checks if it's empty (null or
     * "").
     *
     * @param <T>      the type of the value
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value (empty)
     * @throws ValidateException if the validation fails
     */
    public static <T> T validateEmpty(final T value, final String errorMsg) throws ValidateException {
        if (isNotEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates that the value is not empty, throwing an exception if it is. For String types, checks if it's not empty
     * (null or "").
     *
     * @param <T>      the type of the value
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value (non-empty)
     * @throws ValidateException if the validation fails
     */
    public static <T> T validateNotEmpty(final T value, final String errorMsg) throws ValidateException {
        if (isEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if two values are equal. Returns true when both values are null.
     *
     * @param t1 the first object
     * @param t2 the second object
     * @return true if both values are null or equal
     */
    public static boolean equal(final Object t1, final Object t2) {
        return ObjectKit.equals(t1, t2);
    }

    /**
     * Validates that two values are equal, throwing an exception if not.
     *
     * @param t1       the first object
     * @param t2       the second object
     * @param errorMsg error message
     * @return the first value
     * @throws ValidateException if the validation fails
     */
    public static Object validateEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        if (!equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
        return t1;
    }

    /**
     * Validates that two values are not equal, throwing an exception if they are.
     *
     * @param t1       the first object
     * @param t2       the second object
     * @param errorMsg error message
     * @throws ValidateException if the validation fails
     */
    public static void validateNotEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        if (equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * Validates that a value is not empty and equals the specified value. Throws an exception if the value is empty or
     * not equal.
     *
     * @param t1       the first object
     * @param t2       the second object
     * @param errorMsg error message
     * @throws ValidateException if the validation fails
     */
    public static void validateNotEmptyAndEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateEqual(t1, t2, errorMsg);
    }

    /**
     * Validates that a value is not empty and not equal to the specified value. Throws an exception if the value is
     * empty or equal.
     *
     * @param t1       the first object
     * @param t2       the second object
     * @param errorMsg error message
     * @throws ValidateException if the validation fails
     */
    public static void validateNotEmptyAndNotEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateNotEqual(t1, t2, errorMsg);
    }

    /**
     * Validates a value against a regular expression. Throws a {@link ValidateException} if the value doesn't match the
     * pattern.
     *
     * @param <T>      the string type
     * @param regex    the regular expression
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateMatchRegex(
            final String regex,
            final T value,
            final String errorMsg) throws ValidateException {
        if (!isMatchRegex(regex, value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates a value against a compiled regular expression pattern.
     *
     * @param pattern the compiled regular expression pattern
     * @param value   the value to validate
     * @return true if the value matches the pattern
     */
    public static boolean isMatchRegex(final java.util.regex.Pattern pattern, final CharSequence value) {
        return PatternKit.isMatch(pattern, value);
    }

    /**
     * Validates a value against a regular expression.
     *
     * @param regex the regular expression
     * @param value the value to validate
     * @return true if the value matches the regular expression
     */
    public static boolean isMatchRegex(final String regex, final CharSequence value) {
        return PatternKit.isMatch(regex, value);
    }

    /**
     * Validates if the value consists of English letters, numbers, and underscores.
     *
     * @param value the value to validate
     * @return true if the value consists of English letters, numbers, and underscores
     */
    public static boolean isGeneral(final CharSequence value) {
        return isMatchRegex(Pattern.GENERAL_PATTERN, value);
    }

    /**
     * Validates that the value consists of English letters, numbers, and underscores.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateGeneral(final T value, final String errorMsg)
            throws ValidateException {
        if (!isGeneral(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value consists of English letters, numbers, and underscores within the specified length range.
     *
     * @param value the value to validate
     * @param min   minimum length, negative values are treated as 0
     * @param max   maximum length, 0 or negative values indicate no maximum length limit
     * @return true if the value meets the criteria
     */
    public static boolean isGeneral(final CharSequence value, int min, final int max) {
        if (min < 0) {
            min = 0;
        }
        String reg = "^\\w{" + min + Symbol.COMMA + max + "}$";
        if (max <= 0) {
            reg = "^\\w{" + min + ",}$";
        }
        return isMatchRegex(reg, value);
    }

    /**
     * Validates that the value consists of English letters, numbers, and underscores within the specified length range.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param min      minimum length, negative values are treated as 0
     * @param max      maximum length, 0 or negative values indicate no maximum length limit
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateGeneral(
            final T value,
            final int min,
            final int max,
            final String errorMsg) throws ValidateException {
        if (!isGeneral(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value consists of English letters, numbers, and underscores with at least the specified minimum
     * length.
     *
     * @param value the value to validate
     * @param min   minimum length, negative values are treated as 0
     * @return true if the value meets the criteria
     */
    public static boolean isGeneral(final CharSequence value, final int min) {
        return isGeneral(value, min, 0);
    }

    /**
     * Validates that the value consists of English letters, numbers, and underscores with at least the specified
     * minimum length.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param min      minimum length, negative values are treated as 0
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateGeneral(final T value, final int min, final String errorMsg)
            throws ValidateException {
        return validateGeneral(value, min, 0, errorMsg);
    }

    /**
     * Checks if the string consists entirely of letters, including uppercase, lowercase, and Chinese characters.
     *
     * @param value the value to check
     * @return true if the string consists entirely of letters
     */
    public static boolean isLetter(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLetter);
    }

    /**
     * Validates that the string consists entirely of letters, including uppercase, lowercase, and Chinese characters.
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateLetter(final T value, final String errorMsg)
            throws ValidateException {
        if (!isLetter(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Checks if the string consists entirely of uppercase letters.
     *
     * @param value the value to check
     * @return true if the string consists entirely of uppercase letters
     */
    public static boolean isUpperCase(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isUpperCase);
    }

    /**
     * Validates that the string consists entirely of uppercase letters.
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateUpperCase(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUpperCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Checks if the string consists entirely of lowercase letters.
     *
     * @param value the value to check
     * @return true if the string consists entirely of lowercase letters
     */
    public static boolean isLowerCase(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLowerCase);
    }

    /**
     * Validates that the string consists entirely of lowercase letters.
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateLowerCase(final T value, final String errorMsg)
            throws ValidateException {
        if (!isLowerCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the string is a number.
     *
     * @param value the string content
     * @return true if the string is a number
     */
    public static boolean isNumber(final CharSequence value) {
        return MathKit.isNumber(value);
    }

    /**
     * Checks if the string contains any numbers.
     *
     * @param value the current string
     * @return true if the string contains any numbers
     */
    public static boolean hasNumber(final CharSequence value) {
        return PatternKit.contains(Pattern.NUMBERS_PATTERN, value);
    }

    /**
     * Validates that the string is a number.
     *
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static String validateNumber(final String value, final String errorMsg) throws ValidateException {
        if (!isNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the string consists of letters (uppercase and lowercase).
     *
     * @param value the string content
     * @return true if the string consists of letters
     */
    public static boolean isWord(final CharSequence value) {
        return isMatchRegex(Pattern.WORD_PATTERN, value);
    }

    /**
     * Validates that the string consists of letters (uppercase and lowercase).
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateWord(final T value, final String errorMsg)
            throws ValidateException {
        if (!isWord(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a monetary amount.
     *
     * @param value the value to validate
     * @return true if the value is a monetary amount
     */
    public static boolean isMoney(final CharSequence value) {
        return isMatchRegex(Pattern.MONEY_PATTERN, value);
    }

    /**
     * Validates that the value is a monetary amount.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateMoney(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMoney(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;

    }

    /**
     * Validates if the value is a postal code (China).
     *
     * @param value the value to validate
     * @return true if the value is a postal code (China)
     */
    public static boolean isZipCode(final CharSequence value) {
        return isMatchRegex(Pattern.ZIP_CODE_PATTERN, value);
    }

    /**
     * Validates that the value is a postal code (China).
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateZipCode(final T value, final String errorMsg)
            throws ValidateException {
        if (!isZipCode(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a valid email address. Email addresses are limited to 254 characters, reference:
     * https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754
     *
     * @param value the value to validate
     * @return true if the value is a valid email address
     */
    public static boolean isEmail(final CharSequence value) {
        final int codeLength = StringKit.codeLength(value);
        if (codeLength < 1 || codeLength > 254) {
            return false;
        }

        return isMatchRegex(Pattern.EMAIL_PATTERN, value);
    }

    /**
     * Validates that the value is a valid email address.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateEmail(final T value, final String errorMsg)
            throws ValidateException {
        if (!isEmail(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a mobile phone number (China).
     *
     * @param value the value to validate
     * @return true if the value is a mobile phone number (China)
     */
    public static boolean isMobile(final CharSequence value) {
        return isMatchRegex(Regex.MOBILE_PATTERN, value);
    }

    /**
     * Validates that the value is a mobile phone number (China).
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateMobile(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMobile(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a valid ID card number (supports 18-digit, 15-digit, and 10-digit for Hong Kong, Macau,
     * and Taiwan).
     *
     * @param value the ID card number, supports 18-digit, 15-digit, and 10-digit for Hong Kong, Macau, and Taiwan
     * @return true if the value is a valid ID card number
     */
    public static boolean isCitizenId(final CharSequence value) {
        return CitizenIdKit.isValidCard(String.valueOf(value));
    }

    /**
     * Validates that the value is a valid ID card number (supports 18-digit, 15-digit, and 10-digit for Hong Kong,
     * Macau, and Taiwan).
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateCitizenIdNumber(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCitizenId(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the given year, month, and day form a valid birthday.
     *
     * @param year  the year, starting from 1900
     * @param month the month, starting from 1
     * @param day   the day, starting from 1
     * @return true if the values form a valid birthday
     */
    public static boolean isBirthday(final int year, final int month, final int day) {
        // Validate year
        final int thisYear = DateKit.thisYear();
        if (year < 1900 || year > thisYear) {
            return false;
        }

        // Validate month
        if (month < 1 || month > 12) {
            return false;
        }

        // Validate day
        if (day < 1 || day > 31) {
            return false;
        }
        // Check special months with maximum 30 days
        if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
            return false;
        }
        if (month == 2) {
            // In February, non-leap years have maximum 28 days, leap years have 29
            return day < 29 || (day == 29 && DateKit.isLeapYear(year));
        }
        return true;
    }

    /**
     * Validates if the value is a valid birthday. Only supports the following formats:
     * <ul>
     * <li>yyyyMMdd</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>yyyy年MM月dd日</li>
     * </ul>
     *
     * @param value the value to validate
     * @return true if the value is a valid birthday
     */
    public static boolean isBirthday(final CharSequence value) {
        final Matcher matcher = Pattern.BIRTHDAY_PATTERN.matcher(value);
        if (matcher.find()) {
            final int year = Integer.parseInt(matcher.group(1));
            final int month = Integer.parseInt(matcher.group(3));
            final int day = Integer.parseInt(matcher.group(5));
            return isBirthday(year, month, day);
        }
        return false;
    }

    /**
     * Validates that the value is a valid birthday.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateBirthday(final T value, final String errorMsg)
            throws ValidateException {
        if (!isBirthday(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is an IPv4 address.
     *
     * @param value the value to validate
     * @return true if the value is an IPv4 address
     */
    public static boolean isIpv4(final CharSequence value) {
        return isMatchRegex(Pattern.IPV4_PATTERN, value);
    }

    /**
     * Validates that the value is an IPv4 address.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateIpv4(final T value, final String errorMsg)
            throws ValidateException {
        if (!isIpv4(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is an IPv6 address.
     *
     * @param value the value to validate
     * @return true if the value is an IPv6 address
     */
    public static boolean isIpv6(final CharSequence value) {
        return isMatchRegex(Pattern.IPV6_PATTERN, value);
    }

    /**
     * Validates that the value is an IPv6 address.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateIpv6(final T value, final String errorMsg)
            throws ValidateException {
        if (!isIpv6(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a MAC address.
     *
     * @param value the value to validate
     * @return true if the value is a MAC address
     */
    public static boolean isMac(final CharSequence value) {
        return isMatchRegex(Pattern.MAC_ADDRESS_PATTERN, value);
    }

    /**
     * Validates that the value is a MAC address.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateMac(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMac(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a Chinese license plate number.
     *
     * @param value the value to validate
     * @return true if the value is a Chinese license plate number
     */
    public static boolean isPlateNumber(final CharSequence value) {
        return isMatchRegex(Pattern.PLATE_NUMBER_PATTERN, value);
    }

    /**
     * Validates that the value is a Chinese license plate number.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validatePlateNumber(final T value, final String errorMsg)
            throws ValidateException {
        if (!isPlateNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a URL.
     *
     * @param value the value to validate
     * @return true if the value is a URL
     */
    public static boolean isUrl(final CharSequence value) {
        if (StringKit.isBlank(value)) {
            return false;
        }
        try {
            new java.net.URL(value.toString());
        } catch (final MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * Validates that the value is a URL.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateUrl(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUrl(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value consists entirely of Chinese characters.
     *
     * @param value the value to validate
     * @return true if the value consists entirely of Chinese characters
     */
    public static boolean isChinese(final CharSequence value) {
        return isMatchRegex(Pattern.CHINESES_PATTERN, value);
    }

    /**
     * Validates if the value contains any Chinese characters.
     *
     * @param value the value to validate
     * @return true if the value contains any Chinese characters
     */
    public static boolean hasChinese(final CharSequence value) {
        return PatternKit.contains(Regex.CHINESES, value);
    }

    /**
     * Validates that the value consists entirely of Chinese characters.
     *
     * @param <T>      the string type
     * @param value    the form value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateChinese(final T value, final String errorMsg)
            throws ValidateException {
        if (!isChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value consists of Chinese characters, English letters, numbers, and underscores.
     *
     * @param value the value to validate
     * @return true if the value consists of Chinese characters, English letters, numbers, and underscores
     */
    public static boolean isGeneralWithChinese(final CharSequence value) {
        return isMatchRegex(Pattern.GENERAL_WITH_CHINESE_PATTERN, value);
    }

    /**
     * Validates that the value consists of Chinese characters, English letters, numbers, and underscores.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateGeneralWithChinese(final T value, final String errorMsg)
            throws ValidateException {
        if (!isGeneralWithChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a UUID, including both standard format with hyphens and simple format without hyphens.
     *
     * @param value the value to validate
     * @return true if the value is a UUID
     */
    public static boolean isUUID(final CharSequence value) {
        return isMatchRegex(Pattern.UUID_PATTERN, value) || isMatchRegex(Pattern.UUID_SIMPLE_PATTERN, value);
    }

    /**
     * Validates that the value is a UUID, including both standard format with hyphens and simple format without
     * hyphens.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateUUID(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUUID(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a Hex (16进制) string.
     *
     * @param value the value to validate
     * @return true if the value is a Hex (16进制) string
     */
    public static boolean isHex(final CharSequence value) {
        return isMatchRegex(Pattern.HEX_PATTERN, value);
    }

    /**
     * Validates that the value is a Hex (16进制) string.
     *
     * @param <T>      the string type
     * @param value    the value to validate
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateHex(final T value, final String errorMsg)
            throws ValidateException {
        if (!isHex(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Checks if the given number is within the specified range.
     *
     * @param value the value to check
     * @param min   the minimum value (inclusive)
     * @param max   the maximum value (inclusive)
     * @return true if the value is within the range
     */
    public static boolean isBetween(final Number value, final Number min, final Number max) {
        Assert.notNull(value);
        Assert.notNull(min);
        Assert.notNull(max);
        final double doubleValue = value.doubleValue();
        return (doubleValue >= min.doubleValue()) && (doubleValue <= max.doubleValue());
    }

    /**
     * Validates that the given number is within the specified range.
     *
     * @param value    the value to validate
     * @param min      the minimum value (inclusive)
     * @param max      the maximum value (inclusive)
     * @param errorMsg validation error message
     * @throws ValidateException if the validation fails
     */
    public static void validateBetween(final Number value, final Number min, final Number max, final String errorMsg)
            throws ValidateException {
        if (!isBetween(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * Validates that the given date is within the specified range.
     *
     * @param value    the value to validate
     * @param start    the minimum value (inclusive)
     * @param end      the maximum value (inclusive)
     * @param errorMsg validation error message
     * @throws ValidateException if the validation fails
     */
    public static void validateBetween(final Date value, final Date start, final Date end, final String errorMsg) {
        Assert.notNull(value);
        Assert.notNull(start);
        Assert.notNull(end);

        if (!DateKit.isIn(value, start, end)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * Checks if the value is a valid unified social credit code.
     *
     * <pre>
     * Part 1: Registration management department code, 1 digit (number or uppercase English letter)
     * Part 2: Institution category code, 1 digit (number or uppercase English letter)
     * Part 3: Registration management administrative division code, 6 digits (numbers)
     * Part 4: Subject identification code (organization code), 9 digits (numbers or uppercase English letters)
     * Part 5: Check code, 1 digit (number or uppercase English letter)
     * </pre>
     *
     * @param creditCode the unified social credit code
     * @return validation result
     */
    public static boolean isCreditCode(final CharSequence creditCode) {
        return CreditCode.isCreditCode(creditCode);
    }

    /**
     * Validates if the value is a car VIN (Vehicle Identification Number). Alias: driving license number, vehicle
     * identification code.
     *
     * @param value the value, 17-digit VIN; e.g., LSJA24U62JG269225, LDC613P23A1305189
     * @return true if the value is a car VIN
     */
    public static boolean isCarVin(final CharSequence value) {
        return isMatchRegex(Pattern.CAR_VIN_PATTERN, value);
    }

    /**
     * Validates that the value is a car VIN (Vehicle Identification Number). Alias: driving license number, vehicle
     * identification code.
     *
     * @param <T>      the string type
     * @param value    the value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateCarVin(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCarVin(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the value is a driving license number. Alias: driving license file number, vehicle license number.
     * Limited to: Chinese driving license file number.
     *
     * @param value the value, 12-digit number string, e.g., 430101758218
     * @return true if the value is a file number
     */
    public static boolean isCarDrivingLicence(final CharSequence value) {
        return isMatchRegex(Pattern.CAR_DRIVING_LICENCE_PATTERN, value);
    }

    /**
     * Checks if the value is a Chinese name. The dot in Uyghur names is · (input method in Chinese state, the symbol
     * before number 1 in the upper left corner of the keyboard); Wrong characters: {@code ．.。．.} Correct Uyghur names:
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
     * Wrong example: 孟  伟                reason: has spaces
     * Wrong example: 连逍遥0               reason: has numbers
     * Wrong example: 依帕古丽-艾则孜        reason: has special symbols
     * Wrong example: 牙力空.买提萨力        reason: wrong dot for Xinjiang people
     * Wrong example: 王建鹏2002-3-2        reason: has numbers, special symbols
     * Wrong example: 雷金默(雷皓添）        reason: has parentheses
     * Wrong example: 翟冬:亮               reason: has special symbols
     * Wrong example: 李                   reason: less than 2 characters
     * ----------
     * </pre>
     *
     * Summary of Chinese names: 2-60 characters, only Chinese characters and ·
     *
     * @param value the Chinese name
     * @return true if it's a correct Chinese name
     */
    public static boolean isChineseName(final CharSequence value) {
        return isMatchRegex(Regex.CHINESE_NAME, value);
    }

    /**
     * Validates that the value is a driving license number. Alias: driving license file number, vehicle license number.
     *
     * @param <T>      the string type
     * @param value    the value
     * @param errorMsg validation error message
     * @return the validated value
     * @throws ValidateException if the validation fails
     */
    public static <T extends CharSequence> T validateCarDrivingLicence(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCarDrivingLicence(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * Validates if the character length meets the requirements.
     *
     * @param text     the string
     * @param min      minimum length
     * @param max      maximum length
     * @param errorMsg error message
     */
    public static void validateLength(final CharSequence text, final int min, final int max, final String errorMsg) {
        final int len = StringKit.length(text);
        if (len < min || len > max) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * Validates if the byte length of the string meets the requirements, using "utf-8" encoding by default.
     *
     * @param text     the string
     * @param min      minimum length
     * @param max      maximum length
     * @param errorMsg error message
     */
    public static void validateByteLength(
            final CharSequence text,
            final int min,
            final int max,
            final String errorMsg) {
        validateByteLength(text, min, max, Charset.UTF_8, errorMsg);
    }

    /**
     * Validates if the byte length of the string meets the requirements.
     *
     * @param text     the string
     * @param min      minimum length
     * @param max      maximum length
     * @param charset  character encoding
     * @param errorMsg error message
     */
    public static void validateByteLength(
            final CharSequence text,
            final int min,
            final int max,
            final java.nio.charset.Charset charset,
            final String errorMsg) {
        final int len = StringKit.byteLength(text, charset);
        if (len < min || len > max) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * Checks if the given index exceeds the length limit.
     * <ul>
     * <li>When calling setOrPadding on an array, the maximum allowed padding length</li>
     * <li>When calling setOrPadding on a List, the maximum allowed padding length</li>
     * <li>When calling setOrPadding on a JSONArray, the maximum allowed padding length</li>
     * </ul>
     *
     * @param index the index
     * @param limit the limit size
     */
    public static void checkIndexLimit(final int index, final int limit) {
        // Add safety check, maximum increase of 10 times
        if (index > limit) {
            throw new ValidateException("Index [{}] is too large for limit: [{}]", index, limit);
        }
    }

}
