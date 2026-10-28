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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.util.*;

/**
 * A shared-map wrapper with read-only, live reverse lookup. The supplied map remains shared: modifications through its
 * owner, this wrapper or its forward views are observed by subsequent reverse queries.
 * <p>
 * Reverse lookup scans the current mappings in O(n) time without retaining an inverse index. When values repeat, the
 * last matching key in the forward map's current iteration order represents that value; this is not a historical
 * last-write guarantee. Reverse iteration uses a temporary O(n) snapshot to eliminate duplicate values, and reverse
 * size queries use a temporary set of distinct values. A retained reverse view does not retain an inverse table, but a
 * retained iterator keeps its snapshot's keys and values alive.
 * <p>
 * This wrapper adds no synchronization or stronger iteration guarantees than the backing map.
 *
 * @param <K> forward key type
 * @param <V> forward value type
 * @author Kimi Liu
 */
public class BiMap<K, V> extends MapWrapper<K, V> {

    @Serial
    private static final long serialVersionUID = 2535812966621L;

    /**
     * Wraps a shared forward map without copying it or changing its reference policy.
     *
     * @param raw non-null backing map
     */
    public BiMap(final Map<K, V> raw) {
        super(raw);
    }

    /**
     * Returns a read-only reverse view. Lookup and membership queries observe the current forward map. Entry
     * collections remain backed views, while each iterator represents a read-only snapshot captured when created.
     *
     * @return read-only live reverse view
     */
    public Map<V, K> getInverse() {
        return Collections.unmodifiableMap(new AbstractMap<>() {

            /**
             * Scans current forward mappings for the last key whose value equals the supplied reverse key.
             *
             * @param value forward value to locate
             * @return the representative forward key, or {@code null} if absent or represented by a null key
             */
            @Override
            public K get(final Object value) {
                return BiMap.this.getKey((V) value);
            }

            /**
             * Tests whether the current forward map contains the supplied reverse key as a value.
             *
             * @param value forward value whose presence is to be tested
             * @return {@code true} if a current forward mapping has that value
             */
            @Override
            public boolean containsKey(final Object value) {
                return BiMap.this.containsValue(value);
            }

            /**
             * Tests whether the current forward map contains no mappings.
             *
             * @return {@code true} if the forward map and this reverse view are empty
             */
            @Override
            public boolean isEmpty() {
                return BiMap.this.isEmpty();
            }

            /**
             * Counts current distinct forward values using a temporary set.
             *
             * @return the number of reverse keys currently represented
             */
            @Override
            public int size() {
                return inverseSize();
            }

            /**
             * Creates a reverse-entry collection whose size is current and whose iterators capture independent
             * snapshots. The enclosing unmodifiable map prevents mutation through the exposed collection.
             *
             * @return the reverse-entry collection backed by subsequent forward-map observations
             */
            @Override
            public Set<Entry<V, K>> entrySet() {
                return new AbstractSet<>() {

                    /**
                     * Captures current reverse mappings, keeping the last encountered forward key for each value.
                     * Iterator removal and entry replacement are not supported.
                     *
                     * @return a read-only iterator over the captured reverse-entry snapshot
                     */
                    @Override
                    public Iterator<Entry<V, K>> iterator() {
                        return Collections.unmodifiableMap(inverseSnapshot()).entrySet().iterator();
                    }

                    /**
                     * Counts current distinct forward values independently of any previously created iterator snapshot.
                     *
                     * @return the current number of distinct reverse keys
                     */
                    @Override
                    public int size() {
                        return inverseSize();
                    }
                };
            }
        });
    }

    /**
     * Finds the last matching key in the forward map's current iteration order. No inverse mappings are retained.
     *
     * @param value value to locate, subject to the backing map's null policy
     * @return representative key, or null if absent or represented by a null key
     */
    public K getKey(final V value) {
        K result = null;
        for (final Entry<K, V> entry : getRaw().entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                result = entry.getKey();
            }
        }
        return result;
    }

    /**
     * Captures one reverse-iteration snapshot. This temporary table is owned by the iterator, never by the BiMap.
     *
     * @return reverse mappings in forward encounter order, with the last matching key retained for each value
     */
    private Map<V, K> inverseSnapshot() {
        final Map<V, K> snapshot = new LinkedHashMap<>();
        getRaw().forEach((key, value) -> snapshot.put(value, key));
        return snapshot;
    }

    /**
     * Counts reverse keys using only a temporary set of current forward values.
     *
     * @return the number of distinct current forward values
     */
    private int inverseSize() {
        return new HashSet<>(getRaw().values()).size();
    }

}
