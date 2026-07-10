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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
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
     * The context containing configuration details for the storage provider.
     */
    protected Context context;

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param headers headers
     * @return response
     */
    protected HttpResult get(final String url, final Header... headers) {
        return execute(HTTP.GET, url, null, null, headers);
    }

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param headers headers
     * @return response
     */
    protected HttpResult get(final String url, final Map<String, ?> headers) {
        return execute(HTTP.GET, url, null, null, headers);
    }

    /**
     * Sends a HEAD request.
     *
     * @param url     URL
     * @param headers headers
     * @return response
     */
    protected HttpResult head(final String url, final Header... headers) {
        return execute(HTTP.HEAD, url, null, null, headers);
    }

    /**
     * Sends a POST request with an empty body.
     *
     * @param url     URL
     * @param headers headers
     * @return response
     */
    protected HttpResult post(final String url, final Header... headers) {
        return execute(HTTP.POST, url, Payload.empty(), MediaType.APPLICATION_OCTET_STREAM_TYPE, headers);
    }

    /**
     * Sends a POST request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return response
     */
    protected HttpResult post(final String url, final String data, final String contentType, final Header... headers) {
        return execute(
                HTTP.POST,
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
     * @return response
     */
    protected HttpResult post(final String url, final byte[] data, final String contentType, final Header... headers) {
        return execute(HTTP.POST, url, Payload.of(data == null ? new byte[0] : data), media(contentType), headers);
    }

    /**
     * Sends a PUT request with a byte body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return response
     */
    protected HttpResult put(final String url, final byte[] data, final String contentType, final Header... headers) {
        return execute(HTTP.PUT, url, Payload.of(data == null ? new byte[0] : data), media(contentType), headers);
    }

    /**
     * Sends a PUT request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return response
     */
    protected HttpResult put(final String url, final String data, final String contentType, final Header... headers) {
        return execute(HTTP.PUT, url, Payload.of(data == null ? "" : data, Charset.UTF_8), media(contentType), headers);
    }

    /**
     * Sends a PATCH request with a string body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @param headers     headers
     * @return response
     */
    protected HttpResult patch(final String url, final String data, final String contentType, final Header... headers) {
        return execute(
                HTTP.PATCH,
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
     * @return response
     */
    protected HttpResult delete(final String url, final Header... headers) {
        return execute(HTTP.DELETE, url, null, null, headers);
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
     * @return response
     */
    private HttpResult execute(
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
        return new HttpResult(builder.execute());
    }

    /**
     * Executes the HTTP request with Fabric.
     *
     * @param method  HTTP method
     * @param url     URL
     * @param body    body payload
     * @param media   body media
     * @param headers headers
     * @return response
     */
    private HttpResult execute(
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
        return new HttpResult(builder.execute());
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
        return org.miaixz.bus.fabric.Context.create().withOptions(Options.of("timeout", timeout));
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
     * Storage HTTP response.
     */
    protected static final class HttpResult implements AutoCloseable {

        /**
         * Fabric HTTP response.
         */
        private final HttpResponse response;

        /**
         * Creates a storage response.
         *
         * @param response response
         */
        private HttpResult(final HttpResponse response) {
            this.response = response;
        }

        /**
         * Returns whether the response is successful.
         *
         * @return true if status code is 2xx
         */
        public boolean isSuccessful() {
            return response.successful();
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
         * Returns the HTTP status text.
         *
         * @return status text
         */
        public String message() {
            return response.message();
        }

        /**
         * Returns the last value for a header.
         *
         * @param name header name
         * @return header value
         */
        public String header(final String name) {
            return response.headers().get(name);
        }

        /**
         * Returns the last value for a header, or the default value when absent.
         *
         * @param name         header name
         * @param defaultValue default value
         * @return header value
         */
        public String header(final String name, final String defaultValue) {
            final String value = header(name);
            return value == null ? defaultValue : value;
        }

        /**
         * Returns response headers.
         *
         * @return headers
         */
        public Map<String, java.util.List<String>> headers() {
            return response.headers().asMap();
        }

        /**
         * Returns the response body.
         *
         * @return body
         */
        public Body body() {
            return new Body(response);
        }

        /**
         * Opens a stream that closes the response when the stream is closed.
         *
         * @return managed response stream
         */
        public InputStream stream() {
            return new ResponseInputStream(response.body().stream(), this);
        }

        @Override
        public void close() {
            response.close();
        }

    }

    /**
     * Storage HTTP response body.
     */
    protected static final class Body {

        /**
         * Fabric HTTP response.
         */
        private final HttpResponse response;

        /**
         * Creates a response body wrapper.
         *
         * @param response response
         */
        private Body(final HttpResponse response) {
            this.response = response;
        }

        /**
         * Returns response bytes.
         *
         * @return bytes
         */
        public byte[] bytes() {
            return response.body().bytes();
        }

        /**
         * Returns response text.
         *
         * @return response text
         */
        public String string() {
            return response.body().text(Charset.UTF_8);
        }

        /**
         * Opens the response body stream.
         *
         * @return body stream
         */
        public InputStream byteStream() {
            return response.body().stream();
        }

    }

    /**
     * Response body stream that closes the response with the stream.
     */
    private static final class ResponseInputStream extends FilterInputStream {

        /**
         * Response to close with the stream.
         */
        private final AutoCloseable response;

        /**
         * Creates a response input stream.
         *
         * @param input    input stream
         * @param response response
         */
        private ResponseInputStream(final InputStream input, final AutoCloseable response) {
            super(input);
            this.response = response;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                try {
                    response.close();
                } catch (final Exception e) {
                    if (e instanceof IOException io) {
                        throw io;
                    }
                    throw new IOException(e);
                }
            }
        }
    }

}
