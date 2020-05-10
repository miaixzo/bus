/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io;

import org.aoju.bus.core.lang.Symbol;

import java.io.IOException;

/**
 * 将调用转发给另一个调用的{@link Sink}
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public abstract class DelegateSink implements Sink {

    private final Sink delegate;

    public DelegateSink(Sink delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate == null");
        this.delegate = delegate;
    }

    public final Sink delegate() {
        return delegate;
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        delegate.write(source, byteCount);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Symbol.PARENTHESE_LEFT + delegate.toString() + Symbol.PARENTHESE_RIGHT;
    }

}
