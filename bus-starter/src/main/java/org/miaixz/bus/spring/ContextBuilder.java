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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.CaffeineCache;
import org.miaixz.bus.cache.metric.MemoryCache;
import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.data.id.ID;
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
import org.miaixz.bus.spring.http.MutableRequestWrapper;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * HTTP 请求、用户信息等的便捷操作工具类
 * <p>
 * 使用 CacheX 接口实现缓存功能，支持延迟初始化和运行时替换。优化了请求获取和上下文管理，提供从多种来源获取参数的功能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContextBuilder extends WebUtils {

    /**
     * 请求头缓存实现
     */
    private static volatile CacheX<String, Map<String, String>> HEADER_CACHE;

    /**
     * 请求参数缓存实现
     */
    private static volatile CacheX<String, Map<String, String>> PARAMETER_CACHE;

    /**
     * 请求体缓存实现
     */
    private static volatile CacheX<String, String> BODY_CACHE;

    /**
     * 线程本地存储请求 ID
     */
    private static final ThreadLocal<String> REQUEST_ID = ThreadKit.createThreadLocal(false);

    /**
     * 默认缓存最大条目数
     */
    private static final long DEFAULT_CACHE_SIZE = 1000;

    /**
     * 默认缓存过期时间（毫秒）
     */
    private static final long DEFAULT_CACHE_EXPIRE = TimeUnit.SECONDS.toMillis(10);

    /**
     * 租户 ID 提供者实例
     */
    public static volatile ContextProvider provider;

    /**
     * 设置用户信息提供者
     * <p>
     * 配置自定义用户信息提供者，用于获取用户授权信息。
     * </p>
     *
     * <pre>{@code
     * ContextProvider customProvider = new CustomContextProvider();
     * ContextBuilder.setProvider(customProvider);
     * }</pre>
     *
     * @param provider 用户信息提供者
     */
    public static void setProvider(ContextProvider provider) {
        ContextBuilder.provider = provider;
    }

    /**
     * 初始化请求上下文
     * <p>
     * 为当前请求生成新的请求 ID 并存储在 ThreadLocal 中。
     * </p>
     *
     * <pre>{@code
     * HttpServletRequest request = getRequest();
     * ContextBuilder.setRequestId();
     * }</pre>
     */
    public static void setRequestId() {
        String requestId = ID.objectId();
        REQUEST_ID.set(requestId);
    }

    /**
     * 获取当前请求 ID
     * <p>
     * 返回当前线程的请求 ID，如果不存在则生成新的 ID。
     * </p>
     *
     * <pre>{@code
     * String requestId = ContextBuilder.getRequestId();
     * if (requestId != null) {
     *     System.out.println("请求ID: " + requestId);
     * }
     * }</pre>
     *
     * @return 请求 ID，或 null 如果无法获取
     */
    public static String getRequestId() {
        String requestId = REQUEST_ID.get();
        if (requestId == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                requestId = ID.objectId();
                REQUEST_ID.set(requestId);
                Logger.debug("==> Request ID: {}", requestId);
            } else {
                Logger.debug("==> Request ID: No request available to generate request ID");
            }
        }
        return requestId;
    }

    /**
     * 获取当前 HTTP 请求对象
     * <p>
     * 从 RequestContextHolder 获取当前 HTTP 请求的 ServletRequestAttributes。
     * </p>
     *
     * <pre>{@code
     * HttpServletRequest request = ContextBuilder.getRequest();
     * if (request != null) {
     *     System.out.println("请求URI: " + request.getRequestURI());
     * }
     * }</pre>
     *
     * @return HttpServletRequest 对象，或 null 如果无法获取
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || !(requestAttributes instanceof ServletRequestAttributes)) {
            Logger.debug("No ServletRequestAttributes available");
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取请求头缓存实例
     * <p>
     * 返回延迟初始化的请求头缓存实例，优先使用 CaffeineCache，失败时回退到 MemoryCache。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, Map<String, String>> cache = ContextBuilder.getHeaderCache();
     * Map<String, String> headers = cache.read("request123");
     * }</pre>
     *
     * @return 请求头缓存实例
     */
    public static CacheX<String, Map<String, String>> getHeaderCache() {
        CacheX<String, Map<String, String>> cache = HEADER_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = HEADER_CACHE;
                if (cache == null) {
                    try {
                        cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    } catch (Throwable t) {
                        Logger.warn(
                                "==>      Cache: Header cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    HEADER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 获取请求参数缓存实例
     * <p>
     * 返回延迟初始化的请求参数缓存实例，优先使用 CaffeineCache，失败时回退到 MemoryCache。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, Map<String, String>> cache = ContextBuilder.getParameterCache();
     * Map<String, String> params = cache.read("request123");
     * }</pre>
     *
     * @return 请求参数缓存实例
     */
    public static CacheX<String, Map<String, String>> getParameterCache() {
        CacheX<String, Map<String, String>> cache = PARAMETER_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = PARAMETER_CACHE;
                if (cache == null) {
                    try {
                        cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    } catch (Throwable t) {
                        Logger.warn(
                                "==>      Cache: Parameter cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    PARAMETER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 获取 JSON 请求体缓存实例
     * <p>
     * 返回延迟初始化的 JSON 请求体缓存实例，优先使用 CaffeineCache，失败时回退到 MemoryCache。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, String> cache = ContextBuilder.getBodyCache();
     * String jsonBody = cache.read("request123");
     * }</pre>
     *
     * @return JSON 请求体缓存实例
     */
    public static CacheX<String, String> getBodyCache() {
        CacheX<String, String> cache = BODY_CACHE;
        if (cache == null) {
            synchronized (ContextBuilder.class) {
                cache = BODY_CACHE;
                if (cache == null) {
                    try {
                        cache = new CaffeineCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    } catch (Throwable t) {
                        Logger.warn(
                                "==>      Cache: Body cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    BODY_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 设置请求头缓存实现
     * <p>
     * 配置自定义的请求头缓存实现。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, Map<String, String>> customCache = new CustomCacheImpl<>();
     * ContextBuilder.setHeaderCache(customCache);
     * }</pre>
     *
     * @param cache 缓存实现
     */
    public static void setHeaderCache(@NonNull CacheX<String, Map<String, String>> cache) {
        HEADER_CACHE = cache;
    }

    /**
     * 设置请求参数缓存实现
     * <p>
     * 配置自定义的请求参数缓存实现。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, Map<String, String>> customCache = new CustomCacheImpl<>();
     * ContextBuilder.setParameterCache(customCache);
     * }</pre>
     *
     * @param cache 缓存实现
     */
    public static void setParameterCache(@NonNull CacheX<String, Map<String, String>> cache) {
        PARAMETER_CACHE = cache;
    }

    /**
     * 设置 JSON 请求体缓存实现
     * <p>
     * 配置自定义作为 JSON 请求体缓存实现。
     * </p>
     *
     * <pre>{@code
     * CacheX<String, String> customCache = new CustomCacheImpl<>();
     * ContextBuilder.setBodyCache(customCache);
     * }</pre>
     *
     * @param cache 缓存实现
     */
    public static void setBodyCache(@NonNull CacheX<String, String> cache) {
        BODY_CACHE = cache;
    }

    /**
     * 通用缓存数据获取方法
     * <p>
     * 从缓存中读取数据，如果未命中则从供应商获取并写入缓存。
     * </p>
     *
     * @param <T>          数据类型
     * @param requestId    请求 ID
     * @param dataSupplier 数据供应商
     * @param cache        缓存实例
     * @param defaultValue 默认值
     * @return 缓存数据或默认值
     */
    private static <T> T getCached(String requestId, SupplierX<T> dataSupplier, CacheX<String, T> cache,
            T defaultValue) {
        if (requestId == null) {
            return defaultValue;
        }
        T data = cache.read(requestId);
        if (data != null) {
            return data;
        }
        data = dataSupplier.get();
        if (data != null) {
            cache.write(requestId, data, DEFAULT_CACHE_EXPIRE);
        }
        return data != null ? data : defaultValue;
    }

    /**
     * 获取 HTTP 请求的所有 Header
     * <p>
     * 从请求中获取所有请求头，优先从缓存读取，缓存未命中时从请求中获取并存入缓存。
     * </p>
     *
     * <pre>{@code
     * Map<String, String> headers = ContextBuilder.getHeaders();
     * System.out.println("请求头: " + headers);
     * }</pre>
     *
     * @return 请求头键值对映射（大小写不敏感）
     */
    public static Map<String, String> getHeaders() {
        String requestId = getRequestId();
        if (requestId == null) {
            return new CaseInsensitiveMap<>();
        }
        return getCached(requestId, () -> {
            HttpServletRequest request = getRequest();
            Map<String, String> headers = new CaseInsensitiveMap<>();
            if (request != null) {
                request.getHeaderNames().asIterator().forEachRemaining(name -> {
                    String value = request.getHeader(name);
                    if (value != null) {
                        headers.put(name, value);
                    }
                });
            }
            return headers;
        }, getHeaderCache(), new CaseInsensitiveMap<>());
    }

    /**
     * 获取 HTTP 请求的所有参数
     * <p>
     * 从请求中获取所有请求参数，优先从缓存读取，缓存未命中时从请求中获取并存入缓存。 对于 MutableRequestWrapper，如果 body 包含 URL-encoded 数据，解析并合并到参数中。
     * </p>
     *
     * <pre>{@code
     * Map<String, String> params = ContextBuilder.getParameters();
     * System.out.println("请求参数: " + params);
     * }</pre>
     *
     * @return 请求参数键值对映射（大小写不敏感）
     */
    public static Map<String, String> getParameters() {
        String requestId = getRequestId();
        if (requestId == null) {
            return new CaseInsensitiveMap<>();
        }
        return getCached(requestId, () -> {
            HttpServletRequest request = getRequest();
            Map<String, String> parameters = new CaseInsensitiveMap<>();
            if (request != null) {
                // 添加 getParameterMap 中的参数
                request.getParameterMap().forEach((key, values) -> {
                    if (values != null && values.length > 0) {
                        parameters.put(key, values[0]);
                    }
                });
                // 如果是 MutableRequestWrapper 且 Content-Type 为 form-urlencoded，解析 body
                if (request instanceof MutableRequestWrapper wrapper) {
                    String contentType = request.getContentType();
                    byte[] bodyBytes = wrapper.getBody();
                    if (contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED)
                            && bodyBytes != null && bodyBytes.length > 0) {
                        String bodyString = new String(bodyBytes, Charset.UTF_8);
                        Map<String, String[]> urlEncodedParams = parseUrlEncoded(bodyString);
                        urlEncodedParams.forEach((key, values) -> {
                            if (values != null && values.length > 0) {
                                parameters.put(key, values[0]);
                            }
                        });
                    }
                }
            }
            return parameters;
        }, getParameterCache(), new CaseInsensitiveMap<>());
    }

    /**
     * 解析 URL-encoded 字符串为参数映射
     *
     * @param urlEncoded URL-encoded 字符串 (e.g., "id=1&name=test")
     * @return 参数映射，键为参数名，值为参数值数组
     */
    private static Map<String, String[]> parseUrlEncoded(String urlEncoded) {
        Map<String, String[]> paramMap = new HashMap<>();
        if (StringKit.isEmpty(urlEncoded)) {
            return paramMap;
        }
        String[] pairs = urlEncoded.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String[] values = keyValue[1].split(",");
                paramMap.put(key, values);
            } else if (keyValue.length == 1) {
                paramMap.put(keyValue[0], new String[] { "" });
            }
        }
        return paramMap;
    }

    /**
     * 从 JSON 请求体中获取指定键的值
     * <p>
     * 从请求中获取 JSON 请求体，优先从 MutableRequestWrapper 的 body 或缓存读取，缓存未命中时从请求中读取并存入缓存。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getValueFromJsonBody("userId");
     * if (value != null) {
     *     System.out.println("JSON 值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return JSON 请求体中指定键的值，或 null 如果未找到或非 JSON 请求
     */
    public static String getValueFromJsonBody(String key) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug("No request available for JSON body lookup, key: {}", key);
            return null;
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith(MediaType.APPLICATION_JSON)) {
            Logger.debug("Request is not JSON content, key: {}, contentType: {}", key, contentType);
            return null;
        }
        String requestId = getRequestId();
        if (requestId == null) {
            Logger.debug("No request ID available for JSON body lookup, key: {}", key);
            return null;
        }
        String cachedBody = getBodyCache().read(requestId);
        if (cachedBody != null) {
            return extractValueFromJson(cachedBody, key);
        }
        String requestBody;
        try {
            if (request instanceof MutableRequestWrapper wrapper) {
                byte[] bodyBytes = wrapper.getBody();
                requestBody = bodyBytes != null && bodyBytes.length > 0 ? new String(bodyBytes, Charset.UTF_8) : "";
            } else {
                try (var inputStream = request.getInputStream()) {
                    requestBody = new String(inputStream.readAllBytes(), Charset.UTF_8);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to read JSON body, key: {}", key, e);
            return null;
        }
        if (StringKit.isEmpty(requestBody) || !JsonKit.isJson(requestBody)) {
            Logger.debug("Empty or invalid JSON body, key: {}", key);
            return null;
        }
        getBodyCache().write(requestId, requestBody, DEFAULT_CACHE_EXPIRE);
        return extractValueFromJson(requestBody, key);
    }

    /**
     * 从 JSON 字符串中提取指定键的值
     * <p>
     * 尝试解析 JSON 字符串并提取指定键的值，支持 Map 解析或直接字段提取。
     * </p>
     *
     * <pre>{@code
     * String json = "{\"userId\": \"123\"}";
     * String value = ContextBuilder.extractValueFromJson(json, "userId");
     * System.out.println("提取值: " + value);
     * }</pre>
     *
     * @param json JSON 字符串
     * @param key  键名
     * @return 提取的值，或 null 如果未找到
     */
    public static String extractValueFromJson(String json, String key) {
        try {
            String value = JsonKit.getValue(json, key);
            if (StringKit.isNotEmpty(value)) {
                return value;
            }
            Map<String, Object> jsonMap = JsonKit.toMap(json);
            if (jsonMap.containsKey(key)) {
                return StringKit.toString(jsonMap.get(key));
            }
        } catch (Exception e) {
            Logger.error("Failed to extract JSON value, key: {}, json: {}", key, json, e);
            return null;
        }
        return null;
    }

    /**
     * 从请求头中获取指定键的值
     * <p>
     * 从请求头映射中获取指定键的值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getHeaderValue("x-user-id");
     * if (value != null) {
     *     System.out.println("请求头值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return 请求头中指定键的值，或 null 如果未找到
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
     * 从请求参数中获取指定键的值
     * <p>
     * 从请求参数映射中获取指定键的值，包括表单和 URL 参数。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getParameterValue("userId");
     * if (value != null) {
     *     System.out.println("参数值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return 请求参数中指定键的值，或 null 如果未找到
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
     * 从 JSON 请求体中获取指定键的值
     * <p>
     * 从 JSON 请求体中提取指定键的值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getJsonBodyValue("userId");
     * if (value != null) {
     *     System.out.println("JSON 值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return JSON 请求体中指定键的值，或 null 如果未找到
     */
    @Nullable
    public static String getJsonBodyValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return getValueFromJsonBody(key);
    }

    /**
     * 从 Cookie 中获取指定键的值
     * <p>
     * 从 HTTP 请求的 Cookie 中获取指定键的值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getCookieValue("sessionId");
     * if (value != null) {
     *     System.out.println("Cookie 值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return Cookie 中指定键的值，或 null 如果未找到
     */
    @Nullable
    public static String getCookieValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug("No request available for cookie lookup, key: {}", key);
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            Logger.debug("No cookies found, key: {}", key);
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
     * 从路径变量中获取指定键的值
     * <p>
     * 从 HTTP 请求的路径变量中获取指定键的值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getPathVariable("userId");
     * if (value != null) {
     *     System.out.println("路径变量值: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return 路径变量中指定键的值，或 null 如果未找到
     */
    @Nullable
    public static String getPathVariable(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug("No request available for path variable lookup, key: {}", key);
            return null;
        }
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) {
            return pathVariables.get(key);
        }
        return null;
    }

    /**
     * 从文件上传请求中获取指定键的值
     * <p>
     * 从 multipart 请求的表单字段中获取指定键的值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getMultipartParameterValue("fileDesc");
     * if (value != null) {
     *     System.out.println("文件描述: " + value);
     * }
     * }</pre>
     *
     * @param key 键名
     * @return 文件上传请求中指定键的值，或 null 如果未找到
     */
    @Nullable
    public static String getMultipartParameterValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug("No request available for multipart lookup, key: {}", key);
            return null;
        }
        if (!isMultipartContent(request)) {
            Logger.debug("Request is not multipart, key: {}", key);
            return null;
        }
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (key.equals(part.getName()) && part.getContentType() == null) {
                    try (var inputStream = part.getInputStream()) {
                        return new String(inputStream.readAllBytes(), Charset.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to get multipart parameter, key: {}", key, e);
            return null;
        }
        return null;
    }

    /**
     * 从指定来源获取参数值
     * <p>
     * 根据指定的参数来源（如请求头、参数、JSON 请求体等）获取键值。
     * </p>
     *
     * <pre>{@code
     * String value = ContextBuilder.getValue("userId", EnumValue.Params.HEADER);
     * if (value != null) {
     *     System.out.println("值: " + value);
     * }
     * }</pre>
     *
     * @param key    键名
     * @param source 参数来源
     * @return 参数值，或 null 如果未找到
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
            String value = getHeaderValue(key);
            if (value != null)
                return value;
            value = getParameterValue(key);
            if (value != null)
                return value;
            value = getPathVariable(key);
            if (value != null)
                return value;
            value = getJsonBodyValue(key);
            if (value != null)
                return value;
            value = getCookieValue(key);
            if (value != null)
                return value;
            return getMultipartParameterValue(key);
        default:
            return null;
        }
    }

    /**
     * 获取整型参数值
     * <p>
     * 从所有来源获取指定键的整型值，失败时返回默认值。
     * </p>
     *
     * <pre>{@code
     * int value = ContextBuilder.getIntValue("age", 0);
     * System.out.println("年龄: " + value);
     * }</pre>
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
            Logger.warn("Failed to parse int value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * 获取长整型参数值
     * <p>
     * 从所有来源获取指定键的长整型值，失败时返回默认值。
     * </p>
     *
     * <pre>{@code
     * long value = ContextBuilder.getLongValue("timestamp", 0L);
     * System.out.println("时间戳: " + value);
     * }</pre>
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
            Logger.warn("Failed to parse long value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * 获取布尔型参数值
     * <p>
     * 从所有来源获取指定键的布尔型值，失败时返回默认值。
     * </p>
     *
     * <pre>{@code
     * boolean value = ContextBuilder.getBooleanValue("isActive", false);
     * System.out.println("是否激活: " + value);
     * }</pre>
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
     * 获取双精度浮点型参数值
     * <p>
     * 从所有来源获取指定键的双精度浮点值，失败时返回默认值。
     * </p>
     *
     * <pre>{@code
     * double value = ContextBuilder.getDoubleValue("price", 0.0);
     * System.out.println("价格: " + value);
     * }</pre>
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
            Logger.warn("Failed to parse double value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * 获取指定类型的参数值
     * <p>
     * 从所有来源获取指定键的值并转换为目标类型，失败时返回 null。
     * </p>
     *
     * <pre>{@code
     * Integer value = ContextBuilder.getValue("age", Integer.class);
     * if (value != null) {
     *     System.out.println("年龄: " + value);
     * }
     * }</pre>
     *
     * @param key   键名
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 指定类型的参数值，或 null 如果未找到或转换失败
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
            try {
                return (T) Integer.valueOf(value);
            } catch (NumberFormatException e) {
                Logger.warn("Failed to convert value to Integer, key: {}, value: {}", key, value, e);
                return null;
            }
        } else if (clazz == Long.class || clazz == long.class) {
            try {
                return (T) Long.valueOf(value);
            } catch (NumberFormatException e) {
                Logger.warn("Failed to convert value to Long, key: {}, value: {}", key, value, e);
                return null;
            }
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (clazz == Double.class || clazz == double.class) {
            try {
                return (T) Double.valueOf(value);
            } catch (NumberFormatException e) {
                Logger.warn("Failed to convert value to Double, key: {}, value: {}", key, value, e);
                return null;
            }
        } else if (clazz == Float.class || clazz == float.class) {
            try {
                return (T) Float.valueOf(value);
            } catch (NumberFormatException e) {
                Logger.warn("Failed to convert value to Float, key: {}, value: {}", key, value, e);
                return null;
            }
        } else {
            try {
                return JsonKit.toPojo(value, clazz);
            } catch (Exception e) {
                Logger.warn("Failed to convert value to {}, key: {}, value: {}", clazz.getSimpleName(), key, value, e);
                return null;
            }
        }
    }

    /**
     * 获取 JSON 对象参数值
     * <p>
     * 从 JSON 请求体中获取指定键的值并转换为目标类型。
     * </p>
     *
     * <pre>{@code
     * User user = ContextBuilder.getJsonValue("user", User.class);
     * if (user != null) {
     *     System.out.println("用户: " + user.getName());
     * }
     * }</pre>
     *
     * @param key   键名
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 指定类型的参数值，或 null 如果未找到或转换失败
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
            Logger.warn("Failed to convert JSON value to {}, key: {}, value: {}", clazz.getSimpleName(), key, value, e);
            return null;
        }
    }

    /**
     * 获取当前用户信息
     * <p>
     * 从自定义提供者或请求头、上下文中的用户 ID 获取授权信息。
     * </p>
     *
     * <pre>{@code
     * Authorize auth = ContextBuilder.getAuthorize();
     * if (auth != null) {
     *     System.out.println("用户ID: " + auth.getX_user_id());
     * }
     * }</pre>
     *
     * @return Authorize 对象，或 null 如果无法获取
     */
    public static Authorize getAuthorize() {
        try {
            if (provider != null) {
                Authorize authorize = provider.getAuthorize();
                Logger.info("==>  Authorize: {}", authorize);
                return authorize;
            }
            String userId = getValue("x_user_id", EnumValue.Params.HEADER);
            if (StringKit.isEmpty(userId)) {
                userId = getValue("x_user_id", EnumValue.Params.CONTEXT);
            }
            if (StringKit.isEmpty(userId)) {
                Logger.info("==>  Authorize: No user ID found in headers or context");
                return null;
            }
            return JsonKit.toPojo(UrlDecoder.decode(userId, Charset.UTF_8), Authorize.class);
        } catch (Exception e) {
            Logger.info("==>  Authorize: Failed to get authorize");
            return null;
        }
    }

    /**
     * 获取租户 ID
     * <p>
     * 按优先级从用户授权数据、请求头、请求参数、JSON 请求体或上下文获取租户 ID。
     * </p>
     *
     * <pre>{@code
     * String tenantId = ContextBuilder.getTenantId();
     * if (tenantId != null) {
     *     System.out.println("租户ID: " + tenantId);
     * }
     * }</pre>
     *
     * @return 租户 ID，或 null 如果未找到
     */
    public static String getTenantId() {
        try {
            if (provider != null) {
                String tenantId = provider.getTenantId();
                Logger.info("==>  Tenant ID: {}", tenantId);
                return tenantId;
            }
            Authorize authorize = getAuthorize();
            if (authorize != null) {
                String tenantId = authorize.getX_tenant_id();
                if (!StringKit.isEmpty(tenantId)) {
                    Logger.info("==>  Tenant ID: {}", tenantId);
                    return tenantId;
                }
            }
            String tenantId = getValue("x_tenant_id", EnumValue.Params.HEADER);
            if (!StringKit.isEmpty(tenantId)) {
                Logger.info("==>  Tenant ID: {}", tenantId);
                return tenantId;
            }
            tenantId = getValue("tenant_id", EnumValue.Params.PARAMETER);
            if (!StringKit.isEmpty(tenantId)) {
                Logger.info("==>  Tenant ID: {}", tenantId);
                return tenantId;
            }
            tenantId = getValue("tenant_id", EnumValue.Params.JSON_BODY);
            Logger.info("==>  Tenant ID: {}", tenantId);
            return tenantId;
        } catch (Exception e) {
            Logger.info("==>  Tenant ID: Failed to get tenant ID");
            return null;
        }
    }

    /**
     * 检查请求是否包含文件上传
     * <p>
     * 检查 HTTP 请求的 Content-Type 是否为 multipart 类型。
     * </p>
     *
     * <pre>{@code
     * HttpServletRequest request = ContextBuilder.getRequest();
     * boolean isMultipart = ContextBuilder.isMultipartContent(request);
     * System.out.println("是否文件上传: " + isMultipart);
     * }</pre>
     *
     * @param request HTTP 请求对象
     * @return 如果是文件上传请求则返回 true，否则返回 false
     */
    public static boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * 清除请求上下文
     * <p>
     * 移除当前线程的请求 ID，并清除相关缓存。
     * </p>
     *
     * <pre>{@code
     * ContextBuilder.clear();
     * System.out.println("请求上下文已清除");
     * }</pre>
     */
    public static void clear() {
        String requestId = REQUEST_ID.get();
        if (requestId != null) {
            CacheX<String, Map<String, String>> headerCache = getHeaderCache();
            if (headerCache != null) {
                headerCache.remove(requestId);
            }
            CacheX<String, Map<String, String>> parameterCache = getParameterCache();
            if (parameterCache != null) {
                parameterCache.remove(requestId);
            }
            CacheX<String, String> jsonBodyCache = getBodyCache();
            if (jsonBodyCache != null) {
                jsonBodyCache.remove(requestId);
            }
            REQUEST_ID.remove();
            Logger.debug("<==    Cleared: {}", requestId);
        } else {
            Logger.debug("<==    Cleared: No request ID to clear");
        }
    }

    /**
     * 清除指定请求的缓存
     * <p>
     * 移除指定请求 ID 的请求头、参数和 JSON 请求体缓存。
     * </p>
     *
     * <pre>{@code
     * ContextBuilder.clear("request123");
     * System.out.println("请求缓存已清除");
     * }</pre>
     *
     * @param requestId 请求 ID
     */
    public static void clear(String requestId) {
        if (requestId != null) {
            CacheX<String, Map<String, String>> headerCache = getHeaderCache();
            if (headerCache != null) {
                headerCache.remove(requestId);
            }
            CacheX<String, Map<String, String>> parameterCache = getParameterCache();
            if (parameterCache != null) {
                parameterCache.remove(requestId);
            }
            CacheX<String, String> jsonBodyCache = getBodyCache();
            if (jsonBodyCache != null) {
                jsonBodyCache.remove(requestId);
            }
            Logger.debug("<==    Cleared: {}", requestId);
        }
    }

    /**
     * 重置所有缓存实例
     * <p>
     * 将所有缓存实例置空，用于测试或重新初始化。
     * </p>
     *
     * <pre>{@code
     * ContextBuilder.reset();
     * System.out.println("所有缓存实例已重置");
     * }</pre>
     */
    public static void reset() {
        HEADER_CACHE = null;
        PARAMETER_CACHE = null;
        BODY_CACHE = null;
        Logger.debug("All cache instances reset");
    }

}