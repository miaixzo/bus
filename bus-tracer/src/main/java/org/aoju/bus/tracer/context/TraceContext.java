package org.aoju.bus.tracer.context;

import org.aoju.bus.tracer.consts.TraceConsts;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public final class TraceContext {

    private TraceContext() {
    }

    public static String getTraceId() {
        return MDC.get(TraceConsts.X_TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        MDC.put(TraceConsts.X_TRACE_ID, traceId);
        MDC.put(TraceConsts.X_TRACE_LOG, "-traceId-" + traceId + "-#");
    }

    public static Map<String, String> getContextMap() {
        return MDC.getCopyOfContextMap();
    }

    public static void setContextMap(Map<String, String> contextMap) {
        if (contextMap == null) {
            contextMap = new HashMap<>();
        }
        MDC.setContextMap(contextMap);
    }

    public static void clear() {
        MDC.remove(TraceConsts.X_TRACE_ID);
        MDC.remove(TraceConsts.X_TRACE_LOG);
    }

    public static void clearAll() {
        MDC.clear();
    }

}
