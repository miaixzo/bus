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

    /** Reuses the adapter for the process-wide selector without changing selector identity semantics. */
    private static volatile ProxySelector cachedSelector;

    /** Adapter paired with {@link #cachedSelector}. */
    private static volatile ProxySelectorAdapter cachedAdapter;

    /** Short cache avoids repeatedly parsing an unchanged platform proxy decision. */
    private volatile SelectionCache selectionCache;

    /**
     * Non-null JDK selector delegated to for decisions and failure reporting.
     */
    private final ProxySelector selector;

    /**
     * Creates an adapter around a required JDK selector.
     *
     * @param selector JDK proxy selector to delegate to
     */
    private ProxySelectorAdapter(final ProxySelector selector) {
        this.selector = Assert.notNull(selector, () -> new ValidateException("Proxy selector must not be null"));
    }

    /**
     * Wraps a JDK selector.
     *
     * @param selector JDK proxy selector to delegate to
     * @return fabric proxy-plan adapter over the supplied selector
     * @throws ValidateException if {@code selector} is {@code null}
     */
    public static ProxySelectorAdapter of(final ProxySelector selector) {
        final ProxySelector current = Assert
                .notNull(selector, () -> new ValidateException("Proxy selector must not be null"));
        final ProxySelectorAdapter adapter = cachedAdapter;
        if (adapter != null && cachedSelector == current) {
            return adapter;
        }
        synchronized (ProxySelectorAdapter.class) {
            if (cachedAdapter == null || cachedSelector != current) {
                cachedSelector = current;
                cachedAdapter = new ProxySelectorAdapter(current);
            }
            return cachedAdapter;
        }
    }

    /**
     * Converts a fabric URL to a URI and selects ordered proxy plans for it.
     *
     * @param url fabric URL passed to the wrapped selector as a URI
     * @return immutable ordered proxy plans, with a direct fallback when the selector returns null or empty
     * @throws ValidateException if {@code url} is {@code null} or the selector returns a null proxy element
     * @throws ProtocolException if a selected non-direct proxy has an unsupported address type
     */
    public List<ProxyPlan> select(final UnoUrl url) {
        return select(Assert.notNull(url, () -> new ValidateException("URL must not be null")).toUri());
    }

    /**
     * Selects proxy plans for a URI.
     *
     * @param uri target URI passed unchanged to the wrapped selector
     * @return immutable plans in selector order, with a direct fallback when the result is null or empty
     * @throws ValidateException if {@code uri} is {@code null} or the selector returns a null proxy element
     * @throws ProtocolException if a selected non-direct proxy has an unsupported address type
     */
    public List<ProxyPlan> select(final URI uri) {
        final URI current = Assert.notNull(uri, () -> new ValidateException("URI must not be null"));
        final long now = System.nanoTime();
        final SelectionCache cached = selectionCache;
        if (cached != null && cached.expiresAtNanos() - now > 0L && cached.uri().equals(current)) {
            return cached.plans();
        }
        final List<Proxy> proxies = selector.select(current);
        final List<ProxyPlan> selected;
        if (proxies == null || proxies.isEmpty()) {
            selected = List.of(ProxyPlan.direct());
        } else {
            final ArrayList<ProxyPlan> plans = new ArrayList<>(proxies.size());
            for (final Proxy proxy : proxies) {
                plans.add(plan(proxy));
            }
            selected = List.copyOf(plans);
        }
        selectionCache = new SelectionCache(current, selected, now + 1_000_000_000L);
        return selected;
    }

    /**
     * Reports connection failure to the selector.
     *
     * @param uri     target URI whose connection attempt failed
     * @param address proxy socket address used by the failed attempt
     * @param failure I/O failure reported to the wrapped selector
     * @throws ValidateException if any argument is {@code null}
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
     * @param proxy non-null JDK proxy returned by {@link ProxySelector#select(URI)}
     * @return direct, HTTP, or SOCKS plan using the proxy host string and port
     * @throws ValidateException if {@code proxy} is {@code null}
     * @throws ProtocolException if a non-direct proxy address is not an {@link InetSocketAddress}
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

    /**
     * One bounded-lifetime selector result.
     *
     * @param uri            URI supplied to the selector
     * @param plans          normalized proxy plans
     * @param expiresAtNanos monotonic expiration time
     */
    private record SelectionCache(URI uri, List<ProxyPlan> plans, long expiresAtNanos) {
    }

}
