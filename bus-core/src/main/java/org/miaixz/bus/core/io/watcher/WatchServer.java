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
package org.miaixz.bus.core.io.watcher;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.function.BiConsumerX;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 文件监听服务，此服务可以同时监听多个路径
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WatchServer extends Thread implements Closeable, Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * WatchKey 和 Path的对应表
     */
    private final Map<WatchKey, Path> watchKeyPathMap = new HashMap<>();
    /**
     * 监听事件列表
     */
    protected WatchEvent.Kind<?>[] events;
    /**
     * 监听是否已经关闭
     */
    protected boolean isClosed;
    /**
     * 监听服务
     */
    private WatchService watchService;
    /**
     * 监听选项，例如监听频率等
     */
    private WatchEvent.Modifier[] modifiers;

    /**
     * 初始化<br>
     * 初始化包括：
     * <pre>
     * 1、解析传入的路径，判断其为目录还是文件
     * 2、创建{@link WatchService} 对象
     * </pre>
     *
     * @throws InternalException 监听异常，IO异常时抛出此异常
     */
    public void init() throws InternalException {
        //初始化监听
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        isClosed = false;
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
     */
    public void setModifiers(final WatchEvent.Modifier[] modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * 将指定路径加入到监听中
     *
     * @param path     路径
     * @param maxDepth 递归下层目录的最大深度
     */
    public void registerPath(final Path path, final int maxDepth) {
        final WatchEvent.Kind<?>[] kinds = ObjectKit.defaultIfEmpty(this.events, WatchKind.ALL);
        try {
            final WatchKey key;
            if (ArrayKit.isEmpty(this.modifiers)) {
                key = path.register(this.watchService, kinds);
            } else {
                key = path.register(this.watchService, kinds, this.modifiers);
            }
            watchKeyPathMap.put(key, path);

            // 递归注册下一层层级的目录
            if (maxDepth > 1) {
                //遍历所有子目录并加入监听
                Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                        registerPath(dir, 0);//继续添加目录
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            }
        } catch (final IOException e) {
            if (!(e instanceof AccessDeniedException)) {
                throw new InternalException(e);
            }

            //对于禁止访问的目录，跳过监听
        }
    }

    /**
     * 执行事件获取并处理
     *
     * @param action      监听回调函数，实现此函数接口用于处理WatchEvent事件
     * @param watchFilter 监听过滤接口，通过实现此接口过滤掉不需要监听的情况，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤
     */
    public void watch(final BiConsumerX<WatchEvent<?>, Path> action, final Predicate<WatchEvent<?>> watchFilter) {
        final WatchKey wk;
        try {
            wk = watchService.take();
        } catch (final InterruptedException | ClosedWatchServiceException e) {
            // 用户中断
            close();
            return;
        }

        final Path currentPath = watchKeyPathMap.get(wk);

        for (final WatchEvent<?> event : wk.pollEvents()) {
            // 如果监听文件，检查当前事件是否与所监听文件关联
            if (null != watchFilter && !watchFilter.test(event)) {
                continue;
            }

            action.accept(event, currentPath);
        }

        wk.reset();
    }

    /**
     * 执行事件获取并处理
     *
     * @param watcher     {@link Watcher}
     * @param watchFilter 监听过滤接口，通过实现此接口过滤掉不需要监听的情况，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤
     */
    public void watch(final Watcher watcher, final Predicate<WatchEvent<?>> watchFilter) {
        watch((event, currentPath) -> {
            final WatchEvent.Kind<?> kind = event.kind();

            if (kind == WatchKind.CREATE.getValue()) {
                watcher.onCreate(event, currentPath);
            } else if (kind == WatchKind.MODIFY.getValue()) {
                watcher.onModify(event, currentPath);
            } else if (kind == WatchKind.DELETE.getValue()) {
                watcher.onDelete(event, currentPath);
            } else if (kind == WatchKind.OVERFLOW.getValue()) {
                watcher.onOverflow(event, currentPath);
            }
        }, watchFilter);
    }

    /**
     * 关闭监听
     */
    @Override
    public void close() {
        isClosed = true;
        IoKit.close(watchService);
    }

}
