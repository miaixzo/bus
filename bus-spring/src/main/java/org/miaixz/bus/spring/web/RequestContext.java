/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.web;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.spring.web.wrapper.CachedBodyRequestWrapper;

/**
 * Servlet request values scoped and cached exclusively through the current request.
 *
 * @author Kimi Liu
 */
public class RequestContext {

    /**
     * Initializes a request accessor that stores all derived values on the active Servlet request.
     */
    public RequestContext() {
        // No initialization required.
    }

    /**
     * Prefix isolating request-scoped cache attributes from application attributes.
     */
    private static final String ATTRIBUTE_PREFIX = RequestContext.class.getName() + Symbol.DOT;
    /**
     * Request attribute containing the immutable header snapshot.
     */
    private static final String HEADERS_ATTRIBUTE = ATTRIBUTE_PREFIX + "headers";
    /**
     * Request attribute containing the immutable parameter snapshot.
     */
    private static final String PARAMETERS_ATTRIBUTE = ATTRIBUTE_PREFIX + "parameters";
    /**
     * Request attribute containing the parsed JSON body cache.
     */
    private static final String JSON_BODY_ATTRIBUTE = ATTRIBUTE_PREFIX + "jsonBody";
    /**
     * Request attribute containing the immutable parsed JSON-object snapshot.
     */
    private static final String JSON_VALUES_ATTRIBUTE = ATTRIBUTE_PREFIX + "jsonValues";
    /**
     * Request attribute containing the immutable cookie snapshot.
     */
    private static final String COOKIES_ATTRIBUTE = ATTRIBUTE_PREFIX + "cookies";
    /**
     * Marker distinguishing an absent body from an uninitialized cache.
     */
    private static final Object EMPTY_BODY = new Object();

    /**
     * Returns the request bound by Spring to the current thread.
     *
     * @return current HTTP request, or {@code null} outside a Servlet request
     */
    @Nullable
    public HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    /**
     * Returns an immutable case-insensitive snapshot of current request headers.
     *
     * @return immutable header snapshot
     */
    public Map<String, String> getHeaders() {
        return getHeaders(getRequest());
    }

    /**
     * Returns an immutable case-insensitive header snapshot for an explicit request.
     *
     * @param request HTTP request, or {@code null}
     * @return immutable header snapshot
     */
    public Map<String, String> getHeaders(@Nullable HttpServletRequest request) {
        if (request == null) {
            return Map.of();
        }
        Object cached = request.getAttribute(HEADERS_ATTRIBUTE);
        if (cached instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        Map<String, String> headers = new CaseInsensitiveMap<>();
        if (request.getHeaderNames() != null) {
            request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
        }
        Map<String, String> snapshot = Collections.unmodifiableMap(headers);
        request.setAttribute(HEADERS_ATTRIBUTE, snapshot);
        return snapshot;
    }

    /**
     * Returns an immutable case-insensitive snapshot of query and Servlet-decoded form parameters.
     *
     * @return immutable parameter snapshot
     */
    public Map<String, String> getParameters() {
        return getParameters(getRequest());
    }

    /**
     * Returns an immutable parameter snapshot for an explicit request.
     *
     * @param request HTTP request, or {@code null}
     * @return immutable parameter snapshot
     */
    public Map<String, String> getParameters(@Nullable HttpServletRequest request) {
        if (request == null) {
            return Map.of();
        }
        Object cached = request.getAttribute(PARAMETERS_ATTRIBUTE);
        if (cached instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        Map<String, String> parameters = new CaseInsensitiveMap<>();
        request.getParameterMap().forEach((name, values) -> {
            if (values != null && values.length > 0) {
                parameters.put(name, values[0]);
            }
        });
        Map<String, String> snapshot = Collections.unmodifiableMap(parameters);
        request.setAttribute(PARAMETERS_ATTRIBUTE, snapshot);
        return snapshot;
    }

    /**
     * Returns an immutable snapshot of the cached JSON request object.
     * <p>
     * The request body is never consumed directly. An empty map is returned when body caching is unavailable, the
     * content is not a JSON object, or parsing fails.
     *
     * @return immutable JSON-object snapshot
     */
    public Map<String, Object> getJsonBody() {
        return getJsonBody(getRequest());
    }

    /**
     * Returns an immutable cached JSON-object snapshot for an explicit request.
     *
     * @param request HTTP request, or {@code null}
     * @return immutable JSON-object snapshot
     */
    public Map<String, Object> getJsonBody(@Nullable HttpServletRequest request) {
        if (request == null) {
            return Map.of();
        }
        Object cached = request.getAttribute(JSON_VALUES_ATTRIBUTE);
        if (cached instanceof Map<?, ?> map) {
            return objectMap(map);
        }
        try {
            String body = getCachedJsonBody(request);
            if (body == null) {
                request.setAttribute(JSON_VALUES_ATTRIBUTE, Map.of());
                return Map.of();
            }
            Map<String, Object> values = JsonKit.toPojo(body, Map.class);
            Map<String, Object> snapshot = values == null ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(values));
            request.setAttribute(JSON_VALUES_ATTRIBUTE, snapshot);
            return snapshot;
        } catch (RuntimeException ignored) {
            request.setAttribute(JSON_VALUES_ATTRIBUTE, Map.of());
            return Map.of();
        }
    }

    /**
     * Returns an immutable snapshot of current request cookies, retaining the first value for duplicate names.
     *
     * @return immutable cookie snapshot
     */
    public Map<String, String> getCookies() {
        return getCookies(getRequest());
    }

    /**
     * Returns an immutable cookie snapshot for an explicit request, retaining the first value for duplicate names.
     *
     * @param request HTTP request, or {@code null}
     * @return immutable cookie snapshot
     */
    public Map<String, String> getCookies(@Nullable HttpServletRequest request) {
        if (request == null) {
            return Map.of();
        }
        Object cached = request.getAttribute(COOKIES_ATTRIBUTE);
        if (cached instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        Map<String, String> values = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                values.putIfAbsent(cookie.getName(), cookie.getValue());
            }
        }
        Map<String, String> snapshot = Collections.unmodifiableMap(values);
        request.setAttribute(COOKIES_ATTRIBUTE, snapshot);
        return snapshot;
    }

