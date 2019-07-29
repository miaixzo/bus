package org.aoju.bus.sensitive;

import org.aoju.bus.core.instance.Instances;

/**
 * 脱敏策略工具类
 * 1.提供常见的脱敏策略
 * 2.主要供单独的字符串处理使用
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Builder {

    /**
     * 脱敏对象
     * <p>
     * 每次都创建一个新的对象，避免线程问题
     * 可以使用 {@link ThreadLocal} 简单优化。
     *
     * @param object 原始对象
     * @param <T>    泛型
     * @return 脱敏后的对象
     * @since 0.0.4 以前用的是单例。建议使用 spring 等容器管理 Provider 实现。
     */
    public static <T> T on(Object object) {
        return (T) Instances.singletion(Provider.class).on(object, null);
    }

    /**
     * 返回脱敏后的对象 json
     * null 对象，返回字符串 "null"
     *
     * @param object 对象
     * @return 结果 json
     * @since 0.0.6
     */
    public static String json(Object object) {
        return Instances.singletion(Provider.class).json(object, null);
    }

    public enum Mode {

        /**
         * 头部
         */
        HEAD,
        /**
         * 尾部
         */
        TAIL,
        /**
         * 中间
         */
        MIDDLE

    }

    public enum Type {
        /**
         * 不脱敏
         */
        NONE,
        /**
         * 默认脱敏方式
         */
        DEFAUL,
        /**
         * 中文名
         */
        NAME,
        /**
         * 身份证号
         */
        ID_CARD,
        /**
         * 座机号
         */
        PHONE,
        /**
         * 手机号
         */
        MOBILE,
        /**
         * 地址
         */
        ADDRESS,
        /**
         * 电子邮件
         */
        EMAIL,
        /**
         * 银行卡
         */
        BANK_CARD,
        /**
         * 公司开户银行联号
         */
        CNAPS_CODE,
        /**
         * 支付签约协议号
         */
        PAY_SIGN_NO,
        /**
         * 密码
         */
        PASSWORD,
        /**
         * 普通号码
         */
        GENERIC
    }

}
