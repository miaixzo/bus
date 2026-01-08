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
