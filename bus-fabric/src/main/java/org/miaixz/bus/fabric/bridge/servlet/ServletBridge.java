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

import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.bridge.Ingress;
import org.miaixz.bus.fabric.bridge.Translator;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;

/**
 * Servlet-style bridge translator that maps external ingresses to HTTP request snapshots without executing HTTP chains.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ServletBridge implements Translator<HttpRequest> {

    /**
     * Closed state reserved for lifecycle integration.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a servlet bridge.
     */
    private ServletBridge() {
        this.closed = new AtomicBoolean();
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
     * Returns whether an ingress can be translated as servlet input.
     *
     * @param ingress ingress
     * @return true when supported
     */
    public boolean supports(final Ingress ingress) {
        require(ingress, "Ingress");
        final Object servlet = ingress.attributes().get("servlet");
        final Object type = ingress.attributes().get("type");
        return Boolean.TRUE.equals(servlet) || "servlet".equals(type) || !ingress.method().isBlank();
    }

    /**
     * Translates an ingress into an HTTP request snapshot.
     *
     * @param ingress ingress
     * @return HTTP request
     */
    @Override
    public HttpRequest translate(final Ingress ingress) {
        require(ingress, "Ingress");
        final Headers headers = ingress.headers();
        final String method = ingress.method().isBlank() ? "GET" : ingress.method();
        final String path = ingress.path();
        final String host = headers.get("Host") == null ? "localhost" : headers.get("Host");
        final MediaType media = headers.get("Content-Type") == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE
                : MediaType.parse(headers.get("Content-Type"));
        final Payload payload = ingress.payload();
        return HttpRequest.builder().method(method(method)).url(UnoUrl.parse("http://" + host + path)).headers(headers)
                .body(HttpBody.of(payload, media)).tag(ingress).build();
    }

    /**
     * Converts an ingress into an HTTP request snapshot.
     *
     * @param ingress ingress
     * @return HTTP request
     */
    public HttpRequest toRequest(final Ingress ingress) {
        return translate(ingress);
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

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
