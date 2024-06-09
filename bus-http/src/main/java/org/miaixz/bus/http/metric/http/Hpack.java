/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Headers;

import java.io.IOException;
import java.util.*;

/**
 * 读写HPACK v10.
 * <p>
 * 这个实现为动态表使用一个数组，为索引条目使用一个列表。
 * 动态条目被添加到数组中，从最后一个位置开始向前移动。当数组填满时，它被加倍.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class Hpack {

    static final Headers.Header[] STATIC_HEADER_TABLE = new Headers.Header[]{
            new Headers.Header(Headers.Header.TARGET_AUTHORITY, Normal.EMPTY),
            new Headers.Header(Headers.Header.TARGET_METHOD, Http.GET),
            new Headers.Header(Headers.Header.TARGET_METHOD, Http.POST),
            new Headers.Header(Headers.Header.TARGET_PATH, Symbol.SLASH),
            new Headers.Header(Headers.Header.TARGET_PATH, "/index.html"),
            new Headers.Header(Headers.Header.TARGET_SCHEME, Http.HTTP),
            new Headers.Header(Headers.Header.TARGET_SCHEME, Http.HTTPS),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_OK)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_NO_CONTENT)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_PARTIAL)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_NOT_MODIFIED)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_BAD_REQUEST)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_NOT_FOUND)),
            new Headers.Header(Headers.Header.RESPONSE_STATUS, StringKit.toString(Http.HTTP_INTERNAL_ERROR)),
            new Headers.Header(Header.ACCEPT_CHARSET, Normal.EMPTY),
            new Headers.Header(Header.ACCEPT_ENCODING, "gzip, deflate"),
            new Headers.Header(Header.ACCEPT_LANGUAGE, Normal.EMPTY),
            new Headers.Header(Header.ACCEPT_RANGES, Normal.EMPTY),
            new Headers.Header(Header.ACCEPT, Normal.EMPTY),
            new Headers.Header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, Normal.EMPTY),
            new Headers.Header(Header.AGE, Normal.EMPTY),
            new Headers.Header(Header.ALLOW, Normal.EMPTY),
            new Headers.Header(Header.AUTHORIZATION, Normal.EMPTY),
            new Headers.Header(Header.CACHE_CONTROL, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_DISPOSITION, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_ENCODING, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_LANGUAGE, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_LENGTH, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_LOCATION, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_RANGE, Normal.EMPTY),
            new Headers.Header(Header.CONTENT_TYPE, Normal.EMPTY),
            new Headers.Header(Header.COOKIE, Normal.EMPTY),
            new Headers.Header(Header.DATE, Normal.EMPTY),
            new Headers.Header(Header.ETAG, Normal.EMPTY),
            new Headers.Header(Header.EXPECT, Normal.EMPTY),
            new Headers.Header(Header.EXPIRES, Normal.EMPTY),
            new Headers.Header(Header.FROM, Normal.EMPTY),
            new Headers.Header(Header.HOST, Normal.EMPTY),
            new Headers.Header(Header.IF_MATCH, Normal.EMPTY),
            new Headers.Header(Header.IF_MODIFIED_SINCE, Normal.EMPTY),
            new Headers.Header(Header.IF_NONE_MATCH, Normal.EMPTY),
            new Headers.Header(Header.IF_RANGE, Normal.EMPTY),
            new Headers.Header(Header.IF_UNMODIFIED_SINCE, Normal.EMPTY),
            new Headers.Header(Header.LAST_MODIFIED, Normal.EMPTY),
            new Headers.Header(Header.LINK, Normal.EMPTY),
            new Headers.Header(Header.LOCATION, Normal.EMPTY),
            new Headers.Header(Header.MAX_FORWARDS, Normal.EMPTY),
            new Headers.Header(Header.PROXY_AUTHENTICATE, Normal.EMPTY),
            new Headers.Header(Header.PROXY_AUTHORIZATION, Normal.EMPTY),
            new Headers.Header(Header.RANGE, Normal.EMPTY),
            new Headers.Header(Header.REFERER, Normal.EMPTY),
            new Headers.Header(Header.REFRESH, Normal.EMPTY),
            new Headers.Header(Header.RETRY_AFTER, Normal.EMPTY),
            new Headers.Header(Header.SERVER, Normal.EMPTY),
            new Headers.Header(Header.SET_COOKIE, Normal.EMPTY),
            new Headers.Header(Header.STRICT_TRANSPORT_SECURITY, Normal.EMPTY),
            new Headers.Header(Header.TRANSFER_ENCODING, Normal.EMPTY),
            new Headers.Header(Header.USER_AGENT, Normal.EMPTY),
            new Headers.Header(Header.VARY, Normal.EMPTY),
            new Headers.Header(Header.VIA, Normal.EMPTY),
            new Headers.Header(Header.WWW_AUTHENTICATE, Normal.EMPTY)
    };
    static final Map<ByteString, Integer> NAME_TO_FIRST_INDEX = nameToFirstIndex();
    private static final int PREFIX_4_BITS = 0x0f;
    private static final int PREFIX_5_BITS = 0x1f;
    private static final int PREFIX_6_BITS = 0x3f;
    private static final int PREFIX_7_BITS = 0x7f;

    private Hpack() {
    }

    private static Map<ByteString, Integer> nameToFirstIndex() {
        Map<ByteString, Integer> result = new LinkedHashMap<>(STATIC_HEADER_TABLE.length);
        for (int i = 0; i < STATIC_HEADER_TABLE.length; i++) {
            if (!result.containsKey(STATIC_HEADER_TABLE[i].name)) {
                result.put(STATIC_HEADER_TABLE[i].name, i);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * An HTTP/2 response cannot contain uppercase header characters and must be treated as
     * malformed.
     */
    static ByteString checkLowercase(ByteString name) throws IOException {
        for (int i = 0, length = name.size(); i < length; i++) {
            byte c = name.getByte(i);
            if (c >= 'A' && c <= 'Z') {
                throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
            }
        }
        return name;
    }

    static class Reader {

        private final List<Headers.Header> headerList = new ArrayList<>();
        private final BufferSource source;

        private final int headerTableSizeSetting;
        Headers.Header[] dynamicTable = new Headers.Header[8];
        int nextHeaderIndex = dynamicTable.length - 1;
        int headerCount = 0;
        int dynamicTableByteCount = 0;
        private int maxDynamicTableByteCount;

        Reader(int headerTableSizeSetting, Source source) {
            this(headerTableSizeSetting, headerTableSizeSetting, source);
        }

        Reader(int headerTableSizeSetting, int maxDynamicTableByteCount, Source source) {
            this.headerTableSizeSetting = headerTableSizeSetting;
            this.maxDynamicTableByteCount = maxDynamicTableByteCount;
            this.source = IoKit.buffer(source);
        }

        int maxDynamicTableByteCount() {
            return maxDynamicTableByteCount;
        }

        private void adjustDynamicTableByteCount() {
            if (maxDynamicTableByteCount < dynamicTableByteCount) {
                if (maxDynamicTableByteCount == 0) {
                    clearDynamicTable();
                } else {
                    evictToRecoverBytes(dynamicTableByteCount - maxDynamicTableByteCount);
                }
            }
        }

        private void clearDynamicTable() {
            Arrays.fill(dynamicTable, null);
            nextHeaderIndex = dynamicTable.length - 1;
            headerCount = 0;
            dynamicTableByteCount = 0;
        }

        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                for (int j = dynamicTable.length - 1; j >= nextHeaderIndex && bytesToRecover > 0; j--) {
                    bytesToRecover -= dynamicTable[j].hpackSize;
                    dynamicTableByteCount -= dynamicTable[j].hpackSize;
                    headerCount--;
                    entriesToEvict++;
                }
                System.arraycopy(dynamicTable, nextHeaderIndex + 1, dynamicTable,
                        nextHeaderIndex + 1 + entriesToEvict, headerCount);
                nextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        /**
         * Read {@code byteCount} bytes of headers from the source stream. This implementation does not
         * propagate the never indexed flag of a header.
         */
        void readHeaders() throws IOException {
            while (!source.exhausted()) {
                int b = source.readByte() & 0xff;
                if (b == 0x80) {
                    throw new IOException("index == 0");
                } else if ((b & 0x80) == 0x80) {
                    int index = readInt(b, PREFIX_7_BITS);
                    readIndexedHeader(index - 1);
                } else if (b == 0x40) {
                    readLiteralHeaderWithIncrementalIndexingNewName();
                } else if ((b & 0x40) == 0x40) {
                    int index = readInt(b, PREFIX_6_BITS);
                    readLiteralHeaderWithIncrementalIndexingIndexedName(index - 1);
                } else if ((b & 0x20) == 0x20) {
                    maxDynamicTableByteCount = readInt(b, PREFIX_5_BITS);
                    if (maxDynamicTableByteCount < 0
                            || maxDynamicTableByteCount > headerTableSizeSetting) {
                        throw new IOException("Invalid dynamic table size update " + maxDynamicTableByteCount);
                    }
                    adjustDynamicTableByteCount();
                } else if (b == 0x10 || b == 0) {
                    readLiteralHeaderWithoutIndexingNewName();
                } else {
                    int index = readInt(b, PREFIX_4_BITS);
                    readLiteralHeaderWithoutIndexingIndexedName(index - 1);
                }
            }
        }

        public List<Headers.Header> getAndResetHeaderList() {
            List<Headers.Header> result = new ArrayList<>(headerList);
            headerList.clear();
            return result;
        }

        private void readIndexedHeader(int index) throws IOException {
            if (isStaticHeader(index)) {
                Headers.Header staticEntry = STATIC_HEADER_TABLE[index];
                headerList.add(staticEntry);
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADER_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= dynamicTable.length) {
                    throw new IOException("Header index too large " + (index + 1));
                }
                headerList.add(dynamicTable[dynamicTableIndex]);
            }
        }

        private int dynamicTableIndex(int index) {
            return nextHeaderIndex + 1 + index;
        }

        private void readLiteralHeaderWithoutIndexingIndexedName(int index) throws IOException {
            ByteString name = getName(index);
            ByteString value = readByteString();
            headerList.add(new Headers.Header(name, value));
        }

        private void readLiteralHeaderWithoutIndexingNewName() throws IOException {
            ByteString name = checkLowercase(readByteString());
            ByteString value = readByteString();
            headerList.add(new Headers.Header(name, value));
        }

        private void readLiteralHeaderWithIncrementalIndexingIndexedName(int nameIndex)
                throws IOException {
            ByteString name = getName(nameIndex);
            ByteString value = readByteString();
            insertIntoDynamicTable(-1, new Headers.Header(name, value));
        }

        private void readLiteralHeaderWithIncrementalIndexingNewName() throws IOException {
            ByteString name = checkLowercase(readByteString());
            ByteString value = readByteString();
            insertIntoDynamicTable(-1, new Headers.Header(name, value));
        }

        private ByteString getName(int index) throws IOException {
            if (isStaticHeader(index)) {
                return STATIC_HEADER_TABLE[index].name;
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADER_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= dynamicTable.length) {
                    throw new IOException("Header index too large " + (index + 1));
                }

                return dynamicTable[dynamicTableIndex].name;
            }
        }

        private boolean isStaticHeader(int index) {
            return index >= 0 && index <= STATIC_HEADER_TABLE.length - 1;
        }

        /**
         * index == -1 when new.
         */
        private void insertIntoDynamicTable(int index, Headers.Header entry) {
            headerList.add(entry);

            int delta = entry.hpackSize;
            if (index != -1) { // Index -1 == new header.
                delta -= dynamicTable[dynamicTableIndex(index)].hpackSize;
            }

            // if the new or replacement header is too big, drop all entries.
            if (delta > maxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            // Evict headers to the required length.
            int bytesToRecover = (dynamicTableByteCount + delta) - maxDynamicTableByteCount;
            int entriesEvicted = evictToRecoverBytes(bytesToRecover);

            if (index == -1) { // Adding a value to the dynamic table.
                if (headerCount + 1 > dynamicTable.length) { // Need to grow the dynamic table.
                    Headers.Header[] doubled = new Headers.Header[dynamicTable.length * 2];
                    System.arraycopy(dynamicTable, 0, doubled, dynamicTable.length, dynamicTable.length);
                    nextHeaderIndex = dynamicTable.length - 1;
                    dynamicTable = doubled;
                }
                index = nextHeaderIndex--;
                dynamicTable[index] = entry;
                headerCount++;
            } else { // Replace value at same position.
                index += dynamicTableIndex(index) + entriesEvicted;
                dynamicTable[index] = entry;
            }
            dynamicTableByteCount += delta;
        }

        private int readByte() throws IOException {
            return source.readByte() & 0xff;
        }

        int readInt(int firstByte, int prefixMask) throws IOException {
            int prefix = firstByte & prefixMask;
            if (prefix < prefixMask) {
                return prefix; // This was a single byte value.
            }

            // This is a multibyte value. Read 7 bits at a time.
            int result = prefixMask;
            int shift = 0;
            while (true) {
                int b = readByte();
                if ((b & 0x80) != 0) { // Equivalent to (b >= 128) since b is in [0..255].
                    result += (b & 0x7f) << shift;
                    shift += 7;
                } else {
                    result += b << shift; // Last byte.
                    break;
                }
            }
            return result;
        }

        /**
         * Reads a potentially Huffman encoded byte string.
         */
        ByteString readByteString() throws IOException {
            int firstByte = readByte();
            boolean huffmanDecode = (firstByte & 0x80) == 0x80; // 1NNNNNNN
            int length = readInt(firstByte, PREFIX_7_BITS);

            if (huffmanDecode) {
                return ByteString.of(Huffman.get().decode(source.readByteArray(length)));
            } else {
                return source.readByteString(length);
            }
        }
    }

    static class Writer {
        private static final int SETTINGS_HEADER_TABLE_SIZE = 4096;

        /**
         * The decoder has ultimate control of the maximum size of the dynamic table but we can choose
         * to use less. We'll put a cap at 16K. This is arbitrary but should be enough for most
         * purposes.
         */
        private static final int SETTINGS_HEADER_TABLE_SIZE_LIMIT = 16384;

        private final Buffer out;
        private final boolean useCompression;
        int headerTableSizeSetting;
        int maxDynamicTableByteCount;
        // Visible for testing.
        Headers.Header[] dynamicTable = new Headers.Header[8];
        // Array is populated back to front, so new entries always have lowest index.
        int nextHeaderIndex = dynamicTable.length - 1;
        int headerCount = 0;
        int dynamicTableByteCount = 0;
        /**
         * In the scenario where the dynamic table size changes multiple times between transmission of
         * header blocks, we need to keep track of the smallest value in that interval.
         */
        private int smallestHeaderTableSizeSetting = Integer.MAX_VALUE;
        private boolean emitDynamicTableSizeUpdate;

        Writer(Buffer out) {
            this(SETTINGS_HEADER_TABLE_SIZE, true, out);
        }

        Writer(int headerTableSizeSetting, boolean useCompression, Buffer out) {
            this.headerTableSizeSetting = headerTableSizeSetting;
            this.maxDynamicTableByteCount = headerTableSizeSetting;
            this.useCompression = useCompression;
            this.out = out;
        }

        private void clearDynamicTable() {
            Arrays.fill(dynamicTable, null);
            nextHeaderIndex = dynamicTable.length - 1;
            headerCount = 0;
            dynamicTableByteCount = 0;
        }

        /**
         * Returns the count of entries evicted.
         */
        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                // determine how many headers need to be evicted.
                for (int j = dynamicTable.length - 1; j >= nextHeaderIndex && bytesToRecover > 0; j--) {
                    bytesToRecover -= dynamicTable[j].hpackSize;
                    dynamicTableByteCount -= dynamicTable[j].hpackSize;
                    headerCount--;
                    entriesToEvict++;
                }
                System.arraycopy(dynamicTable, nextHeaderIndex + 1, dynamicTable,
                        nextHeaderIndex + 1 + entriesToEvict, headerCount);
                Arrays.fill(dynamicTable, nextHeaderIndex + 1, nextHeaderIndex + 1 + entriesToEvict, null);
                nextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        private void insertIntoDynamicTable(Headers.Header entry) {
            int delta = entry.hpackSize;

            // if the new or replacement header is too big, drop all entries.
            if (delta > maxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            // Evict headers to the required length.
            int bytesToRecover = (dynamicTableByteCount + delta) - maxDynamicTableByteCount;
            evictToRecoverBytes(bytesToRecover);

            if (headerCount + 1 > dynamicTable.length) { // Need to grow the dynamic table.
                Headers.Header[] doubled = new Headers.Header[dynamicTable.length * 2];
                System.arraycopy(dynamicTable, 0, doubled, dynamicTable.length, dynamicTable.length);
                nextHeaderIndex = dynamicTable.length - 1;
                dynamicTable = doubled;
            }
            int index = nextHeaderIndex--;
            dynamicTable[index] = entry;
            headerCount++;
            dynamicTableByteCount += delta;
        }

        /**
         * This does not use "never indexed" semantics for sensitive headers.
         */
        void writeHeaders(List<Headers.Header> headerBlock) throws IOException {
            if (emitDynamicTableSizeUpdate) {
                if (smallestHeaderTableSizeSetting < maxDynamicTableByteCount) {
                    // Multiple dynamic table size updates!
                    writeInt(smallestHeaderTableSizeSetting, PREFIX_5_BITS, 0x20);
                }
                emitDynamicTableSizeUpdate = false;
                smallestHeaderTableSizeSetting = Integer.MAX_VALUE;
                writeInt(maxDynamicTableByteCount, PREFIX_5_BITS, 0x20);
            }

            for (int i = 0, size = headerBlock.size(); i < size; i++) {
                Headers.Header header = headerBlock.get(i);
                ByteString name = header.name.toAsciiLowercase();
                ByteString value = header.value;
                int headerIndex = -1;
                int headerNameIndex = -1;

                Integer staticIndex = NAME_TO_FIRST_INDEX.get(name);
                if (staticIndex != null) {
                    headerNameIndex = staticIndex + 1;
                    if (headerNameIndex > 1 && headerNameIndex < 8) {
                        // Only search a subset of the static header table. Most entries have an empty value, so
                        // it's unnecessary to waste cycles looking at them. This check is built on the
                        // observation that the header entries we care about are in adjacent pairs, and we
                        // always know the first index of the pair.
                        if (Objects.equals(STATIC_HEADER_TABLE[headerNameIndex - 1].value, value)) {
                            headerIndex = headerNameIndex;
                        } else if (Objects.equals(STATIC_HEADER_TABLE[headerNameIndex].value, value)) {
                            headerIndex = headerNameIndex + 1;
                        }
                    }
                }

                if (headerIndex == -1) {
                    for (int j = nextHeaderIndex + 1, length = dynamicTable.length; j < length; j++) {
                        if (Objects.equals(dynamicTable[j].name, name)) {
                            if (Objects.equals(dynamicTable[j].value, value)) {
                                headerIndex = j - nextHeaderIndex + STATIC_HEADER_TABLE.length;
                                break;
                            } else if (headerNameIndex == -1) {
                                headerNameIndex = j - nextHeaderIndex + STATIC_HEADER_TABLE.length;
                            }
                        }
                    }
                }

                if (headerIndex != -1) {
                    // Indexed Header Field.
                    writeInt(headerIndex, PREFIX_7_BITS, 0x80);
                } else if (headerNameIndex == -1) {
                    // Literal Header Field with Incremental Indexing - New Name.
                    out.writeByte(0x40);
                    writeByteString(name);
                    writeByteString(value);
                    insertIntoDynamicTable(header);
                } else if (name.startsWith(Headers.Header.PSEUDO_PREFIX) && !Headers.Header.TARGET_AUTHORITY.equals(name)) {
                    // Follow Chromes lead - only include the :authority pseudo header, but exclude all other
                    // pseudo headers. Literal Header Field without Indexing - Indexed Name.
                    writeInt(headerNameIndex, PREFIX_4_BITS, 0);
                    writeByteString(value);
                } else {
                    // Literal Header Field with Incremental Indexing - Indexed Name.
                    writeInt(headerNameIndex, PREFIX_6_BITS, 0x40);
                    writeByteString(value);
                    insertIntoDynamicTable(header);
                }
            }
        }

        void writeInt(int value, int prefixMask, int bits) {
            // Write the raw value for a single byte value.
            if (value < prefixMask) {
                out.writeByte(bits | value);
                return;
            }

            // Write the mask to start a multibyte value.
            out.writeByte(bits | prefixMask);
            value -= prefixMask;

            // Write 7 bits at a time 'til we're done.
            while (value >= 0x80) {
                int b = value & 0x7f;
                out.writeByte(b | 0x80);
                value >>>= 7;
            }
            out.writeByte(value);
        }

        void writeByteString(ByteString data) throws IOException {
            if (useCompression && Huffman.get().encodedLength(data) < data.size()) {
                Buffer huffmanBuffer = new Buffer();
                Huffman.get().encode(data, huffmanBuffer);
                ByteString huffmanBytes = huffmanBuffer.readByteString();
                writeInt(huffmanBytes.size(), PREFIX_7_BITS, 0x80);
                out.write(huffmanBytes);
            } else {
                writeInt(data.size(), PREFIX_7_BITS, 0);
                out.write(data);
            }
        }

        void setHeaderTableSizeSetting(int headerTableSizeSetting) {
            this.headerTableSizeSetting = headerTableSizeSetting;
            int effectiveHeaderTableSize = Math.min(headerTableSizeSetting,
                    SETTINGS_HEADER_TABLE_SIZE_LIMIT);

            if (maxDynamicTableByteCount == effectiveHeaderTableSize) return; // No change.

            if (effectiveHeaderTableSize < maxDynamicTableByteCount) {
                smallestHeaderTableSizeSetting = Math.min(smallestHeaderTableSizeSetting,
                        effectiveHeaderTableSize);
            }
            emitDynamicTableSizeUpdate = true;
            maxDynamicTableByteCount = effectiveHeaderTableSize;
            adjustDynamicTableByteCount();
        }

        private void adjustDynamicTableByteCount() {
            if (maxDynamicTableByteCount < dynamicTableByteCount) {
                if (maxDynamicTableByteCount == 0) {
                    clearDynamicTable();
                } else {
                    evictToRecoverBytes(dynamicTableByteCount - maxDynamicTableByteCount);
                }
            }
        }

    }

}
