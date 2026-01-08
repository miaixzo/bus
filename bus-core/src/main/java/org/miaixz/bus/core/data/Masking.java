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

import org.miaixz.bus.core.data.masking.MaskingManager;
import org.miaixz.bus.core.data.masking.MaskingProcessor;
import org.miaixz.bus.core.data.masking.TextMaskingRule;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Data Masking utility class for masking sensitive information (e.g., ID card number, mobile phone number, card number,
 * name, address, email, etc.).
 * <p>
 * Supports automatic masking for the following types of information:
 *
 * <ul>
 * <li>User ID</li>
 * <li>Chinese Name</li>
 * <li>ID Card</li>
 * <li>Landline Number</li>
 * <li>Mobile Phone Number</li>
 * <li>Address</li>
 * <li>Email</li>
 * <li>Password</li>
 * <li>License Plate</li>
 * <li>Bank Card Number</li>
 * <li>IPv4</li>
 * <li>IPv6</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Masking {

    /**
     * The default rich text masking processor.
     */
    private static final MaskingProcessor DEFAULT_PROCESSOR = createDefaultProcessor();

    /**
     * Masks the rich text content.
     *
     * @param text The rich text content.
     * @return The masked text.
     */
    public static String mask(final String text) {
        return DEFAULT_PROCESSOR.mask(text);
    }

    /**
     * Masks the rich text content using a custom processor.
     *
     * @param text      The rich text content.
     * @param processor The custom processor.
     * @return The masked text.
     */
    public static String mask(final String text, final MaskingProcessor processor) {
        return processor.mask(text);
    }

    /**
     * Masks the text using the default masking strategy.
     *
     * @param text    The string.
     * @param masking The masking type; can be used for: user ID, Chinese name, ID card number, landline number, mobile
     *                phone number, address, email, password.
     * @return The masked string.
     */
    public static String masking(final EnumValue.Masking masking, final CharSequence text) {
        return MaskingManager.getInstance().masking(masking.name(), text);
    }

    /**
     * Clears to an empty string.
     *
     * @return The cleared value.
     */
    public static String clear() {
        return Normal.EMPTY;
    }

    /**
     * Clears to {@code null}.
     *
     * @return The cleared value (null).
     */
    public static String clearToNull() {
        return null;
    }

    /**
     * [User ID] Do not provide userId externally.
     *
     * @return The masked primary key.
     */
    public static Long userId() {
        return 0L;
    }

    /**
     * Defines a "first_mask" rule that only displays the first character. Before masking: 123456789; After masking:
     * 1********.
     *
     * @param text The string.
     * @return The masked string.
     */
    public static String firstMask(final CharSequence text) {
        return MaskingManager.EMPTY.firstMask(text);
    }

    /**
     * [Chinese Name] Only displays the first character, hiding the rest with two asterisks, e.g., Li**.
     *
     * @param fullName The full name.
     * @return The masked name.
     */
    public static String chineseName(final CharSequence fullName) {
        return firstMask(fullName);
    }

    /**
     * [ID Card Number] Shows the first 1 and last 2 digits.
     *
     * @param idCardNum The ID card number.
     * @param front     The number of digits to keep at the beginning; starts from 1.
     * @param end       The number of digits to keep at the end; starts from 1.
     * @return The masked ID card number.
     */
    public static String idCardNum(final CharSequence idCardNum, final int front, final int end) {
        return MaskingManager.EMPTY.idCardNum(idCardNum, front, end);
    }

    /**
     * [Fixed-line Phone] Shows the first four and last two digits.
     *
     * @param num The fixed-line phone number.
     * @return The masked fixed-line phone number.
     */
    public static String fixedPhone(final CharSequence num) {
        return MaskingManager.EMPTY.fixedPhone(num);
    }

    /**
     * [Mobile Phone Number] Shows the first three and last four digits, hiding the rest, e.g., 135****3966.
     *
     * @param num The mobile phone number.
     * @return The masked mobile phone number.
     */
    public static String mobilePhone(final CharSequence num) {
        return MaskingManager.EMPTY.mobilePhone(num);
    }

    /**
     * [Address] Only displays the district, not the detailed address, e.g., Beijing Haidian District****.
     *
     * @param address       The home address.
     * @param sensitiveSize The length of the sensitive information.
     * @return The masked home address.
     */
    public static String address(final CharSequence address, final int sensitiveSize) {
        return MaskingManager.EMPTY.address(address, sensitiveSize);
    }

    /**
     * [Email] Only displays the first letter of the email prefix, hiding the rest with asterisks, while showing the @
     * and the domain, e.g., d**@qq.com.
     *
     * @param email The email address.
     * @return The masked email address.
     */
    public static String email(final CharSequence email) {
        return MaskingManager.EMPTY.email(email);
    }

    /**
     * [Password] All characters of the password are replaced with *, e.g., ******.
     *
     * @param password The password.
     * @return The masked password.
     */
    public static String password(final CharSequence password) {
        if (StringKit.isBlank(password)) {
            return Normal.EMPTY;
        }
        // The password length cannot be guessed, so it is fixed at 10 characters.
        return StringKit.repeat(Symbol.C_STAR, 10);
    }

    /**
     * [Chinese License Plate] The middle of the license plate is replaced with *. e.g.1: null - "" e.g.2: "" - ""
     * e.g.3: SuA60000 - SuA6***0 e.g.4: ShanA12345D - ShanA1****D e.g.5: JingA123 - JingA123. If it is an incorrect
     * license plate, it is not processed.
     *
     * @param carLicense The full license plate number.
     * @return The masked license plate.
     */
    public static String carLicense(final CharSequence carLicense) {
        return MaskingManager.EMPTY.carLicense(carLicense);
    }

    /**
     * Bank card number masking, e.g., 1101 **** **** **** 3256.
     *
     * @param bankCardNo The bank card number.
     * @return The masked bank card number.
     */
    public static String bankCard(final CharSequence bankCardNo) {
        return MaskingManager.EMPTY.bankCard(bankCardNo);
    }

    /**
     * IPv4 masking, e.g., Before: 192.0.2.1; After: 192.*.*.*.
     *
     * @param ipv4 The IPv4 address.
     * @return The masked address.
     */
    public static String ipv4(final CharSequence ipv4) {
        return MaskingManager.EMPTY.ipv4(ipv4);
    }

    /**
     * IPv6 masking, e.g., Before: 2001:0db8:86a3:08d3:1319:8a2e:0370:7344; After: 2001:*:*:*:*:*:*:*.
     *
     * @param ipv6 The IPv6 address.
     * @return The masked address.
     */
    public static String ipv6(final CharSequence ipv6) {
        return MaskingManager.EMPTY.ipv6(ipv6);
    }

    /**
     * Creates a new rich text masking processor.
     *
     * @param preserveHtmlTags Whether to preserve HTML tags.
     * @return The rich text masking processor.
     */
    public static MaskingProcessor createProcessor(final boolean preserveHtmlTags) {
        return new MaskingProcessor(preserveHtmlTags);
    }

    /**
     * Creates an email masking rule.
     *
     * @return The email masking rule.
     */
    public static TextMaskingRule createEmailRule() {
        return new TextMaskingRule("Email", "[\\w.-]+@[\\w.-]+\\.\\w+", EnumValue.Masking.PARTIAL, null)
                .setPreserveLeft(1).setPreserveRight(0).setMaskChar('*');
    }

    /**
     * Creates a URL masking rule.
     *
     * @param replacement The replacement text.
     * @return The URL masking rule.
     */
    public static TextMaskingRule createUrlRule(final String replacement) {
        return new TextMaskingRule("URL", "https?://[\\w.-]+(?:/[\\w.-]*)*", EnumValue.Masking.REPLACE, replacement);
    }

    /**
     * Creates a sensitive word masking rule.
     *
     * @param pattern The sensitive word regular expression.
     * @return The sensitive word masking rule.
     */
    public static TextMaskingRule createSensitiveWordRule(final String pattern) {
        return new TextMaskingRule("Sensitive Word", pattern, EnumValue.Masking.FULL, null).setMaskChar('*');
    }

    /**
     * Creates a custom masking rule.
     *
     * @param name        The name of the rule.
     * @param pattern     The matching pattern (regular expression).
     * @param masking     The masking type.
     * @param replacement The replacement content.
     * @return The custom masking rule.
     */
    public static TextMaskingRule createCustomRule(
            final String name,
            final String pattern,
            final EnumValue.Masking masking,
            final String replacement) {
        return new TextMaskingRule(name, pattern, masking, replacement);
    }

    /**
     * Creates a partial masking rule.
     *
     * @param name          The name of the rule.
     * @param pattern       The matching pattern (regular expression).
     * @param preserveLeft  The number of characters to preserve on the left.
     * @param preserveRight The number of characters to preserve on the right.
     * @param maskChar      The masking character.
     * @return The partial masking rule.
     */
    public static TextMaskingRule createPartialMaskRule(
            final String name,
            final String pattern,
            final int preserveLeft,
            final int preserveRight,
            final char maskChar) {
        return new TextMaskingRule(name, pattern, preserveLeft, preserveRight, maskChar);
    }

    /**
     * Creates the default rich text masking processor.
     *
     * @return The default rich text masking processor.
     */
    private static MaskingProcessor createDefaultProcessor() {
        final MaskingProcessor processor = new MaskingProcessor(true);

        // Add some common masking rules

        // Email masking rule
        processor.addRule(
                new TextMaskingRule("Email", "[\\w.-]+@[\\w.-]+\\.\\w+", EnumValue.Masking.PARTIAL, "[Email Hidden]")
                        .setPreserveLeft(1).setPreserveRight(0).setMaskChar('*'));

        // URL masking rule
        processor.addRule(
                new TextMaskingRule("URL", "https?://[\\w.-]+(?:/[\\w.-]*)*", EnumValue.Masking.REPLACE,
                        "[URL Hidden]"));

        // Sensitive word masking rule (example)
        processor.addRule(
                new TextMaskingRule("Sensitive Word", "(Confidential|Top Secret|Internal|Secret|Proprietary)",
                        EnumValue.Masking.FULL, "***").setMaskChar('*'));

        return processor;
    }

}
