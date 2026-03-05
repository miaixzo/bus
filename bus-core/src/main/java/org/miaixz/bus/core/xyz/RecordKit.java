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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;

/**
 * Utility class for `java.lang.Record`.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordKit {

    /**
     * Checks if the given class is a Record.
     *
     * @param clazz The class.
     * @return `true` if it is a Record class.
     */
    public static boolean isRecord(final Class<?> clazz) {
        return null != clazz && clazz.isRecord();
    }

    /**
     * Gets all record components (name and type) of a Record class.
     *
     * @param recordClass The Record class.
     * @return An array of map entries representing the components.
     */
    public static Map.Entry<String, Type>[] getRecordComponents(final Class<?> recordClass) {
        final RecordComponent[] components = recordClass.getRecordComponents();
        if (null == components) {
            return null;
        }
        final Map.Entry<String, Type>[] entries = new Map.Entry[components.length];
        for (int i = 0; i < components.length; i++) {
            entries[i] = new AbstractMap.SimpleEntry<>(components[i].getName(), components[i].getGenericType());
        }
        return entries;
    }

    /**
     * Instantiates a Record class.
     *
     * @param recordClass   The Record class.
     * @param valueProvider A provider for the constructor arguments.
     * @return A new instance of the Record.
     */
    public static Object newInstance(final Class<?> recordClass, final ValueProvider<String> valueProvider) {
        final Map.Entry<String, Type>[] recordComponents = getRecordComponents(recordClass);
        final Object[] args = new Object[recordComponents.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = valueProvider.value(recordComponents[i].getKey(), recordComponents[i].getValue());
        }

        return ReflectKit.newInstance(recordClass, args);
    }

}
