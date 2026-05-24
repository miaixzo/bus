/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.mapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.sql.DataSource;

import jakarta.annotation.Resource;

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Unified auto-configuration for MyBatis with comprehensive Native Image support.
 * <p>
 * This class handles both:
 * <ul>
 * <li><strong>JVM Runtime:</strong> Creates MyBatis core beans (SqlSessionFactory, SqlSessionTemplate)</li>
 * <li><strong>GraalVM Native Image:</strong> Registers AOT hints for Mapper discovery and entity type resolution</li>
 * </ul>
 * <p>
 * <strong>JVM Mode Behavior:</strong>
 * <ul>
 * <li>Loads MapperConfiguration (this class) as auto-configuration</li>
 * <li>Creates SqlSessionFactory and SqlSessionTemplate beans</li>
 * <li>AOT processors are instantiated but not executed (Spring ignores BeanFactoryInitializationAotProcessor in
 * JVM)</li>
 * <li>Bean post-processors from {@code MapperAotProcessors} handle any String-based mapperInterface gracefully</li>
 * <li>No runtime overhead - only infrastructure beans</li>
 * </ul>
 * <p>
 * <strong>Native Image Compilation Behavior:</strong>
 * <ul>
 * <li>Spring AOT discovers and processes this configuration</li>
 * <li>{@code MapperAotProcessors} mapper hint processor executes dynamically</li>
 * <li>Scans for all MapperFactoryBean definitions</li>
 * <li>Registers JDK proxies for Mapper interfaces</li>
 * <li>Analyzes method signatures to extract entity types</li>
 * <li>Registers SQL Provider classes from annotations</li>
 * <li>Generates reflection metadata into native image binary</li>
 * </ul>
 * <p>
 * <strong>Native Image Runtime Behavior:</strong>
 * <ul>
 * <li>Creates SqlSessionFactory and SqlSessionTemplate (same as JVM)</li>
 * <li>{@code MapperAotProcessors} fixes AOT-generated mapper definitions</li>
 * <li>Uses pre-compiled reflection metadata from native-image compilation</li>
 * <li>All Mapper methods work with zero reflection overhead</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { MapperProperties.class })
