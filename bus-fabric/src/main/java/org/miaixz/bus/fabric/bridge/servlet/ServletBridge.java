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
package org.miaixz.bus.fabric.bridge.servlet;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.bridge.Ingestion;
import org.miaixz.bus.fabric.bridge.Translator;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Servlet-style bridge translator that maps external ingestions to HTTP request snapshots without executing HTTP
 * chains.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ServletBridge implements Translator<HttpRequest> {

    /**
     * Creates a servlet bridge.
     */
    private ServletBridge() {
        // No initialization required.
    }

    /**
     * Creates a default servlet bridge.
     *
     * @return stateless servlet-ingestion translator
     */
    public static ServletBridge create() {
        return new ServletBridge();
    }

    /**
     * Returns whether an ingestion declares servlet metadata or provides any non-blank method.
     *
     * @param ingestion immutable external ingestion to inspect
     * @return {@code true} when attribute {@code servlet} is {@link Boolean#TRUE}, attribute {@code type} equals
     *         {@code servlet}, or the ingestion method is non-blank
     * @throws ValidateException if {@code ingestion} is {@code null}
     */
    public boolean supports(final Ingestion ingestion) {
        Assert.notNull(ingestion, () -> new ValidateException("Ingestion must not be null"));
        final Object servlet = ingestion.attributes().get("servlet");
        final Object type = ingestion.attributes().get("type");
        return Boolean.TRUE.equals(servlet) || "servlet".equals(type) || !ingestion.method().isBlank();
    }

    /**
     * Translates an ingestion into an HTTP request without executing an HTTP chain.
     * <p>
     * A blank method defaults to GET, a missing Host header defaults to localhost, and a missing Content-Type defaults
     * to {@code application/octet-stream}.
     * </p>
     *
     * @param ingestion external path, method, headers, payload, and tag source
     * @return immutable HTTP request targeting the synthesized {@code http://host/path} URL
     * @throws ValidateException if {@code ingestion} is {@code null} or its method is unsupported
     */
    @Override
    public HttpRequest translate(final Ingestion ingestion) {
        Assert.notNull(ingestion, () -> new ValidateException("Ingestion must not be null"));
        final Headers headers = ingestion.headers();
        final String method = ingestion.method().isBlank() ? Http.Method.GET.value() : ingestion.method();
        final String path = ingestion.path();
        final String host = headers.get(Http.Header.HOST) == null ? Protocol.HOST_LOCAL : headers.get(Http.Header.HOST);
        final MediaType media = headers.get(Http.Header.CONTENT_TYPE) == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE
                : MediaType.parse(headers.get(Http.Header.CONTENT_TYPE));
        final Payload payload = ingestion.payload();
        return HttpRequest.builder().method(method(method)).url(UnoUrl.parse(Protocol.HTTP_PREFIX + host + path))
                .headers(headers).body(PayloadBody.of(payload, media)).tag(ingestion).build();
    }

    /**
     * Translates an ingestion through the servlet-facing convenience alias.
     *
     * @param ingestion external ingestion passed unchanged to {@link #translate(Ingestion)}
     * @return same request snapshot produced by {@link #translate(Ingestion)}
     * @throws ValidateException if {@code ingestion} is {@code null} or its method is unsupported
     */
    public HttpRequest toRequest(final Ingestion ingestion) {
        return translate(ingestion);
    }

    /**
     * Resolves a canonical HTTP method.
     *
     * @param value external method text to resolve
     * @return canonical HTTP method enum value
     * @throws ValidateException if the method is not recognized
     */
    private static Http.Method method(final String value) {
        try {
            return Http.Method.of(value);
        } catch (final IllegalArgumentException e) {
            throw new ValidateException("Unsupported HTTP method: " + value, e);
        }
    }

}
