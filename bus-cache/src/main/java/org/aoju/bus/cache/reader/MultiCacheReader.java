/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.cache.reader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.aoju.bus.cache.Context;
import org.aoju.bus.cache.Manage;
import org.aoju.bus.cache.Provider;
import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheKeys;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.proxy.ProxyChain;
import org.aoju.bus.cache.support.*;
import org.aoju.bus.logger.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
@Singleton
public class MultiCacheReader extends AbstractReader {

    @Inject
    private Manage cacheManager;

    @Inject
    private Context config;

    @Inject(optional = true)
    private Provider baseProvider;

    private static Map mergeMap(Class<?> resultMapType,
                                Map proceedEntryValueMap,
                                Map<String, Object> key2MultiEntry,
                                Map<String, Object> hitKeyValueMap) {

        Map resultMap = Addables.newMap(resultMapType, proceedEntryValueMap);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    private static Map toMap(Class<?> resultMapType,
                             Map<String, Object> key2MultiEntry,
                             Map<String, Object> hitKeyValueMap) {
        Map resultMap = Addables.newMap(resultMapType, null);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    // 将缓存命中的内容都合并到返回值内
    private static void mergeCacheValueToResultMap(Map resultMap,
                                                   Map<String, Object> hitKeyValueMap,
                                                   Map<String, Object> key2MultiEntry) {
        for (Map.Entry<String, Object> entry : hitKeyValueMap.entrySet()) {
            Object inCacheValue = entry.getValue();
            if (PreventObjects.isPrevent(inCacheValue)) {
                continue;
            }

            String cacheKey = entry.getKey();
            Object multiArgEntry = key2MultiEntry.get(cacheKey);

            resultMap.put(multiArgEntry, inCacheValue);
        }
    }

    private static Collection mergeCollection(Class<?> collectionType,
                                              Collection proceedCollection,
                                              Map<String, Object> hitKeyValueMap) {
        Collection resultCollection = Addables.newCollection(collectionType, proceedCollection);
        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);
        return resultCollection;
    }

    private static Collection toCollection(Class<?> collectionType,
                                           Map<String, Object> hitKeyValueMap) {

        Collection resultCollection = Addables.newCollection(collectionType, null);

        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);

        return resultCollection;
    }

    private static void mergeCacheValueToResultCollection(Collection resultCollection,
                                                          Map<String, Object> hitKeyValueMap) {
        for (Object inCacheValue : hitKeyValueMap.values()) {
            if (PreventObjects.isPrevent(inCacheValue)) {
                continue;
            }

            resultCollection.add(inCacheValue);
        }
    }

    @Override
    public Object read(CacheHolder cacheHolder, CacheMethod cacheMethod, ProxyChain baseInvoker, boolean needWrite) throws Throwable {
        // compose keys
        Map[] pair = KeyGenerator.generateMultiKey(cacheHolder, baseInvoker.getArgs());
        Map<String, Object> key2MultiEntry = pair[1];

        // request cache
        Set<String> keys = key2MultiEntry.keySet();
        CacheKeys cacheKeys = cacheManager.readBatch(cacheHolder.getCache(), keys);
        doRecord(cacheKeys, cacheHolder);

        Object result;
        // have miss keys : part hit || all not hit
        if (!cacheKeys.getMissKeySet().isEmpty()) {
            result = handlePartHit(baseInvoker, cacheKeys, cacheHolder, cacheMethod, pair, needWrite);
        }
        // no miss keys : all hit || empty key
        else {
            Map<String, Object> keyValueMap = cacheKeys.getHitKeyMap();
            result = handleFullHit(baseInvoker, keyValueMap, cacheMethod, key2MultiEntry);
        }

        return result;
    }

