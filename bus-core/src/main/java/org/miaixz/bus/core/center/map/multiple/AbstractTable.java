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
 * concrete subclasses. Cell traversal skips empty rows and returns immutable snapshots of individual cells; the cell
 * and value collections themselves are backed views. No synchronization is added around the subclass's storage.
 *
 * @param <R> The type of the row key.
 * @param <C> The type of the column key.
 * @param <V> The type of the value stored in the table.
 * @author Kimi Liu
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
     * Creates a new {@code AbstractTable} instance.
     */
    public AbstractTable() {
        // No initialization required.
    }

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
     * so table changes are reflected in subsequent traversals. Supported removals and clear operations write through to
     * the table; adding values through this view is unsupported.
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
     * {@link Table.Cell}. The set is backed by the table: supported removals and clear operations write through, while
     * additions are unsupported. Each returned cell is an immutable snapshot rather than a write-through entry.
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
     * @author Kimi Liu
     */
    private static class SimpleCell<R, C, V> implements Cell<R, C, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 2535915816251L;

        /**
         * Row key represented by this cell.
         */
        private final R rowKey;

        /**
         * Column key represented by this cell.
         */
        private final C columnKey;

        /**
         * Value stored at the row and column intersection.
         */
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

        /**
         * Returns this snapshot's row key.
         *
         * @return the captured row key
         */
        @Override
        public R getRowKey() {
            return rowKey;
        }

        /**
         * Returns this snapshot's column key.
         *
         * @return the captured column key
         */
        @Override
        public C getColumnKey() {
            return columnKey;
        }

        /**
         * Returns the value captured when this cell was created.
         *
         * @return the captured value, possibly null
         */
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
     *
     * @author Kimi Liu
     */
    private class Values extends AbstractCollection<V> {

        /**
         * Projects the table's cell iterator to values while retaining its removal behavior.
         *
         * @return an iterator over the current cells' values
         */
        @Override
        public Iterator<V> iterator() {
            return new TransIterator<>(cellSet().iterator(), Cell::getValue);
        }

        /**
         * Tests whether the table contains the given value.
         *
         * @param o the candidate value
         * @return whether any table cell contains the candidate
         */
        @Override
        public boolean contains(final Object o) {
            return containsValue((V) o);
        }

        /**
         * Removes all of the elements from this collection.
         */
        @Override
        public void clear() {
            AbstractTable.this.clear();
        }

        /**
         * Returns the number of elements in this collection.
         *
         * @return the number of elements
         */
        @Override
        public int size() {
            return AbstractTable.this.size();
        }

    }

    /**
     * A {@link Set} view of the cells contained in the table.
     *
     * @author Kimi Liu
     */
    private class CellSet extends AbstractSet<Cell<R, C, V>> {

        /**
         * Tests whether the candidate cell's coordinates exist with an equal value. A stored null value is distinct
         * from a missing coordinate.
         *
         * @param o the candidate cell
         * @return whether the table contains an equal cell
         */
        @Override
        public boolean contains(final Object o) {
            if (o instanceof Cell) {
                final Cell<R, C, V> cell = (Cell<R, C, V>) o;
                final Map<C, V> row = getRow(cell.getRowKey());
                if (null != row) {
                    return row.containsKey(cell.getColumnKey())
                            && ObjectKit.equals(row.get(cell.getColumnKey()), cell.getValue());
                }
            }
            return false;
        }

        /**
         * Removes the candidate cell when its coordinates and value match a cell in the table.
         *
         * @param o the candidate cell
         * @return whether a matching cell was found and removed
         */
        @Override
        public boolean remove(final Object o) {
            if (contains(o)) {
                final Cell<R, C, V> cell = (Cell<R, C, V>) o;
                AbstractTable.this.remove(cell.getRowKey(), cell.getColumnKey());
                return true;
            }
            return false;
        }

        /**
         * Removes all of the elements from this collection.
         */
        @Override
        public void clear() {
            AbstractTable.this.clear();
        }

        /**
         * Returns a table-cell iterator with write-through removal and immutable cell snapshots.
         *
         * @return an iterator over the current table cells
         */
        @Override
        public Iterator<Table.Cell<R, C, V>> iterator() {
            return new AbstractTable<R, C, V>.CellIterator();
        }

        /**
         * Returns the number of elements in this collection.
         *
         * @return the number of elements
         */
        @Override
        public int size() {
            return AbstractTable.this.size();
        }

    }

    /**
     * Traverses non-empty rows and their cells. Lookahead may move to a subsequent row without changing the last cell
     * eligible for removal. An emptied row is removed from the outer map only while its row iterator position is still
     * current; removal after cross-row lookahead can leave an empty row mapping behind.
     *
     * @author Kimi Liu
     */
    private class CellIterator implements Iterator<Cell<R, C, V>> {

        /**
         * Iterator over table rows.
         */
        final Iterator<Map.Entry<R, Map<C, V>>> rowIterator = rowMap().entrySet().iterator();

        /**
         * Row currently being traversed.
         */
        Map.Entry<R, Map<C, V>> rowEntry;

        /**
         * Iterator over columns in the current row.
         */
        Iterator<Map.Entry<C, V>> columnIterator = IteratorKit.empty();

        /**
         * Column iterator that produced the last returned cell, independent of hasNext moving to another row.
         */
        Iterator<Map.Entry<C, V>> lastReturnedIterator;

        /**
         * Searches past empty rows for another cell without changing which returned cell can be removed.
         *
         * @return true if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            while (!columnIterator.hasNext() && rowIterator.hasNext()) {
                rowEntry = rowIterator.next();
                columnIterator = rowEntry.getValue().entrySet().iterator();
            }
            return columnIterator.hasNext();
        }

        /**
         * Returns an immutable snapshot of the next cell.
         *
         * @return the next cell snapshot
         * @throws NoSuchElementException if no cell remains
         */
        @Override
        public Cell<R, C, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final Map.Entry<C, V> columnEntry = columnIterator.next();
            lastReturnedIterator = columnIterator;
            return new SimpleCell<>(rowEntry.getKey(), columnEntry.getKey(), columnEntry.getValue());
        }

        /**
         * Removes the last returned cell from the row iterator that produced it. An emptied current row is also
         * removed; an older row reached before cross-row lookahead is left structurally intact.
         *
         * @throws IllegalStateException         if no cell has been returned since construction or the previous removal
         * @throws UnsupportedOperationException if the underlying row or column iterator does not support removal
         */
        @Override
        public void remove() {
            if (lastReturnedIterator == null) {
                throw new IllegalStateException();
            }
            final Iterator<Map.Entry<C, V>> removedFrom = lastReturnedIterator;
            removedFrom.remove();
            lastReturnedIterator = null;
            // Remove an empty current row only while rowIterator still owns it. Lookahead may have advanced the row.
            if (removedFrom == columnIterator && rowEntry.getValue().isEmpty()) {
                rowIterator.remove();
            }
        }

    }

}
