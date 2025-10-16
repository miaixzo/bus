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
package org.miaixz.bus.core.io.watch;

import java.io.Closeable;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Path monitor for file system events. This monitor can observe directories or individual files. If the monitored path
 * does not exist, it will recursively create empty directories and then monitor the created directory. When recursively
 * monitoring a directory, newly created subdirectories are not automatically monitored.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchMonitor extends Thread implements Closeable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852236557339L;

    /**
     * The wrapper around the {@link java.nio.file.WatchService} that handles the actual watching mechanism.
     */
    private final WatchServiceWrapper watchService;

    /**
     * The path being monitored. This must be a directory. If a file is provided, its parent directory will be
     * monitored.
     */
    private Path dir;
    /**
     * The specific file being monitored. This is non-null only when monitoring a single file.
     */
    private Path file;

    /**
     * The maximum depth for recursive directory monitoring. If less than 1, only the current directory is monitored.
     */
    private int maxDepth;
    /**
     * The {@link Watcher} instance that will be notified of file system events.
     */
    private Watcher watcher;

    /**
     * Constructs a new {@code WatchMonitor} for the given directory and specified event kinds. This constructor sets
     * the maximum depth for recursive monitoring to 0, meaning only the immediate directory is monitored.
     *
     * @param dir    The path to the directory or file to monitor.
     * @param events An array of {@link WatchEvent.Kind} representing the types of events to listen for (e.g., create,
     *               modify, delete).
     */
    public WatchMonitor(final Path dir, final WatchEvent.Kind<?>... events) {
        this(dir, 0, events);
    }

    /**
     * Constructs a new {@code WatchMonitor} for the given directory, maximum recursion depth, and specified event
     * kinds. Examples for {@code maxDepth}:
     * 
     * <pre>
     * maxDepth &lt;= 1: Only monitors the current directory.
     * maxDepth = 2: Monitors the current directory and its immediate subdirectories.
     * maxDepth = 3: Monitors the current directory and its subdirectories up to two levels deep.
     * </pre>
     *
     * @param dir      The path to the directory or file to monitor.
     * @param maxDepth The maximum depth for recursive directory monitoring. If less than 1, only the current directory
     *                 is monitored. To monitor all subdirectories recursively, use {@link Integer#MAX_VALUE}.
     * @param events   An array of {@link WatchEvent.Kind} representing the types of events to listen for (e.g., create,
     *                 modify, delete).
     */
    public WatchMonitor(final Path dir, final int maxDepth, final WatchEvent.Kind<?>... events) {
        this.watchService = WatchServiceWrapper.of(events);
        this.dir = dir;
        this.maxDepth = maxDepth;
        this.init();
    }

    /**
     * Sets the {@link Watcher} for this monitor. If multiple watchers are needed, use {@link WatcherChain}.
     *
     * @param watcher The {@link Watcher} instance to be notified of events.
     * @return This {@code WatchMonitor} instance, allowing for method chaining.
     */
    public WatchMonitor setWatcher(final Watcher watcher) {
        this.watcher = watcher;
        return this;
    }

    /**
     * Starts the monitoring process. This method runs in a separate thread and blocks until the monitor is closed.
     */
    @Override
    public void run() {
        watch();
    }

    /**
     * Starts the monitoring process. This method blocks the current thread and continuously watches for file system
     * events. The events are handled by the {@link Watcher} set via {@link #setWatcher(Watcher)}.
     *
     * @throws InternalException If the monitor is closed before or during the watching process.
     */
    public void watch() {
        watch(this.watcher);
    }

    /**
     * Starts the monitoring process with a specific {@link Watcher}. This method blocks the current thread and
     * continuously watches for file system events. The events are handled by the provided {@code watcher}.
     *
     * @param watcher The {@link Watcher} instance to be notified of events.
     * @throws InternalException If the monitor is closed before or during the watching process.
     */
    public void watch(final Watcher watcher) throws InternalException {
        if (this.watchService.isClosed()) {
            throw new InternalException("Watch Monitor is closed !");
        }

        // Register the path and its sub-paths according to the specified depth.
        registerPath();

        while (!this.watchService.isClosed()) {
            doTakeAndWatch(watcher);
        }
    }

    /**
     * Sets the maximum depth for recursive directory monitoring. When the value is 1 (or less than 1), it means only
     * the current directory is monitored. Examples:
     * 
     * <pre>
     * maxDepth &lt;= 1: Only monitors the current directory.
     * maxDepth = 2: Monitors the current directory and its immediate subdirectories.
     * maxDepth = 3: Monitors the current directory and its subdirectories up to two levels deep.
     * </pre>
     *
     * @param maxDepth The maximum depth for recursive directory monitoring. Use {@link Integer#MAX_VALUE} to monitor
     *                 all subdirectories.
     * @return This {@code WatchMonitor} instance, allowing for method chaining.
     */
    public WatchMonitor setMaxDepth(final int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    /**
     * Closes the underlying {@link java.nio.file.WatchService}, releasing any resources associated with it. Once
     * closed, the monitor can no longer watch for events.
     */
    @Override
    public void close() {
        this.watchService.close();
    }

    /**
     * Initializes the monitor. This includes:
     * <ol>
     * <li>Resolving the input path to determine if it's a directory or a file.</li>
     * <li>If the path does not exist, it attempts to create the necessary directories.</li>
     * </ol>
     *
     * @throws InternalException If an I/O error occurs during path resolution or directory creation.
     */
    private void init() throws InternalException {
        // Get the directory or file path.
        if (!PathResolve.exists(this.dir, false)) {
            // Path does not exist.
            final Path lastPathEle = FileKit.getLastPathEle(this.dir);
            if (null != lastPathEle) {
                final String lastPathEleStr = lastPathEle.toString();
                // If the path contains a dot and is not a .d directory (Linux convention),
                // treat it as an uncreated file. Otherwise, it's a directory.
                if (StringKit.contains(lastPathEleStr, Symbol.C_DOT)
                        && !StringKit.endWithIgnoreCase(lastPathEleStr, ".d")) {
                    this.file = this.dir;
                    this.dir = this.file.getParent();
                }
            }

            // Create non-existent directory or parent directory.
            PathResolve.mkdir(this.dir);
        } else if (PathResolve.isFile(this.dir, false)) {
            // If the provided path is a file, set it as the file to monitor and monitor its parent directory.
            this.file = this.dir;
            this.dir = this.file.getParent();
        }
    }

    /**
     * Executes the event retrieval and processing loop. This method takes events from the
     * {@link java.nio.file.WatchService} and dispatches them to the provided {@link Watcher}.
     *
     * @param watcher The {@link Watcher} to which events are dispatched.
     */
    private void doTakeAndWatch(final Watcher watcher) {
        this.watchService.watch(
                watcher,
                // For file monitoring, ignore events for other files and directories within the monitored directory.
                watchEvent -> null == file || file.endsWith(watchEvent.context().toString()));
    }

    /**
     * Registers the monitoring path (directory) with the {@link java.nio.file.WatchService}. If monitoring a single
     * file, the depth is set to 0. Otherwise, the specified {@code maxDepth} is used.
     */
    private void registerPath() {
        this.watchService.registerPath(this.dir, (null != this.file) ? 0 : this.maxDepth);
    }

}
