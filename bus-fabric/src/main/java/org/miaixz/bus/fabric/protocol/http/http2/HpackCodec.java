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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * HPACK header block codec with static and dynamic table support.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HpackCodec {

    /**
     * Static table names.
     */
    private static final String[] STATIC_NAMES = { ":authority", ":method", ":method", ":path", ":path", ":scheme",
            ":scheme", ":status", ":status", ":status", ":status", ":status", ":status", ":status", "accept-charset",
            "accept-encoding", "accept-language", "accept-ranges", "accept", "access-control-allow-origin", "age",
            "allow", "authorization", "cache-control", "content-disposition", "content-encoding", "content-language",
            "content-length", "content-location", "content-range", "content-type", "cookie", "date", "etag", "expect",
            "expires", "from", "host", "if-match", "if-modified-since", "if-none-match", "if-range",
            "if-unmodified-since", "last-modified", "link", "location", "max-forwards", "proxy-authenticate",
            "proxy-authorization", "range", "referer", "refresh", "retry-after", "server", "set-cookie",
            "strict-transport-security", "transfer-encoding", "user-agent", "vary", "via", "www-authenticate" };

    /**
     * Static table values.
     */
    private static final String[] STATIC_VALUES = { Normal.EMPTY, "GET", "POST", Symbol.SLASH, "/index.html", "http",
            "https", "200", "204", "206", "304", "400", "404", "500", Normal.EMPTY, "gzip, deflate", Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY };

    /**
     * First static name indexes.
     */
    private static final Map<String, Integer> STATIC_NAME_INDEX = staticNameIndex();

    /**
     * Default dynamic table size.
     */
    private static final int DEFAULT_TABLE_SIZE = 4_096;

    /**
     * Maximum dynamic table size accepted by this implementation.
     */
    static final int MAX_DYNAMIC_TABLE_SIZE = 65_536;

    /**
     * Default maximum compressed header block bytes.
     */
    static final int DEFAULT_MAX_HEADER_BLOCK_BYTES = 65_536;

    /**
     * Default maximum decompressed header list size.
     */
    static final int DEFAULT_MAX_HEADER_LIST_SIZE = 65_536;

    /**
     * Default maximum bytes for a single header name or value.
     */
    static final int DEFAULT_MAX_HEADER_FIELD_BYTES = 16_384;

    /**
     * HPACK entry overhead.
     */
    private static final int ENTRY_OVERHEAD = 32;

    /**
     * Maximum integer prefix shift.
     */
    private static final int MAX_SHIFT = 28;

    /**
     * Dynamic table.
     */
    private final ArrayList<Http2Header> dynamicTable;

    /**
     * Dynamic table size.
     */
    private int tableSize;

    /**
     * Maximum dynamic table size accepted from HPACK updates.
     */
    private int maxTableSize;

    /**
     * Current dynamic table bytes.
     */
    private int tableBytes;

    /**
     * Maximum compressed header block bytes.
     */
    private int maxHeaderBlockBytes;

    /**
     * Maximum decompressed header list bytes.
     */
    private int maxHeaderListSize;

    /**
     * Maximum bytes for a single decoded header name or value.
     */
    private int maxHeaderFieldBytes;

    /**
     * Creates a codec.
     */
    public HpackCodec() {
        this.dynamicTable = new ArrayList<>();
        this.tableSize = DEFAULT_TABLE_SIZE;
        this.maxTableSize = DEFAULT_TABLE_SIZE;
        this.maxHeaderBlockBytes = DEFAULT_MAX_HEADER_BLOCK_BYTES;
        this.maxHeaderListSize = DEFAULT_MAX_HEADER_LIST_SIZE;
        this.maxHeaderFieldBytes = DEFAULT_MAX_HEADER_FIELD_BYTES;
    }

    /**
     * Encodes headers.
     *
     * @param headers headers
     * @return encoded buffer
     */
    public ByteBuffer encode(final List<Http2Header> headers) {
        if (headers == null) {
            throw new ValidateException("HTTP/2 headers must not contain null values");
        }
        int headerListBytes = 0;
        for (final Http2Header header : headers) {
            if (header == null) {
                throw new ValidateException("HTTP/2 headers must not contain null values");
            }
            headerListBytes = enforceHeaderBudget(headerListBytes, header);
        }
        validatePseudoOrder(headers);
        final ByteWriter output = new ByteWriter(Math.max(32, headers.size() << 4));
        for (final Http2Header header : headers) {
            final int exact = exactIndex(header);
            if (exact > 0) {
                writeInteger(output, exact, 0x80, 7);
                continue;
            }
            final int name = nameIndex(header.name());
            if (name > 0) {
                writeInteger(output, name, 0x40, 6);
                writeString(output, header.value());
            } else {
                writeInteger(output, 0, 0x40, 6);
                writeString(output, header.name());
                writeString(output, header.value());
            }
            insert(header);
        }
        return output.buffer();
    }

    /**
     * Decodes headers.
     *
     * @param source source
     * @return headers
     */
    public List<Http2Header> decode(final ByteBuffer source) {
        if (source == null) {
            throw new ValidateException("HPACK source must not be null");
        }
        final ByteBuffer input = source.asReadOnlyBuffer();
        if (input.remaining() > maxHeaderBlockBytes) {
            throw new ProtocolException("HPACK header block exceeds max size");
        }
        final ArrayList<Http2Header> headers = new ArrayList<>();
        int headerListBytes = 0;
        while (input.hasRemaining()) {
            final int first = input.get(input.position()) & 0xff;
            if ((first & 0x80) != 0) {
                headerListBytes = addHeader(headers, headerListBytes, indexed(input));
            } else if ((first & 0x40) != 0) {
                final Http2Header header = literal(input, 6);
                headerListBytes = addHeader(headers, headerListBytes, header);
                insert(header);
            } else if ((first & 0x20) != 0) {
                final int size = readInteger(input, 5);
                updateTableSize(size);
            } else {
                headerListBytes = addHeader(headers, headerListBytes, literal(input, 4));
            }
        }
        validatePseudoOrder(headers);
        return List.copyOf(headers);
    }

    /**
     * Sets the dynamic table size.
     *
     * @param size size
     */
    public void tableSize(final int size) {
        if (size < 0 || size > maxTableSize) {
            throw new ValidateException("HPACK table size must be between 0 and " + maxTableSize);
        }
        this.tableSize = size;
        evict();
    }

    /**
     * Returns the dynamic table size.
     *
     * @return size
     */
    public int tableSize() {
        return tableSize;
    }

    /**
     * Sets the maximum dynamic table size accepted from peer updates.
     *
     * @param size maximum size
     */
    public void maxTableSize(final int size) {
        if (size < 0 || size > MAX_DYNAMIC_TABLE_SIZE) {
            throw new ValidateException("HPACK maximum table size must be between 0 and " + MAX_DYNAMIC_TABLE_SIZE);
        }
        this.maxTableSize = size;
        if (tableSize > size) {
            tableSize = size;
            evict();
        }
    }

    /**
     * Returns the maximum dynamic table size.
     *
     * @return maximum table size
     */
    public int maxTableSize() {
        return maxTableSize;
    }

    /**
     * Sets maximum compressed header block bytes.
     *
     * @param size maximum bytes
     */
    public void maxHeaderBlockBytes(final int size) {
        if (size <= 0) {
            throw new ValidateException("HPACK max header block bytes must be positive");
        }
        this.maxHeaderBlockBytes = size;
    }

    /**
     * Sets maximum decompressed header list bytes.
     *
     * @param size maximum bytes
     */
    public void maxHeaderListSize(final int size) {
        if (size < 0) {
            throw new ValidateException("HPACK max header list size must be non-negative");
        }
        this.maxHeaderListSize = size;
    }

    /**
     * Returns maximum decompressed header list bytes.
     *
     * @return max header list size
     */
    public int maxHeaderListSize() {
        return maxHeaderListSize;
    }

    /**
     * Sets maximum bytes for one decoded header name or value.
     *
     * @param size maximum bytes
     */
    public void maxHeaderFieldBytes(final int size) {
        if (size <= 0) {
            throw new ValidateException("HPACK max header field bytes must be positive");
        }
        this.maxHeaderFieldBytes = size;
    }

    /**
     * Returns current dynamic table bytes.
     *
     * @return dynamic table bytes
     */
    int tableBytes() {
        return tableBytes;
    }

    /**
     * Returns current dynamic table entry count.
     *
     * @return dynamic table entry count
     */
    int dynamicTableLength() {
        return dynamicTable.size();
    }

    /**
     * Resets codec state.
     */
    public void reset() {
        dynamicTable.clear();
        tableBytes = 0;
        tableSize = DEFAULT_TABLE_SIZE;
        maxTableSize = DEFAULT_TABLE_SIZE;
        maxHeaderBlockBytes = DEFAULT_MAX_HEADER_BLOCK_BYTES;
        maxHeaderListSize = DEFAULT_MAX_HEADER_LIST_SIZE;
        maxHeaderFieldBytes = DEFAULT_MAX_HEADER_FIELD_BYTES;
    }

    /**
     * Decodes an indexed header.
     *
     * @param input input
     * @return header
     */
    private Http2Header indexed(final ByteBuffer input) {
        return header(readInteger(input, 7));
    }

    /**
     * Decodes a literal header.
     *
     * @param input      input
     * @param prefixBits prefix bits
     * @return header
     */
    private Http2Header literal(final ByteBuffer input, final int prefixBits) {
        final int index = readInteger(input, prefixBits);
        final String name = index == 0 ? readString(input, maxHeaderFieldBytes) : name(index);
        final String value = readString(input, maxHeaderFieldBytes);
        return Http2Header.of(name, value);
    }

    /**
     * Applies an HPACK dynamic table size update from a header block.
     *
     * @param size requested size
     */
    private void updateTableSize(final int size) {
        try {
            tableSize(size);
        } catch (final ValidateException e) {
            throw new ProtocolException("HPACK table size update exceeds maximum", e);
        }
    }

    /**
     * Adds a decoded header and enforces decompressed header-list budgets.
     *
     * @param headers         decoded headers
     * @param headerListBytes current header list bytes
     * @param header          new header
     * @return updated header list bytes
     */
    private int addHeader(final List<Http2Header> headers, final int headerListBytes, final Http2Header header) {
        final int next = enforceHeaderBudget(headerListBytes, header);
        headers.add(header);
        return next;
    }

    /**
     * Enforces decompressed header-list budgets.
     *
     * @param headerListBytes current header list bytes
     * @param header          header
     * @return updated header list bytes
     */
    private int enforceHeaderBudget(final int headerListBytes, final Http2Header header) {
        final int nameBytes = utf8Length(header.name());
        final int valueBytes = utf8Length(header.value());
        if (nameBytes > maxHeaderFieldBytes || valueBytes > maxHeaderFieldBytes) {
            throw new ProtocolException("HPACK header field exceeds max size");
        }
        final int fieldBytes = safeAdd(safeAdd(nameBytes, valueBytes), ENTRY_OVERHEAD);
        final int next = safeAdd(headerListBytes, fieldBytes);
        if (next > maxHeaderListSize) {
            throw new ProtocolException("HPACK header list exceeds max size");
        }
        return next;
    }

    /**
     * Returns an indexed header.
     *
     * @param index index
     * @return header
     */
    private Http2Header header(final int index) {
        if (index <= 0) {
            throw new ProtocolException("HPACK index must be positive");
        }
        if (index <= STATIC_NAMES.length) {
            return Http2Header.of(STATIC_NAMES[index - 1], STATIC_VALUES[index - 1]);
        }
        final int dynamic = index - STATIC_NAMES.length - 1;
        if (dynamic < 0 || dynamic >= dynamicTable.size()) {
            throw new ProtocolException("HPACK index is out of range");
        }
        return dynamicTable.get(dynamic);
    }

    /**
     * Returns an indexed name.
     *
     * @param index index
     * @return name
     */
    private String name(final int index) {
        if (index <= 0) {
            throw new ProtocolException("HPACK name index must be positive");
        }
        if (index <= STATIC_NAMES.length) {
            return STATIC_NAMES[index - 1];
        }
        final int dynamic = index - STATIC_NAMES.length - 1;
        if (dynamic < 0 || dynamic >= dynamicTable.size()) {
            throw new ProtocolException("HPACK name index is out of range");
        }
        return dynamicTable.get(dynamic).name();
    }

    /**
     * Finds an exact table index.
     *
     * @param header header
     * @return index or zero
     */
    private int exactIndex(final Http2Header header) {
        final int exact = staticExactIndex(header.name(), header.value());
        if (exact > 0) {
            return exact;
        }
        for (int i = 0; i < dynamicTable.size(); i++) {
            final Http2Header current = dynamicTable.get(i);
            if (current.name().equals(header.name()) && current.value().equals(header.value())) {
                return STATIC_NAMES.length + i + 1;
            }
        }
        return 0;
    }

    /**
     * Finds a name table index.
     *
     * @param name name
     * @return index or zero
     */
    private int nameIndex(final String name) {
        final Integer index = STATIC_NAME_INDEX.get(name);
        if (index != null) {
            return index;
        }
        for (int i = 0; i < dynamicTable.size(); i++) {
            if (dynamicTable.get(i).name().equals(name)) {
                return STATIC_NAMES.length + i + 1;
            }
        }
        return 0;
    }

    /**
     * Inserts a dynamic table entry.
     *
     * @param header header
     */
    private void insert(final Http2Header header) {
        final int size = size(header);
        if (size > tableSize) {
            dynamicTable.clear();
            tableBytes = 0;
            return;
        }
        dynamicTable.add(0, header);
        tableBytes += size;
        evict();
    }

    /**
     * Evicts dynamic entries to fit the configured size.
     */
    private void evict() {
        while (tableBytes > tableSize && !dynamicTable.isEmpty()) {
            final Http2Header removed = dynamicTable.remove(dynamicTable.size() - 1);
            tableBytes -= size(removed);
        }
    }

    /**
     * Calculates entry size.
     *
     * @param header header
     * @return size
     */
    private static int size(final Http2Header header) {
        return utf8Length(header.name()) + utf8Length(header.value()) + ENTRY_OVERHEAD;
    }

    /**
     * Writes a string without Huffman coding.
     *
     * @param output output
     * @param value  value
     */
    private static void writeString(final ByteWriter output, final String value) {
        final byte[] bytes = value.getBytes(Charset.UTF_8);
        writeInteger(output, bytes.length, 0, 7);
        output.write(bytes);
    }

    /**
     * Reads a string.
     *
     * @param input input
     * @return value
     */
    private static String readString(final ByteBuffer input, final int maxBytes) {
        if (!input.hasRemaining()) {
            throw new ProtocolException("Truncated HPACK string");
        }
        final boolean huffman = (input.get(input.position()) & 0x80) != 0;
        final int length = readInteger(input, 7);
        if (!huffman && length > maxBytes) {
            throw new ProtocolException("HPACK string exceeds max size");
        }
        if (input.remaining() < length) {
            throw new ProtocolException("Truncated HPACK string bytes");
        }
        final byte[] bytes = new byte[length];
        input.get(bytes);
        if (huffman) {
            return new String(decodeHuffman(bytes, maxBytes), Charset.UTF_8);
        }
        return new String(bytes, Charset.UTF_8);
    }

    /**
     * Safely adds positive byte counts.
     *
     * @param left  left value
     * @param right right value
     * @return sum
     */
    private static int safeAdd(final int left, final int right) {
        final long sum = (long) left + right;
        if (sum > Integer.MAX_VALUE) {
            throw new ProtocolException("HPACK header size overflow");
        }
        return (int) sum;
    }

    /**
     * Writes an HPACK integer.
     *
     * @param output     output
     * @param value      value
     * @param prefixMask prefix mask
     * @param prefixBits prefix bits
     */
    private static void writeInteger(
            final ByteWriter output,
            final int value,
            final int prefixMask,
            final int prefixBits) {
        final int maxPrefix = (1 << prefixBits) - 1;
        if (value < maxPrefix) {
            output.write(prefixMask | value);
            return;
        }
        output.write(prefixMask | maxPrefix);
        int remaining = value - maxPrefix;
        while (remaining >= 128) {
            output.write((remaining & 0x7f) | 0x80);
            remaining >>>= 7;
        }
        output.write(remaining);
    }

    /**
     * Reads an HPACK integer.
     *
     * @param input      input
     * @param prefixBits prefix bits
     * @return integer
     */
    private static int readInteger(final ByteBuffer input, final int prefixBits) {
        if (!input.hasRemaining()) {
            throw new ProtocolException("Truncated HPACK integer");
        }
        final int first = input.get() & 0xff;
        final int maxPrefix = (1 << prefixBits) - 1;
        int value = first & maxPrefix;
        if (value < maxPrefix) {
            return value;
        }
        int shift = 0;
        while (input.hasRemaining()) {
            final int next = input.get() & 0xff;
            value += (next & 0x7f) << shift;
            if ((next & 0x80) == 0) {
                return value;
            }
            shift += 7;
            if (shift > MAX_SHIFT) {
                throw new ProtocolException("HPACK integer overflow");
            }
        }
        throw new ProtocolException("Truncated HPACK integer continuation");
    }

    /**
     * Validates pseudo-header order.
     *
     * @param headers headers
     */
    private static void validatePseudoOrder(final List<Http2Header> headers) {
        boolean regular = false;
        for (final Http2Header header : headers) {
            if (header.pseudo()) {
                if (regular) {
                    throw new ProtocolException("HTTP/2 pseudo headers must precede regular headers");
                }
            } else {
                regular = true;
            }
        }
    }

    /**
     * Builds the static name index map.
     *
     * @return name index map
     */
    private static Map<String, Integer> staticNameIndex() {
        final HashMap<String, Integer> indexes = new HashMap<>(STATIC_NAMES.length << 1);
        for (int i = 0; i < STATIC_NAMES.length; i++) {
            indexes.putIfAbsent(STATIC_NAMES[i], i + 1);
        }
        return Map.copyOf(indexes);
    }

    /**
     * Finds a static exact index without allocating lookup keys.
     *
     * @param name  name
     * @param value value
     * @return index or zero
     */
    private static int staticExactIndex(final String name, final String value) {
        return switch (name) {
            case ":method" -> switch (value) {
                case "GET" -> 2;
                case "POST" -> 3;
                default -> 0;
            };
            case ":path" -> switch (value) {
                case "/" -> 4;
                case "/index.html" -> 5;
                default -> 0;
            };
            case ":scheme" -> switch (value) {
                case "http" -> 6;
                case "https" -> 7;
                default -> 0;
            };
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
            default -> emptyStaticIndex(name, value);
        };
    }

    /**
     * Finds an exact empty-value static index.
     *
     * @param name  name
     * @param value value
     * @return index or zero
     */
    private static int emptyStaticIndex(final String name, final String value) {
        if (!value.isEmpty()) {
            return 0;
        }
        final Integer index = STATIC_NAME_INDEX.get(name);
        return index != null && STATIC_VALUES[index - 1].isEmpty() ? index : 0;
    }

    /**
     * Returns UTF-8 byte length without allocating bytes.
     *
     * @param value value
     * @return byte length
     */
    private static int utf8Length(final String value) {
        int length = 0;
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < 0x80) {
                length++;
            } else if (current < 0x800) {
                length += 2;
            } else if (Character.isHighSurrogate(current) && i + 1 < value.length()
                    && Character.isLowSurrogate(value.charAt(i + 1))) {
                length += 4;
                i++;
            } else {
                length += 3;
            }
        }
        return length;
    }

    /**
     * Maximum HPACK Huffman code length.
     */
    private static final int MAX_HUFFMAN_CODE_BITS = 30;

    /**
     * Maximum legal Huffman EOS padding bits.
     */
    private static final int MAX_HUFFMAN_PADDING_BITS = 7;

    /**
     * HPACK Huffman codes for byte symbols.
     */
    private static final int[] HUFFMAN_CODES = { 0x1ff8, 0x7fffd8, 0xfffffe2, 0xfffffe3, 0xfffffe4, 0xfffffe5,
            0xfffffe6, 0xfffffe7, 0xfffffe8, 0xffffea, 0x3ffffffc, 0xfffffe9, 0xfffffea, 0x3ffffffd, 0xfffffeb,
            0xfffffec, 0xfffffed, 0xfffffee, 0xfffffef, 0xffffff0, 0xffffff1, 0xffffff2, 0x3ffffffe, 0xffffff3,
            0xffffff4, 0xffffff5, 0xffffff6, 0xffffff7, 0xffffff8, 0xffffff9, 0xffffffa, 0xffffffb, 0x14, 0x3f8, 0x3f9,
            0xffa, 0x1ff9, 0x15, 0xf8, 0x7fa, 0x3fa, 0x3fb, 0xf9, 0x7fb, 0xfa, 0x16, 0x17, 0x18, 0x0, 0x1, 0x2, 0x19,
            0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x5c, 0xfb, 0x7ffc, 0x20, 0xffb, 0x3fc, 0x1ffa, 0x21, 0x5d, 0x5e, 0x5f,
            0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71,
            0x72, 0xfc, 0x73, 0xfd, 0x1ffb, 0x7fff0, 0x1ffc, 0x3ffc, 0x22, 0x7ffd, 0x3, 0x23, 0x4, 0x24, 0x5, 0x25,
            0x26, 0x27, 0x6, 0x74, 0x75, 0x28, 0x29, 0x2a, 0x7, 0x2b, 0x76, 0x2c, 0x8, 0x9, 0x2d, 0x77, 0x78, 0x79,
            0x7a, 0x7b, 0x7ffe, 0x7fc, 0x3ffd, 0x1ffd, 0xffffffc, 0xfffe6, 0x3fffd2, 0xfffe7, 0xfffe8, 0x3fffd3,
            0x3fffd4, 0x3fffd5, 0x7fffd9, 0x3fffd6, 0x7fffda, 0x7fffdb, 0x7fffdc, 0x7fffdd, 0x7fffde, 0xffffeb,
            0x7fffdf, 0xffffec, 0xffffed, 0x3fffd7, 0x7fffe0, 0xffffee, 0x7fffe1, 0x7fffe2, 0x7fffe3, 0x7fffe4,
            0x1fffdc, 0x3fffd8, 0x7fffe5, 0x3fffd9, 0x7fffe6, 0x7fffe7, 0xffffef, 0x3fffda, 0x1fffdd, 0xfffe9, 0x3fffdb,
            0x3fffdc, 0x7fffe8, 0x7fffe9, 0x1fffde, 0x7fffea, 0x3fffdd, 0x3fffde, 0xfffff0, 0x1fffdf, 0x3fffdf,
            0x7fffeb, 0x7fffec, 0x1fffe0, 0x1fffe1, 0x3fffe0, 0x1fffe2, 0x7fffed, 0x3fffe1, 0x7fffee, 0x7fffef, 0xfffea,
            0x3fffe2, 0x3fffe3, 0x3fffe4, 0x7ffff0, 0x3fffe5, 0x3fffe6, 0x7ffff1, 0x3ffffe0, 0x3ffffe1, 0xfffeb,
            0x7fff1, 0x3fffe7, 0x7ffff2, 0x3fffe8, 0x1ffffec, 0x3ffffe2, 0x3ffffe3, 0x3ffffe4, 0x7ffffde, 0x7ffffdf,
            0x3ffffe5, 0xfffff1, 0x1ffffed, 0x7fff2, 0x1fffe3, 0x3ffffe6, 0x7ffffe0, 0x7ffffe1, 0x3ffffe7, 0x7ffffe2,
            0xfffff2, 0x1fffe4, 0x1fffe5, 0x3ffffe8, 0x3ffffe9, 0xffffffd, 0x7ffffe3, 0x7ffffe4, 0x7ffffe5, 0xfffec,
            0xfffff3, 0xfffed, 0x1fffe6, 0x3fffe9, 0x1fffe7, 0x1fffe8, 0x7ffff3, 0x3fffea, 0x3fffeb, 0x1ffffee,
            0x1ffffef, 0xfffff4, 0xfffff5, 0x3ffffea, 0x7ffff4, 0x3ffffeb, 0x7ffffe6, 0x3ffffec, 0x3ffffed, 0x7ffffe7,
            0x7ffffe8, 0x7ffffe9, 0x7ffffea, 0x7ffffeb, 0xffffffe, 0x7ffffec, 0x7ffffed, 0x7ffffee, 0x7ffffef,
            0x7fffff0, 0x3ffffee };

    /**
     * HPACK Huffman code lengths for byte symbols.
     */
    private static final byte[] HUFFMAN_LENGTHS = { 13, 23, 28, 28, 28, 28, 28, 28, 28, 24, 30, 28, 28, 30, 28, 28, 28,
            28, 28, 28, 28, 28, 30, 28, 28, 28, 28, 28, 28, 28, 28, 28, 6, 10, 10, 12, 13, 6, 8, 11, 10, 10, 8, 11, 8,
            6, 6, 6, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 8, 15, 6, 12, 10, 13, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 8, 13, 19, 13, 14, 6, 15, 5, 6, 5, 6, 5, 6, 6, 6, 5, 7, 7, 6, 6, 6, 5, 6, 7,
            6, 5, 5, 6, 7, 7, 7, 7, 7, 15, 11, 14, 13, 28, 20, 22, 20, 20, 22, 22, 22, 23, 22, 23, 23, 23, 23, 23, 24,
            23, 24, 24, 22, 23, 24, 23, 23, 23, 23, 21, 22, 23, 22, 23, 23, 24, 22, 21, 20, 22, 22, 23, 23, 21, 23, 22,
            22, 24, 21, 22, 23, 23, 21, 21, 22, 21, 23, 22, 23, 23, 20, 22, 22, 22, 23, 22, 22, 23, 26, 26, 20, 19, 22,
            23, 22, 25, 26, 26, 26, 27, 27, 26, 24, 25, 19, 21, 26, 27, 27, 26, 27, 24, 21, 21, 26, 26, 28, 27, 27, 27,
            20, 24, 20, 21, 22, 21, 21, 23, 22, 22, 25, 25, 24, 24, 26, 23, 26, 27, 26, 26, 27, 27, 27, 27, 27, 28, 27,
            27, 27, 27, 27, 26 };

    /**
     * Huffman decoding root.
     */
    private static final HuffmanNode HUFFMAN_ROOT = huffmanRoot();

    /**
     * Decodes an HPACK Huffman string with a decoded-byte budget.
     *
     * @param bytes    encoded bytes
     * @param maxBytes decoded byte budget
     * @return decoded bytes
     */
    private static byte[] decodeHuffman(final byte[] bytes, final int maxBytes) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(bytes.length, maxBytes));
        HuffmanNode node = HUFFMAN_ROOT;
        int residualBits = 0;
        int residualValue = 0;
        for (final byte current : bytes) {
            final int value = current & 0xff;
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                final int bit = (value >>> bitIndex) & 1;
                node = bit == 0 ? node.zero : node.one;
                if (node == null) {
                    throw new ProtocolException("Invalid HPACK Huffman code");
                }
                residualBits++;
                residualValue = (residualValue << 1) | bit;
                if (node.symbol >= 0) {
                    if (output.size() >= maxBytes) {
                        throw new ProtocolException("HPACK Huffman string exceeds max size");
                    }
                    output.write(node.symbol);
                    node = HUFFMAN_ROOT;
                    residualBits = 0;
                    residualValue = 0;
                } else if (residualBits > MAX_HUFFMAN_CODE_BITS) {
                    throw new ProtocolException("Invalid HPACK Huffman EOS");
                }
            }
        }
        if (node != HUFFMAN_ROOT) {
            if (residualBits > MAX_HUFFMAN_PADDING_BITS || residualValue != (1 << residualBits) - 1) {
                throw new ProtocolException("Invalid HPACK Huffman padding");
            }
        }
        return output.toByteArray();
    }

    /**
     * Builds the HPACK Huffman decoding tree.
     *
     * @return root node
     */
    private static HuffmanNode huffmanRoot() {
        final HuffmanNode root = new HuffmanNode();
        for (int symbol = 0; symbol < HUFFMAN_CODES.length; symbol++) {
            HuffmanNode node = root;
            final int code = HUFFMAN_CODES[symbol];
            final int length = HUFFMAN_LENGTHS[symbol];
            for (int bit = length - 1; bit >= 0; bit--) {
                if (node.symbol >= 0) {
                    throw new IllegalStateException("Invalid HPACK Huffman prefix table");
                }
                if (((code >>> bit) & 1) == 0) {
                    if (node.zero == null) {
                        node.zero = new HuffmanNode();
                    }
                    node = node.zero;
                } else {
                    if (node.one == null) {
                        node.one = new HuffmanNode();
                    }
                    node = node.one;
                }
            }
            if (node.symbol >= 0 || node.zero != null || node.one != null) {
                throw new IllegalStateException("Invalid HPACK Huffman table");
            }
            node.symbol = symbol;
        }
        return root;
    }

    /**
     * Binary Huffman node.
     */
    private static final class HuffmanNode {

        /**
         * Zero-bit child.
         */
        private HuffmanNode zero;

        /**
         * One-bit child.
         */
        private HuffmanNode one;

        /**
         * Decoded symbol, or -1 for an internal node.
         */
        private int symbol = -1;

    }

    /**
     * Lightweight byte writer for HPACK blocks.
     */
    private static final class ByteWriter {

        /**
         * Buffer bytes.
         */
        private byte[] bytes;

        /**
         * Written byte count.
         */
        private int length;

        /**
         * Creates a writer.
         *
         * @param capacity initial capacity
         */
        private ByteWriter(final int capacity) {
            this.bytes = new byte[capacity];
        }

        /**
         * Writes one byte.
         *
         * @param value byte value
         */
        private void write(final int value) {
            ensure(1);
            bytes[length++] = (byte) value;
        }

        /**
         * Writes bytes.
         *
         * @param value bytes
         */
        private void write(final byte[] value) {
            ensure(value.length);
            System.arraycopy(value, 0, bytes, length, value.length);
            length += value.length;
        }

        /**
         * Returns a readable buffer.
         *
         * @return buffer
         */
        private ByteBuffer buffer() {
            return ByteBuffer.wrap(bytes, 0, length).slice().asReadOnlyBuffer();
        }

        /**
         * Ensures writable capacity.
         *
         * @param extra extra byte count
         */
        private void ensure(final int extra) {
            final int required = length + extra;
            if (required <= bytes.length) {
                return;
            }
            int capacity = bytes.length << 1;
            while (capacity < required) {
                capacity <<= 1;
            }
            final byte[] copy = new byte[capacity];
            System.arraycopy(bytes, 0, copy, 0, length);
            bytes = copy;
        }

    }

}
