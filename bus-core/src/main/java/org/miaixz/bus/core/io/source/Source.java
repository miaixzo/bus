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
package org.miaixz.bus.core.io.source;

import java.io.Closeable;
import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * Provides a byte stream. Use this interface to read data from various locations, such as networks, storage, or
 * in-memory buffers. Sources can be layered to transform the provided data, for example, to decompress, decrypt, or
 * remove protocol framing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Source extends Closeable {

    /**
     * Removes at least 1 byte and at most {@code byteCount} bytes from this source and appends them to {@code sink}.
     * Returns the number of bytes read, or -1 if this source has been exhausted.
     *
     * @param sink      The buffer to which bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    long read(Buffer sink, long byteCount) throws IOException;

    /**
     * Returns the timeout for this source.
     *
     * @return The timeout instance.
     */
    Timeout timeout();

    /**
     * Closes this source and releases any resources held by it. It is an error to read from a closed source. It is safe
     * to close a source multiple times.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    void close() throws IOException;

}
