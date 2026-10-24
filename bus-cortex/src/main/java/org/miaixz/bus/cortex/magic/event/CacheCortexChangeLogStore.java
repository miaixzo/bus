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

import java.util.*;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.center.date.Chrono;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.registry.RegistryIdentity;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed fallback outbox store.
 *
 * @author Kimi Liu
 */
public class CacheCortexChangeLogStore implements CortexChangeLogStore {

    /**
     * Root cache-key prefix for Cortex outbox data.
     */
    private static final String PREFIX = "outbox:cortex:v2:";

    /**
     * Cache-key prefix for serialized outbox records.
     */
    private static final String RECORD_PREFIX = PREFIX + "record:";

    /**
     * Cache-key prefix for idempotency lookups.
     */
    private static final String IDEMPOTENCY_PREFIX = PREFIX + "idempotency:";

    /**
     * Default number of failed delivery attempts before dead lettering.
     */
    private static final int DEFAULT_MAX_RETRIES = Normal._3;

    /**
     * Default retry backoff in milliseconds.
     */
    private static final long DEFAULT_RETRY_BACKOFF_MS = Chrono.SECOND.getMillis();

    /**
     * Maximum retry backoff in milliseconds.
     */
    private static final long MAX_RETRY_BACKOFF_MS = Chrono.MINUTE.getMillis();

    /**
     * Cache facade used to persist outbox records.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Maximum retry count before a record becomes dead letter.
     */
    private final int maxRetries;

    /**
     * Base retry backoff in milliseconds.
     */
    private final long retryBackoffMs;

    /**
     * Creates a cache-backed change-log store with default retry settings.
     *
     * @param cacheX cache facade
     */
    public CacheCortexChangeLogStore(CacheX<String, Object> cacheX) {
        this(cacheX, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_BACKOFF_MS);
    }

    /**
     * Creates a cache-backed change-log store.
     *
     * @param cacheX         cache facade
     * @param maxRetries     maximum failed delivery attempts before dead lettering
     * @param retryBackoffMs base retry backoff in milliseconds
     */
    public CacheCortexChangeLogStore(CacheX<String, Object> cacheX, int maxRetries, long retryBackoffMs) {
        this.cacheX = cacheX;
        this.maxRetries = Math.max(1, maxRetries);
        this.retryBackoffMs = Math.max(1L, retryBackoffMs);
    }

    /**
     * Appends one event or returns its existing record without changing delivery state.
     *
     * @param record outbox event
     * @return persisted outbox record
     * @throws IllegalArgumentException if an idempotency key or record identifier is reused for a different event
     */
    @Override
    public synchronized CortexChangeRecord append(CortexChangeRecord record) {
        CortexChangeRecord prepared = prepare(record);
        CortexChangeRecord retained = read(prepared.getId());
        if (retained != null && !sameEvent(prepared, retained)) {
            throw new IllegalArgumentException("Outbox record identifier belongs to a different event");
        }
        CortexChangeRecord existing = findByIdempotencyKey(prepared.getTenant_id(), prepared.getIdempotencyKey());
        if (existing != null) {
            if (!sameEvent(prepared, existing)) {
                throw new IllegalArgumentException("Outbox event identity conflicts with an existing record");
            }
            return existing;
        }
        if (retained != null) {
            cacheX.write(idempotencyKey(retained.getTenant_id(), retained.getIdempotencyKey()), retained.getId(), 0L);
            return retained;
        }
        if (prepared.getStatus() != CortexChangeStatus.CLAIMED) {
            prepared.setClaimedBy(null);
            prepared.setClaimedUntil(0L);
        }
        write(prepared);
        cacheX.write(idempotencyKey(prepared.getTenant_id(), prepared.getIdempotencyKey()), prepared.getId(), 0L);
        return prepared;
    }

    /**
     * Marks a record as delivered when the supplied claim still owns delivery.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    @Override
    public synchronized CortexChangeRecord markDelivered(CortexChangeRecord claim) {
        return update(claim, CortexChangeStatus.DELIVERED, null, false, false);
    }

    /**
     * Marks an owned record as failed and schedules a retry when retries remain.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @param error failure message
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    @Override
    public synchronized CortexChangeRecord markFailed(CortexChangeRecord claim, String error) {
        return update(claim, CortexChangeStatus.FAILED, error, true, false);
    }

    /**
     * Claims pending records without domain filtering.
     *
     * @param limit       maximum records to claim
     * @param owner       worker owner name
     * @param leaseMillis claim lease duration in milliseconds
     * @return claimed records
     */
    @Override
    public synchronized List<CortexChangeRecord> claimPending(int limit, String owner, long leaseMillis) {
        return claimPending(limit, owner, leaseMillis, null, null);
    }

