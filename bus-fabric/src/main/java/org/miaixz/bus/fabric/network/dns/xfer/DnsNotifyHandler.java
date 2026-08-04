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
package org.miaixz.bus.fabric.network.dns.xfer;

import java.net.InetAddress;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.resolve.RuntimeIndex;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;

/**
 * DNS NOTIFY handler that validates a NOTIFY request and triggers a controlled snapshot refresh.
 *
 * <p>
 * Instances are thread-safe. The handler stores immutable ACL configuration and delegates the actual snapshot loading,
 * validation, listener notification, and rollback behavior to the supplied refresh action owned by {@code DnsServer}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsNotifyHandler {

    /**
     * Client CIDR blocks allowed to submit DNS NOTIFY messages.
     */
    private final List<CidrBlock> allowedCidrs;

    /**
     * Creates a NOTIFY handler.
     *
     * @param allowedCidrs client CIDR blocks allowed to submit DNS NOTIFY messages
     */
    public DnsNotifyHandler(final List<CidrBlock> allowedCidrs) {
        this.allowedCidrs = immutableCidrs(allowedCidrs);
    }

    /**
     * Handles one DNS NOTIFY query.
     *
     * @param current       active runtime index before refresh
     * @param query         decoded NOTIFY query
     * @param clientAddress client address, or {@code null} when unavailable
     * @param refreshAction snapshot refresh action supplied by the server
     * @return DNS response model
     */
    public DnsResponse handle(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress,
            final RefreshAction refreshAction) {
        if (current == null) {
            throw new ValidateException("DNS runtime index must not be null");
        }
        if (query == null) {
            throw new ValidateException("DNS NOTIFY query must not be null");
        }
        if (refreshAction == null) {
            throw new ValidateException("DNS NOTIFY refresh action must not be null");
        }
        if (query.opcode() != DnsQuery.OPCODE_NOTIFY || !allowed(clientAddress)
                || !knownZone(current, query, clientAddress)) {
            return DnsResponse.empty(query, DnsResponseCode.REFUSED, false);
        }
        try {
            refreshAction.refresh();
            return DnsResponse.empty(query, DnsResponseCode.NOERROR, false);
        } catch (final RuntimeException e) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
    }

    /**
     * Returns whether the NOTIFY zone exists in the active index.
     *
     * @param current       active runtime index before refresh
     * @param query         decoded NOTIFY query
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the NOTIFY zone is known
     */
    private static boolean knownZone(
            final RuntimeIndex current,
            final DnsQuery query,
            final InetAddress clientAddress) {
        final DnsZone zone = current.findZone(query.question().name(), clientAddress);
        return zone != null && zone.origin().equals(query.question().name());
    }

    /**
     * Returns whether a client address is allowed to submit NOTIFY.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the client is allowed
     */
    private boolean allowed(final InetAddress clientAddress) {
        if (clientAddress == null) {
            return false;
        }
        for (final CidrBlock cidr : allowedCidrs) {
            if (cidr.contains(clientAddress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates and copies CIDR blocks.
     *
     * @param cidrs source CIDR blocks
     * @return immutable CIDR blocks
     */
    private static List<CidrBlock> immutableCidrs(final List<CidrBlock> cidrs) {
        if (cidrs == null) {
            throw new ValidateException("DNS NOTIFY ACL CIDRs must not be null");
        }
        for (final CidrBlock cidr : cidrs) {
            if (cidr == null) {
                throw new ValidateException("DNS NOTIFY ACL CIDRs must not contain null");
            }
        }
        return List.copyOf(cidrs);
    }

    /**
     * Snapshot refresh operation invoked after NOTIFY validation succeeds.
     *
     * @since Java 21+
     */
    @FunctionalInterface
    public interface RefreshAction {

        /**
         * Loads, validates, and installs a fresh snapshot.
         */
        void refresh();

    }

}
