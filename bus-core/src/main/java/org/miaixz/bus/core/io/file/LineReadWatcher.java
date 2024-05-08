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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.watcher.IgnoreWatcher;
import org.miaixz.bus.core.lang.function.ConsumerX;
import org.miaixz.bus.core.toolkit.FileKit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * 行处理的Watcher实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineReadWatcher extends IgnoreWatcher implements Runnable {

    private final RandomAccessFile randomAccessFile;
    private final Charset charset;
    private final ConsumerX<String> lineHandler;

    /**
     * 构造
     *
     * @param randomAccessFile {@link RandomAccessFile}
     * @param charset          编码
     * @param lineHandler      行处理器{@link ConsumerX}实现
     */
    public LineReadWatcher(RandomAccessFile randomAccessFile, Charset charset, ConsumerX<String> lineHandler) {
        this.randomAccessFile = randomAccessFile;
        this.charset = charset;
        this.lineHandler = lineHandler;
    }

    @Override
    public void run() {
        onModify(null, null);
    }

    @Override
    public void onModify(WatchEvent<?> event, Path currentPath) {
        final RandomAccessFile randomAccessFile = this.randomAccessFile;
        final Charset charset = this.charset;
        final ConsumerX<String> lineHandler = this.lineHandler;

        try {
            final long currentLength = randomAccessFile.length();
            final long position = randomAccessFile.getFilePointer();
            if (position == currentLength) {
                // 内容长度不变时忽略此次事件
                return;
            } else if (currentLength < position) {
                // 如果内容变短或变0,说明文件做了删改或清空,回到内容末尾或0
                randomAccessFile.seek(currentLength);
                return;
            }

            // 读取行
            FileKit.readLines(randomAccessFile, charset, lineHandler);

            // 记录当前读到的位置
            randomAccessFile.seek(currentLength);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

}
