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
package org.miaixz.bus.cortex.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed version store used when no dedicated release store is configured.
 *
 * @author Kimi Liu
 */
public class CacheVersionStore implements VersionStore {

    /**
     * Cache-key prefix for version release records.
     */
    private static final String PREFIX = "ver:v2:";

    /**
     * Cache facade used as the backing persistence surface.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a cache-backed version store.
     *
     * @param cacheX cache facade
     */
    public CacheVersionStore(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Saves or replaces a release record in the cache.
     *
     * @param record release record to persist
     * @return persisted release record
     */
    @Override
    public VersionRecord save(VersionRecord record) {
        VersionRecord prepared = normalize(record);
        cacheX.write(
                key(prepared.getTenant_id(), prepared.getTrack(), prepared.getVersion()),
                JsonKit.toJsonString(prepared),
                0L);
        return prepared;
    }

    /** Atomically inserts within this shared cache object; distributed hosts must supply a transactional store. */
    @Override
    public VersionRecord create(VersionRecord record) {
        synchronized (cacheX) {
            VersionRecord prepared = normalize(record);
            if (find(prepared.getTenant_id(), prepared.getVersion(), prepared.getTrack()) != null) {
                throw new IllegalStateException("Release version already exists");
            }
            return save(prepared);
        }
    }

    /**
     * Finds a release record in the cache.
     *
     * @param tenantId   release tenantId
     * @param version release version
     * @param track   release track
     * @return matching release record or {@code null}
     */
    @Override
    public VersionRecord find(String tenantId, String version, String track) {
        Object raw = cacheX.read(key(tenantId, track, version));
        VersionRecord record = raw instanceof String json ? JsonKit.toPojo(json, VersionRecord.class) : null;
        if (record != null) {
            CortexIdentity.requireTenant(tenantId, record.getTenant_id());
        }
        return record;
    }

    /**
     * Lists release records from the cache.
     *
     * @param tenantId release tenantId
     * @param track release track, or {@code null} to list every track
     * @return matching release records
     */
    @Override
    public List<VersionRecord> list(String tenantId, String track) {
        String prefix = track == null || track.isBlank() ? PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON
                : PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON + segment(ReleaseTrack.normalize(track)) + Symbol.COLON;
        Map<String, Object> entries = cacheX.scan(prefix);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<VersionRecord> result = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof String json) {
                VersionRecord record = JsonKit.toPojo(json, VersionRecord.class);
                if (record != null) {
                    CortexIdentity.requireTenant(tenantId, record.getTenant_id());
                    result.add(record);
                }
            }
        }
        result.sort(VersionStore.currentComparator().reversed());
        return result;
    }

    /**
     * Deletes a release record from the cache.
     *
     * @param tenantId   release tenantId
     * @param version release version
     * @param track   release track
     * @return deleted release record or {@code null}
     */
    @Override
    public VersionRecord delete(String tenantId, String version, String track) {
        String currentVersion = currentVersion(tenantId, track);
        VersionRecord existing = find(tenantId, version, track);
        cacheX.remove(key(tenantId, track, version));
        if (Objects.equals(currentVersion, version)) {
            cacheX.remove(currentKey(tenantId, track));
        }
        return existing;
    }

    /**
     * Returns the current release from the explicit cache pointer when present.
     *
     * @param tenantId release tenantId
     * @param track release track
     * @return current release record or {@code null}
     */
    @Override
    public VersionRecord current(String tenantId, String track) {
        String version = currentVersion(tenantId, track);
        if (version != null && !version.isBlank()) {
            VersionRecord record = find(tenantId, version, track);
            if (record != null) {
                return record;
            }
        }
        return VersionStore.super.current(tenantId, track);
    }

    /**
     * Updates the current release pointer in the cache.
     *
     * @param tenantId   release tenantId
     * @param track   release track
     * @param version release version
     * @return persisted current release or {@code null}
     */
    @Override
    public VersionRecord setCurrent(String tenantId, String track, String version) {
        String normalizedTrack = ReleaseTrack.normalize(track);
        VersionRecord target = find(tenantId, version, normalizedTrack);
        if (target == null) {
            return null;
        }
        VersionRecord previous = current(tenantId, normalizedTrack);
        if (previous != null && !Objects.equals(previous.getVersion(), target.getVersion())
                && previous.getVersionStatus() == VersionStatus.ACTIVE) {
            previous.setVersionStatus(VersionStatus.DEPRECATED);
            save(previous);
        }
        target.setTrack(normalizedTrack);
        target.setVersionStatus(VersionStatus.ACTIVE);
        if (target.getPublished() == null) {
            target.setPublished(System.currentTimeMillis());
        }
        VersionRecord saved = save(target);
        cacheX.write(currentKey(tenantId, normalizedTrack), saved.getVersion(), 0L);
        return saved;
    }

    /**
     * Normalizes required release fields before persistence.
     *
     * @param record raw release record
     * @return normalized release record
     */
    private VersionRecord normalize(VersionRecord record) {
        VersionRecord prepared = record == null ? new VersionRecord() : record;
        prepared.setTenant_id(CortexIdentity.tenantId(prepared.getTenant_id()));
        prepared.setTrack(ReleaseTrack.normalize(prepared.getTrack()));
        segment(prepared.getVersion());
        if (prepared.getVersionStatus() == null) {
            prepared.setVersionStatus(VersionStatus.DRAFT);
        }
        long now = System.currentTimeMillis();
        if (prepared.getCreated() == null) {
            prepared.setCreated(now);
        }
        return prepared;
    }

    /**
     * Builds the cache key for one release record.
     *
     * @param tenantId   release tenantId
     * @param track   release track
     * @param version release version
     * @return release cache key
     */
    private String key(String tenantId, String track, String version) {
        return PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON + segment(ReleaseTrack.normalize(track)) + Symbol.COLON
                + segment(version);
    }

    /**
     * Builds the cache key that stores the current version pointer.
     *
     * @param tenantId release tenantId
     * @param track release track
     * @return current pointer cache key
     */
    private String currentKey(String tenantId, String track) {
        return PREFIX + "current:" + CortexIdentity.tenantKey(tenantId) + Symbol.COLON + segment(ReleaseTrack.normalize(track));
    }

    /** Encodes one required coordinate without delimiter collisions. */
    private String segment(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Version coordinate is required");
        }
        return value.length() + ":" + value;
    }

    /** Reads the tenant-scoped current version pointer. */
    private String currentVersion(String tenantId, String track) {
        Object value = cacheX.read(currentKey(tenantId, track));
        return value == null ? null : value.toString();
    }

}
