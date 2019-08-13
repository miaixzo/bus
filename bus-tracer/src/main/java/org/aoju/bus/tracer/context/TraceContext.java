package org.aoju.bus.tracer.context;

import lombok.Data;
import org.aoju.bus.base.entity.Tracer;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Data
public class TraceContext extends Tracer {

    public static Map<String, String> getContextMap() {
        return MDC.getCopyOfContextMap();
    }

    public static void setContextMap(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            map = new HashMap<>();
        }
        MDC.setContextMap(map);
    }

    public static void clear() {
        MDC.clear();
    }

    @Override
    public String getX_trace_id() {
        return MDC.get(TraceConsts.X_TRACE_ID);
    }

    @Override
    public void setX_trace_id(String x_trace_id) {
        MDC.put(TraceConsts.X_TRACE_ID, x_trace_id);
    }

    @Override
    public String getX_span_id() {
        return MDC.get(TraceConsts.X_SPAN_ID);
    }

    @Override
    public void setX_span_id(String x_span_id) {
        MDC.put(TraceConsts.X_SPAN_ID, x_span_id);
    }

    @Override
    public String getX_child_Id() {
        return MDC.get(TraceConsts.X_CHILD_ID);
    }

    @Override
    public void setX_child_Id(String x_child_Id) {
        MDC.put(TraceConsts.X_CHILD_ID, x_child_Id);
    }

    @Override
    public String getX_local_ip() {
        return MDC.get(TraceConsts.X_LOCAL_IP);
    }

    @Override
    public void setX_local_ip(String x_local_ip) {
        MDC.put(TraceConsts.X_LOCAL_IP, x_local_ip);
    }

    @Override
    public String getX_remote_ip() {
        return MDC.get(TraceConsts.X_REMOTE_IP);
    }
    @Override
    public void setX_remote_ip(String x_remote_ip) {
        MDC.put(TraceConsts.X_REMOTE_IP, x_remote_ip);
    }

}
