/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 读取和写入目标文件
 *
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public final class FileOperator {

    private final FileChannel fileChannel;

    public FileOperator(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    /**
     * 将{@code byteCount}字节从{@code source}写到{@code pos}的文件中
     *
     * @param pos       写入大小
     * @param source    缓存流信息
     * @param byteCount 字节流大小
     * @throws IOException 异常
     */
    public void write(long pos, Buffer source, long byteCount) throws IOException {
        if (byteCount < 0 || byteCount > source.size()) throw new IndexOutOfBoundsException();

        while (byteCount > 0L) {
            long bytesWritten = fileChannel.transferFrom(source, pos, byteCount);
            pos += bytesWritten;
            byteCount -= bytesWritten;
        }
    }

    /**
     * 将{@code byteCount}字节从文件{@code pos}复制到{@code source}
     * 调用者有责任确保有足够的字节来读取:如果没有，这个方法会抛出一个{@link EOFException}
     *
     * @param pos       写入大小
     * @param sink      缓存流信息
     * @param byteCount 字节流大小
     * @throws IOException 异常
     */
    public void read(long pos, Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IndexOutOfBoundsException();

        while (byteCount > 0L) {
            long bytesRead = fileChannel.transferTo(pos, byteCount, sink);
            pos += bytesRead;
            byteCount -= bytesRead;
        }
    }

}
