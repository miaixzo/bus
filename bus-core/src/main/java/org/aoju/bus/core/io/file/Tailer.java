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

import org.aoju.bus.core.io.LineHandler;
import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Stack;
import java.util.concurrent.*;

/**
 * 文件内容跟随器，实现类似Linux下"tail -f"命令功能
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public class Tailer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编码
     */
    private Charset charset;
    /**
     * 行处理器
     */
    private LineHandler lineHandler;
    /**
     * 初始读取的行数
     */
    private int initReadLine;
    /**
     * 定时任务检查间隔时长
     */
    private long period;

    private RandomAccessFile randomAccessFile;
    private ScheduledExecutorService executorService;

    /**
     * 构造，默认UTF-8编码
     *
     * @param file        文件
     * @param lineHandler 行处理器
     */
    public Tailer(File file, LineHandler lineHandler) {
        this(file, lineHandler, 0);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param file         文件
     * @param lineHandler  行处理器
     * @param initReadLine 启动时预读取的行数
     */
    public Tailer(File file, LineHandler lineHandler, int initReadLine) {
        this(file, org.aoju.bus.core.lang.Charset.UTF_8, lineHandler, initReadLine, Fields.Unit.SECOND.getMillis());
    }

    /**
     * 构造
     *
     * @param file        文件
     * @param charset     编码
     * @param lineHandler 行处理器
     */
    public Tailer(File file, Charset charset, LineHandler lineHandler) {
        this(file, charset, lineHandler, 0, Fields.Unit.SECOND.getMillis());
    }

    /**
     * 构造
     *
     * @param file         文件
     * @param charset      编码
     * @param lineHandler  行处理器
     * @param initReadLine 启动时预读取的行数
     * @param period       检查间隔
     */
    public Tailer(File file, Charset charset, LineHandler lineHandler, int initReadLine, long period) {
        checkFile(file);
        this.charset = charset;
        this.lineHandler = lineHandler;
        this.period = period;
        this.initReadLine = initReadLine;
        this.randomAccessFile = FileUtils.createRandomAccessFile(file, FileMode.r);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 检查文件有效性
     *
     * @param file 文件
     */
    private static void checkFile(File file) {
        if (false == file.exists()) {
            throw new InstrumentException("File [{}] not exist !", file.getAbsolutePath());
        }
        if (false == file.isFile()) {
            throw new InstrumentException("Path [{}] is not a file !", file.getAbsolutePath());
        }
    }

    /**
     * 开始监听
     */
    public void start() {
        start(false);
    }

    /**
     * 开始监听
     *
     * @param async 是否异步执行
     */
    public void start(boolean async) {
        // 初始读取
        try {
            this.readTail();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }

        final LineReadWatcher lineReadWatcher = new LineReadWatcher(this.randomAccessFile, this.charset, this.lineHandler);
        final ScheduledFuture<?> scheduledFuture = this.executorService.scheduleAtFixedRate(//
                lineReadWatcher,
                0,
                this.period, TimeUnit.MILLISECONDS
        );

        if (false == async) {
            try {
                scheduledFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 预读取行
     *
     * @throws IOException IO异常
     */
    private void readTail() throws IOException {
        final long len = this.randomAccessFile.length();

        if (initReadLine > 0) {
            Stack<String> stack = new Stack<>();

            long start = this.randomAccessFile.getFilePointer();
            long nextEnd = len - 1;
            this.randomAccessFile.seek(nextEnd);
            int c;
            int currentLine = 0;
            while (nextEnd > start) {
                // 满
                if (currentLine > initReadLine) {
                    break;
                }

                c = this.randomAccessFile.read();
                if (c == Symbol.C_LF || c == Symbol.C_CR) {
                    // FileUtils.readLine(this.randomAccessFile, this.charset, this.lineHandler);
                    final String line = FileUtils.readLine(this.randomAccessFile, this.charset);
                    if (null != line) {
                        stack.push(line);
                    }
                    currentLine++;
                    nextEnd--;
                }
                nextEnd--;
                this.randomAccessFile.seek(nextEnd);
                if (nextEnd == 0) {
                    // 当文件指针退至文件开始处，输出第一行
                    // FileUtils.readLine(this.randomAccessFile, this.charset, this.lineHandler);
                    final String line = FileUtils.readLine(this.randomAccessFile, this.charset);
                    if (null != line) {
                        stack.push(line);
                    }
                    break;
                }
            }

            // 输出缓存栈中的内容
            while (false == stack.isEmpty()) {
                this.lineHandler.handle(stack.pop());
            }
        }

        // 将指针置于末尾
        try {
            this.randomAccessFile.seek(len);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 命令行打印的行处理器
     */
    public static class ConsoleLineHandler implements LineHandler {
        @Override
        public void handle(String line) {
            Console.log(line);
        }
    }

}
