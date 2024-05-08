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
package org.miaixz.bus.core.collection.Iterator;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.IoKit;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 将Reader包装为一个按照行读取的Iterator
 * 此对象遍历结束后,应关闭之,推荐使用方式:
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineIterator extends ComputeIterator<String> implements IterableIterator<String>, Closeable, Serializable {

    private static final long serialVersionUID = 1L;

    private final BufferedReader bufferedReader;

    /**
     * 构造
     *
     * @param in      {@link InputStream}
     * @param charset 编码
     * @throws IllegalArgumentException reader为null抛出此异常
     */
    public LineIterator(InputStream in, Charset charset) throws IllegalArgumentException {
        this(IoKit.getReader(in, charset));
    }

    /**
     * 构造
     *
     * @param reader {@link Reader}对象，不能为null
     * @throws IllegalArgumentException reader为null抛出此异常
     */
    public LineIterator(Reader reader) throws IllegalArgumentException {
        Assert.notNull(reader, "Reader must not be null");
        this.bufferedReader = IoKit.getReader(reader);
    }

    @Override
    protected String computeNext() {
        try {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    return null;
                } else if (isValidLine(line)) {
                    return line;
                }
                // 无效行，则跳过进入下一行
            }
        } catch (IOException ioe) {
            close();
            throw new InternalException(ioe);
        }
    }

    /**
     * 关闭Reader
     */
    @Override
    public void close() {
        super.finish();
        IoKit.close(bufferedReader);
    }

    /**
     * 重写此方法来判断是否每一行都被返回，默认全部为true
     *
     * @param line 需要验证的行
     * @return 是否通过验证
     */
    protected boolean isValidLine(String line) {
        return true;
    }

}
