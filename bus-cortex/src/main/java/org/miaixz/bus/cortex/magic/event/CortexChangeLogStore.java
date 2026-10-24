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
package org.miaixz.bus.cortex.magic.event;

import java.util.List;

import org.miaixz.bus.core.center.date.Chrono;
import org.miaixz.bus.core.lang.Normal;

/**
 * Reliable change-log store used by registry, setting, and version mutation paths.
 *
 * @author Kimi Liu
 */
public interface CortexChangeLogStore {

    /**
     * Appends one event without changing a previously persisted event or its delivery state. Repeating the same
     * idempotency key must preserve the original record; conflicting event content must be rejected.
     *
     * @param record change-log record
     * @return persisted change-log record
     * @throws IllegalArgumentException if an idempotency key or record identifier is reused for a different event
     */
    CortexChangeRecord append(CortexChangeRecord record);

    /**
     * Marks a change-log record as delivered only while the supplied claim still owns the record.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    CortexChangeRecord markDelivered(CortexChangeRecord claim);

    /**
     * Marks a change-log record as failed only while the supplied claim still owns the record.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @param error failure message
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    CortexChangeRecord markFailed(CortexChangeRecord claim, String error);

    /**
     * Claims pending records with the default owner and lease.
     *
     * @param limit maximum records to claim
     * @return claimed records
     */
    default List<CortexChangeRecord> claimPending(int limit) {
        return claimPending(limit, "cortex", Normal._30 * Chrono.SECOND.getMillis());
    }

    /**
     * Claims pending records for delivery.
     *
     * @param limit       maximum records to claim
     * @param owner       worker owner
     * @param leaseMillis claim lease duration in milliseconds
     * @return claimed records
     */
    List<CortexChangeRecord> claimPending(int limit, String owner, long leaseMillis);

    /**
     * Claims pending records for delivery and filters them by domain and resource type.
     *
     * @param limit        maximum records to claim
     * @param owner        worker owner
     * @param leaseMillis  claim lease duration in milliseconds
     * @param domain       optional domain filter
     * @param resourceType optional resource type filter
     * @return claimed records
     */
    default List<CortexChangeRecord> claimPending(
            int limit,
            String owner,
            long leaseMillis,
            String domain,
            String resourceType) {
        return claimPending(limit, owner, leaseMillis).stream()
                .filter(record -> domain == null || domain.equals(record.getDomain()))
                .filter(record -> resourceType == null || resourceType.equals(record.getResourceType())).toList();
    }

    /**
     * Moves one change-log record to dead letter state only while the supplied claim still owns the record.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @param error terminal failure message
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    CortexChangeRecord deadLetter(CortexChangeRecord claim, String error);

}
