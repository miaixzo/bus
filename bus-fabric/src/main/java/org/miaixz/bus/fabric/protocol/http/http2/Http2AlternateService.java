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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * HTTP/2 ALTSVC payload value.
 *
 * @param origin UTF-8 origin field, empty only for stream-scoped ALTSVC frames
 * @param value  UTF-8 Alt-Svc field content
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2AlternateService(String origin, String value) {

    /**
     * Creates an ALTSVC payload value.
     *
     * @param origin ALTSVC origin field
     * @param value  Alt-Svc field value
     * @throws ValidateException if either field is {@code null} or the encoded origin exceeds 65535 bytes
     */
    public Http2AlternateService {
        origin = Assert.notNull(origin, () -> new ValidateException("Invalid HTTP/2 alternate service metadata"));
        value = Assert.notNull(value, () -> new ValidateException("Invalid HTTP/2 alternate service metadata"));
        if (ByteString.encodeUtf8(origin).size() > Normal._65535) {
            throw new ValidateException("Invalid HTTP/2 alternate service metadata");
        }
    }

    /**
     * Creates an ALTSVC value.
     *
     * @param origin UTF-8 ALTSVC origin field
     * @param value  Alt-Svc field value
     * @return immutable alternate-service value without stream-context validation
     * @throws ValidateException if either field is {@code null} or the encoded origin exceeds 65535 bytes
     */
    public static Http2AlternateService of(final String origin, final String value) {
        return new Http2AlternateService(origin, value);
    }

    /**
     * Decodes an ALTSVC frame payload.
     *
     * @param payload  immutable ALTSVC payload bytes including the origin-length prefix
     * @param streamId non-negative frame stream identifier used to validate origin presence
     * @return decoded and stream-context-validated alternate service
     * @throws ProtocolException if the payload layout, stream identifier, or origin context is invalid
     */
    static Http2AlternateService decode(final ByteString payload, final int streamId) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ProtocolException("Invalid HTTP/2 ALTSVC payload"));
        if (checkedPayload.size() < Normal._2) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC payload");
        }
        final int originLength = unsignedShort(checkedPayload);
        if (originLength > checkedPayload.size() - Normal._2) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC origin length");
        }
        final int valueOffset = Normal._2 + originLength;
        final ByteString originBytes = checkedPayload.substring(Normal._2, valueOffset);
        final ByteString valueBytes = checkedPayload.substring(valueOffset);
        return fromBytes(originBytes, valueBytes, streamId);
    }

    /**
     * Creates an ALTSVC value from decoded byte fields.
     *
     * @param originBytes UTF-8 bytes for the origin field
     * @param valueBytes  remaining UTF-8 Alt-Svc field bytes
     * @param streamId    frame stream id
     * @return decoded value after stream-context validation
     */
    private static Http2AlternateService fromBytes(
            final ByteString originBytes,
            final ByteString valueBytes,
            final int streamId) {
        final Http2AlternateService service = new Http2AlternateService(originBytes.utf8(), valueBytes.utf8());
        validateStreamContext(streamId, service);
        return service;
    }

    /**
     * Decodes an ALTSVC frame payload from a core buffer.
     *
     * @param payload  source buffer inspected without consumption
     * @param streamId non-negative frame stream identifier used to validate origin presence
     * @return decoded and stream-context-validated alternate service
     * @throws ProtocolException if the payload layout, stream identifier, or origin context is invalid
     */
    static Http2AlternateService decode(final Buffer payload, final int streamId) {
        final Buffer checkedPayload = Assert
                .notNull(payload, () -> new ProtocolException("Invalid HTTP/2 ALTSVC payload"));
        final long payloadSize = checkedPayload.size();
        if (payloadSize < Normal._2) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC payload");
        }
        final int originLength = unsignedShort(checkedPayload);
        if (originLength > payloadSize - Normal._2) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC origin length");
        }
        final long valueOffset = Normal._2 + (long) originLength;
        return fromBytes(
                readByteString(checkedPayload, Normal._2, originLength),
                readByteString(checkedPayload, valueOffset, payloadSize - valueOffset),
                streamId);
    }

    /**
     * Encodes this value as immutable ALTSVC payload bytes.
     *
     * @return immutable bytes containing the two-byte origin length, origin, and Alt-Svc field content
     */
    public ByteString encodeBytes() {
        final ByteString originBytes = ByteString.encodeUtf8(origin);
        final ByteString valueBytes = ByteString.encodeUtf8(value);
        final Buffer payload = new Buffer();
        payload.writeShort(originBytes.size());
        payload.write(originBytes);
        payload.write(valueBytes);
        return payload.readByteString();
    }

    /**
     * Validates stream-specific ALTSVC origin rules.
     *
     * @param streamId non-negative connection ({@code 0}) or application stream identifier
     * @param service  decoded alternate-service fields to validate
     * @throws ProtocolException if service is null, the identifier is negative, or origin presence violates scope
     */
    static void validateStreamContext(final int streamId, final Http2AlternateService service) {
        final Http2AlternateService checkedService = Assert
                .notNull(service, () -> new ProtocolException("Invalid HTTP/2 ALTSVC payload"));
        if (streamId < Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 ALTSVC stream id");
        }
        if (streamId == Normal._0 && checkedService.origin().isEmpty()) {
            throw new ProtocolException("HTTP/2 ALTSVC origin is required on the connection stream");
        }
        if (streamId > Normal._0 && !checkedService.origin().isEmpty()) {
            throw new ProtocolException("HTTP/2 ALTSVC origin must be empty on a stream frame");
        }
    }

    /**
     * Reads the big-endian unsigned origin length from immutable bytes.
     *
     * @param payload immutable ALTSVC bytes containing at least the two-byte prefix
     * @return unsigned length
     */
    private static int unsignedShort(final ByteString payload) {
        return ((payload.getByte(Normal._0) & Builder.UNSIGNED_BYTE_MASK) << Normal._8)
                | (payload.getByte(Normal._1) & Builder.UNSIGNED_BYTE_MASK);
    }

    /**
     * Reads the big-endian unsigned origin length from a buffer without consuming it.
     *
     * @param payload buffer containing at least the two-byte prefix
     * @return unsigned length
     */
    private static int unsignedShort(final Buffer payload) {
        return ((payload.getByte(Normal._0) & Builder.UNSIGNED_BYTE_MASK) << Normal._8)
                | (payload.getByte(Normal._1) & Builder.UNSIGNED_BYTE_MASK);
    }

    /**
     * Reads an immutable byte slice without consuming the source buffer.
     *
     * @param payload   source buffer retained unchanged
     * @param offset    slice offset
     * @param byteCount slice byte count
     * @return immutable copy of the requested byte range
     */
    private static ByteString readByteString(final Buffer payload, final long offset, final long byteCount) {
        final Buffer view = new Buffer();
        payload.copyTo(view, offset, byteCount);
        return view.readByteString();
    }

}
