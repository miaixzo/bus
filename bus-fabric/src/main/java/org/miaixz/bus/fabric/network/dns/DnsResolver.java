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
import java.util.List;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;

/**
 * DNS resolver with observer events and system fallback.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsResolver {

    /**
     * Shared resolver backend.
     */
    private final Resolver backend;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Creates a DNS resolver.
     *
     * @param backend  resolver backend
     * @param observer observer
     */
    private DnsResolver(final Resolver backend, final EventObserver observer) {
        this.backend = Assert.notNull(backend, () -> new ValidateException("Resolver must not be null"));
        this.observer = EventObserver
                .safe(Assert.notNull(observer, () -> new ValidateException("Observer must not be null")));
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
        return resolve(host, Clock.system());
    }

    /**
     * Resolves a host using the owning Context or Reactor clock.
     *
     * @param host  host
     * @param clock operation clock
     * @return result
     */
    public DnsResult resolve(final String host, final Clock clock) {
        final String normalized = NetKit.normalizeHost(host, "DNS host");
        final Clock operationClock = Assert.notNull(clock, () -> new ValidateException("DNS clock must not be null"));
        final String operationId = ID.objectId();
        final long started = operationClock.nanos();
        emit(ObservationMarker.DNS_START, normalized, null, operationClock, operationId);
        List<InetAddress> addresses;
        try {
            addresses = backend.resolve(normalized);
        } catch (final RuntimeException e) {
            emit(ObservationMarker.DNS_FAILED, normalized, e, operationClock, operationId);
            throw e instanceof SocketException ? e : new SocketException("Unable to resolve host " + normalized, e);
        }
        final Duration duration = Duration.ofNanos(Math.max(0L, operationClock.nanos() - started));
        final DnsResult result = DnsResult
                .of(normalized, addresses, operationClock.now(), Builder.DNS_NO_TTL, duration);
        if (result.empty()) {
            emit(
                    ObservationMarker.DNS_FAILED,
                    normalized,
                    new SocketException("DNS returned no address for " + normalized),
                    operationClock,
                    operationId);
        } else {
            emit(ObservationMarker.DNS_SUCCESS, normalized, null, operationClock, operationId);
        }
        return result;
    }

    /**
     * Returns an observer-isolated wrapper over the same backend.
     *
     * @param observer observer
     * @return resolver wrapper
     */
    public DnsResolver withObserver(final EventObserver observer) {
        return new DnsResolver(backend,
                Assert.notNull(observer, () -> new ValidateException("DNS observer must not be null")));
    }

    /**
     * Emits an event.
     *
     * @param marker      marker
     * @param host        host
     * @param cause       cause
     * @param clock       operation clock
     * @param operationId operation identifier
     */
    private void emit(
            final ObservationMarker marker,
            final String host,
            final Throwable cause,
            final Clock clock,
            final String operationId) {
        observer.emit(
                FabricEvent.builder(marker, clock).tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.HOST, host)
                        .cause(cause).build());
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

}
