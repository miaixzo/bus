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
package org.miaixz.bus.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.parsing.SqlMetaCache;
import org.miaixz.bus.mapper.parsing.SqlSourceEnhancer;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A custom MyBatis {@link XMLLanguageDriver} that caches XML-based SqlSource to avoid redundant parsing.
 *
 * @author Kimi Liu
 */
public class Caching extends XMLLanguageDriver {

    /**
     * MyBatis 3.6 provides a parameter-name-aware parser; 3.5 uses the original three-argument parser. Detection
     * happens once, outside SQL execution.
     */
    private static final Method PARAMETER_AWARE_PARSER = parameterAwareParser(String.class);

    /**
     * The MyBatis four-argument XML parser, detected once and absent on MyBatis 3.5. Passing the parameter-name
     * resolver directly to this overload preserves it when parsing {@code <script>} content.
     */
    private static final Method PARAMETER_AWARE_XML_PARSER = parameterAwareParser(XNode.class);

    /**
     * A separate driver avoids virtual dispatch back into this cache when MyBatis delegates between overloads.
     */
    private final XMLLanguageDriver parser = new XMLLanguageDriver();

    /**
     * Locates the parameter-name-aware parser without requiring it at compile time.
     *
     * @param scriptType the script representation, either {@link String} or {@link XNode}
     * @return the four-argument parser method, or {@code null} when the installed MyBatis version does not provide it
     */
    private static Method parameterAwareParser(Class<?> scriptType) {
        try {
            return XMLLanguageDriver.class.getMethod(
                    "createSqlSource",
                    Configuration.class,
                    scriptType,
                    Class.class,
                    ParamNameResolver.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Parses SQL using the available MyBatis overload, preserving parameter names for XML scripts. Falls back to the
     * three-argument parser when the parameter-name-aware overload is unavailable.
     *
     * @param configuration the MyBatis configuration used to parse the script
     * @param script        the SQL text or XML script to parse
     * @param parameterType the resolved mapper parameter type
     * @param resolver      the mapper method parameter-name resolver
     * @return the parsed SQL source
     * @throws IllegalStateException if the parser cannot be accessed or throws a checked exception
     */
    private SqlSource parseSqlSource(
            Configuration configuration,
            String script,
            Class<?> parameterType,
            ParamNameResolver resolver) {
        if (PARAMETER_AWARE_PARSER == null) {
            return parser.createSqlSource(configuration, script, parameterType);
        }
        try {
            if (PARAMETER_AWARE_XML_PARSER != null && script.startsWith("<script>")) {
                // Pass the resolver directly to the XML overload; the 3.6 string overload drops it for scripts.
                XPathParser xml = new XPathParser(script, false, configuration.getVariables(),
                        new XMLMapperEntityResolver());
                return (SqlSource) PARAMETER_AWARE_XML_PARSER
                        .invoke(parser, configuration, xml.evalNode("/script"), parameterType, resolver);
            }
            return (SqlSource) PARAMETER_AWARE_PARSER.invoke(parser, configuration, script, parameterType, resolver);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Unable to parse mapper SQL", cause);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to access MyBatis SQL parser", exception);
        }
    }

    /**
     * A map to cache {@link SqlMetaCache} objects. The initial capacity is set based on an estimate (e.g., 30 entities
     * with 25 methods each).
     * <p>
     * For a single data source, this cache can eventually be cleared. For multiple data sources, the cache must be
     * retained because the cleanup timing is indeterminate.
     * </p>
     */
    private static final Map<String, SqlMetaCache> CACHE_SQL = new ConcurrentHashMap<>(
            Context.INSTANCE.getInt(Args.PROVIDER_KEY + Symbol.DOT + Args.INITSIZE_KEY, 1024));

    /**
     * Caches {@link SqlSource} per {@link Configuration} to handle multi-datasource or multi-configuration scenarios
     * (e.g., in unit tests), ensuring consistency.
     */
    private static final WeakHashMap<Configuration, Map<String, SqlSource>> CONFIGURATION_CACHE_KEY_MAP_BACKING = new WeakHashMap<>(
            4);

    /**
     * Weak-key cache for configuration-specific {@link SqlSource} instances.
     */
    private static final Map<Configuration, Map<String, SqlSource>> CONFIGURATION_CACHE_KEY_MAP = Collections
            .synchronizedMap(CONFIGURATION_CACHE_KEY_MAP_BACKING);

    /**
     * If true, the cache is cleared after its first use, allowing for garbage collection. This should be set to
     * {@code false} when using multiple data sources with a single SqlSessionFactory to prevent premature cache
     * eviction. It can be set to {@code true} for a single SqlSessionFactory with multiple data sources. Defaults to
     * {@code false}.
     */
    private static final boolean USE_ONCE = Context.INSTANCE
            .getBoolean(Args.PROVIDER_KEY + Symbol.DOT + Args.USEONCE_KEY, false);

    /**
     * Constructs a new Caching instance.
     */
    public Caching() {
        // No initialization required.
    }

    /**
     * Generates a cache key based on the mapper interface and method.
     *
     * @param context The provider context containing mapper interface and method information.
     * @return A cache key, which is interned to be used as a lock object.
     */
    private static String cacheKey(ProviderContext context) {
        return (context.getMapperType().getName() + Symbol.DOT + context.getMapperMethod().getName()).intern();
    }

    /**
     * Checks if the mapper method is annotated with {@code @Lang(Caching.class)}.
     *
     * @param context The provider context containing method information.
     * @throws RuntimeException if the method is not annotated with {@code @Lang(Caching.class)}.
     */
    private static void isAnnotationPresentLang(ProviderContext context) {
        Method mapperMethod = context.getMapperMethod();
        if (mapperMethod.isAnnotationPresent(Lang.class)) {
            Lang lang = mapperMethod.getAnnotation(Lang.class);
            if (lang.value() == Caching.class) {
                return;
            }
        }
        throw new RuntimeException(
                mapperMethod + " need to configure @Lang(Caching.class) to use the Caching.cache method for caching");
    }

    /**
     * Caches the SQL script and its associated metadata.
     *
     * @param context           The provider context, containing mapper interface and method information.
     * @param entity            The entity metadata.
     * @param sqlScriptSupplier A supplier for the SQL script string.
     * @return The generated cache key.
     */
    public static String cache(ProviderContext context, TableMeta entity, SupplierX<String> sqlScriptSupplier) {
        String cacheKey = cacheKey(context);

        // Initialize metadata atomically for each provider key.
        CACHE_SQL.computeIfAbsent(cacheKey, k -> {
            isAnnotationPresentLang(context);
            return new SqlMetaCache(Assert.notNull(context), Assert.notNull(entity), Assert.notNull(sqlScriptSupplier));
        });

        return cacheKey;
    }

    /**
     * Caches a dynamic SQL script that depends on database dialect.
     *
     * <p>
     * This method is used for SQL that needs to be generated differently based on the database dialect. The SQL is
     * generated at execution time when the dialect is known, not at cache time.
     * </p>
     *
     * @param context                  The provider context, containing mapper interface and method information.
     * @param entity                   The entity metadata.
     * @param dynamicSqlScriptFunction A function that accepts Dialect and returns SQL script.
     * @return The generated cache key.
     */
    public static String cacheDynamic(
            ProviderContext context,
            TableMeta entity,
            FunctionX<Dialect, String> dynamicSqlScriptFunction) {
        String cacheKey = cacheKey(context);

        // Initialize metadata atomically for each provider key.
        CACHE_SQL.computeIfAbsent(cacheKey, k -> {
            isAnnotationPresentLang(context);
            return new SqlMetaCache(Assert.notNull(context), Assert.notNull(entity), null,
                    Assert.notNull(dynamicSqlScriptFunction));
        });

        return cacheKey;
    }

    /**
     * Gets the configuration-local SQL source cache.
     *
     * @param configuration the MyBatis configuration
     * @return the configuration-local cache
     */
    private static Map<String, SqlSource> configurationCache(Configuration configuration) {
        synchronized (CONFIGURATION_CACHE_KEY_MAP) {
            return CONFIGURATION_CACHE_KEY_MAP.computeIfAbsent(configuration, k -> new ConcurrentHashMap<>());
        }
    }

    /**
     * Clears cached SQL sources for a single MyBatis configuration.
     *
     * @param configuration the MyBatis configuration to clear
     */
    public static void clear(Configuration configuration) {
        synchronized (CONFIGURATION_CACHE_KEY_MAP) {
            CONFIGURATION_CACHE_KEY_MAP.remove(configuration);
        }
    }

    /**
     * Clears cached SQL sources for all MyBatis configurations.
     */
    public static void clearAll() {
        synchronized (CONFIGURATION_CACHE_KEY_MAP) {
            CONFIGURATION_CACHE_KEY_MAP.clear();
        }
    }

    /**
     * Creates an {@link SqlSource}. If a cached version exists, it is reused; otherwise, a new instance is created and
     * cached. This method uses the script parameter as a key to look up pre-parsed SQL metadata.
     *
     * @param configuration The MyBatis configuration.
     * @param script        The script content or a cache key.
     * @param parameterType The parameter type class.
     * @return The created or cached {@link SqlSource}.
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        return createSqlSource(configuration, script, parameterType, null);
    }

    /**
     * Preserves parameter-name information on MyBatis 3.6 while remaining callable on MyBatis 3.5. This overload
     * intentionally has no Override annotation because it is absent from the 3.5 superclass.
     *
     * @param configuration the MyBatis configuration
     * @param script        the SQL script or provider cache key
     * @param parameterType the mapper parameter type
     * @param resolver      the method parameter-name resolver, if supplied by MyBatis
     * @return the parsed or cached SQL source
     */
    public SqlSource createSqlSource(
            Configuration configuration,
            String script,
            Class<?> parameterType,
            ParamNameResolver resolver) {
        if (CACHE_SQL.containsKey(script)) {
            Map<String, SqlSource> cacheKeyMap = configurationCache(configuration);
            return cacheKeyMap.computeIfAbsent(script, k -> {
                SqlMetaCache cache = CACHE_SQL.get(k);
                if (cache == SqlMetaCache.NULL) {
                    throw new RuntimeException(
                            k + " => CACHE_SQL is NULL, you need to configure mapper.provider.cacheSql.useOnce=false");
                }
                cache.getTableMeta().initRuntimeContext(configuration, cache.getContext(), k);
                MappedStatement ms = configuration.getMappedStatement(k);
                Registry.SPI.customize(cache.getTableMeta(), ms, cache.getContext());

                // Check if this is dynamic SQL (depends on dialect)
                if (cache.isDynamic()) {
                    // For dynamic SQL, we don't generate SQL at this point
                    // Instead, we create a SqlSource that will generate SQL at runtime based on dialect
                    if (Logger.isTraceEnabled()) {
                        Logger.trace(false, "Mapper", "Dynamic SQL deferred until runtime: cacheKey={}", k);
                    }
                    // Create a placeholder SQL source that will be replaced by DynamicSqlSource
                    SqlSource originalSource = parseSqlSource(
                            configuration,
                            "<script></script>",
                            parameterType,
                            resolver);
                    SqlSource dynamicSource = new org.miaixz.bus.mapper.parsing.SqlSource(ms, Normal.EMPTY,
                            originalSource, cache);
                    if (USE_ONCE) {
                        CACHE_SQL.put(k, SqlMetaCache.NULL);
                    }
                    return dynamicSource;
                } else {
                    // Static SQL - generate at cache time
                    String sqlScript = cache.getSqlScript();
                    if (Logger.isTraceEnabled()) {
                        Logger.trace(
                                false,
                                "Mapper",
                                "Static SQL source created: cacheKey={}, sqlChars={}",
                                k,
                                sqlScript.length());
                    }
                    SqlSource sqlSource = parseSqlSource(configuration, sqlScript, parameterType, resolver);
                    sqlSource = SqlSourceEnhancer.SPI
                            .customize(sqlSource, cache.getTableMeta(), ms, cache.getContext());
                    if (USE_ONCE) {
                        CACHE_SQL.put(k, SqlMetaCache.NULL);
                    }
                    return sqlSource;
                }
            });
        } else {
            return parseSqlSource(configuration, script, parameterType, resolver);
        }
    }

}
