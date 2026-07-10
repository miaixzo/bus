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
package org.miaixz.bus.fabric.network.dns;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable DNS resolution result.
 *
 * @param host       resolved host
 * @param addresses  address snapshot
 * @param resolvedAt resolution time
 * @param ttl        positive DNS ttl or {@link #NO_TTL} when no ttl is available
 * @param duration   resolution duration
 * @author Kimi Liu
 * @since Java 21+
 */
public record DnsResult(String host, List<InetAddress> addresses, Instant resolvedAt, Duration ttl, Duration duration) {

    /**
     * Fixed marker used when a resolver does not expose DNS TTL metadata.
     */
    public static final Duration NO_TTL = Duration.ZERO;

    /**
     * Creates a DNS result.
     */
    public DnsResult {
        host = validateHost(host);
        addresses = normalizeAddresses(addresses);
        if (resolvedAt == null) {
            throw new ValidateException("DNS resolved time must not be null");
        }
        if (ttl == null || ttl.isNegative()) {
            throw new ValidateException("DNS ttl must be non-null and non-negative");
        }
        if (duration == null || duration.isNegative()) {
            throw new ValidateException("DNS duration must be non-null and non-negative");
        }
    }

    /**
     * Creates a DNS result.
     *
     * @param host      host
     * @param addresses addresses
     * @param duration  duration
     * @return result
     */
    public static DnsResult of(final String host, final List<InetAddress> addresses, final Duration duration) {
        return of(host, addresses, Instant.now(), NO_TTL, duration);
    }

    /**
     * Creates a DNS result.
     *
     * @param host       host
     * @param addresses  addresses
     * @param resolvedAt resolution time
     * @param ttl        positive ttl or {@link #NO_TTL}
     * @param duration   duration
     * @return result
     */
    public static DnsResult of(
            final String host,
            final List<InetAddress> addresses,
            final Instant resolvedAt,
            final Duration ttl,
            final Duration duration) {
        return new DnsResult(host, addresses, resolvedAt, ttl, duration);
    }

    /**
     * Returns the host.
     *
     * @return host
     */
    @Override
    public String host() {
        return host;
    }

    /**
     * Returns address snapshot.
     *
     * @return addresses
     */
    @Override
    public List<InetAddress> addresses() {
        return addresses;
    }

    /**
     * Returns resolution time.
     *
     * @return resolution time
     */
    @Override
    public Instant resolvedAt() {
        return resolvedAt;
    }

    /**
     * Returns ttl metadata.
     *
     * @return ttl or {@link #NO_TTL}
     */
    @Override
    public Duration ttl() {
        return ttl;
    }

    /**
     * Returns duration.
     *
     * @return duration
     */
    @Override
    public Duration duration() {
        return duration;
    }

    /**
     * Returns whether no addresses were resolved.
     *
     * @return true when empty
     */
    public boolean empty() {
        return addresses.isEmpty();
    }

    /**
     * Returns whether ttl metadata is available.
     *
     * @return true when ttl is available
     */
    public boolean hasTtl() {
        return !NO_TTL.equals(ttl);
    }

    /**
     * Validates a host.
     *
     * @param host host
     * @return normalized host
     */
    private static String validateHost(final String host) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("DNS host must be non-blank and single-line");
        }
        String normalized = host.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith(Symbol.DOT)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            throw new ValidateException("DNS host must be non-blank");
        }
        if (normalized.indexOf(Symbol.C_COLON) >= 0) {
            return normalized;
        }
        try {
            return IDN.toASCII(normalized, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (final IllegalArgumentException e) {
            throw new ValidateException("DNS host must be a valid domain", e);
        }
    }

    /**
     * Returns a stable, duplicate-free address snapshot.
     *
     * @param addresses addresses
     * @return normalized addresses
     */
    private static List<InetAddress> normalizeAddresses(final List<InetAddress> addresses) {
        if (addresses == null) {
            throw new ValidateException("DNS addresses must not be null");
        }
        final HashSet<InetAddress> seen = new HashSet<>();
        final ArrayList<InetAddress> normalized = new ArrayList<>(addresses.size());
        for (final InetAddress address : addresses) {
            if (address == null) {
                throw new ValidateException("DNS addresses must contain no null elements");
            }
            if (seen.add(address)) {
                normalized.add(address);
            }
        }
        normalized.sort(Comparator.comparingInt(DnsResult::family).thenComparing(InetAddress::getHostAddress));
        return List.copyOf(normalized);
    }

    /**
     * Returns address family order.
     *
     * @param address address
     * @return family order
     */
    private static int family(final InetAddress address) {
        return address instanceof Inet4Address ? 0 : 1;
    }

}
