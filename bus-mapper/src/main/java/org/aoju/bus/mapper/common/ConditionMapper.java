package org.aoju.bus.mapper.common;


import org.aoju.bus.mapper.common.condition.*;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ConditionMapper<T> extends
        SelectByConditionMapper<T>,
        SelectCountByConditionMapper<T>,
        DeleteByConditionMapper<T>,
        UpdateByConditionMapper<T>,
        UpdateByConditionSelectiveMapper<T> {

}