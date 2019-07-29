package org.aoju.bus.mapper.common.dialect;


import org.aoju.bus.mapper.common.dialect.sqlserver.InsertMapper;
import org.aoju.bus.mapper.common.dialect.sqlserver.InsertSelectiveMapper;

/**
 * 通用Mapper接口,SqlServerMapper独有的通用方法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SqlServerMapper<T> extends
        InsertMapper<T>,
        InsertSelectiveMapper<T> {

}