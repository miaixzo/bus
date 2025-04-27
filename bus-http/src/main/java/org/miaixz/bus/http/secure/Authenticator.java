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
package org.miaixz.bus.http.secure;

import java.io.IOException;

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.Route;

/**
 * 在连接到代理服务器之前执行抢占式身份验证， 或者在收到来自源web服务器或代理服务器的挑战后执行被动身份验证. 代理身份验证器可以实现抢占式身份验证、反应式身份验证或两者都实现.
 * 应用程序可以为源服务器或代理服务器配置Httpd的身份验证器，或者两者都配置.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Authenticator {

    /**
     * 不知道任何凭据且不尝试进行身份验证的验证程序
     */
    Authenticator NONE = (route, response) -> null;

    /**
     * 该请求包含满足{@code response}中的身份验证挑战的凭据。如果无法满足挑战，则返回null 该路线是最好的努力，它目前可能不总是提供，即使在逻辑上可用
     * 在应用程序拦截器中手动重用身份验证器时，例如在实现特定于客户机的重试时，也可能不提供此功能
     *
     * @param route    路由信息
     * @param response 响应体
     * @return 返回一个请求
     * @throws IOException 异常信息
     */
    Request authenticate(Route route, Response response) throws IOException;

}
