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
public interface UpdateByConditionSelectiveMapper<T> {

    /**
     * 根据Condition条件更新实体`record`包含的不是null的属性值
     *
     * @param record
     * @param object
     * @return
     */
    @UpdateProvider(type = ConditionProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByConditionSelective(@Param("record") T record, @Param("condition") Object object);

    /**
     * 根据Condition条件更新实体`record`包含的不是null的属性值
     *
     * @param record
     * @param object
     * @return
     */
    @UpdateProvider(type = ConditionProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByWhereSelective(@Param("record") T record, @Param("condition") Object object);
}