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
package org.miaixz.bus.core.data.masking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Masking manager, used to manage all masking processors. There are three ways to use it:
 * <ul>
 * <li>Global default: Use {@link MaskingManager#getInstance()}, which comes with predefined masking methods.</li>
 * <li>Custom default: Use {@link MaskingManager#ofDefault(char)}, which allows you to customize the masking character
 * and comes with predefined masking methods.</li>
 * <li>Custom: Use the {@link #MaskingManager(Map, char)} constructor, which does not come with default rules.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskingManager {

    /**
     * An empty masking manager that does not process any data.
     */
    public static final MaskingManager EMPTY = new MaskingManager(null);

    /**
     * The map of masking handlers.
     */
    private final Map<String, MaskingHandler> handlerMap;
    /**
     * The character used for masking.
     */
    private final char maskChar;

    /**
     * Constructs a new masking manager.
     *
     * @param handlerMap The map of masking handlers. If used in a singleton, a thread-safe map should be provided.
     */
    public MaskingManager(final Map<String, MaskingHandler> handlerMap) {
        this(handlerMap, Symbol.C_STAR);
    }

    /**
     * Constructs a new masking manager.
     *
     * @param handlerMap The map of masking handlers. If used in a singleton, a thread-safe map should be provided.
     * @param maskChar   The default masking character, which is '*' by default.
     */
    public MaskingManager(final Map<String, MaskingHandler> handlerMap, final char maskChar) {
        this.handlerMap = handlerMap;
        this.maskChar = maskChar;
    }

    /**
     * Gets the singleton instance of the masking manager.
     *
     * @return The masking manager instance.
     */
    public static MaskingManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Creates a default masking manager with the given masking character and default masking rules.
     *
     * @param maskChar The masking character, which is '*' by default.
     * @return The default masking manager.
     */
    public static MaskingManager ofDefault(final char maskChar) {
        return registerDefault(maskChar);
    }

    /**
     * Registers the default masking handlers.
     *
     * @param maskChar The default masking character, which is '*' by default.
     * @return The default masking manager.
     */
    private static MaskingManager registerDefault(final char maskChar) {
        final MaskingManager manager = new MaskingManager(new ConcurrentHashMap<>(15, 1), maskChar);

        manager.register(EnumValue.Masking.USER_ID.name(), (str) -> "0");
        manager.register(EnumValue.Masking.CHINESE_NAME.name(), manager::firstMask);
        manager.register(EnumValue.Masking.CITIZENID.name(), (str) -> manager.idCardNum(str, 1, 2));
        manager.register(EnumValue.Masking.PHONE.name(), manager::fixedPhone);
        manager.register(EnumValue.Masking.MOBILE.name(), manager::mobilePhone);
        manager.register(EnumValue.Masking.ADDRESS.name(), (str) -> manager.address(str, 8));
        manager.register(EnumValue.Masking.EMAIL.name(), manager::email);
        manager.register(EnumValue.Masking.PASSWORD.name(), manager::password);
        manager.register(EnumValue.Masking.CAR_LICENSE.name(), manager::carLicense);
        manager.register(EnumValue.Masking.BANK_CARD.name(), manager::bankCard);
        manager.register(EnumValue.Masking.IPV4.name(), manager::ipv4);
        manager.register(EnumValue.Masking.IPV6.name(), manager::ipv6);
        manager.register(EnumValue.Masking.FIRST_MASK.name(), manager::firstMask);
        manager.register(EnumValue.Masking.CLEAR_TO_EMPTY.name(), (str) -> Normal.EMPTY);
        manager.register(EnumValue.Masking.CLEAR_TO_NULL.name(), (str) -> null);

        return manager;
    }

    /**
     * Registers a masking handler.
     *
     * @param type    The type of the handler.
     * @param handler The masking handler.
     * @return This masking manager.
     */
    public MaskingManager register(final String type, final MaskingHandler handler) {
        this.handlerMap.put(type, handler);
        return this;
    }

    /**
     * Masks the given value. If no handler is found for the given type, returns {@code null}.
     *
     * @param type  The type of the masking.
     * @param value The value to be masked.
     * @return The masked value.
     */
    public String masking(String type, final CharSequence value) {
        if (StringKit.isEmpty(type)) {
            type = EnumValue.Masking.CLEAR_TO_NULL.name();
        }
        final MaskingHandler handler = handlerMap.get(type);
        return null == handler ? null : handler.handle(value);
    }

    /**
     * Defines a "first_mask" rule that only displays the first character. Before masking: 123456789; After masking:
     * 1********.
     *
     * @param str The string to be masked.
     * @return The masked string.
     */
    public String firstMask(final CharSequence str) {
        if (StringKit.isBlank(str)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(str, 1, str.length(), maskChar);
    }

    /**
     * Masks an ID card number, showing the first and last few digits.
     *
     * @param idCardNum The ID card number.
     * @param front     The number of digits to keep at the beginning (from 1).
     * @param end       The number of digits to keep at the end (from 1).
     * @return The masked ID card number.
     */
    public String idCardNum(final CharSequence idCardNum, final int front, final int end) {
        // ID card number cannot be blank.
        if (StringKit.isBlank(idCardNum)) {
            return Normal.EMPTY;
        }
        // The length to be truncated cannot be greater than the ID card number length.
        if ((front + end) > idCardNum.length()) {
            return Normal.EMPTY;
        }
        // The length to be truncated cannot be less than 0.
        if (front < 0 || end < 0) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(idCardNum, front, idCardNum.length() - end, maskChar);
    }

    /**
     * Masks a fixed-line phone number, showing the first four and last two digits.
     *
     * @param num The fixed-line phone number.
     * @return The masked fixed-line phone number.
     */
    public String fixedPhone(final CharSequence num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(num, 4, num.length() - 2, maskChar);
    }

    /**
     * Masks a mobile phone number, showing the first three and last four digits, e.g., 135****2210.
     *
     * @param num The mobile phone number.
     * @return The masked mobile phone number.
     */
    public String mobilePhone(final CharSequence num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(num, 3, num.length() - 4, maskChar);
    }

    /**
     * Masks an address, showing only the district and not the detailed address, e.g., Beijing Haidian District****.
     *
     * @param address       The home address.
     * @param sensitiveSize The length of the sensitive information.
     * @return The masked home address.
     */
    public String address(final CharSequence address, final int sensitiveSize) {
        if (StringKit.isBlank(address)) {
            return Normal.EMPTY;
        }
        final int length = address.length();
        return StringKit.replaceByCodePoint(address, length - sensitiveSize, length, maskChar);
    }

    /**
     * Masks an email address, showing only the first letter of the prefix and hiding the rest with asterisks, while
     * showing the @ and the domain, e.g., d**@126.com.
     *
     * @param email The email address.
     * @return The masked email address.
     */
    public String email(final CharSequence email) {
        if (StringKit.isBlank(email)) {
            return Normal.EMPTY;
        }
        final int index = StringKit.indexOf(email, '@');
        if (index <= 1) {
            return email.toString();
        }
        return StringKit.replaceByCodePoint(email, 1, index, maskChar);
    }

    /**
     * Masks a password by replacing all characters with the defined masking character (e.g., *), e.g., ******. The
     * password length cannot be guessed, so it is fixed at 10 characters.
     *
     * @param password The password.
     * @return The masked password.
     */
    public String password(final CharSequence password) {
        if (StringKit.isBlank(password)) {
            return Normal.EMPTY;
        }
        // The password length cannot be guessed, so it is fixed at 10 characters.
        return StringKit.repeat(maskChar, 10);
    }

    /**
     * Masks a Chinese car license plate by replacing the middle part with the masking character (e.g., *). e.g.1: null
     * - "" e.g.2: "" - "" e.g.3: SuD40000 - SuD4***0 e.g.4: ShanV12345A - ShanV1****A e.g.5: JingA123 - JingA123 If the
     * car license plate is invalid, it is not processed.
     *
     * @param carLicense The full car license plate number.
     * @return The masked car license plate.
     */
    public String carLicense(CharSequence carLicense) {
        if (StringKit.isBlank(carLicense)) {
            return Normal.EMPTY;
        }
        // Regular car license plate
        if (carLicense.length() == 7) {
            carLicense = StringKit.replaceByCodePoint(carLicense, 3, 6, maskChar);
        } else if (carLicense.length() == 8) {
            // New energy car license plate
            carLicense = StringKit.replaceByCodePoint(carLicense, 3, 7, maskChar);
        }
        return carLicense.toString();
    }

    /**
     * Masks a bank card number, e.g., 1102 **** **** **** 3201.
     *
     * @param bankCardNo The bank card number.
     * @return The masked bank card number.
     */
    public String bankCard(CharSequence bankCardNo) {
        if (StringKit.isBlank(bankCardNo)) {
            return StringKit.toStringOrNull(bankCardNo);
        }
        bankCardNo = StringKit.cleanBlank(bankCardNo);
        if (bankCardNo.length() < 9) {
            return bankCardNo.toString();
        }

        final int length = bankCardNo.length();
        final int endLength = length % 4 == 0 ? 4 : length % 4;
        final int midLength = length - 4 - endLength;
        final StringBuilder buf = new StringBuilder();

        buf.append(bankCardNo, 0, 4);
        for (int i = 0; i < midLength; ++i) {
            if (i % 4 == 0) {
                buf.append(Symbol.C_SPACE);
            }
            buf.append(maskChar);
        }
        buf.append(Symbol.C_SPACE).append(bankCardNo, length - endLength, length);
        return buf.toString();
    }

    /**
     * Masks an IPv4 address, e.g., Before: 192.0.2.1; After: 192.*.*.*.
     *
     * @param ipv4 The IPv4 address.
     * @return The masked address.
     */
    public String ipv4(final CharSequence ipv4) {
        return StringKit.subBefore(ipv4, '.', false) + StringKit.repeat("." + maskChar, 3);
    }

    /**
     * Masks an IPv6 address, e.g., Before: 2001:0db8:86a3:08d3:1319:8a2e:0370:7344; After: 2001:*:*:*:*:*:*:*.
     *
     * @param ipv6 The IPv6 address.
     * @return The masked address.
     */
    public String ipv6(final CharSequence ipv6) {
        return StringKit.subBefore(ipv6, ':', false) + StringKit.repeat(":" + maskChar, 7);
    }

    /**
     * A static nested class, which is a static member inner class. The instance of this inner class is not bound to the
     * instance of the outer class, and it is loaded only when it is called, thus achieving lazy loading.
     */
    private static class SingletonHolder {

        /**
         * The static initializer is guaranteed to be thread-safe by the JVM.
         */
        private static final MaskingManager INSTANCE = registerDefault(Symbol.C_STAR);
    }

}
