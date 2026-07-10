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

import java.math.BigInteger;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.ip.IPv4;
import org.miaixz.bus.core.net.ip.IPv6;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;

/**
 * Parsed PROXY protocol v1 header with cached source address metadata.
 *
 * @param source        source address text
 * @param target        target address text
 * @param sourcePort    source port
 * @param targetPort    target port
 * @param sourceAddress source address mapping
 * @author Kimi Liu
 * @since Java 21+
 */
public record ProxyHeader(String source, String target, int sourcePort, int targetPort, Address sourceAddress) {

    /**
     * Creates a validated proxy header.
     */
    public ProxyHeader {
        if (unknown(source, target, sourcePort, targetPort)) {
            source = Normal.EMPTY;
            target = Normal.EMPTY;
            if (sourceAddress != null) {
                throw new ValidateException("UNKNOWN proxy header must not contain a source address");
            }
        } else {
            source = normalizeAnyIp(source, "Source");
            target = normalizeAnyIp(target, "Target");
            sourcePort = validatePort(sourcePort);
            targetPort = validatePort(targetPort);
            if (sourceAddress == null || !source.equals(sourceAddress.host()) || sourcePort != sourceAddress.port()) {
                throw new ValidateException("Source address must match parsed source endpoint");
            }
        }
    }

    /**
     * Parses a PROXY protocol v1 line.
     *
     * @param line header line without CRLF
     * @return proxy header
     */
    public static ProxyHeader parse(final String line) {
        final String value = validateLine(line);
        final String[] tokens = value.split(Symbol.SPACE, -1);
        if (tokens.length < 2) {
            throw new ValidateException("PROXY header must include command and protocol");
        }
        if (!"PROXY".equals(tokens[0])) {
            throw new ProtocolException("Invalid PROXY header command");
        }
        final String protocol = tokens[1].toUpperCase(Locale.ROOT);
        if ("UNKNOWN".equals(protocol)) {
            // UNKNOWN intentionally carries no synthesized Address.
            return new ProxyHeader(Normal.EMPTY, Normal.EMPTY, 0, 0, null);
        }
        if (!"TCP4".equals(protocol) && !"TCP6".equals(protocol)) {
            throw new ProtocolException("Unsupported PROXY header protocol: " + protocol);
        }
        if (tokens.length != 6) {
            throw new ValidateException("TCP PROXY header must contain six tokens");
        }
        final String source = normalizeIp(tokens[2], protocol, "Source");
        final String target = normalizeIp(tokens[3], protocol, "Target");
        final int sourcePort = parsePort(tokens[4], "Source");
        final int targetPort = parsePort(tokens[5], "Target");
        return new ProxyHeader(source, target, sourcePort, targetPort,
                new Address("tcp", source, sourcePort, Symbol.SLASH));
    }

    /**
     * Returns the source address.
     *
     * @return source address
     */
    @Override
    public String source() {
        return source;
    }

    /**
     * Returns the target address.
     *
     * @return target address
     */
    @Override
    public String target() {
        return target;
    }

    /**
     * Returns the source port.
     *
     * @return source port
     */
    @Override
    public int sourcePort() {
        return sourcePort;
    }

    /**
     * Returns the target port.
     *
     * @return target port
     */
    @Override
    public int targetPort() {
        return targetPort;
    }

    /**
     * Returns the cached source address.
     *
     * @return source address, or null for UNKNOWN headers
     */
    @Override
    public Address sourceAddress() {
        return sourceAddress;
    }

    /**
     * Validates a header line.
     *
     * @param line header line
     * @return trimmed line
     */
    private static String validateLine(final String line) {
        if (StringKit.isBlank(line) || StringKit.containsAny(line, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("PROXY header line must be non-blank and single-line");
        }
        return line.trim();
    }

    /**
     * Returns whether the values represent UNKNOWN.
     *
     * @param source     source
     * @param target     target
     * @param sourcePort source port
     * @param targetPort target port
     * @return true when UNKNOWN
     */
    private static boolean unknown(
            final String source,
            final String target,
            final int sourcePort,
            final int targetPort) {
        return Normal.EMPTY.equals(source) && Normal.EMPTY.equals(target) && sourcePort == 0 && targetPort == 0;
    }

    /**
     * Normalizes an IP address with any supported family.
     *
     * @param value value
     * @param name  field name
     * @return normalized IP
     */
    private static String normalizeAnyIp(final String value, final String name) {
        try {
            return normalizeIpv4(value);
        } catch (final ProtocolException e) {
            return normalizeIpv6(value, name);
        }
    }

    /**
     * Normalizes an IP address for a PROXY protocol family.
     *
     * @param value    value
     * @param protocol protocol token
     * @param name     field name
     * @return normalized IP
     */
    private static String normalizeIp(final String value, final String protocol, final String name) {
        return "TCP4".equals(protocol) ? normalizeIpv4(value) : normalizeIpv6(value, name);
    }

    /**
     * Normalizes an IPv4 address.
     *
     * @param value value
     * @return normalized IPv4
     */
    private static String normalizeIpv4(final String value) {
        final String checked = validateToken(value, "IPv4 address");
        try {
            return IPv4.longToIpv4(IPv4.ipv4ToLong(checked));
        } catch (final RuntimeException e) {
            throw new ProtocolException("Invalid IPv4 address: " + checked, e);
        }
    }

    /**
     * Normalizes an IPv6 address.
     *
     * @param value value
     * @param name  field name
     * @return normalized IPv6
     */
    private static String normalizeIpv6(final String value, final String name) {
        final String checked = validateToken(value, name);
        if (checked.indexOf(Symbol.C_PERCENT) >= 0) {
            throw new ProtocolException("IPv6 zone identifiers are not supported");
        }
        final BigInteger number = IPv6.ipv6ToBigInteger(checked);
        if (number == null) {
            throw new ProtocolException("Invalid IPv6 address: " + checked);
        }
        final String normalized = IPv6.bigIntegerToIPv6(number);
        if (normalized == null) {
            throw new ProtocolException("Invalid IPv6 address: " + checked);
        }
        return normalized;
    }

    /**
     * Parses and validates a port.
     *
     * @param value value
     * @param name  field name
     * @return port
     */
    private static int parsePort(final String value, final String name) {
        final String checked = validateToken(value, name + " port");
        try {
            return validatePort(Integer.parseInt(checked));
        } catch (final NumberFormatException e) {
            throw new ValidateException(name + " port must be numeric", e);
        }
    }

    /**
     * Validates a port range.
     *
     * @param port port
     * @return port
     */
    private static int validatePort(final int port) {
        if (port < 1 || port > 65535) {
            throw new ValidateException("PROXY port must be between 1 and 65535");
        }
        return port;
    }

    /**
     * Validates a single-line token.
     *
     * @param value value
     * @param name  field name
     * @return token
     */
    private static String validateToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

}
