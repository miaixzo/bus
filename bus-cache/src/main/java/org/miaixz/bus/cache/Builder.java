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
package org.miaixz.bus.cache;

import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.annotation.CacheKey;
import org.miaixz.bus.cache.support.PreventObjects;
import org.miaixz.bus.cache.support.SpelCalculator;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 通用方法构建
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 参数前缀
     */
    private static final String X_ARGS_PREFIX = "args";
    /**
     * 方法到模式字符串的缓存映射
     */
    private static final ConcurrentMap<Method, String> patterns = new ConcurrentHashMap<>();
    /**
     * 预定义的参数名数组
     */
    private static String[] X_ARGS = { X_ARGS_PREFIX + 0, X_ARGS_PREFIX + 1, X_ARGS_PREFIX + 2, X_ARGS_PREFIX + 3,
            X_ARGS_PREFIX + 4, X_ARGS_PREFIX + 5, X_ARGS_PREFIX + 6, X_ARGS_PREFIX + 7, X_ARGS_PREFIX + 8,
            X_ARGS_PREFIX + 9, X_ARGS_PREFIX + 10, X_ARGS_PREFIX + 11, X_ARGS_PREFIX + 12, X_ARGS_PREFIX + 13,
            X_ARGS_PREFIX + 14, X_ARGS_PREFIX + 15, X_ARGS_PREFIX + Normal._16, X_ARGS_PREFIX + 17, X_ARGS_PREFIX + 18,
            X_ARGS_PREFIX + 19 };
    /**
     * 方法参数名缓存，键为方法对象，值为参数名数组
     */
    private static final ConcurrentMap<Method, String[]> methodParameterNames = new ConcurrentHashMap<>();
    /**
     * 是否首次执行的标志
     */
    private static boolean isFirst = true;

    /**
     * 获取方法参数名称
     * <p>
     * 从缓存中获取方法参数名称，如果缓存中不存在，则计算并缓存
     * </p>
     *
     * @param method 方法对象
     * @return 方法参数名称数组
     */
    public static String[] getArgNames(Method method) {
        return methodParameterNames.computeIfAbsent(method, Builder::doGetArgNamesWithJava8);
    }

    /**
     * 获取X格式的参数名称
     * <p>
     * 生成xArgN格式的参数名称数组
     * </p>
     *
     * @param valueSize 参数数量
     * @return X格式的参数名称数组
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
     * 使用Java 8特性获取方法参数名称
     * <p>
     * Java 8之后提供了获取参数名方法，但需要编译时添加`–parameters`参数支持， 如`javac –parameters`，不然参数名为'arg0'格式
     * </p>
     *
     * @param method 方法对象
     * @return 方法参数名称数组
     */
    private static String[] doGetArgNamesWithJava8(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] argNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
        if (isFirst && argNames.length != 0 && argNames[0].equals("arg0")) {
            Logger.warn("compile not set '–parameters', used default method parameter names");
            isFirst = false;
        }
        return argNames;
    }

    /**
     * 生成单键
     * <p>
     * 根据注解信息和方法参数生成单个缓存键
     * </p>
     *
     * @param annoHolder 注解持有者
     * @param argValues  方法参数值数组
     * @return 生成的缓存键
     */
    public static String generateSingleKey(AnnoHolder annoHolder, Object[] argValues) {
        String[] argNames = getArgNames(annoHolder.getMethod());
        Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
        String prefix = annoHolder.getPrefix();
        return doGenerateKey(cacheKeyMap, prefix, argNames, argValues);
    }

    /**
     * 生成多键
     * <p>
     * 根据注解信息和方法参数生成多个缓存键，适用于集合或数组类型的参数
     * </p>
     *
     * @param annoHolder 注解持有者
     * @param argValues  方法参数值数组
     * @return 包含两个映射的数组：第一个是多元素到键的映射，第二个是键到多元素的映射
     */
    public static Map[] generateMultiKey(AnnoHolder annoHolder, Object[] argValues) {
        /* 由于要将Collection内的元素作为Map的Key, 因此就要求元素必须实现的hashcode & equals方法 */
        Map<Object, String> multiEntry2Key = new LinkedHashMap<>();
        Map<String, Object> key2MultiEntry = new LinkedHashMap<>();

        // 准备要拼装key所需的原材料
        // 标记为multi的参数
        Collection multiArgEntries = getMultiArgEntries(argValues[annoHolder.getMultiIndex()]);
        // 参数索引 -> CacheKey
        Map<Integer, CacheKey> argIndex2CacheKey = annoHolder.getCacheKeyMap();
        // 全局prefix
        String prefix = annoHolder.getPrefix();
        // 根据方法获取原始的参数名
        String[] argNames = getArgNames(annoHolder.getMethod());
        // 给参数名添加一个`#i`遍历指令
        String[] appendArgNames = (String[]) appendArray(argNames, "i");
        int i = 0;
        for (Object multiElement : multiArgEntries) {
            // 给参数值数组的`#i`指令赋值
            Object[] appendArgValues = appendArray(argValues, i);
            String key = doGenerateKey(argIndex2CacheKey, prefix, appendArgNames, appendArgValues);
            key2MultiEntry.put(key, multiElement);
            multiEntry2Key.put(multiElement, key);
            ++i;
        }
        return new Map[] { multiEntry2Key, key2MultiEntry };
    }

    /**
     * 生成键的核心方法
     *
     * @param parameterIndex2CacheKey 参数索引到缓存键注解的映射
     * @param prefix                  键前缀
     * @param argNames                参数名数组
     * @param argValues               参数值数组
     * @return 生成的缓存键
     */
    private static String doGenerateKey(Map<Integer, CacheKey> parameterIndex2CacheKey, String prefix,
            String[] argNames, Object[] argValues) {
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
     * 获取当spel表达式为空(null or '')时，默认的拼装keyPart
     * <p>
     * 注意：当multi的spel表达式为空时，这时会将整个`Collection`实例作为keyPart（当然，这种情况不会发生）...
     * </p>
     *
     * @param argValues 参数值数组
     * @param argIndex  参数索引
     * @return 默认值
     */
    private static Object getDefaultValue(Object[] argValues, int argIndex) {
        return argValues[argIndex];
    }

    /**
     * 将标记为`multi`的参数转成`Collection`实例
     *
     * @param multiArg 多参数对象
     * @return 集合实例
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
            // 此处应该在multi参数校验的时候确保只能为Collection、Map、Object[]三种类型
            return Arrays.stream((Object[]) multiArg).collect(Collectors.toList());
        }
    }

    /**
     * 追加元素到数组
     *
     * @param origin 原始数组
     * @param append 要追加的元素
     * @return 追加元素后的新数组
     */
    private static Object[] appendArray(Object[] origin, Object append) {
        Object[] dest = Arrays.copyOf(origin, origin.length + 1);
        dest[origin.length] = append;
        return dest;
    }

    /**
     * 将Map转换为键值映射
     * <p>
     * 将方法返回的Map转换为键值映射，用于批量写入缓存。 支持防击穿逻辑，当开启防击穿功能时，会将未命中的键设置为防击穿对象。
     * </p>
     *
     * @param proceedEntryValueMap 方法返回的Map
     * @param missKeys             未命中的键集合
     * @param multiEntry2Key       多元素到键的映射
     * @param prevent              防击穿开关
     * @return 键值映射
     */
    public static Map<String, Object> mapToKeyValue(Map proceedEntryValueMap, Set<String> missKeys,
            Map<Object, String> multiEntry2Key, EnumValue.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedEntryValueMap.size());
        proceedEntryValueMap.forEach((multiArgEntry, value) -> {
            String key = multiEntry2Key.get(multiArgEntry);
            if (StringKit.isEmpty(key)) {
                return;
            }
            missKeys.remove(key);
            keyValueMap.put(key, value);
        });

        // 触发防击穿逻辑
        if (prevent == EnumValue.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }
        return keyValueMap;
    }

    /**
     * 将Collection转换为键值映射
     * <p>
     * 将方法返回的Collection转换为键值映射，用于批量写入缓存。 支持防击穿逻辑，当开启防击穿功能时，会将未命中的键设置为防击穿对象。
     * </p>
     *
     * @param proceedCollection 方法返回的Collection
     * @param idSpel            ID的SpEL表达式
     * @param missKeys          未命中的键集合
     * @param id2Key            ID到键的映射
     * @param prevent           防击穿开关
     * @return 键值映射
     */
    public static Map<String, Object> collectionToKeyValue(Collection proceedCollection, String idSpel,
            Set<String> missKeys, Map<Object, String> id2Key, EnumValue.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedCollection.size());
        for (Object value : proceedCollection) {
            Object id = SpelCalculator.calcSpelWithNoContext(idSpel, value);
            String key = id2Key.get(id);
            if (StringKit.isEmpty(key)) {
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
     * 生成缓存模式
     * <p>
     * 根据注解信息生成缓存命中率统计的模式字符串，如果缓存中存在则直接返回
     * </p>
     *
     * @param annoHolder 注解持有者
     * @return 缓存模式字符串
     */
    public static String generatePattern(AnnoHolder annoHolder) {
        return patterns.computeIfAbsent(annoHolder.getMethod(), (method) -> doPatternCombiner(annoHolder));
    }

    /**
     * 组合模式字符串
     * <p>
     * 将注解持有者中的前缀和缓存键表达式组合成一个唯一的模式标识
     * </p>
     *
     * @param annoHolder 注解持有者
     * @return 组合后的模式字符串
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
