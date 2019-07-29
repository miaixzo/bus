package org.aoju.bus.mapper.common.condition;

import org.aoju.bus.mapper.provider.ConditionProvider;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SelectCountByConditionMapper<T> {

    /**
     * 根据Condition条件进行查询总数
     *
     * @param object
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    int selectCountByCondition(Object object);

    /**
     * 根据Condition条件进行查询总数
     *
     * @param object
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    int selectCountByWhere(Object object);
}