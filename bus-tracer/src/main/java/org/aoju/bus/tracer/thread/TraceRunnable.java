package org.aoju.bus.tracer.thread;

import org.aoju.bus.tracer.context.TraceContext;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceRunnable implements Runnable {

    private final Runnable delegate;

    private final Map<String, String> context;

    public TraceRunnable(Runnable delegate, Map<String, String> context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public void run() {
        TraceContext.setContextMap(context);
        try {
            delegate.run();
        } finally {
            TraceContext.clearAll();
        }
    }

}
