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
package org.miaixz.bus.sensitive;

import java.lang.annotation.Annotation;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.text.dfa.Sensitive;

/**
 * 脱敏策略工具类 1.提供常见的脱敏策略 2.主要供单独的字符串处理使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder extends Sensitive {

    /**
     * 全局处理
     */
    public static final String ALL = "ALL";
    /**
     * 数据脱敏
     */
    public static final String SENS = "SENS";
    /**
     * 数据安全
     */
    public static final String SAFE = "SAFE";
    /**
     * 请求解密
     */
    public static final String IN = "IN";
    /**
     * 响应加密
     */
    public static final String OUT = "OUT";

    /**
     * 不做任何处理
     */
    public static final String NOTHING = "NOTHING";

    /**
     * 不做任何处理
     */
    public static final String OVERALL = "OVERALL";

    /**
     * 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param object 原始对象
     * @param <T>    泛型
     * @return 脱敏后的对象
     */
    public static <T> T on(Object object) {
        return on(object, null, false);
    }

    /**
     * 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param object 原始对象
     * @param clone  是否克隆
     * @param <T>    泛型
     * @return 脱敏后的对象
     */
    public static <T> T on(Object object, boolean clone) {
        return on(object, null, clone);
    }

    /**
     * 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param object     原始对象
     * @param annotation 注解信息
     * @param <T>        泛型
     * @return 脱敏后的对象
     */
    public static <T> T on(Object object, Annotation annotation) {
        return (T) Instances.singletion(Provider.class).on(object, annotation, false);
    }

    /**
     * 返回脱敏后的对象 json null 对象,返回字符串 "null"
     *
     * @param object 对象
     * @return 结果 json
     */
    public static String json(Object object) {
        return Instances.singletion(Provider.class).json(object, null);
    }

    /**
     * 每次都创建一个新的对象,避免线程问题 可以使用 {@link ThreadLocal} 简单优化
     *
     * @param object     原始对象
     * @param annotation 注解信息
     * @param clone      是否克隆
     * @param <T>        泛型
     * @return 脱敏后的对象
     */
    public static <T> T on(Object object, Annotation annotation, boolean clone) {
        return (T) Instances.singletion(Provider.class).on(object, annotation, clone);
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
        CITIZENID,
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
         * 企业银行联号
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
