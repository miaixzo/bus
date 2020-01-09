/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.io.Source;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 读写HPACK v10.
 * <p>
 * 这个实现为动态表使用一个数组，为索引条目使用一个列表。
 * 动态条目被添加到数组中，从最后一个位置开始向前移动。当数组填满时，它被加倍.
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
final class Hpack {

    static final HttpHeaders[] STATIC_HEADERS_TABLE = new HttpHeaders[]{
            new HttpHeaders(HttpHeaders.TARGET_AUTHORITY, Normal.EMPTY),
            new HttpHeaders(HttpHeaders.TARGET_METHOD, Http.GET),
            new HttpHeaders(HttpHeaders.TARGET_METHOD, Http.POST),
            new HttpHeaders(HttpHeaders.TARGET_PATH, Symbol.SLASH),
            new HttpHeaders(HttpHeaders.TARGET_PATH, "/index.html"),
            new HttpHeaders(HttpHeaders.TARGET_SCHEME, Http.HTTP),
            new HttpHeaders(HttpHeaders.TARGET_SCHEME, Http.HTTPS),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_OK)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_NO_CONTENT)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_PARTIAL)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_NOT_MODIFIED)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_BAD_REQUEST)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_NOT_FOUND)),
            new HttpHeaders(HttpHeaders.RESPONSE_STATUS, StringUtils.toString(Http.HTTP_INTERNAL_ERROR)),
            new HttpHeaders("accept-charset", ""),
            new HttpHeaders("accept-encoding", "gzip, deflate"),
            new HttpHeaders("accept-language", ""),
            new HttpHeaders("accept-ranges", ""),
            new HttpHeaders("accept", ""),
            new HttpHeaders("access-control-allow-origin", ""),
            new HttpHeaders("age", ""),
            new HttpHeaders("allow", ""),
            new HttpHeaders("authorization", ""),
            new HttpHeaders("cache-control", ""),
            new HttpHeaders("content-disposition", ""),
            new HttpHeaders("content-encoding", ""),
            new HttpHeaders("content-language", ""),
            new HttpHeaders("content-length", ""),
            new HttpHeaders("content-location", ""),
            new HttpHeaders("content-range", ""),
            new HttpHeaders("content-type", ""),
            new HttpHeaders("cookie", ""),
            new HttpHeaders("date", ""),
            new HttpHeaders("etag", ""),
            new HttpHeaders("expect", ""),
            new HttpHeaders("expires", ""),
            new HttpHeaders("from", ""),
            new HttpHeaders("host", ""),
            new HttpHeaders("if-match", ""),
            new HttpHeaders("if-modified-since", ""),
            new HttpHeaders("if-none-match", ""),
            new HttpHeaders("if-range", ""),
            new HttpHeaders("if-unmodified-since", ""),
            new HttpHeaders("last-modified", ""),
            new HttpHeaders("link", ""),
            new HttpHeaders("location", ""),
            new HttpHeaders("max-forwards", ""),
            new HttpHeaders("proxy-authenticate", ""),
            new HttpHeaders("proxy-authorization", ""),
            new HttpHeaders("range", ""),
            new HttpHeaders("referer", ""),
            new HttpHeaders("refresh", ""),
            new HttpHeaders("retry-after", ""),
            new HttpHeaders("server", ""),
            new HttpHeaders("set-cookie", ""),
            new HttpHeaders("strict-transport-security", ""),
            new HttpHeaders("transfer-encoding", ""),
            new HttpHeaders("user-agent", ""),
            new HttpHeaders("vary", ""),
            new HttpHeaders("via", ""),
            new HttpHeaders("www-authenticate", "")
    };
    static final Map<ByteString, Integer> NAME_TO_FIRST_INDEX = nameToFirstIndex();
    private static final int PREFIX_4_BITS = 0x0f;
    private static final int PREFIX_5_BITS = 0x1f;
    private static final int PREFIX_6_BITS = 0x3f;
    private static final int PREFIX_7_BITS = 0x7f;

    private Hpack() {
    }

    private static Map<ByteString, Integer> nameToFirstIndex() {
        Map<ByteString, Integer> result = new LinkedHashMap<>(STATIC_HEADERS_TABLE.length);
        for (int i = 0; i < STATIC_HEADERS_TABLE.length; i++) {
            if (!result.containsKey(STATIC_HEADERS_TABLE[i].name)) {
                result.put(STATIC_HEADERS_TABLE[i].name, i);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    static ByteString checkLowercase(ByteString name) throws IOException {
        for (int i = 0, length = name.size(); i < length; i++) {
            byte c = name.getByte(i);
            if (c >= 'A' && c <= 'Z') {
                throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
            }
        }
        return name;
    }

    static final class Reader {

        private final List<HttpHeaders> headersList = new ArrayList<>();
        private final BufferSource source;

        private final int headerTableSizeSetting;
        HttpHeaders[] dynamicTable = new HttpHeaders[8];
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
            this.source = IoUtils.buffer(source);
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

        public List<HttpHeaders> getAndResetHeaderList() {
            List<HttpHeaders> result = new ArrayList<>(headersList);
            headersList.clear();
            return result;
        }

        private void readIndexedHeader(int index) throws IOException {
            if (isStaticHeader(index)) {
                HttpHeaders staticEntry = STATIC_HEADERS_TABLE[index];
                headersList.add(staticEntry);
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADERS_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= dynamicTable.length) {
                    throw new IOException("Header index too large " + (index + 1));
                }
                headersList.add(dynamicTable[dynamicTableIndex]);
            }
        }

        private int dynamicTableIndex(int index) {
            return nextHeaderIndex + 1 + index;
        }

        private void readLiteralHeaderWithoutIndexingIndexedName(int index) throws IOException {
            ByteString name = getName(index);
            ByteString value = readByteString();
            headersList.add(new HttpHeaders(name, value));
        }

        private void readLiteralHeaderWithoutIndexingNewName() throws IOException {
            ByteString name = checkLowercase(readByteString());
            ByteString value = readByteString();
            headersList.add(new HttpHeaders(name, value));
        }

        private void readLiteralHeaderWithIncrementalIndexingIndexedName(int nameIndex)
                throws IOException {
            ByteString name = getName(nameIndex);
            ByteString value = readByteString();
            insertIntoDynamicTable(-1, new HttpHeaders(name, value));
        }

        private void readLiteralHeaderWithIncrementalIndexingNewName() throws IOException {
            ByteString name = checkLowercase(readByteString());
            ByteString value = readByteString();
            insertIntoDynamicTable(-1, new HttpHeaders(name, value));
        }

        private ByteString getName(int index) throws IOException {
            if (isStaticHeader(index)) {
                return STATIC_HEADERS_TABLE[index].name;
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADERS_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= dynamicTable.length) {
                    throw new IOException("Header index too large " + (index + 1));
                }

                return dynamicTable[dynamicTableIndex].name;
            }
        }

        private boolean isStaticHeader(int index) {
            return index >= 0 && index <= STATIC_HEADERS_TABLE.length - 1;
        }

        private void insertIntoDynamicTable(int index, HttpHeaders entry) {
            headersList.add(entry);

            int delta = entry.hpackSize;
            if (index != -1) {
                delta -= dynamicTable[dynamicTableIndex(index)].hpackSize;
            }

            if (delta > maxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            int bytesToRecover = (dynamicTableByteCount + delta) - maxDynamicTableByteCount;
            int entriesEvicted = evictToRecoverBytes(bytesToRecover);

            if (index == -1) {
                if (headerCount + 1 > dynamicTable.length) {
                    HttpHeaders[] doubled = new HttpHeaders[dynamicTable.length * 2];
                    System.arraycopy(dynamicTable, 0, doubled, dynamicTable.length, dynamicTable.length);
                    nextHeaderIndex = dynamicTable.length - 1;
                    dynamicTable = doubled;
                }
                index = nextHeaderIndex--;
                dynamicTable[index] = entry;
                headerCount++;
            } else {
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
                return prefix;
            }

            int result = prefixMask;
            int shift = 0;
            while (true) {
                int b = readByte();
                if ((b & 0x80) != 0) {
                    result += (b & 0x7f) << shift;
                    shift += 7;
                } else {
                    result += b << shift;
                    break;
                }
            }
            return result;
        }

        ByteString readByteString() throws IOException {
            int firstByte = readByte();
            boolean huffmanDecode = (firstByte & 0x80) == 0x80;
            int length = readInt(firstByte, PREFIX_7_BITS);

            if (huffmanDecode) {
                return ByteString.of(Huffman.get().decode(source.readByteArray(length)));
            } else {
                return source.readByteString(length);
            }
        }
    }

    static final class Writer {
        private static final int SETTINGS_HEADER_TABLE_SIZE = 4096;

        private static final int SETTINGS_HEADER_TABLE_SIZE_LIMIT = 16384;

        private final Buffer out;
        private final boolean useCompression;
        int headerTableSizeSetting;
        int maxDynamicTableByteCount;
        HttpHeaders[] dynamicTable = new HttpHeaders[8];
        int nextHeaderIndex = dynamicTable.length - 1;
        int headerCount = 0;
        int dynamicTableByteCount = 0;

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
                Arrays.fill(dynamicTable, nextHeaderIndex + 1, nextHeaderIndex + 1 + entriesToEvict, null);
                nextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        private void insertIntoDynamicTable(HttpHeaders entry) {
            int delta = entry.hpackSize;

            if (delta > maxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            int bytesToRecover = (dynamicTableByteCount + delta) - maxDynamicTableByteCount;
            evictToRecoverBytes(bytesToRecover);

            if (headerCount + 1 > dynamicTable.length) {
                HttpHeaders[] doubled = new HttpHeaders[dynamicTable.length * 2];
                System.arraycopy(dynamicTable, 0, doubled, dynamicTable.length, dynamicTable.length);
                nextHeaderIndex = dynamicTable.length - 1;
                dynamicTable = doubled;
            }
            int index = nextHeaderIndex--;
            dynamicTable[index] = entry;
            headerCount++;
            dynamicTableByteCount += delta;
        }

        void writeHeaders(List<HttpHeaders> headersBlock) throws IOException {
            if (emitDynamicTableSizeUpdate) {
                if (smallestHeaderTableSizeSetting < maxDynamicTableByteCount) {
                    writeInt(smallestHeaderTableSizeSetting, PREFIX_5_BITS, 0x20);
                }
                emitDynamicTableSizeUpdate = false;
                smallestHeaderTableSizeSetting = Integer.MAX_VALUE;
                writeInt(maxDynamicTableByteCount, PREFIX_5_BITS, 0x20);
            }

            for (int i = 0, size = headersBlock.size(); i < size; i++) {
                HttpHeaders headers = headersBlock.get(i);
                ByteString name = headers.name.toAsciiLowercase();
                ByteString value = headers.value;
                int headerIndex = -1;
                int headerNameIndex = -1;

                Integer staticIndex = NAME_TO_FIRST_INDEX.get(name);
                if (staticIndex != null) {
                    headerNameIndex = staticIndex + 1;
                    if (headerNameIndex > 1 && headerNameIndex < 8) {
                        if (ObjectUtils.equal(STATIC_HEADERS_TABLE[headerNameIndex - 1].value, value)) {
                            headerIndex = headerNameIndex;
                        } else if (ObjectUtils.equal(STATIC_HEADERS_TABLE[headerNameIndex].value, value)) {
                            headerIndex = headerNameIndex + 1;
                        }
                    }
                }

                if (headerIndex == -1) {
                    for (int j = nextHeaderIndex + 1, length = dynamicTable.length; j < length; j++) {
                        if (ObjectUtils.equal(dynamicTable[j].name, name)) {
                            if (ObjectUtils.equal(dynamicTable[j].value, value)) {
                                headerIndex = j - nextHeaderIndex + STATIC_HEADERS_TABLE.length;
                                break;
                            } else if (headerNameIndex == -1) {
                                headerNameIndex = j - nextHeaderIndex + STATIC_HEADERS_TABLE.length;
                            }
                        }
                    }
                }

                if (headerIndex != -1) {
                    writeInt(headerIndex, PREFIX_7_BITS, 0x80);
                } else if (headerNameIndex == -1) {
                    out.writeByte(0x40);
                    writeByteString(name);
                    writeByteString(value);
                    insertIntoDynamicTable(headers);
                } else if (name.startsWith(HttpHeaders.PSEUDO_PREFIX) && !HttpHeaders.TARGET_AUTHORITY.equals(name)) {
                    writeInt(headerNameIndex, PREFIX_4_BITS, 0);
                    writeByteString(value);
                } else {
                    writeInt(headerNameIndex, PREFIX_6_BITS, 0x40);
                    writeByteString(value);
                    insertIntoDynamicTable(headers);
                }
            }
        }

        void writeInt(int value, int prefixMask, int bits) {
            if (value < prefixMask) {
                out.writeByte(bits | value);
                return;
            }

            out.writeByte(bits | prefixMask);
            value -= prefixMask;

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

            if (maxDynamicTableByteCount == effectiveHeaderTableSize) return;

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
