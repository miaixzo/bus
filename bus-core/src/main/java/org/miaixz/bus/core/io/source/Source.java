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
