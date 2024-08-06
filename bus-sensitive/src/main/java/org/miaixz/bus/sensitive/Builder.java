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
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.dfa.WordTree;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * 脱敏策略工具类 1.提供常见的脱敏策略 2.主要供单独的字符串处理使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Builder {

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
    private static final WordTree sensitiveTree = new WordTree();

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

    /**
     * @return 是否已经被初始化
     */
    public static boolean isInited() {
        return !sensitiveTree.isEmpty();
    }

    /**
     * 初始化敏感词树
     *
     * @param isAsync        是否异步初始化
     * @param sensitiveWords 敏感词列表
     */
    public static void init(final Collection<String> sensitiveWords, boolean isAsync) {
        if (isAsync) {
            ThreadKit.execAsync(() -> {
                init(sensitiveWords);
                return true;
            });
        } else {
            init(sensitiveWords);
        }
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表
     */
    public static void init(Collection<String> sensitiveWords) {
        sensitiveTree.clear();
        sensitiveTree.addWords(sensitiveWords);
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync        是否异步初始化
     * @param separator      分隔符
     */
    public static void init(String sensitiveWords, String separator, boolean isAsync) {
        if (StringKit.isNotBlank(sensitiveWords)) {
            init(StringKit.split(sensitiveWords, separator), isAsync);
        }
    }

    /**
     * 初始化敏感词树，使用逗号分隔每个单词
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync        是否异步初始化
     */
    public static void init(String sensitiveWords, boolean isAsync) {
        init(sensitiveWords, Symbol.COMMA, isAsync);
    }

    /**
     * 设置字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符 当accept为false时，此字符不参与匹配
     *
     * @param charFilter 过滤函数
     */
    public static void setCharFilter(Predicate<Character> charFilter) {
        if (null != charFilter) {
            sensitiveTree.setCharFilter(charFilter);
        }
    }

    /**
     * 是否包含敏感词
     *
     * @param text 文本
     * @return 是否包含
     */
    public static boolean containsSensitive(String text) {
        return sensitiveTree.isMatch(text);
    }

    /**
     * 是否包含敏感词
     *
     * @param object bean，会被转为JSON字符串
     * @return 是否包含
     */
    public static boolean containsSensitive(Object object) {
        return sensitiveTree.isMatch(JsonKit.toJsonString(object));
    }

    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static String getFindedFirstSensitive(String text) {
        return sensitiveTree.match(text);
    }

    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param object bean，会被转为JSON字符串
     * @return 敏感词
     */
    public static String getFindedFirstSensitive(Object object) {
        return sensitiveTree.match(JsonKit.toJsonString(object));
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static List<String> getFindedAllSensitive(String text) {
        return sensitiveTree.matchAll(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab] 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           文本
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public static List<String> getFindedAllSensitive(String text, boolean isDensityMatch, boolean isGreedMatch) {
        return sensitiveTree.matchAll(text, -1, isDensityMatch, isGreedMatch);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param bean 对象，会被转为JSON
     * @return 敏感词
     */
    public static List<String> getFindedAllSensitive(Object bean) {
        return sensitiveTree.matchAll(JsonKit.toJsonString(bean));
    }

    /**
     * 查找敏感词，返回找到的所有敏感词 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab] 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param bean           对象，会被转为JSON
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public static List<String> getFindedAllSensitive(Object bean, boolean isDensityMatch, boolean isGreedMatch) {
        return getFindedAllSensitive(JsonKit.toJsonString(bean), isDensityMatch, isGreedMatch);
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
