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
package org.aoju.bus.core.io.file;

import org.aoju.bus.core.io.LineHandler;
import org.aoju.bus.core.io.watchers.SimpleWatcher;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * 行处理的Watcher实现
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class LineReadWatcher extends SimpleWatcher implements Runnable {

    private RandomAccessFile randomAccessFile;
    private Charset charset;
    private LineHandler lineHandler;

    /**
     * 构造
     *
     * @param randomAccessFile {@link RandomAccessFile}
     * @param charset          编码
     * @param lineHandler      行处理器{@link LineHandler}实现
     */
    public LineReadWatcher(RandomAccessFile randomAccessFile, Charset charset, LineHandler lineHandler) {
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
        final LineHandler lineHandler = this.lineHandler;

        try {
            final long currentLength = randomAccessFile.length();
            final long position = randomAccessFile.getFilePointer();
            if (0 == currentLength || position == currentLength) {
                // 内容长度不变时忽略此次事件
                return;
            } else if (currentLength < position) {
                // 如果内容变短,说明文件做了删改,回到内容末尾
                randomAccessFile.seek(currentLength);
                return;
            }

            // 读取行
            FileUtils.readLines(randomAccessFile, charset, lineHandler);

            // 记录当前读到的位置
            randomAccessFile.seek(currentLength);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

}
