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
package org.miaixz.bus.cache.reader;

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.CacheKeys;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.cache.support.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * 多缓存读取器
 * <p>
 * 用于处理多键缓存操作，支持批量读取、部分命中和全部命中场景。 能够处理Map和Collection类型的返回值，并提供缓存命中率统计功能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiCacheReader extends AbstractReader {

    /**
     * 合并Map类型的结果
     * <p>
     * 将方法执行返回的Map与缓存命中的键值对合并，返回合并后的Map
     * </p>
     *
     * @param resultMapType        返回结果Map的类型
     * @param proceedEntryValueMap 方法执行返回的Map
     * @param key2MultiEntry       键到多参数条目的映射
     * @param hitKeyValueMap       缓存命中的键值对
     * @return 合并后的Map
     */
    private static Map mergeMap(Class<?> resultMapType, Map proceedEntryValueMap, Map<String, Object> key2MultiEntry,
            Map<String, Object> hitKeyValueMap) {
        Map resultMap = Addables.newMap(resultMapType, proceedEntryValueMap);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    /**
     * 将缓存命中的键值对转换为Map类型的结果
     *
     * @param resultMapType  返回结果Map的类型
     * @param key2MultiEntry 键到多参数条目的映射
     * @param hitKeyValueMap 缓存命中的键值对
     * @return 转换后的Map
     */
    private static Map toMap(Class<?> resultMapType, Map<String, Object> key2MultiEntry,
            Map<String, Object> hitKeyValueMap) {
        Map resultMap = Addables.newMap(resultMapType, null);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    /**
     * 将缓存命中的内容合并到返回Map中
     *
     * @param resultMap      返回结果Map
     * @param hitKeyValueMap 缓存命中的键值对
     * @param key2MultiEntry 键到多参数条目的映射
     */
    private static void mergeCacheValueToResultMap(Map resultMap, Map<String, Object> hitKeyValueMap,
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

    /**
     * 合并Collection类型的结果
     * <p>
     * 将方法执行返回的Collection与缓存命中的值合并，返回合并后的Collection
     * </p>
     *
     * @param collectionType    返回结果Collection的类型
     * @param proceedCollection 方法执行返回的Collection
     * @param hitKeyValueMap    缓存命中的键值对
     * @return 合并后的Collection
     */
    private static Collection mergeCollection(Class<?> collectionType, Collection proceedCollection,
            Map<String, Object> hitKeyValueMap) {
        Collection resultCollection = Addables.newCollection(collectionType, proceedCollection);
        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);
        return resultCollection;
    }

    /**
     * 将缓存命中的键值对转换为Collection类型的结果
     *
     * @param collectionType 返回结果Collection的类型
     * @param hitKeyValueMap 缓存命中的键值对
     * @return 转换后的Collection
     */
    private static Collection toCollection(Class<?> collectionType, Map<String, Object> hitKeyValueMap) {
        Collection resultCollection = Addables.newCollection(collectionType, null);
        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);
        return resultCollection;
    }

    /**
     * 将缓存命中的内容合并到返回Collection中
     *
     * @param resultCollection 返回结果Collection
     * @param hitKeyValueMap   缓存命中的键值对
     */
    private static void mergeCacheValueToResultCollection(Collection resultCollection,
            Map<String, Object> hitKeyValueMap) {
        for (Object inCacheValue : hitKeyValueMap.values()) {
            if (PreventObjects.isPrevent(inCacheValue)) {
                continue;
            }
            resultCollection.add(inCacheValue);
        }
    }

    /**
     * 执行缓存读取操作
     *
     * @param annoHolder   注解持有者，包含缓存相关的注解信息
     * @param methodHolder 方法持有者，包含方法相关的信息
     * @param baseInvoker  代理调用链，用于执行原始方法
     * @param needWrite    是否需要写入缓存
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Override
    public Object read(AnnoHolder annoHolder, MethodHolder methodHolder, ProxyChain baseInvoker, boolean needWrite)
            throws Throwable {
        // 组装键
        Map[] pair = Builder.generateMultiKey(annoHolder, baseInvoker.getArguments());
        Map<String, Object> key2MultiEntry = pair[1];
        // 请求缓存
        Set<String> keys = key2MultiEntry.keySet();
        CacheKeys cacheKeys = manage.readBatch(annoHolder.getCache(), keys);
        doRecord(cacheKeys, annoHolder);
        Object result;
        // 有未命中键：部分命中或全部未命中
        if (!cacheKeys.getMissKeySet().isEmpty()) {
            result = handlePartHit(baseInvoker, cacheKeys, annoHolder, methodHolder, pair, needWrite);
        }
        // 没有未命中键：全部命中或空键
        else {
            Map<String, Object> keyValueMap = cacheKeys.getHitKeyMap();
            result = handleFullHit(baseInvoker, keyValueMap, methodHolder, key2MultiEntry);
        }
        return result;
    }

    /**
     * 处理部分命中场景
     *
     * @param baseInvoker  代理调用链
     * @param cacheKeys    缓存键集合
     * @param annoHolder   注解持有者
     * @param methodHolder 方法持有者
     * @param pair         键映射对
     * @param needWrite    是否需要写入缓存
     * @return 处理结果
     * @throws Throwable 可能抛出的异常
     */
    private Object handlePartHit(ProxyChain baseInvoker, CacheKeys cacheKeys, AnnoHolder annoHolder,
            MethodHolder methodHolder, Map[] pair, boolean needWrite) throws Throwable {
        Map<Object, String> multiEntry2Key = pair[0];
        Map<String, Object> key2MultiEntry = pair[1];
        Set<String> missKeys = cacheKeys.getMissKeySet();
        Map<String, Object> hitKeyValueMap = cacheKeys.getHitKeyMap();
        // 用未命中的keys调用方法
        Object[] missArgs = toMissArgs(missKeys, key2MultiEntry, baseInvoker.getArguments(),
                annoHolder.getMultiIndex());
        Object proceed = doLogInvoke(() -> baseInvoker.proceed(missArgs));
        Object result;
        if (null != proceed) {
            Class<?> returnType = proceed.getClass();
            methodHolder.setReturnType(returnType);
            if (Map.class.isAssignableFrom(returnType)) {
                Map proceedEntryValueMap = (Map) proceed;
                // 为了兼容@CachedGet注解, 客户端缓存
                if (needWrite) {
                    // 将方法调用返回的map转换成key_value_map写入Cache
                    Map<String, Object> keyValueMap = Builder.mapToKeyValue(proceedEntryValueMap, missKeys,
                            multiEntry2Key, context.getPrevent());
                    manage.writeBatch(annoHolder.getCache(), keyValueMap, annoHolder.getExpire());
                }
                // 将方法调用返回的map与从Cache中读取的key_value_map合并返回
                result = mergeMap(returnType, proceedEntryValueMap, key2MultiEntry, hitKeyValueMap);
            } else {
                Collection proceedCollection = asCollection(proceed, returnType);
                // 为了兼容@CachedGet注解, 客户端缓存
                if (needWrite) {
                    // 将方法调用返回的collection转换成key_value_map写入Cache
                    Map<String, Object> keyValueMap = Builder.collectionToKeyValue(proceedCollection,
                            annoHolder.getId(), missKeys, multiEntry2Key, context.getPrevent());
                    manage.writeBatch(annoHolder.getCache(), keyValueMap, annoHolder.getExpire());
                }
                // 将方法调用返回的collection与从Cache中读取的key_value_map合并返回
                Collection resultCollection = mergeCollection(returnType, proceedCollection, hitKeyValueMap);
                result = asType(resultCollection, returnType);
            }
        } else {
            // 作为全部命中处理
            result = handleFullHit(baseInvoker, hitKeyValueMap, methodHolder, key2MultiEntry);
        }
        return result;
    }

    /**
     * 将Collection转换为指定类型
     *
     * @param collection 集合对象
     * @param returnType 返回类型
     * @return 转换后的对象
     */
    private Object asType(Collection collection, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return collection;
        }
        return collection.toArray();
    }

    /**
     * 将对象转换为Collection
     *
     * @param proceed    原始对象
     * @param returnType 返回类型
     * @return 转换后的Collection
     */
    private Collection asCollection(Object proceed, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return (Collection) proceed;
        }
        return Arrays.asList((Object[]) proceed);
    }

    /**
     * 处理全部命中场景
     *
     * @param baseInvoker    代理调用链
     * @param keyValueMap    缓存命中的键值对
     * @param methodHolder   方法持有者
     * @param key2MultiEntry 键到多参数条目的映射
     * @return 处理结果
     * @throws Throwable 可能抛出的异常
     */
    private Object handleFullHit(ProxyChain baseInvoker, Map<String, Object> keyValueMap, MethodHolder methodHolder,
            Map<String, Object> key2MultiEntry) throws Throwable {
        Object result;
        Class<?> returnType = methodHolder.getReturnType();
        // 当方法返回类型未被缓存时，例如：应用重启后的全部命中
        if (null == returnType) {
            result = doLogInvoke(baseInvoker::proceed);
            // 捕获返回类型以备下次使用
            if (null != result) {
                methodHolder.setReturnType(result.getClass());
            }
        } else {
            if (methodHolder.isCollection()) {
                result = toCollection(returnType, keyValueMap);
            } else {
                result = toMap(returnType, key2MultiEntry, keyValueMap);
            }
        }
        return result;
    }

    /**
     * 将未命中的键转换为方法参数
     *
     * @param missKeys   未命中的键集合
     * @param keyIdMap   键到ID的映射
     * @param args       原始方法参数
     * @param multiIndex 多参数索引
     * @return 转换后的方法参数
     */
    private Object[] toMissArgs(Set<String> missKeys, Map<String, Object> keyIdMap, Object[] args, int multiIndex) {
        List<Object> missedMultiEntries = missKeys.stream().map(keyIdMap::get).collect(Collectors.toList());
        Class<?> multiArgType = args[multiIndex].getClass();
        // 对将Map作为CacheKey的支持就到这儿了, 不会再继续下去...
        Addables.Addable addable = Addables.newAddable(multiArgType, missedMultiEntries.size());
        args[multiIndex] = addable.addAll(missedMultiEntries).get();
        return args;
    }

    /**
     * 记录缓存命中率
     *
     * @param cacheKeys  缓存键集合
     * @param annoHolder 注解持有者
     */
    private void doRecord(CacheKeys cacheKeys, AnnoHolder annoHolder) {
        Set<String> missKeys = cacheKeys.getMissKeySet();
        // 计数
        int hitCount = cacheKeys.getHitKeyMap().size();
        int totalCount = hitCount + missKeys.size();
        Logger.info("multi cache hit rate: {}/{}, missed keys: {}", hitCount, totalCount, missKeys);
        if (null != this.metrics) {
            // 分组模板
            String pattern = Builder.generatePattern(annoHolder);
            this.metrics.hitIncr(pattern, hitCount);
            this.metrics.reqIncr(pattern, totalCount);
        }
    }

}