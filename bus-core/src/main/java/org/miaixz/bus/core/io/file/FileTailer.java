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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.center.date.culture.en.Units;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.io.watch.WatchKind;
import org.miaixz.bus.core.io.watch.WatchMonitor;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.WatchKit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Stack;
import java.util.concurrent.*;

/**
 * 文件内容跟随器，实现类似Linux下"tail -f"命令功能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileTailer implements Serializable {

    /**
     * 控制台打印的处理类
     */
    public static final ConsumerX<String> CONSOLE_HANDLER = new ConsoleLineHandler();
    private static final long serialVersionUID = -1L;
    /**
     * 编码
     */
    private final java.nio.charset.Charset charset;
    /**
     * 行处理器
     */
    private final ConsumerX<String> lineHandler;
    /**
     * 初始读取的行数
     */
    private final int initReadLine;
    /**
     * 定时任务检查间隔时长
     */
    private final long period;

    private final String filePath;
    private final RandomAccessFile randomAccessFile;
    private final ScheduledExecutorService executorService;
    private WatchMonitor fileWatchMonitor;

    private boolean stopOnRemove;

    /**
     * 构造，默认UTF-8编码
     *
     * @param file        文件
     * @param lineHandler 行处理器
     */
    public FileTailer(final File file, final ConsumerX<String> lineHandler) {
        this(file, lineHandler, 0);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param file         文件
     * @param lineHandler  行处理器
     * @param initReadLine 启动时预读取的行数，1表示一行
     */
    public FileTailer(final File file, final ConsumerX<String> lineHandler, final int initReadLine) {
        this(file, Charset.UTF_8, lineHandler, initReadLine, Units.SECOND.getMillis());
    }

    /**
     * 构造
     *
     * @param file        文件
     * @param charset     编码
     * @param lineHandler 行处理器
     */
    public FileTailer(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> lineHandler) {
        this(file, charset, lineHandler, 0, Units.SECOND.getMillis());
    }

    /**
     * 构造
     *
     * @param file         文件
     * @param charset      编码
     * @param lineHandler  行处理器
     * @param initReadLine 启动时预读取的行数，1表示一行
     * @param period       检查间隔
     */
    public FileTailer(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> lineHandler, final int initReadLine, final long period) {
        checkFile(file);
        this.filePath = file.getAbsolutePath();
        this.charset = charset;
        this.lineHandler = lineHandler;
        this.period = period;
        this.initReadLine = initReadLine;
        this.randomAccessFile = FileKit.createRandomAccessFile(file, FileMode.r);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 检查文件有效性
     *
     * @param file 文件
     */
    private static void checkFile(final File file) {
        if (!file.exists()) {
            throw new InternalException("File [{}] not exist !", file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new InternalException("Path [{}] is not a file !", file.getAbsolutePath());
        }
    }

    /**
     * 设置删除文件后是否退出并抛出异常
     *
     * @param stopOnRemove 删除文件后是否退出并抛出异常
     */
    public void setStopOnRemove(final boolean stopOnRemove) {
        this.stopOnRemove = stopOnRemove;
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
    public void start(final boolean async) {
        // 初始读取
        try {
            this.readTail();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        final LineWatcher lineWatcher = new LineWatcher(this.randomAccessFile, this.charset, this.lineHandler);
        final ScheduledFuture<?> scheduledFuture = this.executorService.scheduleAtFixedRate(//
                lineWatcher,
                0,
                this.period, TimeUnit.MILLISECONDS
        );

        // 监听删除
        if (stopOnRemove) {
            fileWatchMonitor = WatchKit.of(this.filePath, WatchKind.DELETE.getValue());
            fileWatchMonitor.setWatcher(new SimpleWatcher() {

                private static final long serialVersionUID = -1L;

                @Override
                public void onDelete(final WatchEvent<?> event, final WatchKey key) {
                    super.onDelete(event, key);
                    stop();
                    throw new InternalException("{} has been deleted", filePath);
                }
            });
            fileWatchMonitor.start();
        }

        if (!async) {
            try {
                scheduledFuture.get();
            } catch (final ExecutionException e) {
                throw new InternalException(e);
            } catch (final InterruptedException e) {
                // ignore and exist
            }
        }
    }

    /**
     * 结束，此方法需在异步模式或
     */
    public void stop() {
        try {
            this.executorService.shutdown();
        } finally {
            IoKit.closeQuietly(this.randomAccessFile);
            IoKit.closeQuietly(this.fileWatchMonitor);
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
            final Stack<String> stack = new Stack<>();

            final long start = this.randomAccessFile.getFilePointer();
            long nextEnd = (len - 1) < 0 ? 0 : len - 1;
            this.randomAccessFile.seek(nextEnd);
            int c;
            int currentLine = 0;
            while (nextEnd > start) {
                // 满
                if (currentLine >= initReadLine) {
                    // initReadLine是行数，从1开始，currentLine是行号，从0开始
                    // 因此行号0表示一行，所以currentLine == initReadLine表示读取完毕
                    break;
                }

                c = this.randomAccessFile.read();
                if (c == Symbol.C_LF || c == Symbol.C_CR) {
                    // FileKit.readLine(this.randomAccessFile, this.charset, this.lineHandler);
                    final String line = FileKit.readLine(this.randomAccessFile, this.charset);
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
                    // FileKit.readLine(this.randomAccessFile, this.charset, this.lineHandler);
                    final String line = FileKit.readLine(this.randomAccessFile, this.charset);
                    if (null != line) {
                        stack.push(line);
                    }
                    break;
                }
            }

            // 输出缓存栈中的内容
            while (!stack.isEmpty()) {
                this.lineHandler.accept(stack.pop());
            }
        }

        // 将指针置于末尾
        try {
            this.randomAccessFile.seek(len);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 命令行打印的行处理器
     */
    public static class ConsoleLineHandler implements ConsumerX<String> {
        private static final long serialVersionUID = -1L;

        @Override
        public void accepting(final String line) {
            Console.log(line);
        }
    }

}
