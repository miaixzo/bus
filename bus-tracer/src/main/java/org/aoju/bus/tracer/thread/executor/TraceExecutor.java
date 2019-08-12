package org.aoju.bus.tracer.thread.executor;

import org.aoju.bus.tracer.context.TraceContext;
import org.aoju.bus.tracer.thread.TraceRunnable;

import java.util.concurrent.Executor;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceExecutor implements Executor {

    private final Executor delegate;

    public TraceExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        this.delegate.execute(new TraceRunnable(command, TraceContext.getContextMap()));
    }
    
}
