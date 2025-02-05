/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.plugin.httpx;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.Credentials;

/**
 * HTTP代理配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpProxy {

    public final String hostAddress;
    public final int port;
    public final String user;
    public final String password;
    public final Proxy.Type type;

    /**
     * @param hostAddress 服务器域名或IP,比如miaixz.org, 192.168.1.1
     * @param port        端口
     * @param user        用户名,无则填null
     * @param password    用户密码,无则填null
     * @param type        代理类型
     */
    public HttpProxy(String hostAddress, int port, String user, String password, java.net.Proxy.Type type) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.user = user;
        this.password = password;
        this.type = type;
    }

    public HttpProxy(String hostAddress, int port) {
        this(hostAddress, port, null, null, java.net.Proxy.Type.HTTP);
    }

    public java.net.Proxy proxy() {
        return new java.net.Proxy(type, new InetSocketAddress(hostAddress, port));
    }

    public Authenticator authenticator() {
        return (route, response) -> {
            String credential = Credentials.basic(user, password);
            return response.request().newBuilder().header(HTTP.PROXY_AUTHORIZATION, credential)
                    .header(HTTP.PROXY_CONNECTION, HTTP.KEEP_ALIVE).build();
        };
    }

}
