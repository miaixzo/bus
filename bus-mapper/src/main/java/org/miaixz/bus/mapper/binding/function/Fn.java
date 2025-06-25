/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.binding.function;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.mapper.OGNL;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.support.ClassField;

/**
 * 方法引用工具接口，用于获取方法引用对应的字段和列信息
 *
 * @param <T> 实体类类型
 * @param <R> 返回值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Fn<T, R> extends Function<T, R>, Serializable {

    /**
     * 缓存方法引用与对应的列信息
     */
    Map<Fn<?, ?>, ColumnMeta> FN_COLUMN_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存方法引用与对应的字段信息
     */
    Map<Fn<?, ?>, ClassField> FN_CLASS_FIELD_MAP = new ConcurrentHashMap<>();

    /**
     * 创建包含指定字段的虚拟表，适用于基类或泛型基类场景
     *
     * @param entityClass 实体类类型
     * @param fns         方法引用数组
     * @param <E>         实体类型
     * @return 虚拟表对象
     */
    @SafeVarargs
    static <E> FnArray<E> of(Class<E> entityClass, Fn<E, Object>... fns) {
        return new FnArray<>(entityClass, fns);
    }

    /**
     * 创建包含指定字段的虚拟表
     *
     * @param fns 方法引用数组
     * @param <E> 实体类型
     * @return 虚拟表对象
     */
    @SafeVarargs
    static <E> FnArray<E> of(Fn<E, Object>... fns) {
        return of(null, fns);
    }

    /**
     * 创建包含指定列名的虚拟表
     *
     * @param entityClass 实体类类型
     * @param columnNames 列名数组
     * @param <E>         实体类型
     * @return 虚拟表对象
     */
    static <E> FnArray<E> of(Class<E> entityClass, String... columnNames) {
        TableMeta entityTable = MapperFactory.create(entityClass);
        Set<String> columnNameSet = Arrays.stream(columnNames).collect(Collectors.toSet());
        List<ColumnMeta> columns = entityTable.columns().stream()
                .filter(column -> columnNameSet.contains(column.property())).collect(Collectors.toList());
        return new FnArray<>(entityClass, entityTable.tableName(), columns);
    }

    /**
     * 指定实体类中的字段
     *
     * @param entityClass 实体类类型
     * @param field       方法引用
     * @param <T>         实体类型
     * @return 方法引用对象
     */
    static <T> Fn<T, Object> field(Class<T> entityClass, Fn<T, Object> field) {
        return field.in(entityClass);
    }

    /**
     * 通过字段名指定实体类中的字段
     *
     * @param entityClass 实体类类型
     * @param field       字段名
     * @param <T>         实体类型
     * @return 方法引用对象
     */
    static <T> Fn<T, Object> field(Class<T> entityClass, String field) {
        return new FnName<>(entityClass, field);
    }

    /**
     * 通过列名指定实体类中的列
     *
     * @param entityClass 实体类类型
     * @param column      列名
     * @param <T>         实体类型
     * @return 方法引用对象
     */
    static <T> Fn<T, Object> column(Class<T> entityClass, String column) {
        return new FnName<>(entityClass, column, true);
    }

    /**
     * 指定方法引用所属的实体类，适用于继承场景
     *
     * @param entityClass 实体类类型
     * @return 带有指定实体类的 Fn 对象
     */
    default Fn<T, R> in(Class<?> entityClass) {
        return new FnType<>(this, entityClass);
    }

    /**
     * 获取方法引用对应的字段名
     *
     * @return 字段名
     */
    default String toField() {
        return toClassField().getField();
    }

    /**
     * 获取方法引用对应的列名
     *
     * @return 列名
     */
    default String toColumn() {
        return toEntityColumn().column();
    }

    /**
     * 获取方法引用对应的字段信息
     *
     * @return 字段名及所属类信息
     */
    default ClassField toClassField() {
        return FN_CLASS_FIELD_MAP.computeIfAbsent(this, key -> OGNL.fnToFieldName(key));
    }

    /**
     * 获取方法引用对应的列信息
     *
     * @return 列信息对象
     */
    default ColumnMeta toEntityColumn() {
        return FN_COLUMN_MAP.computeIfAbsent(this, key -> {
            ClassField classField = toClassField();
            List<ColumnMeta> columns = MapperFactory.create(classField.getClazz()).columns();
            return columns.stream().filter(column -> column.property().equals(classField.getField())).findFirst()
                    .orElseGet(() -> columns.stream().filter(classField).findFirst()
                            .orElseThrow(() -> new RuntimeException(classField.getField()
                                    + " does not mark database column field annotations, unable to obtain column information")));
        });
    }

    /**
     * 带有指定实体类类型的方法引用
     *
     * @param <T> 实体类型
     * @param <R> 返回值类型
     */
    class FnType<T, R> implements Fn<T, R> {
        public final Fn<T, R> fn;
        public final Class<?> entityClass;

        /**
         * 构造函数，初始化方法引用和实体类
         *
         * @param fn          原始方法引用
         * @param entityClass 实体类类型
         */
        public FnType(Fn<T, R> fn, Class<?> entityClass) {
            this.fn = fn;
            this.entityClass = entityClass;
        }

        /**
         * 应用方法引用到指定对象
         *
         * @param t 输入对象
         * @return 方法引用执行结果
         */
        @Override
        public R apply(T t) {
            return fn.apply(t);
        }

        /**
         * 比较两个 FnType 对象是否相等
         *
         * @param o 比较对象
         * @return true 表示相等，false 表示不相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FnType<?, ?> fnType = (FnType<?, ?>) o;
            return Objects.equals(entityClass, fnType.entityClass) && Objects.equals(fn, fnType.fn);
        }

        /**
         * 计算对象的哈希值
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            return Objects.hash(entityClass, fn);
        }
    }

    /**
     * 支持直接指定字段名或列名的方法引用
     *
     * @param <T> 实体类型
     * @param <R> 返回值类型
     */
    class FnName<T, R> implements Fn<T, R> {

        public final Class<?> entityClass;
        public final String name;
        /**
         * 是否为列名，false 表示字段名，true 表示列名
         */
        public final boolean column;

        /**
         * 构造函数，指定字段名
         *
         * @param entityClass 实体类类型
         * @param name        字段名
         */
        public FnName(Class<?> entityClass, String name) {
            this(entityClass, name, false);
        }

        /**
         * 构造函数，指定字段名或列名
         *
         * @param entityClass 实体类类型
         * @param name        字段名或列名
         * @param column      是否为列名
         */
        public FnName(Class<?> entityClass, String name, boolean column) {
            this.entityClass = entityClass;
            this.name = name;
            this.column = column;
        }

        /**
         * 指定方法引用所属的实体类
         *
         * @param entityClass 实体类类型
         * @return 新的 FnName 对象
         */
        @Override
        public Fn<T, R> in(Class<?> entityClass) {
            return new FnName<>(entityClass, this.name, this.column);
        }

        /**
         * 应用方法引用到指定对象（占位实现，返回 null）
         *
         * @param o 输入对象
         * @return 始终返回 null
         */
        @Override
        public R apply(Object o) {
            return null;
        }

        /**
         * 比较两个 FnName 对象是否相等
         *
         * @param o 比较对象
         * @return true 表示相等，false 表示不相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FnName<?, ?> fnName = (FnName<?, ?>) o;
            return column == fnName.column && Objects.equals(entityClass, fnName.entityClass)
                    && Objects.equals(name, fnName.name);
        }

        /**
         * 计算对象的哈希值
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            return Objects.hash(entityClass, name, column);
        }
    }

    /**
     * 字段数组，表示部分字段的虚拟表
     *
     * @param <E> 实体类型
     */
    class FnArray<E> extends TableMeta {
        /**
         * 通过列信息构造虚拟表
         *
         * @param entityClass 实体类类型
         * @param table       表名
         * @param columns     列信息列表
         */
        private FnArray(Class<E> entityClass, String table, List<ColumnMeta> columns) {
            super(entityClass);
            this.table = table;
            this.columns = columns;
        }

        /**
         * 通过方法引用构造虚拟表
         *
         * @param entityClass 实体类类型
         * @param fns         方法引用数组
         */
        @SafeVarargs
        private FnArray(Class<E> entityClass, Fn<E, Object>... fns) {
            super(entityClass);
            this.columns = new ArrayList<>(fns.length);
            for (int i = 0; i < fns.length; i++) {
                if (entityClass != null) {
                    this.columns.add(fns[i].in(entityClass).toEntityColumn());
                } else {
                    this.columns.add(fns[i].toEntityColumn());
                }
                if (i == 0) {
                    TableMeta entityTable = this.columns.get(i).tableMeta();
                    this.table = entityTable.tableName();
                    this.style = entityTable.style();
                    this.entityClass = entityTable.entityClass();
                    this.resultMap = entityTable.resultMap();
                    this.autoResultMap = entityTable.autoResultMap();
                }
            }
        }

        /**
         * 判断虚拟表字段是否非空
         *
         * @return true 表示非空，false 表示空
         */
        public boolean isNotEmpty() {
            return !columns().isEmpty();
        }
    }

}