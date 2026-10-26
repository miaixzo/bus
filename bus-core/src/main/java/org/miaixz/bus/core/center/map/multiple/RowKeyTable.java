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
package org.miaixz.bus.core.center.map.multiple;

import java.util.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.center.map.AbstractEntry;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * A {@link Table} implementation that uses a row key as the primary key. The internal structure is a map where each row
 * key maps to another map of column keys to values. Structure: {@code Map<Row, Map<Column, Value>>}. The raw map and
 * its row maps are shared rather than copied. Empty rows can exist through direct map access and do not by themselves
 * make the table non-empty. Callers must provide any synchronization required by the chosen maps.
 *
 * @param <R> The type of the row keys.
 * @param <C> The type of the column keys.
 * @param <V> The type of the values.
 * @author Kimi Liu
 */
public class RowKeyTable<R, C, V> extends AbstractTable<R, C, V> {

    /**
     * The raw underlying map that stores the table data, keyed by row.
     */
    final Map<R, Map<C, V>> raw;

    /**
     * A builder used to create new column maps when a new row is added.
     */
    final Builder<? extends Map<C, V>> columnBuilder;

    /**
     * A cached, lazily-initialized view of the table as a map of columns to rows.
     */
    private Map<C, Map<R, V>> columnMap;

    /**
     * A cached, lazily-initialized set of all unique column keys.
     */
    private Set<C> columnKeySet;

    /**
     * Constructs a new, empty {@code RowKeyTable} backed by a {@link HashMap}.
     */
    public RowKeyTable() {
        this(new HashMap<>());
    }

    /**
     * Constructs a new {@code RowKeyTable}.
     *
     * @param isLinked If {@code true}, the underlying maps will be {@link LinkedHashMap}s to preserve insertion order.
     */
    public RowKeyTable(final boolean isLinked) {
        this(MapKit.newHashMap(isLinked), () -> MapKit.newHashMap(isLinked));
    }

    /**
     * Constructs a new {@code RowKeyTable} wrapping the specified raw map.
     *
     * @param raw the shared raw map to wrap; row maps must be non-null when traversed
     */
    public RowKeyTable(final Map<R, Map<C, V>> raw) {
        this(raw, HashMap::new);
    }

    /**
     * Constructs a new {@code RowKeyTable} with a specified raw map and column map builder.
     *
     * @param raw              the shared raw map to wrap; row maps must be non-null when traversed
     * @param columnMapBuilder a builder producing empty inner maps, or null to use {@link HashMap}
     */
    public RowKeyTable(final Map<R, Map<C, V>> raw, final Builder<? extends Map<C, V>> columnMapBuilder) {
        this.raw = raw;
        this.columnBuilder = columnMapBuilder == null ? HashMap::new : columnMapBuilder;
    }

    /**
     * Returns the shared row-indexed backing map. Direct modifications are visible to this table.
     *
     * @return the shared map from row keys to column maps
     */
    @Override
    public Map<R, Map<C, V>> rowMap() {
        return raw;
    }

    /**
     * Stores a value at the given coordinates, creating the row with the configured builder if necessary.
     *
     * @param rowKey    the row key
     * @param columnKey the column key
     * @param value     the value to store
     * @return the previous value at the coordinates, or null if no value was associated
     */
    @Override
    public V put(final R rowKey, final C columnKey, final V value) {
        return raw.computeIfAbsent(rowKey, (key) -> columnBuilder.build()).put(columnKey, value);
    }

    /**
     * Removes the given coordinate and removes its outer row mapping if the row becomes empty.
     *
     * @param rowKey    the row key
     * @param columnKey the column key
     * @return the removed value, or null if no value was associated
     */
    @Override
    public V remove(final R rowKey, final C columnKey) {
        final Map<C, V> map = getRow(rowKey);
        if (null == map) {
            return null;
        }
        final V value = map.remove(columnKey);
        if (map.isEmpty()) {
            raw.remove(rowKey);
        }
        return value;
    }

