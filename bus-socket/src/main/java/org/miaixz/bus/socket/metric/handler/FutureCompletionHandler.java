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
package org.miaixz.bus.socket.metric.handler;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @param <V>
 * @param <A>
 * @author Kimi Liu
 * @since Java 17+
 */
public final class FutureCompletionHandler<V, A> implements CompletionHandler<V, A>, Future<V> {

    private V result;
    private boolean done = false;
    private boolean cancel = false;
    private Throwable exception;

    @Override
    public void completed(V result, A selectionKey) {
        this.result = result;
        done = true;
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void failed(Throwable exc, A attachment) {
        exception = exc;
        done = true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (done || cancel) {
            return false;
        }
        cancel = true;
        done = true;
        synchronized (this) {
            notify();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public synchronized V get() throws InterruptedException, ExecutionException {
        if (done) {
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            return result;
        } else {
            wait();
        }
        return get();
    }

    @Override
    public synchronized V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (done) {
            return get();
        } else {
            wait(unit.toMillis(timeout));
        }
        if (done) {
            return get();
        }
        throw new TimeoutException();
    }

}
