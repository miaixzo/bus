package org.aoju.bus.tracer.thread;

import org.aoju.bus.tracer.context.TraceContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceThread {

    private static Map<Thread, TraceContext> traceThreadMap = new HashMap<>();

    public static TraceContext getTraceThreadData(Thread thread) {
        if (null == traceThreadMap.get(thread)) {
            return new TraceContext();
        }
        return traceThreadMap.get(thread);
    }

    public static void set(Thread thread, TraceContext traceThreadData) {
        traceThreadMap.put(thread, traceThreadData);
    }

}
