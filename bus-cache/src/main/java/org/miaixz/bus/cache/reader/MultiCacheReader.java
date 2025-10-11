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
import org.miaixz.bus.cache.support.Addables;
import org.miaixz.bus.cache.support.PreventObjects;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * A cache reader for handling multi-key (batch) cache operations.
 * <p>
 * This class implements the logic for batch reads, handling scenarios involving partial hits, full hits, and full
 * misses. It is capable of processing methods that return either a {@link Map} or a {@link Collection}, and it
 * integrates with the metrics component to record hit rates.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiCacheReader extends AbstractReader {

    /**
     * Merges the results from a method invocation with cached values into a single map.
     *
     * @param resultMapType        The expected type of the final result map.
     * @param proceedEntryValueMap The map returned by the original method invocation (containing non-cached values).
     * @param key2MultiEntry       A map from a cache key to its corresponding source entry (from the multi-key
     *                             argument).
     * @param hitKeyValueMap       The map of keys to values that were found in the cache.
     * @return A new map containing the merged results.
     */
    private static Map<Object, Object> mergeMap(
            Class<?> resultMapType,
            Map<Object, Object> proceedEntryValueMap,
            Map<String, Object> key2MultiEntry,
            Map<String, Object> hitKeyValueMap) {
        Map<Object, Object> resultMap = Addables.newMap(resultMapType, proceedEntryValueMap);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    /**
     * Converts cache hits into a result map.
     *
     * @param resultMapType  The expected type of the final result map.
     * @param key2MultiEntry A map from a cache key to its corresponding source entry.
     * @param hitKeyValueMap The map of keys to values that were found in the cache.
     * @return A new map containing only the values from the cache.
     */
    private static Map<Object, Object> toMap(
            Class<?> resultMapType,
            Map<String, Object> key2MultiEntry,
            Map<String, Object> hitKeyValueMap) {
        Map<Object, Object> resultMap = Addables.newMap(resultMapType, null);
        mergeCacheValueToResultMap(resultMap, hitKeyValueMap, key2MultiEntry);
        return resultMap;
    }

    /**
     * Merges cached values into a result map.
     *
     * @param resultMap      The map to merge values into.
     * @param hitKeyValueMap The map of keys to values that were found in the cache.
     * @param key2MultiEntry A map from a cache key to its corresponding source entry.
     */
    private static void mergeCacheValueToResultMap(
            Map<Object, Object> resultMap,
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

    /**
     * Merges the results from a method invocation with cached values into a single collection.
     *
     * @param collectionType    The expected type of the final result collection.
     * @param proceedCollection The collection returned by the original method invocation.
     * @param hitKeyValueMap    The map of keys to values that were found in the cache.
     * @return A new collection containing the merged results.
     */
    private static Collection<Object> mergeCollection(
            Class<?> collectionType,
            Collection<Object> proceedCollection,
            Map<String, Object> hitKeyValueMap) {
        Collection<Object> resultCollection = Addables.newCollection(collectionType, proceedCollection);
        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);
        return resultCollection;
    }

    /**
     * Converts cache hits into a result collection.
     *
     * @param collectionType The expected type of the final result collection.
     * @param hitKeyValueMap The map of keys to values that were found in the cache.
     * @return A new collection containing only the values from the cache.
     */
    private static Collection<Object> toCollection(Class<?> collectionType, Map<String, Object> hitKeyValueMap) {
        Collection<Object> resultCollection = Addables.newCollection(collectionType, null);
        mergeCacheValueToResultCollection(resultCollection, hitKeyValueMap);
        return resultCollection;
    }

    /**
     * Merges cached values into a result collection.
     *
     * @param resultCollection The collection to merge values into.
     * @param hitKeyValueMap   The map of keys to values that were found in the cache.
     */
    private static void mergeCacheValueToResultCollection(
            Collection<Object> resultCollection,
            Map<String, Object> hitKeyValueMap) {
        for (Object inCacheValue : hitKeyValueMap.values()) {
            if (PreventObjects.isPrevent(inCacheValue)) {
                continue;
            }
            resultCollection.add(inCacheValue);
        }
    }

    /**
     * Executes the multi-key cache read operation.
     *
     * @param annoHolder   The holder for the caching annotations.
     * @param methodHolder The holder for method metadata.
     * @param baseInvoker  The proxy chain invoker.
     * @param needWrite    If {@code true}, write results to cache on miss.
     * @return The final result, assembled from the cache and/or method invocation.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    @Override
    public Object read(AnnoHolder annoHolder, MethodHolder methodHolder, ProxyChain baseInvoker, boolean needWrite)
            throws Throwable {
        // 1. Generate keys
        Map[] pair = Builder.generateMultiKey(annoHolder, baseInvoker.getArguments());
        Map<String, Object> key2MultiEntry = pair[1];

        // 2. Request from cache
        Set<String> keys = key2MultiEntry.keySet();
        CacheKeys cacheKeys = manage.readBatch(annoHolder.getCache(), keys);
        doRecord(cacheKeys, annoHolder);

        Object result;
        // 3. Handle misses (partial or full miss)
        if (!cacheKeys.getMissKeySet().isEmpty()) {
            result = handlePartHit(baseInvoker, cacheKeys, annoHolder, methodHolder, pair, needWrite);
        }
        // 4. Handle full hit
        else {
            Map<String, Object> keyValueMap = cacheKeys.getHitKeyMap();
            result = handleFullHit(baseInvoker, keyValueMap, methodHolder, key2MultiEntry);
        }
        return result;
    }

    /**
     * Handles the scenario where some, but not all, keys were found in the cache.
     *
     * @param baseInvoker  The proxy chain invoker.
     * @param cacheKeys    The results from the initial cache read.
     * @param annoHolder   The annotation metadata.
     * @param methodHolder The method metadata.
     * @param pair         The key mapping pair.
     * @param needWrite    Flag indicating if results should be written to cache.
     * @return The merged result.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    private Object handlePartHit(
            ProxyChain baseInvoker,
            CacheKeys cacheKeys,
            AnnoHolder annoHolder,
            MethodHolder methodHolder,
            Map[] pair,
            boolean needWrite) throws Throwable {
        Map<Object, String> multiEntry2Key = pair[0];
        Map<String, Object> key2MultiEntry = pair[1];
        Set<String> missKeys = cacheKeys.getMissKeySet();
        Map<String, Object> hitKeyValueMap = cacheKeys.getHitKeyMap();

        // Invoke the original method with only the arguments for the missed keys
        Object[] missArgs = toMissArgs(
                missKeys,
                key2MultiEntry,
                baseInvoker.getArguments(),
                annoHolder.getMultiIndex());
        Object proceed = doLogInvoke(() -> baseInvoker.proceed(missArgs));

        Object result;
        if (null != proceed) {
            Class<?> returnType = proceed.getClass();
            methodHolder.setReturnType(returnType);

            if (Map.class.isAssignableFrom(returnType)) {
                Map<Object, Object> proceedEntryValueMap = (Map<Object, Object>) proceed;
                if (needWrite) {
                    Map<String, Object> keyValueMap = Builder
                            .mapToKeyValue(proceedEntryValueMap, missKeys, multiEntry2Key, context.getPrevent());
                    manage.writeBatch(annoHolder.getCache(), keyValueMap, annoHolder.getExpire());
                }
                result = mergeMap(returnType, proceedEntryValueMap, key2MultiEntry, hitKeyValueMap);
            } else {
                Collection<Object> proceedCollection = asCollection(proceed, returnType);
                if (needWrite) {
                    Map<String, Object> keyValueMap = Builder.collectionToKeyValue(
                            proceedCollection,
                            annoHolder.getId(),
                            missKeys,
                            multiEntry2Key,
                            context.getPrevent());
                    manage.writeBatch(annoHolder.getCache(), keyValueMap, annoHolder.getExpire());
                }
                Collection<Object> resultCollection = mergeCollection(returnType, proceedCollection, hitKeyValueMap);
                result = asType(resultCollection, returnType);
            }
        } else {
            // If the method returns null, treat it as a full hit with only the cached values.
            result = handleFullHit(baseInvoker, hitKeyValueMap, methodHolder, key2MultiEntry);
        }
        return result;
    }

    /**
     * Converts a collection to the specified return type (either Collection or Array).
     *
     * @param collection The collection to convert.
     * @param returnType The target type.
     * @return The converted object.
     */
    private Object asType(Collection<Object> collection, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return collection;
        }
        return collection.toArray();
    }

    /**
     * Converts an object (which may be an array) into a Collection.
     *
     * @param proceed    The object to convert.
     * @param returnType The class of the object.
     * @return The object as a Collection.
     */
    private Collection<Object> asCollection(Object proceed, Class<?> returnType) {
        if (Collection.class.isAssignableFrom(returnType)) {
            return (Collection<Object>) proceed;
        }
        return Arrays.asList((Object[]) proceed);
    }

    /**
     * Handles the scenario where all requested keys were found in the cache.
     *
     * @param baseInvoker    The proxy chain invoker.
     * @param keyValueMap    The map of keys to values found in the cache.
     * @param methodHolder   The method metadata.
     * @param key2MultiEntry A map from cache key to its source entry.
     * @return The reconstructed result object.
     * @throws Throwable if the underlying method invocation throws an exception.
     */
    private Object handleFullHit(
            ProxyChain baseInvoker,
            Map<String, Object> keyValueMap,
            MethodHolder methodHolder,
            Map<String, Object> key2MultiEntry) throws Throwable {
        Object result;
        Class<?> returnType = methodHolder.getReturnType();
        // If the return type is unknown (e.g., after an app restart), invoke the method to discover it.
        if (null == returnType) {
            result = doLogInvoke(baseInvoker::proceed);
            if (null != result) {
                methodHolder.setReturnType(result.getClass());
            }
        } else {
            // Reconstruct the result from the cached values.
            if (methodHolder.isCollection()) {
                result = toCollection(returnType, keyValueMap);
            } else {
                result = toMap(returnType, key2MultiEntry, keyValueMap);
            }
        }
        return result;
    }

    /**
     * Creates a new arguments array for re-invoking the original method with only the missed keys.
     *
     * @param missKeys   The set of keys that were not found in the cache.
     * @param keyIdMap   A map from cache key to its source entry.
     * @param args       The original method arguments.
     * @param multiIndex The index of the argument that contains the collection of keys.
     * @return A new arguments array.
     */
    private Object[] toMissArgs(Set<String> missKeys, Map<String, Object> keyIdMap, Object[] args, int multiIndex) {
        List<Object> missedMultiEntries = missKeys.stream().map(keyIdMap::get).collect(Collectors.toList());
        Class<?> multiArgType = args[multiIndex].getClass();
        Addables.Addable addable = Addables.newAddable(multiArgType, missedMultiEntries.size());
        args[multiIndex] = addable.addAll(missedMultiEntries).get();
        return args;
    }

    /**
     * Records cache hit and request counts for metrics.
     *
     * @param cacheKeys  The results from the cache read.
     * @param annoHolder The annotation metadata.
     */
    private void doRecord(CacheKeys cacheKeys, AnnoHolder annoHolder) {
        Set<String> missKeys = cacheKeys.getMissKeySet();
        int hitCount = cacheKeys.getHitKeyMap().size();
        int totalCount = hitCount + missKeys.size();
        Logger.info("multi cache hit rate: {}/{}, missed keys: {}", hitCount, totalCount, missKeys);
        if (null != this.metrics) {
            String pattern = Builder.generatePattern(annoHolder);
            this.metrics.hitIncr(pattern, hitCount);
            this.metrics.reqIncr(pattern, totalCount);
        }
    }

}
