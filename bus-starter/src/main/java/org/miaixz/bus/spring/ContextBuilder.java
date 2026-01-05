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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NonNull;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.http.MutableRequestWrapper;
import org.miaixz.bus.vortex.Args;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * A utility class for convenient operations on HTTP requests, user information, and more.
 * <p>
 * It uses the {@link CacheX} interface for caching, supports lazy initialization and runtime replacement, and provides
 * methods to retrieve parameters from various sources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContextBuilder extends WebUtils {

    /**
     * Cache implementation for request headers.
     */
    private static volatile CacheX<String, Map<String, String>> HEADER_CACHE;

    /**
     * Cache implementation for request parameters.
     */
    private static volatile CacheX<String, Map<String, String>> PARAMETER_CACHE;

    /**
     * Cache implementation for request bodies.
     */
    private static volatile CacheX<String, String> BODY_CACHE;

    /**
     * Thread-local storage for the current request ID.
     */
    private static final ThreadLocal<String> REQUEST_ID = ThreadKit.newThreadLocal(false);

    /**
     * The default maximum number of entries for caches.
     */
    private static final long DEFAULT_CACHE_SIZE = 1000;

    /**
     * The default cache expiration time in milliseconds.
     */
    private static final long DEFAULT_CACHE_EXPIRE = TimeUnit.SECONDS.toMillis(10);

    /**
     * The instance of the context provider for user and tenant information.
     */
    public static volatile ContextProvider provider;

    /**
     * Sets a custom context provider for retrieving user and tenant information.
     *
     * @param provider The user information provider.
     */
    public static void setProvider(ContextProvider provider) {
        ContextBuilder.provider = provider;
    }

    /**
     * Initializes the request context by generating a new request ID and storing it in a ThreadLocal.
     */
    public static void setRequestId() {
        String requestId = ID.objectId();
        REQUEST_ID.set(requestId);
    }

    /**
     * Gets the ID for the current request. If it doesn't exist, a new one is generated.
     *
     * @return The request ID, or null if no request is available.
     */
    public static String getRequestId() {
        String requestId = REQUEST_ID.get();
        if (requestId == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                requestId = ID.objectId();
                REQUEST_ID.set(requestId);
                Logger.debug(true, "Context", "Request ID: {}", requestId);
            } else {
                Logger.debug(true, "Context", "No request available to generate request ID");
            }
        }
        return requestId;
    }

    /**
     * Gets the current {@link HttpServletRequest} object from the {@code RequestContextHolder}.
     *
     * @return The {@code HttpServletRequest} object, or null if not available.
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * Gets the lazily-initialized cache instance for headers, falling back to a memory cache if Caffeine is
     * unavailable.
     *
     * @return The cache instance for request headers.
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
                                true,
                                "Context",
                                "Header cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    HEADER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * Gets the lazily-initialized cache instance for parameters, falling back to a memory cache if Caffeine is
     * unavailable.
     *
     * @return The cache instance for request parameters.
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
                                true,
                                "Context",
                                "Parameter cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    PARAMETER_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * Gets the lazily-initialized cache instance for JSON request bodies, falling back to a memory cache if Caffeine is
     * unavailable.
     *
     * @return The cache instance for JSON request bodies.
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
                                true,
                                "Context",
                                "Body cache failed to initialize CaffeineCache, falling back to MemoryCache");
                        cache = new MemoryCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE);
                    }
                    BODY_CACHE = cache;
                }
            }
        }
        return cache;
    }

    /**
     * Sets a custom cache implementation for request headers.
     *
     * @param cache The cache implementation to use.
     */
    public static void setHeaderCache(@NonNull CacheX<String, Map<String, String>> cache) {
        HEADER_CACHE = cache;
    }

    /**
     * Sets a custom cache implementation for request parameters.
     *
     * @param cache The cache implementation to use.
     */
    public static void setParameterCache(@NonNull CacheX<String, Map<String, String>> cache) {
        PARAMETER_CACHE = cache;
    }

    /**
     * Sets a custom cache implementation for JSON request bodies.
     *
     * @param cache The cache implementation to use.
     */
    public static void setBodyCache(@NonNull CacheX<String, String> cache) {
        BODY_CACHE = cache;
    }

    /**
     * A generic method to retrieve data from a cache, falling back to a supplier and caching the result if not found.
     *
     * @param <T>          The type of the data.
     * @param requestId    The request ID used as the cache key.
     * @param dataSupplier A supplier to provide the data if it's not in the cache.
     * @param cache        The cache instance to use.
     * @param defaultValue The default value to return if no data is found.
     * @return The cached data, newly supplied data, or the default value.
     */
    private static <T> T getCached(
            String requestId,
            SupplierX<T> dataSupplier,
            CacheX<String, T> cache,
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
     * Gets all headers from the current HTTP request, using a cache for performance.
     *
     * @return A case-insensitive map of request headers.
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
     * Gets all parameters from the current HTTP request, including query string, form data, and form-urlencoded body
     * content. The results are cached.
     *
     * @return A case-insensitive map of request parameters.
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
                request.getParameterMap().forEach((key, values) -> {
                    if (values != null && values.length > 0) {
                        parameters.put(key, values[0]);
                    }
                });
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
     * Parses a URL-encoded string into a map of parameters.
     *
     * @param urlEncoded The URL-encoded string (e.g., "id=1&name=test").
     * @return A map where keys are parameter names and values are arrays of parameter values.
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
                paramMap.put(keyValue[0], keyValue[1].split(","));
            } else if (keyValue.length == 1) {
                paramMap.put(keyValue[0], new String[] { "" });
            }
        }
        return paramMap;
    }

    /**
     * Gets the value of a specified key from a JSON request body. The request body is read once and cached for the
     * duration of the request.
     *
     * @param key The key name to look for in the JSON body.
     * @return The value associated with the key, or null if not found or if the request is not JSON.
     */
    public static String getValueFromJsonBody(String key) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug(true, "Context", "No request available for JSON body lookup, key: {}", key);
            return null;
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith(MediaType.APPLICATION_JSON)) {
            Logger.debug(true, "Context", "Request is not JSON, key: {}, contentType: {}", key, contentType);
            return null;
        }
        String requestId = getRequestId();
        if (requestId == null) {
            Logger.debug(true, "Context", "No request ID available for JSON body lookup, key: {}", key);
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
                requestBody = (bodyBytes != null && bodyBytes.length > 0) ? new String(bodyBytes, Charset.UTF_8)
                        : Normal.EMPTY;
            } else {
                requestBody = new String(request.getInputStream().readAllBytes(), Charset.UTF_8);
            }
        } catch (IOException e) {
            Logger.error(true, "Context", "Failed to read JSON body, key: {}", key, e);
            return null;
        }
        if (StringKit.isEmpty(requestBody) || !JsonKit.isJson(requestBody)) {
            Logger.debug(true, "Context", "Empty or invalid JSON body, key: {}", key);
            return null;
        }
        getBodyCache().write(requestId, requestBody, DEFAULT_CACHE_EXPIRE);
        return extractValueFromJson(requestBody, key);
    }

    /**
     * Extracts the value of a specified key from a JSON string.
     *
     * @param json The JSON string.
     * @param key  The key name.
     * @return The extracted value as a string, or null if not found.
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
            Logger.error(true, "Context", "Failed to extract JSON value, key: {}, json: {}", key, json, e);
        }
        return null;
    }

    /**
     * Gets the value of a specified header.
     *
     * @param key The header name.
     * @return The header value, or null if not found.
     */
    @Nullable
    public static String getHeaderValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return getHeaders().get(key);
    }

    /**
     * Gets the value of a specified request parameter (from query string or form data).
     *
     * @param key The parameter name.
     * @return The parameter value, or null if not found.
     */
    @Nullable
    public static String getParameterValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return getParameters().get(key);
    }

    /**
     * Gets the value of a specified key from the JSON request body.
     *
     * @param key The key name.
     * @return The value from the JSON body, or null if not found.
     */
    @Nullable
    public static String getJsonBodyValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return getValueFromJsonBody(key);
    }

    /**
     * Gets the value of a specified cookie.
     *
     * @param key The cookie name.
     * @return The cookie value, or null if not found.
     */
    @Nullable
    public static String getCookieValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug(true, "Context", "No request available for cookie lookup, key: {}", key);
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            Logger.debug(true, "Context", "No cookies found for key: {}", key);
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
     * Gets the value of a specified path variable.
     *
     * @param key The path variable name.
     * @return The path variable value, or null if not found.
     */
    @Nullable
    public static String getPathVariable(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug(true, "Context", "No request available for path variable lookup, key: {}", key);
            return null;
        }
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return (pathVariables != null) ? pathVariables.get(key) : null;
    }

    /**
     * Gets the value of a form field from a multipart request.
     *
     * @param key The name of the form field.
     * @return The value of the field, or null if not found or not a multipart request.
     */
    @Nullable
    public static String getMultipartParameterValue(@Nullable String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            Logger.debug(true, "Context", "No request available for multipart lookup, key: {}", key);
            return null;
        }
        if (!isMultipartContent(request)) {
            Logger.debug(true, "Context", "Request is not multipart, key: {}", key);
            return null;
        }
        try {
            for (Part part : request.getParts()) {
                if (key.equals(part.getName()) && part.getContentType() == null) { // Form field
                    return new String(part.getInputStream().readAllBytes(), Charset.UTF_8);
                }
            }
        } catch (Exception e) {
            Logger.error(true, "Context", "Failed to get multipart parameter, key: {}", key, e);
            return null;
        }
        return null;
    }

    /**
     * Gets a parameter value from a specified source (e.g., header, parameter).
     *
     * @param key    The key name.
     * @param source The source to look in (e.g., {@code EnumValue.Params.HEADER}).
     * @return The parameter value, or null if not found.
     */
    @Nullable
    public static String getValue(@Nullable String key, @NonNull EnumValue.Params source) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        return switch (source) {
            case HEADER -> getHeaderValue(key);
            case PARAMETER -> getParameterValue(key);
            case JSON_BODY -> getJsonBodyValue(key);
            case COOKIE -> getCookieValue(key);
            case PATH_VARIABLE -> getPathVariable(key);
            case MULTIPART -> getMultipartParameterValue(key);
            case ALL -> {
                String value = getHeaderValue(key);
                if (value == null)
                    value = getParameterValue(key);
                if (value == null)
                    value = getPathVariable(key);
                if (value == null)
                    value = getJsonBodyValue(key);
                if (value == null)
                    value = getCookieValue(key);
                if (value == null)
                    value = getMultipartParameterValue(key);
                yield value;
            }
            default -> null;
        };
    }

    /**
     * Gets an integer value from any source (header, parameter, etc.), with a default value.
     *
     * @param key          The key name.
     * @param defaultValue The default value to return if the key is not found or parsing fails.
     * @return The integer value.
     */
    public static int getIntValue(@Nullable String key, int defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Logger.warn(true, "Context", "Failed to parse int value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * Gets a long value from any source, with a default value.
     *
     * @param key          The key name.
     * @param defaultValue The default value.
     * @return The long value.
     */
    public static long getLongValue(@Nullable String key, long defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Logger.warn(true, "Context", "Failed to parse long value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean value from any source, with a default value.
     *
     * @param key          The key name.
     * @param defaultValue The default value.
     * @return The boolean value.
     */
    public static boolean getBooleanValue(@Nullable String key, boolean defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        return StringKit.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Gets a double value from any source, with a default value.
     *
     * @param key          The key name.
     * @param defaultValue The default value.
     * @return The double value.
     */
    public static double getDoubleValue(@Nullable String key, double defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Logger.warn(true, "Context", "Failed to parse double value, key: {}, value: {}", key, value, e);
            return defaultValue;
        }
    }

    /**
     * Gets a value from any source and converts it to the specified type.
     *
     * @param key   The key name.
     * @param clazz The target class type.
     * @param <T>   The generic type.
     * @return The converted value, or null if not found or conversion fails.
     */
    public static <T> T getValue(@Nullable String key, @NonNull Class<T> clazz) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return null;
        }
        try {
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
                return JsonKit.toPojo(value, clazz);
            }
        } catch (Exception e) {
            Logger.warn(
                    true,
                    "Context",
                    "Failed to convert value to {}, key: {}, value: {}",
                    clazz.getSimpleName(),
                    key,
                    value,
                    e);
            return null;
        }
    }

    /**
     * Gets a value from the JSON request body and converts it to the specified type.
     *
     * @param key   The key name in the JSON body.
     * @param clazz The target class type.
     * @param <T>   The generic type.
     * @return The converted value, or null if not found or conversion fails.
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
            Logger.warn(
                    true,
                    "Context",
                    "Failed to convert JSON value to {}, key: {}, value: {}",
                    clazz.getSimpleName(),
                    key,
                    value,
                    e);
            return null;
        }
    }

    /**
     * Gets the authorization information for the current user, either from a custom provider or by parsing a user ID
     * from the request context.
     *
     * @return The {@link Authorize} object, or null if not available.
     */
    public static Authorize getAuthorize() {
        try {
            if (provider != null) {
                Authorize authorize = provider.getAuthorize();
                Logger.info(true, "Context", "Authorize: {}", authorize);
                return authorize;
            }
            String userId = getValue("x_user_id", EnumValue.Params.HEADER);
            if (StringKit.isEmpty(userId)) {
                userId = getValue("x_user_id", EnumValue.Params.CONTEXT);
            }
            if (StringKit.isEmpty(userId)) {
                Logger.info(true, "Context", "No user ID found in headers or context");
                return null;
            }
            return JsonKit.toPojo(UrlDecoder.decode(userId, Charset.UTF_8), Authorize.class);
        } catch (Exception e) {
            Logger.info(true, "Context", "Failed to get authorize");
            return null;
        }
    }

    public static Authorize setAuthorize(String id) {
        ContextBuilder.setProvider(new ContextProvider() {

            @Override
            public Authorize getAuthorize() {
                return Authorize.builder().x_tenant_id(id).build();
            }
        });
        return getAuthorize();
    }

    /**
     * Gets the tenant ID from various sources with a defined priority: custom provider > user authorization object >
     * header &gt; parameter &gt; JSON body.
     *
     * @return The tenant ID, or null if not found.
     */
    public static String getTenantId() {
        try {
            Authorize authorize = getAuthorize();
            if (authorize != null && StringKit.isNotEmpty(authorize.getX_tenant_id())) {
                Logger.info(true, "Context", "Tenant ID: {}", authorize.getX_tenant_id());
                return authorize.getX_tenant_id();
            }
            String tenantId = getValue("x_tenant_id", EnumValue.Params.HEADER);
            if (StringKit.isNotEmpty(tenantId)) {
                Logger.info(true, "Context", "Tenant ID: {}", tenantId);
                return tenantId;
            }
            tenantId = getValue("tenant_id", EnumValue.Params.PARAMETER);
            if (StringKit.isNotEmpty(tenantId)) {
                Logger.info(true, "Context", "Tenant ID: {}", tenantId);
                return tenantId;
            }
            tenantId = getValue("tenant_id", EnumValue.Params.JSON_BODY);
            Logger.info(true, "Context", "Tenant ID: {}", tenantId);
            return tenantId;
        } catch (Exception e) {
            Logger.info(true, "Context", "Failed to get tenant ID");
            return null;
        }
    }

    /**
     * Checks if the request has a "multipart/" content type.
     *
     * @param request The HTTP request.
     * @return {@code true} if it is a multipart request, {@code false} otherwise.
     */
    public static boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * Clears the context for the current request, removing the request ID and associated cache entries.
     */
    public static void clear() {
        String requestId = REQUEST_ID.get();
        if (requestId != null) {
            getHeaderCache().remove(requestId);
            getParameterCache().remove(requestId);
            getBodyCache().remove(requestId);
            REQUEST_ID.remove();
            Logger.debug(false, "Context", "Cleared: {}", requestId);
        } else {
            Logger.debug(false, "Context", "No request ID to clear");
        }
    }

    /**
     * Extracts the authentication token from the incoming request.
     *
     * <p>
     * The token extraction follows a specific order of precedence to ensure compatibility with both standard and legacy
     * authentication methods:
     * </p>
     *
     * <ol>
     * <li><b>Standard Authorization Header:</b> It first checks for the standard {@code Authorization: Bearer <token>}
     * header. This is the preferred and most secure method.</li>
     * <li><b>Custom Header for Backward Compatibility:</b> If the standard header is not found, it searches for a
     * custom header, {@code X-Access-Token}. This check is performed against a list of common case variations (e.g.,
     * {@code X_ACCESS_TOKEN}, {@code x_access_token}) to accommodate different client implementations.</li>
     * <li><b>Request Parameter as Fallback:</b> As a final fallback, if no token is found in the headers, the method
     * searches for the token in the request parameters (query string) using the same set of keys as the custom header.
     * </li>
     * </ol>
     *
     * @return The extracted token string, or {@code null} if no token is found in any of the checked locations.
     */
    public static String getToken() {
        // 1. Prioritize the standard `Authorization` header with the `Bearer` scheme.
        String authorization = getHeaderValue("Authorization");
        if (StringKit.isNotEmpty(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // 2. Check for a custom `X-Access-Token` header for backward compatibility.
        final String[] keys = { Args.X_ACCESS_TOKEN, Args.X_ACCESS_TOKEN.toUpperCase(),
                Args.X_ACCESS_TOKEN.toLowerCase(), "X_Access_Token", "X_ACCESS_TOKEN", "x_access_token" };

        String token = MapKit.getFirstNonNull(getHeaders(), keys);
        if (StringKit.isNotEmpty(token)) {
            return token;
        }

        // 3. If not found in headers, search in request parameters as a fallback.
        if (StringKit.isBlank(token)) {
            token = MapKit.getFirstNonNull(getParameters(), keys);
        }

        return token;
    }

    /**
     * Searches for an API key in a predefined list of request parameters and headers.
     *
     * @return The found API key, or {@code null} if not present.
     */
    public static String getApiKey() {
        final String[] keys = { "apiKey", "apikey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID",
                "X-API-KEY", "API-KEY", "API-ID" };

        // First, search in request parameters.
        String apiKey = MapKit.getFirstNonNull(getParameters(), keys);

        // If not found, search in request headers.
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(getHeaders(), keys);
        }

        return apiKey;
    }

    /**
     * Clears the cache entries for a specific request ID.
     *
     * @param requestId The ID of the request whose cache should be cleared.
     */
    public static void clear(String requestId) {
        if (requestId != null) {
            getHeaderCache().remove(requestId);
            getParameterCache().remove(requestId);
            getBodyCache().remove(requestId);
            Logger.debug(false, "Context", "Cleared: {}", requestId);
        }
    }

    /**
     * Resets all cache instances to null, for testing or re-initialization.
     */
    public static void reset() {
        HEADER_CACHE = null;
        PARAMETER_CACHE = null;
        BODY_CACHE = null;
        Logger.debug(true, "Context", "All cache instances reset");
    }

}
