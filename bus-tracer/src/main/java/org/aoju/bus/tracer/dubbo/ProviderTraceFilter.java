package org.aoju.bus.tracer.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.aoju.bus.tracer.TraceUtils;
import org.aoju.bus.tracer.consts.DubboType;
import org.aoju.bus.tracer.context.TraceContext;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Activate(group = Constants.PROVIDER, order = -10001)
public class ProviderTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TraceUtils.trace(invocation, DubboType.PROVIDER);
        try {
            return invoker.invoke(invocation);
        } finally {
            TraceContext.clear();
        }
    }

}
