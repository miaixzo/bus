package com.ukettle.basics.page.dialect;

import java.util.List;

import com.ukettle.basics.page.Sorting;


/**
 * 类似hibernate的Dialect,但只精简出分页部分
 *
 * @author Kimi Liu
 * @Date Aug 18, 2014
 * @Time 10:09:12
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface Dialect {

    /**
     * 数据库本身是否支持分页当前的分页查询方式
     * 如果数据库不支持的话，则不进行数据库分页
     *
     * @return true：支持当前的分页查询方式
     */
    public boolean limit();

    /**
     * 将sql转换为分页SQL，分别调用分页sql
     *
     * @param sql    SQL语句
     * @param offset 开始条数
     * @param limit  每页显示多少纪录条数
     * @return 分页查询的sql
     */
    public String getLimit(String sql, int offset, int limit);
    
    /**
     * 将sql转换为总记录数SQL
     * @param sql SQL语句
     * @return 总记录数的sql
     */
    public String getCount(String sql);
    /**
     * 将sql转换为带排序的SQL
     * @param sql SQL语句
     * @return 总记录数的sql
     */
    public String getSort(String sql, List<Sorting> sort);

}