@ConditionalOnProperty(prefix = GeniusBuilder.MAPPER, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@AutoConfigureBefore(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
public class MapperConfiguration implements InitializingBean {

    /**
     * Constructs a MapperConfiguration instance for Spring field injection.
     */
    public MapperConfiguration() {
        // No initialization required.
    }

    /**
     * Spring environment configuration.
     */
    @Resource
    private Environment environment;

    /**
     * Spring resource loader.
     */
    @Resource
    private ResourceLoader resourceLoader;

    /**
     * MyBatis-specific configuration properties.
     */
    @Resource
    private MapperProperties properties;

    /**
     * Spring resource pattern resolver used to resolve mapper XML locations.
     */
    private ResourcePatternResolver mapperResourceResolver;

    /**
     * Provider for optional MyBatis configuration customizers.
     */
    @Resource
    private ObjectProvider<List<MyBatisConfigCustomizer>> configurationCustomizersProvider;

    /**
     * Checks the configured MyBatis XML configuration resource after Spring has injected all properties.
     * <p>
     * The check only runs when {@code bus.mapper.check-config-location} is enabled and a config location is configured.
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
            Logger.debug(true, "Starter", "Checked MyBatis config location: {}", this.properties.getConfigLocation());
        }
        Logger.info(
                true,
                "Starter",
                "Mapper configuration initialization started: customizerCount={}",
                configurationCustomizers().size());
    }

    /**
     * Creates the {@link SqlSessionFactory} bean.
     *
     * @param dataSource  The primary data source.
     * @param beanFactory The Spring bean factory used by mapper plugin configuration.
     * @return The configured {@link SqlSessionFactory}.
     * @throws Exception if an error occurs during factory creation.
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, ConfigurableListableBeanFactory beanFactory)
            throws Exception {
        Logger.info(
                true,
                "Starter",
                "SqlSessionFactory creation started: dataSourceClass={}, configLocation={}, typeAliasesPackage={}, typeHandlersPackage={}",
                dataSource.getClass().getName(),
                this.properties.getConfigLocation(),
                this.properties.getTypeAliasesPackage(),
                this.properties.getTypeHandlersPackage());
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
        List<MyBatisConfigCustomizer> configurationCustomizers = configurationCustomizers();
        if (configuration != null && !CollKit.isEmpty(configurationCustomizers)) {
            for (MyBatisConfigCustomizer customizer : configurationCustomizers) {
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
        org.springframework.core.io.Resource[] mapperLocations = resolveMapperLocations();
        if (!ObjectKit.isEmpty(mapperLocations)) {
            factory.setMapperLocations(mapperLocations);
        }

        factory.setConfiguration(configuration);

        MapperPluginBuilder.configureSqlSessionFactory(
                factory,
                this.properties,
                this.environment,
                this.resourceLoader,
                dataSource,
                beanFactory);

        SqlSessionFactory sqlSessionFactory = factory.getObject();
        Logger.info(
                false,
                "Starter",
                "SqlSessionFactory created: factoryClass={}",
                sqlSessionFactory == null ? null : sqlSessionFactory.getClass().getName());
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
            Logger.info(false, "Starter", "Created SqlSessionTemplate with executor type: {}", executorType);
        } else {
            template = new SqlSessionTemplate(sqlSessionFactory);
            Logger.info(false, "Starter", "Created SqlSessionTemplate with default executor type");
        }
        return template;
    }

    /**
     * Registers the {@link BeanFactoryInitializationAotProcessor} that scans for {@link MapperFactoryBean} definitions
     * and registers runtime hints.
     * <p>
     * <strong>JVM Mode:</strong> Instantiated but not executed (AOT processors are ignored) <strong>Native Image
     * Mode:</strong> Executed during native-image compilation
     *
     * @return the AOT processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MapperAotProcessors.MyBatisBeanFactoryInitializationAotProcessor myBatisBeanFactoryInitializationAotProcessor() {
        return new MapperAotProcessors.MyBatisBeanFactoryInitializationAotProcessor();
    }

    /**
     * Registers a {@link MergedBeanDefinitionPostProcessor} that preserves the mapper factory bean post-processing
     * extension point.
     * <p>
     * Mapper interface String-to-Class conversion and target generic refresh are performed by
     * {@link MapperAotProcessors.MapperInterfaceStringToClassConverter}.
     *
     * @return the post-processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MapperAotProcessors.MyBatisMapperFactoryBeanPostProcessor myBatisMapperFactoryBeanPostProcessor() {
        return new MapperAotProcessors.MyBatisMapperFactoryBeanPostProcessor();
    }

    /**
     * Registers a BeanFactoryPostProcessor to fix MapperFactoryBean definitions at runtime.
     * <p>
     * This is needed when AOT-generated bean definitions set {@code mapperInterface} as a class name string.
     *
     * @return the bean factory post-processor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MapperAotProcessors.MapperInterfaceStringToClassConverter mapperInterfaceStringToClassConverter() {
        return new MapperAotProcessors.MapperInterfaceStringToClassConverter();
    }

    /**
     * Resolves mapper XML location patterns into Spring resources.
     * <p>
     * This method lives in the starter configuration because it uses Spring resource resolution. The inherited
     * {@code MapperOptions.mapperLocations} field remains a plain string configuration value.
     *
     * @return resolved mapper XML resources
     */
    private org.springframework.core.io.Resource[] resolveMapperLocations() {
        List<org.springframework.core.io.Resource> resources = new ArrayList<>();
        String[] mapperLocations = this.properties.getMapperLocations();
        if (mapperLocations != null) {
            for (String mapperLocation : mapperLocations) {
                resources.addAll(Arrays.asList(getResources(mapperLocation)));
            }
        }
        return resources.toArray(new org.springframework.core.io.Resource[resources.size()]);
    }

    /**
     * Retrieves resources from a mapper XML location pattern.
     * <p>
     * Resolution failures are treated as an empty result to preserve the previous starter behavior.
     *
     * @param location mapper XML location pattern
     * @return resolved resources, or an empty array when resolution fails
     */
    private org.springframework.core.io.Resource[] getResources(String location) {
        try {
            return mapperResourceResolver().getResources(location);
        } catch (IOException e) {
            return new org.springframework.core.io.Resource[0];
        }
    }

    /**
     * Returns the optional MyBatis configuration customizers registered in the Spring container.
     *
     * @return configuration customizers, never {@code null}
     */
    private List<MyBatisConfigCustomizer> configurationCustomizers() {
        if (this.configurationCustomizersProvider == null) {
            return Collections.emptyList();
        }
        return this.configurationCustomizersProvider.getIfAvailable(Collections::emptyList);
    }

    /**
     * Returns the mapper XML resource resolver backed by the injected Spring resource loader.
     *
     * @return mapper resource pattern resolver
     */
    private ResourcePatternResolver mapperResourceResolver() {
        if (this.mapperResourceResolver == null) {
            this.mapperResourceResolver = new PathMatchingResourcePatternResolver(this.resourceLoader);
        }
        return this.mapperResourceResolver;
    }

    /**
     * A custom {@link VFS} implementation for MyBatis that works correctly in a Spring Boot environment, especially
     * with executable jars.
     * <p>
     * This class remains in the starter because it relies on Spring's resource pattern resolver.
     *
     * @author Kimi Liu
     * @since Java 21+
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
            Logger.debug(true, "Starter", "Initialized SpringBootVFS with resource resolver");
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
         * Lists all class resources under a given MyBatis VFS path.
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
                String resourceUrl = resource.getURI().toString();
                resourcePaths.add(resourceUrl.substring(resourceUrl.indexOf(path)));
            }
            Logger.debug(false, "Starter", "Listed resources for path: {}", path);
            return resourcePaths;
        }

    }

}
