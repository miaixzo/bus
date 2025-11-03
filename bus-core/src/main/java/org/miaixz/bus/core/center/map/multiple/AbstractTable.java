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

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An abstract base implementation of the {@link Table} interface, providing default implementations for common methods.
 * This class handles the basic structure and behavior of a two-key map, leaving the specific storage mechanism to
 * concrete subclasses.
 *
 * @param <R> The type of the row key.
 * @param <C> The type of the column key.
 * @param <V> The type of the value stored in the table.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractTable<R, C, V> implements Table<R, C, V> {

    /**
     * Cached collection view of all values in the table. Lazily initialized.
     */
    private transient Collection<V> values;
    /**
     * Cached set view of all cells in the table. Lazily initialized.
     */
    private transient Set<Cell<R, C, V>> cellSet;

    /**
     * Compares the specified object with this table for equality. Returns {@code true} if the given object is also a
     * {@code Table} and the two tables represent the same mappings (i.e., their {@link #cellSet()}s are equal).
     *
     * @param object The object to be compared for equality with this table.
     * @return {@code true} if the specified object is equal to this table.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof Table<?, ?, ?> that) {
            return this.cellSet().equals(that.cellSet());
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code value for this table. The hash code of a table is defined as the hash code of its
     * {@link #cellSet()}.
     *
     * @return The hash code value for this table.
     */
    @Override
    public int hashCode() {
        return cellSet().hashCode();
    }

    /**
     * Returns a string representation of this table. The string representation is the same as that of its
     * {@link #rowMap()}.
     *
     * @return A string representation of this table.
     */
    @Override
    public String toString() {
        return rowMap().toString();
    }

    /**
     * Returns a {@link Collection} view of all values contained in this table. The collection is backed by the table,
     * so changes to the table are reflected in the collection, and vice-versa.
     *
     * @return A collection view of the values contained in this table.
     */
    @Override
    public Collection<V> values() {
        final Collection<V> result = values;
        return (result == null) ? values = new Values() : result;
    }

    /**
     * Returns a {@link Set} view of all cell entries contained in this table. Each element in the returned set is a
     * {@link Table.Cell}. The set is backed by the table, so changes to the table are reflected in the set, and
     * vice-versa.
     *
     * @return A set view of the cells contained in this table.
     */
    @Override
    public Set<Cell<R, C, V>> cellSet() {
        final Set<Cell<R, C, V>> result = cellSet;
        return (result == null) ? cellSet = new CellSet() : result;
    }

    /**
     * Returns an iterator over the cells in this table.
     *
     * @return An {@link Iterator} over {@link Table.Cell} objects.
     */
    @Override
    public Iterator<Cell<R, C, V>> iterator() {
        return new CellIterator();
    }

    /**
     * A simple, immutable implementation of {@link Table.Cell}.
     *
     * @param <R> The type of the row key.
     * @param <C> The type of the column key.
     * @param <V> The type of the value.
     */
    private static class SimpleCell<R, C, V> implements Cell<R, C, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 2852277093211L;

        private final R rowKey;
        private final C columnKey;
        private final V value;

        /**
         * Constructs a new {@code SimpleCell}.
         *
         * @param rowKey    The row key.
         * @param columnKey The column key.
         * @param value     The value.
         */
        SimpleCell(final R rowKey, final C columnKey, final V value) {
            this.rowKey = rowKey;
            this.columnKey = columnKey;
            this.value = value;
        }

        @Override
        public R getRowKey() {
            return rowKey;
        }

        @Override
        public C getColumnKey() {
            return columnKey;
        }

        @Override
        public V getValue() {
            return value;
        }

        /**
         * Compares the specified object with this cell for equality. Returns {@code true} if the given object is also a
         * {@code Cell} and the two cells represent the same row key, column key, and value.
         *
         * @param object The object to be compared for equality with this cell.
         * @return {@code true} if the specified object is equal to this cell.
         */
        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (object instanceof Cell<?, ?, ?> other) {
                return ObjectKit.equals(rowKey, other.getRowKey()) && ObjectKit.equals(columnKey, other.getColumnKey())
                        && ObjectKit.equals(value, other.getValue());
            }
            return false;
        }

        /**
         * Returns the hash code value for this cell. The hash code is based on the row key, column key, and value.
         *
         * @return The hash code value for this cell.
         */
        @Override
        public int hashCode() {
            return Objects.hash(rowKey, columnKey, value);
        }

        /**
         * Returns a string representation of this cell. The string representation is in the format
         * "(rowKey,columnKey)=value".
         *
         * @return A string representation of this cell.
         */
        @Override
        public String toString() {
            return Symbol.PARENTHESE_LEFT + rowKey + Symbol.COMMA + columnKey + ")=" + value;
        }
    }

    /**
     * A {@link Collection} view of the values contained in the table.
     */
    private class Values extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new TransIterator<>(cellSet().iterator(), Cell::getValue);
        }

        @Override
        public boolean contains(final Object o) {
            return containsValue((V) o);
        }

        @Override
        public void clear() {
            AbstractTable.this.clear();
        }

        @Override
        public int size() {
            return AbstractTable.this.size();
        }
    }

    /**
     * A {@link Set} view of the cells contained in the table.
     */
    private class CellSet extends AbstractSet<Cell<R, C, V>> {

        @Override
        public boolean contains(final Object o) {
            if (o instanceof Cell) {
                final Cell<R, C, V> cell = (Cell<R, C, V>) o;
                final Map<C, V> row = getRow(cell.getRowKey());
                if (null != row) {
                    return ObjectKit.equals(row.get(cell.getColumnKey()), cell.getValue());
                }
            }
            return false;
        }

        @Override
        public boolean remove(final Object o) {
            if (contains(o)) {
                final Cell<R, C, V> cell = (Cell<R, C, V>) o;
                AbstractTable.this.remove(cell.getRowKey(), cell.getColumnKey());
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            AbstractTable.this.clear();
        }

        @Override
        public Iterator<Table.Cell<R, C, V>> iterator() {
            return new AbstractTable<R, C, V>.CellIterator();
        }

        @Override
        public int size() {
            return AbstractTable.this.size();
        }
    }

    /**
     * An iterator for traversing the cells of the table.
     */
    private class CellIterator implements Iterator<Cell<R, C, V>> {

        final Iterator<Map.Entry<R, Map<C, V>>> rowIterator = rowMap().entrySet().iterator();
        Map.Entry<R, Map<C, V>> rowEntry;
        Iterator<Map.Entry<C, V>> columnIterator = IteratorKit.empty();

        @Override
        public boolean hasNext() {
            return rowIterator.hasNext() || columnIterator.hasNext();
        }

        @Override
        public Cell<R, C, V> next() {
            if (!columnIterator.hasNext()) {
                rowEntry = rowIterator.next();
                columnIterator = rowEntry.getValue().entrySet().iterator();
            }
            final Map.Entry<C, V> columnEntry = columnIterator.next();
            return new SimpleCell<>(rowEntry.getKey(), columnEntry.getKey(), columnEntry.getValue());
        }

        @Override
        public void remove() {
            columnIterator.remove();
            if (rowEntry.getValue().isEmpty()) {
                rowIterator.remove();
            }
        }
    }

}
