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
package org.miaixz.bus.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * 解析主机名的IP地址的域名服务。大多数应用程序将使用默认的 {@linkplain #SYSTEM SYSTEM DNS服务}，应用程序可能提供 它们自己的实现来使用不同的DNS服务器
 * 选择IPv6地址、选择IPv4地址或强制使用特定的已知IP地址
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DnsX {

    /**
     * 使用{@link InetAddress#getAllByName(String)}请求底层操作系统 查找IP地址的DNS。大多数自定义{@link DnsX}实现应该委托给这个实例.
     */
    DnsX SYSTEM = hostname -> {
        if (null == hostname)
            throw new UnknownHostException("hostname == null");
        try {
            return Arrays.asList(InetAddress.getAllByName(hostname));
        } catch (NullPointerException e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Broken system behaviour for dns lookup of " + hostname);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    };

    /**
     * 返回{@code hostname}的IP地址，按Httpd尝试的顺序排列。如果到地址的连接 失败，Httpd将重试下一个地址的连接，直到建立连接、耗尽IP地址集或超出限制
     *
     * @param hostname 主机名信息
     * @return ip地址信息
     * @throws UnknownHostException 异常信息
     */
    List<InetAddress> lookup(String hostname) throws UnknownHostException;

}
