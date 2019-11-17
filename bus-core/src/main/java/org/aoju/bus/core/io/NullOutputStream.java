/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 此OutputStream写出数据到<b>/dev/null</b>,既忽略所有数据
 * 来自 Apache Commons io
 *
 * @author Kimi Liu
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class NullOutputStream extends OutputStream {

    /**
     * 什么也不做,写出到<code>/dev/null</code>.
     *
     * @param b   写出的数据
     * @param off 开始位置
     * @param len 长度
     */
    @Override
    public void write(byte[] b, int off, int len) {
        // to /dev/null
    }

    /**
     * 什么也不做,写出到 <code>/dev/null</code>.
     *
     * @param b 写出的数据
     */
    @Override
    public void write(int b) {
        // to /dev/null
    }

    /**
     * 什么也不做,写出到 <code>/dev/null</code>.
     *
     * @param b 写出的数据
     * @throws IOException 不抛出
     */
    @Override
    public void write(byte[] b) throws IOException {
        // to /dev/null
    }

}
