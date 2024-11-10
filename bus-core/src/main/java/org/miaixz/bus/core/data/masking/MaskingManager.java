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
package org.miaixz.bus.core.data.masking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 脱敏管理器，用于管理所有脱敏处理器，使用方式有三种：
 * <ul>
 * <li>全局默认：使用{@link MaskingManager#getInstance()}，带有预定义的脱敏方法</li>
 * <li>自定义默认：使用{@link MaskingManager#ofDefault(char)}，可以自定义脱敏字符，带有预定义的脱敏方法</li>
 * <li>自定义：使用{@link #MaskingManager(Map, char)}构造，不带有默认规则</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskingManager {

    /**
     * 空脱敏管理器，用于不处理任何数据的情况
     */
    public static final MaskingManager EMPTY = new MaskingManager(null);

    private final Map<String, MaskingHandler> handlerMap;
    private final char maskChar;

    /**
     * 构造
     *
     * @param handlerMap 脱敏处理器Map，如果应用于单例，则需要传入线程安全的Map
     */
    public MaskingManager(final Map<String, MaskingHandler> handlerMap) {
        this(handlerMap, Symbol.C_STAR);
    }

    /**
     * 构造
     *
     * @param handlerMap 脱敏处理器Map，如果应用于单例，则需要传入线程安全的Map
     * @param maskChar   默认的脱敏字符，默认为*
     */
    public MaskingManager(final Map<String, MaskingHandler> handlerMap, final char maskChar) {
        this.handlerMap = handlerMap;
        this.maskChar = maskChar;
    }

    /**
     * 获得单例的 MaskingManager
     *
     * @return MaskingManager
     */
    public static MaskingManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 创建默认的脱敏管理器，通过给定的脱敏字符，提供默认的脱敏规则
     *
     * @param maskChar 脱敏字符，默认为*
     * @return 默认的脱敏管理器
     */
    public static MaskingManager ofDefault(final char maskChar) {
        return registerDefault(maskChar);
    }

    /**
     * 默认的脱敏处理器注册
     *
     * @param maskChar 默认的脱敏字符，默认为*
     * @return 默认的脱敏处理器注册
     */
    private static MaskingManager registerDefault(final char maskChar) {
        final MaskingManager manager = new MaskingManager(new ConcurrentHashMap<>(15, 1), maskChar);

        manager.register(EnumValue.Masking.USER_ID.name(), (str) -> "0");
        manager.register(EnumValue.Masking.CHINESE_NAME.name(), manager::firstMask);
        manager.register(EnumValue.Masking.ID_CARD.name(), (str) -> manager.idCardNum(str, 1, 2));
        manager.register(EnumValue.Masking.FIXED_PHONE.name(), manager::fixedPhone);
        manager.register(EnumValue.Masking.MOBILE_PHONE.name(), manager::mobilePhone);
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
     * 注册一个脱敏处理器
     *
     * @param type    类型
     * @param handler 脱敏处理器
     * @return this
     */
    public MaskingManager register(final String type, final MaskingHandler handler) {
        this.handlerMap.put(type, handler);
        return this;
    }

    /**
     * 脱敏处理 如果没有指定的脱敏处理器，则返回{@code null}
     *
     * @param type  类型
     * @param value 待脱敏值
     * @return 脱敏后的值
     */
    public String masking(String type, final CharSequence value) {
        if (StringKit.isEmpty(type)) {
            type = EnumValue.Masking.CLEAR_TO_NULL.name();
        }
        final MaskingHandler handler = handlerMap.get(type);
        return null == handler ? null : handler.handle(value);
    }

    /**
     * 定义了一个first_mask的规则，只显示第一个字符。 脱敏前：123456789；脱敏后：1********。
     *
     * @param str 字符串
     * @return 脱敏后的字符串
     */
    public String firstMask(final CharSequence str) {
        if (StringKit.isBlank(str)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(str, 1, str.length(), maskChar);
    }

    /**
     * 【身份证号】前1位 和后2位
     *
     * @param idCardNum 身份证
     * @param front     保留：前面的front位数；从1开始
     * @param end       保留：后面的end位数；从1开始
     * @return 脱敏后的身份证
     */
    public String idCardNum(final CharSequence idCardNum, final int front, final int end) {
        // 身份证不能为空
        if (StringKit.isBlank(idCardNum)) {
            return Normal.EMPTY;
        }
        // 需要截取的长度不能大于身份证号长度
        if ((front + end) > idCardNum.length()) {
            return Normal.EMPTY;
        }
        // 需要截取的不能小于0
        if (front < 0 || end < 0) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(idCardNum, front, idCardNum.length() - end, maskChar);
    }

    /**
     * 【固定电话 前四位，后两位
     *
     * @param num 固定电话
     * @return 脱敏后的固定电话；
     */
    public String fixedPhone(final CharSequence num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(num, 4, num.length() - 2, maskChar);
    }

    /**
     * 【手机号码】前三位，后4位，其他隐藏，比如135****2210
     *
     * @param num 移动电话；
     * @return 脱敏后的移动电话；
     */
    public String mobilePhone(final CharSequence num) {
        if (StringKit.isBlank(num)) {
            return Normal.EMPTY;
        }
        return StringKit.replaceByCodePoint(num, 3, num.length() - 4, maskChar);
    }

    /**
     * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
     *
     * @param address       家庭住址
     * @param sensitiveSize 敏感信息长度
     * @return 脱敏后的家庭地址
     */
    public String address(final CharSequence address, final int sensitiveSize) {
        if (StringKit.isBlank(address)) {
            return Normal.EMPTY;
        }
        final int length = address.length();
        return StringKit.replaceByCodePoint(address, length - sensitiveSize, length, maskChar);
    }

    /**
     * 【电子邮箱】邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@126.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
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
     * 【密码】密码的全部字符都用定义的脱敏字符（如*）代替，比如：****** 密码位数不能被猜测，因此固定10位
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public String password(final CharSequence password) {
        if (StringKit.isBlank(password)) {
            return Normal.EMPTY;
        }
        // 密码位数不能被猜测，因此固定10位
        return StringKit.repeat(maskChar, 10);
    }

    /**
     * 【中国车牌】车牌中间用脱敏字符（如*)代替 eg1：null - "" eg1："" - "" eg3：苏D40000 - 苏A2***0 eg2：陕V12345A - 陕A1****D eg5：京A123 - 京A123
     * 如果是错误的车牌，不处理
     *
     * @param carLicense 完整的车牌号
     * @return 脱敏后的车牌
     */
    public String carLicense(CharSequence carLicense) {
        if (StringKit.isBlank(carLicense)) {
            return Normal.EMPTY;
        }
        // 普通车牌
        if (carLicense.length() == 7) {
            carLicense = StringKit.replaceByCodePoint(carLicense, 3, 6, maskChar);
        } else if (carLicense.length() == 8) {
            // 新能源车牌
            carLicense = StringKit.replaceByCodePoint(carLicense, 3, 7, maskChar);
        }
        return carLicense.toString();
    }

    /**
     * 银行卡号脱敏 eg: 1102 **** **** **** 3201
     *
     * @param bankCardNo 银行卡号
     * @return 脱敏之后的银行卡号
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
     * IPv4脱敏，如：脱敏前：192.0.2.1；脱敏后：192.*.*.*。
     *
     * @param ipv4 IPv4地址
     * @return 脱敏后的地址
     */
    public String ipv4(final CharSequence ipv4) {
        return StringKit.subBefore(ipv4, '.', false) + StringKit.repeat("." + maskChar, 3);
    }

    /**
     * IPv6脱敏，如：脱敏前：2001:0db8:86a3:08d3:1319:8a2e:0370:7344；脱敏后：2001:*:*:*:*:*:*:*
     *
     * @param ipv6 IPv6地址
     * @return 脱敏后的地址
     */
    public String ipv6(final CharSequence ipv6) {
        return StringKit.subBefore(ipv6, ':', false) + StringKit.repeat(":" + maskChar, 7);
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final MaskingManager INSTANCE = registerDefault(Symbol.C_STAR);
    }

}
