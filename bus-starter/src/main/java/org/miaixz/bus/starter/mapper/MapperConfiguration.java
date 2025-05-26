/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import jakarta.annotation.Resource;

/**
 * Mybatis配置类，提供SqlSessionFactory和SqlSessionTemplate
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnMissingBean(MapperFactoryBean.class)
@EnableConfigurationProperties(value = { MybatisProperties.class })
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@AutoConfigureBefore(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
public class MapperConfiguration implements InitializingBean {

    /** Spring环境配置 */
    private final Environment environment;

    /** 资源加载器 */
    private final ResourceLoader resourceLoader;

    /** MyBatis配置定制器列表 */
    private final List<ConfigurationCustomizer> configurationCustomizers;

    /** MyBatis属性配置 */
    @Resource
    MybatisProperties properties;

    /**
     * 构造函数，初始化环境、资源加载器和配置定制器
     *
     * @param environment                      Spring环境
     * @param resourceLoader                   资源加载器
     * @param configurationCustomizersProvider MyBatis配置定制器提供者
     */
    public MapperConfiguration(Environment environment, ResourceLoader resourceLoader,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
        Logger.info("Initializing MapperConfiguration with provided environment and resource loader");
    }

    /**
     * 初始化后检查配置文件是否存在
     */
    @Override
    public void afterPropertiesSet() {
        if (this.properties.isCheckConfigLocation() && StringKit.hasText(this.properties.getConfigLocation())) {
            org.springframework.core.io.Resource resource = this.resourceLoader
                    .getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
            Logger.debug("Checked MyBatis config location: {}", this.properties.getConfigLocation());
        }
    }

    /**
     * 创建SqlSessionFactory bean
     *
     * @param dataSource 数据源
     * @return SqlSessionFactory
     * @throws Exception 异常
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        Logger.info("Creating SqlSessionFactory with dataSource");
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        if (properties.getConfiguration() == null || properties.getConfiguration().getVfsImpl() == null) {
            factory.setVfs(SpringBootVFS.class);
        }
        if (StringKit.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringKit.hasText(this.properties.getConfigLocation())) {
            configuration = new Configuration();
        }

        // 应用配置定制器
        if (configuration != null && !CollKit.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
            Context.INSTANCE.setProperties(this.properties.getConfigurationProperties());
        }
        factory.setPlugins(MybatisPluginBuilder.build(environment));
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

        SqlSessionFactory sqlSessionFactory = factory.getObject();
        Logger.info("SqlSessionFactory created successfully");
        return sqlSessionFactory;
    }

    /**
     * 创建SqlSessionTemplate bean
     *
     * @param sqlSessionFactory SqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        SqlSessionTemplate template;
        if (executorType != null) {
            template = new SqlSessionTemplate(sqlSessionFactory, executorType);
            Logger.info("Created SqlSessionTemplate with executor type: {}", executorType);
        } else {
            template = new SqlSessionTemplate(sqlSessionFactory);
            Logger.info("Created SqlSessionTemplate with default executor type");
        }
        return template;
    }

    /**
     * 自定义VFS实现，用于Spring Boot环境
     */
    class SpringBootVFS extends VFS {

        /** 资源解析器 */
        private final ResourcePatternResolver resourceResolver;

        /**
         * 构造函数，初始化资源解析器
         */
        public SpringBootVFS() {
            this.resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            Logger.debug("Initialized SpringBootVFS with resource resolver");
        }

        /**
         * 保留子包名称
         *
         * @param uri      URI
         * @param rootPath 根路径
         * @return 子包路径
         */
        private String preserveSubpackageName(final URI uri, final String rootPath) {
            final String url = uri.toString();
            return url.substring(url.indexOf(rootPath));
        }

        /**
         * 检查VFS是否有效
         *
         * @return 始终返回true
         */
        @Override
        public boolean isValid() {
            return true;
        }

        /**
         * 列出指定路径下的类文件
         *
         * @param url  URL
         * @param path 路径
         * @return 类文件路径列表
         * @throws IOException IO exception
         */
        @Override
        protected List<String> list(URL url, String path) throws IOException {
            org.springframework.core.io.Resource[] resources = resourceResolver
                    .getResources("classpath*:" + path + "/**/*.class");
            List<String> resourcePaths = new ArrayList<>();
            for (org.springframework.core.io.Resource resource : resources) {
                resourcePaths.add(preserveSubpackageName(resource.getURI(), path));
            }
            Logger.debug("Listed resources for path: {}", path);
            return resourcePaths;
        }

    }

}