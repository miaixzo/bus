package org.aoju.bus.pager.dialect.rowbounds;

import org.aoju.bus.pager.dialect.AbstractRowBoundsDialect;
import org.aoju.bus.pager.dialect.ReplaceSql;
import org.aoju.bus.pager.dialect.replace.RegexWithNolockReplaceSql;
import org.aoju.bus.pager.dialect.replace.SimpleWithNolockReplaceSql;
import org.aoju.bus.pager.parser.SqlServerParser;
import org.aoju.bus.pager.plugin.PageFromObject;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * sqlserver 基于 RowBounds 的分页
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SqlServerRowBoundsDialect extends AbstractRowBoundsDialect {
    protected SqlServerParser pageSql = new SqlServerParser();
    protected ReplaceSql replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        sql = replaceSql.replace(sql);
        sql = countSqlParser.getSmartCountSql(sql);
        sql = replaceSql.restore(sql);
        return sql;
    }

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(rowBounds.getOffset());
        pageKey.update(rowBounds.getLimit());
        sql = replaceSql.replace(sql);
        sql = pageSql.convertToPageSql(sql, null, null);
        sql = replaceSql.restore(sql);
        sql = sql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(rowBounds.getOffset()));
        sql = sql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(rowBounds.getLimit()));
        return sql;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String replaceSql = properties.getProperty("replaceSql");
        if (PageFromObject.isEmpty(replaceSql) || "simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolockReplaceSql();
        } else if ("regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolockReplaceSql();
        } else {
            try {
                this.replaceSql = (ReplaceSql) Class.forName(replaceSql).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("replaceSql 参数配置的值不符合要求，可选值为 simple 和 regex，或者是实现了 "
                        + ReplaceSql.class.getCanonicalName() + " 接口的全限定类名", e);
            }
        }
    }

}
