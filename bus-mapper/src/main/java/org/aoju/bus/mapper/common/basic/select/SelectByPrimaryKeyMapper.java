package org.aoju.bus.mapper.common.basic.select;

import org.aoju.bus.mapper.provider.BaseSelectProvider;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 通用Mapper接口,其他接口继承该接口即可
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface SelectByPrimaryKeyMapper<T> {

    /**
     * 根据主键字段进行查询，方法参数必须包含完整的主键属性，查询条件使用等号
     *
     * @param key
     * @return
     */
    @SelectProvider(type = BaseSelectProvider.class, method = "dynamicSQL")
    T selectByPrimaryKey(Object key);

}