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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionNumbers;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Cache-backed append-only revision store.
 *
 * @author Kimi Liu
 */
public class CacheRevisionStore implements RevisionStore {

    /**
     * Namespace prefix for cached revisions.
     */
    private static final String PREFIX = "cortex:setting:revision:";

    /**
     * Cache used as the append-only revision backend.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a cache-backed revision store.
     *
     * @param cacheX revision cache
     */
    public CacheRevisionStore(CacheX<String, Object> cacheX) {
        if (cacheX == null) {
            throw new IllegalArgumentException("Revision cache is required");
        }
        this.cacheX = cacheX;
    }

    /**
     * Appends one immutable revision.
     *
     * @param revision revision to append
     * @return appended revision
     */
    @Override
    public Revision save(Revision revision) {
        if (revision == null || revision.getItem_id() == null || revision.getRevision() == null) {
            throw new IllegalArgumentException("Revision item_id and revision are required");
        }
        String key = key(revision.getTenant_id(), revision.getItem_id(), revision.getRevision());
        if (cacheX.read(key) != null) {
            throw new IllegalStateException("Revision already exists: " + revision.getRevision());
        }
        cacheX.write(key, JsonKit.toJsonString(revision), 0L);
        return revision;
    }

    /**
     * Finds one revision by tenant, item, and revision number.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @param revision  item-scoped revision number
     * @return matching revision, or {@code null} when absent
     */
    @Override
    public Revision find(String tenant_id, String item_id, String revision) {
        Object value = cacheX.read(key(tenant_id, item_id, revision));
        if (value instanceof Revision snapshot) {
            return snapshot;
        }
        return value instanceof String json ? JsonKit.toPojo(json, Revision.class) : null;
    }

    /**
     * Lists revisions for one item in newest-first order.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @return immutable revisions
     */
    @Override
    public List<Revision> query(String tenant_id, String item_id) {
        Map<String, Object> values = cacheX.scan(prefix(tenant_id, item_id));
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<Revision> revisions = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            Revision revision = value instanceof Revision snapshot ? snapshot
                    : value instanceof String json ? JsonKit.toPojo(json, Revision.class) : null;
            if (revision != null) {
                revisions.add(revision);
            }
        }
        revisions.sort(
                Comparator.comparingLong((Revision value) -> RevisionNumbers.sortKey(value.getRevision())).reversed());
        return revisions;
    }

    /**
     * Removes revisions older than the configured retention boundary.
     *
     * @param tenant_id    tenant identifier, when available
     * @param item_id      item identifier
     * @param maxRevisions maximum number of newest revisions to retain
     */
    @Override
    public void retainLatest(String tenant_id, String item_id, int maxRevisions) {
        if (maxRevisions < 1) {
            throw new IllegalArgumentException("maxRevisions must be positive");
        }
        List<Revision> revisions = query(tenant_id, item_id);
        if (revisions.size() <= maxRevisions) {
            return;
        }
        List<String> expired = new ArrayList<>();
        for (int index = maxRevisions; index < revisions.size(); index++) {
            Revision revision = revisions.get(index);
            expired.add(key(tenant_id, item_id, revision.getRevision()));
        }
        cacheX.remove(expired.toArray(String[]::new));
    }

    /**
     * Builds the full key for one revision.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @param revision  item-scoped revision number
     * @return revision cache key
     */
    private String key(String tenant_id, String item_id, String revision) {
        return prefix(tenant_id, item_id) + value(revision);
    }

    /**
     * Builds the scan prefix for all revisions of one item.
     *
     * @param tenant_id tenant identifier, when available
     * @param item_id   item identifier
     * @return item revision prefix
     */
    private String prefix(String tenant_id, String item_id) {
        return PREFIX + value(tenant_id) + ':' + value(item_id) + ':';
    }

    /**
     * Normalizes a nullable key segment.
     *
     * @param value segment value
     * @return normalized segment
     */
    private String value(String value) {
        return value == null ? "_" : value;
    }
}
