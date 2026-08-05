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

import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.provider.MyBatisConfigCustomizer;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.bean.BeanProvider;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.spring.jdbc.DataSourceHolder;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableMapper;

/**
 * Configures MyBatis runtime beans and GraalVM Native Image support.
 * <p>
 * This configuration performs two related tasks:
 * <ul>
 * <li>Creates the {@link SqlSessionFactory} and {@link SqlSessionTemplate} runtime beans.</li>
 * <li>Exposes AOT processors that register Mapper proxies, provider methods, entity types, and XML resources.</li>
 * </ul>
 * <p>
 * Mapper scanning is imported from {@link MapperScannerRegistrar} for both annotation activation and
 * {@code bus.mapper.enabled=true}. Optional customizers and plugin providers are obtained from the Spring container;
 * the Mapper integration never owns JDBC routing state.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { MapperProperties.class })
@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@Import(MapperScannerRegistrar.class)
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@AutoConfigureBefore(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
@ConditionalOnEnabled(annotation = EnableMapper.class, prefix = GeniusBuilder.MAPPER)
public class MapperConfiguration implements InitializingBean {

    /**
     * Spring environment used for property and profile resolution.
     */
    private final Environment environment;

    /**
     * Resource loader used for MyBatis configuration, mapper XML, and VFS resources.
     */
    private final ResourceLoader resourceLoader;

    /**
     * Bound mapper configuration properties.
     */
    private final MapperProperties properties;

    /**
     * Spring resource pattern resolver used to resolve mapper XML locations.
     */
    private ResourcePatternResolver mapperResourceResolver;

    /**
     * Provider for optional MyBatis configuration customizers.
     */
    private final ObjectProvider<List<MyBatisConfigCustomizer>> configurationCustomizersProvider;

    /**
     * Creates the Mapper integration configuration from its required collaborators.
     *
     * @param environment                      Spring environment
     * @param resourceLoader                   resource loader
     * @param properties                       bound feature configuration properties
     * @param configurationCustomizersProvider configuration customizers provider
     */
    public MapperConfiguration(Environment environment, ResourceLoader resourceLoader, MapperProperties properties,
            ObjectProvider<List<MyBatisConfigCustomizer>> configurationCustomizersProvider) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.properties = properties;
        this.configurationCustomizersProvider = configurationCustomizersProvider;
    }

    /**
     * Creates the Mapper observer that synchronizes dialect state with JDBC route changes.
     *
     * @param dataSourceHolder application-context-scoped datasource routing state
     * @return datasource route listener for Mapper dialects
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(DialectListener.class)
    @ConditionalOnBean(DataSourceHolder.class)
    public DialectListener dialectListener(DataSourceHolder dataSourceHolder) {
        return new DialectListener(dataSourceHolder);
    }

    /**
     * Creates the infrastructure processor that binds both default and user-supplied MyBatis factories to datasource
     * dialect routing.
     *
     * @param dialectListeners lazy context-local dialect listener provider
     * @return MyBatis factory dialect binding processor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static DialectBinding dialectBinding(ObjectProvider<DialectListener> dialectListeners) {
        return new DialectBinding(dialectListeners);
    }

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
     * @param dataSource   primary data source
     * @param beanFactory  Spring bean factory used by Mapper plugin configuration
     * @param beanProvider Spring Bean provider
     * @return configured {@link SqlSessionFactory}
     * @throws Exception if factory creation or schema initialization fails
     */
    @Bean
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory,
            BeanProvider beanProvider) throws Exception {
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
                beanFactory,
                beanProvider);

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
    @ConditionalOnMissingBean(SqlSessionTemplate.class)
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
     * Adapts the authenticated application context when the application has not supplied its own tenant provider.
     *
     * @param contextBuilder authenticated context facade
     * @return context-backed tenant provider
     */
    @Bean
    @ConditionalOnMissingBean(TenantProvider.class)
    @ConditionalOnProperty(prefix = GeniusBuilder.MAPPER
            + ".tenant", name = "enabled", havingValue = "true", matchIfMissing = false)
    public TenantProvider contextTenantProvider(ContextBuilder contextBuilder) {
        return new ContextTenantProvider(contextBuilder);
    }

    /**
     * Registers the {@link BeanFactoryInitializationAotProcessor} that scans for {@link MapperFactoryBean} definitions
     * and registers runtime hints.
     * <p>
     * <strong>JVM Mode:</strong> Instantiated but not executed (AOT processors are ignored) <strong>Native Image
     * Mode:</strong> Executed during native-image compilation
     *
     * @param environment Spring environment used for early mapper property binding
     * @return the AOT processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(MapperAotProcessors.MyBatisBeanFactoryInitializationAotProcessor.class)
    static MapperAotProcessors.MyBatisBeanFactoryInitializationAotProcessor myBatisBeanFactoryInitializationAotProcessor(
            Environment environment) {
        return new MapperAotProcessors.MyBatisBeanFactoryInitializationAotProcessor(environment);
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
    @ConditionalOnMissingBean(MapperAotProcessors.MapperInterfaceStringToClassConverter.class)
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
        MapperLocationResolver.Result result = MapperLocationResolver
                .resolve(this.properties, mapperResourceResolver());
        Logger.info(
                false,
                "Starter",
                "Mapper runtime resources resolved: resourceCount={}, patterns={}",
                result.resources().length,
                result.patterns());
        return result.resources();
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
         * Initializes the VFS with a resolver bound to the current application class loader.
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
