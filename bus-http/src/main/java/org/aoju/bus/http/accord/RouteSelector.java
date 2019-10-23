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
package org.aoju.bus.http.accord;

import org.aoju.bus.http.*;
import org.aoju.bus.http.offers.EventListener;

import java.io.IOException;
import java.net.Proxy;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Selects routes to connect to an origin server. Each connection requires a choice of proxy server,
 * IP address, and TLS mode. Connections may also be recycled.
 *
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public final class RouteSelector {

    private final Address address;
    private final RouteDatabase routeDatabase;
    private final Call call;
    private final EventListener eventListener;
    /* State for negotiating failed routes */
    private final List<Route> postponedRoutes = new ArrayList<>();
    /* State for negotiating the next proxy to use. */
    private List<Proxy> proxies = Collections.emptyList();
    private int nextProxyIndex;
    /* State for negotiating the next socket address to use. */
    private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();

    public RouteSelector(Address address, RouteDatabase routeDatabase, Call call,
                         EventListener eventListener) {
        this.address = address;
        this.routeDatabase = routeDatabase;
        this.call = call;
        this.eventListener = eventListener;

        resetNextProxy(address.url(), address.proxy());
    }

    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address = socketAddress.getAddress();
        if (address == null) {

            return socketAddress.getHostName();
        }

        return address.getHostAddress();
    }

    public boolean hasNext() {
        return hasNextProxy() || !postponedRoutes.isEmpty();
    }

    public Selection next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        List<Route> routes = new ArrayList<>();
        while (hasNextProxy()) {
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

    public void connectFailed(Route failedRoute, IOException failure) {
        if (failedRoute.proxy().type() != Proxy.Type.DIRECT && address.proxySelector() != null) {
            address.proxySelector().connectFailed(
                    address.url().uri(), failedRoute.proxy().address(), failure);
        }

        routeDatabase.failed(failedRoute);
    }

    private void resetNextProxy(Url url, Proxy proxy) {
        if (proxy != null) {
            proxies = Collections.singletonList(proxy);
        } else {
            List<Proxy> proxiesOrNull = address.proxySelector().select(url.uri());
            proxies = proxiesOrNull != null && !proxiesOrNull.isEmpty()
                    ? Internal.immutableList(proxiesOrNull)
                    : Internal.immutableList(Proxy.NO_PROXY);
        }
        nextProxyIndex = 0;
    }

    private boolean hasNextProxy() {
        return nextProxyIndex < proxies.size();
    }

    private Proxy nextProxy() throws IOException {
        if (!hasNextProxy()) {
            throw new SocketException("No route to " + address.url().host()
                    + "; exhausted proxy configurations: " + proxies);
        }
        Proxy result = proxies.get(nextProxyIndex++);
        resetNextInetSocketAddress(result);
        return result;
    }

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
            throw new SocketException("No route to " + socketHost + ":" + socketPort
                    + "; port is out of range");
        }

        if (proxy.type() == Proxy.Type.SOCKS) {
            inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
        } else {
            eventListener.dnsStart(call, socketHost);

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

    public static final class Selection {

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
