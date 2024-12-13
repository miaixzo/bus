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
package org.miaixz.bus.core.lang.thread;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;

/**
 * ExecutorService代理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DelegatedExecutorService extends AbstractExecutorService implements Wrapper<ExecutorService> {

    private final ExecutorService raw;

    /**
     * 构造
     *
     * @param executor {@link ExecutorService}
     */
    public DelegatedExecutorService(final ExecutorService executor) {
        this.raw = Assert.notNull(executor, "executor must be not null !");
    }

    @Override
    public void execute(final Runnable command) {
        this.raw.execute(command);
    }

    @Override
    public void shutdown() {
        this.raw.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.raw.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.raw.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.raw.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.raw.awaitTermination(timeout, unit);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return this.raw.submit(task);
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return this.raw.submit(task);
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return this.raw.submit(task, result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.raw.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        return this.raw.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return this.raw.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.raw.invokeAny(tasks, timeout, unit);
    }

    @Override
    public ExecutorService getRaw() {
        return this.raw;
    }

}
