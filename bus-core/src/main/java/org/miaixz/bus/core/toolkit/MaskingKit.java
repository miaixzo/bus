package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * 数据脱敏（Data Masking）工具类，对某些敏感信息（比如，身份证号、手机号、卡号、姓名、地址、邮箱等 ）屏蔽敏感数据
 * <p>支持以下类型信息的脱敏自动处理：</p>
 *
 * <ul>
 *     <li>用户ID</li>
 *     <li>中文名</li>
 *     <li>身份证</li>
 *     <li>座机号</li>
 *     <li>手机号</li>
 *     <li>地址</li>
 *     <li>电子邮件</li>
 *     <li>密码</li>
 *     <li>车牌</li>
 *     <li>银行卡号</li>
 *     <li>IPv4</li>
 *     <li>IPv6</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskingKit {

    /**
     * 脱敏，使用默认的脱敏策略
     * <pre>
     * masking("100", DesensitizedType.USER_ID)) =  "0"
     * masking("段正淳", DesensitizedType.CHINESE_NAME)) = "段**"
     * masking("51343620000320711X", DesensitizedType.ID_CARD)) = "5***************1X"
     * masking("09157518479", DesensitizedType.FIXED_PHONE)) = "0915*****79"
     * masking("18049531999", DesensitizedType.MOBILE_PHONE)) = "180****1999"
     * masking("北京市海淀区马连洼街道289号", DesensitizedType.ADDRESS)) = "北京市海淀区马********"
     * masking("duandazhi-jack@gmail.com.cn", DesensitizedType.EMAIL)) = "d*************@gmail.com.cn"
     * masking("1234567890", DesensitizedType.PASSWORD)) = "**********"
     * masking("苏D40000", DesensitizedType.CAR_LICENSE)) = "苏D4***0"
     * masking("11011111222233333256", DesensitizedType.BANK_CARD)) = "1101 **** **** **** 3256"
     * masking("192.168.1.1", DesensitizedType.IPV4)) = "192.*.*.*"
     * </pre>
     *
     * @param str         字符串
     * @param maskingType 脱敏类型;可以脱敏：用户id、中文名、身份证号、座机号、手机号、地址、电子邮件、密码
     * @return 脱敏之后的字符串
     */
    public static String masking(final CharSequence str, final MaskingType maskingType) {
        if (StringKit.isBlank(str)) {
            return Normal.EMPTY;
        }
        String newStr = String.valueOf(str);
        switch (maskingType) {
            case USER_ID:
                newStr = String.valueOf(userId());
                break;
            case CHINESE_NAME:
                newStr = name(String.valueOf(str));
                break;
            case ID_CARD:
                newStr = idCard(String.valueOf(str), 1, 2);
                break;
            case FIXED_PHONE:
                newStr = phone(String.valueOf(str));
                break;
            case MOBILE_PHONE:
                newStr = mobile(String.valueOf(str));
                break;
            case ADDRESS:
                newStr = address(String.valueOf(str), 8);
                break;
            case EMAIL:
                newStr = email(String.valueOf(str));
                break;
            case PASSWORD:
                newStr = password(String.valueOf(str));
                break;
            case CAR_LICENSE:
                newStr = carLicense(String.valueOf(str));
                break;
            case BANK_CARD:
                newStr = bankCard(String.valueOf(str));
                break;
            case IPV4:
                newStr = ipv4(String.valueOf(str));
                break;
            case IPV6:
                newStr = ipv6(String.valueOf(str));
                break;
            case FIRST_MASK:
                newStr = firstMask(String.valueOf(str));
                break;
            case CLEAR_TO_EMPTY:
                newStr = clear();
                break;
            case CLEAR_TO_NULL:
                newStr = clearToNull();
                break;
            default:
        }
        return newStr;
    }

    /**
     * 清空为空字符串
     *
     * @return 清空后的值
     */
    public static String clear() {
        return Normal.EMPTY;
    }

    /**
     * 清空为{@code null}
     *
     * @return 清空后的值(null)
     */
    public static String clearToNull() {
        return null;
    }

    /**
     * 【用户id】不对外提供userId
     *
     * @return 脱敏后的主键
     */
    public static Long userId() {
        return 0L;
    }

    /**
     * 定义了一个first_mask的规则，只显示第一个字符
     * 脱敏前：123456789；脱敏后：1********。
     *
     * @param text 字符串
     * @return 脱敏后的字符串
     */
    public static String firstMask(final String text) {
        if (StringKit.isBlank(text)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(text, 1, text.length());
    }

    /**
     * 【中文姓名】只显示第一个汉字，其他隐藏为2个星号，比如：李**
     *
     * @param fullName 姓名
     * @return 脱敏后的姓名
     */
    public static String name(final String fullName) {
        return firstMask(fullName);
    }

    /**
     * 【身份证号】前1位 和后2位
     *
     * @param idCardNo 身份证
     * @param front    保留：前面的front位数；从1开始
     * @param end      保留：后面的end位数；从1开始
     * @return 脱敏后的身份证
     */
    public static String idCard(final String idCardNo, final int front, final int end) {
        //身份证不能为空
        if (StringKit.isBlank(idCardNo)) {
            return Normal.EMPTY;
        }
        //需要截取的长度不能大于身份证号长度
        if ((front + end) > idCardNo.length()) {
            return Normal.EMPTY;
        }
        //需要截取的不能小于0
        if (front < 0 || end < 0) {
            return Normal.EMPTY;
        }
        return StringKit.hide(idCardNo, front, idCardNo.length() - end);
    }

    /**
     * 固定电话 前四位，后两位
     *
     * @param phone 固定电话
     * @return 脱敏后的固定电话
     */
    public static String phone(final String phone) {
        if (StringKit.isBlank(phone)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(phone, 4, phone.length() - 2);
    }

    /**
     * 【手机号码】前三位，后4位，其他隐藏，比如139****5500
     *
     * @param mobile 移动电话
     * @return 脱敏后的移动电话
     */
    public static String mobile(final String mobile) {
        if (StringKit.isBlank(mobile)) {
            return Normal.EMPTY;
        }
        return StringKit.hide(mobile, 3, mobile.length() - 4);
    }

    /**
     * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
     *
     * @param address       家庭住址
     * @param sensitiveSize 敏感信息长度
     * @return 脱敏后的家庭地址
     */
    public static String address(final String address, final int sensitiveSize) {
        if (StringKit.isBlank(address)) {
            return Normal.EMPTY;
        }
        final int length = address.length();
        return StringKit.hide(address, length - sensitiveSize, length);
    }

    /**
     * 【电子邮箱】邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@126.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String email(final String email) {
        if (StringKit.isBlank(email)) {
            return Normal.EMPTY;
        }
        final int index = StringKit.indexOf(email, '@');
        if (index <= 1) {
            return email;
        }
        return StringKit.hide(email, 1, index);
    }

    /**
     * 【密码】密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(final String password) {
        if (StringKit.isBlank(password)) {
            return Normal.EMPTY;
        }
        return StringKit.repeat('*', password.length());
    }

    /**
     * 【中国车牌】车牌中间用*代替
     * eg1：null       -》 ""
     * eg1：""         -》 ""
     * eg3：苏D40000   -》 苏D4***0
     * eg4：陕A12345D  -》 陕A1****D
     * eg5：京A123     -》 京A123     如果是错误的车牌，不处理
     *
     * @param plateNo 完整的车牌号
     * @return 脱敏后的车牌
     */
    public static String carLicense(String plateNo) {
        if (StringKit.isBlank(plateNo)) {
            return Normal.EMPTY;
        }
        // 普通车牌
        if (plateNo.length() == 7) {
            plateNo = StringKit.hide(plateNo, 3, 6);
        } else if (plateNo.length() == 8) {
            // 新能源车牌
            plateNo = StringKit.hide(plateNo, 3, 7);
        }
        return plateNo;
    }

    /**
     * 银行卡号脱敏
     * eg: 1230 **** **** **** 5000
     *
     * @param cardNo 银行卡号
     * @return 脱敏之后的银行卡号
     */
    public static String bankCard(String cardNo) {
        if (StringKit.isBlank(cardNo)) {
            return cardNo;
        }
        cardNo = StringKit.cleanBlank(cardNo);
        if (cardNo.length() < 9) {
            return cardNo;
        }

        final int length = cardNo.length();
        final int endLength = length % 4 == 0 ? 4 : length % 4;
        final int midLength = length - 4 - endLength;
        final StringBuilder buf = new StringBuilder();

        buf.append(cardNo, 0, 4);
        for (int i = 0; i < midLength; ++i) {
            if (i % 4 == 0) {
                buf.append(Symbol.C_SPACE);
            }
            buf.append('*');
        }
        buf.append(Symbol.C_SPACE).append(cardNo, length - endLength, length);
        return buf.toString();
    }

    /**
     * IPv4脱敏，如：脱敏前：192.168.1.1；脱敏后：192.*.*.*
     *
     * @param ipv4 IPv4地址
     * @return 脱敏后的地址
     */
    public static String ipv4(final String ipv4) {
        return StringKit.subBefore(ipv4, '.', false) + ".*.*.*";
    }

    /**
     * IPv6脱敏，如：脱敏前：2001:0db2:81a3:02d3:1359:8a2e:0120:5299；脱敏后：2001:*:*:*:*:*:*:*
     *
     * @param ipv6 IPv6地址
     * @return 脱敏后的地址
     */
    public static String ipv6(final String ipv6) {
        return StringKit.subBefore(ipv6, ':', false) + ":*:*:*:*:*:*:*";
    }

    /**
     * 支持的脱敏类型枚举
     *
     * @author dazer and neusoft and qiaomu
     */
    public enum MaskingType {
        /**
         * 用户id
         */
        USER_ID,
        /**
         * 中文名
         */
        CHINESE_NAME,
        /**
         * 身份证号
         */
        ID_CARD,
        /**
         * 座机号
         */
        FIXED_PHONE,
        /**
         * 手机号
         */
        MOBILE_PHONE,
        /**
         * 地址
         */
        ADDRESS,
        /**
         * 电子邮件
         */
        EMAIL,
        /**
         * 密码
         */
        PASSWORD,
        /**
         * 中国大陆车牌，包含普通车辆、新能源车辆
         */
        CAR_LICENSE,
        /**
         * 银行卡
         */
        BANK_CARD,
        /**
         * IPv4地址
         */
        IPV4,
        /**
         * IPv6地址
         */
        IPV6,
        /**
         * 定义了一个first_mask的规则，只显示第一个字符。
         */
        FIRST_MASK,
        /**
         * 清空为null
         */
        CLEAR_TO_NULL,
        /**
         * 清空为""
         */
        CLEAR_TO_EMPTY
    }

}
