/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.support;

import com.google.common.base.Strings;
import org.aoju.bus.cache.annotation.CacheKey;
import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.entity.Expire;
import org.aoju.bus.cache.entity.Pair;
import org.aoju.bus.logger.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 定位: 将@Cached、@Invalid、@CachedGet、(@CachedPut未来)以及将@CacheKey整体融合到一起
 *
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class CacheInfoContainer {

    private static final ConcurrentMap<Method, Pair<CacheHolder, CacheMethod>> cacheMap = new ConcurrentHashMap<>();

    public static Pair<CacheHolder, CacheMethod> getCacheInfo(Method method) {
        return cacheMap.computeIfAbsent(method, CacheInfoContainer::doGetMethodInfo);
    }

    private static Pair<CacheHolder, CacheMethod> doGetMethodInfo(Method method) {
        CacheHolder cacheHolder = getAnnoHolder(method);
        CacheMethod cacheMethod = getMethodHolder(method, cacheHolder);

        return Pair.of(cacheHolder, cacheMethod);
    }

    /****
     * cache key doGetMethodInfo
     ****/

    private static CacheHolder getAnnoHolder(Method method) {

        CacheHolder.Builder builder = CacheHolder.Builder.newBuilder(method);

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

    private static CacheHolder.Builder scanKeys(CacheHolder.Builder builder, Annotation[][] pAnnotations) {
        int multiIndex = -1;
        String id = "";
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

        return builder
                .setCacheKeyMap(cacheKeyMap)
                .setMultiIndex(multiIndex)
                .setId(id);
    }

    private static CacheHolder.Builder scanCached(CacheHolder.Builder builder, Cached cached) {
        return builder
                .setCache(cached.value())
                .setPrefix(cached.prefix())
                .setExpire(cached.expire());
    }

    private static CacheHolder.Builder scanCachedGet(CacheHolder.Builder builder, CachedGet cachedGet) {
        return builder
                .setCache(cachedGet.value())
                .setPrefix(cachedGet.prefix())
                .setExpire(Expire.NO);
    }

    private static CacheHolder.Builder scanInvalid(CacheHolder.Builder builder, Invalid invalid) {
        return builder
                .setCache(invalid.value())
                .setPrefix(invalid.prefix())
                .setExpire(Expire.NO);
    }

    /***
     * cache method doGetMethodInfo
     ***/

    private static CacheMethod getMethodHolder(Method method, CacheHolder cacheHolder) {
        boolean isCollectionReturn = Collection.class.isAssignableFrom(method.getReturnType());
        boolean isMapReturn = Map.class.isAssignableFrom(method.getReturnType());

        staticAnalyze(method.getParameterTypes(),
                cacheHolder,
                isCollectionReturn,
                isMapReturn);

        return new CacheMethod(isCollectionReturn);
    }

    private static void staticAnalyze(Class<?>[] pTypes, CacheHolder cacheHolder,
                                      boolean isCollectionReturn, boolean isMapReturn) {
        if (isInvalidParam(pTypes, cacheHolder)) {
            throw new RuntimeException("cache need at least one param key");
        } else if (isInvalidMultiCount(cacheHolder.getCacheKeyMap())) {
            throw new RuntimeException("only one multi key");
        } else {
            Map<Integer, CacheKey> cacheKeyMap = cacheHolder.getCacheKeyMap();
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

    private static boolean isMulti(CacheKey cacheKey) {
        if (cacheKey == null) {
            return false;
        }

        String value = cacheKey.value();
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        return value.contains("#i");
    }

    private static boolean isInvalidParam(Class<?>[] pTypes, CacheHolder cacheHolder) {
        Map<Integer, CacheKey> cacheKeyMap = cacheHolder.getCacheKeyMap();
        String prefix = cacheHolder.getPrefix();

        return (pTypes == null
                || pTypes.length == 0
                || cacheKeyMap.isEmpty())
                && Strings.isNullOrEmpty(prefix);
    }

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

    private static boolean isInvalidIdentifier(boolean isMapReturn,
                                               boolean isCollectionReturn,
                                               String field) {
        if (isMapReturn && !Strings.isNullOrEmpty(field)) {
            Logger.warn("@CacheKey's 'field = \"{}\"' is useless.", field);
            return false;
        }

        return !Strings.isNullOrEmpty(field) && !isCollectionReturn;
    }

    private static boolean isInvalidResult(boolean isCollectionReturn, String id) {
        return isCollectionReturn && Strings.isNullOrEmpty(id);
    }

    private static boolean isInvalidMulti(Class<?> paramType) {
        return !Collection.class.isAssignableFrom(paramType)
                && !paramType.isArray();
        // 永久不能放开  && !Map.class.isAssignableFrom(paramType);
    }
}
