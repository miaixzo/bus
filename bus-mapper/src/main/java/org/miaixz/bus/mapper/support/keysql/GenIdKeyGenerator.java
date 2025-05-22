/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.support.keysql;

import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 主键生成器，负责在插入操作前或后生成主键值。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenIdKeyGenerator implements KeyGenerator {

    /**
     * 并发度，默认 1000，限制首次并发插入时的最大并发数，确保 executeBefore=true 时不会漏生成主键。
     */
    private static volatile Integer CONCURRENCY;

    /**
     * 主键生成器接口实例
     */
    private final GenId<?> genId;

    /**
     * 实体表信息
     */
    private final TableMeta table;

    /**
     * 主键列信息
     */
    private final ColumnMeta column;

    /**
     * MyBatis 配置对象
     */
    private final Configuration configuration;

    /**
     * 是否在插入前生成主键
     */
    private final boolean executeBefore;

    /**
     * 计数器，记录生成主键的调用次数
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 构造函数，初始化主键生成器。
     *
     * @param genId         主键生成器接口实例
     * @param table         实体表信息
     * @param column        主键列信息
     * @param configuration MyBatis 配置对象
     * @param executeBefore 是否在插入前生成主键
     */
    public GenIdKeyGenerator(GenId<?> genId, TableMeta table, ColumnMeta column, Configuration configuration,
            boolean executeBefore) {
        this.genId = genId;
        this.table = table;
        this.column = column;
        this.configuration = configuration;
        this.executeBefore = executeBefore;
    }

    /**
     * 获取并发度，默认值为 1000，通过全局配置加载。
     *
     * @return 并发度值
     */
    private static int getConcurrency() {
        if (CONCURRENCY == null) {
            synchronized (GenIdKeyGenerator.class) {
                if (CONCURRENCY == null) {
                    CONCURRENCY = Context.INSTANCE.getInt(Args.CONCURRENCY_KEY, 1000);
                }
            }
        }
        return CONCURRENCY;
    }

    /**
     * 在 SQL 执行前处理主键生成（若配置为插入前生成）。
     *
     * @param executor  MyBatis 执行器
     * @param ms        MappedStatement 对象
     * @param stmt      JDBC 语句对象
     * @param parameter 参数对象
     */
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            genId(parameter);
        }
    }

    /**
     * 在 SQL 执行后处理主键生成（若配置为插入后生成）。
     *
     * @param executor  MyBatis 执行器
     * @param ms        MappedStatement 对象
     * @param stmt      JDBC 语句对象
     * @param parameter 参数对象
     */
    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            genId(parameter);
        }
    }

    /**
     * 为参数对象生成主键值，支持单对象、Map、Iterator 和 Iterable 类型。
     *
     * @param parameter 参数对象
     */
    public void genId(Object parameter) {
        if (parameter != null) {
            if (table.entityClass().isInstance(parameter)) {
                MetaObject metaObject = configuration.newMetaObject(parameter);
                if (metaObject.getValue(column.property()) == null) {
                    Object id = genId.genId(table, column);
                    metaObject.setValue(column.property(), id);
                }
            } else if (parameter instanceof Map) {
                new HashSet<>(((Map<String, Object>) parameter).values()).forEach(this::genId);
            } else if (parameter instanceof Iterator iterator) {
                Set<Object> set = new HashSet<>();
                while (iterator.hasNext()) {
                    set.add(iterator.next());
                }
                set.forEach(this::genId);
            } else if (parameter instanceof Iterable) {
                Set<Object> set = new HashSet<>();
                ((Iterable<?>) parameter).forEach(set::add);
                set.forEach(this::genId);
            }
        }
    }

    /**
     * 准备参数，当 executeBefore=true 时，确保在执行前生成主键。
     * <p>
     * 如果在 MappedStatement 初始化前调用此方法，可能未生成主键，此处补充生成。 初始化后，MyBatis 会通过 selectKey 自动调用，无需重复生成。
     * </p>
     * <p>
     * 使用并发度阈值 ({@link #CONCURRENCY}) 限制重复生成，超过阈值后不再检查。 仅当首次并发插入超过并发度时，可能漏生成主键。
     * </p>
     *
     * @param parameter 参数对象
     */
    public void prepare(Object parameter) {
        if (executeBefore) {
            if (count.get() < getConcurrency()) {
                count.incrementAndGet();
                genId(parameter);
            }
        }
    }

}