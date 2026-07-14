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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
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
     * PROXY protocol v1 command token.
     */
    private static final String COMMAND_PROXY = "PROXY";

    /**
     * PROXY protocol v1 unknown transport token.
     */
    private static final String PROTOCOL_UNKNOWN = "UNKNOWN";

    /**
     * PROXY protocol v1 IPv4 TCP token.
     */
    private static final String PROTOCOL_TCP4 = "TCP4";

    /**
     * PROXY protocol v1 IPv6 TCP token.
     */
    private static final String PROTOCOL_TCP6 = "TCP6";

    /**
     * Creates a validated proxy header.
     */
    public ProxyHeader {
        if (unknown(source, target, sourcePort, targetPort)) {
            source = Normal.EMPTY;
            target = Normal.EMPTY;
            Assert.isTrue(
                    sourceAddress == null,
                    () -> new ValidateException("UNKNOWN proxy header must not contain a source address"));
        } else {
            source = normalizeAnyIp(source, "Source");
            target = normalizeAnyIp(target, "Target");
            sourcePort = validatePort(sourcePort);
            targetPort = validatePort(targetPort);
            sourceAddress = Assert.notNull(
                    sourceAddress,
                    () -> new ValidateException("Source address must match parsed source endpoint"));
            Assert.isTrue(
                    source.equals(sourceAddress.host()) && sourcePort == sourceAddress.port(),
                    () -> new ValidateException("Source address must match parsed source endpoint"));
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
        final String[] tokens = value.split(Symbol.SPACE, Normal.__1);
        if (tokens.length < Normal._2) {
            throw new ValidateException("PROXY header must include command and protocol");
        }
        if (!COMMAND_PROXY.equals(tokens[Normal._0])) {
            throw new ProtocolException("Invalid PROXY header command");
        }
        final String protocol = tokens[Normal._1].toUpperCase(Locale.ROOT);
        if (PROTOCOL_UNKNOWN.equals(protocol)) {
            // UNKNOWN intentionally carries no synthesized Address.
            return new ProxyHeader(Normal.EMPTY, Normal.EMPTY, Normal._0, Normal._0, null);
        }
        if (!PROTOCOL_TCP4.equals(protocol) && !PROTOCOL_TCP6.equals(protocol)) {
            throw new ProtocolException("Unsupported PROXY header protocol: " + protocol);
        }
        if (tokens.length != Normal._6) {
            throw new ValidateException("TCP PROXY header must contain six tokens");
        }
        final String source = normalizeIp(tokens[Normal._2], protocol, "Source");
        final String target = normalizeIp(tokens[Normal._3], protocol, "Target");
        final int sourcePort = parsePort(tokens[Normal._4], "Source");
        final int targetPort = parsePort(tokens[Normal._5], "Target");
        return new ProxyHeader(source, target, sourcePort, targetPort,
                new Address(Protocol.TCP.name, source, sourcePort, Symbol.SLASH));
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
        final String checked = Assert
                .notBlank(line, () -> new ValidateException("PROXY header line must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("PROXY header line must be non-blank and single-line"));
        return checked.trim();
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
        return Normal.EMPTY.equals(source) && Normal.EMPTY.equals(target) && sourcePort == Normal._0
                && targetPort == Normal._0;
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
        return PROTOCOL_TCP4.equals(protocol) ? normalizeIpv4(value) : normalizeIpv6(value, name);
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
        if (checked.indexOf(Symbol.C_PERCENT) >= Normal._0) {
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
        return Assert.checkBetween(
                port,
                Normal._1,
                Normal._65535,
                () -> new ValidateException("PROXY port must be between 1 and 65535"));
    }

    /**
     * Validates a single-line token.
     *
     * @param value value
     * @param name  field name
     * @return token
     */
    private static String validateToken(final String value, final String name) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException(name + " must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return checked;
    }

}
