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
package org.miaixz.bus.mapper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.mapping.KeySqlSource;
import org.miaixz.bus.mapper.mapping.MapperTable;
import org.miaixz.bus.mapper.mapping.SqlCache;

/**
 * 缓存 XML 形式的 SqlSource，避免重复解析。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Caching extends XMLLanguageDriver {

    /**
     * 缓存 SqlCache 对象的映射，初始容量约为 1024（假设约 30 个实体，每个实体 25 个方法）。
     * <p>
     * 对于单一数据源，缓存最终可被清除；对于多数据源，必须保留缓存，因为无法确定清理时机。
     * </p>
     */
    private static final Map<String, SqlCache> CACHE_SQL = new ConcurrentHashMap<>(
            Keys.getInt(Args.DEFAULT_INITSIZE_KEY, 1024));

    /**
     * 按 Configuration 缓存 SqlSource，处理多数据源或多配置场景（如单元测试），确保一致性。
     */
    private static final Map<Configuration, Map<String, SqlSource>> CONFIGURATION_CACHE_KEY_MAP = new ConcurrentHashMap<>(
            4);

    /**
     * 是否仅使用一次缓存，默认为 false。若为 true，首次使用后清除缓存，允许 GC 清理。
     * <p>
     * 当使用 SqlSessionFactory 配置多数据源时，必须设为 false，避免 GC 清理影响新数据源。 对于单一 SqlSessionFactory 的多数据源场景，可设为 true。
     * </p>
     */
    private static final boolean USE_ONCE = Keys.getBoolean(Args.DEFAULT_USEONCE_KEY, false);

    /**
     * 根据接口和方法生成缓存键。
     *
     * @param providerContext 提供者上下文，包含方法和接口信息
     * @return 经过 String.intern 处理的缓存键，可作为锁对象
     */
    private static String cacheKey(ProviderContext providerContext) {
        return (providerContext.getMapperType().getName() + "." + providerContext.getMapperMethod().getName()).intern();
    }

    /**
     * 检查方法是否标注了 @Lang(Caching.class) 注解。
     *
     * @param providerContext 提供者上下文，包含方法信息
     * @throws RuntimeException 如果未配置 @Lang(Caching.class) 注解
     */
    private static void isAnnotationPresentLang(ProviderContext providerContext) {
        Method mapperMethod = providerContext.getMapperMethod();
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
     * 缓存 sqlScript 对应的 SQL 和配置。
     *
     * @param providerContext   提供者上下文，包含方法和接口信息
     * @param entity            实体类信息
     * @param sqlScriptSupplier SQL 脚本提供者
     * @return 缓存键
     */
    public static String cache(ProviderContext providerContext, MapperTable entity,
            Supplier<String> sqlScriptSupplier) {
        String cacheKey = cacheKey(providerContext);
        if (!CACHE_SQL.containsKey(cacheKey)) {
            isAnnotationPresentLang(providerContext);
            synchronized (cacheKey) {
                if (!CACHE_SQL.containsKey(cacheKey)) {
                    CACHE_SQL.put(cacheKey, new SqlCache(Objects.requireNonNull(providerContext),
                            Objects.requireNonNull(entity), Objects.requireNonNull(sqlScriptSupplier)));
                }
            }
        }
        return cacheKey;
    }

    /**
     * 创建 SqlSource，若缓存中存在则重用，否则生成新实例。
     *
     * @param configuration MyBatis 配置
     * @param script        脚本或缓存键
     * @param parameterType 参数类型
     * @return SqlSource 对象
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        if (CACHE_SQL.containsKey(script)) {
            String cacheKey = script;
            if (!(CONFIGURATION_CACHE_KEY_MAP.containsKey(configuration)
                    && CONFIGURATION_CACHE_KEY_MAP.get(configuration).containsKey(cacheKey))) {
                synchronized (cacheKey) {
                    if (!(CONFIGURATION_CACHE_KEY_MAP.containsKey(configuration)
                            && CONFIGURATION_CACHE_KEY_MAP.get(configuration).containsKey(cacheKey))) {
                        SqlCache cache = CACHE_SQL.get(cacheKey);
                        if (cache == SqlCache.NULL) {
                            throw new RuntimeException(script
                                    + " => CACHE_SQL is NULL, you need to configure mybatis.provider.cacheSql.useOnce=false");
                        }
                        cache.getEntity().initRuntimeContext(configuration, cache.getProviderContext(), cacheKey);
                        Map<String, SqlSource> cachekeyMap = CONFIGURATION_CACHE_KEY_MAP.computeIfAbsent(configuration,
                                k -> new ConcurrentHashMap<>());
                        MappedStatement ms = configuration.getMappedStatement(cacheKey);
                        Registry.SPI.customize(cache.getEntity(), ms, cache.getProviderContext());
                        String sqlScript = cache.getSqlScript();
                        if (Logger.isTraceEnabled()) {
                            Logger.trace("cacheKey - " + cacheKey + " :\n" + sqlScript + "\n");
                        }
                        SqlSource sqlSource = super.createSqlSource(configuration, sqlScript, parameterType);
                        sqlSource = KeySqlSource.SPI.customize(sqlSource, cache.getEntity(), ms,
                                cache.getProviderContext());
                        cachekeyMap.put(cacheKey, sqlSource);
                        if (USE_ONCE) {
                            CACHE_SQL.put(cacheKey, SqlCache.NULL);
                        }
                    }
                }
            }
            return CONFIGURATION_CACHE_KEY_MAP.get(configuration).get(cacheKey);
        } else {
            return super.createSqlSource(configuration, script, parameterType);
        }
    }

}