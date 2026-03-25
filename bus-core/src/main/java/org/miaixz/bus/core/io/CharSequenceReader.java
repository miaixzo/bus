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

import java.io.Reader;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * A fast {@link Reader} implementation for {@link CharSequence} that is not thread-safe but offers better performance
 * compared to JDK's {@code StringReader}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CharSequenceReader extends Reader {

    /**
     * The starting position (inclusive) for reading.
     */
    private final int start;
    /**
     * The ending position (exclusive) for reading.
     */
    private final int end;
    /**
     * The character sequence to read from.
     */
    private final CharSequence text;
    /**
     * The current reading position.
     */
    private int next;
    /**
     * The marked position.
     */
    private int mark;

    /**
     * Constructs a new {@code CharSequenceReader}.
     *
     * @param text The {@link CharSequence} to read from.
     */
    public CharSequenceReader(final CharSequence text) {
        this(text, 0, Assert.notNull(text).length());
    }

    /**
     * Constructs a new {@code CharSequenceReader}.
     *
     * @param text         The {@link CharSequence} to read from.
     * @param startInclude The starting position (inclusive).
     * @param endExclude   The ending position (exclusive).
     * @throws IllegalArgumentException if {@code startInclude} is negative or {@code endExclude} is less than or equal
     *                                  to {@code startInclude}.
     */
    public CharSequenceReader(CharSequence text, final int startInclude, final int endExclude) {
        Assert.isTrue(startInclude >= 0, "Start index is less than zero: {}", startInclude);
        Assert.isTrue(endExclude > startInclude, "End index is less than start {}: {}", startInclude, endExclude);

        if (null == text) {
            text = Normal.EMPTY;
        }
        this.text = text;
        final int length = text.length();
        this.start = Math.min(length, startInclude);
        this.end = Math.min(length, endExclude);

        this.next = startInclude;
        this.mark = startInclude;
    }

    /**
     * Tells whether this stream supports the mark operation.
     *
     * @return True, as this stream supports the mark operation.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the present position in the stream.
     *
     * @param readAheadLimit The maximum limit of characters that can be read before the mark position becomes invalid.
     *                       This parameter is ignored in this implementation.
     */
    @Override
    public void mark(final int readAheadLimit) {
        mark = next;
    }

    /**
     * Resets the stream to the most recent mark.
     */
    @Override
    public void reset() {
        next = mark;
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been reached.
     */
    @Override
    public int read() {
        if (next >= end) {
            return Normal.__1;
        }
        return text.charAt(next++);
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param array  The array to transfer characters into.
     * @param offset The offset at which to start writing characters.
     * @param length The maximum number of characters to read.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws NullPointerException      if {@code array} is null.
     * @throws IndexOutOfBoundsException if {@code offset} is negative, {@code length} is negative, or
     *                                   {@code offset + length} is greater than {@code array.length}.
     */
    @Override
    public int read(final char[] array, final int offset, final int length) {
        if (next >= end) {
            return Normal.__1;
        }
        Objects.requireNonNull(array, "array");
        if (length < 0 || offset < 0 || offset + length > array.length) {
            throw new IndexOutOfBoundsException(
                    "Array Size=" + array.length + ", offset=" + offset + ", length=" + length);
        }

        if (text instanceof String) {
            final int count = Math.min(length, end - next);
            ((String) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }
        if (text instanceof StringBuilder) {
            final int count = Math.min(length, end - next);
            ((StringBuilder) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }
        if (text instanceof StringBuffer) {
            final int count = Math.min(length, end - next);
            ((StringBuffer) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }

        int count = 0;
        for (int i = 0; i < length; i++) {
            final int c = read();
            if (c == Normal.__1) {
                return count;
            }
            array[offset + i] = (char) c;
            count++;
        }
        return count;
    }

    /**
     * Skips characters.
     *
     * @param n The number of characters to skip.
     * @return The number of characters actually skipped.
     * @throws IllegalArgumentException if {@code n} is negative.
     */
    @Override
    public long skip(final long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
        }
        if (this.next >= this.end) {
            return 0;
        }
        final int dest = (int) Math.min(this.end, this.next + n);
        final int count = dest - this.next;
        this.next = dest;
        return count;
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input, false otherwise.
     */
    @Override
    public boolean ready() {
        return this.next < end;
    }

    /**
     * Closes the stream. This implementation resets the internal pointers to the beginning of the character sequence.
     */
    @Override
    public void close() {
        this.next = start;
        this.mark = start;
    }

    /**
     * Returns a string representation of the portion of the character sequence being read.
     *
     * @return A string representation of the character sequence.
     */
    @Override
    public String toString() {
        return this.text.subSequence(this.start, this.end).toString();
    }

}
