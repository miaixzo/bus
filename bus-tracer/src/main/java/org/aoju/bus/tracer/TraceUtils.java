package org.aoju.bus.tracer;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcInvocation;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.consts.DubboType;
import org.aoju.bus.tracer.context.TraceContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceUtils {

    /**
     * traceId最短长度
     */
    private final static int MIN_TRACE_ID_LEN = 5;

    public static void trace(Invocation invocation, DubboType type) {
        String traceId;
        switch (type) {
            case CONSUMER:
                RpcInvocation rpcInvocation = (RpcInvocation) invocation;
                traceId = getDefaultContextTraceId();
                rpcInvocation.setAttachment(TraceConsts.X_COMMON_TRACE_ID, traceId);
                break;
            case PROVIDER:
                traceId = invocation.getAttachment(TraceConsts.X_COMMON_TRACE_ID);
                setDefaultContextTraceId(traceId);
                break;
            default:
                break;
        }
    }

    public static void trace(HttpServletRequest request, HttpServletResponse response) {
        String traceId = request.getHeader(TraceConsts.X_COMMON_TRACE_ID);
        traceId = setDefaultContextTraceId(traceId);
        response.setHeader(TraceConsts.X_COMMON_TRACE_ID, traceId);
    }

    public static String getDefaultContextTraceId() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null || traceId.length() < MIN_TRACE_ID_LEN) {
            traceId = newTraceId();
        }
        return traceId;
    }

    public static String setDefaultContextTraceId(String traceId) {
        if (traceId == null || traceId.length() < MIN_TRACE_ID_LEN) {
            traceId = getDefaultContextTraceId();
        }
        TraceContext.setTraceId(traceId);
        return traceId;
    }

    public static String newTraceId() {
        //getTraceTime new Date(Long.parseLong(Integer.toBinaryString(Integer.parseInt(TraceUtils.newTraceId().substring(0,8), 16)),2)*1000)
        int random = ThreadLocalRandom.current().nextInt();
        long epochSeconds = System.currentTimeMillis() / 1000;
        return Long.toHexString((epochSeconds & 0xffffffffL) << 32 | (random & 0xffffffffL));
    }

}
