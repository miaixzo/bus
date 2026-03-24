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
package org.miaixz.bus.cache;

import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.annotation.CacheKey;
import org.miaixz.bus.cache.builtin.PreventObjects;
import org.miaixz.bus.cache.builtin.SpelCalculator;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A utility class for building cache keys, patterns, and other cache-related constructs.
 * <p>
 * This class provides static methods to handle the generation of cache keys from method arguments and annotations,
 * supporting both single and multi-key scenarios. It also includes helpers for processing results for batch operations
 * and generating patterns for statistics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * The prefix used for synthetic argument names, e.g., "args0", "args1".
     */
    private static final String X_ARGS_PREFIX = "args";

    /**
     * A cache mapping methods to their generated pattern strings for statistics.
     */
    private static final ConcurrentMap<Method, String> patterns = new ConcurrentHashMap<>();

    /**
     * A pre-defined array of synthetic argument names for quick access.
     */
    private static final String[] X_ARGS = { X_ARGS_PREFIX + 0, X_ARGS_PREFIX + 1, X_ARGS_PREFIX + 2, X_ARGS_PREFIX + 3,
            X_ARGS_PREFIX + 4, X_ARGS_PREFIX + 5, X_ARGS_PREFIX + 6, X_ARGS_PREFIX + 7, X_ARGS_PREFIX + 8,
            X_ARGS_PREFIX + 9, X_ARGS_PREFIX + 10, X_ARGS_PREFIX + 11, X_ARGS_PREFIX + 12, X_ARGS_PREFIX + 13,
            X_ARGS_PREFIX + 14, X_ARGS_PREFIX + 15, X_ARGS_PREFIX + 16, X_ARGS_PREFIX + 17, X_ARGS_PREFIX + 18,
            X_ARGS_PREFIX + 19 };

    /**
     * A cache mapping methods to their parameter names.
     */
    private static final ConcurrentMap<Method, String[]> methodParameterNames = new ConcurrentHashMap<>();

    /**
     * A flag to ensure the warning about missing compiler parameters is logged only once. Uses {@link AtomicBoolean} to
     * guarantee thread-safe one-shot semantics.
     */
    private static final AtomicBoolean isFirst = new AtomicBoolean(true);

    /**
     * Retrieves the parameter names for a given method.
     * <p>
     * It uses a cache to store the parameter names. If not found in the cache, it resolves them using Java 8's
     * reflection capabilities and caches the result.
     * </p>
     *
     * @param method The method whose parameter names are to be retrieved.
     * @return An array of parameter names.
     */
    public static String[] getArgNames(Method method) {
        return methodParameterNames.computeIfAbsent(method, Builder::doGetArgNamesWithJava8);
    }

    /**
     * Generates an array of synthetic argument names (e.g., "args0", "args1", ...).
     *
     * @param valueSize The number of argument names to generate.
     * @return An array of synthetic argument names.
     */
    public static String[] getXArgNames(int valueSize) {
        if (valueSize == 0) {
            return Normal.EMPTY_STRING_ARRAY;
        }
        String[] xArgs = new String[valueSize];
        for (int i = 0; i < valueSize; ++i) {
            xArgs[i] = i < X_ARGS.length ? X_ARGS[i] : X_ARGS_PREFIX + i;
        }
        return xArgs;
    }

    /**
     * Retrieves method parameter names using Java 8+ reflection.
     * <p>
     * This relies on the `-parameters` flag being passed to the Java compiler. If the flag is not present, the names
     * will be like "arg0", "arg1", etc., and a warning will be logged once.
     * </p>
     *
     * @param method The method to inspect.
     * @return An array of parameter names.
     */
    private static String[] doGetArgNamesWithJava8(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] argNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
        if (isFirst.compareAndSet(true, false) && argNames.length != 0 && argNames[0].equals("arg0")) {
            Logger.warn("compile not set '–parameters', used default method parameter names");
        }
        return argNames;
    }

    /**
     * Generates a single cache key based on annotation details and method arguments.
     *
     * @param annoHolder The annotation holder containing metadata about the cached method.
     * @param argValues  The actual arguments passed to the method.
     * @return The generated cache key as a string.
     */
    public static String generateSingleKey(AnnoHolder annoHolder, Object[] argValues) {
        String[] argNames = getArgNames(annoHolder.getMethod());
        Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
        String prefix = annoHolder.getPrefix();
        return doGenerateKey(cacheKeyMap, prefix, argNames, argValues);
    }

    /**
     * Generates multiple cache keys for a batch operation.
     * <p>
     * This is used when an argument is a collection or array, and a separate cache key needs to be generated for each
     * element.
     * </p>
     *
     * @param annoHolder The annotation holder containing metadata about the cached method.
     * @param argValues  The actual arguments passed to the method.
     * @return An array of two maps: the first maps each element of the collection to its generated key, and the second
     *         maps each generated key back to the element.
     */
    public static Map[] generateMultiKey(AnnoHolder annoHolder, Object[] argValues) {
        /* The elements of the collection will be used as Map keys, so they must implement hashCode() & equals(). */
        Map<Object, String> multiEntry2Key = new LinkedHashMap<>();
        Map<String, Object> key2MultiEntry = new LinkedHashMap<>();

        // Prepare materials for key assembly
        // The argument marked as the source for multiple keys
        Collection multiArgEntries = getMultiArgEntries(argValues[annoHolder.getMultiIndex()]);
        // Map of argument index to @CacheKey annotation
        Map<Integer, CacheKey> argIndex2CacheKey = annoHolder.getCacheKeyMap();
        // Global key prefix
        String prefix = annoHolder.getPrefix();
        // Original parameter names of the method
        String[] argNames = getArgNames(annoHolder.getMethod());
        // Append an iteration variable '#i' for SpEL context
        String[] appendArgNames = (String[]) appendArray(argNames, "i");
        int i = 0;
        for (Object multiElement : multiArgEntries) {
            // Assign the current iteration index to the '#i' variable in the SpEL context
            Object[] appendArgValues = appendArray(argValues, i);
            String key = doGenerateKey(argIndex2CacheKey, prefix, appendArgNames, appendArgValues);
            key2MultiEntry.put(key, multiElement);
            multiEntry2Key.put(multiElement, key);
            ++i;
        }
        return new Map[] { multiEntry2Key, key2MultiEntry };
    }

    /**
     * The core logic for generating a cache key.
     *
     * @param parameterIndex2CacheKey A map from parameter index to its {@link CacheKey} annotation.
     * @param prefix                  The key prefix.
     * @param argNames                The names of the method arguments.
     * @param argValues               The values of the method arguments.
     * @return The final generated cache key.
     */
    private static String doGenerateKey(
            Map<Integer, CacheKey> parameterIndex2CacheKey,
            String prefix,
            String[] argNames,
            Object[] argValues) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Map.Entry<Integer, CacheKey> entry : parameterIndex2CacheKey.entrySet()) {
            int argIndex = entry.getKey();
            String argSpel = entry.getValue().value();
            Object defaultValue = getDefaultValue(argValues, argIndex);
            Object keyPart = SpelCalculator.calcSpelValueWithContext(argSpel, argNames, argValues, defaultValue);
            sb.append(keyPart);
        }
        return sb.toString();
    }

    /**
     * Gets the default value for a key part when the SpEL expression is empty.
     * <p>
     * When a {@link CacheKey} has an empty SpEL expression, the entire argument at that index is used as the key part.
     * </p>
     *
     * @param argValues The method argument values.
     * @param argIndex  The index of the argument.
     * @return The argument value at the specified index.
     */
    private static Object getDefaultValue(Object[] argValues, int argIndex) {
        return argValues[argIndex];
    }

    /**
     * Converts the argument marked for a multi-key operation into a {@link Collection}.
     *
     * @param multiArg The object to be converted (can be a Collection, Map, or array).
     * @return A {@link Collection} instance.
     */
    private static Collection getMultiArgEntries(Object multiArg) {
        if (null == multiArg) {
            return Collections.emptyList();
        }
        if (multiArg instanceof Collection) {
            return (Collection) multiArg;
        } else if (multiArg instanceof Map) {
            return ((Map) multiArg).keySet();
        } else {
            // Validation should ensure that the multi-arg is one of Collection, Map, or Object[]
            return Arrays.stream((Object[]) multiArg).collect(Collectors.toList());
        }
    }

    /**
     * Appends an element to an array, returning a new, larger array.
     *
     * @param origin The original array.
     * @param append The element to append.
     * @return A new array containing all original elements plus the appended element.
     */
    private static Object[] appendArray(Object[] origin, Object append) {
        Object[] dest = Arrays.copyOf(origin, origin.length + 1);
        dest[origin.length] = append;
        return dest;
    }

    /**
     * Converts a result map from a method into a key-value map for batch cache writing.
     * <p>
     * It also handles cache penetration prevention by inserting a placeholder for keys that were requested but not
     * found in the method's result.
     * </p>
     *
     * @param proceedEntryValueMap The map returned by the original method.
     * @param missKeys             The set of keys that were not found in the cache initially.
     * @param multiEntry2Key       A map from the multi-key source element to its generated cache key.
     * @param prevent              A switch to enable or disable cache penetration prevention.
     * @return A map of cache keys to values, ready for batch writing.
     */
    public static Map<String, Object> mapToKeyValue(
            Map proceedEntryValueMap,
            Set<String> missKeys,
            Map<Object, String> multiEntry2Key,
            EnumValue.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedEntryValueMap.size());
        proceedEntryValueMap.forEach((multiArgEntry, value) -> {
            String key = multiEntry2Key.get(multiArgEntry);
            if (StringKit.isEmpty(key)) {
                return;
            }
            missKeys.remove(key);
            keyValueMap.put(key, value);
        });

        // Trigger cache penetration prevention logic
        if (prevent == EnumValue.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }
        return keyValueMap;
    }

    /**
     * Converts a result collection from a method into a key-value map for batch cache writing.
     * <p>
     * It uses a SpEL expression to extract an ID from each element in the collection, which is then used to look up the
     * corresponding cache key.
     * </p>
     *
     * @param proceedCollection The collection returned by the original method.
     * @param idSpel            The SpEL expression to extract the ID from each collection element.
     * @param missKeys          The set of keys that were not found in the cache initially.
     * @param id2Key            A map from the extracted ID to its generated cache key.
     * @param prevent           A switch to enable or disable cache penetration prevention.
     * @return A map of cache keys to values, ready for batch writing.
     */
    public static Map<String, Object> collectionToKeyValue(
            Collection proceedCollection,
            String idSpel,
            Set<String> missKeys,
            Map<Object, String> id2Key,
            EnumValue.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedCollection.size());
        for (Object value : proceedCollection) {
            Object id = SpelCalculator.calcSpelWithNoContext(idSpel, value);
            String key = id2Key.get(id);
            if (StringKit.isNotEmpty(key)) {
                missKeys.remove(key);
                keyValueMap.put(key, value);
            }
        }

        if (prevent == EnumValue.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }
        return keyValueMap;
    }

    /**
     * Converts a map of string keys and object values into a flat byte array for Redis {@code MSET}.
     *
     * @param keyValueMap The map to convert.
     * @param serializer  The serializer to use for values.
     * @return A byte array of interleaved keys and serialized values.
     */
    public static byte[][] toByteArray(Map<String, Object> keyValueMap, Serializer serializer) {
        byte[][] kvs = new byte[keyValueMap.size() * 2][];
        int index = 0;
        for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
            kvs[index++] = entry.getKey().getBytes();
            kvs[index++] = serializer.serialize(entry.getValue());
        }
        return kvs;
    }

    /**
     * Converts a collection of string keys into a 2D byte array for Redis commands.
     *
     * @param keys The collection of keys.
     * @return A 2D byte array where each inner array is the byte representation of a key.
     */
    public static byte[][] toByteArray(Collection<String> keys) {
        byte[][] array = new byte[keys.size()][];
        int index = 0;
        for (String text : keys) {
            array[index++] = text.getBytes();
        }
        return array;
    }

    /**
     * Converts a list of byte array values back into a map of string keys to objects.
     *
     * @param keys        The original collection of keys, used for mapping.
     * @param bytesValues The list of serialized values returned from Redis.
     * @param serializer  The serializer to use for deserialization.
     * @return A map of keys to their deserialized object values.
     */
    public static Map<String, Object> toObjectMap(
            Collection<String> keys,
            List<byte[]> bytesValues,
            Serializer serializer) {
        int index = 0;
        Map<String, Object> result = new HashMap<>(keys.size());
        for (String key : keys) {
            byte[] valueBytes = bytesValues.get(index++);
            if (valueBytes != null) {
                Object value = serializer.deserialize(valueBytes);
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Generates and caches a pattern string for a method, used for statistics.
     * <p>
     * The pattern is created by combining the key prefix and the SpEL expressions from {@link CacheKey} annotations.
     * </p>
     *
     * @param annoHolder The annotation holder containing metadata.
     * @return The generated (or cached) pattern string.
     */
    public static String generatePattern(AnnoHolder annoHolder) {
        // The pattern depends only on the annotation metadata (prefix + key expressions), not on runtime
        // argument values, so it is safe to cache by Method. However, to avoid stale captures when the
        // same Method could theoretically appear in multiple annotation configurations, compute the value
        // from the holder directly and use computeIfAbsent only when the pattern for this exact holder
        // has not been stored yet. We derive a stable key from the method and store the full pattern.
        return patterns.computeIfAbsent(annoHolder.getMethod(), method -> doPatternCombiner(annoHolder));
    }

    /**
     * Combines the prefix and key expressions into a single pattern identifier.
     *
     * @param annoHolder The annotation holder.
     * @return The combined pattern string.
     */
    private static String doPatternCombiner(AnnoHolder annoHolder) {
        StringBuilder sb = new StringBuilder(annoHolder.getPrefix());
        Collection<CacheKey> cacheKeys = annoHolder.getCacheKeyMap().values();
        for (CacheKey cacheKey : cacheKeys) {
            sb.append(cacheKey.value());
        }
        return sb.toString();
    }

}
