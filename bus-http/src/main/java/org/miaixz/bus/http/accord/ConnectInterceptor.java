/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.RealInterceptorChain;

import java.io.IOException;

/**
 * 打开到目标服务器的连接并继续到下一个拦截器.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConnectInterceptor implements Interceptor {

    public final Httpd httpd;

    public ConnectInterceptor(Httpd httpd) {
        this.httpd = httpd;
    }

    @Override
    public Response intercept(NewChain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        Transmitter transmitter = realChain.transmitter();

        // 我们需要网络来满足这个要求。可能用于验证条件GET
        boolean doExtensiveHealthChecks = !HTTP.GET.equals(request.method());
        Exchange exchange = transmitter.newExchange(chain, doExtensiveHealthChecks);

        return realChain.proceed(request, transmitter, exchange);
    }

}
