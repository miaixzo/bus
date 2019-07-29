package org.aoju.bus.mapper.common;

import org.aoju.bus.mapper.common.rowbounds.SelectRowBoundsMapper;

/**
 * 通用Mapper接口,带RowBounds参数的查询
 * <p/>
 * 配合分页插件可以实现物理分页
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface RowBoundsMapper<T> extends
        SelectRowBoundsMapper<T> {

}