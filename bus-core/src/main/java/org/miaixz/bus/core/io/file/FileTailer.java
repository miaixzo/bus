/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Stack;
import java.util.concurrent.*;

import org.miaixz.bus.core.center.date.Chrono;
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

/**
 * File content tailer, implementing functionality similar to the "tail -f" command in Linux. This utility continuously
 * monitors a file for new content and processes it line by line.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileTailer implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852227513936L;

    /**
     * A predefined line handler that prints each line to the console.
     */
    public static final ConsumerX<String> CONSOLE_HANDLER = new ConsoleLineHandler();

    /**
     * The character set used for reading the file.
     */
    private final java.nio.charset.Charset charset;
    /**
     * The handler responsible for processing each new line read from the file.
     */
    private final ConsumerX<String> lineHandler;
    /**
     * The number of lines to read from the end of the file when the tailer starts. If 0, no initial lines are read.
     */
    private final int initReadLine;
    /**
     * The interval in milliseconds at which the file is checked for new content.
     */
    private final long period;

    /**
     * The absolute path of the file being tailed.
     */
    private final String filePath;
    /**
     * The {@link RandomAccessFile} used for reading the file content.
     */
    private final RandomAccessFile randomAccessFile;
    /**
     * The scheduled executor service used to periodically check for file changes.
     */
    private final ScheduledExecutorService executorService;
    /**
     * A {@link WatchMonitor} to detect if the tailed file is removed.
     */
    private WatchMonitor fileWatchMonitor;

    /**
     * Flag indicating whether the tailer should stop and throw an exception if the file is removed.
     */
    private boolean stopOnRemove;

    /**
     * Constructs a new {@code FileTailer} with default UTF-8 encoding and a default check period of 1 second. No
     * initial lines are read.
     *
     * @param file        The file to tail. Must exist and be a regular file.
     * @param lineHandler The line handler to process each line. Must not be {@code null}.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public FileTailer(final File file, final ConsumerX<String> lineHandler) {
        this(file, lineHandler, 0);
    }

    /**
     * Constructs a new {@code FileTailer} with default UTF-8 encoding and a default check period of 1 second.
     *
     * @param file         The file to tail. Must exist and be a regular file.
     * @param lineHandler  The line handler to process each line. Must not be {@code null}.
     * @param initReadLine The number of lines to read initially from the end of the file when starting.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public FileTailer(final File file, final ConsumerX<String> lineHandler, final int initReadLine) {
        this(file, Charset.UTF_8, lineHandler, initReadLine, Chrono.SECOND.getMillis());
    }

    /**
     * Constructs a new {@code FileTailer} with a default check period of 1 second. No initial lines are read.
     *
     * @param file        The file to tail. Must exist and be a regular file.
     * @param charset     The character set for reading the file. Must not be {@code null}.
     * @param lineHandler The line handler to process each line. Must not be {@code null}.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public FileTailer(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> lineHandler) {
        this(file, charset, lineHandler, 0, Chrono.SECOND.getMillis());
    }

    /**
     * Constructs a new {@code FileTailer}.
     *
     * @param file         The file to tail. Must exist and be a regular file.
     * @param charset      The character set for reading the file. Must not be {@code null}.
     * @param lineHandler  The line handler to process each line. Must not be {@code null}.
     * @param initReadLine The number of lines to read initially from the end of the file when starting.
     * @param period       The interval in milliseconds for checking file changes. Must be positive.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public FileTailer(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> lineHandler,
            final int initReadLine, final long period) {
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
     * Checks the validity of the provided file.
     *
     * @param file The file to check. Must not be {@code null}.
     * @throws InternalException if the file does not exist or is not a regular file.
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
     * Sets whether the tailer should stop and throw an exception if the monitored file is removed.
     *
     * @param stopOnRemove {@code true} to stop and throw an exception on file removal; {@code false} otherwise.
     */
    public void setStopOnRemove(final boolean stopOnRemove) {
        this.stopOnRemove = stopOnRemove;
    }

    /**
     * Starts the file tailing process in synchronous mode. This method will block until the tailer is stopped or an
     * error occurs.
     */
    public void start() {
        start(false);
    }

    /**
     * Starts the file tailing process.
     *
     * @param async {@code true} to execute asynchronously (non-blocking); {@code false} for synchronous execution
     *              (blocking).
     * @throws InternalException if an I/O error occurs during initial read or if an execution error occurs in
     *                           asynchronous mode.
     */
    public void start(final boolean async) {
        // Initial read of tail lines
        try {
            this.readTail();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        final LineWatcher lineWatcher = new LineWatcher(this.randomAccessFile, this.charset, this.lineHandler);
        final ScheduledFuture<?> scheduledFuture = this.executorService.scheduleAtFixedRate(//
                lineWatcher,
                0,
                this.period,
                TimeUnit.MILLISECONDS);

        // Monitor for file deletion if stopOnRemove is enabled
        if (stopOnRemove) {
            fileWatchMonitor = WatchKit.of(this.filePath, WatchKind.DELETE.getValue());
            fileWatchMonitor.setWatcher(new SimpleWatcher() {

                @Serial
                private static final long serialVersionUID = 2852571830580L;

                /**
                 * Ondelete method.
                 *
                 * @return the void value
                 */
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
                // ignore and exit gracefully
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stops the file tailing process. This method should be called to gracefully shut down the tailer, especially when
     * it's running in asynchronous mode.
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
     * Reads the initial lines from the tail of the file based on {@link #initReadLine}. The file pointer is then set to
     * the end of the file.
     *
     * @throws IOException       if an I/O error occurs during file access.
     * @throws InternalException if an I/O error occurs.
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
                // If the desired number of initial lines has been read, stop.
                if (currentLine >= initReadLine) {
                    // initReadLine is the number of lines, starting from 1. currentLine is the line number, starting
                    // from 0.
                    // Therefore, currentLine == initReadLine means reading is complete.
                    break;
                }

                c = this.randomAccessFile.read();
                if (c == Symbol.C_LF || c == Symbol.C_CR) {
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
                    // When the file pointer retreats to the beginning of the file, read the first line.
                    final String line = FileKit.readLine(this.randomAccessFile, this.charset);
                    if (null != line) {
                        stack.push(line);
                    }
                    break;
                }
            }

            // Output the content in the buffer stack in correct order (LIFO)
            while (!stack.isEmpty()) {
                this.lineHandler.accept(stack.pop());
            }
        }

        // Set the pointer to the end of the file for continuous tailing
        try {
            this.randomAccessFile.seek(len);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * A concrete implementation of {@link ConsumerX} that prints each accepted string (line) to the console.
     */
    public static class ConsoleLineHandler implements ConsumerX<String> {

        /**
         * Constructs a new ConsoleLineHandler.
         */
        public ConsoleLineHandler() {
        }

        @Serial
        private static final long serialVersionUID = 2852227591586L;

        /**
         * Accepting method.
         */
        @Override
        public void accepting(final String line) {
            Console.log(line);
        }
    }

}
