/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.collection;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.IoKit;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 将Reader包装为一个按照行读取的Iterator
 * 此对象遍历结束后,应关闭之,推荐使用方式:
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class LineIter implements Iterator<String>, Iterable<String>, Closeable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓冲区读取.
     */
    private final BufferedReader bufferedReader;
    /**
     * 当前行.
     */
    private String cachedLine;
    /**
     * 指示迭代器是否已被完全读取的标志.
     */
    private boolean finished = false;

    /**
     * 构造
     *
     * @param in      {@link InputStream}
     * @param charset 编码
     * @throws IllegalArgumentException reader为null抛出此异常
     */
    public LineIter(InputStream in, Charset charset) throws IllegalArgumentException {
        this(IoKit.getReader(in, charset));
    }

    /**
     * 构造
     *
     * @param reader {@link Reader}对象,不能为null
     * @throws IllegalArgumentException reader为null抛出此异常
     */
    public LineIter(Reader reader) throws IllegalArgumentException {
        Assert.notNull(reader, "Reader must not be null");
        this.bufferedReader = IoKit.getReader(reader);
    }


    /**
     * 判断{@link Reader}是否可以存在下一行.
     *
     * @return {@code true} 表示有更多行
     * @throws InstrumentException 内部异常
     */
    @Override
    public boolean hasNext() throws InstrumentException {
        if (cachedLine != null) {
            return true;
        } else if (finished) {
            return false;
        } else {
            try {
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        finished = true;
                        return false;
                    } else if (isValidLine(line)) {
                        cachedLine = line;
                        return true;
                    }
                }
            } catch (IOException ioe) {
                close();
                throw new InstrumentException(ioe);
            }
        }
    }

    /**
     * 返回下一行内容
     *
     * @return 下一行内容
     * @throws NoSuchElementException 没有新行
     */
    @Override
    public String next() throws NoSuchElementException {
        return nextLine();
    }

    /**
     * 返回下一行
     *
     * @return 下一行
     * @throws NoSuchElementException 没有更多行
     */
    public String nextLine() throws NoSuchElementException {
        if (false == hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        String currentLine = this.cachedLine;
        this.cachedLine = null;
        return currentLine;
    }

    /**
     * 关闭Reader
     */
    @Override
    public void close() {
        finished = true;
        IoKit.close(bufferedReader);
        cachedLine = null;
    }

    /**
     * 不支持移除
     *
     * @throws UnsupportedOperationException 始终抛出此异常
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove unsupported on LineIterator");
    }

    /**
     * 重写此方法来判断是否每一行都被返回,默认全部为true
     *
     * @param line 需要验证的行
     * @return 是否通过验证
     */
    protected boolean isValidLine(String line) {
        return true;
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

}
