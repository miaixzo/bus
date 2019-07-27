package org.aoju.bus.mapper.common;

import org.aoju.bus.mapper.common.basic.BasicDeleteMapper;
import org.aoju.bus.mapper.common.basic.BasicInsertMapper;
import org.aoju.bus.mapper.common.basic.BasicSelectMapper;
import org.aoju.bus.mapper.common.basic.BasicUpdateMapper;

/**
 * 通用Mapper接口,其他接口继承该接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BasicMapper<T> extends
        BasicSelectMapper<T>,
        BasicInsertMapper<T>,
        BasicUpdateMapper<T>,
        BasicDeleteMapper<T> {

}