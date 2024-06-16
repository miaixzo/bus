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
package org.miaixz.bus.core.io.watch;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Closeable;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * 路径监听器
 * <p>
 * 监听器可监听目录或文件
 * 如果监听的Path不存在，则递归创建空目录然后监听此空目录
 * 递归监听目录时，并不会监听新创建的目录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchMonitor extends Thread implements Closeable, Serializable {

    private static final long serialVersionUID = -1L;

    private final WatchServiceWrapper watchService;

    /**
     * 监听路径，必须为目录
     */
    private Path dir;
    /**
     * 监听的文件，对于单文件监听不为空
     */
    private Path file;

    /**
     * 递归目录的最大深度，当小于1时不递归下层目录
     */
    private int maxDepth;
    /**
     * 监听器
     */
    private Watcher watcher;

    /**
     * 构造
     *
     * @param dir    字符串路径
     * @param events 监听事件列表，如创建、修改和删除等
     */
    public WatchMonitor(final Path dir, final WatchEvent.Kind<?>... events) {
        this(dir, 0, events);
    }

    /**
     * 构造
     * 例如设置：
     * <pre>
     * maxDepth &lt;= 1 表示只监听当前目录
     * maxDepth = 2 表示监听当前目录以及下层目录
     * maxDepth = 3 表示监听当前目录以及下两层
     * </pre>
     *
     * @param dir      路径
     * @param maxDepth 递归目录的最大深度，当小于2时不递归下层目录
     * @param events   监听事件列表，如创建、修改和删除等
     */
    public WatchMonitor(final Path dir, final int maxDepth, final WatchEvent.Kind<?>... events) {
        this.watchService = WatchServiceWrapper.of(events);
        this.dir = dir;
        this.maxDepth = maxDepth;
        this.init();
    }

    /**
     * 设置监听
     * 多个监听请使用{@link WatcherChain}
     *
     * @param watcher 监听
     * @return WatchMonitor
     */
    public WatchMonitor setWatcher(final Watcher watcher) {
        this.watcher = watcher;
        return this;
    }

    @Override
    public void run() {
        watch();
    }

    /**
     * 开始监听事件，阻塞当前进程
     */
    public void watch() {
        watch(this.watcher);
    }

    /**
     * 开始监听事件，阻塞当前进程
     *
     * @param watcher 监听
     * @throws InternalException 监听异常，如果监听关闭抛出此异常
     */
    public void watch(final Watcher watcher) throws InternalException {
        if (this.watchService.isClosed()) {
            throw new InternalException("Watch Monitor is closed !");
        }

        // 按照层级注册路径及其子路径
        registerPath();

        while (!this.watchService.isClosed()) {
            doTakeAndWatch(watcher);
        }
    }

    /**
     * 当监听目录时，监听目录的最大深度
     * 当设置值为1（或小于1）时，表示不递归监听子目录
     * 例如设置：
     * <pre>
     * maxDepth &lt;= 1 表示只监听当前目录
     * maxDepth = 2 表示监听当前目录以及下层目录
     * maxDepth = 3 表示监听当前目录以及下层
     * </pre>
     *
     * @param maxDepth 最大深度，当设置值为1（或小于1）时，表示不递归监听子目录，监听所有子目录请传{@link Integer#MAX_VALUE}
     * @return this
     */
    public WatchMonitor setMaxDepth(final int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    @Override
    public void close() {
        this.watchService.close();
    }

    /**
     * 初始化
     * 初始化包括：
     * <pre>
     * 1、解析传入的路径，判断其为目录还是文件
     * </pre>
     *
     * @throws InternalException 监听异常，IO异常时抛出此异常
     */
    private void init() throws InternalException {
        // 获取目录或文件路径
        if (!PathResolve.exists(this.dir, false)) {
            // 不存在的路径
            final Path lastPathEle = FileKit.getLastPathEle(this.dir);
            if (null != lastPathEle) {
                final String lastPathEleStr = lastPathEle.toString();
                // 带有点表示有扩展名，按照未创建的文件对待。Linux下.d的为目录，排除之
                if (StringKit.contains(lastPathEleStr, Symbol.C_DOT) && !StringKit.endWithIgnoreCase(lastPathEleStr, ".d")) {
                    this.file = this.dir;
                    this.dir = this.file.getParent();
                }
            }

            // 创建不存在的目录或父目录
            PathResolve.mkdir(this.dir);
        } else if (PathResolve.isFile(this.dir, false)) {
            // 文件路径
            this.file = this.dir;
            this.dir = this.file.getParent();
        }
    }

    /**
     * 执行事件获取并处理
     *
     * @param watcher {@link Watcher}
     */
    private void doTakeAndWatch(final Watcher watcher) {
        this.watchService.watch(watcher,
                // 对于文件监听，忽略目录下其他文件和目录的事件
                watchEvent -> null == file || file.endsWith(watchEvent.context().toString()));
    }

    /**
     * 注册监听路径
     */
    private void registerPath() {
        this.watchService.registerPath(this.dir, (null != this.file) ? 0 : this.maxDepth);
    }

}
