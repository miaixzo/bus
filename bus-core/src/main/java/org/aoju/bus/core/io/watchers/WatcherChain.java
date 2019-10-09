/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import org.aoju.bus.core.lang.Chain;
import org.aoju.bus.core.utils.CollUtils;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Iterator;
import java.util.List;

/**
 * 观察者链
 * 用于加入多个观察者
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class WatcherChain implements Watcher, Chain<Watcher, WatcherChain> {

    /**
     * 观察者列表
     */
    final private List<Watcher> chain;

    /**
     * 构造
     *
     * @param watchers 观察者列表
     */
    public WatcherChain(Watcher... watchers) {
        chain = CollUtils.newArrayList(watchers);
    }

    /**
     * 创建观察者链{@link WatcherChain}
     *
     * @param watchers 观察者列表
     * @return {@link WatcherChain}
     */
    public static WatcherChain create(Watcher... watchers) {
        return new WatcherChain(watchers);
    }

    @Override
    public void onCreate(WatchEvent<?> event, Path currentPath) {
        for (Watcher watcher : chain) {
            watcher.onCreate(event, currentPath);
        }
    }

    @Override
    public void onModify(WatchEvent<?> event, Path currentPath) {
        for (Watcher watcher : chain) {
            watcher.onModify(event, currentPath);
        }
    }

    @Override
    public void onDelete(WatchEvent<?> event, Path currentPath) {
        for (Watcher watcher : chain) {
            watcher.onDelete(event, currentPath);
        }
    }

    @Override
    public void onOverflow(WatchEvent<?> event, Path currentPath) {
        for (Watcher watcher : chain) {
            watcher.onOverflow(event, currentPath);
        }
    }

    @Override
    public Iterator<Watcher> iterator() {
        return this.chain.iterator();
    }

    @Override
    public WatcherChain addChain(Watcher element) {
        this.chain.add(element);
        return this;
    }

}
