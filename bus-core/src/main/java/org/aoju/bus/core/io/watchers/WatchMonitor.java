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
package org.aoju.bus.core.io.watchers;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;

/**
 * 路径监听器
 * 监听器可监听目录或文件
 * 如果监听的Path不存在,则递归创建空目录然后监听此空目录
 * 递归监听目录时,并不会监听新创建的目录
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class WatchMonitor extends WatchServer {

    /**
     * 事件丢失
     */
    public static final WatchEvent.Kind<?> OVERFLOW = StandardWatchEventKinds.OVERFLOW;
    /**
     * 修改事件
     */
    public static final WatchEvent.Kind<?> ENTRY_MODIFY = StandardWatchEventKinds.ENTRY_MODIFY;
    /**
     * 创建事件
     */
    public static final WatchEvent.Kind<?> ENTRY_CREATE = StandardWatchEventKinds.ENTRY_CREATE;
    /**
     * 删除事件
     */
    public static final WatchEvent.Kind<?> ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE;
    /**
     * 全部事件
     */
    public static final WatchEvent.Kind<?>[] EVENTS_ALL = {
            OVERFLOW,
            ENTRY_MODIFY,
            ENTRY_CREATE,
            ENTRY_DELETE
    };
    private static final long serialVersionUID = 1L;
    /**
     * 监听路径，必须为目录
     */
    private Path path;
    /**
     * 递归目录的最大深度，当小于1时不递归下层目录
     */
    private int maxDepth;
    /**
     * 监听的文件，对于单文件监听不为空
     */
    private Path filePath;

    /**
     * 监听器
     */
    private Watcher watcher;

    /**
     * 构造
     *
     * @param file   文件
     * @param events 监听的事件列表
     */
    public WatchMonitor(File file, WatchEvent.Kind<?>... events) {
        this(file.toPath(), events);
    }

    /**
     * 构造
     *
     * @param path   字符串路径
     * @param events 监听的事件列表
     */
    public WatchMonitor(String path, WatchEvent.Kind<?>... events) {
        this(Paths.get(path), events);
    }

    /**
     * 构造
     *
     * @param path   字符串路径
     * @param events 监听事件列表
     */
    public WatchMonitor(Path path, WatchEvent.Kind<?>... events) {
        this(path, 0, events);
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
     * @param path     字符串路径
     * @param maxDepth 递归目录的最大深度，当小于2时不递归下层目录
     * @param events   监听事件列表
     */
    public WatchMonitor(Path path, int maxDepth, WatchEvent.Kind<?>... events) {
        this.path = path;
        this.maxDepth = maxDepth;
        this.events = events;
        this.init();
    }

    /**
     * 创建并初始化监听
     *
     * @param url    URL
     * @param events 监听的事件列表
     * @return 监听对象
     */
    public static WatchMonitor create(URL url, WatchEvent.Kind<?>... events) {
        return create(url, 0, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param url      URL
     * @param events   监听的事件列表
     * @param maxDepth 当监听目录时，监听目录的最大深度，当设置值为1（或小于1）时，表示不递归监听子目录
     * @return 监听对象
     */
    public static WatchMonitor create(URL url, int maxDepth, WatchEvent.Kind<?>... events) {
        return create(UriUtils.toURI(url), maxDepth, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param uri    URI
     * @param events 监听的事件列表
     * @return 监听对象
     */
    public static WatchMonitor create(URI uri, WatchEvent.Kind<?>... events) {
        return create(uri, 0, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param uri      URI
     * @param events   监听的事件列表
     * @param maxDepth 当监听目录时，监听目录的最大深度，当设置值为1（或小于1）时，表示不递归监听子目录
     * @return 监听对象
     */
    public static WatchMonitor create(URI uri, int maxDepth, WatchEvent.Kind<?>... events) {
        return create(Paths.get(uri), maxDepth, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param file   文件
     * @param events 监听的事件列表
     * @return 监听对象
     */
    public static WatchMonitor create(File file, WatchEvent.Kind<?>... events) {
        return create(file, 0, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param file     文件
     * @param events   监听的事件列表
     * @param maxDepth 当监听目录时，监听目录的最大深度，当设置值为1（或小于1）时，表示不递归监听子目录
     * @return 监听对象
     */
    public static WatchMonitor create(File file, int maxDepth, WatchEvent.Kind<?>... events) {
        return create(file.toPath(), maxDepth, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param path   路径
     * @param events 监听的事件列表
     * @return 监听对象
     */
    public static WatchMonitor create(String path, WatchEvent.Kind<?>... events) {
        return create(path, 0, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param path     路径
     * @param events   监听的事件列表
     * @param maxDepth 当监听目录时，监听目录的最大深度，当设置值为1（或小于1）时，表示不递归监听子目录
     * @return 监听对象
     */
    public static WatchMonitor create(String path, int maxDepth, WatchEvent.Kind<?>... events) {
        return create(Paths.get(path), maxDepth, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param path   路径
     * @param events 监听事件列表
     * @return 监听对象
     */
    public static WatchMonitor create(Path path, WatchEvent.Kind<?>... events) {
        return create(path, 0, events);
    }

    /**
     * 创建并初始化监听
     *
     * @param path     路径
     * @param events   监听事件列表
     * @param maxDepth 当监听目录时，监听目录的最大深度，当设置值为1（或小于1）时，表示不递归监听子目录
     * @return 监听对象
     */
    public static WatchMonitor create(Path path, int maxDepth, WatchEvent.Kind<?>... events) {
        return new WatchMonitor(path, maxDepth, events);
    }

    /**
     * 创建并初始化监听，监听所有事件
     *
     * @param uri     URI
     * @param watcher {@link Watcher}
     * @return {@link WatchMonitor}
     */
    public static WatchMonitor createAll(URI uri, Watcher watcher) {
        return createAll(Paths.get(uri), watcher);
    }

    /**
     * 创建并初始化监听，监听所有事件
     *
     * @param url     URL
     * @param watcher {@link Watcher}
     * @return {@link WatchMonitor}
     */
    public static WatchMonitor createAll(URL url, Watcher watcher) {
        try {
            return createAll(Paths.get(url.toURI()), watcher);
        } catch (URISyntaxException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 创建并初始化监听，监听所有事件
     *
     * @param file    被监听文件
     * @param watcher {@link Watcher}
     * @return {@link WatchMonitor}
     */
    public static WatchMonitor createAll(File file, Watcher watcher) {
        return createAll(file.toPath(), watcher);
    }

    /**
     * 创建并初始化监听，监听所有事件
     *
     * @param path    路径
     * @param watcher {@link Watcher}
     * @return {@link WatchMonitor}
     */
    public static WatchMonitor createAll(String path, Watcher watcher) {
        return createAll(Paths.get(path), watcher);
    }

    /**
     * 创建并初始化监听，监听所有事件
     *
     * @param path    路径
     * @param watcher {@link Watcher}
     * @return {@link WatchMonitor}
     */
    public static WatchMonitor createAll(Path path, Watcher watcher) {
        final WatchMonitor watchMonitor = create(path, EVENTS_ALL);
        watchMonitor.setWatcher(watcher);
        return watchMonitor;
    }

    /**
     * 初始化
     * 初始化包括：
     * <pre>
     * 1、解析传入的路径，判断其为目录还是文件
     * 2、创建{@link WatchService} 对象
     * </pre>
     *
     * @throws InstrumentException 监听异常，IO异常时抛出此异常
     */
    @Override
    public void init() throws InstrumentException {
        //获取目录或文件路径
        if (false == Files.exists(this.path, LinkOption.NOFOLLOW_LINKS)) {
            // 不存在的路径
            final Path lastPathEle = FileUtils.getLastPathEle(this.path);
            if (null != lastPathEle) {
                final String lastPathEleStr = lastPathEle.toString();
                //带有点表示有扩展名，按照未创建的文件对待。Linux下.d的为目录，排除之
                if (StringUtils.contains(lastPathEleStr, Symbol.C_DOT) && false == StringUtils.endWithIgnoreCase(lastPathEleStr, ".d")) {
                    this.filePath = this.path;
                    this.path = this.filePath.getParent();
                }
            }

            //创建不存在的目录或父目录
            try {
                Files.createDirectories(this.path);
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        } else if (Files.isRegularFile(this.path, LinkOption.NOFOLLOW_LINKS)) {
            // 文件路径
            this.filePath = this.path;
            this.path = this.filePath.getParent();
        }

        super.init();
    }

    /**
     * 设置监听
     * 多个监听请使用{@link WatcherChain}
     *
     * @param watcher 监听
     * @return {@link WatchMonitor}
     */
    public WatchMonitor setWatcher(Watcher watcher) {
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
     * @throws InstrumentException 监听异常，如果监听关闭抛出此异常
     */
    public void watch(Watcher watcher) throws InstrumentException {
        if (isClosed) {
            throw new InstrumentException("Watch Monitor is closed !");
        }

        // 按照层级注册路径及其子路径
        registerPath();

        while (false == isClosed) {
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
    public WatchMonitor setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    /**
     * 执行事件获取并处理
     *
     * @param watcher {@link Watcher}
     */
    private void doTakeAndWatch(Watcher watcher) {
        super.watch(watcher, watchEvent -> null == filePath || filePath.endsWith(watchEvent.context().toString()));
    }

    /**
     * 注册监听路径
     */
    private void registerPath() {
        registerPath(this.path, (null != this.filePath) ? 0 : this.maxDepth);
    }

}
