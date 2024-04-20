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
package org.miaixz.bus.core.swing;

import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.SwingKit;
import org.miaixz.bus.core.toolkit.ThreadKit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 剪贴板监听
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ClipboardMonitor implements ClipboardOwner, Runnable, Closeable {
    INSTANCE;

    /**
     * 默认重试此时：10
     */
    public static final int DEFAULT_TRY_COUNT = 10;
    /**
     * 默认重试等待：100
     */
    public static final long DEFAULT_DELAY = 100;
    /**
     * 系统剪贴板对象
     */
    private final Clipboard clipboard;
    /**
     * 监听事件处理
     */
    private final Set<ClipboardListener> listenerSet = new LinkedHashSet<>();
    /**
     * 重试次数
     */
    private int tryCount;
    /**
     * 重试等待
     */
    private long delay;
    /**
     * 是否正在监听
     */
    private boolean isRunning;

    /**
     * 构造,尝试获取剪贴板内容的次数为10,第二次之后延迟100毫秒
     */
    ClipboardMonitor() {
        this(DEFAULT_TRY_COUNT, DEFAULT_DELAY);
    }

    /**
     * 构造
     *
     * @param tryCount 尝试获取剪贴板内容的次数
     * @param delay    响应延迟,当从第二次开始,延迟一定毫秒数等待剪贴板可以获取,当tryCount小于2时无效
     */
    ClipboardMonitor(int tryCount, long delay) {
        this(tryCount, delay, SwingKit.getClipboard());
    }

    /**
     * 构造
     *
     * @param tryCount  尝试获取剪贴板内容的次数
     * @param delay     响应延迟,当从第二次开始,延迟一定毫秒数等待剪贴板可以获取,当tryCount小于2时无效
     * @param clipboard 剪贴板对象
     */
    ClipboardMonitor(int tryCount, long delay, Clipboard clipboard) {
        this.tryCount = tryCount;
        this.delay = delay;
        this.clipboard = clipboard;
    }

    /**
     * 设置重试次数
     *
     * @param tryCount 重试次数
     * @return this
     */
    public ClipboardMonitor setTryCount(int tryCount) {
        this.tryCount = tryCount;
        return this;
    }

    /**
     * 设置重试等待
     *
     * @param delay 重试等待
     * @return this
     */
    public ClipboardMonitor setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    /**
     * 设置 监听事件处理
     *
     * @param listener 监听事件处理
     * @return this
     */
    public ClipboardMonitor addListener(ClipboardListener listener) {
        this.listenerSet.add(listener);
        return this;
    }

    /**
     * 去除指定监听
     *
     * @param listener 监听
     * @return this
     */
    public ClipboardMonitor removeListener(ClipboardListener listener) {
        this.listenerSet.remove(listener);
        return this;
    }

    /**
     * 清空监听
     *
     * @return this
     */
    public ClipboardMonitor clearListener() {
        this.listenerSet.clear();
        return this;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        Transferable newContents;
        try {
            newContents = tryGetContent(clipboard);
        } catch (InterruptedException e) {
            // 中断后结束简体
            return;
        }

        Transferable transferable = null;
        for (ClipboardListener listener : listenerSet) {
            try {
                transferable = listener.onChange(clipboard, ObjectKit.defaultIfNull(transferable, newContents));
            } catch (Throwable e) {
                // 忽略事件处理异常,保证所有监听正常执行
            }
        }

        if (isRunning) {
            clipboard.setContents(ObjectKit.defaultIfNull(transferable, ObjectKit.defaultIfNull(newContents, contents)), this);
        }
    }

    @Override
    public synchronized void run() {
        if (false == isRunning) {
            final Clipboard clipboard = this.clipboard;
            clipboard.setContents(clipboard.getContents(null), this);
            isRunning = true;
        }
    }

    /**
     * 开始监听
     *
     * @param sync 是否阻塞
     */
    public void listen(boolean sync) {
        run();

        if (sync) {
            ThreadKit.sync(this);
        }
    }

    /**
     * 关闭(停止)监听
     */
    @Override
    public void close() {
        this.isRunning = false;
    }

    /**
     * 尝试获取剪贴板内容
     *
     * @param clipboard 剪贴板
     * @return 剪贴板内容,{@code null} 表示未获取到
     * @throws InterruptedException 线程中断
     */
    private Transferable tryGetContent(Clipboard clipboard) throws InterruptedException {
        Transferable newContents = null;
        for (int i = 0; i < this.tryCount; i++) {
            if (this.delay > 0 && i > 0) {
                Thread.sleep(this.delay);
            }

            try {
                newContents = clipboard.getContents(null);
            } catch (IllegalStateException e) {

            }
            if (null != newContents) {
                return newContents;
            }
        }
        return newContents;
    }

}
