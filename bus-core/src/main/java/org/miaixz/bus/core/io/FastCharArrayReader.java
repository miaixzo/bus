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

import java.io.IOException;
import java.io.Reader;

import org.miaixz.bus.core.text.CharsBacker;

/**
 * A {@link Reader} implementation backed by a {@code char[]}, designed for reading characters from an array.
 * <p>
 * Unlike the standard JDK {@link java.io.CharArrayReader}, this implementation is <b>not thread-safe</b>. Removing
 * synchronization overhead results in significantly faster read operations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FastCharArrayReader extends Reader {

    /**
     * The character buffer containing the data to be read. This field is set to {@code null} when the stream is closed.
     */
    protected char[] buffer;

    /**
     * The current reading position in the buffer. Represents the index of the next character to be read.
     */
    protected int position;

    /**
     * The saved position in the buffer. Repositions the stream to this point when {@link #reset()} is called.
     */
    protected int mark = 0;

    /**
     * The index one greater than the last valid character in the buffer. This defines the boundary for read operations.
     */
    protected int count;

    /**
     * Constructs a {@code FastCharArrayReader} from the specified {@link CharSequence}.
     *
     * @param sequence the character sequence to read from
     */
    public FastCharArrayReader(final CharSequence sequence) {
        this(CharsBacker.toCharArray(sequence));
    }

    /**
     * Constructs a {@code FastCharArrayReader} from the specified character array.
     *
     * @param buffer the character array to read from
     */
    public FastCharArrayReader(final char[] buffer) {
        this.buffer = buffer;
        this.position = 0;
        this.count = buffer.length;
    }

    /**
     * Constructs a {@code FastCharArrayReader} from the specified character array, starting at the given offset and
     * reading up to the specified length.
     *
     * @param buffer the character array to read from
     * @param offset the starting offset in the array
     * @param length the maximum number of characters to read
     * @throws IllegalArgumentException if the offset or length are out of bounds
     */
    public FastCharArrayReader(final char[] buffer, final int offset, final int length) {
        if ((offset < 0) || (offset > buffer.length) || (length < 0) || ((offset + length) < 0)) {
            throw new IllegalArgumentException();
        }
        this.buffer = buffer;
        this.position = offset;
        this.count = Math.min(offset + length, buffer.length);
        this.mark = offset;
    }

    /**
     * Reads a single character from the array.
     *
     * @return the character read, as an integer in the range 0 to 65535 ({@code 0x00-0xffff}), or -1 if the end of the
     *         stream has been reached
     * @throws IOException if the stream has been closed
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        if (position >= count) {
            return -1;
        } else {
            return buffer[position++];
        }
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param destination the destination buffer
     * @param offset      the offset at which to start storing characters
     * @param length      the maximum number of characters to read
     * @return the number of characters actually read, or -1 if the end of the stream has been reached
     * @throws IOException               if the stream has been closed
     * @throws IndexOutOfBoundsException if {@code offset} is negative, or {@code length} is negative, or
     *                                   {@code offset + length} is negative or greater than the length of the
     *                                   destination array
     */
    @Override
    public int read(final char[] destination, final int offset, int length) throws IOException {
        ensureOpen();
        if ((offset < 0) || (offset > destination.length) || (length < 0) || ((offset + length) > destination.length)
                || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (length == 0) {
            return 0;
        }

        if (position >= count) {
            return -1;
        }
        if (position + length > count) {
            length = count - position;
        }
        if (length <= 0) {
            return 0;
        }
        System.arraycopy(buffer, position, destination, offset, length);
        position += length;
        return length;
    }

    /**
     * Skips characters in the stream.
     *
     * @param n the number of characters to skip
     * @return the number of characters actually skipped
     * @throws IOException if the stream has been closed
     */
    @Override
    public long skip(long n) throws IOException {
        ensureOpen();
        if (position + n > count) {
            n = (long) count - position;
        }
        if (n < 0) {
            return 0;
        }
        position += (int) n;
        return n;
    }

    /**
     * Tells whether this stream is ready to be read. A {@code FastCharArrayReader} is ready if there are unread
     * characters in the buffer.
     *
     * @return {@code true} if the next read() is guaranteed not to block for input, {@code false} otherwise
     * @throws IOException if the stream has been closed
     */
    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return (count - position) > 0;
    }

    /**
     * Tells whether this stream supports the {@link #mark(int)} operation.
     *
     * @return {@code true} as {@code FastCharArrayReader} always supports mark
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the present position in the stream. Subsequent calls to {@link #reset()} will reposition the stream to this
     * point.
     *
     * @param readAheadLimit limit on the number of characters that may be read while still preserving the mark. Because
     *                       the stream's input comes from a character array, there is no actual limit, so this argument
     *                       is ignored.
     * @throws IOException if the stream has been closed
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        ensureOpen();
        mark = position;
    }

    /**
     * Resets the stream to the most recent mark, or to the beginning if it has never been marked.
     *
     * @throws IOException if the stream has been closed
     */
    @Override
    public void reset() throws IOException {
        ensureOpen();
        position = mark;
    }

    /**
     * Closes the stream and releases the underlying character buffer. Once the stream has been closed, further read
     * operations will throw an IOException.
     */
    @Override
    public void close() {
        buffer = null;
    }

    /**
     * Ensures that the stream is open before performing any operations.
     *
     * @throws IOException if the stream has been closed (i.e., the {@link #buffer} is null)
     */
    private void ensureOpen() throws IOException {
        if (buffer == null) {
            throw new IOException("Stream closed");
        }
    }

}
