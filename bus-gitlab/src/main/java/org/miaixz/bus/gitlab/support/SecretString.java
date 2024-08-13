/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
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
package org.miaixz.bus.gitlab.support;

import java.util.Arrays;

/**
 * This class implements a CharSequence that can be cleared of it's contained characters. This class is utilized to pass
 * around secrets (passwords) instead of a String instance.
 */
public class SecretString implements CharSequence, AutoCloseable {

    private final char[] chars;

    public SecretString(CharSequence charSequence) {

        int length = charSequence.length();
        chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = charSequence.charAt(i);
        }
    }

    public SecretString(char[] chars) {
        this(chars, 0, chars.length);
    }

    public SecretString(char[] chars, int start, int end) {
        this.chars = new char[end - start];
        System.arraycopy(chars, start, this.chars, 0, this.chars.length);
    }

    @Override
    public char charAt(int index) {
        return chars[index];
    }

    @Override
    public void close() {
        clear();
    }

    @Override
    public int length() {
        return chars.length;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new SecretString(this.chars, start, end);
    }

    /**
     * Clear the contents of this SecretString instance by setting each character to 0. This is automatically done in
     * the finalize() method.
     */
    public void clear() {
        Arrays.fill(chars, '\0');
    }

    @Override
    public void finalize() throws Throwable {
        clear();
        super.finalize();
    }

}