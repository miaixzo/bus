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

import lombok.Getter;
import lombok.Setter;

/**
 * Reliable change-log record used as the first-stage Cortex outbox payload.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class CortexChangeRecord {

    /**
     * Creates an empty change-log record.
     */
    public CortexChangeRecord() {
    }

    /**
     * Unique change-log record identifier.
     */
    private String id;
    /**
     * Idempotency key used to collapse duplicate change-log appends.
     */
    private String idempotencyKey;
    /**
     * Logical domain that produced the change.
     */
    private String domain;
    /**
     * Mutation action name.
     */
    private String action;
    /**
     * Resource type affected by the change.
     */
    private String resourceType;
    /**
     * Resource identifier affected by the change.
     */
    private String resourceId;
    /**
     * Namespace affected by the change.
     */
    private String namespace_id;
    /**
     * Serialized payload for downstream delivery.
     */
    private String payload;
    /**
     * Monotonic or timestamp sequence used for delivery ordering.
     */
    private long sequence;
    /**
     * Current delivery status.
     */
    private CortexChangeStatus status;
    /**
     * Number of failed delivery attempts.
     */
    private int retryCount;
    /**
     * Last delivery error message.
     */
    private String lastError;
    /**
     * Worker identity that currently owns the delivery lease.
     */
    private String claimedBy;
    /**
     * Lease expiration timestamp in epoch milliseconds.
     */
    private long claimedUntil;
    /**
     * Next retry timestamp in epoch milliseconds.
     */
    private long nextRetryAt;
    /**
     * Terminal dead-letter reason.
     */
    private String deadReason;
    /**
     * Creation timestamp in epoch milliseconds.
     */
    private long created;
    /**
     * Last modification timestamp in epoch milliseconds.
     */
    private long modified;

}
