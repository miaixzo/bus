/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A copier that copies properties from a {@link ValueProvider} to a Bean.
 *
 * @param <T> The type of the target Bean.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValueToBeanCopier<T> extends AbstractCopier<ValueProvider<String>, T> {

    /**
     * The generic type of the target (used for injecting generic parameters into the Bean).
     */
    private final Type targetType;

    /**
     * Constructs a new {@code ValueToBeanCopier} instance.
     *
     * @param source      The source {@link ValueProvider}. Must not be {@code null}.
     * @param target      The target Bean object. Must not be {@code null}.
     * @param targetType  The generic type of the target Bean.
     * @param copyOptions The options to configure the copying process.
     */
    public ValueToBeanCopier(final ValueProvider<String> source, final T target, final Type targetType,
            final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.targetType = targetType;
    }

    /**
     * Performs the property copying operation from the {@link ValueProvider} to the target Bean.
     *
     * @return The target object with copied properties.
     */
    @Override
    public T copy() {
        Class<?> actualEditable = target.getClass();
        if (null != copyOptions.editable) {
            // Check if the restricted class is a superclass or interface of the target.
            Assert.isTrue(
                    copyOptions.editable.isInstance(target),
                    "Target class [{}] not assignable to Editable class [{}]",
                    actualEditable.getName(),
                    copyOptions.editable.getName());
            actualEditable = copyOptions.editable;
        }
        final Map<String, PropDesc> targetPropDescMap = getBeanDesc(actualEditable).getPropMap(copyOptions.ignoreCase);

        targetPropDescMap.forEach((tFieldName, propDesc) -> {
            if (null == tFieldName) {
                return;
            }

            // Check target field writability.
            if (null == propDesc || !propDesc.isWritable(this.copyOptions.transientSupport)) {
                // Field is not writable, skip.
                return;
            }

            // Edit key-value pair.
            final MutableEntry<Object, Object> entry = copyOptions.editField(tFieldName, null);
            if (null == entry) {
                return;
            }
            tFieldName = StringKit.toStringOrNull(entry.getKey());
            // If the key is null after conversion, skip.
            if (null == tFieldName) {
                return;
            }
            // If the source does not contain the field, skip.
            if (!source.containsKey(tFieldName)) {
                return;
            }

            // Get the actual type of the target field and retrieve the source value.
            final Type fieldType = TypeKit.getActualType(this.targetType, propDesc.getFieldType());
            final Object sValue = source.value(tFieldName, fieldType);

            // Check if the target property is filtered.
            if (!copyOptions.testPropertyFilter(propDesc.getField(), sValue)) {
                return;
            }

            // Assign to target.
            propDesc.setValue(
                    this.target,
                    sValue,
                    copyOptions.ignoreNullValue,
                    copyOptions.ignoreError,
                    copyOptions.override);
        });
        return this.target;
    }

}
