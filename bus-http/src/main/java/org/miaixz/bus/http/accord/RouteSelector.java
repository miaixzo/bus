/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Selects routes to connect to an origin server. Each connection requires a choice of proxy server, IP address, and TLS
 * mode. Connections may also be recycled.
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
     * The state of the failed routes.
     */
    private final List<Route> postponedRoutes = new ArrayList<>();
    /**
     * The state for negotiating the next proxy to use.
     */
    private List<Proxy> proxies = Collections.emptyList();
    private int nextProxyIndex;
    /**
     * The state for negotiating the next socket address to use.
     */
    private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();

    public RouteSelector(Address address, RouteDatabase routeDatabase, NewCall call, EventListener eventListener) {
        this.address = address;
        this.routeDatabase = routeDatabase;
        this.call = call;
        this.eventListener = eventListener;

        resetNextProxy(address.url(), address.proxy());
    }

    /**
     * Gets the host string from an {@link InetSocketAddress}. This will return a string containing either the actual
     * hostname or the numeric IP address.
     *
     * @param socketAddress The socket address.
     * @return The hostname or host address.
     */
    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address = socketAddress.getAddress();
        if (address == null) {
            // The InetSocketAddress was specified with a string (either a numeric IP or a hostname).
            // If it is a name, all IPs for that name should be tried.
            // If it is an IP address, only that IP address should be tried.
            return socketAddress.getHostName();
        }
        // The InetSocketAddress has a specific address: we should only try that address.
        // Therefore, we return the address and ignore any available hostname.
        return address.getHostAddress();
    }

    /**
     * Returns true if there is another set of routes to attempt. Every address has at least one route.
     *
     * @return {@code true} if there is another route to attempt.
     */
    public boolean hasNext() {
        return hasNextProxy() || !postponedRoutes.isEmpty();
    }

    /**
     * Returns the next selection of routes to attempt.
     *
     * @return The next selection of routes.
     * @throws IOException if an I/O error occurs.
     */
    public Selection next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Compute the next set of routes to attempt.
        List<Route> routes = new ArrayList<>();
        while (hasNextProxy()) {
            // Postponed routes are always tried last. For example, if we have 2 proxies and all of the routes for
            // proxy1 should be postponed, we'll move to proxy2. Only after we've exhausted all good routes will we
            // fall back to the postponed routes.
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
            // We've exhausted all good routes. Fall back to the postponed routes.
            routes.addAll(postponedRoutes);
            postponedRoutes.clear();
        }

        return new Selection(routes);
    }

    /**
     * Prepares the proxy servers to be attempted.
     *
     * @param url   The URL to connect to.
     * @param proxy The explicit proxy to use, or null to use the proxy selector.
     */
    private void resetNextProxy(UnoUrl url, Proxy proxy) {
        if (proxy != null) {
            // If the user specifies a proxy, try that and only that.
            proxies = Collections.singletonList(proxy);
        } else {
            // Try each of the ProxySelector choices until one connection succeeds.
            List<Proxy> proxiesOrNull = address.proxySelector().select(url.uri());
            proxies = proxiesOrNull != null && !proxiesOrNull.isEmpty() ? Builder.immutableList(proxiesOrNull)
                    : Builder.immutableList(Proxy.NO_PROXY);
        }
        nextProxyIndex = 0;
    }

    /**
     * Returns true if there is another proxy to attempt.
     *
     * @return {@code true} if there is another proxy to attempt.
     */
    private boolean hasNextProxy() {
        return nextProxyIndex < proxies.size();
    }

    /**
     * Returns the next proxy to attempt. This may be {@link Proxy#NO_PROXY}, but will not be null.
     *
     * @return The next proxy.
     * @throws IOException if an I/O error occurs.
     */
    private Proxy nextProxy() throws IOException {
        if (!hasNextProxy()) {
            throw new SocketException(
                    "No route to " + address.url().host() + "; exhausted proxy configurations: " + proxies);
        }
        Proxy result = proxies.get(nextProxyIndex++);
        resetNextInetSocketAddress(result);
        return result;
    }

    /**
     * Prepares the socket addresses for the current proxy or host.
     *
     * @param proxy The proxy to prepare socket addresses for.
     * @throws IOException if an I/O error occurs.
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
                        "Proxy.address() is not an InetSocketAddress: " + proxyAddress.getClass());
            }
            InetSocketAddress proxySocketAddress = (InetSocketAddress) proxyAddress;
            socketHost = getHostString(proxySocketAddress);
            socketPort = proxySocketAddress.getPort();
        }

        if (socketPort < 1 || socketPort > 65535) {
            throw new SocketException(
                    "No route to " + socketHost + Symbol.COLON + socketPort + "; port is out of range");
        }

        if (proxy.type() == Proxy.Type.SOCKS) {
            inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
        } else {
            eventListener.dnsStart(call, socketHost);

            // In mixed IPv4/IPv6 environments, try each address for best performance.
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
     * A selection of routes to try.
     */
    public static class Selection {

        private final List<Route> routes;
        private int nextRouteIndex = 0;

        Selection(List<Route> routes) {
            this.routes = routes;
        }

        /**
         * Returns true if there is another route to attempt.
         *
         * @return {@code true} if there is another route.
         */
        public boolean hasNext() {
            return nextRouteIndex < routes.size();
        }

        /**
         * Returns the next route to attempt.
         *
         * @return The next route.
         */
        public Route next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return routes.get(nextRouteIndex++);
        }

        /**
         * Returns a list of all routes in this selection.
         *
         * @return A new list containing all routes.
         */
        public List<Route> getAll() {
            return new ArrayList<>(routes);
        }
    }

}
