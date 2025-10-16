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

import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;

/**
 * A {@link Source} that delegates all calls to another {@link Source} instance. This class serves as a base for
 * implementing custom sources that wrap an existing source, allowing for modification or inspection of the data flow
 * without altering the original source.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AssignSource implements Source {

    /**
     * The underlying {@link Source} to which all calls are delegated.
     */
    private final Source delegate;

    /**
     * Constructs an {@code AssignSource} with the specified delegate.
     *
     * @param delegate The {@link Source} to which all calls will be forwarded.
     * @throws IllegalArgumentException If the delegate is null.
     */
    public AssignSource(Source delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
    }

    /**
     * Returns the underlying {@link Source} delegate.
     *
     * @return The delegate {@link Source}.
     */
    public final Source delegate() {
        return delegate;
    }

    /**
     * Delegates the read operation to the underlying source.
     *
     * @param sink      The buffer to which bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs during the read operation.
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        return delegate.read(sink, byteCount);
    }

    /**
     * Delegates the timeout operation to the underlying source.
     *
     * @return The timeout instance of the delegate source.
     */
    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    /**
     * Delegates the close operation to the underlying source.
     *
     * @throws IOException If an I/O error occurs during the close operation.
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * Returns a string representation of this {@code AssignSource}, including its class name and the delegate source.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + Symbol.PARENTHESE_LEFT + delegate + Symbol.PARENTHESE_RIGHT;
    }

}
