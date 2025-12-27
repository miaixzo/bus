/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Provider;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Binary data provider for handling file streams and binary content.
 * <p>
 * This provider handles binary data that shouldn't be encoded as strings. It's designed for file downloads, image
 * streams, PDFs, and other binary content. The provider treats the input as binary data and passes it through without
 * string conversion to avoid encoding corruption.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BinaryProvider implements Provider {

    /**
     * Handles binary data serialization.
     * <p>
     * For binary data, we don't actually serialize to a string. Instead, we return the data as-is in a format that can
     * be properly handled by the streaming infrastructure. If the input is already a byte array or DataBuffer, we pass
     * it through. If it's a string (unlikely for binary), we encode it as UTF-8 bytes.
     * </p>
     *
     * @param object The binary object to be processed (can be byte[], DataBuffer, Flux&lt;DataBuffer&gt;, or String)
     * @return A Mono emitting the binary data as a string for compatibility with the Provider interface
     */
    @Override
    public Mono<String> serialize(Object object) {
        return Mono.fromCallable(() -> {
            if (object == null) {
                Logger.debug(true, "BinaryProvider", "Binary object is null, returning empty string");
                return "";
            }

            // If it's already a byte array, we need special handling
            if (object instanceof byte[]) {
                byte[] bytes = (byte[]) object;
                Logger.debug(true, "BinaryProvider", "Processing byte array of size: {}", bytes.length);
                // For binary data, we don't actually want to convert to string
                // But for Provider interface compatibility, we'll use a marker
                return new String(bytes, Charset.ISO_8859_1);
            }

            // If it's a DataBuffer, extract bytes
            if (object instanceof DataBuffer) {
                DataBuffer dataBuffer = (DataBuffer) object;
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                Logger.debug(true, "BinaryProvider", "Processing DataBuffer of size: {}", bytes.length);
                return new String(bytes, Charset.ISO_8859_1);
            }

            // If it's a Flux of DataBuffer, we can't serialize to a single string
            // This should be handled at a higher level
            if (object instanceof Flux) {
                Logger.debug(
                        true,
                        "BinaryProvider",
                        "Flux detected - this should be handled by streaming infrastructure");
                // Return a marker that indicates streaming should be used
                return "BINARY_STREAM_MARKER";
            }

            // For any other object, convert to string (fallback)
            String result = object.toString();
            Logger.debug(
                    true,
                    "BinaryProvider",
                    "Fallback processing for object type: {}, length: {}",
                    object.getClass().getSimpleName(),
                    result.length());
            return result;
        });
    }

    /**
     * Static utility method to create a binary stream from bytes.
     * <p>
     * This method should be used when working with binary data that needs to be streamed directly without string
     * conversion.
     * </p>
     *
     * @param bytes The binary data to stream
     * @return A Flux of DataBuffer containing the binary data
     */
    public static Flux<DataBuffer> createBinaryStream(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return Flux.empty();
        }

        DefaultDataBufferFactory bufferFactory = DefaultDataBufferFactory.sharedInstance;
        DataBuffer buffer = bufferFactory.wrap(bytes);

        Logger.debug(true, "BinaryProvider", "Created binary stream for {} bytes", bytes.length);
        return Flux.just(buffer);
    }

    /**
     * Static utility method to check if an object represents streaming binary data.
     *
     * @param object The object to check
     * @return true if the object should be handled as a binary stream
     */
    public static boolean isBinaryStream(Object object) {
        return object instanceof Flux || object instanceof byte[] || object instanceof DataBuffer;
    }

}
