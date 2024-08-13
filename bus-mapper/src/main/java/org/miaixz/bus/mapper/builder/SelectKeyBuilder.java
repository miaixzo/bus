/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.entity.EntityColumn;
import org.miaixz.bus.mapper.support.MetaObject;

/**
 * 查询信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SelectKeyBuilder {

    /**
     * 新建SelectKey节点
     *
     * @param ms            MappedStatement
     * @param column        列
     * @param entityClass   实体Class对象
     * @param executeBefore 执行策略
     * @param identity      自增信息
     */
    public static void newSelectKeyMappedStatement(MappedStatement ms, EntityColumn column, Class<?> entityClass,
            Boolean executeBefore, String identity) {
        String keyId = ms.getId() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        if (ms.getConfiguration().hasKeyGenerator(keyId)) {
            return;
        }
        Configuration configuration = ms.getConfiguration();
        KeyGenerator keyGenerator;
        String IDENTITY = (column.getGenerator() == null || Normal.EMPTY.equals(column.getGenerator())) ? identity
                : column.getGenerator();
        if ("JDBC".equalsIgnoreCase(IDENTITY)) {
            keyGenerator = new Jdbc3KeyGenerator();
        } else {
            SqlSource sqlSource = new RawSqlSource(configuration, IDENTITY, entityClass);

            MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, keyId, sqlSource,
                    SqlCommandType.SELECT);
            statementBuilder.resource(ms.getResource());
            statementBuilder.fetchSize(null);
            statementBuilder.statementType(StatementType.STATEMENT);
            statementBuilder.keyGenerator(new NoKeyGenerator());
            statementBuilder.keyProperty(column.getProperty());
            statementBuilder.keyColumn(null);
            statementBuilder.databaseId(null);
            statementBuilder.lang(configuration.getDefaultScriptingLanguageInstance());
            statementBuilder.resultOrdered(false);
            statementBuilder.resultSets(null);
            statementBuilder.timeout(configuration.getDefaultStatementTimeout());

            List<ParameterMapping> parameterMappings = new ArrayList<>();
            ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(configuration,
                    statementBuilder.id() + "-Inline", entityClass, parameterMappings);
            statementBuilder.parameterMap(inlineParameterMapBuilder.build());

            List<ResultMap> resultMaps = new ArrayList<>();
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration,
                    statementBuilder.id() + "-Inline", column.getJavaType(), new ArrayList<>(), null);
            resultMaps.add(inlineResultMapBuilder.build());
            statementBuilder.resultMaps(resultMaps);
            statementBuilder.resultSetType(null);

            statementBuilder.flushCacheRequired(false);
            statementBuilder.useCache(false);
            statementBuilder.cache(null);

            MappedStatement statement = statementBuilder.build();
            try {
                configuration.addMappedStatement(statement);
            } catch (Exception e) {
                // ignore
            }
            MappedStatement keyStatement = configuration.getMappedStatement(keyId, false);
            // 如果单独设置了 order，使用 column 提供的，否则使用全局的
            keyGenerator = new SelectKeyGenerator(keyStatement,
                    column.getOrder() != ORDER.DEFAULT ? (column.getOrder() == ORDER.BEFORE) : executeBefore);
            try {
                configuration.addKeyGenerator(keyId, keyGenerator);
            } catch (Exception e) {
                // ignore
            }
        }
        try {
            org.apache.ibatis.reflection.MetaObject msObject = MetaObject.forObject(ms);
            msObject.setValue("keyGenerator", keyGenerator);
            msObject.setValue("keyProperties", column.getTable().getKeyProperties());
            msObject.setValue("keyColumns", column.getTable().getKeyColumns());
        } catch (Exception e) {
            // ignore
        }
    }

}