    /**
     * Reads a JSON value only from an already cached request wrapper.
     *
     * @param key JSON field name
     * @return field value, or {@code null} when unavailable
     */
    @Nullable
    public String getValueFromJsonBody(String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        Object value = getJsonBody().get(key);
        return value == null ? null : StringKit.toString(value);
    }

    /**
     * Extracts a JSON field without logging either the document or its value.
     *
     * @param json JSON document
     * @param key  field name
     * @return field value, or {@code null} when absent or invalid
     */
    @Nullable
    public String extractValueFromJson(String json, String key) {
        if (StringKit.isEmpty(json) || StringKit.isEmpty(key) || !JsonKit.isJson(json)) {
            return null;
        }
        try {
            Map<String, Object> values = JsonKit.toPojo(json, Map.class);
            return values.containsKey(key) ? StringKit.toString(values.get(key)) : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Returns a header value without case sensitivity.
     *
     * @param key lookup key
     * @return the header value
     */
    @Nullable
    public String getHeaderValue(String key) {
        return StringKit.isEmpty(key) ? null : getHeaders().get(key);
    }

    /**
     * Returns a query or form parameter value.
     *
     * @param key lookup key
     * @return the parameter value
     */
    @Nullable
    public String getParameterValue(String key) {
        return StringKit.isEmpty(key) ? null : getParameters().get(key);
    }

    /**
     * Returns a cached JSON body field value.
     *
     * @param key lookup key
     * @return the json body value
     */
    @Nullable
    public String getJsonBodyValue(String key) {
        return getValueFromJsonBody(key);
    }

    /**
     * Returns a cookie value.
     *
     * @param key lookup key
     * @return the cookie value
     */
    @Nullable
    public String getCookieValue(String key) {
        return StringKit.isEmpty(key) ? null : getCookies().get(key);
    }

    /**
     * Returns a URI-template variable exposed by Spring MVC.
     *
     * @param key variable name
     * @return variable value, or {@code null}
     */
    @Nullable
    public String getPathVariable(String key) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return attribute instanceof Map<?, ?> values ? StringKit.toString(values.get(key)) : null;
    }

    /**
     * Returns a text multipart field; uploaded file parts are never read as parameter values.
     *
     * @param key multipart field name
     * @return text field value, or {@code null}
     */
    @Nullable
    public String getMultipartParameterValue(String key) {
        HttpServletRequest request = getRequest();
        if (StringKit.isEmpty(key) || request == null || !isMultipartContent(request)) {
            return null;
        }
        try {
            for (Part part : request.getParts()) {
                if (key.equals(part.getName()) && part.getSubmittedFileName() == null
                        && part.getContentType() == null) {
                    return new String(part.getInputStream().readAllBytes(), Charset.UTF_8);
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    /**
     * Resolves one value using the documented source priority.
     *
     * @param key    value name
     * @param source allowed request source
     * @return resolved value, or {@code null}
     */
    @Nullable
    public String getValue(String key, EnumValue.Params source) {
        if (StringKit.isEmpty(key)) {
            return null;
        }
        Objects.requireNonNull(source, "source");
        return switch (source) {
            case HEADER -> getHeaderValue(key);
            case PARAMETER -> getParameterValue(key);
            case JSON_BODY -> getJsonBodyValue(key);
            case COOKIE -> getCookieValue(key);
            case PATH_VARIABLE -> getPathVariable(key);
            case MULTIPART -> getMultipartParameterValue(key);
            case ALL -> firstNonNull(
                    getHeaderValue(key),
                    getParameterValue(key),
                    getPathVariable(key),
                    getJsonBodyValue(key),
                    getCookieValue(key),
                    getMultipartParameterValue(key));
            case CONTEXT -> null;
        };
    }

    /**
     * Returns an integer request value or its default.
     *
     * @param key          lookup key
     * @param defaultValue fallback value
     * @return the int value
     */
    public int getIntValue(String key, int defaultValue) {
        return number(key, Integer::parseInt, defaultValue);
    }

    /**
     * Returns a long request value or its default.
     *
     * @param key          lookup key
     * @param defaultValue fallback value
     * @return the long value
     */
    public long getLongValue(String key, long defaultValue) {
        return number(key, Long::parseLong, defaultValue);
    }

    /**
     * Returns a double request value or its default.
     *
     * @param key          lookup key
     * @param defaultValue fallback value
     * @return the double value
     */
    public double getDoubleValue(String key, double defaultValue) {
        return number(key, Double::parseDouble, defaultValue);
    }

    /**
     * Returns a boolean request value or its default.
     *
     * @param key          lookup key
     * @param defaultValue fallback value
     * @return the boolean value
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        return StringKit.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Converts a request value to the required type.
     *
     * @param <T>  result type
     * @param key  lookup key
     * @param type target scalar or JSON-mappable result type
     * @return converted request value, or {@code null} when absent or invalid
     */
    @Nullable
    public <T> T getValue(String key, Class<T> type) {
        Objects.requireNonNull(type, "type");
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return null;
        }
        try {
            if (type == String.class)
                return (T) value;
            if (type == Integer.class || type == Integer.TYPE)
                return (T) Integer.valueOf(value);
            if (type == Long.class || type == Long.TYPE)
                return (T) Long.valueOf(value);
            if (type == Boolean.class || type == Boolean.TYPE)
                return (T) Boolean.valueOf(value);
            if (type == Double.class || type == Double.TYPE)
                return (T) Double.valueOf(value);
            if (type == Float.class || type == Float.TYPE)
                return (T) Float.valueOf(value);
            return JsonKit.toPojo(value, type);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Converts a cached JSON body field to the required type.
     *
     * @param <T>  result type
     * @param key  lookup key
     * @param type target type used to decode the JSON field
     * @return decoded field value, or {@code null} when absent or invalid
     */
    @Nullable
    public <T> T getJsonValue(String key, Class<T> type) {
        String value = getJsonBodyValue(key);
        if (StringKit.isEmpty(value)) {
            return null;
        }
        try {
            return JsonKit.toPojo(value, Objects.requireNonNull(type, "type"));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Returns whether the request content type is multipart.
     *
     * @param request HTTP request
     * @return {@code true} for multipart content
     */
    public boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request == null ? null : request.getContentType();
        return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("multipart/");
    }

    /**
     * Returns the cached bounded JSON body for an explicit request.
     *
     * @param request HTTP request, or {@code null}
     * @return cached JSON body, or {@code null}
     */
    @Nullable
    private String getCachedJsonBody(@Nullable HttpServletRequest request) {
        if (request == null || !MediaType.isJson(request.getContentType())) {
            return null;
        }
        Object cached = request.getAttribute(JSON_BODY_ATTRIBUTE);
        if (cached == EMPTY_BODY) {
            return null;
        }
        if (cached instanceof String body) {
            return body;
        }
        if (!(request instanceof CachedBodyRequestWrapper wrapper)) {
            request.setAttribute(JSON_BODY_ATTRIBUTE, EMPTY_BODY);
            return null;
        }
        byte[] bytes = wrapper.getBody();
        String body = bytes == null || bytes.length == 0 ? null : new String(bytes, Charset.UTF_8);
        if (StringKit.isEmpty(body) || !JsonKit.isJson(body)) {
            request.setAttribute(JSON_BODY_ATTRIBUTE, EMPTY_BODY);
            return null;
        }
        request.setAttribute(JSON_BODY_ATTRIBUTE, body);
        return body;
    }

    /**
     * Converts a request value with a numeric parser and fallback.
     *
     * @param <T>          result type
     * @param key          lookup key
     * @param parser       function used to parse the parameter
     * @param defaultValue fallback value
     * @return parsed number, or the supplied fallback
     */
    private <T extends Number> T number(String key, Parser<T> parser, T defaultValue) {
        String value = getValue(key, EnumValue.Params.ALL);
        if (StringKit.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return parser.parse(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * Casts an internally created immutable string map.
     *
     * @param map source map to copy
     * @return a string-keyed copy of the supplied map
     */
    private static Map<String, String> stringMap(Map<?, ?> map) {
        return (Map<String, String>) map;
    }

    /**
     * Casts an internally created immutable object map.
     *
     * @param map source map to cast
     * @return an object-valued map created by this accessor
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    /**
     * Returns the first non-null candidate.
     *
     * @param values candidate values in encounter order
     * @return the first non-null value, or {@code null} when both are absent
     */
    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null)
                return value;
        }
        return null;
    }

    /**
     * Parses a numeric request value.
     *
     * @param <T> result type
     */
    @FunctionalInterface
    private interface Parser<T extends Number> {

        /**
         * Parses a numeric string.
         *
         * @param value numeric text
         * @return parsed number
         */

        T parse(String value) throws NumberFormatException;
    }

}
