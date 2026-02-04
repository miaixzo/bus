/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.sink;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * A {@code Sink} is an interface for writing a stream of bytes to a destination, such as a network, storage, or an
 * in-memory buffer. Sinks can be layered to transform data, for example, to compress, encrypt, throttle, or frame a
 * protocol. This interface can be adapted to an {@code OutputStream} via {@link BufferSink#outputStream()}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Sink extends Closeable, Flushable {

    /**
     * Writes {@code byteCount} bytes from {@code source} to this sink.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to read from {@code source} and write to this sink.
     * @throws IOException If an I/O error occurs during the read or write operation.
     */
    void write(Buffer source, long byteCount) throws IOException;

    /**
     * Flushes any buffered data to the ultimate destination. This method forces any buffered output bytes to be written
     * out to the underlying stream.
     *
     * @throws IOException If an I/O error occurs during the flush operation.
     */
    @Override
    void flush() throws IOException;

    /**
     * Returns the timeout for this sink.
     *
     * @return The {@link Timeout} object associated with this sink.
     */
    Timeout timeout();

    /**
     * Pushes any buffered data to the ultimate destination and then releases any system resources associated with this
     * sink. After the sink has been closed, further write operations will throw an {@link IOException}. Closing a
     * previously closed sink has no effect.
     *
     * @throws IOException If an I/O error occurs during the close operation.
     */
    @Override
    void close() throws IOException;

}
