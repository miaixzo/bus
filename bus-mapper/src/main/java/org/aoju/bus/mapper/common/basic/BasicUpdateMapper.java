package org.aoju.bus.mapper.common.basic;

import org.aoju.bus.mapper.common.basic.update.UpdateByPrimaryKeyMapper;
import org.aoju.bus.mapper.common.basic.update.UpdateByPrimaryKeySelectiveMapper;

/**
 * 通用Mapper接口,基础查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BasicUpdateMapper<T> extends
        UpdateByPrimaryKeyMapper<T>,
        UpdateByPrimaryKeySelectiveMapper<T> {

}