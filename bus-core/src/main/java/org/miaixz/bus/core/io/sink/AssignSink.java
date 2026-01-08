/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;

/**
 * An abstract sink that delegates all operations to another {@link Sink}. This class provides a convenient way to wrap
 * an existing {@link Sink} and add custom behavior without modifying the original sink.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AssignSink implements Sink {

    /**
     * The delegate sink to which all operations are forwarded.
     */
    private final Sink delegate;

    /**
     * Constructs an {@code AssignSink} with the specified delegate.
     *
     * @param delegate The delegate sink, must not be {@code null}.
     * @throws IllegalArgumentException If the delegate is {@code null}.
     */
    public AssignSink(Sink delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
    }

    /**
     * Returns the delegate sink.
     *
     * @return The delegate sink.
     */
    public final Sink delegate() {
        return delegate;
    }

    /**
     * Writes {@code byteCount} bytes from {@code source} to the delegate sink.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to write.
     * @throws IOException If an I/O error occurs during the write operation.
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        delegate.write(source, byteCount);
    }

    /**
     * Flushes any buffered data to the delegate sink.
     *
     * @throws IOException If an I/O error occurs during the flush operation.
     */
    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    /**
     * Returns the timeout for the delegate sink.
     *
     * @return The timeout object associated with the delegate sink.
     */
    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    /**
     * Closes the delegate sink and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs during the close operation.
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * Returns a string representation of this {@code AssignSink}. The string representation includes the simple name of
     * this class and the string representation of the delegate sink.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + Symbol.PARENTHESE_LEFT + delegate.toString() + Symbol.PARENTHESE_RIGHT;
    }

}
