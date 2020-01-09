/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.config.process;

import io.netty.channel.Channel;
import org.aoju.bus.metric.config.ConfigClient;
import org.aoju.bus.metric.config.CountDownLatchManager;
import org.aoju.bus.metric.config.NettyProcessor;
import org.aoju.bus.metric.consts.NettyMode;

/**
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public abstract class AbstractNettyProcessor implements NettyProcessor {

    private volatile static int lockObjectCount = 0;

    protected ConfigClient configClient;
    protected NettyMode nettyMode;

    public AbstractNettyProcessor(ConfigClient configClient, NettyMode nettyMode) {
        super();
        this.configClient = configClient;
        this.nettyMode = nettyMode;
        if (this.hasLock()) {
            lockObjectCount++;
        }
    }

    public static int getLockObjectCount() {
        return lockObjectCount;
    }

    /**
     * 执行netty操作
     *
     * @param channel channel
     * @param data    数据
     */
    protected abstract void doProcess(Channel channel, String data);

    protected boolean hasLock() {
        return true;
    }

    @Override
    public synchronized void process(final Channel channel, final String data) {
        try {
            doProcess(channel, data);
        } finally {
            if (hasLock()) {
                CountDownLatchManager.countDown();
            }
        }
    }

    public String getCode() {
        return this.nettyMode.getCode();
    }

}
