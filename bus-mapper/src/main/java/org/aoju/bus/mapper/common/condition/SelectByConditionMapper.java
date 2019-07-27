package org.aoju.bus.mapper.common.condition;

import org.aoju.bus.mapper.provider.ConditionProvider;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SelectByConditionMapper<T> {

    /**
     * 根据Condition条件进行查询
     *
     * @param object
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    List<T> selectByCondition(Object object);

    /**
     * 根据Condition条件进行查询
     *
     * @param object
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    List<T> selectByWhere(Object object);

    /**
     * 根据Condition条件进行查询
     *
     * @param object
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    T selectOneByWhere(Object object);
}