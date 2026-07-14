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
package org.miaixz.bus.fabric.network.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Adapter from JDK {@link ProxySelector} to {@link ProxyPlan} snapshots.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ProxySelectorAdapter {

    /**
     * JDK selector.
     */
    private final ProxySelector selector;

    /**
     * Creates an adapter.
     *
     * @param selector selector
     */
    private ProxySelectorAdapter(final ProxySelector selector) {
        this.selector = Assert.notNull(selector, () -> new ValidateException("Proxy selector must not be null"));
    }

    /**
     * Wraps a JDK selector.
     *
     * @param selector selector
     * @return adapter
     */
    public static ProxySelectorAdapter of(final ProxySelector selector) {
        return new ProxySelectorAdapter(selector);
    }

    /**
     * Selects proxy plans for a URL.
     *
     * @param url URL
     * @return plans
     */
    public List<ProxyPlan> select(final UnoUrl url) {
        return select(Assert.notNull(url, () -> new ValidateException("URL must not be null")).toUri());
    }

    /**
     * Selects proxy plans for a URI.
     *
     * @param uri URI
     * @return plans
     */
    public List<ProxyPlan> select(final URI uri) {
        final URI current = Assert.notNull(uri, () -> new ValidateException("URI must not be null"));
        final List<Proxy> proxies = selector.select(current);
        if (proxies == null || proxies.isEmpty()) {
            return List.of(ProxyPlan.direct());
        }
        final ArrayList<ProxyPlan> plans = new ArrayList<>(proxies.size());
        for (final Proxy proxy : proxies) {
            plans.add(plan(proxy));
        }
        return List.copyOf(plans);
    }

    /**
     * Reports connection failure to the selector.
     *
     * @param uri     URI
     * @param address socket address
     * @param failure failure
     */
    public void connectFailed(final URI uri, final SocketAddress address, final IOException failure) {
        selector.connectFailed(
                Assert.notNull(uri, () -> new ValidateException("URI must not be null")),
                Assert.notNull(address, () -> new ValidateException("Socket address must not be null")),
                Assert.notNull(failure, () -> new ValidateException("Failure must not be null")));
    }

    /**
     * Converts one JDK proxy decision into the route-level proxy model used by fabric connectors.
     *
     * @param proxy JDK proxy returned by {@link ProxySelector}
     * @return proxy plan
     */
    private static ProxyPlan plan(final Proxy proxy) {
        final Proxy current = Assert.notNull(proxy, () -> new ValidateException("Proxy must not be null"));
        if (current.type() == Proxy.Type.DIRECT) {
            return ProxyPlan.direct();
        }
        if (!(current.address() instanceof InetSocketAddress address)) {
            throw new ProtocolException("ProxySelector returned unsupported address");
        }
        final String host = address.getHostString();
        final int port = address.getPort();
        return switch (current.type()) {
            case HTTP -> ProxyPlan.http(new Address(Protocol.HTTP.name, host, port, Symbol.SLASH));
            case SOCKS -> ProxyPlan.socks(new Address(Protocol.TCP.name, host, port, Symbol.SLASH));
            case DIRECT -> ProxyPlan.direct();
        };
    }

}
