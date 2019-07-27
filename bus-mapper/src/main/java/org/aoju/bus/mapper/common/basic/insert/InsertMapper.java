package org.aoju.bus.mapper.common.basic.insert;

import org.aoju.bus.mapper.provider.BaseInsertProvider;
import org.apache.ibatis.annotations.InsertProvider;

/**
 * 通用Mapper接口,插入
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface InsertMapper<T> {

    /**
     * 保存一个实体，null的属性也会保存，不会使用数据库默认值
     *
     * @param record
     * @return
     */
    @InsertProvider(type = BaseInsertProvider.class, method = "dynamicSQL")
    int insert(T record);

}