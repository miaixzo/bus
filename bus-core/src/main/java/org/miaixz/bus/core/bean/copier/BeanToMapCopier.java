/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A copier that copies properties from a Bean to a Map.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanToMapCopier extends AbstractCopier<Object, Map> {

    /**
     * The generic parameter types of the target Map.
     */
    private final Type[] typeArguments;

    /**
     * Constructs a new {@code BeanToMapCopier} instance.
     *
     * @param source      The source Bean object. Must not be {@code null}.
     * @param target      The target Map object. Must not be {@code null}.
     * @param targetType  The generic type of the target Map.
     * @param copyOptions The options to configure the copying process.
     */
    public BeanToMapCopier(final Object source, final Map target, final Type targetType,
            final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.typeArguments = TypeKit.getTypeArguments(targetType);
    }

    /**
     * Performs the property copying operation from the source Bean to the target Map.
     *
     * @return The target Map with copied properties.
     */
    @Override
    public Map copy() {
        final CopyOptions copyOptions = this.copyOptions;
        Class<?> actualEditable = source.getClass();
        if (null != copyOptions.editable) {
            // Check if the restricted class is a superclass or interface of the source.
            Assert.isTrue(
                    copyOptions.editable.isInstance(source),
                    "Source class [{}] not assignable to Editable class [{}]",
                    actualEditable.getName(),
                    copyOptions.editable.getName());
            actualEditable = copyOptions.editable;
        }

        final Map<String, PropDesc> sourcePropDescMap = getBeanDesc(actualEditable).getPropMap(copyOptions.ignoreCase);
        sourcePropDescMap.forEach((sFieldName, sDesc) -> {
            if (null == sFieldName || !sDesc.isReadable(copyOptions.transientSupport)) {
                // Field is null or not readable, skip.
                return;
            }

            // Check if the source object property is filtered.
            Object sValue = sDesc.getValue(this.source, copyOptions.ignoreError);
            if (!copyOptions.testPropertyFilter(sDesc.getField(), sValue)) {
                return;
            }

            // Edit key-value pair.
            final MutableEntry<Object, Object> entry = copyOptions.editField(sFieldName, sValue);
            if (null == entry) {
                return;
            }
            sFieldName = StringKit.toStringOrNull(entry.getKey());
            // If the key is null after conversion, skip.
            if (null == sFieldName) {
                return;
            }
            sValue = entry.getValue();

            // Get the actual type of the target value and convert the source value.
            if (null != typeArguments && typeArguments.length > 1) {
                sValue = copyOptions.convertField(typeArguments[1], sValue);
            }

            // Assign to target.
            if (null != sValue || !copyOptions.ignoreNullValue) {
                target.put(sFieldName, sValue);
            }
        });
        return this.target;
    }

}
