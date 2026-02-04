/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.support.keygen;

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
import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A primary key generator responsible for generating primary key values before or after an insert operation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenIdKeyGenerator implements KeyGenerator {

    /**
     * Concurrency level, defaults to 1000. Limits the maximum concurrency during the first insertion, ensuring that
     * primary keys are not missed when {@code executeBefore=true}.
     */
    private static volatile Integer CONCURRENCY;

    /**
     * The {@link GenId} interface instance for primary key generation.
     */
    private final GenId<?> genId;

    /**
     * The entity table information.
     */
    private final TableMeta table;

    /**
     * The primary key column information.
     */
    private final ColumnMeta column;

    /**
     * The MyBatis configuration object.
     */
    private final Configuration configuration;

    /**
     * Whether to generate the primary key before insertion.
     */
    private final boolean executeBefore;

    /**
     * A counter to track the number of times primary keys have been generated.
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * Constructs a new primary key generator.
     *
     * @param genId         The {@link GenId} interface instance.
     * @param table         The entity table information.
     * @param column        The primary key column information.
     * @param configuration The MyBatis configuration object.
     * @param executeBefore Whether to generate the primary key before insertion.
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
     * Gets the concurrency level. Defaults to 1000, loaded from global configuration.
     *
     * @return The concurrency level value.
     */
    private static int getConcurrency() {
        if (CONCURRENCY == null) {
            synchronized (GenIdKeyGenerator.class) {
                if (CONCURRENCY == null) {
                    CONCURRENCY = Context.INSTANCE.getInt(Args.PROVIDER_KEY + Symbol.DOT + Args.CONCURRENCY_KEY, 1000);
                }
            }
        }
        return CONCURRENCY;
    }

    /**
     * Processes primary key generation before SQL execution (if configured for pre-insertion generation).
     *
     * @param executor  The MyBatis executor.
     * @param ms        The MappedStatement object.
     * @param stmt      The JDBC Statement object.
     * @param parameter The parameter object.
     */
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            genId(parameter);
        }
    }

    /**
     * Processes primary key generation after SQL execution (if configured for post-insertion generation).
     *
     * @param executor  The MyBatis executor.
     * @param ms        The MappedStatement object.
     * @param stmt      The JDBC Statement object.
     * @param parameter The parameter object.
     */
    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            genId(parameter);
        }
    }

    /**
     * Generates primary key values for the parameter object, supporting single objects, Maps, Iterators, and Iterables.
     *
     * @param parameter The parameter object.
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
     * Prepares parameters. If {@code executeBefore=true}, ensures primary keys are generated before execution.
     * <p>
     * If this method is called before MappedStatement initialization, primary keys might not have been generated yet,
     * so they are generated here. After initialization, MyBatis will automatically call this via selectKey, so no
     * duplicate generation is needed.
     * </p>
     * <p>
     * A concurrency threshold ({@link #CONCURRENCY}) is used to limit duplicate generation. Beyond this threshold, no
     * further checks are performed. Primary keys might only be missed if the first concurrent insertion exceeds the
     * concurrency threshold.
     * </p>
     *
     * @param parameter The parameter object.
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
