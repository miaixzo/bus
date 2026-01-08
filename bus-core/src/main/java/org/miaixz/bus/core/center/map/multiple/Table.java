/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.center.function.Consumer3X;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Defines a table data structure that maps an ordered pair of keys (row and column) to a single value. This interface
 * is inspired by Guava's Table and provides methods for accessing, modifying, and querying data in a two-dimensional
 * fashion.
 *
 * @param <R> The type of the row key.
 * @param <C> The type of the column key.
 * @param <V> The type of the value stored in the table.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Table<R, C, V> extends Iterable<Table.Cell<R, C, V>> {

    /**
     * Checks if the table contains a mapping for the specified row and column keys.
     *
     * @param rowKey    The row key.
     * @param columnKey The column key.
     * @return {@code true} if a mapping exists for the specified row and column, {@code false} otherwise.
     */
    default boolean contains(final R rowKey, final C columnKey) {
        return Optional.ofNullable(getRow(rowKey)).map((map) -> map.containsKey(columnKey)).orElse(false);
    }

    /**
     * Checks if the table contains the specified row key.
     *
     * @param rowKey The row key to check for.
     * @return {@code true} if the row key exists in the table, {@code false} otherwise.
     */
    default boolean containsRow(final R rowKey) {
        return Optional.ofNullable(rowMap()).map((map) -> map.containsKey(rowKey)).orElse(false);
    }

    /**
     * Retrieves the row corresponding to the specified row key.
     *
     * @param rowKey The row key.
     * @return A {@link Map} where keys are column keys and values are the table's values for that row, or {@code null}
     *         if the row key does not exist.
     */
    default Map<C, V> getRow(final R rowKey) {
        return Optional.ofNullable(rowMap()).map((map) -> map.get(rowKey)).orElse(null);
    }

    /**
     * Returns a {@link Set} of all unique row keys contained in this table.
     *
     * @return A set of row keys.
     */
    default Set<R> rowKeySet() {
        return Optional.ofNullable(rowMap()).map(Map::keySet).orElse(Collections.emptySet());
    }

    /**
     * Returns a view of this table as a map from row keys to maps of column keys to values.
     *
     * @return A map where keys are row keys, and values are maps from column keys to table values.
     */
    Map<R, Map<C, V>> rowMap();

    /**
     * Checks if the table contains the specified column key.
     *
     * @param columnKey The column key to check for.
     * @return {@code true} if the column key exists in the table, {@code false} otherwise.
     */
    default boolean containsColumn(final C columnKey) {
        return Optional.ofNullable(columnMap()).map((map) -> map.containsKey(columnKey)).orElse(false);
    }

    /**
     * Retrieves the column corresponding to the specified column key.
     *
     * @param columnKey The column key.
     * @return A {@link Map} where keys are row keys and values are the table's values for that column, or {@code null}
     *         if the column key does not exist.
     */
    default Map<R, V> getColumn(final C columnKey) {
        return Optional.ofNullable(columnMap()).map((map) -> map.get(columnKey)).orElse(null);
    }

    /**
     * Returns a {@link Set} of all unique column keys contained in this table.
     *
     * @return A set of column keys.
     */
    default Set<C> columnKeySet() {
        return Optional.ofNullable(columnMap()).map(Map::keySet).orElse(Collections.emptySet());
    }

    /**
     * Returns a {@link List} of all column keys contained in this table. If the underlying map implementation allows
     * duplicate keys, this list will reflect those duplicates. This method is useful when the order of column keys
     * matters or when dealing with non-unique column keys.
     *
     * @return A list of column keys.
     */
    default List<C> columnKeys() {
        final Map<C, Map<R, V>> columnMap = columnMap();
        if (MapKit.isEmpty(columnMap)) {
            return ListKit.empty();
        }

        final List<C> result = new ArrayList<>(columnMap.size());
        for (final Map.Entry<C, Map<R, V>> cMapEntry : columnMap.entrySet()) {
            result.add(cMapEntry.getKey());
        }
        return result;
    }

    /**
     * Returns a view of this table as a map from column keys to maps of row keys to values.
     *
     * @return A map where keys are column keys, and values are maps from row keys to table values.
     */
    Map<C, Map<R, V>> columnMap();

    /**
     * Checks if the table contains the specified value anywhere within its cells.
     *
     * @param value The value to check for.
     * @return {@code true} if the value is found in the table, {@code false} otherwise.
     */
    default boolean containsValue(final V value) {
        final Collection<Map<C, V>> rows = Optional.ofNullable(rowMap()).map(Map::values).getOrNull();
        if (null != rows) {
            for (final Map<C, V> row : rows) {
                if (row.containsValue(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the value at the specified row and column intersection.
     *
     * @param rowKey    The row key.
     * @param columnKey The column key.
     * @return The value at the specified cell, or {@code null} if no such value exists.
     */
    default V get(final R rowKey, final C columnKey) {
        return Optional.ofNullable(getRow(rowKey)).map((map) -> map.get(columnKey)).orElse(null);
    }

    /**
     * Returns a {@link Collection} view of all values contained in this table.
     *
     * @return A collection view of the values contained in this table.
     */
    Collection<V> values();

    /**
     * Returns a {@link Set} view of all cell entries contained in this table. Each element in the returned set is a
     * {@link Table.Cell}.
     *
     * @return A set view of the cells contained in this table.
     */
    Set<Cell<R, C, V>> cellSet();

    /**
     * Associates the specified value with the specified row and column keys in this table. If the table previously
     * contained a mapping for the keys, the old value is replaced.
     *
     * @param rowKey    The row key with which the specified value is to be associated.
     * @param columnKey The column key with which the specified value is to be associated.
     * @param value     The value to be associated with the specified keys.
     * @return The previous value associated with the keys, or {@code null} if there was no mapping for the keys.
     */
    V put(R rowKey, C columnKey, V value);

    /**
     * Copies all of the mappings from the specified table to this table. These mappings will replace any mappings that
     * this table had for any of the keys currently in the specified table.
     *
     * @param table The table whose mappings are to be placed in this table.
     */
    default void putAll(final Table<? extends R, ? extends C, ? extends V> table) {
        if (null != table) {
            for (final Table.Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
        }
    }

    /**
     * Removes the mapping for a row and column key from this table if it is present.
     *
     * @param rowKey    The row key whose mapping is to be removed from the table.
     * @param columnKey The column key whose mapping is to be removed from the table.
     * @return The previous value associated with the keys, or {@code null} if there was no mapping for the keys.
     */
    V remove(R rowKey, C columnKey);

    /**
     * Returns {@code true} if this table contains no cell entries.
     *
     * @return {@code true} if this table contains no cell entries.
     */
    boolean isEmpty();

    /**
     * Returns the number of cell entries in this table.
     *
     * @return The number of cell entries in this table.
     */
    default int size() {
        final Map<R, Map<C, V>> rowMap = rowMap();
        if (MapKit.isEmpty(rowMap)) {
            return 0;
        }
        int size = 0;
        for (final Map<C, V> map : rowMap.values()) {
            size += map.size();
        }
        return size;
    }

    /**
     * Removes all of the mappings from this table. The table will be empty after this call returns.
     */
    void clear();

    /**
     * Performs the given action for each cell in this table until all cells have been processed or the action throws an
     * exception.
     *
     * @param consumer The action to be performed for each cell. It receives the row key, column key, and value of each
     *                 cell.
     */
    default void forEach(final Consumer3X<? super R, ? super C, ? super V> consumer) {
        for (final Cell<R, C, V> cell : this) {
            consumer.accept(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    /**
     * Represents a single cell in the table, containing its row key, column key, and value.
     *
     * @param <R> The type of the row key.
     * @param <C> The type of the column key.
     * @param <V> The type of the value.
     */
    interface Cell<R, C, V> {

        /**
         * Retrieves the row key of this cell.
         *
         * @return The row key.
         */
        R getRowKey();

        /**
         * Retrieves the column key of this cell.
         *
         * @return The column key.
         */
        C getColumnKey();

        /**
         * Retrieves the value of this cell.
         *
         * @return The value.
         */
        V getValue();
    }

}
