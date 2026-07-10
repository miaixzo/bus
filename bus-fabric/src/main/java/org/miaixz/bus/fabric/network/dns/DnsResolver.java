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
import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tag.Tags;

/**
 * DNS resolver with observer events and system fallback.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsResolver {

    /**
     * Resolver.
     */
    private final Resolver resolver;

    /**
     * Event observer.
     */
    private volatile EventObserver observer;

    /**
     * Creates a DNS resolver.
     *
     * @param resolver resolver
     * @param observer observer
     */
    private DnsResolver(final Resolver resolver, final EventObserver observer) {
        if (resolver == null) {
            throw new ValidateException("Resolver must not be null");
        }
        if (observer == null) {
            throw new ValidateException("Observer must not be null");
        }
        this.resolver = resolver;
        this.observer = EventObserver.safe(observer);
    }

    /**
     * Returns the system DNS resolver.
     *
     * @return system resolver
     */
    public static DnsResolver system() {
        return Instances.get(
                DnsResolver.class.getName() + ".system",
                () -> new DnsResolver(systemResolver(), EventObserver.noop()));
    }

    /**
     * Wraps a resolver.
     *
     * @param resolver resolver
     * @return DNS resolver
     */
    public static DnsResolver of(final Resolver resolver) {
        return new DnsResolver(resolver, EventObserver.noop());
    }

    /**
     * Resolves a host.
     *
     * @param host host
     * @return result
     */
    public DnsResult resolve(final String host) {
        final String normalized = validateHost(host);
        final long started = System.nanoTime();
        emit(ObservationMarker.DNS_START, normalized, null);
        List<InetAddress> addresses;
        try {
            addresses = resolver.resolve(normalized);
        } catch (final RuntimeException e) {
            emit(ObservationMarker.DNS_FAILED, normalized, e);
            throw e instanceof SocketException ? e : new SocketException("Unable to resolve host " + normalized, e);
        }
        final Duration duration = Duration.ofNanos(System.nanoTime() - started);
        final DnsResult result = DnsResult.of(normalized, addresses, duration);
        if (result.empty()) {
            emit(
                    ObservationMarker.DNS_FAILED,
                    normalized,
                    new SocketException("DNS returned no address for " + normalized));
        } else {
            emit(ObservationMarker.DNS_SUCCESS, normalized, null);
        }
        return result;
    }

    /**
     * Sets the event observer.
     *
     * @param observer observer
     */
    public void observer(final EventObserver observer) {
        if (observer == null) {
            throw new ValidateException("DNS observer must not be null");
        }
        this.observer = EventObserver.safe(observer);
    }

    /**
     * Emits an event.
     *
     * @param marker marker
     * @param host   host
     * @param cause  cause
     */
    private void emit(final ObservationMarker marker, final String host, final Throwable cause) {
        observer.emit(FabricEvent.builder(marker).tag(Tags.HOST, host).cause(cause).build());
    }

    /**
     * Returns the shared system resolver contract.
     *
     * @return system resolver contract
     */
    private static Resolver systemResolver() {
        return Instances.get(DnsResolver.class.getName() + ".systemResolver", () -> new Resolver() {
        });
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

}
