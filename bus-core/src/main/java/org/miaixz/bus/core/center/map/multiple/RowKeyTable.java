/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * key maps to another map of column keys to values. Structure: {@code Map<Row, Map<Column, Value>>}.
 *
 * @param <R> The type of the row keys.
 * @param <C> The type of the column keys.
 * @param <V> The type of the values.
 * @author Kimi Liu
 * @since Java 17+
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
     * @param raw The raw map to wrap.
     */
    public RowKeyTable(final Map<R, Map<C, V>> raw) {
        this(raw, HashMap::new);
    }

    /**
     * Constructs a new {@code RowKeyTable} with a specified raw map and column map builder.
     *
     * @param raw              The raw map to wrap.
     * @param columnMapBuilder A builder for creating the inner column maps.
     */
    public RowKeyTable(final Map<R, Map<C, V>> raw, final Builder<? extends Map<C, V>> columnMapBuilder) {
        this.raw = raw;
        this.columnBuilder = (Builder<? extends Map<C, V>>) Objects
                .requireNonNullElseGet(columnMapBuilder, HashMap::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<R, Map<C, V>> rowMap() {
        return raw;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(final R rowKey, final C columnKey, final V value) {
        return raw.computeIfAbsent(rowKey, (key) -> columnBuilder.build()).put(columnKey, value);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.raw.clear();
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> result = columnMap;
        return (result == null) ? columnMap = new ColumnMap() : result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<C> columnKeySet() {
        Set<C> result = columnKeySet;
        return (result == null) ? columnKeySet = new ColumnKeySet() : result;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Map<R, V> getColumn(final C columnKey) {
        return new Column(columnKey);
    }

    /**
     * A view of the table as a map from column keys to maps of row keys to values.
     */
    private class ColumnMap extends AbstractMap<C, Map<R, V>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Entry<C, Map<R, V>>> entrySet() {
            return new ColumnMapEntrySet();
        }
    }

    /**
     * The entry set for the {@link ColumnMap} view.
     */
    private class ColumnMapEntrySet extends AbstractSet<Map.Entry<C, Map<R, V>>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Map.Entry<C, Map<R, V>>> iterator() {
            return new TransIterator<>(columnKeySet().iterator(), c -> MapKit.entry(c, getColumn(c)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return columnKeySet().size();
        }
    }

    /**
     * A view of the set of unique column keys.
     */
    private class ColumnKeySet extends AbstractSet<C> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<C> iterator() {
            return new ColumnKeyIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return IteratorKit.size(iterator());
        }
    }

    /**
     * An iterator that traverses all unique column keys in the table.
     */
    private class ColumnKeyIterator extends ComputeIterator<C> {

        /**
         * Tracks seen column keys to ensure uniqueness.
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
         * {@inheritDoc}
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
     */
    private class Column extends AbstractMap<R, V> {

        /**
         * The column key for this column view.
         */
        final C columnKey;

        /**
         * Constructor.
         * 
         * @param columnKey The column key.
         */
        Column(final C columnKey) {
            this.columnKey = columnKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Entry<R, V>> entrySet() {
            return new EntrySet();
        }

        /**
         * A view of the entry set for a single column.
         */
        private class EntrySet extends AbstractSet<Map.Entry<R, V>> {

            /**
             * {@inheritDoc}
             */
            @Override
            public Iterator<Map.Entry<R, V>> iterator() {
                return new EntrySetIterator();
            }

            /**
             * {@inheritDoc}
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
         */
        private class EntrySetIterator extends ComputeIterator<Entry<R, V>> {

            /**
             * An iterator over all rows in the table.
             */
            final Iterator<Entry<R, Map<C, V>>> iterator = raw.entrySet().iterator();

            /**
             * {@inheritDoc}
             */
            @Override
            protected Entry<R, V> computeNext() {
                while (iterator.hasNext()) {
                    final Entry<R, Map<C, V>> entry = iterator.next();
                    if (entry.getValue().containsKey(columnKey)) {
                        return new AbstractEntry<>() {

                            @Override
                            public R getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public V getValue() {
                                return entry.getValue().get(columnKey);
                            }

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
