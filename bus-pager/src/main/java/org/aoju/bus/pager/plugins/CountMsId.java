package org.aoju.bus.pager.plugins;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

public interface CountMsId {

    /**
     * 默认实现
     */
    CountMsId DEFAULT = (ms, parameter, boundSql, countSuffix) -> ms.getId() + countSuffix;

    /**
     * 构建当前查询对应的 count 方法 id
     *
     * @param ms          查询对应的 MappedStatement
     * @param parameter   方法参数
     * @param boundSql    查询SQL
     * @param countSuffix 配置的 count 后缀
     * @return count 查询丢的 msId
     */
    String genCountMsId(MappedStatement ms,
                        Object parameter,
                        BoundSql boundSql,
                        String countSuffix);

}
