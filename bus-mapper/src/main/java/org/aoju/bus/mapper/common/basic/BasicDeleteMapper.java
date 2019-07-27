package org.aoju.bus.mapper.common.basic;

import org.aoju.bus.mapper.common.basic.delete.DeleteByPrimaryKeyMapper;
import org.aoju.bus.mapper.common.basic.delete.DeleteMapper;

/**
 * 通用Mapper接口,基础删除
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BasicDeleteMapper<T> extends
        DeleteMapper<T>,
        DeleteByPrimaryKeyMapper<T> {

}