    private Object handlePartHit(ProxyChain baseInvoker, CacheKeys cacheKeys,
                                 CacheHolder cacheHolder, CacheMethod cacheMethod,
                                 Map[] pair, boolean needWrite) throws Throwable {

        Map<Object, String> multiEntry2Key = pair[0];
        Map<String, Object> key2MultiEntry = pair[1];

        Set<String> missKeys = cacheKeys.getMissKeySet();
        Map<String, Object> hitKeyValueMap = cacheKeys.getHitKeyMap();

        // 用未命中的keys调用方法
        Object[] missArgs = toMissArgs(missKeys, key2MultiEntry, baseInvoker.getArgs(), cacheHolder.getMultiIndex());
        Object proceed = doLogInvoke(() -> baseInvoker.proceed(missArgs));

        Object result;
        if (proceed != null) {
            Class<?> returnType = proceed.getClass();
            cacheMethod.setReturnType(returnType);
            if (Map.class.isAssignableFrom(returnType)) {
                Map proceedEntryValueMap = (Map) proceed;

                // 为了兼容@CachedGet注解, 客户端缓存
                if (needWrite) {
                    // 将方法调用返回的map转换成key_value_map写入Cache
                    Map<String, Object> keyValueMap = KeyValueUtils.mapToKeyValue(proceedEntryValueMap, missKeys, multiEntry2Key, config.getPrevent());
                    cacheManager.writeBatch(cacheHolder.getCache(), keyValueMap, cacheHolder.getExpire());
                }
                // 将方法调用返回的map与从Cache中读取的key_value_map合并返回
                result = mergeMap(returnType, proceedEntryValueMap, key2MultiEntry, hitKeyValueMap);
            } else {
                Collection proceedCollection = asCollection(proceed, returnType);

                // 为了兼容@CachedGet注解, 客户端缓存
                if (needWrite) {
                    // 将方法调用返回的collection转换成key_value_map写入Cache
                    Map<String, Object> keyValueMap = KeyValueUtils.collectionToKeyValue(proceedCollection, cacheHolder.getId(), missKeys, multiEntry2Key, config.getPrevent());
                    cacheManager.writeBatch(cacheHolder.getCache(), keyValueMap, cacheHolder.getExpire());
                }
                // 将方法调用返回的collection与从Cache中读取的key_value_map合并返回
                Collection resultCollection = mergeCollection(returnType, proceedCollection, hitKeyValueMap);
                result = asType(resultCollection, returnType);
            }
        } else {
            // read as full shooting
            result = handleFullHit(baseInvoker, hitKeyValueMap, cacheMethod, key2MultiEntry);
        }

        return result;
    }

    private Object asType(Collection collection, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return collection;
        }

        return collection.toArray();
    }

    private Collection asCollection(Object proceed, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return (Collection) proceed;
        }

        return Arrays.asList((Object[]) proceed);
    }

    private Object handleFullHit(ProxyChain baseInvoker, Map<String, Object> keyValueMap,
                                 CacheMethod cacheMethod, Map<String, Object> key2Id) throws Throwable {

        Object result;
        Class<?> returnType = cacheMethod.getReturnType();

        // when method return type not cached. case: full shooting when application restart
        if (returnType == null) {
            result = doLogInvoke(baseInvoker::proceed);

            // catch return type for next time
            if (result != null) {
                cacheMethod.setReturnType(result.getClass());
            }
        } else {
            if (cacheMethod.isCollection()) {
                result = toCollection(returnType, keyValueMap);
            } else {
                result = toMap(returnType, key2Id, keyValueMap);
            }
        }

        return result;
    }

    private Object[] toMissArgs(Set<String> missKeys, Map<String, Object> keyIdMap,
                                Object[] args, int multiIndex) {

        List<Object> missedMultiEntries = missKeys.stream()
                .map(keyIdMap::get)
                .collect(Collectors.toList());

        Class<?> multiArgType = args[multiIndex].getClass();

        // 对将Map作为CacheKey的支持就到这儿了, 不会再继续下去...
        Addables.Addable addable = Addables.newAddable(multiArgType, missedMultiEntries.size());
        args[multiIndex] = addable.addAll(missedMultiEntries).getResult();

        return args;
    }

    private void doRecord(CacheKeys cacheKeys, CacheHolder cacheHolder) {
        Set<String> missKeys = cacheKeys.getMissKeySet();

        // 计数
        int hitCount = cacheKeys.getHitKeyMap().size();
        int totalCount = hitCount + missKeys.size();
        Logger.info("multi cache hit rate: {}/{}, missed keys: {}",
                hitCount, totalCount, missKeys);

        if (this.baseProvider != null) {
            // 分组模板
            String pattern = PatternGenerator.generatePattern(cacheHolder);

            this.baseProvider.hitIncr(pattern, hitCount);
            this.baseProvider.reqIncr(pattern, totalCount);
        }
    }

}
