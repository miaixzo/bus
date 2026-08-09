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
package org.miaixz.bus.fabric.protocol.http.chain;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Pure HTTP route planner that performs no DNS, socket, pool, or TLS I/O.
 *
 * @author Kimi Liu
 */
final class HttpRoutePlanner {

    /**
     * Complete TLS policy embedded into stable destination identity.
     */
    private final TlsPolicy tlsPolicy;

    /**
     * Creates a route planner.
     *
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     */
    HttpRoutePlanner(final TlsContext tlsContext, final TlsSettings tlsSettings) {
        if ((tlsContext == null) != (tlsSettings == null)) {
            throw new ValidateException("TLS context and settings must both be present or both be absent");
        }
        this.tlsPolicy = tlsContext == null ? null : TlsPolicy.of(tlsContext, tlsSettings);
    }

    /**
     * Resolves request proxy configuration.
     *
     * @param request request
     * @return proxy plan
     */
    ProxyPlan proxy(final HttpRequest request) {
        final ProxyPlan configured = request.proxy();
        Assert.isTrue(
                configured.isResolved(),
                () -> new ValidateException("HTTP route requires a resolved proxy plan"));
        return configured;
    }

    /**
     * Plans one immutable route.
     *
     * @param target    request target
     * @param proxy     proxy plan
     * @param nativeTls whether the connector supports native TLS
     * @return route plan
     */
    Plan plan(final Address target, final ProxyPlan proxy, final boolean nativeTls) {
        validate(proxy);
        final Protocol protocol = target.secure() ? Protocol.HTTPS : Protocol.HTTP;
        Options options = Options.of(Builder.OPTION_TLS, target.secure()).with(Builder.OPTION_SECURE, target.secure())
                .with(Builder.OPTION_MULTIPLEX, protocol == Protocol.HTTP_2)
                .with(Builder.OPTION_PROTOCOL, protocol.name).with(Builder.OPTION_ROUTE_PROXY, routeIdentity(proxy))
                .with(Builder.OPTION_ROUTE_TUNNEL, proxy.requiresTunnel(target));
        if (tlsPolicy != null) {
            options = options.with(TlsPolicy.OPTION, tlsPolicy);
        }
        final Destination destination = Destination.of(protocol, target, options);
        final Address connectAddress = proxy.proxy().orElseGet(
                () -> target.secure() && !nativeTls
                        ? new Address(Protocol.TCP.toString(), target.host(), target.port(), target.path())
                        : target);
        return new Plan(destination, connectAddress, proxy.requiresTunnel(target), mode(proxy));
    }

    /**
     * Validates supported proxy transports.
     *
     * @param proxy proxy plan
     */
    private static void validate(final ProxyPlan proxy) {
        proxy.proxy().ifPresent(address -> {
            if (proxy.isHttp() && !Protocol.HTTP.name.equals(address.scheme())) {
                throw new ProtocolException("Unsupported HTTP proxy transport");
            }
            if (proxy.isSocks() && (address.secure() || !Transport.fromScheme(address.scheme()).connectionOriented())) {
                throw new ProtocolException("Unsupported SOCKS proxy transport");
            }
        });
    }

    /**
     * Returns a stable diagnostic proxy label.
     *
     * @param proxy proxy plan
     * @return proxy mode
     */
    private static String mode(final ProxyPlan proxy) {
        if (proxy.isDirect()) {
            return Builder.PROXY_PLAN_DIRECT_ID;
        }
        if (proxy.isSocks()) {
            return Builder.PROXY_PLAN_SOCKS_ID;
        }
        return proxy.isHttp() ? Builder.PROXY_PLAN_HTTP_ID : "custom";
    }

    /**
     * Builds a credential-safe pool partition key for one resolved route.
     *
     * @param proxy resolved direct, HTTP, or SOCKS route plan
     * @return redacted route identity including an authorization partition hash when required
     */
    private static String routeIdentity(final ProxyPlan proxy) {
        if (proxy.isDirect()) {
            return Builder.PROXY_PLAN_DIRECT_ID;
        }
        final String authentication = proxy.authorization().size() == 0 ? ""
                : "#auth=" + Integer.toHexString(proxy.authorization().asMap().hashCode());
        return proxy.id() + authentication;
    }

    /**
     * Immutable route decision.
     *
     * @param destination    pool identity
     * @param connectAddress physical address opened before proxy/TLS upgrades
     * @param tunnel         whether HTTP CONNECT is required
     * @param proxyMode      diagnostic proxy mode
     */
    record Plan(Destination destination, Address connectAddress, boolean tunnel, String proxyMode) {
    }

}
