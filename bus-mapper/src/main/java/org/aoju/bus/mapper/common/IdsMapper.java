package org.aoju.bus.mapper.common;

import org.aoju.bus.mapper.common.ids.DeleteByIdsMapper;
import org.aoju.bus.mapper.common.ids.SelectByIdsMapper;

/**
 * 通用Mapper接口,根据ids操作
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface IdsMapper<T> extends SelectByIdsMapper<T>, DeleteByIdsMapper<T> {

}
