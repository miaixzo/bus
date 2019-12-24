package org.aoju.bus.http.accord;

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.http.Httpd;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.metric.http.HttpCodec;
import org.aoju.bus.http.metric.http.RealInterceptorChain;

import java.io.IOException;

/**
 * 打开到目标服务器的连接并继续到下一个拦截器.
 */
public final class ConnectInterceptor implements Interceptor {

    public final Httpd httpd;

    public ConnectInterceptor(Httpd httpd) {
        this.httpd = httpd;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();

        // 我们需要网络来满足这个要求。可能用于验证条件GET
        boolean doExtensiveHealthChecks = !Http.GET.equals(request.method());
        HttpCodec httpCodec = streamAllocation.newStream(httpd, chain, doExtensiveHealthChecks);
        RealConnection connection = streamAllocation.connection();

        return realChain.proceed(request, streamAllocation, httpCodec, connection);
    }

}
