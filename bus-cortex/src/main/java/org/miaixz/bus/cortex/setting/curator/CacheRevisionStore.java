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
import java.util.function.Predicate;

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
     * Complete host-supplied reachability check; unknown reachability retains every revision.
     */
    private final Predicate<Revision> protectedRevision;

    /**
     * Creates a cache-backed revision store.
     *
     * @param cacheX revision cache
     */
    public CacheRevisionStore(CacheX<String, Object> cacheX) {
        this(cacheX, revision -> true);
    }

    /**
     * Creates a store with an authoritative reference, release, gray, and watch reachability check. The host must
     * serialize reachability changes with pruning. This cache adapter is not a distributed transaction.
     */
    public CacheRevisionStore(CacheX<String, Object> cacheX, Predicate<Revision> protectedRevision) {
        if (protectedRevision == null) {
            throw new IllegalArgumentException("Revision reachability policy is required");
        }
        this.protectedRevision = protectedRevision;
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
        if (revision == null) {
            throw new IllegalArgumentException("Revision is required");
        }
        String key = key(revision.getTenant_id(), revision.getItem_id(), revision.getRevision());
        synchronized (cacheX) {
            if (cacheX.read(key) != null) {
                throw new IllegalStateException("Revision already exists: " + revision.getRevision());
            }
            cacheX.write(key, JsonKit.toJsonString(revision), 0L);
        }
        return revision;
    }

    /**
     * Finds one revision by tenant, item, and revision number.
     *
     * @param tenant_id mandatory tenant identifier
     * @param item_id   item identifier
     * @param revision  item-scoped revision number
     * @return matching revision, or {@code null} when absent
     */
    @Override
    public Revision find(String tenant_id, String item_id, String revision) {
        Object value = cacheX.read(key(tenant_id, item_id, revision));
        if (value instanceof Revision snapshot) {
            if (!tenant_id.equals(snapshot.getTenant_id()) || !item_id.equals(snapshot.getItem_id())
                    || !revision.equals(snapshot.getRevision())) {
                throw new IllegalStateException("Revision store returned mismatched coordinates");
            }
            return JsonKit.toPojo(JsonKit.toJsonString(snapshot), Revision.class);
        }
        Revision snapshot = value instanceof String json ? JsonKit.toPojo(json, Revision.class) : null;
        if (snapshot != null && (!tenant_id.equals(snapshot.getTenant_id()) || !item_id.equals(snapshot.getItem_id())
                || !revision.equals(snapshot.getRevision()))) {
            throw new IllegalStateException("Revision store returned mismatched coordinates");
        }
        return snapshot;
    }

    /**
     * Lists revisions for one item in newest-first order.
     *
     * @param tenant_id mandatory tenant identifier
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
                if (!tenant_id.equals(revision.getTenant_id()) || !item_id.equals(revision.getItem_id())) {
                    throw new IllegalStateException("Revision store returned mismatched coordinates");
                }
                revisions.add(JsonKit.toPojo(JsonKit.toJsonString(revision), Revision.class));
            }
        }
        revisions.sort(
                Comparator.comparingLong((Revision value) -> RevisionNumbers.sortKey(value.getRevision())).reversed());
        return revisions;
    }

    /**
     * Removes unreachable revisions beyond the newest-revision retention boundary.
     *
     * @param tenant_id    mandatory tenant identifier
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
            if (!protectedRevision.test(revision)) {
                expired.add(key(tenant_id, item_id, revision.getRevision()));
            }
        }
        if (!expired.isEmpty()) {
            cacheX.remove(expired.toArray(String[]::new));
        }
    }

    /**
     * Builds the full key for one revision.
     *
     * @param tenant_id mandatory tenant identifier
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
     * @param tenant_id mandatory tenant identifier
     * @param item_id   item identifier
     * @return item revision prefix
     */
    private String prefix(String tenant_id, String item_id) {
        return PREFIX + value(tenant_id) + value(item_id);
    }

    /**
     * Validates and length-prefixes one required coordinate to prevent delimiter collisions.
     *
     * @param value segment value
     * @return unambiguous segment
     */
    private String value(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Revision tenant, item, and revision coordinates must be nonblank");
        }
        return value.length() + ":" + value;
    }

}
