/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.io.reader;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.function.XWrapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * {@link Reader} 包装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderWrapper extends Reader implements XWrapper<Reader> {

    protected final Reader raw;

    /**
     * 构造
     *
     * @param reader {@link Reader}
     */
    public ReaderWrapper(final Reader reader) {
        this.raw = Assert.notNull(reader);
    }

    @Override
    public Reader getRaw() {
        return this.raw;
    }

    @Override
    public int read() throws IOException {
        return raw.read();
    }

    @Override
    public int read(final CharBuffer target) throws IOException {
        return raw.read(target);
    }

    @Override
    public int read(final char[] cbuf) throws IOException {
        return raw.read(cbuf);
    }

    @Override
    public int read(final char[] buffer, final int off, final int len) throws IOException {
        return raw.read(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        raw.close();
    }

}
