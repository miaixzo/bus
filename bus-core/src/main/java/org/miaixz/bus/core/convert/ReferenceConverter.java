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
