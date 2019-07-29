package org.aoju.bus.mapper.common;

/**
 * 通用Mapper接口,其他接口继承该接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Mapper<T> extends
        BasicMapper<T>,
        ConditionMapper<T>,
        IdsMapper<T>,
        RowBoundsMapper<T>,
        Marker {

}