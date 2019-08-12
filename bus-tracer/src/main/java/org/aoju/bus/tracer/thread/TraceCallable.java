package org.aoju.bus.tracer.thread;

import org.aoju.bus.tracer.context.TraceContext;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final Map<String, String> context;

    public TraceCallable(Callable<V> delegate, Map<String, String> context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public V call() throws Exception {
        TraceContext.setContextMap(context);
        try {
            return delegate.call();
        } finally {
            TraceContext.clearAll();
        }
    }

}
