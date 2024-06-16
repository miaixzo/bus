/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 电话号码工具类，包括：
 * <ul>
 *     <li>手机号码</li>
 *     <li>400、800号码</li>
 *     <li>座机号码</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PhoneKit {

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public static boolean isMobile(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_PATTERN, value);
    }

    /**
     * 验证是否为手机号码（中国香港）
     *
     * @param value 手机号码
     * @return 是否为中国香港手机号码
     */
    public static boolean isMobileHk(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_HK, value);
    }

    /**
     * 验证是否为手机号码（中国台湾）
     *
     * @param value 手机号码
     * @return 是否为中国台湾手机号码
     */
    public static boolean isMobileTw(final CharSequence value) {
        return Validator.isMatchRegex(Pattern.MOBILE_TW_PATTERN, value);
    }

    /**
     * 验证是否为手机号码（中国澳门）
     *
     * @param value 手机号码
     * @return 是否为中国澳门手机号码
     */
    public static boolean isMobileMo(final CharSequence value) {
        return Validator.isMatchRegex(Regex.MOBILE_MO, value);
    }

    /**
     * 验证是否为座机号码（中国大陆）
     *
     * @param value 值
     * @return 是否为座机号码（中国大陆）
     */
    public static boolean isTel(final CharSequence value) {
        return Validator.isMatchRegex(Pattern.TEL_PATTERN, value);
    }

    /**
     * 验证是否为座机号码（中国大陆）+ 400 + 800
     *
     * @param value 值
     * @return 是否为座机号码（中国大陆）
     */
    public static boolean isTel400800(final CharSequence value) {
        return Validator.isMatchRegex(Regex.TEL_400_800, value);
    }

    /**
     * 验证是否为座机号码+手机号码（CharKit中国大陆）+ 400 + 800电话 + 手机号号码（中国香港）
     *
     * @param value 值
     * @return 是否为座机号码+手机号码（中国大陆）+手机号码（中国香港）+手机号码（中国台湾）+手机号码（中国澳门）
     */
    public static boolean isPhone(final CharSequence value) {
        return isMobile(value) || isTel400800(value) || isMobileHk(value) || isMobileTw(value) || isMobileMo(value);
    }

    /**
     * 隐藏手机号前7位  替换字符为"*"
     *
     * @param phone 手机号码
     * @return 替换后的字符串
     */
    public static CharSequence hideBefore(final CharSequence phone) {
        return StringKit.hide(phone, 0, 7);
    }

    /**
     * 隐藏手机号中间4位  替换字符为"*"
     *
     * @param phone 手机号码
     * @return 替换后的字符串
     */
    public static CharSequence hideBetween(final CharSequence phone) {
        return StringKit.hide(phone, 3, 7);
    }

    /**
     * 隐藏手机号最后4位  替换字符为"*"
     *
     * @param phone 手机号码
     * @return 替换后的字符串
     */
    public static CharSequence hideAfter(final CharSequence phone) {
        return StringKit.hide(phone, 7, 11);
    }

    /**
     * 获取手机号前3位
     *
     * @param phone 手机号码
     * @return 手机号前3位
     */
    public static CharSequence subBefore(final CharSequence phone) {
        return StringKit.sub(phone, 0, 3);
    }

    /**
     * 获取手机号中间4位
     *
     * @param phone 手机号码
     * @return 手机号中间4位
     */
    public static CharSequence subBetween(final CharSequence phone) {
        return StringKit.sub(phone, 3, 7);
    }

    /**
     * 获取手机号后4位
     *
     * @param phone 手机号码
     * @return 手机号后4位
     */
    public static CharSequence subAfter(final CharSequence phone) {
        return StringKit.sub(phone, 7, 11);
    }

    /**
     * 获取固话号码中的区号
     *
     * @param value 完整的固话号码
     * @return 固话号码的区号部分
     */
    public static CharSequence subTelBefore(final CharSequence value) {
        return PatternKit.getGroup1(Pattern.TEL_PATTERN, value);
    }

    /**
     * 获取固话号码中的号码
     *
     * @param value 完整的固话号码
     * @return 固话号码的号码部分
     */
    public static CharSequence subTelAfter(final CharSequence value) {
        return PatternKit.get(Pattern.TEL_PATTERN, value, 2);
    }

}
