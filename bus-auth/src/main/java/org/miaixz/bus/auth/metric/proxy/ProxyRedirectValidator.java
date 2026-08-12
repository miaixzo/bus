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
package org.miaixz.bus.auth.metric.proxy;

import java.net.URI;
import java.util.Set;

import org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy;
import org.miaixz.bus.auth.metric.ProxyAuth.Config;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

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
     * Strict HTTPS policy.
     */
    private final TransportPolicy policy;

    /**
     * Creates one validator.
     *
     * @param configuration closed configuration
     */
    public ProxyRedirectValidator(final Config configuration) {
        final Config source = Assert
                .notNull(configuration, () -> new ValidateException("Proxy configuration must not be null"));
        this.policy = source.transportPolicy();
        Assert.isTrue(
                policy.allowedSchemes().equals(Set.of(Protocol.HTTPS)),
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
     * Validates one URI against a registered origin.
     *
     * @param uri candidate URI
     * @return unchanged validated URI
     */
    public URI validate(final URI uri) {
        final URI candidate = UriValidator.https(UriValidator.transport(uri, policy));
        Assert.isTrue(
                origins.stream().anyMatch(origin -> same(origin, candidate)),
                () -> new ValidateException("Proxy URI origin is not registered"));
        return candidate;
    }

    /**
     * Converts and validates a registered URI as an exact origin.
     *
     * @param uri registered URI
     * @return canonical origin URI
     */
    URI origin(final URI uri) {
        final URI value = UriValidator.https(UriValidator.transport(uri, policy));
        Assert.isTrue(
                (value.getPath().isEmpty() || "/".equals(value.getPath())) && value.getQuery() == null,
                () -> new ValidateException("Proxy registered origin must not contain a path or query"));
        return value;
    }

}
