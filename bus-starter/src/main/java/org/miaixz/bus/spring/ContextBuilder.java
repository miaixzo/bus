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
package org.miaixz.bus.spring;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 请求、SpEL 表达式、用户信息等的便捷操作
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContextBuilder extends WebUtils {

    /**
     * 缓存 SpEL 表达式的 ConcurrentHashMap，用于提高解析性能
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 获取 SpEL 表达式对象
     *
     * @param expressionString SpEL 表达式字符串，例如 #{param.id}
     * @return 解析后的 Expression 对象，如果表达式为空则返回 null
     */
    @Nullable
    public static Expression getExpression(@Nullable String expressionString) {
        if (StringKit.isBlank(expressionString)) {
            return null;
        }
        // 检查缓存中是否已存在该表达式
        if (EXPRESSION_CACHE.containsKey(expressionString)) {
            return EXPRESSION_CACHE.get(expressionString);
        }
        // 解析 SpEL 表达式并存入缓存
        Expression expression = new SpelExpressionParser().parseExpression(expressionString);
        EXPRESSION_CACHE.put(expressionString, expression);
        return expression;
    }

    /**
     * 根据 SpEL 表达式从根对象中获取指定类型的值
     *
     * @param root             根对象
     * @param expressionString SpEL 表达式字符串
     * @param clazz            返回值的目标类型
     * @param <T>              泛型类型
     * @return 表达式求值结果，如果根对象或表达式为空则返回 null
     */
    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root, @Nullable String expressionString,
            @NonNull Class<? extends T> clazz) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }
        // 从根对象中求值并转换为指定类型
        return expression.getValue(root, clazz);
    }

    /**
     * 根据 SpEL 表达式从根对象中获取值（无类型指定）
     *
     * @param root             根对象
     * @param expressionString SpEL 表达式字符串
     * @param <T>              泛型类型
     * @return 表达式求值结果，如果根对象或表达式为空则返回 null
     */
    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root, @Nullable String expressionString) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }
        // 从根对象中求值，返回原始类型
        return (T) expression.getValue(root);
    }

    /**
     * 根据多个 SpEL 表达式从根对象中获取值数组
     *
     * @param root              根对象
     * @param expressionStrings SpEL 表达式字符串数组
     * @param <T>               泛型类型，建议使用 Object 以避免类型转换异常
     * @return 表达式求值结果数组，如果根对象或表达式数组为空则返回 null
     */
    public static <T> T[] getExpressionValue(@Nullable Object root, @Nullable String... expressionStrings) {
        if (root == null) {
            return null;
        }
        if (ArrayKit.isEmpty(expressionStrings)) {
            return null;
        }
        // 遍历表达式数组，逐个求值
        Object[] values = new Object[expressionStrings.length];
        for (int i = 0; i < expressionStrings.length; i++) {
            values[i] = getExpressionValue(root, expressionStrings[i]);
        }
        return (T[]) values;
    }

    /**
     * 根据 SpEL 表达式进行条件求值 如果值为 null 则返回 false；如果为 Boolean 类型则直接返回；如果为 Number 类型则判断是否大于 0
     *
     * @param root             根对象
     * @param expressionString SpEL 表达式字符串
     * @return 条件求值结果
     */
    @Nullable
    public static boolean getConditionValue(@Nullable Object root, @Nullable String expressionString) {
        // 获取 SpEL 表达式的值
        Object value = getExpressionValue(root, expressionString);
        // 如果值为 null，则返回 false
        if (value == null) {
            return false;
        }
        // 如果值为 Boolean 类型，直接返回其值
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        // 如果值为 Number 类型，判断其值是否大于 0
        if (value instanceof Number) {
            return ((Number) value).longValue() > 0;
        }
        // 其他非 null 值返回 true
        return true;
    }

    /**
     * 根据多个 SpEL 表达式进行条件求值，所有表达式结果需为 true 才返回 true
     *
     * @param root              根对象
     * @param expressionStrings SpEL 表达式字符串数组
     * @return 条件求值结果，所有表达式均为 true 时返回 true，否则返回 false
     */
    @Nullable
    public static boolean getConditionValue(@Nullable Object root, @Nullable String... expressionStrings) {
        if (root == null) {
            return false;
        }
        if (ArrayKit.isEmpty(expressionStrings)) {
            return false;
        }
        // 逐个检查表达式结果，若有一个为 false 则返回 false
        for (String expressionString : expressionStrings) {
            if (!getConditionValue(root, expressionString)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取当前 HTTP 请求的 HttpServletRequest 对象
     *
     * @return HttpServletRequest 对象，如果无法获取则返回 null
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return (requestAttributes == null) ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取当前会话的 Session ID
     *
     * @return Session ID，如果请求或会话不存在则返回 null
     */
    public static String getSessionId() {
        HttpServletRequest request = getRequest();
        if (null != request && null != request.getSession(false)) {
            return request.getSession(false).getId();
        } else {
            return null;
        }
    }

    /**
     * 获取 HTTP 请求的所有 Header
     *
     * @param request HTTP 请求对象
     * @return Header 键值对映射
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headerMap.put(name, value);
        }
        return headerMap;
    }

    /**
     * 获取 HTTP 请求的所有参数
     *
     * @param request HTTP 请求对象
     * @return 参数键值对映射
     */
    public static Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getParameter(name);
            parameterMap.put(name, value);
        }
        return parameterMap;
    }

    /**
     * 获取当前用户信息
     *
     * @return Authorize 对象，如果无法获取则返回 null
     */
    public static Authorize getCurrentUser() {
        HttpServletRequest request = getRequest();
        String user;
        if (null != request && !StringKit.isEmpty(request.getHeader("x_user_id"))) {
            user = request.getHeader("x_user_id");
        } else {
            // 当请求为空时，尝试从异步线程池的上下文变量中获取
            user = getThreadPoolContextValue("x_user_id");
        }
        if (StringKit.isEmpty(user)) {
            return null;
        }
        // 将 URL 解码后的用户数据转换为 Authorize 对象
        return JsonKit.toPojo(UrlDecoder.decode(user, Charset.UTF_8), Authorize.class);
    }

    /**
     * 获取当前租户 ID
     *
     * @return 租户 ID，如果无法获取则返回 null
     */
    public static String getTenantId() {
        HttpServletRequest request = getRequest();
        try {
            if (null != request && !StringKit.isEmpty(request.getHeader("x_tenant_id"))) {
                return request.getHeader("x_tenant_id");
            } else {
                // 从当前登录用户获取租户 ID
                Authorize authorize = getCurrentUser();
                if (null != authorize) {
                    return authorize.getX_tenant_id();
                }
                // 当都为空时，尝试从异步线程池的上下文变量中获取
                String tenantId = getThreadPoolContextValue("x_tenant_id");
                return tenantId;
            }
        } catch (Exception e) {
            Logger.error("获取当前租户失败:", e);
            return null;
        }
    }

    /**
     * 从线程池上下文中获取指定 Header 值
     *
     * @param headerKey Header 键名
     * @return Header 值，如果不存在则返回 null
     */
    private static String getThreadPoolContextValue(String headerKey) {
        Map<String, String> headers = RequestHeaderContext.get();
        if (null != headers) {
            // 使用大小写不敏感的 Map 获取 Header 值
            Map<String, String> headersMap = new CaseInsensitiveMap(headers);
            return headersMap.get(headerKey);
        }
        return null;
    }

    /**
     * 请求 Header 上下文类，用于在线程本地存储 Header 信息
     */
    static class RequestHeaderContext {

        /**
         * 线程本地变量，用于存储请求 Header 映射
         */
        public static final ThreadLocal<Map<String, String>> MAP_THREAD_LOCAL = new ThreadLocal<>();

        /**
         * 设置线程本地的 Header 映射
         *
         * @param map Header 键值对映射
         */
        public static void set(Map<String, String> map) {
            MAP_THREAD_LOCAL.set(map);
        }

        /**
         * 获取线程本地的 Header 映射
         *
         * @return Header 键值对映射
         */
        public static Map<String, String> get() {
            return MAP_THREAD_LOCAL.get();
        }

        /**
         * 移除线程本地的 Header 映射
         */
        public static void remove() {
            MAP_THREAD_LOCAL.remove();
        }
    }

}