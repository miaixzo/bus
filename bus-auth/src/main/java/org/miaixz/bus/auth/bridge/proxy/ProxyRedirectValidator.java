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
package org.miaixz.bus.auth.bridge.proxy;

import java.net.URI;
import java.util.Set;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.bridge.proxy.ProxyAuth.Config;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;

/**
 * Accepts only exact registered HTTPS origins and applies the runtime transport policy before origin comparison. DNS
 * resolution and target CIDR enforcement remain the responsibility of the bus-fabric transport adapter.
 *
 * @author Kimi Liu
 */
public final class ProxyRedirectValidator {

    /**
     * Registered canonical origins.
     */
    private final Set<URI> origins;

    /**
     * Fabric scheme and port policy applied before origin comparison.
     */
    private final AddressPolicy addressPolicy;

    /**
     * Creates one validator.
     *
     * @param configuration closed configuration
     * @throws ValidateException if configuration or its redirect policy is invalid
     */
    public ProxyRedirectValidator(final Config configuration) {
        final Config source = Assert
                .notNull(configuration, () -> new ValidateException("Proxy configuration must not be null"));
        this.addressPolicy = source.transportPolicy().addressPolicy();
        Assert.isTrue(
                addressPolicy.allowedSchemes().equals(Set.of(Protocol.HTTPS)),
                () -> new ValidateException("Proxy redirect policy must allow HTTPS only"));
        this.origins = source.registeredOrigins().stream().map(this::origin)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        validate(source.loginUri());
    }

    /**
     * Compares two exact origins using effective HTTPS ports.
     *
     * @param origin    registered origin
     * @param candidate candidate URI
     * @return whether origins match
     */
    static boolean same(final URI origin, final URI candidate) {
        final int left = origin.getPort() < Normal._0 ? 443 : origin.getPort();
        final int right = candidate.getPort() < Normal._0 ? 443 : candidate.getPort();
        return origin.getScheme().equalsIgnoreCase(candidate.getScheme())
                && origin.getHost().equalsIgnoreCase(candidate.getHost()) && left == right;
    }

    /**
     * Validates a normalized absolute HTTPS URI without credentials or fragments.
     *
     * @param uri source URI
     * @return validated URI
     * @throws ValidateException if the URI is not a canonical HTTPS network URI
     */
    private static URI absoluteHttps(final URI uri) {
        final URI value = Assert.notNull(uri, () -> new ValidateException("Proxy URI must not be null"));
        Assert.isTrue(
                value.isAbsolute() && !value.isOpaque() && Protocol.HTTPS.name.equalsIgnoreCase(value.getScheme())
                        && value.getHost() != null && !value.getHost().isBlank() && value.getUserInfo() == null
                        && value.getFragment() == null && value.equals(value.normalize()),
                () -> new ValidateException("Proxy URI must be a canonical absolute HTTPS URI"));
        return value;
    }

    /**
     * Validates one URI against a registered origin.
     *
     * @param uri candidate URI
     * @return unchanged validated URI
     * @throws ValidateException if the URI violates address or registered-origin policy
     */
    public URI validate(final URI uri) {
        final URI candidate = absoluteHttps(uri);
        final Address address = Address.from(candidate);
        Assert.isTrue(
                addressPolicy.allowedSchemes().contains(address.protocol())
                        && addressPolicy.allowedPorts().contains(address.port()),
                () -> new ValidateException("Proxy URI violates the address policy"));
        Assert.isTrue(
                origins.stream().anyMatch(origin -> same(origin, candidate)),
                () -> new ValidateException("Proxy URI origin is not registered"));
        return candidate;
    }

    /**
     * Validates a URI and, when present, requires the Context remote endpoint to have the same origin.
     *
     * @param context non-null invocation context
     * @param uri     candidate URI
     * @return unchanged validated URI
     * @throws ValidateException if the context is null or an origin constraint fails
     */
    public URI validate(final Context context, final URI uri) {
        final Context invocation = Assert
                .notNull(context, () -> new ValidateException("Proxy context must not be null"));
        final URI candidate = validate(uri);
        invocation.remoteEndpoint().map(Endpoint::address).ifPresent(
                endpoint -> Assert.isTrue(
                        same(endpoint.toUri(), candidate),
                        () -> new ValidateException("Proxy URI differs from the Context remote endpoint")));
        return candidate;
    }

    /**
     * Converts and validates a registered URI as an exact origin.
     *
     * @param uri registered URI
     * @return canonical origin URI
     * @throws ValidateException if the URI contains a path or query
     */
    URI origin(final URI uri) {
        final URI value = absoluteHttps(uri);
        final Address address = Address.from(value);
        Assert.isTrue(
                addressPolicy.allowedSchemes().contains(address.protocol())
                        && addressPolicy.allowedPorts().contains(address.port()),
                () -> new ValidateException("Proxy registered origin violates the address policy"));
        Assert.isTrue(
                (value.getPath().isEmpty() || "/".equals(value.getPath())) && value.getQuery() == null,
                () -> new ValidateException("Proxy registered origin must not contain a path or query"));
        return value;
    }

}
