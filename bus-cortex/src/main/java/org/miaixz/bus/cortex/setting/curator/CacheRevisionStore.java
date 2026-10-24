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
package org.miaixz.bus.cortex.setting.curator;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Protected Revision cache; authoritative writes and transactions remain in the host.
 *
 * @author Kimi Liu
 */
public final class CacheRevisionStore implements RevisionStore {

    /**
     * Protected serialized cache only.
     */
    private final CacheX<String, Object> cache;
    /**
     * Authoritative entity store.
     */
    private final RevisionStore delegate;

    /**
     * Creates a bounded store decorator.
     *
     * @param cache    protected cache
     * @param delegate authoritative entity source
     */
    public CacheRevisionStore(CacheX<String, Object> cache, RevisionStore delegate) {
        this.cache = Objects.requireNonNull(cache);
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * Reads exact tenant-qualified content.
     *
     * @param tenant tenant
     * @param id     exact identity
     * @return detached content or absent
     */
    @Override
    public Revision find(String tenant, String id) {
        try {
            Object raw = cache.read(key(tenant, id));
            if (raw instanceof String json) {
                Revision value = JsonKit.toPojo(json, Revision.class);
                check(tenant, id, value);
                return value;
            }
        } catch (RuntimeException unavailable) {
            /* Authoritative immutable source remains available. */
        }
        Revision value = delegate.find(tenant, id);
        if (value != null) {
            check(tenant, id, value);
            put(tenant, id, value);
        }
        return value;
    }

    /**
     * Loads one authoritative batch; never issues per-record delegate reads.
     *
     * @param tenant tenant
     * @param ids    exact identities
     * @return detached content
     */
    @Override
    public List<Revision> findAll(String tenant, Set<String> ids) {
        List<Revision> result = delegate.findAll(tenant, Set.copyOf(ids));
        Set<String> seen = new HashSet<>();
        for (Revision value : result) {
            if (!ids.contains(value.getId()) || !seen.add(value.getId()))
                throw new IllegalStateException("Unexpected store identity");
            check(tenant, value.getId(), value);
            put(tenant, value.getId(), value);
        }
        return List.copyOf(result);
    }

    /**
     * Validates cache identity.
     *
     * @param tenant tenant
     * @param id     exact identity
     * @param value  loaded content
     */
    private void check(String tenant, String id, Revision value) {
        if (!Objects.equals(tenant, value.getTenant_id()) || !Objects.equals(id, value.getId()))
            throw new IllegalStateException("Store identity mismatch");
    }

    /**
     * Populates only protected serialized data.
     *
     * @param tenant tenant
     * @param id     exact identity
     * @param value  protected content
     */
    private void put(String tenant, String id, Revision value) {
        try {
            cache.write(key(tenant, id), JsonKit.toJsonString(value), 0L);
        } catch (RuntimeException unavailable) {
            /* Cache population is not a persistence boundary. */
        }
    }

    /**
     * Builds unambiguous tenant-scoped cache coordinates.
     *
     * @param tenant tenant
     * @param id     exact identity
     * @return cache key
     */
    private String key(String tenant, String id) {
        if (tenant == null || tenant.isBlank() || id == null || id.isBlank())
            throw new IllegalArgumentException("Exact store identity required");
        return "setting:revision:" + tenant.length() + ":" + tenant + id.length() + ":" + id;
    }

    /**
     * Lists formal source history only.
     *
     * @param tenant tenant
     * @param item   source
     * @param offset page offset
     * @param limit  size
     * @return published snapshots
     */
    @Override
    public List<Revision> byItem(String tenant, String item, int offset, int limit) {
        return List.copyOf(delegate.byItem(tenant, item, offset, limit));
    }

    /**
     * Reads one formal release's snapshots.
     *
     * @param tenant  tenant
     * @param release formal release
     * @return snapshots
     */
    @Override
    public List<Revision> byRelease(String tenant, String release) {
        return List.copyOf(delegate.byRelease(tenant, release));
    }

}