    /**
     * Claims pending records for a domain and resource type.
     *
     * @param limit        maximum records to claim
     * @param owner        worker owner name
     * @param leaseMillis  claim lease duration in milliseconds
     * @param domain       optional domain filter
     * @param resourceType optional resource type filter
     * @return claimed records
     */
    @Override
    public synchronized List<CortexChangeRecord> claimPending(
            int limit,
            String owner,
            long leaseMillis,
            String domain,
            String resourceType) {
        long now = DateKit.current();
        Map<String, Object> entries = cacheX.scan(RECORD_PREFIX);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<CortexChangeRecord> records = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof String json) {
                CortexChangeRecord record = JsonKit.toPojo(json, CortexChangeRecord.class);
                if (record != null && matches(record, domain, resourceType) && claimable(record, now)) {
                    records.add(record);
                }
            }
        }
        records.sort(Comparator.comparingLong(CortexChangeRecord::getCreated));
        int size = limit > 0 ? Math.min(limit, records.size()) : records.size();
        List<CortexChangeRecord> claimed = new ArrayList<>(size);
        for (CortexChangeRecord record : records.subList(0, size)) {
            CortexChangeRecord current = read(record.getId());
            if (current == null || !claimable(current, now)) {
                continue;
            }
            current.setStatus(CortexChangeStatus.CLAIMED);
            current.setClaimedBy(owner == null || owner.isBlank() ? "cortex" : owner);
            current.setClaimedUntil(now + Math.max(leaseMillis, 1L));
            current.setModified(now);
            write(current);
            claimed.add(current);
        }
        return claimed;
    }

    /**
     * Moves an owned record into dead letter state.
     *
     * @param claim unmodified snapshot returned by {@code claimPending}
     * @param error terminal failure message
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    @Override
    public synchronized CortexChangeRecord deadLetter(CortexChangeRecord claim, String error) {
        return update(claim, CortexChangeStatus.DEAD, error, true, true);
    }

    /**
     * Updates delivery state only when the current persisted claim matches the delivery snapshot.
     *
     * @param claim          unmodified snapshot returned by {@code claimPending}
     * @param status         target delivery status
     * @param error          failure message
     * @param incrementRetry whether to increase the retry count
     * @param deadLetter     whether to set the dead-letter reason
     * @return updated record, or {@code null} when the record is absent or the claim no longer matches
     */
    private CortexChangeRecord update(
            CortexChangeRecord claim,
            CortexChangeStatus status,
            String error,
            boolean incrementRetry,
            boolean deadLetter) {
        if (claim == null || claim.getId() == null || claim.getStatus() != CortexChangeStatus.CLAIMED
                || claim.getClaimedBy() == null || claim.getClaimedBy().isBlank() || claim.getClaimedUntil() <= 0L) {
            return null;
        }
        CortexChangeRecord record = read(claim.getId());
        if (record == null || record.getStatus() != CortexChangeStatus.CLAIMED || !sameEvent(claim, record)
                || !Objects.equals(claim.getClaimedBy(), record.getClaimedBy())
                || claim.getClaimedUntil() != record.getClaimedUntil()
                || claim.getRetryCount() != record.getRetryCount()) {
            return null;
        }
        long now = DateKit.current();
        if (incrementRetry) {
            record.setRetryCount(record.getRetryCount() + 1);
        }
        CortexChangeStatus nextStatus = status;
        if (status == CortexChangeStatus.FAILED && record.getRetryCount() >= maxRetries) {
            nextStatus = CortexChangeStatus.DEAD;
            deadLetter = true;
        }
        record.setStatus(nextStatus);
        record.setLastError(error);
        record.setModified(now);
        record.setClaimedBy(null);
        record.setClaimedUntil(0L);
        if (nextStatus == CortexChangeStatus.FAILED) {
            record.setNextRetryAt(now + retryDelay(record.getRetryCount()));
        } else {
            record.setNextRetryAt(0L);
        }
        if (deadLetter || nextStatus == CortexChangeStatus.DEAD) {
            record.setDeadReason(error);
        }
        write(record);
        return record;
    }

    /**
     * Compares immutable event coordinates and payload, excluding delivery state and the generated record identifier.
     *
     * @param left  incoming event or delivery claim
     * @param right persisted event
     * @return {@code true} when both records represent the same event
     */
    private boolean sameEvent(CortexChangeRecord left, CortexChangeRecord right) {
        return Objects.equals(left.getTenant_id(), right.getTenant_id())
                && Objects.equals(left.getDomain(), right.getDomain())
                && Objects.equals(left.getIdempotencyKey(), right.getIdempotencyKey())
                && Objects.equals(left.getAction(), right.getAction())
                && Objects.equals(left.getResourceType(), right.getResourceType())
                && Objects.equals(left.getResourceId(), right.getResourceId())
                && left.getSequence() == right.getSequence() && Objects.equals(left.getPayload(), right.getPayload());
    }

    /**
     * Normalizes a record before append.
     *
     * @param record raw outbox record
     * @return prepared outbox record
     */
    private CortexChangeRecord prepare(CortexChangeRecord record) {
        CortexChangeRecord prepared = record == null ? new CortexChangeRecord() : record;
        prepared.setTenant_id(
                "registry".equals(prepared.getDomain())
                        ? RegistryIdentity.tenantId(Type.from(prepared.getResourceType()), prepared.getTenant_id())
                        : "bridge".equals(prepared.getDomain())
                                ? RegistryIdentity.tenantId(Type.API, prepared.getTenant_id())
                                : CortexIdentity.tenantId(prepared.getTenant_id()));
        long now = DateKit.current();
        if (prepared.getId() == null || prepared.getId().isBlank()) {
            prepared.setId(ID.fastSimpleUUID());
        }
        if (prepared.getIdempotencyKey() == null || prepared.getIdempotencyKey().isBlank()) {
            prepared.setIdempotencyKey(prepared.getId());
        }
        if (prepared.getStatus() == null) {
            prepared.setStatus(CortexChangeStatus.PENDING);
        }
        if (prepared.getCreated() <= 0L) {
            prepared.setCreated(now);
        }
        prepared.setModified(now);
        return prepared;
    }

    /**
     * Finds a record by idempotency key.
     *
     * @param tenantId       the actual tenant scope, or null for the global registry partition
     * @param idempotencyKey idempotency key
     * @return existing record or {@code null}
     */
    private CortexChangeRecord findByIdempotencyKey(String tenantId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        Object rawId = cacheX.read(idempotencyKey(tenantId, idempotencyKey));
        return rawId instanceof String id ? read(id) : null;
    }

    /**
     * Reads one outbox record by identifier.
     *
     * @param id outbox record identifier
     * @return outbox record or {@code null}
     */
    private CortexChangeRecord read(String id) {
        Object raw = cacheX.read(key(id));
        if (!(raw instanceof String json)) {
            return null;
        }
        return JsonKit.toPojo(json, CortexChangeRecord.class);
    }

    /**
     * Writes one outbox record.
     *
     * @param record outbox record
     */
    private void write(CortexChangeRecord record) {
        cacheX.write(key(record.getId()), JsonKit.toJsonString(record), 0L);
    }

    /**
     * Returns whether the record can be claimed at the supplied time.
     *
     * @param record outbox record
     * @param now    current timestamp in epoch milliseconds
     * @return {@code true} when the record can be claimed
     */
    private boolean claimable(CortexChangeRecord record, long now) {
        CortexChangeStatus status = record.getStatus() == null ? CortexChangeStatus.PENDING : record.getStatus();
        return switch (status) {
            case PENDING -> true;
            case FAILED -> record.getNextRetryAt() <= 0L || record.getNextRetryAt() <= now;
            case CLAIMED -> record.getClaimedUntil() > 0L && record.getClaimedUntil() <= now;
            case DELIVERED, DEAD -> false;
        };
    }

    /**
     * Returns whether a record matches the optional domain filters.
     *
     * @param record       outbox record
     * @param domain       optional domain filter
     * @param resourceType optional resource type filter
     * @return {@code true} when the record matches
     */
    private boolean matches(CortexChangeRecord record, String domain, String resourceType) {
        return (domain == null || domain.equals(record.getDomain()))
                && (resourceType == null || resourceType.equals(record.getResourceType()));
    }

    /**
     * Calculates exponential retry delay.
     *
     * @param retryCount failed retry count
     * @return retry delay in milliseconds
     */
    private long retryDelay(int retryCount) {
        int exponent = Math.max(0, retryCount - 1);
        long multiplier = 1L << Math.min(exponent, 10);
        return Math.min(MAX_RETRY_BACKOFF_MS, retryBackoffMs * multiplier);
    }

    /**
     * Builds the record cache key.
     *
     * @param id outbox record identifier
     * @return record cache key
     */
    private String key(String id) {
        return RECORD_PREFIX + id;
    }

    /**
     * Builds the idempotency cache key.
     *
     * @param tenantId       the actual tenant scope, or null for the global registry partition
     * @param idempotencyKey idempotency key
     * @return idempotency cache key
     */
    private String idempotencyKey(String tenantId, String idempotencyKey) {
        return IDEMPOTENCY_PREFIX + (tenantId == null ? "registry" : CortexIdentity.tenantKey(tenantId)) + Symbol.COLON
                + idempotencyKey;
    }

}
