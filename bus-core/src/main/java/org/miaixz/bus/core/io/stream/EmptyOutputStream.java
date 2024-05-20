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
package org.miaixz.bus.core.io.stream;

import java.io.OutputStream;

/**
 * 此OutputStream写出数据到<b>/dev/null</b>，即忽略所有数据
 * 来自 Apache Commons io
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmptyOutputStream extends OutputStream {

    /**
     * 单例
     */
    public static final EmptyOutputStream INSTANCE = new EmptyOutputStream();

    private EmptyOutputStream() {
    }

    /**
     * 什么也不做，写出到{@code /dev/null}.
     *
     * @param b   写出的数据
     * @param off 开始位置
     * @param len 长度
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        // to /dev/null
    }

    /**
     * 什么也不做，写出到 {@code /dev/null}.
     *
     * @param b 写出的数据
     */
    @Override
    public void write(final int b) {
        // to /dev/null
    }

    /**
     * 什么也不做，写出到 {@code /dev/null}.
     *
     * @param b 写出的数据
     */
    @Override
    public void write(final byte[] b) {
        // to /dev/null
    }

}
