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
 * Auto-configuration for MyBatis, providing {@link SqlSessionFactory} and {@link SqlSessionTemplate} beans. This
 * configuration is activated when {@link SqlSessionFactory} and {@link SqlSessionFactoryBean} are on the classpath and
 * no other {@link MapperFactoryBean} is defined.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { MapperProperties.class })
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@AutoConfigureBefore(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
public class MapperConfiguration implements InitializingBean {

    /**
     * Spring environment configuration.
     */
    private final Environment environment;

    /**
     * Spring resource loader.
     */
    private final ResourceLoader resourceLoader;

    /**
     * List of MyBatis configuration customizers.
     */
    private final List<ConfigurationCustomizer> configurationCustomizers;

    /**
     * MyBatis-specific configuration properties.
     */
    @Resource
    private MapperProperties properties;

    /**
     * Constructs the configuration, injecting the environment, resource loader, and customizers.
     *
     * @param environment                      The Spring Environment.
     * @param resourceLoader                   The Spring ResourceLoader.
     * @param configurationCustomizersProvider A provider for a list of {@link ConfigurationCustomizer} beans.
     */
    public MapperConfiguration(Environment environment, ResourceLoader resourceLoader,
            ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
        Logger.info(true, "Mapper", "Initializing MapperConfiguration");
    }

    /**
     * After properties are set, this method checks if the specified MyBatis config location exists.
     */
    @Override
    public void afterPropertiesSet() {
        if (this.properties.isCheckConfigLocation() && StringKit.hasText(this.properties.getConfigLocation())) {
            org.springframework.core.io.Resource resource = this.resourceLoader
                    .getResource(this.properties.getConfigLocation());
            Assert.state(
                    resource.exists(),
                    "Cannot find config location: " + resource
                            + " (please add config file or check your Mybatis configuration)");
            Logger.debug(true, "Mapper", "Checked MyBatis config location: {}", this.properties.getConfigLocation());
        }
    }

    /**
     * Creates the {@link SqlSessionFactory} bean.
     *
     * @param dataSource The primary data source.
     * @return The configured {@link SqlSessionFactory}.
     * @throws Exception if an error occurs during factory creation.
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        Logger.info(true, "Mapper", "Creating SqlSessionFactory with dataSource");
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // Use a custom VFS for Spring Boot executable jars if none is specified.
        if (properties.getConfiguration() == null || properties.getConfiguration().getVfsImpl() == null) {
            factory.setVfs(SpringBootVFS.class);
        }

        // Set the config location if specified.
        if (StringKit.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }

        // Create a new Configuration object if none is provided and no config location is set.
        Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringKit.hasText(this.properties.getConfigLocation())) {
            configuration = new Configuration();
        }

        // Apply customizers to the configuration.
        if (configuration != null && !CollKit.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }

        // Apply external properties.
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
            Context.INSTANCE.putAll(this.properties.getConfigurationProperties());
        }

        // Configure type aliases and handlers.
        if (StringKit.isNotEmpty(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (this.properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
        }
        if (StringKit.isNotEmpty(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }

        // Set mapper locations.
        if (!ObjectKit.isEmpty(this.properties.resolveMapperLocations())) {
            factory.setMapperLocations(this.properties.resolveMapperLocations());
        }

        factory.setConfiguration(configuration);

        // Configure plugins.
        factory.setPlugins(MapperPluginBuilder.build(environment));

        SqlSessionFactory sqlSessionFactory = factory.getObject();
        Logger.info(false, "Mapper", "SqlSessionFactory created successfully");
        return sqlSessionFactory;
    }

    /**
     * Creates the {@link SqlSessionTemplate} bean.
     *
     * @param sqlSessionFactory The {@link SqlSessionFactory} to use.
     * @return The configured {@link SqlSessionTemplate}.
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        SqlSessionTemplate template;
        if (executorType != null) {
            template = new SqlSessionTemplate(sqlSessionFactory, executorType);
            Logger.info(false, "Mapper", "Created SqlSessionTemplate with executor type: {}", executorType);
        } else {
            template = new SqlSessionTemplate(sqlSessionFactory);
            Logger.info(false, "Mapper", "Created SqlSessionTemplate with default executor type");
        }
        return template;
    }

    /**
     * A custom {@link VFS} implementation for MyBatis that works correctly in a Spring Boot environment, especially
     * with executable jars.
     */
    class SpringBootVFS extends VFS {

        /**
         * The resource resolver.
         */
        private final ResourcePatternResolver resourceResolver;

        /**
         * Constructs the SpringBootVFS, initializing the resource resolver.
         */
        public SpringBootVFS() {
            this.resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            Logger.debug(true, "Mapper", "Initialized SpringBootVFS with resource resolver");
        }

        /**
         * Checks if the VFS is valid.
         *
         * @return Always returns {@code true}.
         */
        @Override
        public boolean isValid() {
            return true;
        }

        /**
         * Lists all resources under a given path, which is essential for MyBatis to find mappers and type aliases.
         *
         * @param url  The URL of the resource to list.
         * @param path The path within the URL to list.
         * @return A list of resource paths as strings.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        protected List<String> list(URL url, String path) throws IOException {
            org.springframework.core.io.Resource[] resources = resourceResolver
                    .getResources("classpath*:" + path + "/**/*.class");
            List<String> resourcePaths = new ArrayList<>();
            for (org.springframework.core.io.Resource resource : resources) {
                resourcePaths.add(preserveSubpackageName(resource.getURI(), path));
            }
            Logger.debug(false, "Mapper", "Listed resources for path: {}", path);
            return resourcePaths;
        }

        /**
         * Preserves the sub-package name from the full resource URI.
         *
         * @param uri      The URI of the resource.
         * @param rootPath The root path to relativize against.
         * @return The relative path of the resource.
         */
        private String preserveSubpackageName(final URI uri, final String rootPath) {
            final String url = uri.toString();
            return url.substring(url.indexOf(rootPath));
        }
    }

}
