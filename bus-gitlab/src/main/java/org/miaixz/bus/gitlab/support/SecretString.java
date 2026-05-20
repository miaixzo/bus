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
package org.miaixz.bus.gitlab.support;

import java.util.Arrays;

/**
 * This class implements a CharSequence that can be cleared of it's contained characters. This class is utilized to pass
 * around secrets (passwords) instead of a String instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SecretString implements CharSequence, AutoCloseable {

    private final char[] chars;

    /**
     * Constructs a new {@code SecretString} instance.
     *
     * @param charSequence the char sequence value
     */

    public SecretString(CharSequence charSequence) {

        int length = charSequence.length();
        chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = charSequence.charAt(i);
        }
    }

    /**
     * Constructs a new {@code SecretString} instance.
     *
     * @param chars the chars value
     */

    public SecretString(char[] chars) {
        this(chars, 0, chars.length);
    }

    /**
     * Constructs a new {@code SecretString} instance.
     *
     * @param chars the chars value
     * @param start the start value
     * @param end   the end value
     */

    public SecretString(char[] chars, int start, int end) {
        this.chars = new char[end - start];
        System.arraycopy(chars, start, this.chars, 0, this.chars.length);
    }

    /**
     * Executes the char at operation.
     *
     * @param index the index value
     * @return the result
     */

    @Override
    public char charAt(int index) {
        return chars[index];
    }

    /**
     * Executes the close operation.
     */

    @Override
    public void close() {
        clear();
    }

    /**
     * Executes the length operation.
     *
     * @return the result
     */

    @Override
    public int length() {
        return chars.length;
    }

    /**
     * Executes the sub sequence operation.
     *
     * @param start the start value
     * @param end   the end value
     * @return the result
     */

    @Override
    public CharSequence subSequence(int start, int end) {
        return new SecretString(this.chars, start, end);
    }

    /**
     * Clear the contents of this SecretString instance by setting each character to 0.
     */
    public void clear() {
        Arrays.fill(chars, '\0');
    }

}
