/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件追加器
 * 持有一个文件,在内存中积累一定量的数据后统一追加到文件
 * 此类只有在写入文件时打开文件,并在写入结束后关闭之 因此此类不需要关闭
 * 在调用append方法后会缓存于内存,只有超过容量后才会一次性写入文件,因此内存中随时有剩余未写入文件的内容,在最后必须调用flush方法将剩余内容刷入文件
 *
 * @author Kimi Liu
 * @version 5.5.3
 * @since JDK 1.8+
 */
public class FileAppender implements Serializable {

    private FileWriter writer;
    /**
     * 内存中持有的字符串数
     */
    private int capacity;
    /**
     * 追加内容是否为新行
     */
    private boolean isNewLineMode;
    private List<String> list = new ArrayList<>(100);

    /**
     * 构造
     *
     * @param destFile      目标文件
     * @param capacity      当行数积累多少条时刷入到文件
     * @param isNewLineMode 追加内容是否为新行
     */
    public FileAppender(File destFile, int capacity, boolean isNewLineMode) {
        this(destFile, org.aoju.bus.core.lang.Charset.UTF_8, capacity, isNewLineMode);
    }

    /**
     * 构造
     *
     * @param destFile      目标文件
     * @param charset       编码
     * @param capacity      当行数积累多少条时刷入到文件
     * @param isNewLineMode 追加内容是否为新行
     */
    public FileAppender(File destFile, Charset charset, int capacity, boolean isNewLineMode) {
        this.capacity = capacity;
        this.isNewLineMode = isNewLineMode;
        this.writer = FileWriter.create(destFile, charset);
    }

    /**
     * 追加
     *
     * @param line 行
     * @return this
     */
    public FileAppender append(String line) {
        if (list.size() >= capacity) {
            flush();
        }
        list.add(line);
        return this;
    }

    /**
     * 刷入到文件
     *
     * @return this
     */
    public FileAppender flush() {
        try (PrintWriter pw = writer.getPrintWriter(true)) {
            for (String str : list) {
                pw.print(str);
                if (isNewLineMode) {
                    pw.println();
                }
            }
        }
        list.clear();
        return this;
    }

}
