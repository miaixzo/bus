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
package org.miaixz.bus.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Fabric-backed HTTP support for storage providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class FabricX {

    /**
     * Shared Fabric contexts keyed by the public storage timeout policy to preserve connection pooling.
     */
    private static final ConcurrentHashMap<Timeout, org.miaixz.bus.fabric.Context> FABRIC_CONTEXTS =
            new ConcurrentHashMap<>();

    /**
     * The context containing configuration details for the storage provider.
     */
    protected Context context;

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param headers headers
     * @return storage response
     */
    protected Response get(final String url, final Header... headers) {
        return execute(Http.Method.GET.value(), url, null, null, headers);
    }

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param headers headers
     * @return storage response
     */
    protected Response get(final String url, final Map<String, ?> headers) {
        return execute(Http.Method.GET.value(), url, null, null, headers);
    }

    /**
     * Sends a HEAD request.
     *
     * @param url     URL
     * @param headers headers
     * @return storage response
     */
    protected Response head(final String url, final Header... headers) {
        return execute(Http.Method.HEAD.value(), url, null, null, headers);
    }

    /**
     * Sends a POST request with an empty body.
     *
     * @param url     URL
     * @param headers headers
     * @return storage response
     */
    protected Response post(final String url, final Header... headers) {
        return execute(Http.Method.POST.value(), url, Payload.empty(), MediaType.APPLICATION_OCTET_STREAM_TYPE, headers);
    }

    /**
     * Sends a POST request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return storage response
     */
    protected Response post(final String url, final String data, final String contentType, final Header... headers) {
        return execute(
                Http.Method.POST.value(),
                url,
                Payload.of(data == null ? "" : data, Charset.UTF_8),
                media(contentType),
                headers);
    }

    /**
     * Sends a POST request with a byte body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return storage response
     */
    protected Response post(final String url, final byte[] data, final String contentType, final Header... headers) {
        return execute(Http.Method.POST.value(), url, Payload.of(data == null ? new byte[0] : data), media(contentType), headers);
    }

    /**
     * Sends a PUT request with a byte body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return storage response
     */
    protected Response put(final String url, final byte[] data, final String contentType, final Header... headers) {
        return execute(Http.Method.PUT.value(), url, Payload.of(data == null ? new byte[0] : data), media(contentType), headers);
    }

    /**
     * Sends a PUT request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return storage response
     */
    protected Response put(final String url, final String data, final String contentType, final Header... headers) {
        return execute(Http.Method.PUT.value(), url, Payload.of(data == null ? "" : data, Charset.UTF_8), media(contentType), headers);
    }

    /**
     * Sends a PATCH request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return storage response
     */
    protected Response patch(final String url, final String data, final String contentType, final Header... headers) {
        return execute(
                Http.Method.PATCH.value(),
                url,
                Payload.of(data == null ? "" : data, Charset.UTF_8),
                media(contentType),
                headers);
    }

    /**
     * Sends a DELETE request.
     *
     * @param url     URL
     * @param headers headers
     * @return storage response
     */
    protected Response delete(final String url, final Header... headers) {
        return execute(Http.Method.DELETE.value(), url, null, null, headers);
    }

    /**
     * Creates a header pair.
     *
     * @param name  header name
     * @param value header value
     * @return header pair
     */
    protected Header header(final String name, final Object value) {
        return new Header(name, value);
    }

    /**
     * Executes the HTTP request with Fabric.
     *
     * @param method  HTTP method
     * @param url     URL
     * @param body    body payload
     * @param media   body media
     * @param headers headers
     * @return storage response
     */
    private Response execute(
            final String method,
            final String url,
            final Payload body,
            final MediaType media,
            final Header... headers) {
        final var builder = Fabric.http(fabricContext()).method(method).url(url);
        apply(headers, builder);
        if (body != null) {
            builder.body(body, media == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : media);
        }
        return new Response(builder.execute());
    }

    /**
     * Executes the HTTP request with Fabric.
     *
     * @param method  HTTP method
     * @param url     URL
     * @param body    body payload
     * @param media   body media
     * @param headers headers
     * @return storage response
     */
    private Response execute(
            final String method,
            final String url,
            final Payload body,
            final MediaType media,
            final Map<String, ?> headers) {
        final var builder = Fabric.http(fabricContext()).method(method).url(url);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        if (body != null) {
            builder.body(body, media == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : media);
        }
        return new Response(builder.execute());
    }

    /**
     * Applies headers to a Fabric HTTP builder.
     *
     * @param headers headers
     * @param builder builder
     */
    private static void apply(final Header[] headers, final org.miaixz.bus.fabric.protocol.http.HttpX.Builder builder) {
        if (headers == null || headers.length == 0) {
            return;
        }
        for (final Header header : headers) {
            if (header != null && header.name != null && header.value != null) {
                builder.header(header.name, header.value);
            }
        }
    }

    /**
     * Builds the Fabric context for one storage HTTP call.
     *
     * @return Fabric context
     */
    private org.miaixz.bus.fabric.Context fabricContext() {
        final Timeout timeout = Timeout.builder()
                .connect(seconds(context == null ? 10 : context.getConnectTimeout(), 10))
                .read(seconds(context == null ? 30 : context.getReadTimeout(), 30))
                .write(seconds(context == null ? 30 : context.getWriteTimeout(), 30)).build();
        return FABRIC_CONTEXTS.computeIfAbsent(
                timeout,
                policy -> org.miaixz.bus.fabric.Context.builder()
                        .options(Options.of(org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT, policy))
                        .build());
    }

    /**
     * Parses a duration in seconds.
     *
     * @param value        configured value
     * @param defaultValue default value
     * @return duration
     */
    private static Duration seconds(final long value, final long defaultValue) {
        return Duration.ofSeconds(value <= 0 ? defaultValue : value);
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

    /**
     * Header pair.
     *
     * @param name  header name
     * @param value header value
     */
    protected record Header(String name, Object value) {
    }

    /**
     * Returns the last response header value.
     *
     * @param response storage response
     * @param name     header name
     * @return header value
     */
    protected static String header(final Response response, final String name) {
        return response.header(name);
    }

    /**
     * Returns the last response header value or the default value when absent.
     *
     * @param response     storage response
     * @param name         header name
     * @param defaultValue default value
     * @return header value
     */
    protected static String header(final Response response, final String name, final String defaultValue) {
        final String value = header(response, name);
        return value == null ? defaultValue : value;
    }

    /**
     * Opens the response body as an input stream and closes the response when the stream is closed.
     *
     * @param response storage response
     * @return response body stream
     */
    protected static InputStream stream(final Response response) {
        return response.stream();
    }

    /**
     * Storage response facade that keeps Fabric HTTP details inside {@link FabricX}.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    protected static final class Response implements AutoCloseable {

        /**
         * Current Fabric HTTP response.
         */
        private final HttpResponse response;

        /**
         * Creates a storage response facade.
         *
         * @param response current Fabric HTTP response
         */
        private Response(final HttpResponse response) {
            this.response = response;
        }

        /**
         * Returns the HTTP status code.
         *
         * @return status code
         */
        public int code() {
            return response.code();
        }

        /**
         * Returns whether the response status code is in the 2xx range.
         *
         * @return {@code true} when the response is successful
         */
        public boolean successful() {
            return response.successful();
        }

        /**
         * Reads the response body as UTF-8 text and closes the response.
         *
         * @return response body text
         */
        public String text() {
            return response.text();
        }

        /**
         * Reads the response body as bytes and closes the response.
         *
         * @return response body bytes
         */
        public byte[] bytes() {
            try {
                return response.bytes();
            } finally {
                close();
            }
        }

        /**
         * Returns the last response header value.
         *
         * @param name header name
         * @return header value
         */
        public String header(final String name) {
            return response.headers().get(name);
        }

        /**
         * Opens the response body as an input stream and closes the response when the stream is closed.
         *
         * @return response body stream
         */
        public InputStream stream() {
            try {
                return new FilterInputStream(IoKit.buffer(response.body().source()).inputStream()) {

                    /**
                     * Closes the body stream and its owning response.
                     *
                     * @throws IOException when stream closing fails
                     */
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            Response.this.close();
                        }
                    }

                };
            } catch (final RuntimeException e) {
                close();
                throw e;
            }
        }

        /**
         * Closes the underlying response.
         */
        @Override
        public void close() {
            response.close();
        }

    }

}
