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
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;

/**
 * Host resolver contract with a default JDK DNS implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Resolver {

    /**
     * Resolves a host to addresses.
     *
     * @param host host
     * @return immutable address list
     */
    default List<InetAddress> resolve(final String host) {
        final String validHost = NetKit.normalizeHost(host, "Resolver host");
        try {
            return List.of(InetAddress.getAllByName(validHost));
        } catch (final UnknownHostException e) {
            throw new SocketException("Unable to resolve host " + validHost, e);
        }
    }

    /**
     * Resolves one immutable result with explicit no-TTL metadata for legacy resolver implementations.
     *
     * <p>
     * This compatibility method invokes {@link #resolve(String)} exactly once. Caching, asynchronous execution, waiter
     * cancellation, and monotonic expiry remain responsibilities of {@link DnsResolver}.
     * </p>
     *
     * @param host  host
     * @param clock operation clock
     * @return immutable resolution result
     */
    default DnsResult resolveResult(final String host, final Clock clock) {
        final String normalized = NetKit.normalizeHost(host, "Resolver host");
        final Clock operationClock = Assert
                .notNull(clock, () -> new ValidateException("Resolver clock must not be null"));
        final long started = operationClock.nanos();
        final List<InetAddress> addresses = resolve(normalized);
        if (addresses == null || addresses.isEmpty()) {
            throw new SocketException("DNS returned no address for " + normalized);
        }
        for (final InetAddress address : addresses) {
            if (address == null) {
                throw new SocketException("DNS returned a null address for " + normalized);
            }
        }
        final long elapsed = Math.max(0L, operationClock.nanos() - started);
        return DnsResult.of(normalized, addresses, operationClock.now(), Builder.DNS_NO_TTL, Duration.ofNanos(elapsed));
    }

    /**
     * Returns whether this resolver supports a host.
     *
     * @param host host
     * @return true when supported
     */
    default boolean supports(final String host) {
        NetKit.normalizeHost(host, "Resolver host");
        return true;
    }

}
