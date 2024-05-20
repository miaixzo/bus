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
package org.miaixz.bus.core.io.watch;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * {@link WatchEvent} 包装类，提供可选的监听事件和监听选项，实现方法包括：
 * <ul>
 *     <li>注册：{@link #registerPath(Path, int)}注册需要监听的路径。</li>
 *     <li>监听：{@link #watch(Watcher, Predicate)} 启动监听并指定事件触发后的行为。</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchServiceWrapper implements WatchService, Wrapper<WatchService>, Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * 监听服务
     */
    private final WatchService watchService;
    /**
     * 监听事件列表，如新建、修改、删除等
     */
    private WatchEvent.Kind<?>[] events;
    /**
     * 监听选项，例如监听频率等
     */
    private WatchEvent.Modifier[] modifiers;
    /**
     * 监听是否已经关闭
     */
    private boolean isClosed;

    /**
     * 构造
     *
     * @param events 监听事件列表
     */
    public WatchServiceWrapper(final WatchEvent.Kind<?>... events) {
        // 初始化监听
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        this.events = events;
    }

    /**
     * 创建WatchServiceWrapper
     *
     * @param events 监听事件列表，如新建、修改、删除等
     * @return WatchServiceWrapper
     */
    public static WatchServiceWrapper of(final WatchEvent.Kind<?>... events) {
        return new WatchServiceWrapper(events);
    }

    @Override
    public WatchService getRaw() {
        return this.watchService;
    }

    /**
     * 是否已关闭
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public void close() {
        if (!this.isClosed) {
            this.isClosed = true;
            IoKit.closeQuietly(this.watchService);
        }
    }

    @Override
    public WatchKey poll() {
        return this.watchService.poll();
    }

    @Override
    public WatchKey poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.watchService.poll(timeout, unit);
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return this.watchService.take();
    }

    /**
     * 监听事件列表，见：StandardWatchEventKinds
     *
     * @param kinds 事件列表
     * @return this
     */
    public WatchServiceWrapper setEvents(final WatchKind... kinds) {
        if (ArrayKit.isNotEmpty(kinds)) {
            setEvents(ArrayKit.mapToArray(kinds, WatchKind::getValue, WatchEvent.Kind<?>[]::new));
        }
        return this;
    }

    /**
     * 监听事件列表，见：StandardWatchEventKinds
     *
     * @param events 事件列表
     * @return this
     */
    public WatchServiceWrapper setEvents(final WatchEvent.Kind<?>... events) {
        this.events = events;
        return this;
    }

    /**
     * 设置监听选项，例如监听频率等，可设置项包括：
     *
     * <pre>
     * 1、com.sun.nio.file.StandardWatchEventKinds
     * 2、com.sun.nio.file.SensitivityWatchEventModifier
     * </pre>
     *
     * @param modifiers 监听选项，例如监听频率等
     * @return this
     */
    public WatchServiceWrapper setModifiers(final WatchEvent.Modifier... modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    /**
     * 注册单一的监听
     *
     * @param watchable 可注册对象，如Path
     * @return {@link WatchKey}，如果为{@code null}，表示注册失败
     * @see Watchable#register(WatchService, WatchEvent.Kind[])
     * @see Watchable#register(WatchService, WatchEvent.Kind[], WatchEvent.Modifier...)
     */
    public WatchKey register(final Watchable watchable) {
        final WatchEvent.Kind<?>[] kinds = ArrayKit.defaultIfEmpty(this.events, WatchKind.ALL);

        WatchKey watchKey = null;
        try {
            if (ArrayKit.isEmpty(this.modifiers)) {
                watchKey = watchable.register(this.watchService, kinds);
            } else {
                watchKey = watchable.register(this.watchService, kinds, this.modifiers);
            }
        } catch (final IOException e) {
            if (!(e instanceof AccessDeniedException)) {
                throw new InternalException(e);
            }
        }

        return watchKey;
    }

    /**
     * 递归将指定路径加入到监听中
     * 如果提供的是目录，则监听目录本身和目录下的目录和文件，深度取决于maxDepth
     *
     * @param path     路径
     * @param maxDepth 递归下层目录的最大深度
     * @return this
     */
    public WatchServiceWrapper registerPath(final Path path, final int maxDepth) {
        // 注册当前目录或文件
        if (null == register(path)) {
            // 注册失败，跳过（可能目录或文件无权限）
            return this;
        }

        // 递归注册下一层层级的目录和文件
        PathResolve.walkFiles(path, maxDepth, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                //继续添加目录
                registerPath(dir, 0);
                return super.postVisitDirectory(dir, exc);
            }
        });
        return this;
    }

    /**
     * 执行事件获取并处理
     * {@link WatchEvent#context()}是实际操作的文件或目录的相对监听路径的Path，非绝对路径
     * {@link WatchKey#watchable()}是监听的Path
     * 此方法调用后阻塞线程，直到触发监听事件，执行后退出，无循环执行操作
     *
     * @param watcher     {@link Watcher}
     * @param watchFilter 监听过滤接口，通过实现此接口过滤掉不需要监听的情况，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤
     */
    public void watch(final Watcher watcher, final Predicate<WatchEvent<?>> watchFilter) {
        watch((event, watchKey) -> {
            final WatchEvent.Kind<?> kind = event.kind();

            if (kind == WatchKind.CREATE.getValue()) {
                watcher.onCreate(event, watchKey);
            } else if (kind == WatchKind.MODIFY.getValue()) {
                watcher.onModify(event, watchKey);
            } else if (kind == WatchKind.DELETE.getValue()) {
                watcher.onDelete(event, watchKey);
            } else if (kind == WatchKind.OVERFLOW.getValue()) {
                watcher.onOverflow(event, watchKey);
            }
        }, watchFilter);
    }

    /**
     * 执行事件获取并处理
     * {@link WatchEvent#context()}是实际操作的文件或目录的相对监听路径的Path，非绝对路径
     * {@link WatchKey#watchable()}是监听的Path
     * 此方法调用后阻塞线程，直到触发监听事件，执行后退出，无循环执行操作
     *
     * @param action 监听回调函数，实现此函数接口用于处理WatchEvent事件
     */
    public void watch(final BiConsumer<WatchEvent<?>, WatchKey> action) {
        watch(action, null);
    }

    /**
     * 执行事件获取并处理
     * {@link WatchEvent#context()}是实际操作的文件或目录的相对监听路径的Path，非绝对路径
     * {@link WatchKey#watchable()}是监听的Path
     * 此方法调用后阻塞线程，直到触发监听事件，执行后退出，无循环执行操作
     *
     * @param action      监听回调函数，实现此函数接口用于处理WatchEvent事件
     * @param watchFilter 监听过滤接口，通过实现此接口过滤掉不需要监听的情况，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤
     */
    public void watch(final BiConsumer<WatchEvent<?>, WatchKey> action, final Predicate<WatchEvent<?>> watchFilter) {
        final WatchKey wk;
        try {
            wk = watchService.take();
        } catch (final InterruptedException | ClosedWatchServiceException e) {
            // 用户中断
            close();
            return;
        }

        for (final WatchEvent<?> event : wk.pollEvents()) {
            // 如果监听文件，检查当前事件是否与所监听文件关联
            if (null != watchFilter && !watchFilter.test(event)) {
                continue;
            }
            action.accept(event, wk);
        }

        wk.reset();
    }

}
