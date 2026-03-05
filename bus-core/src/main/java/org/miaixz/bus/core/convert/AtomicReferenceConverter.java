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
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converts an object to an {@link AtomicReference}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicReferenceConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852265266938L;

    /**
     * The converter used to convert the object wrapped by the {@link AtomicReference}.
     */
    private final Converter converter;

    /**
     * Constructs a new {@code AtomicReferenceConverter}.
     *
     * @param converter The converter for the referenced object.
     */
    public AtomicReferenceConverter(final Converter converter) {
        this.converter = converter;
    }

    /**
     * Internally converts the given value to an {@link AtomicReference}.
     *
     * @param targetClass The target class, which should be {@link AtomicReference}.
     * @param value       The value to be converted.
     * @return The converted {@link AtomicReference} object.
     */
    @Override
    protected AtomicReference<?> convertInternal(final Class<?> targetClass, final Object value) {
        // Attempt to convert the value to the generic type of the AtomicReference.
        Object targetValue = null;
        final Type paramType = TypeKit.getTypeArgument(AtomicReference.class);
        if (null != paramType && !TypeKit.isUnknown(paramType)) {
            targetValue = converter.convert(paramType, value);
        }

        // If conversion is not possible or not needed, use the original value.
        if (null == targetValue) {
            targetValue = value;
        }

        return new AtomicReference<>(targetValue);
    }

}
