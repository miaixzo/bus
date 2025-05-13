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
package org.miaixz.bus.http;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.cache.CacheControl;

/**
 * HTTP 请求类，封装 HTTP 请求的所有信息，包括 URL、方法、头部、请求体和标签。
 * <p>
 * 注意：当 {@link #body} 为空时，实例是不可变的；否则，请求体可能影响实例状态。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Request {

    /**
     * 请求的 URL
     */
    final UnoUrl url;
    /**
     * HTTP 方法（GET、POST 等）
     */
    final String method;
    /**
     * 请求头部
     */
    final Headers headers;
    /**
     * 请求体（可能为 null）
     */
    final RequestBody body;
    /**
     * 标签映射，用于附加元数据
     */
    final Map<Class<?>, Object> tags;
    /**
     * 缓存控制（延迟初始化）
     */
    private volatile CacheControl cacheControl;

    /**
     * 构造函数，基于 Builder 初始化 Request 实例。
     *
     * @param builder Builder 实例，包含所有请求属性
     */
    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tags = org.miaixz.bus.http.Builder.immutableMap(builder.tags);
    }

    /**
     * 获取请求的 URL。
     *
     * @return UnoUrl 对象
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * 获取 HTTP 方法。
     *
     * @return 方法名称（如 GET、POST）
     */
    public String method() {
        return method;
    }

    /**
     * 获取所有请求头部。
     *
     * @return Headers 对象
     */
    public Headers headers() {
        return headers;
    }

    /**
     * 获取指定名称的第一个头部值。
     *
     * @param name 头部名称
     * @return 头部值（不存在时为 null）
     */
    public String header(String name) {
        return headers.get(name);
    }

    /**
     * 获取指定名称的头部值列表。
     *
     * @param name 头部名称
     * @return 头部值列表（可能为空）
     */
    public List<String> headers(String name) {
        return headers.values(name);
    }

    /**
     * 获取请求体。
     *
     * @return RequestBody 对象（可能为 null）
     */
    public RequestBody body() {
        return body;
    }

    /**
     * 获取使用 {@code Object.class} 作为键的标签。
     * <p>
     * 如果没有附加标签，返回 null。如果需要获取派生请求的标签，需通过 {@link #newBuilder()} 创建新实例。
     * </p>
     *
     * @return 标签对象（可能为 null）
     */
    public Object tag() {
        return tag(Object.class);
    }

    /**
     * 获取指定类型的标签。
     * <p>
     * 使用指定的 {@code type} 作为键从标签映射中获取值。返回值为指定类型的实例， 或 null（如果标签不存在）。
     * </p>
     *
     * @param type 标签类型
     * @param <T>  标签值的类型
     * @return 标签值（可能为 null）
     */
    public <T> T tag(Class<? extends T> type) {
        return type.cast(tags.get(type));
    }

    /**
     * 创建新的 Builder 实例，基于当前 Request。
     *
     * @return Builder 实例
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 获取缓存控制指令。
     * <p>
     * 即使请求不包含 "Cache-Control" 头部，也返回非 null 的 CacheControl 对象。 使用延迟初始化以提高性能。
     * </p>
     *
     * @return CacheControl 对象
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return null != result ? result : (cacheControl = CacheControl.parse(headers));
    }

    /**
     * 检查请求是否使用 HTTPS 协议。
     *
     * @return true 如果 URL 使用 HTTPS
     */
    public boolean isHttps() {
        return url.isHttps();
    }

    /**
     * 返回请求的字符串表示。
     *
     * @return 包含方法、URL 和标签的字符串
     */
    @Override
    public String toString() {
        return "Request{method=" + method + ", url=" + url + ", tags=" + tags + Symbol.C_BRACE_RIGHT;
    }

    /**
     * Request 构建器，用于创建和修改 Request 实例。
     */
    public static class Builder {
        /**
         * 请求的 URL
         */
        UnoUrl url;
        /**
         * HTTP 方法
         */
        String method;
        /**
         * 请求头部构建器
         */
        Headers.Builder headers;
        /**
         * 请求体
         */
        RequestBody body;
        /**
         * 标签映射（可变或空）
         */
        Map<Class<?>, Object> tags = Collections.emptyMap();

        /**
         * 默认构造函数，初始化 GET 请求。
         */
        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        /**
         * 构造函数，基于现有 Request 初始化 Builder。
         *
         * @param request Request 实例
         */
        Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tags = request.tags.isEmpty() ? Collections.emptyMap() : new LinkedHashMap<>(request.tags);
            this.headers = request.headers.newBuilder();
        }

        /**
         * 设置请求的 URL。
         *
         * @param url UnoUrl 对象
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 url 为 null
         */
        public Builder url(UnoUrl url) {
            if (url == null)
                throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * 设置请求的 URL（字符串格式）。
         * <p>
         * 将 WebSocket URL（ws: 或 wss:）转换为 HTTP URL（http: 或 https:）。
         * </p>
         *
         * @param url URL 字符串
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 url 为 null
         * @throws IllegalArgumentException 如果 URL 无效
         */
        public Builder url(String url) {
            if (url == null)
                throw new NullPointerException("url == null");

            // 转换 WebSocket URL 为 HTTP URL
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            return url(UnoUrl.get(url));
        }

        /**
         * 设置请求的 URL（URL 对象）。
         *
         * @param url URL 对象
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 url 为 null
         * @throws IllegalArgumentException 如果 URL 方案不是 http 或 https
         */
        public Builder url(URL url) {
            if (url == null)
                throw new NullPointerException("url == null");
            return url(UnoUrl.get(url.toString()));
        }

        /**
         * 设置指定名称的头部值，替换现有同名头部。
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * 添加指定名称和值的头部，保留现有同名头部。
         * <p>
         * 对于某些头部（如 Content-Length、Content-Encoding），Http 客户端可能根据请求体替换值。
         * </p>
         *
         * @param name  头部名称
         * @param value 头部值
         * @return 当前 Builder 实例
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * 移除指定名称的所有头部。
         *
         * @param name 头部名称
         * @return 当前 Builder 实例
         */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * 设置所有头部，替换现有头部。
         *
         * @param headers Headers 对象
         * @return 当前 Builder 实例
         */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        /**
         * 设置缓存控制头部。
         * <p>
         * 替换现有的 Cache-Control 头部。如果 cacheControl 无指令，移除 Cache-Control 头部。
         * </p>
         *
         * @param cacheControl CacheControl 对象
         * @return 当前 Builder 实例
         */
        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty())
                return removeHeader(HTTP.CACHE_CONTROL);
            return header(HTTP.CACHE_CONTROL, value);
        }

        /**
         * 设置为 GET 请求。
         *
         * @return 当前 Builder 实例
         */
        public Builder get() {
            return method(HTTP.GET, null);
        }

        /**
         * 设置为 HEAD 请求。
         *
         * @return 当前 Builder 实例
         */
        public Builder head() {
            return method(HTTP.HEAD, null);
        }

        /**
         * 设置为 POST 请求。
         *
         * @param body 请求体
         * @return 当前 Builder 实例
         */
        public Builder post(RequestBody body) {
            return method(HTTP.POST, body);
        }

        /**
         * 设置为 DELETE 请求（带请求体）。
         *
         * @param body 请求体
         * @return 当前 Builder 实例
         */
        public Builder delete(RequestBody body) {
            return method(HTTP.DELETE, body);
        }

        /**
         * 设置为 DELETE 请求（无请求体）。
         *
         * @return 当前 Builder 实例
         */
        public Builder delete() {
            return delete(RequestBody.create(null, Normal.EMPTY_BYTE_ARRAY));
        }

        /**
         * 设置为 PUT 请求。
         *
         * @param body 请求体
         * @return 当前 Builder 实例
         */
        public Builder put(RequestBody body) {
            return method(HTTP.PUT, body);
        }

        /**
         * 设置为 PATCH 请求。
         *
         * @param body 请求体
         * @return 当前 Builder 实例
         */
        public Builder patch(RequestBody body) {
            return method(HTTP.PATCH, body);
        }

        /**
         * 设置 HTTP 方法和请求体。
         *
         * @param method HTTP 方法
         * @param body   请求体（可能为 null）
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 method 为 null
         * @throws IllegalArgumentException 如果 method 为空、请求体与方法不匹配
         */
        public Builder method(String method, RequestBody body) {
            if (null == method)
                throw new NullPointerException("method == null");
            if (method.length() == 0)
                throw new IllegalArgumentException("method.length() == 0");
            if (body != null && !HTTP.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HTTP.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        /**
         * 使用 {@code Object.class} 作为键附加标签。
         *
         * @param tag 标签对象
         * @return 当前 Builder 实例
         */
        public Builder tag(Object tag) {
            return tag(Object.class, tag);
        }

        /**
         * 使用指定类型作为键附加标签。
         * <p>
         * 标签用于附加调试、计时或其他元数据，可在拦截器、事件监听器或回调中读取。 使用 null 移除指定类型的现有标签。
         * </p>
         *
         * @param type 标签类型
         * @param tag  标签值（可能为 null）
         * @param <T>  标签值的类型
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 type 为 null
         */
        public <T> Builder tag(Class<? super T> type, T tag) {
            if (null == type)
                throw new NullPointerException("type == null");

            if (null == tag) {
                tags.remove(type);
            } else {
                if (tags.isEmpty())
                    tags = new LinkedHashMap<>();
                tags.put(type, type.cast(tag));
            }

            return this;
        }

        /**
         * 构建 Request 实例。
         *
         * @return Request 对象
         * @throws IllegalStateException 如果 URL 未设置
         */
        public Request build() {
            if (null == url)
                throw new IllegalStateException("url == null");
            return new Request(this);
        }
    }

}