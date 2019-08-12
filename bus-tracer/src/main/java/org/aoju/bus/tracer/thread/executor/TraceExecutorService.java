package org.aoju.bus.tracer.thread.executor;

import org.aoju.bus.tracer.context.TraceContext;
import org.aoju.bus.tracer.thread.TraceCallable;
import org.aoju.bus.tracer.thread.TraceRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceExecutorService implements ExecutorService {
    
    protected final ExecutorService delegate;

    public TraceExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.submit(new TraceCallable<T>(task, TraceContext.getContextMap()));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegate.submit(new TraceRunnable(task, TraceContext.getContextMap()), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate.submit(new TraceRunnable(task, TraceContext.getContextMap()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.invokeAll(wrapCallableCollection(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.invokeAll(wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(wrapCallableCollection(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        this.delegate.execute(new TraceRunnable(command, TraceContext.getContextMap()));
    }

    private <T> Collection<? extends Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> ts = new ArrayList<>();
        for (Callable<T> task : tasks) {
            if (!(task instanceof TraceCallable)) {
                ts.add(new TraceCallable<T>(task, TraceContext.getContextMap()));
            }
        }
        return ts;
    }

}
