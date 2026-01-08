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

import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A copier that copies key-value pairs from a source Map to a target Map.
 *
 * @author Kimi Liu
 * @since Java 17+
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
