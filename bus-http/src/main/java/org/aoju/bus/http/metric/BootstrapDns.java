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
package org.aoju.bus.http.metric;

import org.aoju.bus.http.DnsX;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 内部引导DNS实现，用于处理通过HTTPS服务器到DNS的初始连接
 * 返回已知主机的硬编码结果
 *
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public final class BootstrapDns implements DnsX {

    private final String dnsHostname;
    private final List<InetAddress> dnsServers;

    BootstrapDns(String dnsHostname, List<InetAddress> dnsServers) {
        this.dnsHostname = dnsHostname;
        this.dnsServers = dnsServers;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (!this.dnsHostname.equals(hostname)) {
            throw new UnknownHostException(
                    "BootstrapDns called for " + hostname + " instead of " + dnsHostname);
        }
        return dnsServers;
    }

}