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
package org.miaixz.bus.starter.aot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.javassist.util.proxy.RuntimeSupport;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.base.entity.BaseEntity;
import org.miaixz.bus.base.mapper.BaseMapper;
import org.miaixz.bus.base.mapper.SharedMapper;
import org.miaixz.bus.core.basic.entity.*;
import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.Mapper;
import org.miaixz.bus.mapper.Marker;
import org.miaixz.bus.mapper.binding.BasicMapper;
import org.miaixz.bus.mapper.binding.basic.EntityMapper;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionMapper;
import org.miaixz.bus.mapper.binding.cursor.CursorMapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.builder.GenericTypeResolver;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.provider.ConditionProvider;
import org.miaixz.bus.mapper.provider.EntityProvider;
import org.miaixz.bus.mapper.provider.FunctionProvider;
import org.miaixz.bus.mapper.support.paging.PageContext;
import org.miaixz.bus.starter.jdbc.JdbcProperties;
import org.miaixz.bus.starter.mapper.MapperPluginBuilder;
import org.miaixz.bus.starter.mapper.MapperProperties;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.RequestContext;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Stream;

/**
 * Programmatic registration of {@link RuntimeHints} for Spring Native AOT (Ahead-of-Time) compilation.
 * <p>
 * This class is registered via {@code @ImportRuntimeHints} in {@link MapperNativeConfiguration} and is responsible for
 * declaring all necessary reflection, proxy, and resource hints required by MyBatis, bus-mapper, and associated
 * libraries to function correctly in a GraalVM native image.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(RuntimeHintsRegistrar.class)
public class RuntimeHintsRegistrar implements org.springframework.aot.hint.RuntimeHintsRegistrar {

    /**
     * Registers the runtime hints. This method is called by the Spring AOT build process.
     *
     * @param hints       The {@link RuntimeHints} instance to register hints with.
     * @param classLoader The application's class loader, or {@code null} if not available.
     */
    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }

        // 1. Register Resource Patterns
        // MyBatis XML mappers, DTDs, XSDs, and i18n properties bundles
        // need to be included in the native image.
        Stream.of(
                // MyBatis XML parsing
                "org/apache/ibatis/builder/xml/*.dtd",
                "org/apache/ibatis/builder/xml/*.xsd",
                // Application resources
                "i18n/**",
                "static/**",
                "cache/**",
                "health/**",
                "shade/**",
                // Main location for mapper XML files
                "mapper/**/*.xml").forEach(hints.resources()::registerPattern);

        // 2. Register Types for Reflection
        // All classes accessed reflectively must be registered.
        // MemberCategory.values() enables all members (fields, methods, constructors).
        Stream.of(
                // == MyBatis Core ==
                SqlSessionFactory.class,
                SqlSessionFactoryBean.class,
                BoundSql.class,
                // MyBatis Caches
                PerpetualCache.class,
                FifoCache.class,
                LruCache.class,
                SoftCache.class,
                WeakCache.class,
                // MyBatis Language Drivers
                RawLanguageDriver.class,
                XMLLanguageDriver.class,

                // == MyBatis Logging ==
                // Register all common logging implementations
                Log.class,
                JakartaCommonsLoggingImpl.class,
                Log4j2Impl.class,
                Jdk14LoggingImpl.class,
                Slf4jImpl.class,
                StdOutImpl.class,
                NoLoggingImpl.class,

                // == MyBatis Dependencies (Javassist for proxies) ==
                ProxyFactory.class,
                RuntimeSupport.class,

                // == MyBatis Reflection & Type Handling ==
                TypeParameterResolver.class,
                MetaObject.class,

                // == JDBC & Datasource ==
                Statement.class,
                HikariConfig.class, // Assuming HikariCP is used
                HikariDataSource.class,

                // == Common Java Collections ==
                // Often used as parameters or return types in mappers
                ArrayList.class,
                LinkedList.class,
                Vector.class,
                HashMap.class,
                LinkedHashMap.class,
                TreeMap.class,
                HashSet.class,
                TreeSet.class,
                Properties.class,

                // == Bus-Mapper Framework ==
                // Core mapper interfaces
                BaseMapper.class,
                SharedMapper.class,
                Mapper.class,
                BasicMapper.class,
                EntityMapper.class,
                ConditionMapper.class,
                CursorMapper.class,
                Marker.class,
                // Lambda/Function support
                Fn.class,
                Fn.FnArray.class,
                // Type resolution and metadata
                GenericTypeResolver.class,
                TableMeta.class,
                ColumnMeta.class,
                FieldMeta.class,
                // SQL Providers
                EntityProvider.class,
                FunctionProvider.class,
                ConditionProvider.class,
                // Caching and Condition API
                Caching.class,
                Condition.class,

                // == Bus-Starter & Pager ==
                MapperPluginBuilder.class,
                PageContext.class,

                // == Spring-MyBatis Integration ==
                MapperFactoryBean.class,

                // == Bus-Core Entities & Tookit ==
                Authorize.class,
                Entity.class,
                Message.class,
                Result.class,
                Tracer.class,
                BaseEntity.class,
                ErrorCode.class,
                Iterable.class,
                I18n.class,
                JdbcProperties.class,
                MapperProperties.class).forEach(x -> hints.reflection().registerType(x, MemberCategory.values()));

        // 3. Register Specific Methods for Invocation
        // If a specific method is called only via reflection, it must be registered.
        try {
            // Example: Registering a method from Spring Web
            Method method = RequestContext.class.getMethod("getContextPath");
            hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
        } catch (NoSuchMethodException e) {
            // This would be a build-time error if the method signature changes
            throw new IllegalStateException("Failed to find method for AOT hint registration", e);
        }

    }

}
