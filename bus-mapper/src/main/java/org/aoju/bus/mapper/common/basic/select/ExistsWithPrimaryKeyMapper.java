package org.aoju.bus.mapper.common.basic.select;

import org.aoju.bus.mapper.provider.BaseSelectProvider;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 通用Mapper接口,查询
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ExistsWithPrimaryKeyMapper<T> {

    /**
     * 根据主键字段查询总数，方法参数必须包含完整的主键属性，查询条件使用等号
     *
     * @param key
     * @return
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "dynamicSQL")
    boolean existsWithPrimaryKey(Object key);

}