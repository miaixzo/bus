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
package org.miaixz.bus.core.io;

import java.io.Writer;

/**
 * A fast, non-thread-safe {@link Writer} that uses a {@link StringBuilder} internally. This is an alternative to
 * {@link java.io.StringWriter} that offers better performance for single-threaded operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FastStringWriter extends Writer {

    /**
     * The default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * The underlying {@link StringBuilder}.
     */
    private final StringBuilder stringBuilder;

    /**
     * Constructs a new instance with the default initial capacity.
     */
    public FastStringWriter() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a new instance with a specified initial capacity.
     *
     * @param initialSize The initial capacity.
     */
    public FastStringWriter(int initialSize) {
        if (initialSize < 0) {
            initialSize = DEFAULT_CAPACITY;
        }
        this.stringBuilder = new StringBuilder(initialSize);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public FastStringWriter append(final char c) {
        this.stringBuilder.append(c);
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public FastStringWriter append(final CharSequence csq, final int start, final int end) {
        this.stringBuilder.append(csq, start, end);
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public FastStringWriter append(final CharSequence csq) {
        this.stringBuilder.append(csq);
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void write(final int c) {
        this.stringBuilder.append((char) c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void write(final String text) {
        this.stringBuilder.append(text);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void write(final String text, final int off, final int len) {
        this.stringBuilder.append(text, off, off + len);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void write(final char[] c) {
        this.stringBuilder.append(c, 0, c.length);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void write(final char[] c, final int off, final int len) {
        if ((off < 0) || (off > c.length) || (len < 0) || ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        this.stringBuilder.append(c, off, len);
    }

    /**
     * Flushing has no effect on this writer.
     */
    @Override
    public void flush() {
        // Nothing to be flushed
    }

    /**
     * Closing has no effect on this writer.
     */
    @Override
    public void close() {
        // Nothing to be closed
    }

    /**
     * Returns the contents of the internal buffer as a string.
     *
     * @return The string content of this writer.
     */
    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

}
