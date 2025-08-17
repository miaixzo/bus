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
package org.miaixz.bus.cache.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.CacheExpire;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.cache.magic.annotation.CacheKey;
import org.miaixz.bus.cache.magic.annotation.Cached;
import org.miaixz.bus.cache.magic.annotation.CachedGet;
import org.miaixz.bus.cache.magic.annotation.Invalid;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * 缓存信息容器
 * <p>
 * 用于解析和缓存方法的缓存相关信息，将@Cached、@Invalid、@CachedGet以及@CacheKey注解信息融合在一起。 提供方法缓存配置的统一访问入口，并支持缓存配置的验证。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheInfoContainer {

    /**
     * 方法缓存信息映射，键为方法对象，值为注解持有者和方法持有者的键值对
     */
    private static final ConcurrentMap<Method, CachePair<AnnoHolder, MethodHolder>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 获取方法缓存信息
     * <p>
     * 从缓存中获取方法缓存信息，如果缓存中不存在，则计算并缓存
     * </p>
     *
     * @param method 方法对象
     * @return 缓存信息键值对，包含注解持有者和方法持有者
     */
    public static CachePair<AnnoHolder, MethodHolder> getCacheInfo(Method method) {
        return cacheMap.computeIfAbsent(method, CacheInfoContainer::doGetMethodInfo);
    }

    /**
     * 获取方法缓存信息
     *
     * @param method 方法对象
     * @return 缓存信息键值对，包含注解持有者和方法持有者
     */
    private static CachePair<AnnoHolder, MethodHolder> doGetMethodInfo(Method method) {
        AnnoHolder annoHolder = getAnnoHolder(method);
        MethodHolder methodHolder = getMethodHolder(method, annoHolder);
        return CachePair.of(annoHolder, methodHolder);
    }

    /**
     * 获取注解持有者
     *
     * @param method 方法对象
     * @return 注解持有者
     */
    private static AnnoHolder getAnnoHolder(Method method) {
        AnnoHolder.Builder builder = AnnoHolder.Builder.newBuilder(method);
        Annotation[][] pAnnotations = method.getParameterAnnotations();
        scanKeys(builder, pAnnotations);
        if (method.isAnnotationPresent(Cached.class)) {
            scanCached(builder, method.getAnnotation(Cached.class));
        } else if (method.isAnnotationPresent(CachedGet.class)) {
            scanCachedGet(builder, method.getAnnotation(CachedGet.class));
        } else {
            scanInvalid(builder, method.getAnnotation(Invalid.class));
        }
        return builder.build();
    }

    /**
     * 扫描参数键注解
     *
     * @param builder      注解持有者构建器
     * @param pAnnotations 参数注解数组
     * @return 注解持有者构建器
     */
    private static AnnoHolder.Builder scanKeys(AnnoHolder.Builder builder, Annotation[][] pAnnotations) {
        int multiIndex = -1;
        String id = Normal.EMPTY;
        Map<Integer, CacheKey> cacheKeyMap = new LinkedHashMap<>(pAnnotations.length);
        for (int pIndex = 0; pIndex < pAnnotations.length; ++pIndex) {
            Annotation[] annotations = pAnnotations[pIndex];
            for (Annotation annotation : annotations) {
                if (annotation instanceof CacheKey) {
                    CacheKey cacheKey = (CacheKey) annotation;
                    cacheKeyMap.put(pIndex, cacheKey);
                    if (isMulti(cacheKey)) {
                        multiIndex = pIndex;
                        id = cacheKey.field();
                    }
                }
            }
        }
        return builder.setCacheKeyMap(cacheKeyMap).setMultiIndex(multiIndex).setId(id);
    }

    /**
     * 扫描@Cached注解
     *
     * @param builder 注解持有者构建器
     * @param cached  @Cached注解
     * @return 注解持有者构建器
     */
    private static AnnoHolder.Builder scanCached(AnnoHolder.Builder builder, Cached cached) {
        return builder.setCache(cached.value()).setPrefix(cached.prefix()).setExpire(cached.expire());
    }

    /**
     * 扫描@CachedGet注解
     *
     * @param builder   注解持有者构建器
     * @param cachedGet @CachedGet注解
     * @return 注解持有者构建器
     */
    private static AnnoHolder.Builder scanCachedGet(AnnoHolder.Builder builder, CachedGet cachedGet) {
        return builder.setCache(cachedGet.value()).setPrefix(cachedGet.prefix()).setExpire(CacheExpire.NO);
    }

    /**
     * 扫描@Invalid注解
     *
     * @param builder 注解持有者构建器
     * @param invalid @Invalid注解
     * @return 注解持有者构建器
     */
    private static AnnoHolder.Builder scanInvalid(AnnoHolder.Builder builder, Invalid invalid) {
        return builder.setCache(invalid.value()).setPrefix(invalid.prefix()).setExpire(CacheExpire.NO);
    }

    /**
     * 获取方法持有者
     *
     * @param method     方法对象
     * @param annoHolder 注解持有者
     * @return 方法持有者
     */
    private static MethodHolder getMethodHolder(Method method, AnnoHolder annoHolder) {
        boolean isCollectionReturn = Collection.class.isAssignableFrom(method.getReturnType());
        boolean isMapReturn = Map.class.isAssignableFrom(method.getReturnType());
        staticAnalyze(method.getParameterTypes(), annoHolder, isCollectionReturn, isMapReturn);
        return new MethodHolder(isCollectionReturn);
    }

    /**
     * 静态分析方法参数和返回类型
     *
     * @param pTypes             参数类型数组
     * @param annoHolder         注解持有者
     * @param isCollectionReturn 是否返回集合类型
     * @param isMapReturn        是否返回Map类型
     */
    private static void staticAnalyze(Class<?>[] pTypes, AnnoHolder annoHolder, boolean isCollectionReturn,
            boolean isMapReturn) {
        if (isInvalidParam(pTypes, annoHolder)) {
            throw new RuntimeException("cache need at least one param key");
        } else if (isInvalidMultiCount(annoHolder.getCacheKeyMap())) {
            throw new RuntimeException("only one multi key");
        } else {
            Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
            for (Map.Entry<Integer, CacheKey> entry : cacheKeyMap.entrySet()) {
                Integer argIndex = entry.getKey();
                CacheKey cacheKey = entry.getValue();
                if (isMulti(cacheKey) && isInvalidMulti(pTypes[argIndex])) {
                    throw new RuntimeException("multi need a collection instance param");
                }
                if (isMulti(cacheKey) && isInvalidResult(isCollectionReturn, cacheKey.field())) {
                    throw new RuntimeException("multi cache && collection method return need a result field");
                }
                if (isInvalidIdentifier(isMapReturn, isCollectionReturn, cacheKey.field())) {
                    throw new RuntimeException("id method a collection return method");
                }
            }
        }
    }

    /**
     * 判断是否为多键缓存
     *
     * @param cacheKey 缓存键注解
     * @return 如果是多键缓存则返回true，否则返回false
     */
    private static boolean isMulti(CacheKey cacheKey) {
        if (null == cacheKey) {
            return false;
        }
        String value = cacheKey.value();
        if (StringKit.isEmpty(value)) {
            return false;
        }
        return value.contains("#i");
    }

    /**
     * 判断参数是否无效
     *
     * @param pTypes     参数类型数组
     * @param annoHolder 注解持有者
     * @return 如果参数无效则返回true，否则返回false
     */
    private static boolean isInvalidParam(Class<?>[] pTypes, AnnoHolder annoHolder) {
        Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
        String prefix = annoHolder.getPrefix();
        return (null == pTypes || pTypes.length == 0 || cacheKeyMap.isEmpty()) && StringKit.isEmpty(prefix);
    }

    /**
     * 判断多键数量是否无效
     *
     * @param keyMap 缓存键映射
     * @return 如果多键数量无效则返回true，否则返回false
     */
    private static boolean isInvalidMultiCount(Map<Integer, CacheKey> keyMap) {
        int multiCount = 0;
        for (CacheKey cacheKey : keyMap.values()) {
            if (isMulti(cacheKey)) {
                ++multiCount;
                if (multiCount > 1) {
                    break;
                }
            }
        }
        return multiCount > 1;
    }

    /**
     * 判断标识符是否无效
     *
     * @param isMapReturn        是否返回Map类型
     * @param isCollectionReturn 是否返回集合类型
     * @param field              字段名
     * @return 如果标识符无效则返回true，否则返回false
     */
    private static boolean isInvalidIdentifier(boolean isMapReturn, boolean isCollectionReturn, String field) {
        if (isMapReturn && !StringKit.isEmpty(field)) {
            Logger.warn("@CacheKey's 'field = \"{}\"' is useless.", field);
            return false;
        }
        return !StringKit.isEmpty(field) && !isCollectionReturn;
    }

    /**
     * 判断结果是否无效
     *
     * @param isCollectionReturn 是否返回集合类型
     * @param id                 标识字段
     * @return 如果结果无效则返回true，否则返回false
     */
    private static boolean isInvalidResult(boolean isCollectionReturn, String id) {
        return isCollectionReturn && StringKit.isEmpty(id);
    }

    /**
     * 判断多键参数类型是否无效
     *
     * @param paramType 参数类型
     * @return 如果多键参数类型无效则返回true，否则返回false
     */
    private static boolean isInvalidMulti(Class<?> paramType) {
        return !Collection.class.isAssignableFrom(paramType) && !paramType.isArray();
    }

}