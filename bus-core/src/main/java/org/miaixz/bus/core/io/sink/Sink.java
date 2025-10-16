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
