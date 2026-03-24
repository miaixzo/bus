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
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A copier that copies key-value pairs from a source Map to a target Map.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapToMapCopier extends AbstractCopier<Map, Map> {

    /**
     * The generic parameter types of the target Map.
     */
    private final Type[] typeArguments;

    /**
     * Constructs a new {@code MapToMapCopier} instance.
     *
     * @param source      The source Map. Must not be {@code null}.
     * @param target      The target Map object. Must not be {@code null}.
     * @param targetType  The generic type of the target Map.
     * @param copyOptions The options to configure the copying process. If {@code null}, default options will be used.
     */
    public MapToMapCopier(final Map source, final Map target, final Type targetType, final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.typeArguments = TypeKit.getTypeArguments(targetType);
    }

    /**
     * Performs the property copying operation from the source Map to the target Map.
     *
     * @return The target Map with copied properties.
     */
    @Override
    public Map copy() {
        this.source.forEach((sKey, sValue) -> {
            if (null == sKey) {
                return;
            }

            // Edit key-value pair.
            final MutableEntry<Object, Object> entry = copyOptions.editField(sKey.toString(), sValue);
            if (null == entry) {
                return;
            }
            sKey = entry.getKey();
            // If the key is null after conversion, skip.
            if (null == sKey) {
                return;
            }
            sValue = entry.getValue();
            // If ignoreNullValue is true and the value is null, skip.
            if (copyOptions.ignoreNullValue && sValue == null) {
                return;
            }

            final Object targetValue = target.get(sKey);
            // In non-override mode, if the target value exists, skip.
            if (!copyOptions.override && null != targetValue) {
                return;
            }

            // Convert the source value to the target value's type.
            if (null != typeArguments) {
                sValue = this.copyOptions.convertField(typeArguments[1], sValue);
            }

            // Assign to target.
            target.put(sKey, sValue);
        });
        return this.target;
    }

}
