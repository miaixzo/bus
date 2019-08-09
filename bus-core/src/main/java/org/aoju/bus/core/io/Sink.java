/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.core.io;

import org.aoju.bus.core.utils.IoUtils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Receives a stream of bytes. Use this interface to write data wherever it's
 * needed: to the network, storage, or a buffer in memory. Sinks may be layered
 * to transform received data, such as to compress, encrypt, throttle, or add
 * protocol framing.
 *
 * <p>Most application code shouldn't operate on a sink directly, but rather on a
 * {@link BufferedSink} which is both more efficient and more convenient. Use
 * {@link IoUtils.buffer(Sink)} to wrap any sink with a buffer.
 *
 * <p>Sinks are easy to test: just use a {@link Buffer} in your tests, and
 * read from it to confirm it received the data that was expected.
 *
 * <h3>Comparison with OutputStream</h3>
 * This interface is functionally equivalent to {@link java.io.OutputStream}.
 *
 * <p>{@code OutputStream} requires multiple layers when emitted data is
 * heterogeneous: a {@code DataOutputStream} for primitive values, a {@code
 * BufferedOutputStream} for buffering, and {@code OutputStreamWriter} for
 * charset encoding. This class uses {@code BufferedSink} for all of the above.
 *
 * <p>Sink is also easier to layer: there is no {@linkplain
 * java.io.OutputStream#write(int) single-byte write} method that is awkward to
 * implement efficiently.
 *
 * <h3>Interop with OutputStream</h3>
 * Use {@link IoUtils.sink} to adapt an {@code OutputStream} to a sink. Use {@link
 * BufferedSink#outputStream} to adapt a sink to an {@code OutputStream}.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Sink extends Closeable, Flushable {

    /**
     * Removes {@code byteCount} bytes from {@code source} and appends them to this.
     */
    void write(Buffer source, long byteCount) throws IOException;

    /**
     * Pushes all buffered bytes to their final destination.
     */
    @Override
    void flush() throws IOException;

    /**
     * Returns the timeout for this sink.
     */
    Timeout timeout();

    /**
     * Pushes all buffered bytes to their final destination and releases the
     * resources held by this sink. It is an error to write a closed sink. It is
     * safe to close a sink more than once.
     */
    @Override
    void close() throws IOException;
    
}
