package org.aoju.bus.mapper.common.condition;

import org.aoju.bus.mapper.provider.ConditionProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface UpdateByConditionMapper<T> {

    /**
     * 根据Condition条件更新实体`record`包含的全部属性，null值会被更新
     *
     * @param record
     * @param object
     * @return
     */
    @UpdateProvider(type = ConditionProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByCondition(@Param("record") T record, @Param("condition") Object object);

    /**
     * 根据Condition条件更新实体`record`包含的全部属性，null值会被更新
     *
     * @param record
     * @param object
     * @return
     */
    @UpdateProvider(type = ConditionProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByWhere(@Param("record") T record, @Param("condition") Object object);

}