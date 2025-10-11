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
package org.miaixz.bus.mapper.support.keysql;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.Registry;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Handles primary key strategies on entity classes, automatically configuring primary key generation logic.
 * <p>
 * If a method has primary key strategies configured via MyBatis annotations (e.g., {@code @Options} or
 * {@code @SelectKey}), a warning will be logged, and the entity class's primary key strategy configuration will be
 * skipped.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KeySqlRegistry implements Registry {

    /**
     * Customizes the primary key strategy, checking and applying primary key generation logic on entity classes.
     *
     * @param entity  The entity table information.
     * @param ms      The {@link MappedStatement} object.
     * @param context The provider context, containing method and interface information.
     * @throws RuntimeException if multiple fields are configured with a primary key strategy.
     */
    @Override
    public void customize(TableMeta entity, MappedStatement ms, ProviderContext context) {
        Method mapperMethod = context.getMapperMethod();
        if (mapperMethod.isAnnotationPresent(InsertProvider.class)) {
            List<ColumnMeta> ids = entity.idColumns().stream().filter(ColumnMeta::hasPrimaryKeyStrategy)
                    .collect(Collectors.toList());
            if (ids.size() > 1) {
                throw new RuntimeException("Only one field can be configured with a primary key strategy");
            }
            if (ids.isEmpty()) {
                return;
            }
            if (mapperMethod.isAnnotationPresent(Options.class)) {
                Options options = mapperMethod.getAnnotation(Options.class);
                if (options.useGeneratedKeys()) {
                    Logger.warn(
                            "Interface " + context.getMapperType().getName() + " method " + mapperMethod.getName()
                                    + " uses @Options(useGeneratedKeys = true), ignoring entity primary key strategy");
                    return;
                }
            }
            if (mapperMethod.isAnnotationPresent(SelectKey.class)) {
                Logger.warn(
                        "Interface " + context.getMapperType().getName() + " method " + mapperMethod.getName()
                                + " uses @SelectKey, ignoring entity primary key strategy");
                return;
            }
            ColumnMeta id = ids.get(0);
            if (id.useGeneratedKeys()) {
                MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
                metaObject.setValue("keyGenerator", Jdbc3KeyGenerator.INSTANCE);
                metaObject.setValue("keyProperties", new String[] { id.property() });
            } else if (id.afterSql() != null && !id.afterSql().isEmpty()) {
                KeyGenerator keyGenerator = handleSelectKeyGenerator(ms, id, id.afterSql(), false);
                MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
                metaObject.setValue("keyGenerator", keyGenerator);
                metaObject.setValue("keyProperties", new String[] { id.property() });
            } else if (id.genId() != null && id.genId() != GenId.NULL.class) {
                Class<? extends GenId> genIdClass = id.genId();
                boolean executeBefore = id.genIdExecuteBefore();
                GenId<?> genId;
                try {
                    genId = genIdClass.getConstructor(new Class[] {}).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                KeyGenerator keyGenerator = new GenIdKeyGenerator(genId, entity, id, ms.getConfiguration(),
                        executeBefore);
                MetaObject metaObject = ms.getConfiguration().newMetaObject(ms);
                metaObject.setValue("keyGenerator", keyGenerator);
                metaObject.setValue("keyProperties", new String[] { id.property() });
            }
        }
    }

    /**
     * Generates a {@link SelectKeyGenerator} that executes SQL to obtain the primary key value.
     *
     * @param ms            The {@link MappedStatement} object.
     * @param column        The primary key column.
     * @param sql           The SQL for primary key generation.
     * @param executeBefore Whether to execute before insertion.
     * @return The configured {@link SelectKeyGenerator}.
     */
    private KeyGenerator handleSelectKeyGenerator(
            MappedStatement ms,
            ColumnMeta column,
            String sql,
            boolean executeBefore) {
        String id = ms.getId() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Configuration configuration = ms.getConfiguration();
        LanguageDriver languageDriver = configuration.getLanguageDriver(Caching.class);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, ms.getParameterMap().getType());

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource,
                SqlCommandType.SELECT).resource(ms.getResource()).fetchSize(null).timeout(null)
                        .statementType(StatementType.PREPARED).keyGenerator(NoKeyGenerator.INSTANCE)
                        .keyProperty(column.property()).keyColumn(column.column()).databaseId(null).lang(languageDriver)
                        .resultOrdered(false).resultSets(null)
                        .resultMaps(getStatementResultMaps(ms, column.javaType(), id)).resultSetType(null)
                        .flushCacheRequired(false).useCache(false).cache(null);
        ParameterMap statementParameterMap = getStatementParameterMap(ms, ms.getParameterMap().getType(), id);
        if (statementParameterMap != null) {
            statementBuilder.parameterMap(statementParameterMap);
        }

        MappedStatement statement = statementBuilder.build();
        configuration.addMappedStatement(statement);

        SelectKeyGenerator keyGenerator = new SelectKeyGenerator(statement, executeBefore);
        configuration.addKeyGenerator(id, keyGenerator);
        return keyGenerator;
    }

    /**
     * Creates a parameter mapping configuration.
     *
     * @param ms                 The {@link MappedStatement} object.
     * @param parameterTypeClass The parameter type.
     * @param statementId        The statement ID.
     * @return The parameter map object.
     */
    private ParameterMap getStatementParameterMap(MappedStatement ms, Class<?> parameterTypeClass, String statementId) {
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        ParameterMap parameterMap = new ParameterMap.Builder(ms.getConfiguration(), statementId + "-Inline",
                parameterTypeClass, parameterMappings).build();
        return parameterMap;
    }

    /**
     * Creates a result mapping configuration.
     *
     * @param ms          The {@link MappedStatement} object.
     * @param resultType  The result type.
     * @param statementId The statement ID.
     * @return The list of result maps.
     */
    private List<ResultMap> getStatementResultMaps(MappedStatement ms, Class<?> resultType, String statementId) {
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap inlineResultMap = new ResultMap.Builder(ms.getConfiguration(), statementId + "-Inline", resultType,
                new ArrayList<>(), null).build();
        resultMaps.add(inlineResultMap);
        return resultMaps;
    }

}
