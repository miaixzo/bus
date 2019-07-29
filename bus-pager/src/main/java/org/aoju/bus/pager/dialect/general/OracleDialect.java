package org.aoju.bus.pager.dialect.general;

import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.dialect.AbstractHelperDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Map;

/**
 * 数据库方言 oracle
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class OracleDialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getEndRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
        //处理pageKey
        pageKey.update(page.getEndRow());
        pageKey.update(page.getStartRow());
        //处理参数配置
        handleParameter(boundSql, ms);
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        sqlBuilder.append("SELECT * FROM ( ");
        sqlBuilder.append(" SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) TMP_PAGE)");
        sqlBuilder.append(" WHERE ROW_ID <= ? AND ROW_ID > ?");
        return sqlBuilder.toString();
    }

}
