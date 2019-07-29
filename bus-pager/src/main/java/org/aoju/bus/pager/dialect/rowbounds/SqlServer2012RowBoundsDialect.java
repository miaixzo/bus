package org.aoju.bus.pager.dialect.rowbounds;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * sqlserver2012 基于 RowBounds 的分页
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SqlServer2012RowBoundsDialect extends SqlServerRowBoundsDialect {

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        sqlBuilder.append(" OFFSET ");
        sqlBuilder.append(rowBounds.getOffset());
        sqlBuilder.append(" ROWS ");
        pageKey.update(rowBounds.getOffset());
        sqlBuilder.append(" FETCH NEXT ");
        sqlBuilder.append(rowBounds.getLimit());
        sqlBuilder.append(" ROWS ONLY");
        pageKey.update(rowBounds.getLimit());
        return sqlBuilder.toString();
    }

}
