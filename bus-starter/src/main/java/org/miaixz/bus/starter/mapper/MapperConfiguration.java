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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Resource;

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
 * <li>Bean post-processors (MapperInterfaceStringToClassConverter) handle any String-based mapperInterface
 * gracefully</li>
 * <li>No runtime overhead - only infrastructure beans</li>
 * </ul>
 * <p>
 * <strong>Native Image Compilation Behavior:</strong>
 * <ul>
 * <li>Spring AOT discovers and processes this configuration</li>
 * <li>MyBatisBeanFactoryInitializationAotProcessor executes dynamically</li>
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
 * <li>MapperInterfaceStringToClassConverter fixes AOT-generated bean definitions</li>
 * <li>Uses pre-compiled reflection metadata from native-image compilation</li>
 * <li>All Mapper methods work with zero reflection overhead</li>
 * </ul>
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
         * Lists all resources under a given path.
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
         */
        private String preserveSubpackageName(final URI uri, final String rootPath) {
            final String url = uri.toString();
            return url.substring(url.indexOf(rootPath));
        }

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
    static MyBatisBeanFactoryInitializationAotProcessor myBatisBeanFactoryInitializationAotProcessor() {
        return new MyBatisBeanFactoryInitializationAotProcessor();
    }

    /**
     * Registers a {@link MergedBeanDefinitionPostProcessor} to resolve the generic type of {@link MapperFactoryBean}
     * bean definitions.
     *
     * @return the post-processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MyBatisMapperFactoryBeanPostProcessor myBatisMapperFactoryBeanPostProcessor() {
        return new MyBatisMapperFactoryBeanPostProcessor();
    }

    /**
     * Registers a BeanFactoryPostProcessor to fix MapperFactoryBean definitions at runtime. This is needed when
     * AOT-generated bean definitions set mapperInterface as String.
     *
     * @return the bean factory post-processor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MapperInterfaceStringToClassConverter mapperInterfaceStringToClassConverter() {
        return new MapperInterfaceStringToClassConverter();
    }

    /**
     * AOT processor that discovers MapperFactoryBean beans and registers runtime hints for native compilation.
     */
    static class MyBatisBeanFactoryInitializationAotProcessor
            implements BeanFactoryInitializationAotProcessor, BeanRegistrationExcludeFilter {

        private final Set<Class<?>> excludeClasses = new HashSet<>();

        MyBatisBeanFactoryInitializationAotProcessor() {
            excludeClasses.add(MapperScannerConfigurer.class);
        }

        @Override
        public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
            return excludeClasses.contains(registeredBean.getBeanClass());
        }

        @Override
        public BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {
            String[] beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
            if (beanNames.length == 0) {
                return null;
            }
            return (context, code) -> {
                RuntimeHints hints = context.getRuntimeHints();
                for (String beanName : beanNames) {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
                    PropertyValue mapperInterface = beanDefinition.getPropertyValues()
                            .getPropertyValue("mapperInterface");
                    if (mapperInterface != null && mapperInterface.getValue() != null) {
                        Class<?> mapperInterfaceType = null;
                        Object mapperInterfaceValue = mapperInterface.getValue();

                        if (mapperInterfaceValue instanceof Class) {
                            mapperInterfaceType = (Class<?>) mapperInterfaceValue;
                        } else if (mapperInterfaceValue instanceof String) {
                            try {
                                mapperInterfaceType = Class.forName((String) mapperInterfaceValue);
                            } catch (ClassNotFoundException e) {
                                Logger.debug("Failed to load mapper interface class: " + mapperInterfaceValue);
                                e.printStackTrace();
                                continue;
                            }
                        }

                        if (mapperInterfaceType != null) {
                            registerReflectionTypeIfNecessary(mapperInterfaceType, hints);
                            hints.proxies().registerJdkProxy(mapperInterfaceType);
                            String registerPattern = mapperInterfaceType.getName().replace(Symbol.C_DOT, Symbol.C_SLASH)
                                    .replace("org/miaixz/", Normal.EMPTY).concat(FileType.TYPE_XML);
                            hints.resources().registerPattern(registerPattern);
                            registerMapperRelationships(mapperInterfaceType, hints);
                        }
                    }
                }
            };
        }

        private void registerMapperRelationships(Class<?> mapperInterfaceType, RuntimeHints hints) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(mapperInterfaceType);
            for (Method method : methods) {
                if (method.getDeclaringClass() != Object.class) {
                    ReflectionUtils.makeAccessible(method);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            SelectProvider.class,
                            SelectProvider::value,
                            SelectProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            InsertProvider.class,
                            InsertProvider::value,
                            InsertProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            UpdateProvider.class,
                            UpdateProvider::value,
                            UpdateProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            DeleteProvider.class,
                            DeleteProvider::value,
                            DeleteProvider::type);

                    Class<?> returnType = MyBatisMapperTypes.resolveReturnClass(mapperInterfaceType, method);
                    registerReflectionTypeIfNecessary(returnType, hints);
                    MyBatisMapperTypes.resolveParameterClasses(mapperInterfaceType, method)
                            .forEach(x -> registerReflectionTypeIfNecessary(x, hints));
                }
            }
        }

        @SafeVarargs
        private <T extends Annotation> void registerSqlProviderTypes(
                Method method,
                RuntimeHints hints,
                Class<T> annotationType,
                Function<T, Class<?>>... providerTypeResolvers) {
            for (T annotation : method.getAnnotationsByType(annotationType)) {
                for (Function<T, Class<?>> providerTypeResolver : providerTypeResolvers) {
                    registerReflectionTypeIfNecessary(providerTypeResolver.apply(annotation), hints);
                }
            }
        }

        private void registerReflectionTypeIfNecessary(Class<?> type, RuntimeHints hints) {
            if (!type.isPrimitive() && !type.getName().startsWith("java")) {
                hints.reflection().registerType(type, MemberCategory.values());
            }
        }
    }

    /**
     * Utility class to resolve actual parameter and return types from mapper methods with generics.
     */
    static class MyBatisMapperTypes {

        private MyBatisMapperTypes() {
            // Utility class - no instantiation
        }

        static Class<?> resolveReturnClass(Class<?> mapperInterface, Method method) {
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            return typeToClass(resolvedReturnType, method.getReturnType());
        }

        static Set<Class<?>> resolveParameterClasses(Class<?> mapperInterface, Method method) {
            return Stream.of(TypeParameterResolver.resolveParamTypes(method, mapperInterface))
                    .map(x -> typeToClass(x, x instanceof Class ? (Class<?>) x : Object.class))
                    .collect(Collectors.toSet());
        }

        private static Class<?> typeToClass(Type src, Class<?> fallback) {
            Class<?> result = null;
            if (src instanceof Class<?>) {
                if (((Class<?>) src).isArray()) {
                    result = ((Class<?>) src).getComponentType();
                } else {
                    result = (Class<?>) src;
                }
            } else if (src instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) src;
                int index = (parameterizedType.getRawType() instanceof Class
                        && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
                        && parameterizedType.getActualTypeArguments().length > 1) ? 1 : 0;
                Type actualType = parameterizedType.getActualTypeArguments()[index];
                result = typeToClass(actualType, fallback);
            }
            if (result == null) {
                result = fallback;
            }
            return result;
        }
    }

    /**
     * Post-processor that ensures MapperFactoryBean bean definitions include generic type information.
     */
    static class MyBatisMapperFactoryBeanPostProcessor implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

        private ConfigurableBeanFactory beanFactory;

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }

        @Override
        public void postProcessMergedBeanDefinition(
                RootBeanDefinition beanDefinition,
                Class<?> beanType,
                String beanName) {
        }
    }

    /**
     * Converts String-based mapperInterface properties to Class objects in MapperFactoryBean definitions. Necessary
     * because AOT-generated bean definitions set mapperInterface as String.
     */
    static class MapperInterfaceStringToClassConverter
            implements org.springframework.beans.factory.config.BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            Logger.debug("MapperInterfaceStringToClassConverter: Starting to process bean definitions");

            String[] allBeanNames = beanFactory.getBeanDefinitionNames();
            int processedCount = 0;

            Logger.debug("Total bean definitions to check: {}", allBeanNames.length);

            for (String beanName : allBeanNames) {
                try {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

                    if (beanDefinition instanceof RootBeanDefinition) {
                        RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;

                        if (rootBeanDefinition.hasBeanClass()
                                && MapperFactoryBean.class.isAssignableFrom(rootBeanDefinition.getBeanClass())) {

                            Object mapperInterfaceValue = rootBeanDefinition.getPropertyValues().get("mapperInterface");

                            Logger.debug(
                                    "Found MapperFactoryBean: " + beanName + ", mapperInterface type: "
                                            + (mapperInterfaceValue != null ? mapperInterfaceValue.getClass().getName()
                                                    : "null")
                                            + ", value: " + mapperInterfaceValue);

                            if (mapperInterfaceValue instanceof String) {
                                String mapperInterfaceClassName = (String) mapperInterfaceValue;
                                Logger.debug("Converting String to Class: {}", mapperInterfaceClassName);
                                try {
                                    Class<?> mapperInterface = ClassUtils
                                            .forName(mapperInterfaceClassName, beanFactory.getBeanClassLoader());

                                    rootBeanDefinition.getPropertyValues().removePropertyValue("mapperInterface");
                                    rootBeanDefinition.getPropertyValues()
                                            .addPropertyValue("mapperInterface", mapperInterface);

                                    rootBeanDefinition.setTargetType(
                                            ResolvableType
                                                    .forClassWithGenerics(MapperFactoryBean.class, mapperInterface));

                                    Logger.debug(
                                            "Converted mapperInterface from String to Class for bean: " + beanName
                                                    + " -> " + mapperInterface.getName());
                                    processedCount++;
                                } catch (ClassNotFoundException e) {
                                    Logger.error(
                                            "Failed to load mapper interface class: " + mapperInterfaceClassName,
                                            e);
                                }
                            } else if (mapperInterfaceValue instanceof Class) {
                                Logger.debug("Already a Class, no conversion needed");
                            } else {
                                Logger.debug(
                                        "Unexpected type: "
                                                + (mapperInterfaceValue != null ? mapperInterfaceValue.getClass()
                                                        : "null"));
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.warn("Failed to process bean definition for: " + beanName, e);
                }
            }
            Logger.debug(
                    "MapperInterfaceStringToClassConverter: Processed " + processedCount
                            + " MapperFactoryBean definitions");
        }
    }

}
