package org.aoju.bus.mapper.common.condition;

import org.aoju.bus.mapper.provider.ConditionProvider;
import org.apache.ibatis.annotations.DeleteProvider;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface DeleteByConditionMapper<T> {

    /**
     * 根据Condition条件删除数据
     *
     * @param object
     * @return
     */
    @DeleteProvider(type = ConditionProvider.class, method = "dynamicSQL")
    int deleteByCondition(Object object);

    /**
     * 根据Condition条件删除数据
     *
     * @param object
     * @return
     */
    @DeleteProvider(type = ConditionProvider.class, method = "dynamicSQL")
    int deleteByWhere(Object object);
}