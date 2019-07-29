package org.aoju.bus.mapper.entity;

/**
 * 实现动态表名时，实体类需要实现该接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface EntityTableName {

    /**
     * 获取动态表名 - 只要有返回值，不是null和''，就会用返回值作为表名
     */
    String getDynamicTableName();
}
