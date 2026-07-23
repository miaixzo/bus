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
package org.miaixz.bus.fabric.registry.route;

import java.net.InetSocketAddress;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;

/**
 * Immutable route candidate combining a logical destination, proxy decision, and concrete socket endpoint.
 *
 * @param address logical destination address
 * @param proxy   direct, HTTP, or SOCKS proxy plan
 * @param socket  endpoint to connect, which may be resolved or unresolved
 * @author Kimi Liu
 * @since Java 21+
 */
public record Route(Address address, ProxyPlan proxy, InetSocketAddress socket) {

    /**
     * Creates a route.
     *
     * @param address logical destination address
     * @param proxy   proxy routing plan
     * @param socket  concrete socket endpoint to connect
     * @throws ValidateException if any component is {@code null}
     */
    public Route {
        address = Assert.notNull(address, () -> new ValidateException("Route address must not be null"));
        proxy = Assert.notNull(proxy, () -> new ValidateException("Route proxy must not be null"));
        socket = Assert.notNull(socket, () -> new ValidateException("Route socket must not be null"));
    }

    /**
     * Creates a route.
     *
     * @param address logical destination address
     * @param proxy   proxy routing plan
     * @param socket  concrete socket endpoint to connect
     * @return immutable route containing the supplied components
     * @throws ValidateException if any argument is {@code null}
     */
    public static Route of(final Address address, final ProxyPlan proxy, final InetSocketAddress socket) {
        return new Route(address, proxy, socket);
    }

    /**
     * Returns the address.
     *
     * @return logical destination address
     */
    @Override
    public Address address() {
        return address;
    }

    /**
     * Returns the proxy plan.
     *
     * @return direct, HTTP, or SOCKS proxy routing plan
     */
    @Override
    public ProxyPlan proxy() {
        return proxy;
    }

    /**
     * Returns the socket address.
     *
     * @return socket endpoint used by the connection attempt
     */
    @Override
    public InetSocketAddress socket() {
        return socket;
    }

    /**
     * Returns whether the route is secure.
     *
     * @return {@code true} when the logical destination address is secure
     */
    public boolean secure() {
        return address.secure();
    }

    /**
     * Returns whether this route requires a tunnel.
     *
     * @return {@code true} when the proxy plan requires a tunnel for the logical destination
     */
    public boolean requiresTunnel() {
        return proxy.requiresTunnel(address);
    }

    /**
     * Returns a redacted route identifier for logs and metrics.
     *
     * @return destination, redacted proxy id, and socket endpoint joined into diagnostic text
     */
    public String id() {
        return address.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + address.host() + Symbol.COLON + address.port()
                + Symbol.SPACE + "via" + Symbol.SPACE + proxy.id() + Symbol.SPACE + Symbol.AT + Symbol.SPACE
                + socket.getHostString() + Symbol.COLON + socket.getPort();
    }

    /**
     * Returns diagnostic route text.
     *
     * @return diagnostic text containing the route id and derived secure and tunnel flags
     */
    @Override
    public String toString() {
        return "Route[id=" + id() + ", secure=" + secure() + ", tunnel=" + requiresTunnel() + "]";
    }

}
