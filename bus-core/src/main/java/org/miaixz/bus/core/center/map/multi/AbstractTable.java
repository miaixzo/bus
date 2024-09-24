/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.map.multi;

import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 抽象{@link Table}接口实现 默认实现了：
 * <ul>
 * <li>{@link #equals(Object)}</li>
 * <li>{@link #hashCode()}</li>
 * <li>{@link #toString()}</li>
 * <li>{@link #values()}</li>
 * <li>{@link #cellSet()}</li>
 * <li>{@link #iterator()}</li>
 * </ul>
 *
 * @param <R> 行类型
 * @param <C> 列类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractTable<R, C, V> implements Table<R, C, V> {

    private Collection<V> values;
    private Set<Cell<R, C, V>> cellSet;

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof Table) {
            final Table<?, ?, ?> that = (Table<?, ?, ?>) object;
            return this.cellSet().equals(that.cellSet());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return cellSet().hashCode();
    }

    @Override
    public String toString() {
        return rowMap().toString();
    }

    @Override
    public Collection<V> values() {
        final Collection<V> result = values;
        return (result == null) ? values = new Values() : result;
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
        final Set<Cell<R, C, V>> result = cellSet;
        return (result == null) ? cellSet = new CellSet() : result;
    }

    @Override
    public Iterator<Cell<R, C, V>> iterator() {
        return new CellIterator();
    }

    /**
     * 简单{@link Cell} 实现
     *
     * @param <R> 行类型
     * @param <C> 列类型
     * @param <V> 值类型
     */
    private static class SimpleCell<R, C, V> implements Cell<R, C, V>, Serializable {
        private static final long serialVersionUID = -1L;

        private final R rowKey;
        private final C columnKey;
        private final V value;

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

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (object instanceof Cell) {
                final Cell<?, ?, ?> other = (Cell<?, ?, ?>) object;
                return ObjectKit.equals(rowKey, other.getRowKey()) && ObjectKit.equals(columnKey, other.getColumnKey())
                        && ObjectKit.equals(value, other.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowKey, columnKey, value);
        }

        @Override
        public String toString() {
            return Symbol.PARENTHESE_LEFT + rowKey + Symbol.COMMA + columnKey + ")=" + value;
        }
    }

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
     * 基于{@link Cell}的{@link Iterator}实现
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
