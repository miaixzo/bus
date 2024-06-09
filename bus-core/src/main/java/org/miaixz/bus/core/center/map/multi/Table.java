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

import org.miaixz.bus.core.center.function.SerConsumer3;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MapKit;

import java.util.*;

/**
 * 表格数据结构定义
 * 此结构类似于Guava的Table接口，使用两个键映射到一个值，类似于表格结构。
 *
 * @param <R> 行键类型
 * @param <C> 列键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Table<R, C, V> extends Iterable<Table.Cell<R, C, V>> {

    /**
     * 是否包含指定行列的映射
     * 行和列任意一个不存在都会返回{@code false}，如果行和列都存在，值为{@code null}，也会返回{@code true}
     *
     * @param rowKey    行键
     * @param columnKey 列键
     * @return 是否包含映射
     */
    default boolean contains(final R rowKey, final C columnKey) {
        return Optional.ofNullable(getRow(rowKey)).map((map) -> map.containsKey(columnKey))
                .orElse(false);
    }

    /**
     * 行是否存在
     *
     * @param rowKey 行键
     * @return 行是否存在
     */
    default boolean containsRow(final R rowKey) {
        return Optional.ofNullable(rowMap()).map((map) -> map.containsKey(rowKey)).get();
    }

    /**
     * 获取行
     *
     * @param rowKey 行键
     * @return 行映射，返回的键为列键，值为表格的值
     */
    default Map<C, V> getRow(final R rowKey) {
        return Optional.ofNullable(rowMap()).map((map) -> map.get(rowKey)).get();
    }

    /**
     * 返回所有行的key，行的key不可重复
     *
     * @return 行键
     */
    default Set<R> rowKeySet() {
        return Optional.ofNullable(rowMap()).map(Map::keySet).get();
    }

    /**
     * 返回行列对应的Map
     *
     * @return map，键为行键，值为列和值的对应map
     */
    Map<R, Map<C, V>> rowMap();

    /**
     * 列是否存在
     *
     * @param columnKey 列键
     * @return 列是否存在
     */
    default boolean containsColumn(final C columnKey) {
        return Optional.ofNullable(columnMap()).map((map) -> map.containsKey(columnKey)).get();
    }

    /**
     * 获取列
     *
     * @param columnKey 列键
     * @return 列映射，返回的键为行键，值为表格的值
     */
    default Map<R, V> getColumn(final C columnKey) {
        return Optional.ofNullable(columnMap()).map((map) -> map.get(columnKey)).get();
    }

    /**
     * 返回所有列的key，列的key不可重复
     *
     * @return 列set
     */
    default Set<C> columnKeySet() {
        return Optional.ofNullable(columnMap()).map(Map::keySet).get();
    }

    /**
     * 返回所有列的key，列的key如果实现Map是可重复key，则返回对应不去重的List。
     *
     * @return 列set
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
     * 返回列-行对应的map
     *
     * @return map，键为列键，值为行和值的对应map
     */
    Map<C, Map<R, V>> columnMap();

    /**
     * 指定值是否存在
     *
     * @param value 值
     * @return 值
     */
    default boolean containsValue(final V value) {
        final Collection<Map<C, V>> rows = Optional.ofNullable(rowMap()).map(Map::values).get();
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
     * 获取指定值
     *
     * @param rowKey    行键
     * @param columnKey 列键
     * @return 值，如果值不存在，返回{@code null}
     */
    default V get(final R rowKey, final C columnKey) {
        return Optional.ofNullable(getRow(rowKey)).map((map) -> map.get(columnKey)).get();
    }

    /**
     * 所有行列值的集合
     *
     * @return 值的集合
     */
    Collection<V> values();

    /**
     * 所有单元格集合
     *
     * @return 单元格集合
     */
    Set<Cell<R, C, V>> cellSet();

    /**
     * 为表格指定行列赋值，如果不存在，创建之，存在则替换之，返回原值
     *
     * @param rowKey    行键
     * @param columnKey 列键
     * @param value     值
     * @return 原值，不存在返回{@code null}
     */
    V put(R rowKey, C columnKey, V value);

    /**
     * 批量加入
     *
     * @param table 其他table
     */
    default void putAll(final Table<? extends R, ? extends C, ? extends V> table) {
        if (null != table) {
            for (final Table.Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
        }
    }

    /**
     * 移除指定值
     *
     * @param rowKey    行键
     * @param columnKey 列键
     * @return 移除的值，如果值不存在，返回{@code null}
     */
    V remove(R rowKey, C columnKey);

    /**
     * 表格是否为空
     *
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 表格大小，一般为单元格的个数
     *
     * @return 表格大小
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
     * 清空表格
     */
    void clear();

    /**
     * 遍历表格的单元格，处理值
     *
     * @param consumer 单元格值处理器
     */
    default void forEach(final SerConsumer3<? super R, ? super C, ? super V> consumer) {
        for (final Cell<R, C, V> cell : this) {
            consumer.accept(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    /**
     * 单元格，用于表示一个单元格的行、列和值
     *
     * @param <R> 行键类型
     * @param <C> 列键类型
     * @param <V> 值类型
     */
    interface Cell<R, C, V> {
        /**
         * 获取行键
         *
         * @return 行键
         */
        R getRowKey();

        /**
         * 获取列键
         *
         * @return 列键
         */
        C getColumnKey();

        /**
         * 获取值
         *
         * @return 值
         */
        V getValue();
    }

}
