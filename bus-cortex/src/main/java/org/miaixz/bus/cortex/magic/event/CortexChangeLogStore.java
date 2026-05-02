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

/**
 * Reliable change-log store used by registry, setting, and version mutation paths.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface CortexChangeLogStore {

    /**
     * Appends one change-log record.
     *
     * @param record change-log record
     * @return persisted change-log record
     */
    CortexChangeRecord append(CortexChangeRecord record);

    /**
     * Marks a change-log record as delivered.
     *
     * @param id change-log record identifier
     * @return updated record or {@code null}
     */
    CortexChangeRecord markDelivered(String id);

    /**
     * Marks a change-log record as failed.
     *
     * @param id    change-log record identifier
     * @param error failure message
     * @return updated record or {@code null}
     */
    CortexChangeRecord markFailed(String id, String error);

    /**
     * Claims pending records with the default owner and lease.
     *
     * @param limit maximum records to claim
     * @return claimed records
     */
    default List<CortexChangeRecord> claimPending(int limit) {
        return claimPending(limit, "cortex", 30000L);
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
     * Moves one change-log record to dead letter state.
     *
     * @param id    change-log record identifier
     * @param error terminal failure message
     * @return updated record or {@code null}
     */
    CortexChangeRecord deadLetter(String id, String error);

}
