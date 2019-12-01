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
package org.aoju.bus.office.support;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that allows for custom thread names
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadIndex = new AtomicInteger(0);

    private final String baseName;
    private final boolean daemon;

    public NamedThreadFactory(String baseName) {
        this(baseName, true);
    }

    public NamedThreadFactory(String baseName, boolean daemon) {
        this.baseName = baseName;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, baseName + "-" + threadIndex.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }

}
