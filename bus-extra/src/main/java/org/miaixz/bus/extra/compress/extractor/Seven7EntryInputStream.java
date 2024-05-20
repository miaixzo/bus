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
package org.miaixz.bus.extra.compress.extractor;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 7z解压中文件流读取的封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Seven7EntryInputStream extends InputStream {

    private final SevenZFile sevenZFile;
    private final long size;
    private long readSize = 0;

    /**
     * 构造
     *
     * @param sevenZFile {@link SevenZFile}
     * @param entry      {@link SevenZArchiveEntry}
     */
    public Seven7EntryInputStream(final SevenZFile sevenZFile, final SevenZArchiveEntry entry) {
        this(sevenZFile, entry.getSize());
    }

    /**
     * 构造
     *
     * @param sevenZFile {@link SevenZFile}
     * @param size       读取长度
     */
    public Seven7EntryInputStream(final SevenZFile sevenZFile, final long size) {
        this.sevenZFile = sevenZFile;
        this.size = size;
    }

    @Override
    public int available() throws IOException {
        try {
            return Math.toIntExact(this.size);
        } catch (final ArithmeticException e) {
            throw new IOException("Entry size is too large!(max than Integer.MAX)", e);
        }
    }

    /**
     * 获取读取的长度（字节数）
     *
     * @return 读取的字节数
     */
    public long getReadSize() {
        return this.readSize;
    }

    @Override
    public int read() throws IOException {
        this.readSize++;
        return this.sevenZFile.read();
    }
}
