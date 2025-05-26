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

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.annotation.CacheKey;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class KeyGenerator {

    public static String generateSingleKey(AnnoHolder annoHolder, Object[] argValues) {
        String[] argNames = ArgNameGenerator.getArgNames(annoHolder.getMethod());
        Map<Integer, CacheKey> cacheKeyMap = annoHolder.getCacheKeyMap();
        String prefix = annoHolder.getPrefix();

        return doGenerateKey(cacheKeyMap, prefix, argNames, argValues);
    }

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
        String[] argNames = ArgNameGenerator.getArgNames(annoHolder.getMethod());
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
     * 获取当spel表达式为空(null or '')时, 默认的拼装keyPart 注意: 当multi的spel表达式为空时, 这时会将整个`Collection`实例作为keyPart(当然, 这种情况不会发生)...
     */
    private static Object getDefaultValue(Object[] argValues, int argIndex) {
        return argValues[argIndex];
    }

    /**
     * 将标记为`multi`的参数转成`Collection`实例
     *
     * @param multiArg the object
     * @return the collection
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

    private static Object[] appendArray(Object[] origin, Object append) {
        Object[] dest = Arrays.copyOf(origin, origin.length + 1);
        dest[origin.length] = append;

        return dest;
    }

}
