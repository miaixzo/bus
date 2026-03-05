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
package org.miaixz.bus.core.text;

import java.util.Arrays;
import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * CharArray wrapper, providing zero-copy array operations. This class wraps a char array and provides methods to
 * manipulate it, implementing {@link CharSequence} and {@link Iterable} for character iteration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharArray implements CharSequence, Iterable<Character> {

    /**
     * The internal character array.
     */
    private final char[] value;

    /**
     * Constructs a new {@code CharArray} from a {@link String}. The string's characters are converted into a char
     * array.
     *
     * @param value The string value to wrap.
     */
    public CharArray(final String value) {
        this(value.toCharArray(), false);
    }

    /**
     * Constructs a new {@code CharArray} from a char array. This constructor allows for optional copying of the input
     * array.
     *
     * @param value The char array to wrap.
     * @param copy  If {@code true}, the input array is copied; otherwise, the array is reused (zero-copy).
     */
    public CharArray(final char[] value, final boolean copy) {
        this.value = copy ? value.clone() : value;
    }

    /**
     * Returns the length of the character array.
     *
     * @return The number of characters in the array.
     */
    @Override
    public int length() {
        return value.length;
    }

    /**
     * Returns the character at the specified index. Supports negative indexing, where -1 refers to the last character.
     *
     * @param index The index of the character to return. Supports negative values.
     * @return The character at the specified index.
     */
    @Override
    public char charAt(int index) {
        if (index < 0) {
            index += value.length;
        }
        return value[index];
    }

    /**
     * Sets the character at the specified index. Supports negative indexing, where -1 refers to the last position.
     *
     * @param index The index at which to set the character. Supports negative values (e.g., -1 for the last character).
     * @param c     The character to set.
     * @return This {@code CharArray} instance, allowing for method chaining.
     */
    public CharArray set(int index, final char c) {
        if (index < 0) {
            index += value.length;
        }
        value[index] = c;
        return this;
    }

    /**
     * Returns the underlying character array without creating a copy.
     *
     * @return The internal char array.
     */
    public char[] array() {
        return this.value;
    }

    /**
     * Returns a new {@code CharArray} that is a subsequence of this sequence.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return a new {@code CharArray} containing the specified subsequence
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new CharArray(ArrayKit.sub(value, start, end), false);
    }

    /**
     * Checks if this {@code CharArray} is equal to another object. Two {@code CharArray} objects are equal if they
     * contain the same characters in the same order.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CharArray charArray = (CharArray) o;
        return Arrays.equals(value, charArray.value);
    }

    /**
     * Returns the hash code of this {@code CharArray} based on its contents.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    /**
     * Returns an iterator over the characters in this array.
     *
     * @return An iterator for the characters.
     */
    @Override
    public Iterator<Character> iterator() {
        return new ArrayIterator<>(this.value);
    }

    /**
     * Returns a string representation of this {@code CharArray}.
     *
     * @return A string containing the characters in this array.
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

}
