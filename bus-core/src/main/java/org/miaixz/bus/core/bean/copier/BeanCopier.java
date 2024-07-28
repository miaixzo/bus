/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.copier.Copier;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Bean拷贝，提供：
 *
 * <pre>
 *     1. Bean 转 Bean
 *     2. Bean 转 Map
 *     3. Map  转 Bean
 *     4. Map  转 Map
 * </pre>
 *
 * @param <T> 目标对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanCopier<T> implements Copier<T>, Serializable {

    private static final long serialVersionUID = -1L;

    private final Copier<T> copier;

    /**
     * 构造
     *
     * @param source      来源对象，可以是Bean或者Map，不能为{@code null}
     * @param target      目标Bean对象，不能为{@code null}
     * @param targetType  目标的泛型类型，用于标注有泛型参数的Bean对象
     * @param copyOptions 拷贝属性选项
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
     * 创建BeanCopier
     *
     * @param <T>         目标Bean类型
     * @param source      来源对象，可以是Bean或者Map
     * @param target      目标Bean对象
     * @param copyOptions 拷贝属性选项
     * @return BeanCopier
     */
    public static <T> BeanCopier<T> of(final Object source, final T target, final CopyOptions copyOptions) {
        return of(source, target, target.getClass(), copyOptions);
    }

    /**
     * 创建BeanCopier
     *
     * @param <T>         目标Bean类型
     * @param source      来源对象，可以是Bean或者Map
     * @param target      目标Bean对象
     * @param destType    目标的泛型类型，用于标注有泛型参数的Bean对象
     * @param copyOptions 拷贝属性选项
     * @return BeanCopier
     */
    public static <T> BeanCopier<T> of(final Object source, final T target, final Type destType,
            final CopyOptions copyOptions) {
        return new BeanCopier<>(source, target, destType, copyOptions);
    }

    @Override
    public T copy() {
        return copier.copy();
    }

}
