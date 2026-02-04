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
