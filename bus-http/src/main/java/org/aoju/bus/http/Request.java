/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.http;

import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.cache.CacheControl;
import org.aoju.bus.http.metric.http.HttpMethod;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 一个HTTP请求。如果该类的{@link #body}为空或自身为不可变，则该类的实例是不可变的.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public final class Request {

    final UnoUrl url;
    final String method;
    final Headers headers;
    final RequestBody body;
    final Map<Class<?>, Object> tags;

    private volatile CacheControl cacheControl;

    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tags = org.aoju.bus.http.Builder.immutableMap(builder.tags);
    }

    public UnoUrl url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Headers headers() {
        return headers;
    }

    public String header(String name) {
        return headers.get(name);
    }

    public List<String> headers(String name) {
        return headers.values(name);
    }

    public RequestBody body() {
        return body;
    }

    /**
     * 返回带有{@code Object.class}作为键，如果没有附加任何标记，则为null
     * 如果没有附加标记，这个方法就不会返回null。相反，它返回的要么是这个请求，
     * 要么是使用{@link #newBuilder()}派生该请求的请求
     *
     * @return the object
     */
    public Object tag() {
        return tag(Object.class);
    }

    public <T> T tag(Class<? extends T> type) {
        return type.cast(tags.get(type));
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers));
    }

    public boolean isHttps() {
        return url.isHttps();
    }

    @Override
    public String toString() {
        return "Request{method="
                + method
                + ", url="
                + url
                + ", tags="
                + tags
                + Symbol.C_BRACE_RIGHT;
    }

    public static class Builder {
        UnoUrl url;
        String method;
        Headers.Builder headers;
        RequestBody body;

        Map<Class<?>, Object> tags = Collections.emptyMap();

        public Builder() {
            this.method = Http.GET;
            this.headers = new Headers.Builder();
        }

        Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tags = request.tags.isEmpty()
                    ? Collections.emptyMap()
                    : new LinkedHashMap<>(request.tags);
            this.headers = request.headers.newBuilder();
        }

        public Builder url(UnoUrl url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * 设置此请求的URL目标
         *
         * @param url 请求地址
         * @return 构造器
         */
        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");

            // 用HTTP url替换web套接字url.
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            return url(UnoUrl.get(url));
        }

        /**
         * 设置此请求的URL目标
         *
         * @param url 请求地址
         * @return 构造器
         */
        public Builder url(URL url) {
            if (url == null) throw new NullPointerException("url == null");
            return url(UnoUrl.get(url.toString()));
        }

        /**
         * 将header属性{@code name}值设置为{@code value}，
         * 如果此请求已经有任何具有该名称的header，则全部替换
         *
         * @param name  属性名称
         * @param value 属性值
         * @return 构造器
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * 添加带有{@code name}和{@code value}的header，
         * 对于像“Cookie”这样的多值标头，最好使用此方法
         *
         * @param name  属性名称
         * @param value 属性值
         * @return 构造器
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * 删除header所有名为{@code name}的属性
         *
         * @param name 属性名称
         * @return 构造器
         */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * 删除此生成器上的所有headers并添加{@code headers}
         *
         * @param headers 请求头信息
         * @return 构造器
         */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) return removeHeader(Header.CACHE_CONTROL);
            return header(Header.CACHE_CONTROL, value);
        }

        public Builder get() {
            return method(Http.GET, null);
        }

        public Builder head() {
            return method(Http.HEAD, null);
        }

        public Builder post(RequestBody body) {
            return method(Http.POST, body);
        }

        public Builder delete(RequestBody body) {
            return method(Http.DELETE, body);
        }

        public Builder delete() {
            return delete(RequestBody.create(null, Normal.EMPTY_BYTE_ARRAY));
        }

        public Builder put(RequestBody body) {
            return method(Http.PUT, body);
        }

        public Builder patch(RequestBody body) {
            return method(Http.PATCH, body);
        }

        public Builder method(String method, RequestBody body) {
            if (method == null) throw new NullPointerException("method == null");
            if (method.length() == 0) throw new IllegalArgumentException("method.length() == 0");
            if (body != null && !HttpMethod.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        public Builder tag(Object tag) {
            return tag(Object.class, tag);
        }

        public <T> Builder tag(Class<? super T> type, T tag) {
            if (type == null) throw new NullPointerException("type == null");

            if (tag == null) {
                tags.remove(type);
            } else {
                if (tags.isEmpty()) tags = new LinkedHashMap<>();
                tags.put(type, type.cast(tag));
            }

            return this;
        }

        public Request build() {
            if (url == null) throw new IllegalStateException("url == null");
            return new Request(this);
        }
    }

}
