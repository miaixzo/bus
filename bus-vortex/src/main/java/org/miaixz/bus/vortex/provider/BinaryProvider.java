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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Provider;
import org.springframework.core.io.buffer.DataBuffer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Binary data provider for handling file streams and binary content.
 * <p>
 * This provider handles binary data that shouldn't be encoded as strings. It's designed for file downloads, image
 * streams, PDFs, and other binary content. The provider treats the input as binary data and passes it through without
 * string conversion to avoid encoding corruption.
 * <p>
 * Generic type parameters: {@code Provider<Object, byte[]>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BinaryProvider implements Provider<Object, byte[]> {

    /**
     * Handles binary data serialization.
     * <p>
     * For binary data, we convert the input to a byte array. If the input is already a byte array or DataBuffer, we
     * extract the bytes. If it's a string, we encode it as UTF-8 bytes. If it's a Flux of DataBuffer, we return a
     * marker byte array to indicate streaming should be handled at a higher level.
     * </p>
     *
     * @param input The binary object to be processed (can be byte[], DataBuffer, Flux&lt;DataBuffer&gt;, or String)
     * @return A Mono emitting the binary data as a byte array
     */
    @Override
    public Mono<byte[]> serialize(Object input) {
        return Mono.fromCallable(() -> {
            if (input == null) {
                Logger.debug(true, "BinaryProvider", "Binary object is null, returning empty byte array");
                return new byte[0];
            }

            // If it's already a byte array, return it directly
            if (input instanceof byte[]) {
                byte[] bytes = (byte[]) input;
                Logger.debug(true, "BinaryProvider", "Processing byte array of size: {}", bytes.length);
                return bytes;
            }

            // If it's a DataBuffer, extract bytes
            if (input instanceof DataBuffer) {
                DataBuffer dataBuffer = (DataBuffer) input;
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                Logger.debug(true, "BinaryProvider", "Processing DataBuffer of size: {}", bytes.length);
                return bytes;
            }

            // If it's a Flux of DataBuffer, return a marker byte array
            if (input instanceof Flux) {
                Logger.debug(
                        true,
                        "BinaryProvider",
                        "Flux detected - this should be handled by streaming infrastructure");
                // Return a marker that indicates streaming should be used
                return "BINARY_STREAM_MARKER".getBytes(Charset.ISO_8859_1);
            }

            // For any other object, convert to byte array (fallback)
            byte[] result = input.toString().getBytes(Charset.UTF_8);
            Logger.debug(
                    true,
                    "BinaryProvider",
                    "Fallback processing for object type: {}, byte array length: {}",
                    input.getClass().getSimpleName(),
                    result.length);
            return result;
        });
    }

}
