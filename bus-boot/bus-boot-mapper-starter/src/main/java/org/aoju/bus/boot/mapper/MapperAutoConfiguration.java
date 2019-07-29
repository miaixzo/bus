package org.aoju.bus.boot.mapper;

import org.aoju.bus.spring.mapper.MapperConfiguration;
import org.aoju.bus.spring.mapper.MybatisConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Mapper 自动配置
 */
@Configuration
@Import(value = {MybatisConfiguration.class, MapperConfiguration.class})
public class MapperAutoConfiguration {

}
