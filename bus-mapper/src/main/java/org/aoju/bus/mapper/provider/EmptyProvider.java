package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;

/**
 * 空方法Mapper接口默认MapperTemplate
 * <p/>
 * 如BaseSelectMapper，接口纯继承，不包含任何方法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EmptyProvider extends MapperTemplate {

    public EmptyProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    @Override
    public boolean supportMethod(String msId) {
        return false;
    }

}
