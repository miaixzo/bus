package org.aoju.bus.http.internal.connection;

import org.aoju.bus.http.HttpClient;
import org.aoju.bus.http.Interceptor;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.internal.http.HttpCodec;
import org.aoju.bus.http.internal.http.RealInterceptorChain;

import java.io.IOException;

/**
 * Opens a connection to the target server and proceeds to the next interceptor.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class ConnectInterceptor implements Interceptor {

    public final HttpClient client;

    public ConnectInterceptor(HttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();

        // We need the network to satisfy this request. Possibly for validating a conditional GET.
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
        RealConnection connection = streamAllocation.connection();

        return realChain.proceed(request, streamAllocation, httpCodec, connection);
    }

}
