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
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed version store used when no dedicated release store is configured.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheVersionStore implements VersionStore {

    /**
     * Cache-key prefix for version release records.
     */
    private static final String PREFIX = "ver:";

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
                key(prepared.getNamespace_id(), prepared.getTrack(), prepared.getVersion()),
                JsonKit.toJsonString(prepared),
                0L);
        return prepared;
    }

    /**
     * Finds a release record in the cache.
     *
     * @param namespace release namespace
     * @param version   release version
     * @param track     release track
     * @return matching release record or {@code null}
     */
    @Override
    public VersionRecord find(String namespace, String version, String track) {
        Object raw = cacheX.read(key(namespace, track, version));
        return raw instanceof String json ? JsonKit.toPojo(json, VersionRecord.class) : null;
    }

    /**
     * Lists release records from the cache.
     *
     * @param namespace release namespace
     * @param track     release track, or {@code null} to list every track
     * @return matching release records
     */
    @Override
    public List<VersionRecord> list(String namespace, String track) {
        String prefix = track == null || track.isBlank() ? PREFIX + CortexIdentity.namespace(namespace) + ":"
                : PREFIX + CortexIdentity.namespace(namespace) + ":" + ReleaseTrack.normalize(track) + ":";
        Map<String, Object> entries = cacheX.scan(prefix);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<VersionRecord> result = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof String json) {
                VersionRecord record = JsonKit.toPojo(json, VersionRecord.class);
                if (record != null) {
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
     * @param namespace release namespace
     * @param version   release version
     * @param track     release track
     * @return deleted release record or {@code null}
     */
    @Override
    public VersionRecord delete(String namespace, String version, String track) {
        String currentVersion = currentVersion(namespace, track);
        VersionRecord existing = find(namespace, version, track);
        cacheX.remove(key(namespace, track, version));
        if (Objects.equals(currentVersion, version)) {
            cacheX.remove(currentKey(namespace, track));
        }
        return existing;
    }

    /**
     * Returns the current release from the explicit cache pointer when present.
     *
     * @param namespace release namespace
     * @param track     release track
     * @return current release record or {@code null}
     */
    @Override
    public VersionRecord current(String namespace, String track) {
        String version = currentVersion(namespace, track);
        if (version != null && !version.isBlank()) {
            VersionRecord record = find(namespace, version, track);
            if (record != null) {
                return record;
            }
        }
        return VersionStore.super.current(namespace, track);
    }

    /**
     * Updates the current release pointer in the cache.
     *
     * @param namespace release namespace
     * @param track     release track
     * @param version   release version
     * @return persisted current release or {@code null}
     */
    @Override
    public VersionRecord setCurrent(String namespace, String track, String version) {
        String normalizedTrack = ReleaseTrack.normalize(track);
        VersionRecord target = find(namespace, version, normalizedTrack);
        if (target == null) {
            return null;
        }
        VersionRecord previous = current(namespace, normalizedTrack);
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
        cacheX.write(currentKey(namespace, normalizedTrack), saved.getVersion(), 0L);
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
        prepared.setNamespace_id(CortexIdentity.namespace(prepared.getNamespace_id()));
        prepared.setTrack(ReleaseTrack.normalize(prepared.getTrack()));
        if (prepared.getVersionStatus() == null) {
            prepared.setVersionStatus(VersionStatus.ACTIVE);
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
     * @param namespace release namespace
     * @param track     release track
     * @param version   release version
     * @return release cache key
     */
    private String key(String namespace, String track, String version) {
        return PREFIX + CortexIdentity.namespace(namespace) + ":" + ReleaseTrack.normalize(track) + ":" + version;
    }

    /**
     * Builds the cache key that stores the current version pointer.
     *
     * @param namespace release namespace
     * @param track     release track
     * @return current pointer cache key
     */
    private String currentKey(String namespace, String track) {
        return PREFIX + "current:" + CortexIdentity.namespace(namespace) + ":" + ReleaseTrack.normalize(track);
    }

    /**
     * Reads the current version pointer for a namespace and track.
     *
     * @param namespace release namespace
     * @param track     release track
     * @return current version or {@code null}
     */
    private String currentVersion(String namespace, String track) {
        Object value = cacheX.read(currentKey(namespace, track));
        return value == null ? null : value.toString();
    }

}
