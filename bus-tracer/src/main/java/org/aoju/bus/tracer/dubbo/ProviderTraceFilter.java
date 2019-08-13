package org.aoju.bus.tracer.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.context.TraceContext;
import org.aoju.bus.tracer.thread.TraceThread;

import java.util.Arrays;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Activate(group = {Constants.PROVIDER}, order = -10000)
public class ProviderTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (Boolean.TRUE.toString().equals(invocation.getAttachments().get(Constants.ASYNC_KEY))) {
            RpcContext.getContext().getAttachments().remove(Constants.ASYNC_KEY);
        }

        RpcContext rpcContext = RpcContext.getContext();
        TraceContext traceContext = TraceThread.getTraceThreadData(Thread.currentThread());

        traceContext.setX_trace_id(rpcContext.getAttachment(TraceConsts.X_TRACE_ID));
        traceContext.setX_span_id(rpcContext.getAttachment(TraceConsts.X_SPAN_ID));
        traceContext.setX_child_Id(rpcContext.getAttachment(TraceConsts.X_CHILD_ID));
        traceContext.setX_local_ip(rpcContext.getAttachment(TraceConsts.X_LOCAL_IP));
        traceContext.setX_local_ip(rpcContext.getAttachment(TraceConsts.X_REMOTE_IP));

        long start = DateUtils.timestamp();
        Result result = invoker.invoke(invocation);
        long elapsed = DateUtils.timestamp() - start;
        if (invoker.getUrl() != null) {
            Logger.info(StringUtils.format("[{}], [{}], {}, [{}], [{}], [{}] ", invoker.getInterface(), invocation.getMethodName(), Arrays.toString(invocation.getArguments()), result.getValue(), result.getException(), elapsed));
        }
        return result;
    }

}
