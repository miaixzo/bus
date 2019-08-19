/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.http.internal.connection;

import org.aoju.bus.http.*;
import org.aoju.bus.http.internal.Internal;

import java.io.IOException;
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
 * @version 3.1.0
 * @since JDK 1.8
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

    /**
     * Obtain a "host" from an {@link InetSocketAddress}. This returns a string containing either an
     * actual host name or a numeric IP address.
     */
    // Visible for testing
    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address = socketAddress.getAddress();
        if (address == null) {
            // The InetSocketAddress was specified with a string (either a numeric IP or a host name). If
            // it is a name, all IPs for that name should be tried. If it is an IP address, only that IP
            // address should be tried.
            return socketAddress.getHostName();
        }
        // The InetSocketAddress has a specific address: we should only try that address. Therefore we
        // return the address and ignore any host name that may be available.
        return address.getHostAddress();
    }

    /**
     * Returns true if there's another set of routes to attempt. Every address has at least first route.
     */
    public boolean hasNext() {
        return hasNextProxy() || !postponedRoutes.isEmpty();
    }

    public Selection next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Compute the next set of routes to attempt.
        List<Route> routes = new ArrayList<>();
        while (hasNextProxy()) {
            // Postponed routes are always tried last. For example, if we have 2 proxies and all the
            // routes for proxy1 should be postponed, we'll move to proxy2. Only after we've exhausted
            // all the good routes will we attempt the postponed routes.
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
            // We've exhausted all Proxies so fallback to the postponed routes.
            routes.addAll(postponedRoutes);
            postponedRoutes.clear();
        }

        return new Selection(routes);
    }

    /**
     * Clients should invoke this method when they encounter a connectivity failure on a connection
     * returned by this route selector.
     */
    public void connectFailed(Route failedRoute, IOException failure) {
        if (failedRoute.proxy().type() != Proxy.Type.DIRECT && address.proxySelector() != null) {
            // Tell the proxy selector when we fail to connect on a fresh connection.
            address.proxySelector().connectFailed(
                    address.url().uri(), failedRoute.proxy().address(), failure);
        }

        routeDatabase.failed(failedRoute);
    }

    /**
     * Prepares the proxy servers to try.
     */
    private void resetNextProxy(HttpUrl url, Proxy proxy) {
        if (proxy != null) {
            // If the user specifies a proxy, try that and only that.
            proxies = Collections.singletonList(proxy);
        } else {
            // Try each of the ProxySelector choices until first connection succeeds.
            List<Proxy> proxiesOrNull = address.proxySelector().select(url.uri());
            proxies = proxiesOrNull != null && !proxiesOrNull.isEmpty()
                    ? Internal.immutableList(proxiesOrNull)
                    : Internal.immutableList(Proxy.NO_PROXY);
        }
        nextProxyIndex = 0;
    }

    /**
     * Returns true if there's another proxy to try.
     */
    private boolean hasNextProxy() {
        return nextProxyIndex < proxies.size();
    }

    /**
     * Returns the next proxy to try. May be PROXY.NO_PROXY but never null.
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
     * Prepares the socket addresses to attempt for the current proxy or host.
     */
    private void resetNextInetSocketAddress(Proxy proxy) throws IOException {
        // Clear the addresses. Necessary if getAllByName() below throws!
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

            // Try each address for best behavior in mixed IPv4/IPv6 environments.
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
     * A set of selected Routes.
     */
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
