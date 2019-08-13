package org.aoju.bus.tracer.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.context.TraceContext;
import org.aoju.bus.tracer.thread.TraceThread;
import org.slf4j.MDC;

import java.util.Arrays;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Activate(group = {Constants.CONSUMER}, order = -10000)
public class ConsumerTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (Boolean.TRUE.toString().equals(invocation.getAttachments().get(Constants.ASYNC_KEY))) {
            RpcContext.getContext().getAttachments().remove(Constants.ASYNC_KEY);
        }

        RpcContext rpcContext = RpcContext.getContext();
        TraceContext traceContext = TraceThread.getTraceThreadData(Thread.currentThread());

        if (ObjectUtils.isEmpty(traceContext)) {
            /** 设置MDC日志: 调用链,根节点(不存在,若出现做新链处理)*/
            traceContext.setX_trace_id("" + DateUtils.timestamp());
            traceContext.setX_span_id(String.valueOf(Thread.currentThread().getId()));
            traceContext.setX_child_Id("" + DateUtils.timestamp());
            traceContext.setX_local_ip(rpcContext.getLocalHost());
            traceContext.setX_remote_ip(rpcContext.getRemoteHost());
            TraceThread.set(Thread.currentThread(), traceContext);
        }

        /** 设置上下文信息*/
        rpcContext.setAttachment(TraceConsts.X_TRACE_ID, traceContext.getX_trace_id());
        rpcContext.setAttachment(TraceConsts.X_SPAN_ID, traceContext.getX_span_id());
        rpcContext.setAttachment(TraceConsts.X_CHILD_ID, traceContext.getX_child_Id());
        rpcContext.setAttachment(TraceConsts.X_LOCAL_IP, traceContext.getX_local_ip());
        rpcContext.setAttachment(TraceConsts.X_REMOTE_IP, traceContext.getX_remote_ip());

        MDC.put(TraceConsts.X_TRACE_ID, traceContext.getX_trace_id());
        MDC.put(TraceConsts.X_SPAN_ID, traceContext.getX_span_id());
        MDC.put(TraceConsts.X_CHILD_ID, traceContext.getX_child_Id());
        MDC.put(TraceConsts.X_LOCAL_IP, traceContext.getX_local_ip());
        MDC.put(TraceConsts.X_REMOTE_IP, traceContext.getX_remote_ip());

        long start = System.currentTimeMillis();
        Result result = invoker.invoke(invocation);
        long elapsed = System.currentTimeMillis() - start;
        if (invoker.getUrl() != null) {
            Logger.info("[{}], [{}], {}, [{}], [{}], [{}] ", invoker.getInterface(), invocation.getMethodName(), Arrays.toString(invocation.getArguments()), result.getValue(), result.getException(), elapsed);
        }
        return result;
    }

}
