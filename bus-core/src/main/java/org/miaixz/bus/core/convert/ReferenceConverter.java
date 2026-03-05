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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converter for {@link Reference} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReferenceConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852271357551L;

    private final Converter rootConverter;

    /**
     * Constructs a new ReferenceConverter.
     *
     * @param rootConverter the root converter, used to convert Reference generic types
     */
    public ReferenceConverter(final Converter rootConverter) {
        this.rootConverter = Assert.notNull(rootConverter);
    }

    /**
     * Converts the given value to a Reference object.
     * <p>
     * Supports WeakReference and SoftReference. The referenced value is converted using the root converter to match the
     * generic type of the Reference.
     * </p>
     *
     * @param targetClass the target Reference class
     * @param value       the value to be referenced
     * @return a WeakReference or SoftReference containing the converted value
     * @throws UnsupportedOperationException if the target class is not a supported Reference type
     */
    @Override
    protected Reference<?> convertInternal(final Class<?> targetClass, final Object value) {

        // Try to convert the value to the type of the Reference generic
        Object targetValue = null;
        final Type paramType = TypeKit.getTypeArgument(targetClass);
        if (!TypeKit.isUnknown(paramType)) {
            targetValue = rootConverter.convert(paramType, value);
        }
        if (null == targetValue) {
            targetValue = value;
        }

        if (targetClass == WeakReference.class) {
            return new WeakReference(targetValue);
        } else if (targetClass == SoftReference.class) {
            return new SoftReference(targetValue);
        }

        throw new UnsupportedOperationException(
                StringKit.format("Unsupport Reference type: {}", targetClass.getName()));
    }

}
