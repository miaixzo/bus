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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * HTTP/2 ALTSVC payload value.
 *
 * @param origin alternate-service origin
 * @param value  Alt-Svc field value
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2AlternateService(String origin, String value) {

    /**
     * Origin length field size.
     */
    private static final int ORIGIN_LENGTH_BYTES = Short.BYTES;

    /**
     * Creates an ALTSVC payload value.
     */
    public Http2AlternateService {
        if (origin == null || value == null || origin.getBytes(StandardCharsets.UTF_8).length > 0xffff) {
            throw new ValidateException("Invalid HTTP/2 alternate service metadata");
        }
    }

    /**
     * Creates an ALTSVC value.
     *
     * @param origin origin
     * @param value  Alt-Svc field value
     * @return alternate service
     */
    public static Http2AlternateService of(final String origin, final String value) {
        return new Http2AlternateService(origin, value);
    }

    /**
     * Decodes an ALTSVC frame payload.
     *
     * @param payload  payload
     * @param streamId frame stream id
     * @return alternate service
     */
    static Http2AlternateService decode(final ByteBuffer payload, final int streamId) {
        if (payload == null || payload.remaining() < ORIGIN_LENGTH_BYTES) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC payload");
        }
        final ByteBuffer view = payload.asReadOnlyBuffer();
        final int originLength = view.getShort() & 0xffff;
        if (originLength > view.remaining()) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC origin length");
        }
        final byte[] originBytes = new byte[originLength];
        view.get(originBytes);
        final byte[] valueBytes = new byte[view.remaining()];
        view.get(valueBytes);
        final Http2AlternateService service = new Http2AlternateService(new String(originBytes, StandardCharsets.UTF_8),
                new String(valueBytes, StandardCharsets.UTF_8));
        validateStreamContext(streamId, service);
        return service;
    }

    /**
     * Encodes this value as an ALTSVC payload.
     *
     * @return payload
     */
    public ByteBuffer encode() {
        final byte[] originBytes = origin.getBytes(StandardCharsets.UTF_8);
        final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer payload = ByteBuffer.allocate(ORIGIN_LENGTH_BYTES + originBytes.length + valueBytes.length);
        payload.putShort((short) originBytes.length);
        payload.put(originBytes);
        payload.put(valueBytes);
        payload.flip();
        return payload.asReadOnlyBuffer();
    }

    /**
     * Validates stream-specific ALTSVC origin rules.
     *
     * @param streamId stream id
     * @param service  service value
     */
    static void validateStreamContext(final int streamId, final Http2AlternateService service) {
        if (streamId < 0) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC stream id");
        }
        if (streamId == 0 && service.origin().isEmpty()) {
            throw new ProtocolException("HTTP/2 ALTSVC origin is required on the connection stream");
        }
        if (streamId > 0 && !service.origin().isEmpty()) {
            throw new ProtocolException("HTTP/2 ALTSVC origin must be empty on a stream frame");
        }
    }

}
