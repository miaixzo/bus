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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.http.Httpd;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract base builder for creating HTTP requests. It provides a fluent API for setting common request properties
 * like URL, headers, parameters, and tags.
 *
 * @param <T> The type of the concrete builder implementation, for method chaining.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RequestBuilder<T extends RequestBuilder> {

    /**
     * The HTTP client instance.
     */
    protected Httpd httpd;

    /**
     * A unique identifier for the request.
     */
    protected String id;
    /**
     * The URL for the request.
     */
    protected String url;
    /**
     * An optional tag for the request, used for cancellation.
     */
    protected Object tag;

    /**
     * A map of request headers.
     */
    protected Map<String, String> headers;
    /**
     * A map of request parameters (e.g., query string or form data).
     */
    protected Map<String, String> params;
    /**
     * A map of pre-encoded request parameters.
     */
    protected Map<String, String> encoded;

    /**
     * Constructs a new RequestBuilder.
     *
     * @param httpd The {@link Httpd} client instance.
     */
    public RequestBuilder(Httpd httpd) {
        this.httpd = httpd;
        headers = new LinkedHashMap<>();
        params = new LinkedHashMap<>();
        encoded = new LinkedHashMap<>();
    }

    /**
     * Sets the unique identifier for this request.
     *
     * @param id The request ID.
     * @return This builder instance for chaining.
     */
    public T id(String id) {
        this.id = id;
        return (T) this;
    }

    /**
     * Sets the URL for this request.
     *
     * @param url The request URL.
     * @return This builder instance for chaining.
     */
    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    /**
     * Sets the tag for this request, which can be used to cancel it later.
     *
     * @param tag The request tag.
     * @return This builder instance for chaining.
     */
    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    /**
     * Replaces all existing headers with the given map of headers.
     *
     * @param headers A map of headers to set.
     * @return This builder instance for chaining.
     */
    public T addHeader(Map<String, String> headers) {
        this.headers = headers;
        return (T) this;
    }

    /**
     * Adds a single header to the request.
     *
     * @param key The header name.
     * @param val The header value.
     * @return This builder instance for chaining.
     */
    public T addHeader(String key, String val) {
        headers.put(key, val);
        return (T) this;
    }

    /**
     * Replaces all existing parameters with the given map of parameters.
     *
     * @param params A map of parameters to set.
     * @return This builder instance for chaining.
     */
    public T addParam(Map<String, String> params) {
        this.params = params;
        return (T) this;
    }

    /**
     * Adds a single parameter to the request.
     *
     * @param key The parameter name.
     * @param val The parameter value.
     * @return This builder instance for chaining.
     */
    public T addParam(String key, String val) {
        this.params.put(key, val);
        return (T) this;
    }

    /**
     * Adds all properties of a Plain Old Java Object (POJO) as parameters.
     *
     * @param object The object whose properties will be added as parameters.
     * @return This builder instance for chaining.
     */
    public T addParam(Object object) {
        if (null != object) {
            Map<String, Object> map = BeanKit.beanToMap(object);
            map.forEach((key, val) -> addParam(key, (String) val));
        }
        return (T) this;
    }

    /**
     * Replaces all existing pre-encoded parameters with the given map.
     *
     * @param params A map of pre-encoded parameters to set.
     * @return This builder instance for chaining.
     */
    public T addEncoded(Map<String, String> params) {
        this.encoded = params;
        return (T) this;
    }

    /**
     * Adds a single pre-encoded parameter to the request.
     *
     * @param key The pre-encoded parameter name.
     * @param val The pre-encoded parameter value.
     * @return This builder instance for chaining.
     */
    public T addEncoded(String key, String val) {
        this.encoded.put(key, val);
        return (T) this;
    }

    /**
     * Abstract method to be implemented by subclasses to build the final, executable request call.
     *
     * @return The constructed {@link RequestCall}.
     */
    public abstract RequestCall build();

}
