package org.aoju.bus.mapper.common.rowbounds;

import org.aoju.bus.mapper.provider.BaseSelectProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 通用Mapper接口,查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SelectRowBoundsMapper<T> {

    /**
     * 根据实体属性和RowBounds进行分页查询
     *
     * @param record
     * @param rowBounds
     * @return
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "dynamicSQL")
    List<T> selectByRowBounds(T record, RowBounds rowBounds);

}