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
 * 处理实体类上的主键策略，自动配置主键生成逻辑。
 * <p>
 * 如果方法通过 MyBatis 注解（如 @Options 或 @SelectKey）配置了主键策略，将记录警告信息并跳过实体类上的主键策略配置。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KeySqlRegistry implements Registry {

    /**
     * 自定义主键策略，检查并应用实体类上的主键生成策略。
     *
     * @param entity  实体表信息
     * @param ms      MappedStatement 对象
     * @param context 提供者上下文，包含方法和接口信息
     * @throws RuntimeException 如果存在多个字段配置了主键策略
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
                    Logger.warn("Interface " + context.getMapperType().getName() + " method " + mapperMethod.getName()
                            + " uses @Options(useGeneratedKeys = true), ignoring entity primary key strategy");
                    return;
                }
            }
            if (mapperMethod.isAnnotationPresent(SelectKey.class)) {
                Logger.warn("Interface " + context.getMapperType().getName() + " method " + mapperMethod.getName()
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
     * 生成可执行 SQL 的 SelectKeyGenerator，用于通过 SQL 获取主键值。
     *
     * @param ms            MappedStatement 对象
     * @param column        主键字段
     * @param sql           主键生成 SQL
     * @param executeBefore 是否在插入前执行
     * @return 配置好的 SelectKeyGenerator
     */
    private KeyGenerator handleSelectKeyGenerator(MappedStatement ms, ColumnMeta column, String sql,
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
     * 创建参数映射配置。
     *
     * @param ms                 MappedStatement 对象
     * @param parameterTypeClass 参数类型
     * @param statementId        语句 ID
     * @return 参数映射对象
     */
    private ParameterMap getStatementParameterMap(MappedStatement ms, Class<?> parameterTypeClass, String statementId) {
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        ParameterMap parameterMap = new ParameterMap.Builder(ms.getConfiguration(), statementId + "-Inline",
                parameterTypeClass, parameterMappings).build();
        return parameterMap;
    }

    /**
     * 创建结果映射配置。
     *
     * @param ms          MappedStatement 对象
     * @param resultType  结果类型
     * @param statementId 语句 ID
     * @return 结果映射列表
     */
    private List<ResultMap> getStatementResultMaps(MappedStatement ms, Class<?> resultType, String statementId) {
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap inlineResultMap = new ResultMap.Builder(ms.getConfiguration(), statementId + "-Inline", resultType,
                new ArrayList<>(), null).build();
        resultMaps.add(inlineResultMap);
        return resultMaps;
    }

}