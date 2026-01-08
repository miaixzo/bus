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
package org.miaixz.bus.core.text;

import java.io.IOException;

/**
 * AutoCloseable wrapper for pooled StringBuilder instances.
 *
 * <p>
 * This class wraps a StringBuilder from {@link StringBuilderPool} and implements {@link AutoCloseable}, enabling
 * automatic resource management through try-with-resources statements. When closed, the wrapped StringBuilder is
 * automatically released back to the pool.
 * </p>
 *
 * <h2>Usage Example</h2>
 * 
 * <pre>
 * <code>
 * try (PooledStringBuilder sb = StringBuilderPool.acquire(100)) {
 *     sb.append("SELECT * FROM users WHERE id = ").append(userId);
 *     return sb.toString();
 * }  // Automatically released
 * </code>
 * </pre>
 *
 * <h2>Method Delegation</h2>
 * <p>
 * This class delegates all StringBuilder methods to the underlying instance, providing a transparent wrapper that
 * behaves exactly like a regular StringBuilder.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is not thread-safe. Each instance should be used by a single thread only.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class PooledStringBuilder implements Appendable, CharSequence, AutoCloseable {

    /**
     * The underlying StringBuilder instance.
     */
    private final StringBuilder delegate;

    /**
     * Flag indicating whether this instance has been closed.
     */
    private boolean closed = false;

    /**
     * Creates a new pooled StringBuilder wrapper.
     *
     * @param delegate the underlying StringBuilder instance
     */
    PooledStringBuilder(StringBuilder delegate) {
        this.delegate = delegate;
    }

    /**
     * Appends the string representation of the {@code Object} argument.
     *
     * @param obj an {@code Object}
     * @return a reference to this object
     */
    public PooledStringBuilder append(Object obj) {
        delegate.append(obj);
        return this;
    }

    /**
     * Appends the specified string to this character sequence.
     *
     * @param str a string
     * @return a reference to this object
     */
    public PooledStringBuilder append(String str) {
        delegate.append(str);
        return this;
    }

    /**
     * Appends the specified {@code StringBuffer} to this sequence.
     *
     * @param sb the {@code StringBuffer} to append
     * @return a reference to this object
     */
    public PooledStringBuilder append(StringBuffer sb) {
        delegate.append(sb);
        return this;
    }

    /**
     * Appends the specified character sequence to this sequence.
     *
     * @param s The character sequence to append.
     * @return A reference to this object.
     */
    @Override
    public PooledStringBuilder append(CharSequence s) {
        delegate.append(s);
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this sequence.
     *
     * @param s     The character sequence to append.
     * @param start The index of the first character in the subsequence.
     * @param end   The index of the character following the last character in the subsequence.
     * @return A reference to this object.
     */
    @Override
    public PooledStringBuilder append(CharSequence s, int start, int end) {
        delegate.append(s, start, end);
        return this;
    }

    /**
     * Appends the specified character array to this sequence.
     *
     * @param str the characters to be appended
     * @return a reference to this object
     */
    public PooledStringBuilder append(char[] str) {
        delegate.append(str);
        return this;
    }

    /**
     * Appends the string representation of a subarray of the {@code char} array argument to this sequence.
     *
     * @param str    the characters to be appended
     * @param offset the index of the first {@code char} to append
     * @param len    the number of {@code char}s to append
     * @return a reference to this object
     */
    public PooledStringBuilder append(char[] str, int offset, int len) {
        delegate.append(str, offset, len);
        return this;
    }

    /**
     * Appends the string representation of the {@code boolean} argument to the sequence.
     *
     * @param b a {@code boolean}
     * @return a reference to this object
     */
    public PooledStringBuilder append(boolean b) {
        delegate.append(b);
        return this;
    }

    /**
     * Appends the specified character to this sequence.
     *
     * @param c The character to append.
     * @return A reference to this object.
     */
    @Override
    public PooledStringBuilder append(char c) {
        delegate.append(c);
        return this;
    }

    /**
     * Appends the string representation of the {@code int} argument to this sequence.
     *
     * @param i an {@code int}
     * @return a reference to this object
     */
    public PooledStringBuilder append(int i) {
        delegate.append(i);
        return this;
    }

    /**
     * Appends the string representation of the {@code long} argument to this sequence.
     *
     * @param lng a {@code long}
     * @return a reference to this object
     */
    public PooledStringBuilder append(long lng) {
        delegate.append(lng);
        return this;
    }

    /**
     * Appends the string representation of the {@code float} argument to this sequence.
     *
     * @param f a {@code float}
     * @return a reference to this object
     */
    public PooledStringBuilder append(float f) {
        delegate.append(f);
        return this;
    }

    /**
     * Appends the string representation of the {@code double} argument to this sequence.
     *
     * @param d a {@code double}
     * @return a reference to this object
     */
    public PooledStringBuilder append(double d) {
        delegate.append(d);
        return this;
    }

    /**
     * Appends the string representation of the {@code codePoint} argument to this sequence.
     *
     * @param codePoint a Unicode code point
     * @return a reference to this object
     */
    public PooledStringBuilder appendCodePoint(int codePoint) {
        delegate.appendCodePoint(codePoint);
        return this;
    }

    /**
     * Removes the characters in a substring of this sequence.
     *
     * @param start the beginning index, inclusive
     * @param end   the ending index, exclusive
     * @return a reference to this object
     */
    public PooledStringBuilder delete(int start, int end) {
        delegate.delete(start, end);
        return this;
    }

    /**
     * Removes the {@code char} at the specified position in this sequence.
     *
     * @param index the index of the {@code char} to remove
     * @return a reference to this object
     */
    public PooledStringBuilder deleteCharAt(int index) {
        delegate.deleteCharAt(index);
        return this;
    }

    /**
     * Replaces the characters in a substring of this sequence with characters in the specified {@code String}.
     *
     * @param start the beginning index, inclusive
     * @param end   the ending index, exclusive
     * @param str   the string that will replace previous contents
     * @return a reference to this object
     */
    public PooledStringBuilder replace(int start, int end, String str) {
        delegate.replace(start, end, str);
        return this;
    }

    /**
     * Inserts the string representation of the {@code Object} argument into this character sequence.
     *
     * @param offset the offset
     * @param obj    an {@code Object}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, Object obj) {
        delegate.insert(offset, obj);
        return this;
    }

    /**
     * Inserts the string into this character sequence.
     *
     * @param offset the offset
     * @param str    a string
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, String str) {
        delegate.insert(offset, str);
        return this;
    }

    /**
     * Inserts the specified {@code CharSequence} into this sequence.
     *
     * @param dstOffset the offset
     * @param s         the sequence to be inserted
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int dstOffset, CharSequence s) {
        delegate.insert(dstOffset, s);
        return this;
    }

    /**
     * Inserts a subsequence of the specified {@code CharSequence} into this sequence.
     *
     * @param dstOffset the offset in this sequence
     * @param s         the sequence to be inserted
     * @param start     the starting index of the subsequence to be inserted
     * @param end       the end index of the subsequence to be inserted
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        delegate.insert(dstOffset, s, start, end);
        return this;
    }

    /**
     * Inserts the string representation of the {@code char} array argument into this sequence.
     *
     * @param offset the offset
     * @param str    a character array
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, char[] str) {
        delegate.insert(offset, str);
        return this;
    }

    /**
     * Inserts the string representation of a subarray of the {@code str} array argument into this sequence.
     *
     * @param index  position at which to insert subarray
     * @param str    a {@code char} array
     * @param offset the index of the first {@code char} in subarray to be inserted
     * @param len    the number of {@code char}s in the subarray to be inserted
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int index, char[] str, int offset, int len) {
        delegate.insert(index, str, offset, len);
        return this;
    }

    /**
     * Inserts the string representation of the {@code boolean} argument into this sequence.
     *
     * @param offset the offset
     * @param b      a {@code boolean}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, boolean b) {
        delegate.insert(offset, b);
        return this;
    }

    /**
     * Inserts the string representation of the {@code char} argument into this sequence.
     *
     * @param offset the offset
     * @param c      a {@code char}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, char c) {
        delegate.insert(offset, c);
        return this;
    }

    /**
     * Inserts the string representation of the second {@code int} argument into this sequence.
     *
     * @param offset the offset
     * @param i      an {@code int}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, int i) {
        delegate.insert(offset, i);
        return this;
    }

    /**
     * Inserts the string representation of the {@code long} argument into this sequence.
     *
     * @param offset the offset
     * @param l      a {@code long}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, long l) {
        delegate.insert(offset, l);
        return this;
    }

    /**
     * Inserts the string representation of the {@code float} argument into this sequence.
     *
     * @param offset the offset
     * @param f      a {@code float}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, float f) {
        delegate.insert(offset, f);
        return this;
    }

    /**
     * Inserts the string representation of the {@code double} argument into this sequence.
     *
     * @param offset the offset
     * @param d      a {@code double}
     * @return a reference to this object
     */
    public PooledStringBuilder insert(int offset, double d) {
        delegate.insert(offset, d);
        return this;
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring.
     *
     * @param str the substring to search for
     * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence
     */
    public int indexOf(String str) {
        return delegate.indexOf(str);
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring, starting at the
     * specified index.
     *
     * @param str       the substring to search for
     * @param fromIndex the index from which to start the search
     * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence
     */
    public int indexOf(String str, int fromIndex) {
        return delegate.indexOf(str, fromIndex);
    }

    /**
     * Returns the index within this string of the last occurrence of the specified substring.
     *
     * @param str the substring to search for
     * @return the index of the last occurrence of the specified substring, or -1 if there is no such occurrence
     */
    public int lastIndexOf(String str) {
        return delegate.lastIndexOf(str);
    }

    /**
     * Returns the index within this string of the last occurrence of the specified substring, starting at the specified
     * index.
     *
     * @param str       the substring to search for
     * @param fromIndex the index from which to start the search backwards
     * @return the index of the last occurrence of the specified substring, or -1 if there is no such occurrence
     */
    public int lastIndexOf(String str, int fromIndex) {
        return delegate.lastIndexOf(str, fromIndex);
    }

    /**
     * Causes this character sequence to be replaced by the reverse of the sequence.
     *
     * @return a reference to this object
     */
    public PooledStringBuilder reverse() {
        delegate.reverse();
        return this;
    }

    /**
     * Returns a string representation of this sequence.
     *
     * @return A string representation of this sequence.
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Returns the length of this character sequence.
     *
     * @return The number of characters in this sequence.
     */
    @Override
    public int length() {
        return delegate.length();
    }

    /**
     * Returns the current capacity.
     *
     * @return the current capacity
     */
    public int capacity() {
        return delegate.capacity();
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     *
     * @param minimumCapacity the minimum desired capacity
     */
    public void ensureCapacity(int minimumCapacity) {
        delegate.ensureCapacity(minimumCapacity);
    }

    /**
     * Attempts to reduce storage used for the character sequence.
     */
    public void trimToSize() {
        delegate.trimToSize();
    }

    /**
     * Sets the length of the character sequence.
     *
     * @param newLength the new length
     */
    public void setLength(int newLength) {
        delegate.setLength(newLength);
    }

    /**
     * Returns the character at the specified index.
     *
     * @param index The index of the character to return.
     * @return The character at the specified index.
     */
    @Override
    public char charAt(int index) {
        return delegate.charAt(index);
    }

    /**
     * Returns the character (Unicode code point) at the specified index.
     *
     * @param index the index to the {@code char} values
     * @return the code point value of the character at the index
     */
    public int codePointAt(int index) {
        return delegate.codePointAt(index);
    }

    /**
     * Returns the character (Unicode code point) before the specified index.
     *
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index
     */
    public int codePointBefore(int index) {
        return delegate.codePointBefore(index);
    }

    /**
     * Returns the number of Unicode code points in the specified text range.
     *
     * @param beginIndex the index to the first {@code char} of the text range
     * @param endIndex   the index after the last {@code char} of the text range
     * @return the number of Unicode code points in the specified text range
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return delegate.codePointCount(beginIndex, endIndex);
    }

    /**
     * Returns the index within this sequence that is offset from the given {@code index} by {@code codePointOffset}
     * code points.
     *
     * @param index           the index to be offset
     * @param codePointOffset the offset in code points
     * @return the index within this sequence
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return delegate.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * Characters are copied from this sequence into the destination character array {@code dst}.
     *
     * @param srcBegin start copying at this offset
     * @param srcEnd   stop copying at this offset
     * @param dst      the array to copy the data into
     * @param dstBegin offset into {@code dst}
     */
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * The character at the specified index is set to {@code ch}.
     *
     * @param index the index of the character to modify
     * @param ch    the new character
     */
    public void setCharAt(int index, char ch) {
        delegate.setCharAt(index, ch);
    }

    /**
     * Returns a new {@code String} that contains a subsequence of characters currently contained in this character
     * sequence.
     *
     * @param start the beginning index, inclusive
     * @return the new string
     */
    public String substring(int start) {
        return delegate.substring(start);
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * @param start The start index of the subsequence (inclusive).
     * @param end   The end index of the subsequence (exclusive).
     * @return The specified subsequence.
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return delegate.subSequence(start, end);
    }

    /**
     * Returns a new {@code String} that contains a subsequence of characters currently contained in this sequence.
     *
     * @param start the beginning index, inclusive
     * @param end   the ending index, exclusive
     * @return the new string
     */
    public String substring(int start, int end) {
        return delegate.substring(start, end);
    }

    /**
     * Releases the wrapped StringBuilder back to the pool.
     *
     * <p>
     * This method is called automatically when used with try-with-resources. After closing, this instance should not be
     * used.
     * </p>
     *
     * @throws IOException never thrown, but required by AutoCloseable interface
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            StringBuilderPool.release(delegate);
            closed = true;
        }
    }

}
