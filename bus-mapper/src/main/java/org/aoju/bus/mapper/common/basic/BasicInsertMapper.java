package org.aoju.bus.mapper.common.basic;

import org.aoju.bus.mapper.common.basic.insert.InsertListMapper;
import org.aoju.bus.mapper.common.basic.insert.InsertMapper;
import org.aoju.bus.mapper.common.basic.insert.InsertSelectiveMapper;

/**
 * 通用Mapper接口,基础查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BasicInsertMapper<T> extends
        InsertMapper<T>,
        InsertListMapper<T>,
        InsertSelectiveMapper<T> {

}