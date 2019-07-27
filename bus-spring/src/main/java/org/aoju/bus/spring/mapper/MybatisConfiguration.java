package org.aoju.bus.spring.mapper;


import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.pager.plugin.PageInterceptor;
import org.aoju.bus.spring.druid.DataSourceProperties;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * mybatis 配置类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {MybatisProperties.class, DataSourceProperties.class})
public class MybatisConfiguration {

    @Autowired
    MybatisProperties properties;

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        try {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSource);
            if (StringUtils.isNotBlank(this.properties.getTypeAliasesPackage())) {
                bean.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
            }
            PageInterceptor interceptor = new PageInterceptor();
            Properties properties = new Properties();
            properties.setProperty("autoDelimitKeywords", this.properties.getAutoDelimitKeywords());
            properties.setProperty("reasonable", this.properties.getReasonable());
            properties.setProperty("supportMethodsArguments", this.properties.getSupportMethodsArguments());
            properties.setProperty("returnPageInfo", this.properties.getReturnPageInfo());
            properties.setProperty("params", this.properties.getParams());
            interceptor.setProperties(properties);

            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Interceptor[] plugins = new Interceptor[]{
                    interceptor,
                    new PerformanceHandler(),
                    new SQLExplainHandler()};
            bean.setPlugins(plugins);

            bean.setMapperLocations(resolver.getResources(this.properties.getXmlLocation()));
            return bean.getObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}