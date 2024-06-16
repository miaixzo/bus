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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.metric.EventListener;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 选择连接到源服务器的路由。每个连接都需要选择代理
 * 服务器、IP地址和TLS模式。连接也可以循环使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RouteSelector {

    private final Address address;
    private final RouteDatabase routeDatabase;
    private final NewCall call;
    private final EventListener eventListener;
    /**
     * 失败路线的状态
     */
    private final List<Route> postponedRoutes = new ArrayList<>();
    /**
     * 用于协商下一个要使用的代理的状态
     */
    private List<Proxy> proxies = Collections.emptyList();
    private int nextProxyIndex;
    /**
     * 状态，用于协商下一个要使用的套接字地址
     */
    private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();

    public RouteSelector(Address address, RouteDatabase routeDatabase, NewCall call,
                         EventListener eventListener) {
        this.address = address;
        this.routeDatabase = routeDatabase;
        this.call = call;
        this.eventListener = eventListener;

        resetNextProxy(address.url(), address.proxy());
    }

    /**
     * 从{@link InetSocketAddress}获取“主机”
     * 这将返回一个包含实际主机名或数字IP地址的字符串。
     *
     * @param socketAddress 套接字通信地址
     * @return 主机名或者host
     */
    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address = socketAddress.getAddress();
        if (null == address) {
            // InetSocketAddress是用字符串(数字IP或主机名)指定的。
            // 如果它是一个名称，那么应该尝试该名称的所有ip。
            // 如果它是一个IP地址，那么应该只尝试该IP地址
            return socketAddress.getHostName();
        }
        // InetSocketAddress有一个特定的地址:我们应该只尝试该地址。
        // 因此，我们返回地址并忽略任何可用的主机名
        return address.getHostAddress();
    }

    /**
     * 如果要尝试另一组路由，则返回true。每个地址至少有一条路由
     *
     * @return the true/false
     */
    public boolean hasNext() {
        return hasNextProxy() || !postponedRoutes.isEmpty();
    }

    public Selection next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // 计算要尝试的下一组路由
        List<Route> routes = new ArrayList<>();
        while (hasNextProxy()) {
            // 推迟的路线总是最后尝试。例如，如果我们有两个代理，
            // 并且proxy1的所有路由都应该延迟，那么我们将转移到proxy2
            // 只有在我们用尽了所有的好路线后，我们才会尝试推迟的路线
            Proxy proxy = nextProxy();
            for (int i = 0, size = inetSocketAddresses.size(); i < size; i++) {
                Route route = new Route(address, proxy, inetSocketAddresses.get(i));
                if (routeDatabase.shouldPostpone(route)) {
                    postponedRoutes.add(route);
                } else {
                    routes.add(route);
                }
            }

            if (!routes.isEmpty()) {
                break;
            }
        }

        if (routes.isEmpty()) {
            routes.addAll(postponedRoutes);
            postponedRoutes.clear();
        }

        return new Selection(routes);
    }

    /**
     * 准备代理服务器进行尝试
     *
     * @param url   url信息
     * @param proxy 代理
     */
    private void resetNextProxy(UnoUrl url, Proxy proxy) {
        if (proxy != null) {
            // 如果用户指定了代理，则尝试该操作，且仅尝试该操作
            proxies = Collections.singletonList(proxy);
        } else {
            // 尝试每一个ProxySelector选项，直到有一个连接成功
            List<Proxy> proxiesOrNull = address.proxySelector().select(url.uri());
            proxies = proxiesOrNull != null && !proxiesOrNull.isEmpty()
                    ? Builder.immutableList(proxiesOrNull)
                    : Builder.immutableList(Proxy.NO_PROXY);
        }
        nextProxyIndex = 0;
    }

    /**
     * 如果要尝试另一个代理，则返回true
     *
     * @return the true/false
     */
    private boolean hasNextProxy() {
        return nextProxyIndex < proxies.size();
    }

    /**
     * 返回要尝试的下一个代理。可能是代理NO_PROXY，但不能为null
     *
     * @return 代理信息
     * @throws IOException 异常
     */
    private Proxy nextProxy() throws IOException {
        if (!hasNextProxy()) {
            throw new SocketException("No route to " + address.url().host()
                    + "; exhausted proxy configurations: " + proxies);
        }
        Proxy result = proxies.get(nextProxyIndex++);
        resetNextInetSocketAddress(result);
        return result;
    }

    /**
     * 为当前代理或主机准备套接字地址
     *
     * @return 代理信息
     * @throws IOException 异常
     */
    private void resetNextInetSocketAddress(Proxy proxy) throws IOException {
        inetSocketAddresses = new ArrayList<>();

        String socketHost;
        int socketPort;
        if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.SOCKS) {
            socketHost = address.url().host();
            socketPort = address.url().port();
        } else {
            SocketAddress proxyAddress = proxy.address();
            if (!(proxyAddress instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(
                        "Proxy.address() is not an " + "InetSocketAddress: " + proxyAddress.getClass());
            }
            InetSocketAddress proxySocketAddress = (InetSocketAddress) proxyAddress;
            socketHost = getHostString(proxySocketAddress);
            socketPort = proxySocketAddress.getPort();
        }

        if (socketPort < 1 || socketPort > 65535) {
            throw new SocketException("No route to " + socketHost + Symbol.COLON + socketPort
                    + "; port is out of range");
        }

        if (proxy.type() == Proxy.Type.SOCKS) {
            inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
        } else {
            eventListener.dnsStart(call, socketHost);

            // 在IPv4/IPv6混合环境中尝试每个地址以获得最佳性能
            List<InetAddress> addresses = address.dns().lookup(socketHost);
            if (addresses.isEmpty()) {
                throw new UnknownHostException(address.dns() + " returned no addresses for " + socketHost);
            }

            eventListener.dnsEnd(call, socketHost, addresses);

            for (int i = 0, size = addresses.size(); i < size; i++) {
                InetAddress inetAddress = addresses.get(i);
                inetSocketAddresses.add(new InetSocketAddress(inetAddress, socketPort));
            }
        }
    }

    /**
     * 选定的路由
     */
    public static class Selection {

        private final List<Route> routes;
        private int nextRouteIndex = 0;

        Selection(List<Route> routes) {
            this.routes = routes;
        }

        public boolean hasNext() {
            return nextRouteIndex < routes.size();
        }

        public Route next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return routes.get(nextRouteIndex++);
        }

        public List<Route> getAll() {
            return new ArrayList<>(routes);
        }
    }

}
