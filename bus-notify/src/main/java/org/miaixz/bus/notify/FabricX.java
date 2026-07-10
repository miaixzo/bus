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
package org.miaixz.bus.notify;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;

/**
 * Fabric-backed HTTP support for notification providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class FabricX {

    /**
     * Shared Fabric context for notification HTTP calls.
     */
    private static final org.miaixz.bus.fabric.Context CONTEXT = org.miaixz.bus.fabric.Context.create()
            .withOptions(Options.of("timeout", Timeout.of(Duration.ofSeconds(30))));

    /**
     * Form media used by notification requests.
     */
    private static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED_TYPE;

    /**
     * Sends a GET request.
     *
     * @param url URL
     * @return response body
     */
    public static String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url   URL
     * @param query query parameters
     * @return response body
     */
    public static String get(final String url, final Map<String, ?> query) {
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
    public static String get(final String url, final Map<String, ?> query, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).get(url);
        apply(builder::query, query);
        apply(builder::header, headers);
        return builder.executeText();
    }

    /**
     * Sends a POST request.
     *
     * @param url  URL
     * @param body body values
     * @return response body
     */
    public static String post(final String url, final Map<String, ?> body) {
        return post(url, body, null);
    }

    /**
     * Sends a POST request.
     *
     * @param url     URL
     * @param body    body values
     * @param headers headers
     * @return response body
     */
    public static String post(final String url, final Map<String, ?> body, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).post(url);
        apply(builder::header, headers);
        if (body == null || body.isEmpty()) {
            builder.body(Payload.empty(), media(headers, FORM));
        } else if (json(headers)) {
            builder.body(JsonKit.toJsonString(body), media(headers, MediaType.APPLICATION_JSON_TYPE));
        } else {
            apply(builder::form, body);
        }
        return builder.executeText();
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url         URL
     * @param body        body
     * @param headers     headers
     * @param contentType content type
     * @return response body
     */
    public static String post(
            final String url,
            final String body,
            final Map<String, ?> headers,
            final String contentType) {
        final var builder = Fabric.http(CONTEXT).post(url).body(body == null ? "" : body, media(contentType));
        apply(builder::header, headers);
        return builder.executeText();
    }

    /**
     * Applies values to a Fabric HTTP builder.
     *
     * @param consumer value consumer
     * @param values   values
     */
    private static void apply(final BiConsumer<String, Object> consumer, final Map<String, ?> values) {
        if (values != null && !values.isEmpty()) {
            values.forEach((name, value) -> {
                if (name != null && value != null) {
                    consumer.accept(name, value);
                }
            });
        }
    }

    /**
     * Returns whether headers request JSON body semantics.
     *
     * @param headers headers
     * @return true when JSON
     */
    private static boolean json(final Map<String, ?> headers) {
        final Object contentType = header(headers, HTTP.CONTENT_TYPE);
        return contentType != null
                && contentType.toString().toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON);
    }

    /**
     * Resolves request media.
     *
     * @param headers  headers
     * @param fallback fallback media
     * @return media type
     */
    private static MediaType media(final Map<String, ?> headers, final MediaType fallback) {
        final Object contentType = header(headers, HTTP.CONTENT_TYPE);
        return contentType == null ? fallback : media(contentType.toString());
    }

    /**
     * Reads a case-insensitive header.
     *
     * @param headers headers
     * @param name    header name
     * @return header value
     */
    private static Object header(final Map<String, ?> headers, final String name) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (final Map.Entry<String, ?> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
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
