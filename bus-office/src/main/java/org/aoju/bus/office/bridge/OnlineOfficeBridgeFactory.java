/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.bridge;

import org.aoju.bus.http.Httpx;
import org.aoju.bus.office.metric.RequestBuilder;

/**
 * 保存与LibreOffice在线服务器通信的请求配置.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class OnlineOfficeBridgeFactory implements OnlineOfficeContextAware {

    private final Httpx httpx;
    private final RequestBuilder requestBuilder;

    /**
     * 使用指定的客户端和URL构造新连接.
     *
     * @param httpx          用于与LibreOffice在线服务器通信的HTTP客户机(已初始化).
     * @param requestBuilder 转换的请求配置.
     */
    public OnlineOfficeBridgeFactory(final Httpx httpx,
                                     final RequestBuilder requestBuilder) {
        this.httpx = httpx;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public Httpx getHttp() {
        return httpx;
    }

    @Override
    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

}
