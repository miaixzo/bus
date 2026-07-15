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
import org.miaixz.bus.core.net.HTTP;
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
     * @return servlet bridge
     */
    public static ServletBridge create() {
        return new ServletBridge();
    }

    /**
     * Returns whether an ingestion can be translated as servlet input.
     *
     * @param ingestion ingestion
     * @return true when supported
     */
    public boolean supports(final Ingestion ingestion) {
        Assert.notNull(ingestion, () -> new ValidateException("Ingestion must not be null"));
        final Object servlet = ingestion.attributes().get("servlet");
        final Object type = ingestion.attributes().get("type");
        return Boolean.TRUE.equals(servlet) || "servlet".equals(type) || !ingestion.method().isBlank();
    }

    /**
     * Translates an ingestion into an HTTP request snapshot.
     *
     * @param ingestion ingestion
     * @return HTTP request
     */
    @Override
    public HttpRequest translate(final Ingestion ingestion) {
        Assert.notNull(ingestion, () -> new ValidateException("Ingestion must not be null"));
        final Headers headers = ingestion.headers();
        final String method = ingestion.method().isBlank() ? HTTP.GET : ingestion.method();
        final String path = ingestion.path();
        final String host = headers.get(HTTP.HOST) == null ? Protocol.HOST_LOCAL : headers.get(HTTP.HOST);
        final MediaType media = headers.get(HTTP.CONTENT_TYPE) == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE
                : MediaType.parse(headers.get(HTTP.CONTENT_TYPE));
        final Payload payload = ingestion.payload();
        return HttpRequest.builder().method(method(method)).url(UnoUrl.parse(Protocol.HTTP_PREFIX + host + path))
                .headers(headers).body(PayloadBody.of(payload, media)).tag(ingestion).build();
    }

    /**
     * Converts an ingestion into an HTTP request snapshot.
     *
     * @param ingestion ingestion
     * @return HTTP request
     */
    public HttpRequest toRequest(final Ingestion ingestion) {
        return translate(ingestion);
    }

    /**
     * Resolves a canonical HTTP method.
     *
     * @param value raw method
     * @return canonical method
     */
    private static HTTP.Method method(final String value) {
        try {
            return HTTP.Method.of(value);
        } catch (final IllegalArgumentException e) {
            throw new ValidateException("Unsupported HTTP method: " + value, e);
        }
    }

}
