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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.cache.Context;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class KeyValue {

    public static Map<String, Object> mapToKeyValue(Map proceedEntryValueMap, Set<String> missKeys,
            Map<Object, String> multiEntry2Key, Context.Switch prevent) {
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
        if (prevent == Context.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }

        return keyValueMap;
    }

    public static Map<String, Object> collectionToKeyValue(Collection proceedCollection, String idSpel,
            Set<String> missKeys, Map<Object, String> id2Key, Context.Switch prevent) {
        Map<String, Object> keyValueMap = new HashMap<>(proceedCollection.size());

        for (Object value : proceedCollection) {
            Object id = SpelCalculator.calcSpelWithNoContext(idSpel, value);
            String key = id2Key.get(id);

            if (StringKit.isEmpty(key)) {
                missKeys.remove(key);
                keyValueMap.put(key, value);
            }
        }

        if (prevent == Context.Switch.ON && !missKeys.isEmpty()) {
            missKeys.forEach(key -> keyValueMap.put(key, PreventObjects.getPreventObject()));
        }
        return keyValueMap;
    }

}
