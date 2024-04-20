/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.exception.ValidateException;
import org.miaixz.bus.core.toolkit.*;

import java.net.MalformedURLException;
import java.util.regex.Pattern;

/**
 * 字段验证器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Validator {

    /**
     * 给定值是否为{@code null}
     *
     * @param value 值
     * @return 是否为<code>null</code>
     */
    public static boolean isNull(Object value) {
        return null == value;
    }

    /**
     * 检查指定值是否为{@code null}
     *
     * @param <T>              被检查的对象类型
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static <T> T validateNull(T value, String errorMsgTemplate, Object... params) throws ValidateException {
        if (isNotNull(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return null;
    }

    /**
     * 给定值是否不为{@code null}
     *
     * @param value 值
     * @return 是否不为<code>null</code>
     */
    public static boolean isNotNull(Object value) {
        return null != value;
    }

    /**
     * 检查指定值是否非{@code null}
     *
     * @param <T>              被检查的对象类型
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static <T> T validateNotNull(T value, String errorMsgTemplate, Object... params) throws ValidateException {
        if (isNull(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return value;
    }

    /**
     * 验证是否为空
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     */
    public static boolean isEmpty(Object value) {
        return (null == value || (value instanceof String && StringKit.isEmpty((String) value)));
    }

    /**
     * 验证是否为空,非空时抛出异常
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param <T>      值类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值，验证通过返回此值，空值
     * @throws ValidateException 验证异常
     */
    public static <T> T validateEmpty(T value, String errorMsg) throws ValidateException {
        if (isNotEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为非空
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     */
    public static boolean isNotEmpty(Object value) {
        return false == isEmpty(value);
    }

    /**
     * 验证是否为非空，为空时抛出异常
     * 对于String类型判定是否为empty(null 或 "")
     *
     * @param <T>      值类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值，验证通过返回此值，非空值
     * @throws ValidateException 验证异常
     */
    public static <T> T validateNotEmpty(T value, String errorMsg) throws ValidateException {
        if (isEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 给定值是否为{@code true}
     *
     * @param value 值
     * @return 是否为<code>true</code>
     */
    public static boolean isTrue(boolean value) {
        return value;
    }

    /**
     * 检查指定值是否为{@code true}
     *
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static boolean validateTrue(boolean value, String errorMsgTemplate, Object... params) throws ValidateException {
        if (isFalse(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return true;
    }

    /**
     * 给定值是否不为{@code false}
     *
     * @param value 值
     * @return 是否不为<code>false</code>
     */
    public static boolean isFalse(boolean value) {
        return false == value;
    }

    /**
     * 检查指定值是否为{@code false}
     *
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static boolean validateFalse(boolean value, String errorMsgTemplate, Object... params) throws ValidateException {
        if (isTrue(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return false;
    }

    /**
     * 验证是否相等
     * 当两值都为null返回true
     *
     * @param t1 对象1
     * @param t2 对象2
     * @return 当两值都为null或相等返回true
     */
    public static boolean equal(Object t1, Object t2) {
        return ObjectKit.equals(t1, t2);
    }

    /**
     * 验证是否相等,不相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @return 相同值
     * @throws ValidateException 验证异常
     */
    public static Object validateEqual(Object t1, Object t2, String errorMsg) throws ValidateException {
        if (false == equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
        return t1;
    }

    /**
     * 验证是否不等,相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEqual(Object t1, Object t2, String errorMsg) throws ValidateException {
        if (equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 验证是否非空且与指定值相等
     * 当数据为空时抛出验证异常
     * 当两值不等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEmptyAndEqual(Object t1, Object t2, String errorMsg) throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateEqual(t1, t2, errorMsg);
    }

    /**
     * 验证是否非空且与指定值相等
     * 当数据为空时抛出验证异常
     * 当两值相等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEmptyAndNotEqual(Object t1, Object t2, String errorMsg) throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateNotEqual(t1, t2, errorMsg);
    }

    /**
     * 判断字符串是否全部为大写字母
     *
     * @param value 值
     * @return 是否全部为大写字母
     */
    public static boolean isUpperCase(CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isUpperCase);
    }

    /**
     * 验证字符串是否全部为大写字母
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUpperCase(T value, String errorMsg) throws ValidateException {
        if (false == isUpperCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 判断字符串是否全部为小写字母
     *
     * @param value 值
     * @return 是否全部为小写字母
     */
    public static boolean isLowerCase(CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLowerCase);
    }

    /**
     * 验证字符串是否全部为小写字母
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateLowerCase(T value, String errorMsg) throws ValidateException {
        if (false == isLowerCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }


    /**
     * 验证该字符串是否是字母（包括大写和小写字母）
     *
     * @param value 字符串内容
     * @return 是否是字母（包括大写和小写字母）
     */
    public static boolean isLetter(CharSequence value) {
        return isMatchRegex(RegEx.WORD_PATTERN, value);
    }

    /**
     * 验证是否为字母（包括大写和小写字母）
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateLetter(T value, String errorMsg) throws ValidateException {
        if (false == isLetter(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否由为字母汉字组成（包括大写和小写字母和汉字）
     *
     * @param value 值
     * @return 是否为字母汉字组成（包括大写和小写字母和汉字）
     */
    public static boolean isLetterWithChinese(CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLetter);
    }

    /**
     * 验证是否由为字母汉字组成（包括大写和小写字母和汉字）
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateLetterWithChinese(T value, String errorMsg) throws ValidateException {
        if (false == isLetterWithChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为数字
     *
     * @param value 字符串内容
     * @return 是否是数字
     */
    public static boolean isNumber(CharSequence value) {
        return MathKit.isNumber(value);
    }

    /**
     * 验证是否为数字
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static String validateNumber(String value, String errorMsg) throws ValidateException {
        if (false == isNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 是否包含数字
     *
     * @param value 当前字符串
     * @return boolean 是否存在数字
     */
    public static boolean hasNumber(CharSequence value) {
        return PatternKit.contains(RegEx.NUMBERS_PATTERN, value);
    }

    /**
     * 是否包含数字
     *
     * @param value 当前字符串
     * @return boolean 是否存在数字
     * @throws ValidateException 验证异常
     */
    public static String validateHasNumber(String value, String errorMsg) throws ValidateException {
        if (false == hasNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param value 值
     * @return 是否为英文字母 、数字和下划线
     */
    public static boolean isGeneral(CharSequence value) {
        return isMatchRegex(RegEx.GENERAL, value);
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(T value, String errorMsg) throws ValidateException {
        if (false == isGeneral(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度,负数自动识别为0
     * @return 是否为给定最小长度的英文字母 、数字和下划线
     */
    public static boolean isGeneral(CharSequence value, int min) {
        return isGeneral(value, min, 0);
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param min      最小长度,负数自动识别为0
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(T value, int min, String errorMsg) throws ValidateException {
        return validateGeneral(value, min, 0, errorMsg);
    }

    /**
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度,负数自动识别为0
     * @param max   最大长度,0或负数表示不限制最大长度
     * @return 是否为给定长度范围的英文字母 、数字和下划线
     */
    public static boolean isGeneral(CharSequence value, int min, int max) {
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
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param min      最小长度,负数自动识别为0
     * @param max      最大长度,0或负数表示不限制最大长度
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(T value, int min, int max, String errorMsg) throws ValidateException {
        if (false == isGeneral(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param value 值
     * @return 是否为中文字、英文字母、数字和下划线
     */
    public static boolean isGeneralWithChinese(CharSequence value) {
        return isMatchRegex(RegEx.GENERAL_WITH_CHINESE, value);
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneralWithChinese(T value, String errorMsg) throws ValidateException {
        if (false == isGeneralWithChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为货币
     *
     * @param value 值
     * @return 是否为货币
     */
    public static boolean isMoney(CharSequence value) {
        return isMatchRegex(RegEx.MONEY, value);
    }

    /**
     * 验证是否为货币
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMoney(T value, String errorMsg) throws ValidateException {
        if (false == isMoney(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;

    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value 值
     * @return 是否为邮政编码（中国）
     */
    public static boolean isZipCode(CharSequence value) {
        return isMatchRegex(RegEx.ZIP_CODE, value);
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateZipCode(T value, String errorMsg) throws ValidateException {
        if (false == isZipCode(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为可用邮箱地址
     *
     * @param value 值
     * @return true为可用邮箱地址
     */
    public static boolean isEmail(CharSequence value) {
        return isMatchRegex(RegEx.EMAIL, value);
    }

    /**
     * 验证是否为可用邮箱地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateEmail(T value, String errorMsg) throws ValidateException {
        if (false == isEmail(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为座机号码（中国）
     *
     * @param value 值
     * @return 是否为座机号码（中国）
     */
    public static boolean isPhone(CharSequence value) {
        return PhoneKit.isPhone(value);
    }

    /**
     * 验证是否为座机号码（中国）
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validatePhone(T value, String errorMsg) throws ValidateException {
        if (false == isPhone(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }


    /**
     * 验证是否为手机号码（中国大陆）
     *
     * @param value 值
     * @return 是否为手机号码（中国大陆）
     */
    public static boolean isMobile(CharSequence value) {
        return isMatchRegex(RegEx.MOBILE, value);
    }

    /**
     * 验证是否为手机号码（中国大陆）
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMobile(T value, String errorMsg) throws ValidateException {
        if (false == isMobile(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为身份证号码（支持18位/15位和港澳台的10位）
     *
     * @param value 身份证号，支持18位/15位和港澳台的10位
     * @return 是否为有效身份证号码
     */
    public static boolean isCitizenId(CharSequence value) {
        return CitizenIdKit.isValidCard(String.valueOf(value));
    }

    /**
     * 验证是否为身份证号码（支持18位/15位和港澳台的10位）
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCitizenId(T value, String errorMsg) throws ValidateException {
        if (false == isCitizenId(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为生日
     * 只支持以下几种格式：
     * <ul>
     * <li>yyyyMMdd</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>yyyy年MM月dd日</li>
     * </ul>
     *
     * @param value 值
     * @return 是否为生日
     */
    public static boolean isBirthday(CharSequence value) {
        return DateKit.isBirthday(value);
    }

    /**
     * 验证验证是否为生日
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateBirthday(T value, String errorMsg) throws ValidateException {
        if (false == isBirthday(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param value 值
     * @return 是否为IPV4地址
     */
    public static boolean isIpv4(CharSequence value) {
        return isMatchRegex(RegEx.IPV4, value);
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateIpv4(T value, String errorMsg) throws ValidateException {
        if (false == isIpv4(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为IPV6地址
     *
     * @param value 值
     * @return 是否为IPV6地址
     */
    public static boolean isIpv6(CharSequence value) {
        return isMatchRegex(RegEx.IPV6, value);
    }

    /**
     * 验证是否为IPV6地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateIpv6(T value, String errorMsg) throws ValidateException {
        if (false == isIpv6(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为MAC地址
     *
     * @param value 值
     * @return 是否为MAC地址
     */
    public static boolean isMac(CharSequence value) {
        return isMatchRegex(RegEx.MAC_ADDRESS_PATTERN, value);
    }

    /**
     * 验证是否为MAC地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMac(T value, String errorMsg) throws ValidateException {
        if (false == isMac(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param value 值
     * @return 是否为中国车牌号
     */
    public static boolean isPlateNumber(CharSequence value) {
        return isMatchRegex(RegEx.PLATE_NUMBER, value);
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validatePlateNumber(T value, String errorMsg) throws ValidateException {
        if (false == isPlateNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为URL
     *
     * @param value 值
     * @return 是否为URL
     */
    public static boolean isUrl(CharSequence value) {
        if (StringKit.isBlank(value)) {
            return false;
        }
        try {
            new java.net.URL(StringKit.toString(value));
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否为URL
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUrl(T value, String errorMsg) throws ValidateException {
        if (false == isUrl(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否都为汉字
     *
     * @param value 值
     * @return 是否为汉字
     */
    public static boolean isChinese(CharSequence value) {
        return isMatchRegex(RegEx.CHINESES_PATTERN, value);
    }

    /**
     * 验证是否都为汉字
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateChinese(T value, String errorMsg) throws ValidateException {
        if (false == isChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否包含汉字
     *
     * @param value 值
     * @return 是否包含汉字
     */
    public static boolean hasChinese(CharSequence value) {
        return PatternKit.contains(RegEx.CHINESES_PATTERN, value);
    }

    /**
     * 验证是否都为汉字
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateHasChinese(T value, String errorMsg) throws ValidateException {
        if (false == hasChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为UUID
     * 包括带横线标准格式和不带横线的简单模式
     *
     * @param value 值
     * @return 是否为UUID
     */
    public static boolean isUUID(CharSequence value) {
        return isMatchRegex(RegEx.UUID, value) || isMatchRegex(RegEx.UUID_SIMPLE, value);
    }

    /**
     * 验证是否为UUID
     * 包括带横线标准格式和不带横线的简单模式
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUUID(T value, String errorMsg) throws ValidateException {
        if (false == isUUID(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为Hex（16进制）字符串
     *
     * @param value 值
     * @return 是否为Hex（16进制）字符串
     */
    public static boolean isHex(CharSequence value) {
        return isMatchRegex(RegEx.HEX_PATTERN, value);
    }

    /**
     * 验证是否为Hex（16进制）字符串
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateHex(T value, String errorMsg) throws ValidateException {
        if (false == isHex(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value 值
     * @param min   最小值(包含)
     * @param max   最大值(包含)
     * @return 是否满足
     */
    public static boolean isBetween(Number value, Number min, Number max) {
        Assert.notNull(value);
        Assert.notNull(min);
        Assert.notNull(max);
        final double doubleValue = value.doubleValue();
        return (doubleValue >= min.doubleValue()) && (doubleValue <= max.doubleValue());
    }

    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value    值
     * @param min      最小值(包含)
     * @param max      最大值(包含)
     * @param errorMsg 验证错误的信息
     * @throws ValidateException 验证异常
     */
    public static void validateBetween(Number value, Number min, Number max, String errorMsg) throws ValidateException {
        if (false == isBetween(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 通过正则表达式验证
     *
     * @param regex 正则
     * @param value 值
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(String regex, CharSequence value) {
        return PatternKit.isMatch(regex, value);
    }

    /**
     * 通过正则表达式验证
     *
     * @param pattern 正则模式
     * @param value   值
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(Pattern pattern, CharSequence value) {
        return PatternKit.isMatch(pattern, value);
    }

    /**
     * 通过正则表达式验证
     * 不符合正则抛出{@link ValidateException} 异常
     *
     * @param <T>      字符串类型
     * @param regex    正则
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMatchRegex(String regex, T value, String errorMsg) throws ValidateException {
        if (false == isMatchRegex(regex, value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 是否是有效的统一社会信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     *
     * @param value 统一社会信用代码
     * @return 校验结果
     */
    public static boolean isCreditCode(CharSequence value) {
        return LicenseKit.isCreditCode(value);
    }

    /**
     * 是否是有效的统一社会信用代码
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     *
     * @param value 统一社会信用代码
     * @return 校验结果
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCreditCode(T value, String errorMsg) throws ValidateException {
        if (false == isCreditCode(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为车架号；别名：行驶证编号 车辆识别代号 车辆识别码
     *
     * @param value 值，17位车架号；形如：LSJA24U62JG269225、LDC613P23A1305189
     * @return 是否为车架号
     */
    public static boolean isCarVin(CharSequence value) {
        return isMatchRegex(RegEx.CAR_VIN, value);
    }

    /**
     * 验证是否为车架号；别名：行驶证编号 车辆识别代号 车辆识别码
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCarVin(T value, String errorMsg) throws ValidateException {
        if (false == isCarVin(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为驾驶证  别名：驾驶证档案编号、行驶证编号
     * 仅限：中国驾驶证档案编号
     *
     * @param value 值，12位数字字符串,eg:430101758218
     * @return 是否为档案编号
     */
    public static boolean isCarDrivingLicence(CharSequence value) {
        return isMatchRegex(RegEx.CAR_DRIVING_LICENCE, value);
    }

    /**
     * 验证是否为驾驶证  别名：驾驶证档案编号、行驶证编号
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCarDrivingLicence(T value, String errorMsg) throws ValidateException {
        if (false == isCarDrivingLicence(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否符合密码要求
     *
     * @param value 值
     * @param weak  是否弱密码
     * @return 否符合密码要求
     */
    public static boolean isPassword(String value, boolean... weak) {
        boolean result = false;
        for (final boolean element : weak) {
            result ^= element;
        }
        return result ? isMatchRegex(RegEx.PASSWORD_WEAK, value) : isMatchRegex(RegEx.PASSWORD_STRONG, value);
    }

    /**
     * 验证是是否符合密码要求
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @param weak     是否弱密码
     * @throws ValidateException 验证异常
     */
    public static void validatePassword(String value, String errorMsg, boolean... weak) throws ValidateException {
        if (false == isPassword(value, weak)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 是否是中文姓名
     * 维吾尔族姓名里面的点是·
     * 正确维吾尔族姓名：
     * <pre>
     * 霍加阿卜杜拉·麦提喀斯木
     * 玛合萨提别克·哈斯木别克
     * 阿布都热依木江·艾斯卡尔
     * 阿卜杜尼亚孜·毛力尼亚孜
     * </pre>
     * 总结中文姓名：2-60位，只能是中文和·
     *
     * @param value 中文姓名
     * @return 是否是正确的中文姓名
     */
    public static boolean isChineseName(CharSequence value) {
        return isMatchRegex(RegEx.CHINESE_NAME_PATTERN, value);
    }

    /**
     * 验证是是否符合密码要求
     *
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @throws ValidateException 验证异常
     */
    public static void validateisChineseName(String value, String errorMsg) throws ValidateException {
        if (false == isChineseName(value)) {
            throw new ValidateException(errorMsg);
        }
    }

}
