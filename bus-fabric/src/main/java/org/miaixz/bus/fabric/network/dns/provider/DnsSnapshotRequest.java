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
package org.miaixz.bus.fabric.network.dns.provider;

import java.time.Instant;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;

/**
 * Snapshot load request passed to an external DNS snapshot provider outside the query hot path.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsSnapshotRequest {

    /**
     * Active snapshot visible before the load request, or {@code null} during startup.
     */
    private final DnsSnapshot activeSnapshot;

    /**
     * Request creation time.
     */
    private final Instant requestedAt;

    /**
     * Creates a snapshot load request.
     *
     * @param activeSnapshot active snapshot visible before the request, or {@code null} during startup
     * @param requestedAt    request creation time
     */
    public DnsSnapshotRequest(final DnsSnapshot activeSnapshot, final Instant requestedAt) {
        if (requestedAt == null) {
            throw new ValidateException("DNS snapshot request time must not be null");
        }
        this.activeSnapshot = activeSnapshot;
        this.requestedAt = requestedAt;
    }

    /**
     * Creates a startup load request.
     *
     * @return startup load request
     */
    public static DnsSnapshotRequest startup() {
        return new DnsSnapshotRequest(null, Instant.now());
    }

    /**
     * Creates a refresh load request.
     *
     * @param activeSnapshot active snapshot visible before the refresh
     * @return refresh load request
     */
    public static DnsSnapshotRequest refresh(final DnsSnapshot activeSnapshot) {
        if (activeSnapshot == null) {
            throw new ValidateException("DNS active snapshot must not be null for refresh requests");
        }
        return new DnsSnapshotRequest(activeSnapshot, Instant.now());
    }

    /**
     * Returns the active snapshot visible before the load request.
     *
     * @return active snapshot, or {@code null} during startup
     */
    public DnsSnapshot activeSnapshot() {
        return activeSnapshot;
    }

    /**
     * Returns whether this request was created for startup.
     *
     * @return true when no active snapshot existed before the request
     */
    public boolean startupRequest() {
        return activeSnapshot == null;
    }

    /**
     * Returns the request creation time.
     *
     * @return request creation time
     */
    public Instant requestedAt() {
        return requestedAt;
    }

}
