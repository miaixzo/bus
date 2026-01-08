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
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A copier that copies properties from one Bean to another Bean.
 *
 * @param <S> The type of the source Bean.
 * @param <T> The type of the target Bean.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanToBeanCopier<S, T> extends AbstractCopier<S, T> {

    /**
     * The generic type of the target (used for injecting generic parameters into the Bean).
     */
    private final Type targetType;

    /**
     * Constructs a new {@code BeanToBeanCopier} instance.
     *
     * @param source      The source Bean object. Must not be {@code null}.
     * @param target      The target Bean object. Must not be {@code null}.
     * @param targetType  The generic type of the target, used for beans with generic parameters.
     * @param copyOptions The options to configure the copying process.
     */
    public BeanToBeanCopier(final S source, final T target, final Type targetType, final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.targetType = targetType;
    }

    /**
     * Performs the property copying operation from the source Bean to the target Bean.
     *
     * @return The target object with copied properties.
     * @throws BeanException If the source or target bean has no properties, and {@code ignoreError} is {@code false}.
     */
    @Override
    public T copy() {
        final CopyOptions copyOptions = this.copyOptions;
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
        if (MapKit.isEmpty(targetPropDescMap)) {
            if (copyOptions.ignoreError) {
                return target;
            }
            throw new BeanException("No properties for target: {}", actualEditable);
        }

        final Map<String, PropDesc> sourcePropDescMap = getBeanDesc(source.getClass())
                .getPropMap(copyOptions.ignoreCase);
        if (MapKit.isEmpty(sourcePropDescMap)) {
            if (copyOptions.ignoreError) {
                return target;
            }
            throw new BeanException("No properties for source: {}", source.getClass());
        }
        sourcePropDescMap.forEach((sFieldName, sDesc) -> {
            if (null == sFieldName || !sDesc.isReadable(copyOptions.transientSupport)) {
                // Field is null or not readable, skip.
                return;
            }

            // Check if the source object property is filtered.
            Object sValue = sDesc.getValue(this.source, copyOptions.ignoreError);
            if (!this.copyOptions.testPropertyFilter(sDesc.getField(), sValue)) {
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

            // Check target field writability.
            // Target field check is performed after key-value pair editing, because the key might be modified.
            final PropDesc tDesc = copyOptions.findPropDesc(targetPropDescMap, sFieldName);
            if (null == tDesc || !tDesc.isWritable(copyOptions.transientSupport)) {
                // Field is not writable, skip.
                return;
            }

            // Get the actual type of the target field and convert the source value.
            final Type fieldType = TypeKit.getActualType(this.targetType, tDesc.getFieldType());
            sValue = copyOptions.convertField(fieldType, sValue);

            // Assign to target.
            tDesc.setValue(
                    this.target,
                    sValue,
                    copyOptions.ignoreNullValue,
                    copyOptions.ignoreError,
                    copyOptions.override);
        });
        return this.target;
    }

}
