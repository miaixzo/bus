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
package org.miaixz.bus.auth;

import java.time.Duration;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;

/**
 * Fabric-backed HTTP support for authorization providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class FabricX {

    /**
     * Shared Fabric context for authorization HTTP calls.
     */
    private static final org.miaixz.bus.fabric.Context CONTEXT = org.miaixz.bus.fabric.Context.create()
            .withOptions(Options.of("timeout", Timeout.of(Duration.ofSeconds(30))));

    /**
     * Form media used by authorization requests.
     */
    private static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED_TYPE;

    /**
     * Sends a GET request.
     *
     * @param url URL
     * @return response body
     */
    protected static String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url   URL
     * @param query query parameters
     * @return response body
     */
    protected static String get(final String url, final Map<String, ?> query) {
        return get(url, query, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param query   query parameters
     * @param headers headers
     * @return response body
     */
    protected static String get(final String url, final Map<String, ?> query, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).get(url);
        if (query != null && !query.isEmpty()) {
            query.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.query(name, value);
                }
            });
        }
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Sends a POST request with an empty form body.
     *
     * @param url URL
     * @return response body
     */
    protected static String post(final String url) {
        return Fabric.http(CONTEXT).post(url).body(Payload.empty(), FORM).executeText();
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url  URL
     * @param form form fields
     * @return response body
     */
    protected static String post(final String url, final Map<String, ?> form) {
        return post(url, form, null);
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url     URL
     * @param form    form fields
     * @param headers headers
     * @return response body
     */
    protected static String post(final String url, final Map<String, ?> form, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).post(url);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        if (form == null || form.isEmpty()) {
            builder.body(Payload.empty(), FORM);
        } else {
            form.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.form(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @return response body
     */
    protected static String post(final String url, final String data, final String contentType) {
        return post(url, data, null, contentType);
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url         URL
     * @param data        body
     * @param headers     headers
     * @param contentType content type
     * @return response body
     */
    protected static String post(
            final String url,
            final String data,
            final Map<String, ?> headers,
            final String contentType) {
        final var builder = Fabric.http(CONTEXT).post(url).body(data == null ? "" : data, media(contentType));
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Parses a valid content type.
     *
     * @param contentType content type
     * @return media type
     */
    private static MediaType media(final String contentType) {
        if (StringKit.isBlank(contentType) || StringKit.containsAny(contentType, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Content-Type must be non-blank and single-line");
        }
        return MediaType.parse(contentType);
    }

}
