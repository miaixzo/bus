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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.copier.Copier;

/**
 * A utility class for copying properties between different types of objects, including:
 *
 * <pre>
 *     1. Bean to Bean
 *     2. Bean to Map
 *     3. Map to Bean
 *     4. Map to Map
 * </pre>
 *
 * This class acts as a facade, delegating the actual copying logic to specific copier implementations based on the
 * source and target types.
 *
 * @param <T> The type of the target object.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanCopier<T> implements Copier<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852291373181L;

    /**
     * The internal copier instance that performs the actual property copying.
     */
    private final Copier<T> copier;

    /**
     * Constructs a new {@code BeanCopier} instance. It determines the appropriate copier implementation based on the
     * types of the source and target objects.
     *
     * @param source      The source object, which can be a Bean, a Map, or a {@link ValueProvider}. Must not be
     *                    {@code null}.
     * @param target      The target object, which can be a Bean or a Map. Must not be {@code null}.
     * @param targetType  The generic type of the target object, used for beans with generic parameters.
     * @param copyOptions The options to configure the copying process.
     * @throws NullPointerException if {@code source} or {@code target} is {@code null}.
     */
    public BeanCopier(final Object source, final T target, final Type targetType, final CopyOptions copyOptions) {
        Assert.notNull(source, "Source beans must be not null!");
        Assert.notNull(target, "Target beans must be not null!");

        final Copier<T> copier;
        if (source instanceof Map) {
            if (target instanceof Map) {
                copier = (Copier<T>) new MapToMapCopier((Map<?, ?>) source, (Map<?, ?>) target, targetType,
                        copyOptions);
            } else {
                copier = new MapToBeanCopier<>((Map<?, ?>) source, target, targetType, copyOptions);
            }
        } else if (source instanceof ValueProvider) {
            copier = new ValueToBeanCopier<>((ValueProvider<String>) source, target, targetType, copyOptions);
        } else {
            if (target instanceof Map) {
                copier = (Copier<T>) new BeanToMapCopier(source, (Map<?, ?>) target, targetType, copyOptions);
            } else {
                copier = new BeanToBeanCopier<>(source, target, targetType, copyOptions);
            }
        }
        this.copier = copier;
    }

    /**
     * Creates a {@code BeanCopier} instance with the target class's type as the generic type.
     *
     * @param <T>         The type of the target Bean.
     * @param source      The source object, which can be a Bean or a Map.
     * @param target      The target Bean object.
     * @param copyOptions The options to configure the copying process.
     * @return A new {@code BeanCopier} instance.
     */
    public static <T> BeanCopier<T> of(final Object source, final T target, final CopyOptions copyOptions) {
        return of(source, target, target.getClass(), copyOptions);
    }

    /**
     * Creates a {@code BeanCopier} instance with a specified target generic type.
     *
     * @param <T>         The type of the target Bean.
     * @param source      The source object, which can be a Bean or a Map.
     * @param target      The target Bean object.
     * @param destType    The generic type of the target, used for beans with generic parameters.
     * @param copyOptions The options to configure the copying process.
     * @return A new {@code BeanCopier} instance.
     */
    public static <T> BeanCopier<T> of(
            final Object source,
            final T target,
            final Type destType,
            final CopyOptions copyOptions) {
        return new BeanCopier<>(source, target, destType, copyOptions);
    }

    /**
     * Performs the property copying operation.
     *
     * @return The target object with copied properties.
     */
    @Override
    public T copy() {
        return copier.copy();
    }

}
