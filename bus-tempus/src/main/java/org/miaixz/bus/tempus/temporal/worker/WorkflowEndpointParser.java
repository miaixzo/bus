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
package org.miaixz.bus.tempus.temporal.worker;

import java.net.URI;
import java.util.Locale;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Parses Temporal workflow endpoints into normalized gRPC targets.
 * <p>
 * The parser accepts plain host names, host-port pairs, IPv6 literals, {@code dns:///} targets, and HTTP/HTTPS URI
 * forms without requiring any transport-specific dependency.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WorkflowEndpointParser {

    /**
     * Default Temporal frontend port.
     */
    private static final int DEFAULT_TEMPORAL_PORT = 7233;

    /**
     * gRPC DNS target scheme.
     */
    private static final String DNS_SCHEME = "dns";

    /**
     * gRPC DNS target prefix.
     */
    private static final String DNS_TARGET_PREFIX = DNS_SCHEME + Symbol.COLON + Symbol.SLASH + Symbol.FORWARDSLASH;

    /**
     * Plaintext URI scheme.
     */
    private static final String HTTP_SCHEME = Protocol.HTTP.getName();

    /**
     * TLS URI scheme.
     */
    private static final String HTTPS_SCHEME = Protocol.HTTPS.getName();

    /**
     * Prevents utility instantiation.
     */
    private WorkflowEndpointParser() {
        // No initialization required.
    }

    /**
     * Parses a configured endpoint into normalized Temporal endpoint metadata.
     *
     * @param endpoint raw endpoint value
     * @return endpoint metadata
     */
    public static Endpoint resolve(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return new Endpoint(endpoint, endpoint, endpoint, null, -1, false);
        }
        String rawEndpoint = endpoint.trim();
        if (rawEndpoint.startsWith(DNS_TARGET_PREFIX)) {
            Target target = resolveTarget(rawEndpoint.substring(DNS_TARGET_PREFIX.length()), rawEndpoint);
            return new Endpoint(rawEndpoint, target.target(), formatGrpcTarget(target.target()), target.host(),
                    target.port(), false);
        }
        if (rawEndpoint.contains(Symbol.COLON + Symbol.FORWARDSLASH)) {
            return resolveUri(rawEndpoint);
        }
        Target target = resolveTarget(rawEndpoint, rawEndpoint);
        return new Endpoint(rawEndpoint, target.target(), formatGrpcTarget(target.target()), target.host(),
                target.port(), false);
    }

    /**
     * Resolves an HTTP or HTTPS URI endpoint.
     *
     * @param rawEndpoint raw URI endpoint
     * @return endpoint metadata
     */
    private static Endpoint resolveUri(String rawEndpoint) {
        URI uri = URI.create(rawEndpoint);
        String scheme = uri.getScheme();
        if (StringKit.isBlank(scheme)) {
            throw invalidEndpoint(rawEndpoint, "missing URI scheme");
        }
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!HTTP_SCHEME.equals(scheme) && !HTTPS_SCHEME.equals(scheme)) {
            throw invalidEndpoint(rawEndpoint, "unsupported URI scheme: " + scheme);
        }
        if (StringKit.isNotBlank(uri.getRawUserInfo())) {
            throw invalidEndpoint(rawEndpoint, "user info is not supported");
        }
        if (StringKit.isNotBlank(uri.getRawQuery()) || StringKit.isNotBlank(uri.getRawFragment())) {
            throw invalidEndpoint(rawEndpoint, "query and fragment are not supported");
        }
        String path = uri.getRawPath();
        if (StringKit.isNotBlank(path) && !Symbol.SLASH.equals(path)) {
            throw invalidEndpoint(rawEndpoint, "path is not supported");
        }
        String authority = uri.getRawAuthority();
        if (StringKit.isBlank(authority) || authority.endsWith(Symbol.COLON)) {
            throw invalidEndpoint(rawEndpoint, "missing host or port");
        }
        String host = uri.getHost();
        if (StringKit.isBlank(host)) {
            throw invalidEndpoint(rawEndpoint, "missing host");
        }
        int port = uri.getPort() > 0 ? uri.getPort() : DEFAULT_TEMPORAL_PORT;
        String target = formatHostPort(host, port);
        boolean tls = HTTPS_SCHEME.equals(scheme);
        return new Endpoint(rawEndpoint, scheme + Symbol.COLON + Symbol.FORWARDSLASH + target, formatGrpcTarget(target),
                host, port, tls);
    }

    /**
     * Resolves a plain target value.
     *
     * @param targetValue raw target value
     * @param rawEndpoint original endpoint value used for errors
     * @return normalized target
     */
    private static Target resolveTarget(String targetValue, String rawEndpoint) {
        String target = targetValue;
        while (target.startsWith(Symbol.SLASH)) {
            target = target.substring(1);
        }
        if (StringKit.isBlank(target)) {
            throw invalidEndpoint(rawEndpoint, "missing host");
        }
        if (target.contains(Symbol.SLASH)) {
            throw invalidEndpoint(rawEndpoint, "path is not supported");
        }
        if (target.startsWith(Symbol.BRACKET_LEFT)) {
            return resolveBracketedHost(target, rawEndpoint);
        }
        int colonCount = count(target, Symbol.C_COLON);
        if (colonCount == 0) {
            return new Target(target, DEFAULT_TEMPORAL_PORT, formatHostPort(target, DEFAULT_TEMPORAL_PORT));
        }
        if (colonCount == 1) {
            int colon = target.indexOf(Symbol.C_COLON);
            String host = target.substring(0, colon);
            if (StringKit.isBlank(host)) {
                throw invalidEndpoint(rawEndpoint, "missing host");
            }
            String port = target.substring(colon + 1);
            validatePort(port, rawEndpoint);
            int parsedPort = Integer.parseInt(port);
            return new Target(host, parsedPort, formatHostPort(host, parsedPort));
        }
        return new Target(target, DEFAULT_TEMPORAL_PORT, formatHostPort(target, DEFAULT_TEMPORAL_PORT));
    }

    /**
     * Resolves a bracketed IPv6 target.
     *
     * @param target      raw target
     * @param rawEndpoint original endpoint value used for errors
     * @return normalized target
     */
    private static Target resolveBracketedHost(String target, String rawEndpoint) {
        int closeBracket = target.indexOf(Symbol.C_BRACKET_RIGHT);
        if (closeBracket < 0) {
            throw invalidEndpoint(rawEndpoint, "invalid IPv6 endpoint");
        }
        String host = target.substring(1, closeBracket);
        if (StringKit.isBlank(host)) {
            throw invalidEndpoint(rawEndpoint, "missing host");
        }
        String suffix = target.substring(closeBracket + 1);
        if (suffix.isEmpty()) {
            return new Target(host, DEFAULT_TEMPORAL_PORT, formatHostPort(host, DEFAULT_TEMPORAL_PORT));
        }
        if (!suffix.startsWith(Symbol.COLON)) {
            throw invalidEndpoint(rawEndpoint, "invalid IPv6 endpoint");
        }
        String port = suffix.substring(1);
        validatePort(port, rawEndpoint);
        int parsedPort = Integer.parseInt(port);
        return new Target(host, parsedPort, formatHostPort(host, parsedPort));
    }

    /**
     * Formats a host-port pair.
     *
     * @param host host value
     * @param port port value
     * @return formatted target
     */
    private static String formatHostPort(String host, int port) {
        String formattedHost = host.contains(Symbol.COLON) && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
        return formattedHost + Symbol.COLON + port;
    }

    /**
     * Formats a gRPC target.
     *
     * @param target normalized host-port target
     * @return gRPC target
     */
    private static String formatGrpcTarget(String target) {
        return isIpTarget(target) ? target : DNS_TARGET_PREFIX + target;
    }

    /**
     * Checks whether the target points to an IP literal.
     *
     * @param target normalized target
     * @return {@code true} when the host is an IP literal
     */
    private static boolean isIpTarget(String target) {
        String host = extractHost(target);
        if (StringKit.isBlank(host)) {
            return false;
        }
        if (host.indexOf(Symbol.C_COLON) >= 0) {
            return true;
        }
        for (int i = 0; i < host.length(); i++) {
            char value = host.charAt(i);
            if ((value < Symbol.C_ZERO || value > Symbol.C_NINE) && value != Symbol.C_DOT) {
                return false;
            }
        }
        return host.indexOf(Symbol.C_DOT) > 0;
    }

    /**
     * Extracts a host value from a normalized target.
     *
     * @param target normalized target
     * @return host value
     */
    private static String extractHost(String target) {
        if (StringKit.isBlank(target)) {
            return null;
        }
        if (target.startsWith(Symbol.BRACKET_LEFT)) {
            int closeBracket = target.indexOf(Symbol.C_BRACKET_RIGHT);
            return closeBracket > 0 ? target.substring(1, closeBracket) : null;
        }
        int colon = target.lastIndexOf(Symbol.C_COLON);
        return colon > 0 ? target.substring(0, colon) : target;
    }

    /**
     * Validates a port string.
     *
     * @param port        raw port value
     * @param rawEndpoint original endpoint value used for errors
     */
    private static void validatePort(String port, String rawEndpoint) {
        if (StringKit.isBlank(port)) {
            throw invalidEndpoint(rawEndpoint, "missing port");
        }
        try {
            int parsed = Integer.parseInt(port);
            if (parsed <= 0 || parsed > 65535) {
                throw invalidEndpoint(rawEndpoint, "port out of range: " + port);
            }
        } catch (NumberFormatException e) {
            throw invalidEndpoint(rawEndpoint, "invalid port: " + port);
        }
    }

    /**
     * Counts matching characters.
     *
     * @param value  source value
     * @param target target character
     * @return number of matches
     */
    private static int count(String value, char target) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * Builds an invalid endpoint exception.
     *
     * @param endpoint endpoint value
     * @param reason   failure reason
     * @return invalid endpoint exception
     */
    private static IllegalArgumentException invalidEndpoint(String endpoint, String reason) {
        return new IllegalArgumentException("Invalid Temporal endpoint '" + endpoint + "': " + reason);
    }

    /**
     * Temporal endpoint metadata.
     *
     * @param rawEndpoint raw endpoint
     * @param endpoint    normalized endpoint
     * @param target      gRPC target
     * @param host        host value
     * @param port        port value
     * @param enableHttps whether TLS is enabled
     */
    public record Endpoint(String rawEndpoint, String endpoint, String target, String host, int port,
            boolean enableHttps) {
    }

    /**
     * Internal normalized target.
     *
     * @param host   host value
     * @param port   port value
     * @param target normalized target
     */
    record Target(String host, int port, String target) {
    }

}
