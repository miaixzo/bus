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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.thread.lock.Lock;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件追加器
 * 持有一个文件，在内存中积累一定量的数据后统一追加到文件
 * 此类只有在写入文件时打开文件，并在写入结束后关闭之。因此此类不需要关闭
 * 在调用append方法后会缓存于内存，只有超过容量后才会一次性写入文件，因此内存中随时有剩余未写入文件的内容，在最后必须调用flush方法将剩余内容刷入文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileAppender implements Serializable {

    private static final long serialVersionUID = -1L;

    private final FileWriter writer;
    /**
     * 内存中持有的字符串数
     */
    private final int capacity;
    /**
     * 追加内容是否为新行
     */
    private final boolean isNewLineMode;
    /**
     * 数据行缓存
     */
    private final List<String> list;
    /**
     * 写出锁，用于保护写出线程安全
     */
    private final java.util.concurrent.locks.Lock lock;

    /**
     * 构造
     *
     * @param destFile      目标文件
     * @param capacity      当行数积累多少条时刷入到文件
     * @param isNewLineMode 追加内容是否为新行
     */
    public FileAppender(final File destFile, final int capacity, final boolean isNewLineMode) {
        this(destFile, Charset.UTF_8, capacity, isNewLineMode);
    }

    /**
     * 构造
     *
     * @param destFile      目标文件
     * @param charset       编码
     * @param capacity      当行数积累多少条时刷入到文件
     * @param isNewLineMode 追加内容是否为新行
     */
    public FileAppender(final File destFile, final java.nio.charset.Charset charset, final int capacity, final boolean isNewLineMode) {
        this(destFile, charset, capacity, isNewLineMode, null);
    }

    /**
     * 构造
     *
     * @param destFile      目标文件
     * @param charset       编码
     * @param capacity      当行数积累多少条时刷入到文件
     * @param isNewLineMode 追加内容是否为新行
     * @param lock          是否加锁，添加则使用给定锁保护写出，保证线程安全，{@code null}则表示无锁
     */
    public FileAppender(final File destFile, final java.nio.charset.Charset charset, final int capacity, final boolean isNewLineMode, final java.util.concurrent.locks.Lock lock) {
        this.capacity = capacity;
        this.list = new ArrayList<>(capacity);
        this.isNewLineMode = isNewLineMode;
        this.writer = FileWriter.of(destFile, charset);
        this.lock = ObjectKit.defaultIfNull(lock, Lock::getNoLock);
    }

    /**
     * 追加
     *
     * @param line 行
     * @return this
     */
    public FileAppender append(final String line) {
        if (list.size() >= capacity) {
            flush();
        }

        this.lock.lock();
        try {
            list.add(line);
        } finally {
            this.lock.unlock();
        }
        return this;
    }

    /**
     * 刷入到文件
     *
     * @return this
     */
    public FileAppender flush() {
        this.lock.lock();
        try {
            try (final PrintWriter pw = writer.getPrintWriter(true)) {
                for (final String text : list) {
                    pw.print(text);
                    if (isNewLineMode) {
                        pw.println();
                    }
                }
            }
            list.clear();
        } finally {
            this.lock.unlock();
        }
        return this;
    }

}
