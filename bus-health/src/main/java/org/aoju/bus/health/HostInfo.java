/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 代表当前主机的信息。
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public class HostInfo {

    private final String HOST_NAME;
    private final String HOST_ADDRESS;

    public HostInfo() {
        String hostName;
        String hostAddress;

        try {
            InetAddress localhost = getLocalAddress();

            hostName = localhost.getHostName();
            hostAddress = localhost.getHostAddress();
        } catch (UnknownHostException e) {
            hostName = "localhost";
            hostAddress = "127.0.0.1";
        }

        HOST_NAME = hostName;
        HOST_ADDRESS = hostAddress;
    }

    /**
     * 取得当前主机信息。
     *
     * @return 主机地址信息
     * @throws UnknownHostException 异常
     */
    public static InetAddress getLocalAddress() throws UnknownHostException {
        try {
            InetAddress inetAddress = null;
            /** 遍历所有的网络接口 */
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                /** 在所有的网络接口下再遍历IP */
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            /** 如果是site-local地址，就是它了 */
                            return inetAddr;
                        } else if (inetAddress == null) {
                            /** site-local类型的地址未被发现，先记录候选地址 */
                            inetAddress = inetAddr;
                        }
                    }
                }
            }
            if (inetAddress != null) {
                return inetAddress;
            }
            /**  如果没有发现 non-loopback地址.只能用最次选的方案 */
            inetAddress = InetAddress.getLocalHost();
            if (inetAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return inetAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * 取得当前主机的名称。
     *
     * <p>
     * 例如：<code>"webserver1"</code>
     * </p>
     *
     * @return 主机名
     */
    public final String getName() {
        return HOST_NAME;
    }

    /**
     * 取得当前主机的地址。
     *
     * <p>
     * 例如：<code>"192.168.0.1"</code>
     * </p>
     *
     * @return 主机地址
     */
    public final String getAddress() {
        return HOST_ADDRESS;
    }

    /**
     * 将当前主机的信息转换成字符串。
     *
     * @return 主机信息的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        HealthUtils.append(builder, "Host Name:    ", getName());
        HealthUtils.append(builder, "Host Address: ", getAddress());

        return builder.toString();
    }

}
