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

import java.io.Closeable;
import java.io.IOException;

/**
 * Supplies a stream of bytes. Use this interface to read data from wherever
 * it's located: from the network, storage, or a buffer in memory. Sources may
 * be layered to transform supplied data, such as to decompress, decrypt, or
 * remove protocol framing.
 *
 * <p>Most applications shouldn't operate on a source directly, but rather on a
 * {@link BufferedSource} which is both more efficient and more convenient. Use
 *
 * <p>Sources are easy to test: just use a {@link Buffer} in your tests, and
 * fill it with the data your application is to read.
 *
 * <h3>Comparison with InputStream</h3>
 * This interface is functionally equivalent to {@link java.io.InputStream}.
 *
 * <p>{@code InputStream} requires multiple layers when consumed data is
 * heterogeneous: a {@code DataInputStream} for primitive values, a {@code
 * BufferedInputStream} for buffering, and {@code InputStreamReader} for
 * strings. This class uses {@code BufferedSource} for all of the above.
 *
 * <p>Source avoids the impossible-to-implement {@linkplain
 * java.io.InputStream#available available()} method. Instead callers specify
 * how many bytes they {@link BufferedSource#require require}.
 *
 * <p>Source omits the unsafe-to-compose {@linkplain java.io.InputStream#mark
 * mark and reset} state that's tracked by {@code InputStream}; instead, callers
 * just buffer what they need.
 *
 * <p>When implementing a source, you don't need to worry about the {@linkplain
 * java.io.InputStream#read single-byte read} method that is awkward to implement efficiently
 * and returns first of 257 possible values.
 *
 * <p>And source has a stronger {@code skip} method: {@link BufferedSource#skip}
 * won't return prematurely.
 *
 * <h3>Interop with InputStream</h3>
 * {@link BufferedSource#inputStream} to adapt a source to an {@code
 * InputStream}.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Source extends Closeable {

    /**
     * Removes at least 1, and up to {@code byteCount} bytes from this and appends
     * them to {@code sink}. Returns the number of bytes read, or -1 if this
     * source is exhausted.
     */
    long read(Buffer sink, long byteCount) throws IOException;

    /**
     * Returns the timeout for this source.
     */
    Timeout timeout();

    /**
     * Closes this source and releases the resources held by this source. It is an
     * error to read a closed source. It is safe to close a source more than once.
     */
    @Override
    void close() throws IOException;

}
