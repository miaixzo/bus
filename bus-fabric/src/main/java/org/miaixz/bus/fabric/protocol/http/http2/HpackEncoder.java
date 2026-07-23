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

import java.util.List;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;

/**
 * Writer-thread-owned HPACK encoder that writes directly into a frame batch buffer.
 *
 * <p>
 * Common request pseudo fields use RFC 7541 static indexes. Other fields are emitted directly as indexed or literal
 * representations without an intermediate encoded byte array or header list.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HpackEncoder {

    /**
     * Writer-direction dynamic table used for exact and name-only indexing.
     */
    private final HpackDynamicTable dynamicTable;

    /**
     * Maximum RFC 7541 decompressed header-list size accepted for one encoded block.
     */
    private int maxHeaderListBytes = 64 * 1024;

    /**
     * Creates an encoder with a 64-KiB allocation maximum and 4096-byte effective table capacity.
     */
    HpackEncoder() {
        dynamicTable = new HpackDynamicTable(64 * 1024);
        dynamicTable.capacityBytes(4096);
    }

    /**
     * Encodes a request without first constructing a complete {@link Http2Header} list.
     *
     * @param method    request method
     * @param scheme    request scheme
     * @param authority request authority
     * @param path      request path
     * @param headers   regular request headers
     * @param target    frame-batch destination
     */
    void encodeRequest(
            final String method,
            final String scheme,
            final String authority,
            final String path,
            final Headers headers,
            final Buffer target) {
        require(target, "HPACK output");
        int bytes = 0;
        bytes = encodeField(":method", method, false, target, bytes);
        bytes = encodeField(":scheme", scheme, false, target, bytes);
        bytes = encodeField(":authority", authority, false, target, bytes);
        bytes = encodeField(":path", path, false, target, bytes);
        if (headers != null) {
            for (int index = 0; index < headers.size(); index++) {
                final String name = headers.name(index);
                if (!hopByHop(name) && !name.startsWith(":")) {
                    bytes = encodeField(name, headers.value(index), sensitive(name), target, bytes);
                }
            }
        }
    }

    /**
     * Encodes an already ordered compatibility header sequence directly into a target.
     *
     * @param headers ordered fields
     * @param target  destination buffer
     */
    void encode(final List<Http2Header> headers, final Buffer target) {
        require(headers, "HPACK headers");
        require(target, "HPACK output");
        int bytes = 0;
        boolean regularSeen = false;
        for (final Http2Header header : headers) {
            require(header, "HPACK header");
            if (header.pseudo() && regularSeen) {
                throw new ProtocolException("HTTP/2 pseudo-header follows a regular header");
            }
            regularSeen |= !header.pseudo();
            bytes = encodeField(header.name(), header.value(), header.sensitive(), target, bytes);
        }
    }

    /**
     * Sets the effective dynamic-table capacity.
     *
     * @param bytes capacity bytes
     */
    void tableSize(final int bytes) {
        dynamicTable.capacityBytes(bytes);
    }

    /**
     * Sets the decompressed header-list budget.
     *
     * @param bytes positive budget
     */
    void maxHeaderListBytes(final int bytes) {
        if (bytes <= 0) {
            throw new ValidateException("HPACK header-list limit must be positive");
        }
        maxHeaderListBytes = bytes;
    }

    /**
     * Encodes one header and returns the updated list-size total.
     *
     * @param name      field name
     * @param value     field value
     * @param sensitive whether indexing is prohibited
     * @param target    destination
     * @param total     preceding list bytes
     * @return updated list bytes
     */
    private int encodeField(
            final String name,
            final String value,
            final boolean sensitive,
            final Buffer target,
            final int total) {
        if (name == null || value == null) {
            throw new ValidateException("HPACK field name and value must not be null");
        }
        final long next = (long) total + 32L + utf8Length(name) + utf8Length(value);
        if (next > maxHeaderListBytes) {
            throw new ProtocolException("HPACK header list exceeds maximum");
        }
        final int staticExact = staticExactIndex(name, value);
        if (!sensitive && staticExact != 0) {
            writeInteger(target, staticExact, 0x80, 7);
            return (int) next;
        }
        final int dynamicExact = dynamicTable.findExact(name, value);
        if (!sensitive && dynamicExact != 0) {
            writeInteger(target, Builder.HTTP2_HPACK_STATIC_TABLE_ENTRIES + dynamicExact, 0x80, 7);
            return (int) next;
        }
        int nameIndex = staticNameIndex(name);
        if (nameIndex == 0) {
            final int dynamicName = dynamicTable.findName(name);
            nameIndex = dynamicName == 0 ? 0 : Builder.HTTP2_HPACK_STATIC_TABLE_ENTRIES + dynamicName;
        }
        final int prefix = sensitive ? 0x10 : 0x40;
        final int prefixBits = sensitive ? 4 : 6;
        writeInteger(target, nameIndex, prefix, prefixBits);
        if (nameIndex == 0) {
            writeString(target, name);
        }
        writeString(target, value);
        if (!sensitive) {
            dynamicTable.insert(Http2Header.of(name, value));
        }
        return (int) next;
    }

    /**
     * Writes an HPACK integer.
     *
     * @param target     destination
     * @param value      non-negative value
     * @param firstBits  representation bits
     * @param prefixBits integer prefix width
     */
    private static void writeInteger(final Buffer target, final int value, final int firstBits, final int prefixBits) {
        final int maximum = (1 << prefixBits) - 1;
        if (value < maximum) {
            target.writeByte(firstBits | value);
            return;
        }
        target.writeByte(firstBits | maximum);
        int remaining = value - maximum;
        while (remaining >= 128) {
            target.writeByte((remaining & 0x7f) | 0x80);
            remaining >>>= 7;
        }
        target.writeByte(remaining);
    }

    /**
     * Writes an un-Huffman-coded UTF-8 string directly. Huffman is optional in HPACK and is deliberately skipped when
     * no proven size reduction is available.
     *
     * @param target destination
     * @param value  field text
     */
    private static void writeString(final Buffer target, final String value) {
        final int length = utf8Length(value);
        writeInteger(target, length, 0, 7);
        target.writeUtf8(value);
    }

    /**
     * Returns an exact RFC 7541 static-table index for selected common fields.
     *
     * @param name  field name to match case-sensitively
     * @param value field value to match case-sensitively
     * @return one-based static-table index, or zero when the exact pair is not in the selected set
     */
    private static int staticExactIndex(final String name, final String value) {
        return switch (name) {
            case ":method" -> "GET".equals(value) ? 2 : "POST".equals(value) ? 3 : 0;
            case ":path" -> "/".equals(value) ? 4 : "/index.html".equals(value) ? 5 : 0;
            case ":scheme" -> "http".equals(value) ? 6 : "https".equals(value) ? 7 : 0;
            case ":status" -> switch (value) {
                case "200" -> 8;
                case "204" -> 9;
                case "206" -> 10;
                case "304" -> 11;
                case "400" -> 12;
                case "404" -> 13;
                case "500" -> 14;
                default -> 0;
            };
            case "accept-encoding" -> "gzip, deflate".equals(value) ? 16 : 0;
            default -> 0;
        };
    }

    /**
     * Returns the first RFC 7541 static-table index for a selected field name.
     *
     * @param name field name to match case-sensitively
     * @return one-based static-table name index, or zero when the name is not in the selected set
     */
    private static int staticNameIndex(final String name) {
        return switch (name) {
            case ":authority" -> 1;
            case ":method" -> 2;
            case ":path" -> 4;
            case ":scheme" -> 6;
            case ":status" -> 8;
            case "accept-encoding" -> 16;
            case "authorization" -> 23;
            case "cache-control" -> 24;
            case "content-length" -> 28;
            case "content-type" -> 31;
            case "cookie" -> 32;
            case "date" -> 33;
            case "host" -> 38;
            case "location" -> 46;
            case "server" -> 54;
            case "set-cookie" -> 55;
            case "user-agent" -> 58;
            default -> 0;
        };
    }

    /**
     * Returns whether a credential- or cookie-bearing field must use the never-indexed representation.
     *
     * @param name lowercase field name to classify
     * @return true for authorization, proxy authorization, cookie, or set-cookie
     */
    private static boolean sensitive(final String name) {
        return "authorization".equals(name) || "proxy-authorization".equals(name) || "cookie".equals(name)
                || "set-cookie".equals(name);
    }

    /**
     * Returns whether an HTTP/1 connection-specific field must be omitted from an HTTP/2 request.
     *
     * @param name field name to classify without regard to ASCII case
     * @return true for connection, keep-alive, proxy-connection, transfer-encoding, or upgrade
     */
    private static boolean hopByHop(final String name) {
        return "connection".equalsIgnoreCase(name) || "keep-alive".equalsIgnoreCase(name)
                || "proxy-connection".equalsIgnoreCase(name) || "transfer-encoding".equalsIgnoreCase(name)
                || "upgrade".equalsIgnoreCase(name);
    }

    /**
     * Counts UTF-8 bytes without allocating an encoded array.
     *
     * @param value text whose encoded length is required
     * @return number of bytes produced by the method's UTF-8 code-point accounting
     */
    private static int utf8Length(final String value) {
        int length = 0;
        for (int index = 0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current < 0x80) {
                length++;
            } else if (current < 0x800) {
                length += 2;
            } else if (Character.isHighSurrogate(current) && index + 1 < value.length()
                    && Character.isLowSurrogate(value.charAt(index + 1))) {
                length += 4;
                index++;
            } else {
                length += 3;
            }
        }
        return length;
    }

    /**
     * Returns a validated non-null reference.
     *
     * @param value reference to validate
     * @param label reference label included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String label) {
        if (value == null) {
            throw new ValidateException(label + " must not be null");
        }
        return value;
    }

}
