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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.CaffeineCache;
import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.web.RequestContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * HTTP 请求、SpEL 表达式、用户信息等的便捷操作工具类。 使用 CacheX 接口实现缓存功能，支持延迟初始化和运行时替换。 优化了请求获取和上下文管理。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContextBuilder extends WebUtils {

    /**
     * 用于缓存 SpEL 表达式的 ConcurrentHashMap，提高解析性能。
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 请求头缓存实现（使用 CacheX 接口）。
     */
    private static volatile CacheX<String, Map<String, String>> HEADER_CACHE;

    /**
     * 请求参数缓存实现（使用 CacheX 接口）。
     */
    private static volatile CacheX<String, Map<String, String>> PARAMETER_CACHE;

    /**
     * JSON 请求体缓存实现（使用 CacheX 接口）。
     */
    private static volatile CacheX<String, String> JSON_BODY_CACHE;

    /**
     * 线程本地存储请求上下文。
     */
    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadKit.createThreadLocal(false);

    /**
     * 默认缓存配置 - 最大缓存条目数。
     */
    private static final long DEFAULT_CACHE_SIZE = 1000;

    /**
     * 默认缓存配置 - 过期时间（毫秒）。
     */
    private static final long DEFAULT_CACHE_EXPIRE = TimeUnit.MINUTES.toMillis(5);

    /**
     * 租户 ID 提供者实例。
     */
    public static volatile ContextProvider provider;

    /**
     * 设置用户信息提供者。
     *
     * @param provider 用户信息提供者
     */
    public static void setProvider(ContextProvider provider) {
        provider = provider;
    }

    /**
     * 初始化请求上下文。
     *
     * @param request HTTP 请求对象
     */
    public static void setRequestContext(HttpServletRequest request) {
        REQUEST_CONTEXT.set(new RequestContext(request));
    }

    /**
     * 获取当前请求上下文。
     *
     * @return 请求上下文，如果不存在则返回 null
     */
    public static RequestContext getRequestContext() {
        RequestContext context = REQUEST_CONTEXT.get();
        if (context == null) {
            // 尝试从 RequestContextHolder 获取并初始化
            HttpServletRequest request = getRequest();
            if (request != null) {
                context = new RequestContext(request);
                REQUEST_CONTEXT.set(context);
            }
        }
        return context;
    }

    /**
     * 获取当前 HTTP 请求的 HttpServletRequest 对象。
     *
     * @return HttpServletRequest 对象，如果无法获取则返回 null
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        // 确保是 ServletRequestAttributes
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取请求头缓存实例（延迟初始化）。
     *
     * @return 请求头缓存实例
     */
    public static CacheX<String, Map<String, String>> getHeaderCache() {
        CacheX<String, Map<String, String>> cache = HEADER_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = HEADER_CACHE;
                if (cache == null) {
                    cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    HEADER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 获取请求参数缓存实例（延迟初始化）。
     *
     * @return 请求参数缓存实例
     */
    public static CacheX<String, Map<String, String>> getParameterCache() {
        CacheX<String, Map<String, String>> cache = PARAMETER_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = PARAMETER_CACHE;
                if (cache == null) {
                    cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    PARAMETER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 获取 JSON 请求体缓存实例（延迟初始化）。
     *
     * @return JSON 请求体缓存实例
     */
    public static CacheX<String, String> getJsonBodyCache() {
        CacheX<String, String> cache = JSON_BODY_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = JSON_BODY_CACHE;
                if (cache == null) {
                    cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    JSON_BODY_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 设置请求头缓存实现。
     *
     * @param cache 缓存实现
     */
    public static void setHeaderCache(@NonNull CacheX<String, Map<String, String>> cache) {
        HEADER_CACHE = cache;
    }

    /**
     * 设置请求参数缓存实现。
     *
     * @param cache 缓存实现
     */
    public static void setParameterCache(@NonNull CacheX<String, Map<String, String>> cache) {
        PARAMETER_CACHE = cache;
    }

    /**
     * 设置 JSON 请求体缓存实现。
     *
     * @param cache 缓存实现
     */
    public static void setJsonBodyCache(@NonNull CacheX<String, String> cache) {
        JSON_BODY_CACHE = cache;
    }

    /**
     * 获取 SpEL 表达式对象。
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
     * 获取 HTTP 请求的所有 Header（使用请求上下文）。
     *
     * @return Header 键值对映射（大小写不敏感）
     */
    public static Map<String, String> getHeaders() {
        RequestContext context = getRequestContext();
        if (context == null) {
            return new CaseInsensitiveMap();
        }
        String requestId = context.getRequestId();
        if (requestId == null) {
            return new CaseInsensitiveMap();
        }
        // 尝试从缓存获取
        Map<String, String> headers = getHeaderCache().read(requestId);
        if (headers != null) {
            return headers;
        }
        // 缓存未命中，从请求上下文获取
        headers = context.getHeaders();
        // 存入缓存
        getHeaderCache().write(requestId, headers, DEFAULT_CACHE_EXPIRE);
        return headers;
    }

    /**
     * 获取 HTTP 请求的所有参数（使用请求上下文）。
     *
     * @return 参数键值对映射（大小写不敏感）
     */
    public static Map<String, String> getParameters() {
        RequestContext context = getRequestContext();
        if (context == null) {
            return new CaseInsensitiveMap();
        }
        String requestId = context.getRequestId();
        if (requestId == null) {
            return new CaseInsensitiveMap();
        }
        // 尝试从缓存获取
        Map<String, String> parameters = getParameterCache().read(requestId);
        if (parameters != null) {
            return parameters;
        }
        // 缓存未命中，从请求上下文获取
        parameters = context.getParameters();
        // 存入缓存
        getParameterCache().write(requestId, parameters, DEFAULT_CACHE_EXPIRE);
        return parameters;
    }

    /**
     * 从 JSON 请求体中获取指定键的值（使用请求上下文）。
     *
     * @param key 要获取的键名
     * @return 找到的值，如果未找到则返回 null
     */
    public static String getValueFromJsonBody(String key) {
        RequestContext context = getRequestContext();
        if (context == null) {
            return null;
        }
        HttpServletRequest request = context.getRequest();
        if (request == null) {
            return null;
        }
        try {
            // 检查 Content-Type 是否为 JSON 类型
            String contentType = request.getContentType();
            if (contentType == null || !contentType.startsWith(MediaType.APPLICATION_JSON)) {
                return null; // 非 JSON 请求，直接返回 null
            }
            String requestId = context.getRequestId();
            if (requestId == null) {
                return null;
            }
            // 尝试从缓存获取 JSON 请求体
            String cachedBody = getJsonBodyCache().read(requestId);
            if (cachedBody != null) {
                return extractValueFromJson(cachedBody, key);
            }
            // 缓存未命中，读取请求体
            String requestBody;
            try (var inputStream = request.getInputStream()) {
                requestBody = new String(inputStream.readAllBytes(), Charset.UTF_8);
            }
            if (StringKit.isEmpty(requestBody)) {
                return null; // 请求体为空，返回 null
            }
            // 存入缓存
            getJsonBodyCache().write(requestId, requestBody, DEFAULT_CACHE_EXPIRE);
            return extractValueFromJson(requestBody, key);
        } catch (IOException e) {
            Logger.warn("Failed to read request body: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 JSON 字符串中提取指定键的值。
     *
     * @param json JSON 字符串
     * @param key  键名
     * @return 找到的值，如果未找到则返回 null
     */
    public static String extractValueFromJson(String json, String key) {
        try {
            // 方式1：尝试解析为 Map
            Map<String, Object> jsonMap = JsonKit.toMap(json);
            if (jsonMap.containsKey(key)) {
                return StringKit.toString(jsonMap.get(key));
            }
            // 方式2：尝试直接从 JSON 字符串中获取字段值
            String value = JsonKit.toJsonString(json, key);
            if (!StringKit.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Logger.warn("Failed to parse JSON body: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从请求头中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getHeaderValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        Map<String, String> headers = getHeaders();
        return headers.get(key);
    }

    /**
     * 从请求参数（包括表单和 URL 参数）中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getParameterValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        Map<String, String> parameters = getParameters();
        return parameters.get(key);
    }

    /**
     * 从 JSON 请求体中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getJsonBodyValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return getValueFromJsonBody(key);
    }

    /**
     * 从 Cookie 中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getCookieValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        RequestContext context = getRequestContext();
        if (context == null) {
            return null;
        }
        HttpServletRequest request = context.getRequest();
        if (request == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 从路径变量中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getPathVariable(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        RequestContext context = getRequestContext();
        if (context == null) {
            return null;
        }
        HttpServletRequest request = context.getRequest();
        if (request == null) {
            return null;
        }
        // 获取路径变量
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) {
            return pathVariables.get(key);
        }
        return null;
    }

    /**
     * 从文件上传请求中获取指定键的值。
     *
     * @param key 键名
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getMultipartParameterValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        RequestContext context = getRequestContext();
        if (context == null) {
            return null;
        }
        HttpServletRequest request = context.getRequest();
        if (request == null) {
            return null;
        }
        // 检查是否是 multipart 请求
        if (!isMultipartContent(request)) {
            return null;
        }
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (key.equals(part.getName()) && part.getContentType() == null) {
                    // 这是普通表单字段，不是文件
                    try (var inputStream = part.getInputStream()) {
                        return new String(inputStream.readAllBytes(), Charset.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            Logger.warn("Failed to get multipart parameter: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从指定来源获取参数值。
     *
     * @param key    键名
     * @param source 参数来源
     * @return 找到的值，如果未找到则返回 null
     */
    @Nullable
    public static String getValue(@Nullable String key, @NonNull EnumValue.Params source) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        switch (source) {
        case HEADER:
            return getHeaderValue(key);
        case PARAMETER:
            return getParameterValue(key);
        case JSON_BODY:
            return getJsonBodyValue(key);
        case COOKIE:
            return getCookieValue(key);
        case PATH_VARIABLE:
            return getPathVariable(key);
        case MULTIPART:
            return getMultipartParameterValue(key);
        case ALL:
            // 按优先级：Header > Parameter > Path Variable > JSON Body > Cookie > Multipart > Context
            String value = getHeaderValue(key);
            if (value != null) {
                return value;
            }
            value = getParameterValue(key);
            if (value != null) {
                return value;
            }
            value = getPathVariable(key);
            if (value != null) {
                return value;
            }
            value = getJsonBodyValue(key);
            if (value != null) {
                return value;
            }
            value = getCookieValue(key);
            if (value != null) {
                return value;
            }
            return getMultipartParameterValue(key);
        default:
            return null;
        }
    }

    /**
     * 获取整型参数值。
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 整型参数值
     */
    public static int getIntValue(@Nullable String key, int defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Logger.warn("Failed to parse int value for key: {}", key);
            return defaultValue;
        }
    }

    /**
     * 获取长整型参数值。
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 长整型参数值
     */
    public static long getLongValue(@Nullable String key, long defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Logger.warn("Failed to parse long value for key: {}", key);
            return defaultValue;
        }
    }

    /**
     * 获取布尔型参数值。
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 布尔型参数值
     */
    public static boolean getBooleanValue(@Nullable String key, boolean defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 获取双精度浮点型参数值。
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 双精度浮点型参数值
     */
    public static double getDoubleValue(@Nullable String key, double defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Logger.warn("Failed to parse double value for key: {}", key);
            return defaultValue;
        }
    }

    /**
     * 获取指定类型的参数值。
     *
     * @param key   键名
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 指定类型的参数值
     */
    @Nullable
    public static <T> T getValue(@Nullable String key, @NonNull Class<T> clazz) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return null;
        }
        if (clazz == String.class) {
            return (T) value;
        } else if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(value);
        } else {
            // 尝试 JSON 转换
            try {
                return JsonKit.toPojo(value, clazz);
            } catch (Exception e) {
                Logger.warn("Failed to convert value to type: {}", clazz.getSimpleName());
                return null;
            }
        }
    }

    /**
     * 获取 JSON 对象参数值。
     *
     * @param key   键名
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 指定类型的参数值
     */
    @Nullable
    public static <T> T getJsonValue(@Nullable String key, @NonNull Class<T> clazz) {
        String value = getJsonBodyValue(key);
        if (StringKit.isEmpty(value)) {
            return null;
        }
        try {
            return JsonKit.toPojo(value, clazz);
        } catch (Exception e) {
            Logger.warn("Failed to parse JSON value for key: {}", key);
            return null;
        }
    }

    /**
     * 获取当前用户信息。
     *
     * @return Authorize 对象，如果无法获取则返回 null
     */
    public static Authorize getAuthorize() {
        try {
            // 1.如果有自定义访问授权提供者，优先使用
            if (provider != null) {
                return provider.getAuthorize();
            }

            // 2.获取用户ID（只检查请求头和线程池上下文）
            String userId = getValue("x_user_id", EnumValue.Params.HEADER);
            if (StringKit.isEmpty(userId)) {
                userId = getValue("x_user_id", EnumValue.Params.CONTEXT);
            }
            if (StringKit.isEmpty(userId)) {
                return null;
            }

            // 将 URL 解码后的用户数据转换为 Authorize 对象
            return JsonKit.toPojo(UrlDecoder.decode(userId, Charset.UTF_8), Authorize.class);
        } catch (Exception e) {
            Logger.error("Error occurred while retrieving current authorize: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取租户 ID，依次从以下来源尝试获取： 1. 当前登录用户的授权数据 2. HTTP 请求头中的 'x_tenant_id' 3. HTTP 请求参数中的 'tenant_id' 4. 请求体中的 JSON 数据（仅限非
     * GET 请求） 5. 线程池上下文变量
     *
     * 提示: 无法满足所有业务需求 如未达到预期，可重写此方法，进行扩展
     * 
     * @return 租户 ID 字符串，如果未找到或发生错误则返回 null
     */
    public static String getTenantId() {
        try {
            // 1. 如果有自定义租户 ID 提供者，优先使用
            if (provider != null) {
                return provider.getTenantId();
            }

            // 2. 首先尝试从当前登录用户的授权数据中获取租户 ID
            Authorize authorize = getAuthorize();
            if (authorize != null) {
                String tenantId = authorize.getX_tenant_id();
                if (!StringKit.isEmpty(tenantId)) {
                    return tenantId;
                }
            }

            // 3. 然后检查 'x_tenant_id' 请求头
            String tenantId = getValue("x_tenant_id", EnumValue.Params.HEADER);
            if (!StringKit.isEmpty(tenantId)) {
                return tenantId;
            }

            // 4. 接着检查 'tenant_id' 参数（包括请求参数和 JSON 请求体）
            tenantId = getValue("tenant_id", EnumValue.Params.PARAMETER);
            if (!StringKit.isEmpty(tenantId)) {
                return tenantId;
            }

            return getValue("tenant_id", EnumValue.Params.JSON_BODY);
        } catch (Exception e) {
            Logger.error("Error occurred while retrieving tenant ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查请求是否包含文件上传。
     *
     * @param request HTTP 请求
     * @return 如果是文件上传请求则返回 true
     */
    public static boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 清除请求上下文。
     */
    public static void clear() {
        REQUEST_CONTEXT.remove();
    }

    /**
     * 清除指定请求的缓存。
     *
     * @param requestId 请求 ID
     */
    public static void clear(String requestId) {
        CacheX<String, Map<String, String>> headerCache = getHeaderCache();
        if (headerCache != null) {
            headerCache.remove(requestId);
        }
        CacheX<String, Map<String, String>> parameterCache = getParameterCache();
        if (parameterCache != null) {
            parameterCache.remove(requestId);
        }
        CacheX<String, String> jsonBodyCache = getJsonBodyCache();
        if (jsonBodyCache != null) {
            jsonBodyCache.remove(requestId);
        }
    }

    /**
     * 清除所有缓存。
     */
    public static void clearAll() {
        CacheX<String, Map<String, String>> headerCache = getHeaderCache();
        if (headerCache != null) {
            headerCache.clear();
        }
        CacheX<String, Map<String, String>> parameterCache = getParameterCache();
        if (parameterCache != null) {
            parameterCache.clear();
        }
        CacheX<String, String> jsonBodyCache = getJsonBodyCache();
        if (jsonBodyCache != null) {
            jsonBodyCache.clear();
        }
    }

    /**
     * 重置所有缓存实例（用于测试或重新初始化）。
     */
    public static void reset() {
        HEADER_CACHE = null;
        PARAMETER_CACHE = null;
        JSON_BODY_CACHE = null;
    }

}