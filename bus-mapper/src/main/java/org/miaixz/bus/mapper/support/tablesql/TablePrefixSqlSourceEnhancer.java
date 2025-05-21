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
package org.miaixz.bus.mapper.support.tablesql;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.parsing.SqlSourceEnhancer;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 支持表名前缀的 SqlSourceEnhancer 实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TablePrefixSqlSourceEnhancer implements SqlSourceEnhancer {

    /**
     * 从 StaticSqlSource 获取 SQL 语句
     *
     * @param sqlSource StaticSqlSource 实例
     * @return SQL 字符串
     */
    public static String getSqlFromStaticSqlSource(StaticSqlSource sqlSource) {
        try {
            Field sqlField = StaticSqlSource.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            return (String) sqlField.get(sqlSource);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access SQL from StaticSqlSource", e);
        }
    }

    /**
     * 从 TableMeta 或上下文推导表名
     *
     * @param entity  实体表信息
     * @param context 调用方法上下文
     * @return 表名或 null
     */
    public static String getTableNameFromEntity(TableMeta entity, ProviderContext context) {
        if (entity == null) {
            return null;
        }
        // 假设 TableMeta 有字段 tableName
        try {
            Field tableNameField = TableMeta.class.getDeclaredField("tableName");
            tableNameField.setAccessible(true);
            return (String) tableNameField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 回退：从 ProviderContext 推导表名
            String className = context.getMapperType().getSimpleName();
            return className.replace("Mapper", Normal.EMPTY).toLowerCase();
        }
    }

    /**
     * 获取表前缀
     *
     * @param ms      MappedStatement
     * @param context 调用方法上下文
     * @return 表前缀
     */
    public static String getTablePrefix(MappedStatement ms, ProviderContext context) {
        // 优先从 MyBatis 配置属性获取
        String prefix = ms.getConfiguration().getVariables().getProperty("tablePrefix");
        if (prefix != null && !prefix.isEmpty()) {
            return prefix;
        }
        // 回退到默认前缀
        return Normal.EMPTY;
    }

    /**
     * 为 SQL 中的表名添加前缀
     *
     * @param sql       原始 SQL 语句
     * @param tableName 表名
     * @param ms        MappedStatement
     * @param context   调用方法上下文
     * @return 添加前缀后的 SQL 语句
     */
    public static String addTablePrefix(String sql, String tableName, MappedStatement ms, ProviderContext context) {
        String prefix = getTablePrefix(ms, context);
        String prefixedTableName = prefix + tableName;

        // 使用正则表达式替换表名，确保单词边界
        Pattern tableNamePattern = Pattern.compile("\\b" + Pattern.quote(tableName) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = tableNamePattern.matcher(sql);
        StringBuilder modifiedSql = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            modifiedSql.append(sql, lastEnd, matcher.start());
            modifiedSql.append(prefixedTableName);
            lastEnd = matcher.end();
        }
        modifiedSql.append(sql.substring(lastEnd));
        return modifiedSql.toString();
    }

    /**
     * 定制化处理 SqlSource，为表名添加前缀
     *
     * @param sqlSource 原始 SqlSource
     * @param entity    实体表信息
     * @param ms        MappedStatement
     * @param context   调用方法上下文
     * @return 添加前缀后的 SqlSource
     */
    @Override
    public SqlSource customize(SqlSource sqlSource, TableMeta entity, MappedStatement ms, ProviderContext context) {
        if (sqlSource instanceof StaticSqlSource) {
            StaticSqlSource staticSqlSource = (StaticSqlSource) sqlSource;
            String sql = getSqlFromStaticSqlSource(staticSqlSource);
            String tableName = getTableNameFromEntity(entity, context);
            if (tableName != null) {
                String prefixedSql = addTablePrefix(sql, tableName, ms, context);
                return new StaticSqlSource(ms.getConfiguration(), prefixedSql);
            }
        }
        return sqlSource; // 非 StaticSqlSource 或无表名直接返回
    }

}