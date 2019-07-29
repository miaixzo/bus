package org.aoju.bus.mapper.common.basic;

import org.aoju.bus.mapper.common.basic.select.*;

/**
 * 通用Mapper接口,基础查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BasicSelectMapper<T> extends
        SelectOneMapper<T>,
        SelectMapper<T>,
        SelectAllMapper<T>,
        SelectCountMapper<T>,
        SelectByPrimaryKeyMapper<T>,
        ExistsWithPrimaryKeyMapper<T> {

}