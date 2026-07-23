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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;

/**
 * Reader-thread-owned HPACK decoder with static, Huffman and dynamic table support.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HpackDecoder {

    /**
     * Header names from the RFC 7541 static table, indexed from one externally.
     */
    private static final String[] STATIC_NAMES = { Http.Header.PSEUDO_AUTHORITY, Http.Header.PSEUDO_METHOD,
            Http.Header.PSEUDO_METHOD, Http.Header.PSEUDO_PATH, Http.Header.PSEUDO_PATH, Http.Header.PSEUDO_SCHEME,
            Http.Header.PSEUDO_SCHEME, Http.Header.PSEUDO_STATUS, Http.Header.PSEUDO_STATUS, Http.Header.PSEUDO_STATUS,
            Http.Header.PSEUDO_STATUS, Http.Header.PSEUDO_STATUS, Http.Header.PSEUDO_STATUS, Http.Header.PSEUDO_STATUS,
            "accept-charset", "accept-encoding", "accept-language", "accept-ranges", "accept",
            "access-control-allow-origin", "age", "allow", "authorization", "cache-control", "content-disposition",
            "content-encoding", "content-language", "content-length", "content-location", "content-range",
            "content-type", "cookie", "date", "etag", "expect", "expires", "from", Builder.HOST, "if-match",
            "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "last-modified", "link",
            "location", "max-forwards", "proxy-authenticate", "proxy-authorization", "range", "referer", "refresh",
            "retry-after", "server", "set-cookie", "strict-transport-security", "transfer-encoding", "user-agent",
            "vary", "via", "www-authenticate" };

    /**
     * Header values aligned with {@link #STATIC_NAMES} in the RFC 7541 static table.
     */
    private static final String[] STATIC_VALUES = { Normal.EMPTY, Http.Method.GET.value(), Http.Method.POST.value(),
            Symbol.SLASH, "/index.html", Protocol.HTTP.name, Protocol.HTTPS.name, Integer.toString(Http.Status.OK),
            Integer.toString(Http.Status.NO_CONTENT), Integer.toString(Http.Status.PARTIAL_CONTENT),
            Integer.toString(Http.Status.NOT_MODIFIED), Integer.toString(Http.Status.BAD_REQUEST),
            Integer.toString(Http.Status.NOT_FOUND), Integer.toString(Http.Status.INTERNAL_SERVER_ERROR), Normal.EMPTY,
            "gzip, deflate", Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY,
            Normal.EMPTY, Normal.EMPTY, Normal.EMPTY, Normal.EMPTY };

    /**
     * First one-based static-table index for each header name.
     */
    private static final Map<String, Integer> STATIC_NAME_INDEX = staticNameIndex();

    /**
     * Dynamic-table entries ordered from newest to oldest.
     */
    private final ArrayList<Http2Header> dynamicTable;

    /**
     * Latest monotonic insertion sequence for each exact dynamic-table header.
     */
    private final HashMap<Http2Header, Long> dynamicExactIndex;

    /**
     * Latest monotonic insertion sequence for each dynamic-table header name.
     */
    private final HashMap<String, Long> dynamicNameIndex;

    /**
     * Insertion sequences aligned by index with {@link #dynamicTable}.
     */
    private final ArrayList<Long> dynamicSequences;

    /**
     * Reusable encoder scratch owned by this connection-scoped codec.
     */
    private final ByteWriter writer;

    /**
     * Monotonically increasing sequence assigned to newly inserted dynamic entries.
     */
    private long dynamicSequence;

    /**
     * Effective dynamic-table capacity in bytes.
     */
    private int tableSize;

    /**
     * Maximum dynamic table size accepted from HPACK updates.
     */
    private int maxTableSize;

    /**
     * Current sum of HPACK sizes for dynamic-table entries.
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
     * Maximum fields in one decompressed block.
     */
    private int maxHeaderCount;

    /**
     * Creates a codec.
     */
    HpackDecoder() {
        this.dynamicTable = new ArrayList<>();
        this.dynamicExactIndex = new HashMap<>();
        this.dynamicNameIndex = new HashMap<>();
        this.dynamicSequences = new ArrayList<>();
        this.writer = new ByteWriter(Normal._128);
        this.tableSize = Normal._4096;
        this.maxTableSize = Normal._4096;
        this.maxHeaderBlockBytes = Builder.BYTES_64_KIB;
        this.maxHeaderListSize = Builder.BYTES_64_KIB;
        this.maxHeaderFieldBytes = Normal._16384;
        this.maxHeaderCount = 256;
    }

    /**
     * Encodes headers.
     *
     * @param headers ordered HTTP/2 header fields to encode
     * @return buffer containing the encoded HPACK header block
     */
    public Buffer encodeBuffer(final List<Http2Header> headers) {
        final List<Http2Header> checkedHeaders = Assert
                .notNull(headers, () -> new ValidateException("HTTP/2 headers must not contain null values"));
        if (checkedHeaders.size() > maxHeaderCount) {
            throw new ProtocolException("HPACK header field count exceeds maximum");
        }
        int headerListBytes = 0;
        for (final Http2Header header : checkedHeaders) {
            final Http2Header checkedHeader = Assert
                    .notNull(header, () -> new ValidateException("HTTP/2 headers must not contain null values"));
            headerListBytes = enforceHeaderBudget(headerListBytes, checkedHeader);
        }
        validatePseudoOrder(checkedHeaders);
        final ByteWriter output = writer.reset(Math.max(Normal._32, checkedHeaders.size() << Normal._4));
        for (final Http2Header header : checkedHeaders) {
            final int exact = exactIndex(header);
            if (exact > Normal._0 && !header.sensitive()) {
                writeInteger(output, exact, Normal._128, Normal._7);
                continue;
            }
            final int name = nameIndex(header.name());
            final boolean sensitive = header.sensitive();
            if (name > Normal._0) {
                writeInteger(output, name, sensitive ? Normal._16 : Normal._64, sensitive ? Normal._4 : Normal._6);
                writeString(output, header.value());
            } else {
                writeInteger(output, Normal._0, sensitive ? Normal._16 : Normal._64, sensitive ? Normal._4 : Normal._6);
                writeString(output, header.name());
                writeString(output, header.value());
            }
            if (!sensitive) {
                insert(header);
            }
        }
        return output.buffer();
    }

    /**
     * Decodes headers from a core buffer.
     *
     * @param source buffer containing an HPACK header block; consumed during decoding
     * @return immutable decoded HTTP/2 header list
     */
    public List<Http2Header> decode(final Buffer source) {
        final Buffer checkedSource = Assert
                .notNull(source, () -> new ValidateException("HPACK source must not be null"));
        if (checkedSource.size() > maxHeaderBlockBytes) {
            throw new ProtocolException("HPACK header block exceeds max size");
        }
        final ArrayList<Http2Header> headers = new ArrayList<>();
        int headerListBytes = 0;
        boolean fieldsStarted = false;
        int sizeUpdates = 0;
        while (checkedSource.size() > Normal._0) {
            final int first = checkedSource.getByte(Normal._0) & Builder.UNSIGNED_BYTE_MASK;
            if ((first & Normal._128) != 0) {
                headerListBytes = addHeader(headers, headerListBytes, indexed(checkedSource));
                fieldsStarted = true;
            } else if ((first & Normal._64) != 0) {
                final Http2Header header = literal(checkedSource, Normal._6);
                headerListBytes = addHeader(headers, headerListBytes, header);
                insert(header);
                fieldsStarted = true;
            } else if ((first & Normal._32) != 0) {
                if (fieldsStarted || ++sizeUpdates > Normal._2) {
                    throw new ProtocolException("HPACK table size update must precede header fields");
                }
                final int size = readInteger(checkedSource, Normal._5);
                updateTableSize(size);
            } else {
                headerListBytes = addHeader(headers, headerListBytes, literal(checkedSource, Normal._4));
                fieldsStarted = true;
            }
        }
        validatePseudoOrder(headers);
        return List.copyOf(headers);
    }

    /**
     * Sets the dynamic table size.
     *
     * @param size effective dynamic-table capacity in bytes
     */
    public void tableSize(final int size) {
        if (size < Normal._0 || size > maxTableSize) {
            throw new ValidateException("HPACK table size must be between 0 and " + maxTableSize);
        }
        this.tableSize = size;
        evict();
    }

    /**
     * Returns the dynamic table size.
     *
     * @return effective dynamic-table capacity in bytes
     */
    public int tableSize() {
        return tableSize;
    }

    /**
     * Sets the maximum dynamic table size accepted from peer updates.
     *
     * @param size maximum peer-advertised dynamic-table capacity in bytes
     */
    public void maxTableSize(final int size) {
        if (size < Normal._0 || size > Builder.BYTES_64_KIB) {
            throw new ValidateException("HPACK maximum table size must be between 0 and " + Builder.BYTES_64_KIB);
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
     * @return maximum accepted dynamic-table capacity in bytes
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
        if (size <= Normal._0) {
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
        if (size < Normal._0) {
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
        if (size <= Normal._0) {
            throw new ValidateException("HPACK max header field bytes must be positive");
        }
        this.maxHeaderFieldBytes = size;
    }

    /**
     * Sets the maximum number of decoded fields in one block.
     *
     * @param count maximum decoded header fields per block
     */
    public void maxHeaderCount(final int count) {
        if (count <= Normal._0 || count > 256) {
            throw new ValidateException("HPACK max header count must be between 1 and 256");
        }
        this.maxHeaderCount = count;
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
        clearDynamicTable();
        tableSize = Normal._4096;
        maxTableSize = Normal._4096;
        maxHeaderBlockBytes = Builder.BYTES_64_KIB;
        maxHeaderListSize = Builder.BYTES_64_KIB;
        maxHeaderFieldBytes = Normal._16384;
        maxHeaderCount = 256;
    }

    /**
     * Decodes an indexed header.
     *
     * @param input HPACK input positioned at an indexed representation
     * @return header resolved from the static or dynamic table
     */
    private Http2Header indexed(final Buffer input) {
        return header(readInteger(input, Normal._7));
    }

    /**
     * Decodes a literal header.
     *
     * @param input      HPACK input positioned at a literal representation
     * @param prefixBits number of low-order bits carrying the name index prefix
     * @return decoded literal header field
     */
    private Http2Header literal(final Buffer input, final int prefixBits) {
        final int index = readInteger(input, prefixBits);
        final String name = index == Normal._0 ? readString(input, maxHeaderFieldBytes) : name(index);
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
        if (headers.size() >= maxHeaderCount) {
            throw new ProtocolException("HPACK header field count exceeds maximum");
        }
        headers.add(header);
        return next;
    }

    /**
     * Enforces decompressed header-list budgets.
     *
     * @param headerListBytes current header list bytes
     * @param header          header whose HPACK field size is added
     * @return updated header list bytes
     */
    private int enforceHeaderBudget(final int headerListBytes, final Http2Header header) {
        final int nameBytes = utf8Length(header.name());
        final int valueBytes = utf8Length(header.value());
        if (nameBytes > maxHeaderFieldBytes || valueBytes > maxHeaderFieldBytes) {
            throw new ProtocolException("HPACK header field exceeds max size");
        }
        final int fieldBytes = safeAdd(safeAdd(nameBytes, valueBytes), Normal._32);
        final int next = safeAdd(headerListBytes, fieldBytes);
        if (next > maxHeaderListSize) {
            throw new ProtocolException("HPACK header list exceeds max size");
        }
        return next;
    }

    /**
     * Returns an indexed header.
     *
     * @param index one-based HPACK static or dynamic table index
     * @return header field stored at the index
     */
    private Http2Header header(final int index) {
        if (index <= Normal._0) {
            throw new ProtocolException("HPACK index must be positive");
        }
        if (index <= STATIC_NAMES.length) {
            return Http2Header.of(STATIC_NAMES[index - Normal._1], STATIC_VALUES[index - Normal._1]);
        }
        final int dynamic = index - STATIC_NAMES.length - Normal._1;
        if (dynamic < Normal._0 || dynamic >= dynamicTable.size()) {
            throw new ProtocolException("HPACK index is out of range");
        }
        return dynamicTable.get(dynamic);
    }

    /**
     * Returns an indexed name.
     *
     * @param index one-based HPACK static or dynamic table index
     * @return header name stored at the index
     */
    private String name(final int index) {
        if (index <= Normal._0) {
            throw new ProtocolException("HPACK name index must be positive");
        }
        if (index <= STATIC_NAMES.length) {
            return STATIC_NAMES[index - Normal._1];
        }
        final int dynamic = index - STATIC_NAMES.length - Normal._1;
        if (dynamic < Normal._0 || dynamic >= dynamicTable.size()) {
            throw new ProtocolException("HPACK name index is out of range");
        }
        return dynamicTable.get(dynamic).name();
    }

    /**
     * Finds an exact table index.
     *
     * @param header header field to locate by exact name and value
     * @return one-based HPACK table index, or zero when absent
     */
    private int exactIndex(final Http2Header header) {
        final int exact = staticExactIndex(header.name(), header.value());
        if (exact > Normal._0) {
            return exact;
        }
        return dynamicIndex(dynamicExactIndex.get(header));
    }

    /**
     * Finds a name table index.
     *
     * @param name header name to locate
     * @return one-based HPACK table index, or zero when absent
     */
    private int nameIndex(final String name) {
        final Integer index = STATIC_NAME_INDEX.get(name);
        if (index != null) {
            return index;
        }
        return dynamicIndex(dynamicNameIndex.get(name));
    }

    /**
     * Converts an insertion sequence to the current HPACK dynamic index.
     *
     * @param sequence dynamic-table insertion sequence, or {@code null}
     * @return current HPACK index, or zero when the entry is absent
     */
    private int dynamicIndex(final Long sequence) {
        if (sequence == null) {
            return Normal._0;
        }
        final long offset = dynamicSequence - sequence;
        if (offset < Normal._0 || offset >= dynamicTable.size()) {
            return Normal._0;
        }
        return STATIC_NAMES.length + (int) offset + Normal._1;
    }

    /**
     * Inserts a dynamic table entry.
     *
     * @param header non-sensitive header field to add as the newest entry
     */
    private void insert(final Http2Header header) {
        final int size = size(header);
        if (size > tableSize) {
            clearDynamicTable();
            return;
        }
        final long sequence = ++dynamicSequence;
        dynamicTable.add(Normal._0, header);
        dynamicSequences.add(Normal._0, sequence);
        dynamicExactIndex.put(header, sequence);
        dynamicNameIndex.put(header.name(), sequence);
        tableBytes += size;
        evict();
    }

    /**
     * Evicts dynamic entries to fit the configured size.
     */
    private void evict() {
        while (tableBytes > tableSize && !dynamicTable.isEmpty()) {
            final Http2Header removed = dynamicTable.remove(dynamicTable.size() - Normal._1);
            final long removedSequence = dynamicSequences.remove(dynamicSequences.size() - Normal._1);
            dynamicExactIndex.remove(removed, removedSequence);
            dynamicNameIndex.remove(removed.name(), removedSequence);
            tableBytes -= size(removed);
        }
    }

    /**
     * Clears all dynamic table storage and lookup sidecars.
     */
    private void clearDynamicTable() {
        dynamicTable.clear();
        dynamicSequences.clear();
        dynamicExactIndex.clear();
        dynamicNameIndex.clear();
        tableBytes = Normal._0;
    }

    /**
     * Calculates entry size.
     *
     * @param header header field whose HPACK entry size is requested
     * @return HPACK entry size in bytes, including the fixed overhead
     */
    private static int size(final Http2Header header) {
        return header.hpackSize();
    }

    /**
     * Writes an HPACK string using Huffman coding only when it shortens the value.
     *
     * @param output HPACK destination writer
     * @param value  text to encode as UTF-8
     */
    private static void writeString(final ByteWriter output, final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        final int huffmanLength = huffmanLength(bytes);
        if (huffmanLength < bytes.length) {
            writeInteger(output, huffmanLength, Normal._128, Normal._7);
            writeHuffman(output, bytes);
        } else {
            writeInteger(output, bytes.length, Normal._0, Normal._7);
            output.write(bytes);
        }
    }

    /**
     * Returns the encoded Huffman byte count, saturated on overflow.
     *
     * @param bytes uncompressed UTF-8 bytes
     * @return encoded Huffman length in bytes
     */
    private static int huffmanLength(final byte[] bytes) {
        long bits = Normal._0;
        for (final byte value : bytes) {
            bits += HUFFMAN_LENGTHS[value & Builder.UNSIGNED_BYTE_MASK];
        }
        final long length = (bits + Normal._7) >>> Normal._3;
        return length > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) length;
    }

    /**
     * Writes an RFC 7541 Huffman sequence with EOS-prefix padding.
     *
     * @param output encoded destination
     * @param bytes  uncompressed UTF-8 bytes
     */
    private static void writeHuffman(final ByteWriter output, final byte[] bytes) {
        long pending = Normal._0;
        int pendingBits = Normal._0;
        for (final byte value : bytes) {
            final int symbol = value & Builder.UNSIGNED_BYTE_MASK;
            final int length = HUFFMAN_LENGTHS[symbol];
            pending = (pending << length) | (HUFFMAN_CODES[symbol] & Builder.UNSIGNED_INT_MASK);
            pendingBits += length;
            while (pendingBits >= Normal._8) {
                pendingBits -= Normal._8;
                output.write((int) (pending >>> pendingBits));
                pending &= pendingBits == Normal._0 ? Normal._0 : (1L << pendingBits) - Normal._1;
            }
        }
        if (pendingBits != Normal._0) {
            output.write((int) ((pending << (Normal._8 - pendingBits)) | (0xff >>> pendingBits)));
        }
    }

    /**
     * Reads a string.
     *
     * @param input    HPACK input positioned at a string length prefix
     * @param maxBytes maximum decoded string bytes
     * @return decoded UTF-8 string
     */
    private static String readString(final Buffer input, final int maxBytes) {
        if (input.size() == Normal._0) {
            throw new ProtocolException("Truncated HPACK string");
        }
        final boolean huffman = (input.getByte(Normal._0) & Normal._128) != 0;
        final int length = readInteger(input, Normal._7);
        if (!huffman && length > maxBytes) {
            throw new ProtocolException("HPACK string exceeds max size");
        }
        if (input.size() < length) {
            throw new ProtocolException("Truncated HPACK string bytes");
        }
        final ByteString bytes = readByteString(input, length);
        if (huffman) {
            return new ByteString(decodeHuffman(bytes, maxBytes)).string(Charset.UTF_8);
        }
        return bytes.string(Charset.UTF_8);
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
     * @param output     HPACK destination writer
     * @param value      non-negative integer to encode
     * @param prefixMask representation bits placed above the integer prefix
     * @param prefixBits number of low-order bits available in the first byte
     */
    private static void writeInteger(
            final ByteWriter output,
            final int value,
            final int prefixMask,
            final int prefixBits) {
        final int maxPrefix = (Normal._1 << prefixBits) - Normal._1;
        if (value < maxPrefix) {
            output.write(prefixMask | value);
            return;
        }
        output.write(prefixMask | maxPrefix);
        int remaining = value - maxPrefix;
        while (remaining >= Normal._128) {
            output.write((remaining & Builder._127) | Normal._128);
            remaining >>>= Normal._7;
        }
        output.write(remaining);
    }

    /**
     * Reads an HPACK integer.
     *
     * @param input      HPACK input positioned at the integer's first byte
     * @param prefixBits number of low-order integer bits in the first byte
     * @return decoded non-negative HPACK integer
     */
    private static int readInteger(final Buffer input, final int prefixBits) {
        if (input.size() == Normal._0) {
            throw new ProtocolException("Truncated HPACK integer");
        }
        final int first = input.readByte() & Builder.UNSIGNED_BYTE_MASK;
        final int maxPrefix = (Normal._1 << prefixBits) - Normal._1;
        long value = first & maxPrefix;
        if (value < maxPrefix) {
            return (int) value;
        }
        int shift = Normal._0;
        while (input.size() > Normal._0) {
            final int next = input.readByte() & Builder.UNSIGNED_BYTE_MASK;
            value += (long) (next & Builder._127) << shift;
            if (value > Integer.MAX_VALUE) {
                throw new ProtocolException("HPACK integer overflow");
            }
            if ((next & Normal._128) == Normal._0) {
                return (int) value;
            }
            shift += Normal._7;
            if (shift > Normal._28) {
                throw new ProtocolException("HPACK integer overflow");
            }
        }
        throw new ProtocolException("Truncated HPACK integer continuation");
    }

    /**
     * Reads a fixed number of bytes from a core buffer.
     *
     * @param input  source buffer consumed by the read
     * @param length exact number of bytes to read
     * @return byte string containing the requested bytes
     */
    private static ByteString readByteString(final Buffer input, final int length) {
        try {
            return input.readByteString(length);
        } catch (final java.io.EOFException e) {
            throw new ProtocolException("Truncated HPACK bytes", e);
        }
    }

    /**
     * Validates pseudo-header order.
     *
     * @param headers decoded or pending header fields in wire order
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
        final HashMap<String, Integer> indexes = new HashMap<>(STATIC_NAMES.length << Normal._1);
        for (int i = Normal._0; i < STATIC_NAMES.length; i++) {
            indexes.putIfAbsent(STATIC_NAMES[i], i + Normal._1);
        }
        return Map.copyOf(indexes);
    }

    /**
     * Finds a static exact index without allocating lookup keys.
     *
     * @param name  header name to match
     * @param value header value to match
     * @return one-based exact static-table index, or zero when absent
     */
    private static int staticExactIndex(final String name, final String value) {
        return switch (name) {
            case Http.Header.PSEUDO_METHOD -> switch (value) {
                case "GET" -> Normal._2;
                case "POST" -> Normal._3;
                default -> Normal._0;
            };
            case Http.Header.PSEUDO_PATH -> switch (value) {
                case Symbol.SLASH -> Normal._4;
                case "/index.html" -> Normal._5;
                default -> Normal._0;
            };
            case Http.Header.PSEUDO_SCHEME -> {
                if (Protocol.HTTP.name.equals(value)) {
                    yield Normal._6;
                }
                if (Protocol.HTTPS.name.equals(value)) {
                    yield Normal._7;
                }
                yield Normal._0;
            }
            case Http.Header.PSEUDO_STATUS -> {
                if ("200".equals(value)) {
                    yield Normal._8;
                }
                if ("204".equals(value)) {
                    yield Normal._9;
                }
                if ("206".equals(value)) {
                    yield Normal._10;
                }
                if ("304".equals(value)) {
                    yield Normal._11;
                }
                if ("400".equals(value)) {
                    yield Normal._12;
                }
                if ("404".equals(value)) {
                    yield Normal._13;
                }
                if ("500".equals(value)) {
                    yield Normal._14;
                }
                yield Normal._0;
            }
            case "accept-encoding" -> "gzip, deflate".equals(value) ? Normal._16 : Normal._0;
            default -> emptyStaticIndex(name, value);
        };
    }

    /**
     * Finds an exact empty-value static index.
     *
     * @param name  header name to locate in the static table
     * @param value candidate value, which must be empty
     * @return one-based empty-value static-table index, or zero when absent
     */
    private static int emptyStaticIndex(final String name, final String value) {
        if (!value.isEmpty()) {
            return Normal._0;
        }
        final Integer index = STATIC_NAME_INDEX.get(name);
        return index != null && STATIC_VALUES[index - Normal._1].isEmpty() ? index : Normal._0;
    }

    /**
     * Returns UTF-8 byte length without allocating bytes.
     *
     * @param value text whose encoded length is calculated
     * @return number of bytes required by its UTF-8 encoding
     */
    private static int utf8Length(final String value) {
        int length = Normal._0;
        for (int i = Normal._0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current < Normal._128) {
                length++;
            } else if (current < Normal._2048) {
                length += Normal._2;
            } else if (Character.isHighSurrogate(current) && i + Normal._1 < value.length()
                    && Character.isLowSurrogate(value.charAt(i + Normal._1))) {
                length += Normal._4;
                i++;
            } else {
                length += Normal._3;
            }
        }
        return length;
    }

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
    private static byte[] decodeHuffman(final ByteString bytes, final int maxBytes) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(bytes.size(), maxBytes));
        HuffmanNode node = HUFFMAN_ROOT;
        int residualBits = Normal._0;
        int residualValue = Normal._0;
        for (int byteIndex = Normal._0; byteIndex < bytes.size(); byteIndex++) {
            final byte current = bytes.getByte(byteIndex);
            final int value = current & Builder.UNSIGNED_BYTE_MASK;
            for (int bitIndex = Normal._7; bitIndex >= Normal._0; bitIndex--) {
                final int bit = (value >>> bitIndex) & Normal._1;
                node = bit == Normal._0 ? node.zero : node.one;
                if (node == null) {
                    throw new ProtocolException("Invalid HPACK Huffman code");
                }
                residualBits++;
                residualValue = (residualValue << 1) | bit;
                if (node.symbol >= Normal._0) {
                    if (output.size() >= maxBytes) {
                        throw new ProtocolException("HPACK Huffman string exceeds max size");
                    }
                    output.write(node.symbol);
                    node = HUFFMAN_ROOT;
                    residualBits = Normal._0;
                    residualValue = Normal._0;
                } else if (residualBits > Normal._30) {
                    throw new ProtocolException("Invalid HPACK Huffman EOS");
                }
            }
        }
        if (node != HUFFMAN_ROOT) {
            if (residualBits > Normal._7 || residualValue != (Normal._1 << residualBits) - Normal._1) {
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
        for (int symbol = Normal._0; symbol < HUFFMAN_CODES.length; symbol++) {
            HuffmanNode node = root;
            final int code = HUFFMAN_CODES[symbol];
            final int length = HUFFMAN_LENGTHS[symbol];
            for (int bit = length - Normal._1; bit >= Normal._0; bit--) {
                if (node.symbol >= Normal._0) {
                    throw new IllegalStateException("Invalid HPACK Huffman prefix table");
                }
                if (((code >>> bit) & Normal._1) == Normal._0) {
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
            if (node.symbol >= Normal._0 || node.zero != null || node.one != null) {
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
        private int symbol = Normal.__1;

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
            ensure(Normal._1);
            bytes[length++] = (byte) value;
        }

        /**
         * Writes bytes.
         *
         * @param value bytes
         */
        private void write(final byte[] value) {
            ensure(value.length);
            System.arraycopy(value, Normal._0, bytes, length, value.length);
            length += value.length;
        }

        /**
         * Returns a readable buffer.
         *
         * @return buffer
         */
        private Buffer buffer() {
            return new Buffer().write(bytes, 0, length);
        }

        /**
         * Clears written bytes while retaining capacity for the next connection-local header block.
         *
         * @param capacity minimum desired capacity
         * @return this writer
         */
        private ByteWriter reset(final int capacity) {
            length = Normal._0;
            if (bytes.length < capacity) {
                bytes = new byte[capacity];
            }
            return this;
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
            int capacity = bytes.length << Normal._1;
            while (capacity < required) {
                capacity <<= Normal._1;
            }
            final byte[] copy = new byte[capacity];
            System.arraycopy(bytes, Normal._0, copy, Normal._0, length);
            bytes = copy;
        }

    }

}
