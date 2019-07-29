package org.aoju.bus.spring.mapper;


import org.aoju.bus.core.utils.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * mapper 扫描配置类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@AutoConfigureAfter(MybatisConfiguration.class)
public class MapperConfiguration implements EnvironmentAware {

    private String basePackage;

    @Override
    public void setEnvironment(Environment environment) {
        this.basePackage = StringUtils.replaceBlank(environment.getProperty("spring.mybatis.basePackage"));
    }

    @Bean
    public MapperClassScanner mapperScannerConfigurer() {
        MapperClassScanner mapperClassScanner = new MapperClassScanner();
        mapperClassScanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperClassScanner.setBasePackage(basePackage);
        return mapperClassScanner;
    }

}