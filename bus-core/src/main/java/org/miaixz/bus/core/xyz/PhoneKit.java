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

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Regex;
import org.miaixz.bus.core.lang.Validator;

/**
 * Utility class for phone numbers, including:
 * <ul>
 * <li>Mobile numbers</li>
 * <li>400/800 numbers</li>
 * <li>Landline numbers</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PhoneKit {

    /**
     * Validates if the value is a mobile phone number (Mainland China).
     *
     * @param value The value.
     * @return `true` if it's a valid mobile number.
     */
    public static boolean isMobile(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_PATTERN, value);
    }

    /**
     * Validates if the value is a mobile phone number (Hong Kong, China).
     *
     * @param value The phone number.
     * @return `true` if it's a valid Hong Kong mobile number.
     */
    public static boolean isMobileHk(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_HK, value);
    }

    /**
     * Validates if the value is a mobile phone number (Taiwan, China).
     *
     * @param value The phone number.
     * @return `true` if it's a valid Taiwan mobile number.
     */
    public static boolean isMobileTw(final CharSequence value) {
        return Validator.isMatchRegex(Pattern.MOBILE_TW_PATTERN, value);
    }

    /**
     * Validates if the value is a mobile phone number (Macau, China).
     *
     * @param value The phone number.
     * @return `true` if it's a valid Macau mobile number.
     */
    public static boolean isMobileMo(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_MO, value);
    }

    /**
     * Validates if the value is a landline number (Mainland China).
     *
     * @param value The value.
     * @return `true` if it's a valid landline number.
     */
    public static boolean isTel(final CharSequence value) {
        return Validator.isMatchRegex(Pattern.TEL_PATTERN, value);
    }

    /**
     * Validates if the value is a landline, 400, or 800 number (Mainland China).
     *
     * @param value The value.
     * @return `true` if it's a valid number.
     */
    public static boolean isTel400800(final CharSequence value) {
        return Validator.isMatchRegex(Regex.TEL_400_800, value);
    }

    /**
     * Validates if the value is any type of phone number (landline, mobile, 400/800 for Mainland China, HK, TW, MO).
     *
     * @param value The value.
     * @return `true` if it's a valid phone number.
     */
    public static boolean isPhone(final CharSequence value) {
        return isMobile(value) || isTel400800(value) || isMobileHk(value) || isMobileTw(value) || isMobileMo(value);
    }

    /**
     * Hides the first 7 digits of a phone number with '*'.
     *
     * @param phone The phone number.
     * @return The masked string.
     */
    public static CharSequence hideBefore(final CharSequence phone) {
        return StringKit.hide(phone, 0, 7);
    }

    /**
     * Hides the middle 4 digits of a phone number with '*'.
     *
     * @param phone The phone number.
     * @return The masked string.
     */
    public static CharSequence hideBetween(final CharSequence phone) {
        return StringKit.hide(phone, 3, 7);
    }

    /**
     * Hides the last 4 digits of a phone number with '*'.
     *
     * @param phone The phone number.
     * @return The masked string.
     */
    public static CharSequence hideAfter(final CharSequence phone) {
        return StringKit.hide(phone, 7, 11);
    }

    /**
     * Gets the first 3 digits of a phone number.
     *
     * @param phone The phone number.
     * @return The first 3 digits.
     */
    public static CharSequence subBefore(final CharSequence phone) {
        return StringKit.sub(phone, 0, 3);
    }

    /**
     * Gets the middle 4 digits of a phone number.
     *
     * @param phone The phone number.
     * @return The middle 4 digits.
     */
    public static CharSequence subBetween(final CharSequence phone) {
        return StringKit.sub(phone, 3, 7);
    }

    /**
     * Gets the last 4 digits of a phone number.
     *
     * @param phone The phone number.
     * @return The last 4 digits.
     */
    public static CharSequence subAfter(final CharSequence phone) {
        return StringKit.sub(phone, 7, 11);
    }

    /**
     * Gets the area code from a landline number.
     *
     * @param value The full landline number.
     * @return The area code part.
     */
    public static CharSequence subTelBefore(final CharSequence value) {
        return PatternKit.getGroup1(Pattern.TEL_PATTERN, value);
    }

    /**
     * Gets the local number part from a landline number.
     *
     * @param value The full landline number.
     * @return The local number part.
     */
    public static CharSequence subTelAfter(final CharSequence value) {
        return PatternKit.get(Pattern.TEL_PATTERN, value, 2);
    }

}
