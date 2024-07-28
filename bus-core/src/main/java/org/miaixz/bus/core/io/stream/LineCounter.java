/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.stream;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 行数计数器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineCounter implements Closeable {

    private final InputStream is;
    private final int bufferSize;

    private int count = -1;

    /**
     * 构造
     *
     * @param is         输入流
     * @param bufferSize 缓存大小，小于1则使用默认的1024
     */
    public LineCounter(final InputStream is, final int bufferSize) {
        this.is = is;
        this.bufferSize = bufferSize < 1 ? 1024 : bufferSize;
    }

    /**
     * 获取行数
     *
     * @return 行数
     */
    public int getCount() {
        if (this.count < 0) {
            try {
                this.count = count();
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return this.count;
    }

    @Override
    public void close() throws IOException {
        if (null != this.is) {
            this.is.close();
        }
    }

    private int count() throws IOException {
        final byte[] c = new byte[bufferSize];
        int readChars = is.read(c);
        if (readChars == -1) {
            // 空文件，返回0
            return 0;
        }

        // 起始行为1
        // 如果只有一行，无换行符，则读取结束后返回1
        // 如果多行，最后一行无换行符，最后一行需要单独计数
        // 如果多行，最后一行有换行符，则空行算作一行
        int count = 1;
        while (readChars == bufferSize) {
            for (int i = 0; i < bufferSize; i++) {
                if (c[i] == Symbol.C_LF) {
                    ++count;
                }
            }
            readChars = is.read(c);
        }

        // 计算剩余字符数
        while (readChars != -1) {
            for (int i = 0; i < readChars; i++) {
                if (c[i] == Symbol.C_LF) {
                    ++count;
                }
            }
            readChars = is.read(c);
        }

        return count;
    }

}
