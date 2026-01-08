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
package org.miaixz.bus.core.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * An adapter class that wraps an {@link Appendable} and exposes it as a {@link Writer}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AppendableWriter extends Writer implements Appendable {

    /**
     * The underlying {@link Appendable} to which all operations are delegated.
     */
    private final Appendable appendable;
    /**
     * A flag indicating if the underlying appendable is also {@link Flushable}.
     */
    private final boolean flushable;
    /**
     * A flag indicating if the writer has been closed.
     */
    private boolean closed;

    /**
     * Constructs a new {@code AppendableWriter}.
     *
     * @param appendable The {@link Appendable} instance to wrap.
     */
    public AppendableWriter(final Appendable appendable) {
        this.appendable = appendable;
        this.flushable = appendable instanceof Flushable;
        this.closed = false;
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param cbuf An array of characters.
     * @param off  Offset from which to start writing characters.
     * @param len  Number of characters to write.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        checkNotClosed();
        appendable.append(CharBuffer.wrap(cbuf), off, off + len);
    }

    /**
     * Writes a single character.
     *
     * @param c The character to write.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void write(final int c) throws IOException {
        checkNotClosed();
        appendable.append((char) c);
    }

    /**
     * Appends the specified character to this writer.
     *
     * @param c The character to append.
     * @return This writer.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public Writer append(final char c) throws IOException {
        checkNotClosed();
        appendable.append(c);
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     *
     * @param csq   The character sequence from which a subsequence will be appended.
     * @param start The index of the first character in the subsequence.
     * @param end   The index of the character following the last character in the subsequence.
     * @return This writer.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        checkNotClosed();
        appendable.append(csq, start, end);
        return this;
    }

    /**
     * Appends the specified character sequence to this writer.
     *
     * @param csq The character sequence to append.
     * @return This writer.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        checkNotClosed();
        appendable.append(csq);
        return this;
    }

    /**
     * Writes a portion of a string.
     *
     * @param text The string to write.
     * @param off  Offset from which to start writing characters.
     * @param len  Number of characters to write.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void write(final String text, final int off, final int len) throws IOException {
        checkNotClosed();
        appendable.append(text, off, off + len);
    }

    /**
     * Writes a string.
     *
     * @param text The string to write.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void write(final String text) throws IOException {
        checkNotClosed();
        appendable.append(text);
    }

    /**
     * Writes an array of characters.
     *
     * @param c An array of characters.
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void write(final char[] c) throws IOException {
        checkNotClosed();
        appendable.append(CharBuffer.wrap(c));
    }

    /**
     * Flushes the stream. If the underlying appendable implements {@link Flushable}, its {@code flush} method will be
     * called.
     *
     * @throws IOException If an I/O error occurs or the writer is closed.
     */
    @Override
    public void flush() throws IOException {
        checkNotClosed();
        if (flushable) {
            ((Flushable) appendable).flush();
        }
    }

    /**
     * Checks if the writer has been closed.
     *
     * @throws IOException If the writer is closed.
     */
    private void checkNotClosed() throws IOException {
        if (closed) {
            throw new IOException("Writer is closed! " + this);
        }
    }

    /**
     * Closes the stream, flushing it first. If the underlying appendable implements {@link Closeable}, its
     * {@code close} method will be called.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            flush();
            if (appendable instanceof Closeable) {
                ((Closeable) appendable).close();
            }
            closed = true;
        }
    }

}
