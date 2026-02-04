/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Represents a specific route to an origin server chosen by the HTTP client when making a connection.
 * <p>
 * A route encapsulates the specific configuration for a connection, including the target address, the proxy to use, and
 * the socket address. Each route is an immutable instance representing a concrete choice of the client's connection
 * options (like proxy selection, TLS configuration).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Route {

    /**
     * The address configuration for the target server.
     */
    final Address address;
    /**
     * The proxy used for the connection.
     */
    final Proxy proxy;
    /**
     * The target socket address (IP and port).
     */
    final InetSocketAddress inetSocketAddress;

    /**
     * Constructs a new {@code Route} instance.
     *
     * @param address           The address configuration for the target server.
     * @param proxy             The proxy to use for the connection.
     * @param inetSocketAddress The target socket address.
     * @throws NullPointerException if address, proxy, or inetSocketAddress is null.
     */
    public Route(Address address, Proxy proxy, InetSocketAddress inetSocketAddress) {
        if (null == address) {
            throw new NullPointerException("address == null");
        }
        if (null == proxy) {
            throw new NullPointerException("proxy == null");
        }
        if (null == inetSocketAddress) {
            throw new NullPointerException("inetSocketAddress == null");
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    /**
     * Returns the address configuration for the target server.
     *
     * @return The {@link Address} object.
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the proxy used for this route.
     * <p>
     * <strong>Warning:</strong> This may be different from the proxy in the address configuration if a proxy selector
     * is used and the address does not specify a proxy.
     * </p>
     *
     * @return The {@link Proxy} object.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Returns the target socket address.
     *
     * @return The {@link InetSocketAddress} object, representing the target IP and port.
     */
    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }

    /**
     * Returns whether this route requires an HTTPS tunnel through an HTTP proxy.
     * <p>
     * See <a href="http://www.ietf.org/rfc/rfc2817.txt">RFC 2817, Section 5.2</a>.
     * </p>
     *
     * @return {@code true} if this route requires an HTTPS tunnel.
     */
    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }

    /**
     * Compares this route to another object for equality.
     * <p>
     * Two routes are equal if their address, proxy, and socket address are all equal.
     * </p>
     *
     * @param other The other object to compare against.
     * @return {@code true} if the two routes are equal.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Route && ((Route) other).address.equals(address) && ((Route) other).proxy.equals(proxy)
                && ((Route) other).inetSocketAddress.equals(inetSocketAddress);
    }

    /**
     * Computes the hash code for this route.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + address.hashCode();
        result = 31 * result + proxy.hashCode();
        result = 31 * result + inetSocketAddress.hashCode();
        return result;
    }

    /**
     * Returns a string representation of this route.
     *
     * @return A string containing the socket address.
     */
    @Override
    public String toString() {
        return "Route{" + inetSocketAddress + "}";
    }

}
