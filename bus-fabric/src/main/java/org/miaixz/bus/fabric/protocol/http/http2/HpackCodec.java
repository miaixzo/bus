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
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Compatibility facade over direction-private HPACK encoder and decoder state.
 *
 * <p>
 * Existing connection APIs remain stable while the writer and reader use independent dynamic tables. Encoding writes
 * directly to a core buffer; decoding retains the complete RFC 7541 validation and Huffman implementation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HpackCodec {

    /**
     * Writer-direction encoder with compression state independent from the decoder.
     */
    private HpackEncoder encoder;

    /**
     * Reader-direction decoder with compression state independent from the encoder.
     */
    private HpackDecoder decoder;

    /**
     * Effective dynamic-table size applied in both directions.
     */
    private int tableSize = 4096;

    /**
     * Maximum peer dynamic-table size accepted by the decoder.
     */
    private int maxTableSize = 4096;

    /**
     * Maximum decompressed header-list size enforced in both directions.
     */
    private int maxHeaderListSize = 64 * 1024;

    /**
     * Maximum compressed header-block size accepted by the decoder.
     */
    private int maxHeaderBlockBytes = 64 * 1024;

    /**
     * Maximum decoded bytes accepted in one field name or value.
     */
    private int maxHeaderFieldBytes = 16 * 1024;

    /**
     * Maximum number of fields accepted in one decoded header block.
     */
    private int maxHeaderCount = 256;

    /**
     * Creates independent reader and writer HPACK contexts with default limits.
     */
    public HpackCodec() {
        rebuild();
    }

    /**
     * Encodes ordered compatibility fields.
     *
     * @param headers ordered fields
     * @return encoded header-block buffer
     */
    public Buffer encodeBuffer(final List<Http2Header> headers) {
        final Buffer output = new Buffer();
        encoder.encode(headers, output);
        return output;
    }

    /**
     * Encodes request fields directly without creating a complete header list.
     *
     * @param method    request method
     * @param scheme    request scheme
     * @param authority request authority
     * @param path      request path
     * @param headers   regular headers
     * @param target    destination frame batch
     */
    void encodeRequest(
            final String method,
            final String scheme,
            final String authority,
            final String path,
            final org.miaixz.bus.fabric.Headers headers,
            final Buffer target) {
        encoder.encodeRequest(method, scheme, authority, path, headers, target);
    }

    /**
     * Decodes and consumes one complete header block.
     *
     * @param source encoded block
     * @return immutable decoded fields
     */
    public List<Http2Header> decode(final Buffer source) {
        return decoder.decode(source);
    }

    /**
     * Sets the effective dynamic-table size in both directions.
     *
     * @param size byte size from zero through the configured decoder maximum
     */
    public void tableSize(final int size) {
        if (size < 0 || size > maxTableSize) {
            throw new ValidateException("HPACK table size must be between 0 and " + maxTableSize);
        }
        tableSize = size;
        encoder.tableSize(size);
        decoder.tableSize(size);
    }

    /**
     * Returns the effective dynamic-table size.
     *
     * @return current byte size applied in both directions
     */
    public int tableSize() {
        return tableSize;
    }

    /**
     * Sets the decoder's accepted dynamic-table maximum and reduces the effective size when necessary.
     *
     * @param size byte size from zero through 65,536
     */
    public void maxTableSize(final int size) {
        if (size < 0 || size > 64 * 1024) {
            throw new ValidateException("HPACK maximum table size must be between 0 and 65536");
        }
        maxTableSize = size;
        decoder.maxTableSize(size);
        if (tableSize > size) {
            tableSize(size);
        }
    }

    /**
     * Returns the decoder's accepted dynamic-table maximum.
     *
     * @return current maximum in bytes
     */
    public int maxTableSize() {
        return maxTableSize;
    }

    /**
     * Sets the maximum compressed header-block size accepted by the decoder.
     *
     * @param size positive byte limit
     */
    public void maxHeaderBlockBytes(final int size) {
        if (size <= 0) {
            throw new ValidateException("HPACK header block limit must be positive");
        }
        maxHeaderBlockBytes = size;
        decoder.maxHeaderBlockBytes(size);
    }

    /**
     * Sets the maximum decompressed header-list size in both directions.
     *
     * @param size positive byte limit
     */
    public void maxHeaderListSize(final int size) {
        if (size <= 0) {
            throw new ValidateException("HPACK header list limit must be positive");
        }
        maxHeaderListSize = size;
        encoder.maxHeaderListBytes(size);
        decoder.maxHeaderListSize(size);
    }

    /**
     * Returns the maximum decompressed header-list size.
     *
     * @return current byte limit
     */
    public int maxHeaderListSize() {
        return maxHeaderListSize;
    }

    /**
     * Sets the maximum bytes accepted in one decoded field name or value.
     *
     * @param size positive per-component byte limit
     */
    public void maxHeaderFieldBytes(final int size) {
        if (size <= 0) {
            throw new ValidateException("HPACK field limit must be positive");
        }
        maxHeaderFieldBytes = size;
        decoder.maxHeaderFieldBytes(size);
    }

    /**
     * Sets the maximum number of fields accepted in one decoded block.
     *
     * @param count positive field-count limit
     */
    public void maxHeaderCount(final int count) {
        if (count <= 0) {
            throw new ValidateException("HPACK header count limit must be positive");
        }
        maxHeaderCount = count;
        decoder.maxHeaderCount(count);
    }

    /**
     * Discards both directional compression contexts and recreates them with every configured limit preserved.
     */
    public void reset() {
        rebuild();
    }

    /**
     * Rebuilds both directional contexts and reapplies every configured limit.
     */
    private void rebuild() {
        encoder = new HpackEncoder();
        decoder = new HpackDecoder();
        decoder.maxTableSize(maxTableSize);
        encoder.tableSize(tableSize);
        decoder.tableSize(tableSize);
        encoder.maxHeaderListBytes(maxHeaderListSize);
        decoder.maxHeaderBlockBytes(maxHeaderBlockBytes);
        decoder.maxHeaderListSize(maxHeaderListSize);
        decoder.maxHeaderFieldBytes(maxHeaderFieldBytes);
        decoder.maxHeaderCount(maxHeaderCount);
    }

}
