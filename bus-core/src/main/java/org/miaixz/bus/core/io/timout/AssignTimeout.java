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
package org.miaixz.bus.core.io.timout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 将调用转发给另一个调用的 {@link Timeout}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AssignTimeout extends Timeout {

    private Timeout delegate;

    public AssignTimeout(Timeout delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
    }

    /**
     * {@link Timeout} 此实例当前正在委托给的实例
     */
    public final Timeout delegate() {
        return delegate;
    }

    public final AssignTimeout setDelegate(Timeout delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
        return this;
    }

    @Override
    public Timeout timeout(long timeout, TimeUnit unit) {
        return delegate.timeout(timeout, unit);
    }

    @Override
    public long timeoutNanos() {
        return delegate.timeoutNanos();
    }

    @Override
    public boolean hasDeadline() {
        return delegate.hasDeadline();
    }

    @Override
    public long deadlineNanoTime() {
        return delegate.deadlineNanoTime();
    }

    @Override
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        return delegate.deadlineNanoTime(deadlineNanoTime);
    }

    @Override
    public Timeout clearTimeout() {
        return delegate.clearTimeout();
    }

    @Override
    public Timeout clearDeadline() {
        return delegate.clearDeadline();
    }

    @Override
    public void throwIfReached() throws IOException {
        delegate.throwIfReached();
    }

}
