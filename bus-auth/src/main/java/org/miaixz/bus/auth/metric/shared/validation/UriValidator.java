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
package org.miaixz.bus.auth.metric.shared.validation;

import java.net.URI;
import java.util.Locale;

import org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;

/**
 * Performs network-free URI safety validation before a transport adapter resolves or connects. It requires canonical
 * absolute hierarchical URIs with an ASCII host, no user information, and no fragment; maps scheme and effective port
 * to the closed {@link TransportPolicy}; enforces HTTPS where requested; compares complete origins; and admits only
 * same-origin redirects within the configured redirect count. DNS result and CIDR enforcement remain in bus-fabric's
 * address guard at connection time.
 * <p>
 * <strong>Bus dependencies:</strong> {@link Protocol} and {@link Port} provide canonical schemes and registered
 * defaults, {@link TransportPolicy} provides scheme, port, CIDR, and redirect limits already validated through
 * bus-fabric {@code AddressPolicy}, and {@link ErrorCode} provides stable URL and HTTPS failures.
 *
 * @author Kimi Liu
 */
public final class UriValidator {

    /**
     * Prevents instantiation of the URI validation utility.
     */
    private UriValidator() {
        // No initialization required.
    }

    /**
     * Validates one canonical absolute URI independent of a transport policy.
     *
     * @param uri source URI
     * @return unchanged validated URI
     */
    public static URI absolute(final URI uri) {
        final URI current = Assert.notNull(uri, () -> new ValidateException("URI must not be null"));
        require(current.isAbsolute() && !current.isOpaque());
        require(
                current.getScheme() != null
                        && current.getScheme().equals(current.getScheme().toLowerCase(Locale.ROOT)));
        require(current.getRawAuthority() != null && current.getHost() != null && !current.getHost().isBlank());
        require(current.getUserInfo() == null && current.getFragment() == null);
        require(current.equals(current.normalize()));
        require(current.getPort() >= Normal.__1 && current.getPort() <= Normal._65535);
        return current;
    }

    /**
     * Validates an absolute URI against a closed transport scheme and port policy.
     *
     * @param uri    source URI
     * @param policy transport policy
     * @return unchanged validated URI
     */
    public static URI transport(final URI uri, final TransportPolicy policy) {
        final URI current = absolute(uri);
        final TransportPolicy rules = Assert
                .notNull(policy, () -> new ValidateException("Transport policy must not be null"));
        final Protocol protocol = protocol(current.getScheme());
        final int port = effectivePort(current, protocol);
        require(rules.allowedSchemes().contains(protocol) && rules.allowedPorts().contains(port));
        return current;
    }

    /**
     * Requires one absolute URI to use HTTPS.
     *
     * @param uri source URI
     * @return unchanged validated HTTPS URI
     */
    public static URI https(final URI uri) {
        final URI current = absolute(uri);
        if (!Protocol.HTTPS.name().equalsIgnoreCase(current.getScheme())) {
            throw new ProtocolException(ErrorCode._100209);
        }
        return current;
    }

    /**
     * Requires an actual URI to have the exact scheme, host, and effective port of an expected origin.
     *
     * @param expected expected origin URI
     * @param actual   actual URI
     * @return unchanged validated actual URI
     */
    public static URI sameOrigin(final URI expected, final URI actual) {
        final URI left = absolute(expected);
        final URI right = absolute(actual);
        final Protocol leftProtocol = protocol(left.getScheme());
        final Protocol rightProtocol = protocol(right.getScheme());
        require(leftProtocol == rightProtocol);
        require(left.getHost().equalsIgnoreCase(right.getHost()));
        require(effectivePort(left, leftProtocol) == effectivePort(right, rightProtocol));
        return right;
    }

    /**
     * Validates one same-origin redirect against its one-based redirect count and closed transport policy.
     *
     * @param previous previously validated URI
     * @param redirect redirect target URI
     * @param policy   transport policy
     * @param count    one-based redirect count
     * @return unchanged validated redirect URI
     */
    public static URI redirect(final URI previous, final URI redirect, final TransportPolicy policy, final int count) {
        final TransportPolicy rules = Assert
                .notNull(policy, () -> new ValidateException("Transport policy must not be null"));
        require(count > Normal._0 && count <= rules.redirectLimit());
        final URI source = transport(previous, rules);
        final URI target = transport(redirect, rules);
        if (Protocol.HTTPS.name().equalsIgnoreCase(source.getScheme())
                && !Protocol.HTTPS.name().equalsIgnoreCase(target.getScheme())) {
            throw new ProtocolException(ErrorCode._100209);
        }
        return sameOrigin(source, target);
    }

    /**
     * Converts one supported URI scheme to the canonical Bus protocol.
     *
     * @param scheme lowercase URI scheme
     * @return Bus protocol
     */
    private static Protocol protocol(final String scheme) {
        return switch (scheme) {
            case "http" -> Protocol.HTTP;
            case "https" -> Protocol.HTTPS;
            case "ldap" -> Protocol.LDAP;
            case "ldaps" -> Protocol.LDAPS;
            default -> throw new ProtocolException(ErrorCode._100143);
        };
    }

    /**
     * Resolves an explicit or registered default port.
     *
     * @param uri      validated URI
     * @param protocol canonical protocol
     * @return effective port
     */
    private static int effectivePort(final URI uri, final Protocol protocol) {
        if (uri.getPort() >= Normal._0) {
            return uri.getPort();
        }
        return switch (protocol) {
            case HTTP -> Port._80.getPort();
            case HTTPS -> Port._443.getPort();
            case LDAP -> Port._389.getPort();
            case LDAPS -> Port._636.getPort();
            default -> throw new ProtocolException(ErrorCode._100143);
        };
    }

    /**
     * Requires one URI security invariant.
     *
     * @param condition required invariant
     */
    private static void require(final boolean condition) {
        if (!condition) {
            throw new ProtocolException(ErrorCode._100143);
        }
    }

}
