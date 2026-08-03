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
package org.miaixz.bus.fabric.network.dns.update;

import java.net.InetAddress;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.provider.DnsDynamicUpdateSink;
import org.miaixz.bus.fabric.network.dns.provider.DnsUpdateCommand;
import org.miaixz.bus.fabric.network.dns.provider.DnsUpdateResult;
import org.miaixz.bus.fabric.network.dns.resolve.RuntimeIndex;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;

/**
 * Dynamic Update control-path handler that delegates persistence to an external update sink.
 *
 * <p>
 * Instances are thread-safe when the configured sink is thread-safe. The handler never persists update data inside
 * {@code bus-fabric}; accepted changes become visible only after the supplied snapshot installer accepts a replacement
 * snapshot.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsDynamicUpdateHandler {

    /**
     * External Dynamic Update sink, or {@code null} when updates are disabled.
     */
    private final DnsDynamicUpdateSink sink;

    /**
     * Creates a Dynamic Update handler.
     *
     * @param sink external Dynamic Update sink, or {@code null} when updates are disabled
     */
    public DnsDynamicUpdateHandler(final DnsDynamicUpdateSink sink) {
        this.sink = sink;
    }

    /**
     * Handles a DNS Dynamic Update request.
     *
     * @param current       active runtime index
     * @param query         decoded DNS update query
     * @param request       original update wire message
     * @param clientAddress client address, or {@code null} when unavailable
     * @param installer     accepted-snapshot installer supplied by the DNS server
     * @return DNS update response model
     */
    public DnsResponse handle(
            final RuntimeIndex current,
            final DnsQuery query,
            final byte[] request,
            final InetAddress clientAddress,
            final SnapshotInstaller installer) {
        if (current == null) {
            throw new ValidateException("DNS runtime index must not be null");
        }
        if (query == null) {
            throw new ValidateException("DNS update query must not be null");
        }
        if (installer == null) {
            throw new ValidateException("DNS update snapshot installer must not be null");
        }
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        if (sink == null || query.opcode() != DnsQuery.OPCODE_UPDATE || clientAddress == null
                || !zoneMatchesUpdateQuestion(zone, query)) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        try {
            return applyResult(
                    query,
                    sink.submit(new DnsUpdateCommand(query.question().name(), clientAddress, request)),
                    installer);
        } catch (final RuntimeException e) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
    }

    /**
     * Returns whether the update zone section exactly targets the selected authoritative zone.
     *
     * @param zone  selected zone, or {@code null} when no zone matched
     * @param query decoded DNS update query
     * @return {@code true} when the update zone equals the selected zone origin
     */
    private static boolean zoneMatchesUpdateQuestion(final DnsZone zone, final DnsQuery query) {
        return zone != null && zone.origin().equals(query.question().name());
    }

    /**
     * Applies an external sink result.
     *
     * @param query     decoded DNS update query
     * @param result    sink result
     * @param installer accepted-snapshot installer supplied by the DNS server
     * @return DNS update response model
     */
    private static DnsResponse applyResult(
            final DnsQuery query,
            final DnsUpdateResult result,
            final SnapshotInstaller installer) {
        if (result == null) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
        if (result.responseCode() == DnsUpdateResult.REFUSED) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        final DnsSnapshot snapshot = result.snapshot();
        if (snapshot != null) {
            installer.install(snapshot);
        }
        return DnsResponse.empty(query, DnsResponseCode.fromCode(result.responseCode()), false);
    }

    /**
     * Snapshot installation operation invoked after an update sink accepts a replacement snapshot.
     *
     * @since Java 21+
     */
    @FunctionalInterface
    public interface SnapshotInstaller {

        /**
         * Validates and installs a replacement snapshot.
         *
         * @param snapshot replacement snapshot returned by the external update sink
         */
        void install(DnsSnapshot snapshot);

    }

}
