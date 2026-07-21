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

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable DNS resolution result.
 *
 * @param host       resolved host
 * @param addresses  address snapshot
 * @param resolvedAt resolution time
 * @param ttl        positive DNS ttl or {@link org.miaixz.bus.fabric.Builder#DNS_NO_TTL} when no ttl is available
 * @param duration   resolution duration
 * @author Kimi Liu
 * @since Java 21+
 */
public record DnsResult(String host, List<InetAddress> addresses, Instant resolvedAt, Duration ttl, Duration duration) {

    /**
     * Creates a DNS result.
     */
    public DnsResult {
        host = NetKit.normalizeHost(host, "DNS host");
        addresses = normalizeAddresses(addresses);
        resolvedAt = Assert.notNull(resolvedAt, () -> new ValidateException("DNS resolved time must not be null"));
        ttl = Assert.notNull(ttl, () -> new ValidateException("DNS ttl must be non-null and non-negative"));
        Assert.isTrue(!ttl.isNegative(), () -> new ValidateException("DNS ttl must be non-null and non-negative"));
        duration = Assert
                .notNull(duration, () -> new ValidateException("DNS duration must be non-null and non-negative"));
        Assert.isTrue(
                !duration.isNegative(),
                () -> new ValidateException("DNS duration must be non-null and non-negative"));
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
        return of(host, addresses, Instant.now(), Builder.DNS_NO_TTL, duration);
    }

    /**
     * Creates a DNS result.
     *
     * @param host       host
     * @param addresses  addresses
     * @param resolvedAt resolution time
     * @param ttl        positive ttl or {@link org.miaixz.bus.fabric.Builder#DNS_NO_TTL}
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
     * @return ttl or {@link org.miaixz.bus.fabric.Builder#DNS_NO_TTL}
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
        return !Builder.DNS_NO_TTL.equals(ttl);
    }

    /**
     * Returns a stable, duplicate-free address snapshot.
     *
     * @param addresses addresses
     * @return normalized addresses
     */
    private static List<InetAddress> normalizeAddresses(final List<InetAddress> addresses) {
        final List<InetAddress> checkedAddresses = Assert
                .notNull(addresses, () -> new ValidateException("DNS addresses must not be null"));
        final HashSet<InetAddress> seen = new HashSet<>();
        final ArrayList<InetAddress> normalized = new ArrayList<>(checkedAddresses.size());
        for (final InetAddress address : checkedAddresses) {
            final InetAddress checkedAddress = Assert
                    .notNull(address, () -> new ValidateException("DNS addresses must contain no null elements"));
            if (seen.add(checkedAddress)) {
                normalized.add(checkedAddress);
            }
        }
        return List.copyOf(normalized);
    }

}
