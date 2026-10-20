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
package org.miaixz.bus.cortex.release;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed version store used when no dedicated release store is configured.
 *
 * @author Kimi Liu
 */
public class CacheReleaseStore implements ReleaseStore {

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
    public CacheReleaseStore(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Saves or replaces a release record in the cache.
     *
     * @param record release record to persist
     * @return persisted release record
     */
    @Override
    public Release save(Release record) {
        Release prepared = normalize(record);
        cacheX.write(
                key(prepared.getTenant_id(), prepared.getTrack(), prepared.getVersion()),
                JsonKit.toJsonString(prepared),
                0L);
        return prepared;
    }

    /**
     * Atomically inserts within this shared cache object; distributed hosts must supply a transactional store.
     */
    @Override
    public Release create(Release record) {
        synchronized (cacheX) {
            Release prepared = normalize(record);
            if (find(prepared.getTenant_id(), prepared.getVersion(), prepared.getTrack()) != null) {
                throw new IllegalStateException("Release version already exists");
            }
            return save(prepared);
        }
    }

    /**
     * Finds a release record in the cache.
     *
     * @param tenantId release tenantId
     * @param version  release version
     * @param track    release track
     * @return matching release record or {@code null}
     */
    @Override
    public Release find(String tenantId, String version, String track) {
        Object raw = cacheX.read(key(tenantId, track, version));
        Release record = raw instanceof String json ? JsonKit.toPojo(json, Release.class) : null;
        if (record != null) {
            CortexIdentity.requireTenant(tenantId, record.getTenant_id());
        }
        return record;
    }

    /**
     * Lists release records from the cache.
     *
     * @param tenantId release tenantId
     * @param track    release track, or {@code null} to list every track
     * @return matching release records
     */
    @Override
    public List<Release> list(String tenantId, String track) {
        String prefix = track == null || track.isBlank() ? PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON
                : PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON + segment(EnumValue.Track.normalize(track))
                        + Symbol.COLON;
        Map<String, Object> entries = cacheX.scan(prefix);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Release> result = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof String json) {
                Release record = JsonKit.toPojo(json, Release.class);
                if (record != null) {
                    CortexIdentity.requireTenant(tenantId, record.getTenant_id());
                    result.add(record);
                }
            }
        }
        result.sort(ReleaseStore.currentComparator().reversed());
        return result;
    }

    /**
     * Deletes a release record from the cache.
     *
     * @param tenantId release tenantId
     * @param version  release version
     * @param track    release track
     * @return deleted release record or {@code null}
     */
    @Override
    public Release delete(String tenantId, String version, String track) {
        String currentVersion = currentVersion(tenantId, track);
        Release existing = find(tenantId, version, track);
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
     * @param track    release track
     * @return current release record or {@code null}
     */
    @Override
    public Release current(String tenantId, String track) {
        String version = currentVersion(tenantId, track);
        if (version != null && !version.isBlank()) {
            Release record = find(tenantId, version, track);
            if (record != null) {
                return record;
            }
        }
        return ReleaseStore.super.current(tenantId, track);
    }

    /**
     * Updates the current release pointer in the cache.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @return persisted current release or {@code null}
     */
    @Override
    public Release setCurrent(String tenantId, String track, String version) {
        String normalizedTrack = EnumValue.Track.normalize(track);
        Release target = find(tenantId, version, normalizedTrack);
        if (target == null) {
            return null;
        }
        Release previous = current(tenantId, normalizedTrack);
        if (previous != null && !Objects.equals(previous.getVersion(), target.getVersion())
                && EnumValue.Phase.ACTIVE.matches(previous.getStatus())) {
            previous.setStatus(EnumValue.Phase.DEPRECATED.key());
            save(previous);
        }
        target.setTrack(normalizedTrack);
        target.setStatus(EnumValue.Phase.ACTIVE.key());
        if (target.getPublished() == null) {
            target.setPublished(System.currentTimeMillis());
        }
        Release saved = save(target);
        cacheX.write(currentKey(tenantId, normalizedTrack), saved.getVersion(), 0L);
        return saved;
    }

    /**
     * Normalizes required release fields before persistence.
     *
     * @param record raw release record
     * @return normalized release record
     */
    private Release normalize(Release record) {
        Release prepared = record == null ? new Release() : record;
        prepared.setTenant_id(CortexIdentity.tenantId(prepared.getTenant_id()));
        prepared.setTrack(EnumValue.Track.normalize(prepared.getTrack()));
        segment(prepared.getVersion());
        if (prepared.getStatus() == null) {
            prepared.setStatus(EnumValue.Phase.DRAFT.key());
        }
        EnumValue.Phase.fromKey(prepared.getStatus());
        long now = System.currentTimeMillis();
        if (prepared.getCreated() == null) {
            prepared.setCreated(now);
        }
        return prepared;
    }

    /**
     * Builds the cache key for one release record.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @param version  release version
     * @return release cache key
     */
    private String key(String tenantId, String track, String version) {
        return PREFIX + CortexIdentity.tenantKey(tenantId) + Symbol.COLON + segment(EnumValue.Track.normalize(track))
                + Symbol.COLON + segment(version);
    }

    /**
     * Builds the cache key that stores the current version pointer.
     *
     * @param tenantId release tenantId
     * @param track    release track
     * @return current pointer cache key
     */
    private String currentKey(String tenantId, String track) {
        return PREFIX + "current:" + CortexIdentity.tenantKey(tenantId) + Symbol.COLON
                + segment(EnumValue.Track.normalize(track));
    }

    /**
     * Encodes one required coordinate without delimiter collisions.
     */
    private String segment(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Version coordinate is required");
        }
        return value.length() + ":" + value;
    }

    /**
     * Reads the tenant-scoped current version pointer.
     */
    private String currentVersion(String tenantId, String track) {
        Object value = cacheX.read(currentKey(tenantId, track));
        return value == null ? null : value.toString();
    }

}