    /**
     * Tests whether all row maps contain no cells, ignoring any empty outer row mappings.
     *
     * @return whether no row contains a cell
     */
    @Override
    public boolean isEmpty() {
        for (final Map<C, V> row : raw.values()) {
            if (!row.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes every row mapping from the shared backing map.
     */
    @Override
    public void clear() {
        this.raw.clear();
    }

    /**
     * Tests whether any non-null row map contains the specified column key. A null column key is reported absent.
     *
     * @param columnKey the column key to search for
     * @return whether any row map contains the non-null key
     */
    @Override
    public boolean containsColumn(final C columnKey) {
        if (columnKey == null) {
            return false;
        }
        for (final Map<C, V> map : raw.values()) {
            if (null != map && map.containsKey(columnKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a lazily cached view that discovers column keys from the current row maps. Its values are individual
     * column views; their entries can update existing row maps, but the outer view does not support general insertion.
     *
     * @return a column-indexed view over the shared rows
     */
    @Override
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> result = columnMap;
        return (result == null) ? columnMap = new ColumnMap() : result;
    }

    /**
     * Returns a lazily cached view of distinct column keys, discovered by scanning the current rows. The view's
     * iterator does not support removal.
     *
     * @return the distinct column-key view
     */
    @Override
    public Set<C> columnKeySet() {
        Set<C> result = columnKeySet;
        return (result == null) ? columnKeySet = new ColumnKeySet() : result;
    }

    /**
     * Collects the column keys from every current row into a new list. Keys occurring in multiple rows appear more than
     * once; ordering follows the backing row and column maps.
     *
     * @return a snapshot list of column-key occurrences
     */
    @Override
    public List<C> columnKeys() {
        final Collection<Map<C, V>> values = this.raw.values();
        final List<C> result = new ArrayList<>(values.size() * 16);
        for (final Map<C, V> map : values) {
            result.addAll(map.keySet());
        }
        return result;
    }

    /**
     * Creates a view of one column, mapping row keys to values at that column. Entry value replacements write through
     * to their existing row maps; general insertion and iterator removal are not provided by this view.
     *
     * @param columnKey the column represented by the view
     * @return a new column view over the shared table data
     */
    @Override
    public Map<R, V> getColumn(final C columnKey) {
        return new Column(columnKey);
    }

    /**
     * A view of the table as a map from column keys to maps of row keys to values.
     *
     * @author Kimi Liu
     */
    private class ColumnMap extends AbstractMap<C, Map<R, V>> {

        /**
         * Returns a view pairing each discovered column key with its backed column view.
         *
         * @return the column-view entry set
         */
        @Override
        public Set<Entry<C, Map<R, V>>> entrySet() {
            return new ColumnMapEntrySet();
        }

    }

    /**
     * The entry set for the {@link ColumnMap} view.
     *
     * @author Kimi Liu
     */
    private class ColumnMapEntrySet extends AbstractSet<Map.Entry<C, Map<R, V>>> {

        /**
         * Projects distinct column keys to column-view entries. Removal through this iterator is unsupported.
         *
         * @return an iterator over the discovered columns
         */
        @Override
        public Iterator<Map.Entry<C, Map<R, V>>> iterator() {
            return new TransIterator<>(columnKeySet().iterator(), c -> MapKit.entry(c, getColumn(c)));
        }

        /**
         * Counts the currently discoverable distinct column keys.
         *
         * @return the current distinct-column count
         */
        @Override
        public int size() {
            return columnKeySet().size();
        }

    }

    /**
     * A view of the set of unique column keys.
     *
     * @author Kimi Liu
     */
    private class ColumnKeySet extends AbstractSet<C> {

        /**
         * Creates an iterator that scans rows and emits each discovered column key once.
         *
         * @return a distinct-column iterator without removal support
         */
        @Override
        public Iterator<C> iterator() {
            return new ColumnKeyIterator();
        }

        /**
         * Counts distinct column keys by traversing a fresh iterator.
         *
         * @return the current distinct-column count
         */
        @Override
        public int size() {
            return IteratorKit.size(iterator());
        }

    }

    /**
     * An iterator that traverses all unique column keys in the table.
     *
     * @author Kimi Liu
     */
    private class ColumnKeyIterator extends ComputeIterator<C> {

        /**
         * Tracks seen column keys using the configured column-map implementation and its key-equivalence rules.
         */
        final Map<C, V> seen = columnBuilder.build();

        /**
         * An iterator over all rows.
         */
        final Iterator<Map<C, V>> mapIterator = raw.values().iterator();

        /**
         * An iterator over the entries in the current row's column map.
         */
        Iterator<Map.Entry<C, V>> entryIterator = Collections.emptyIterator();

        /**
         * Searches the current and subsequent rows for an unseen column key.
         *
         * @return the next unseen key, or null to signal completion; null column keys cannot be represented as elements
         */
        @Override
        protected C computeNext() {
            while (true) {
                if (entryIterator.hasNext()) {
                    final Map.Entry<C, V> entry = entryIterator.next();
                    if (!seen.containsKey(entry.getKey())) {
                        seen.put(entry.getKey(), entry.getValue());
                        return entry.getKey();
                    }
                } else if (mapIterator.hasNext()) {
                    entryIterator = mapIterator.next().entrySet().iterator();
                } else {
                    return null;
                }
            }
        }

    }

    /**
     * A view of a single column as a map from row keys to values.
     *
     * @author Kimi Liu
     */
    private class Column extends AbstractMap<R, V> {

        /**
         * The column key for this column view.
         */
        final C columnKey;

        /**
         * Creates a backed view for the specified column.
         *
         * @param columnKey The column key.
         */
        Column(final C columnKey) {
            this.columnKey = columnKey;
        }

        /**
         * Returns a view of the existing row entries containing this column. Entry replacements write through to the
         * row map, while iterator removal is unsupported.
         *
         * @return the column's row/value entry view
         */
        @Override
        public Set<Entry<R, V>> entrySet() {
            return new EntrySet();
        }

        /**
         * A view of the entry set for a single column.
         *
         * @author Kimi Liu
         */
        private class EntrySet extends AbstractSet<Map.Entry<R, V>> {

            /**
             * Creates an iterator over row mappings containing this column.
             *
             * @return a write-through-entry iterator without removal support
             */
            @Override
            public Iterator<Map.Entry<R, V>> iterator() {
                return new EntrySetIterator();
            }

            /**
             * Counts rows that currently contain the column, including those mapping it to null.
             *
             * @return the number of matching rows
             */
            @Override
            public int size() {
                int size = 0;
                for (final Map<C, V> map : raw.values()) {
                    if (map.containsKey(columnKey)) {
                        size++;
                    }
                }
                return size;
            }

        }

        /**
         * An iterator that traverses all entries in a single column.
         *
         * @author Kimi Liu
         */
        private class EntrySetIterator extends ComputeIterator<Entry<R, V>> {

            /**
             * An iterator over all rows in the table.
             */
            final Iterator<Entry<R, Map<C, V>>> iterator = raw.entrySet().iterator();

            /**
             * Searches rows for the next mapping containing this column and exposes its current value through a
             * write-through entry.
             *
             * @return the next matching row/value entry, or null when no matching row remains
             */
            @Override
            protected Entry<R, V> computeNext() {
                while (iterator.hasNext()) {
                    final Entry<R, Map<C, V>> entry = iterator.next();
                    if (entry.getValue().containsKey(columnKey)) {
                        return new AbstractEntry<>() {

                            /**
                             * Returns the row key represented by this column entry.
                             *
                             * @return the backing row key
                             */
                            @Override
                            public R getKey() {
                                return entry.getKey();
                            }

                            /**
                             * Reads the current column value from the represented row map.
                             *
                             * @return the current value, possibly null
                             */
                            @Override
                            public V getValue() {
                                return entry.getValue().get(columnKey);
                            }

                            /**
                             * Replaces the column value directly in the represented row map.
                             *
                             * @param value the new value
                             * @return the old value
                             */
                            @Override
                            public V setValue(final V value) {
                                return entry.getValue().put(columnKey, value);
                            }
                        };
                    }
                }
                return null;
            }

        }

    }

}
