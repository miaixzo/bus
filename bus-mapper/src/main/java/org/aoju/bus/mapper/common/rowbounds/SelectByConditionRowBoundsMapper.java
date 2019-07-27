package org.aoju.bus.mapper.common.rowbounds;

import org.aoju.bus.mapper.provider.ConditionProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 通用Mapper接口,Condition查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SelectByConditionRowBoundsMapper<T> {

    /**
     * 根据Condition条件和RowBounds进行分页查询，该方法和selectByWhereAndRowBounds完全一样，只是名字改成了Condition
     *
     * @param condition
     * @param rowBounds
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    List<T> selectByConditionAndRowBounds(Object condition, RowBounds rowBounds);

    /**
     * 根据Condition条件更新实体和RowBounds进行分页查询
     *
     * @param object
     * @param rowBounds
     * @return
     */
    @SelectProvider(type = ConditionProvider.class, method = "dynamicSQL")
    List<T> selectByWhereAndRowBounds(Object object, RowBounds rowBounds);

}