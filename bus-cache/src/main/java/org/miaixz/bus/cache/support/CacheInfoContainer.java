/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * A container that parses and caches method-level caching annotations.
 * <p>
 * This class acts as a central repository for the metadata derived from {@link Cached}, {@link Invalid},
 * {@link CachedGet}, and {@link CacheKey} annotations. It provides a unified, cached access point to a method's
 * complete cache configuration and performs static validation to ensure the annotation setup is valid.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheInfoContainer {

    /**
     * An internal cache mapping a {@link Method} to its parsed caching information.
     */
    private static final ConcurrentMap<Method, CachePair<AnnoHolder, MethodHolder>> cacheMap = new ConcurrentHashMap<>();

    /**
     * Retrieves the combined cache information for a given method.
     * <p>
     * This method uses an internal cache to avoid re-parsing annotations on subsequent calls for the same method.
     * </p>
     *
     * @param method The method to get cache information for.
     * @return A {@link CachePair} containing the annotation and method metadata.
     */
    public static CachePair<AnnoHolder, MethodHolder> getCacheInfo(Method method) {
        return cacheMap.computeIfAbsent(method, CacheInfoContainer::doGetMethodInfo);
    }

    /**
     * Performs the actual parsing of a method's caching annotations.
     *
     * @param method The method to parse.
     * @return A {@link CachePair} containing the parsed metadata.
     */
    private static CachePair<AnnoHolder, MethodHolder> doGetMethodInfo(Method method) {
        AnnoHolder annoHolder = getAnnoHolder(method);
        MethodHolder methodHolder = getMethodHolder(method, annoHolder);
        return CachePair.of(annoHolder, methodHolder);
    }

    /**
     * Parses all caching-related annotations on a method and its parameters to build an {@link AnnoHolder}.
     *
     * @param method The method to parse.
     * @return A fully configured {@link AnnoHolder}.
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
     * Scans the method's parameters for {@link CacheKey} annotations.
     *
     * @param builder      The {@link AnnoHolder.Builder} to populate.
     * @param pAnnotations The array of parameter annotations.
     * @return The same builder instance for chaining.
     */
    private static AnnoHolder.Builder scanKeys(AnnoHolder.Builder builder, Annotation[][] pAnnotations) {
        int multiIndex = -1;
        String id = Normal.EMPTY;
        Map<Integer, CacheKey> cacheKeyMap = new LinkedHashMap<>(pAnnotations.length);
        for (int pIndex = 0; pIndex < pAnnotations.length; ++pIndex) {
            for (Annotation annotation : pAnnotations[pIndex]) {
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
     * Extracts metadata from the {@link Cached} annotation.
     *
     * @param builder The builder to populate.
     * @param cached  The annotation instance.
     * @return The same builder instance for chaining.
     */
    private static AnnoHolder.Builder scanCached(AnnoHolder.Builder builder, Cached cached) {
        return builder.setCache(cached.value()).setPrefix(cached.prefix()).setExpire(cached.expire());
    }

    /**
     * Extracts metadata from the {@link CachedGet} annotation.
     *
     * @param builder   The builder to populate.
     * @param cachedGet The annotation instance.
     * @return The same builder instance for chaining.
     */
    private static AnnoHolder.Builder scanCachedGet(AnnoHolder.Builder builder, CachedGet cachedGet) {
        return builder.setCache(cachedGet.value()).setPrefix(cachedGet.prefix()).setExpire(CacheExpire.NO);
    }

    /**
     * Extracts metadata from the {@link Invalid} annotation.
     *
     * @param builder The builder to populate.
     * @param invalid The annotation instance.
     * @return The same builder instance for chaining.
     */
    private static AnnoHolder.Builder scanInvalid(AnnoHolder.Builder builder, Invalid invalid) {
        return builder.setCache(invalid.value()).setPrefix(invalid.prefix()).setExpire(CacheExpire.NO);
    }

    /**
     * Creates a {@link MethodHolder} containing metadata about the method's return type and validates the annotation
     * configuration.
     *
     * @param method     The method being analyzed.
     * @param annoHolder The parsed annotation metadata.
     * @return A new {@link MethodHolder} instance.
     */
    private static MethodHolder getMethodHolder(Method method, AnnoHolder annoHolder) {
        boolean isCollectionReturn = Collection.class.isAssignableFrom(method.getReturnType());
        boolean isMapReturn = Map.class.isAssignableFrom(method.getReturnType());
        staticAnalyze(method.getParameterTypes(), annoHolder, isCollectionReturn, isMapReturn);
        return new MethodHolder(isCollectionReturn);
    }

    /**
     * Performs static analysis of the method's signature and annotation configuration to ensure validity.
     *
     * @param pTypes             The method's parameter types.
     * @param annoHolder         The parsed annotation metadata.
     * @param isCollectionReturn Whether the method returns a Collection.
     * @param isMapReturn        Whether the method returns a Map.
     * @throws RuntimeException if the configuration is invalid.
     */
    private static void staticAnalyze(
            Class<?>[] pTypes,
            AnnoHolder annoHolder,
            boolean isCollectionReturn,
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
     * Determines if a {@link CacheKey} annotation indicates a multi-key operation (i.e., its value contains '#i').
     *
     * @param cacheKey The annotation to check.
     * @return {@code true} if it is a multi-key annotation, otherwise {@code false}.
     */
    private static boolean isMulti(CacheKey cacheKey) {
        if (null == cacheKey) {
            return false;
        }
        String value = cacheKey.value();
        return StringKit.isNotEmpty(value) && value.contains("#i");
    }

    /**
     * Checks if the caching configuration is invalid because no key can be generated.
     *
     * @param pTypes     The method's parameter types.
     * @param annoHolder The parsed annotation metadata.
     * @return {@code true} if no key can be generated, otherwise {@code false}.
     */
    private static boolean isInvalidParam(Class<?>[] pTypes, AnnoHolder annoHolder) {
        Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
        String prefix = annoHolder.getPrefix();
        return (null == pTypes || pTypes.length == 0 || cacheKeyMap.isEmpty()) && StringKit.isEmpty(prefix);
    }

    /**
     * Checks if more than one parameter is designated as a multi-key source.
     *
     * @param keyMap The map of parameter indices to {@link CacheKey} annotations.
     * @return {@code true} if more than one multi-key is found, otherwise {@code false}.
     */
    private static boolean isInvalidMultiCount(Map<Integer, CacheKey> keyMap) {
        int multiCount = 0;
        for (CacheKey cacheKey : keyMap.values()) {
            if (isMulti(cacheKey)) {
                if (++multiCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the 'field' attribute of @CacheKey is used incorrectly.
     *
     * @param isMapReturn        Whether the method returns a Map.
     * @param isCollectionReturn Whether the method returns a Collection.
     * @param field              The value of the 'field' attribute.
     * @return {@code true} if the usage is invalid, otherwise {@code false}.
     */
    private static boolean isInvalidIdentifier(boolean isMapReturn, boolean isCollectionReturn, String field) {
        if (isMapReturn && !StringKit.isEmpty(field)) {
            Logger.warn("@CacheKey's 'field = \"{}\"' is useless.", field);
            return false;
        }
        return !StringKit.isEmpty(field) && !isCollectionReturn;
    }

    /**
     * Checks if a multi-key operation that returns a Collection has a 'field' specified for mapping results.
     *
     * @param isCollectionReturn Whether the method returns a Collection.
     * @param id                 The value of the 'field' attribute.
     * @return {@code true} if the configuration is invalid, otherwise {@code false}.
     */
    private static boolean isInvalidResult(boolean isCollectionReturn, String id) {
        return isCollectionReturn && StringKit.isEmpty(id);
    }

    /**
     * Checks if the parameter annotated as a multi-key source is a Collection or an array.
     *
     * @param paramType The type of the parameter.
     * @return {@code true} if the type is invalid for a multi-key source, otherwise {@code false}.
     */
    private static boolean isInvalidMulti(Class<?> paramType) {
        return !Collection.class.isAssignableFrom(paramType) && !paramType.isArray();
    }

}
