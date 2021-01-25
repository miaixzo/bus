/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.convert;

import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 各种集合类转换器
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public class CollectionConverter implements Converter<Collection<?>> {

    /**
     * 集合类型
     */
    private final Type collectionType;
    /**
     * 集合元素类型
     */
    private final Type elementType;

    /**
     * 构造,默认集合类型使用{@link Collection}
     */
    public CollectionConverter() {
        this(Collection.class);
    }

    /**
     * 构造
     *
     * @param collectionType 集合类型
     */
    public CollectionConverter(Type collectionType) {
        this(collectionType, TypeKit.getTypeArgument(collectionType));
    }

    /**
     * 构造
     *
     * @param collectionType 集合类型
     */
    public CollectionConverter(Class<?> collectionType) {
        this(collectionType, TypeKit.getTypeArgument(collectionType));
    }

    /**
     * 构造
     *
     * @param collectionType 集合类型
     * @param elementType    集合元素类型
     */
    public CollectionConverter(Type collectionType, Type elementType) {
        this.collectionType = collectionType;
        this.elementType = elementType;
    }

    @Override
    public Collection<?> convert(Object value, Collection<?> defaultValue) throws IllegalArgumentException {
        final Collection<?> result = convertInternal(value);
        return ObjectKit.defaultIfNull(result, defaultValue);
    }

    /**
     * 内部转换
     *
     * @param value 值
     * @return 转换后的集合对象
     */
    protected Collection<?> convertInternal(Object value) {
        return CollKit.addAll(CollKit.create(TypeKit.getClass(this.collectionType)), value, this.elementType);
    }

}
