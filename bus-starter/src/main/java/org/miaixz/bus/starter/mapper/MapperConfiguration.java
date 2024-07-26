/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.mapper;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis的配置，提供一个{@link SqlSessionFactory}和一个{@link SqlSessionTemplate}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnMissingBean(MapperFactoryBean.class)
@EnableConfigurationProperties(value = { MybatisProperties.class })
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@AutoConfigureBefore(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
public class MapperConfiguration implements InitializingBean {

    private final Environment environment;
    private final MybatisProperties properties;
    private final Interceptor[] interceptors;
    private final ResourceLoader resourceLoader;
    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MapperConfiguration(Environment environment, MybatisProperties properties,
            ObjectProvider<Interceptor[]> interceptorsProvider, ResourceLoader resourceLoader,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.environment = environment;
        this.properties = properties;
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Override
    public void afterPropertiesSet() {
        if (this.properties.isCheckConfigLocation() && StringKit.hasText(this.properties.getConfigLocation())) {
            Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringKit.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringKit.hasText(this.properties.getConfigLocation())) {
            configuration = new Configuration();
        }

        // 设置自定义列表
        if (configuration != null && !CollKit.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }
        if (ArrayKit.isNotEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        } else {
            factory.setPlugins(MybatisPluginBuilder.build(environment));
        }
        if (ObjectKit.isEmptyIfString(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (this.properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
        }
        if (ObjectKit.isEmptyIfString(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        if (!ObjectKit.isEmpty(this.properties.resolveMapperLocations())) {
            factory.setMapperLocations(this.properties.resolveMapperLocations());
        }

        return factory.getObject();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    class SpringBootVFS extends VFS {

        private final ResourcePatternResolver resourceResolver;

        public SpringBootVFS() {
            this.resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        }

        private String preserveSubpackageName(final URI uri, final String rootPath) {
            final String url = uri.toString();
            return url.substring(url.indexOf(rootPath));
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        protected List<String> list(URL url, String path) throws IOException {
            Resource[] resources = resourceResolver.getResources("classpath*:" + path + "/**/*.class");
            List<String> resourcePaths = new ArrayList<>();
            for (Resource resource : resources) {
                resourcePaths.add(preserveSubpackageName(resource.getURI(), path));
            }
            return resourcePaths;
        }

    }

}
