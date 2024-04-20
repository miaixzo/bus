/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.lang.Types;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 各种集合类转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionConverter implements Converter {

    /**
     * 单例对象
     */
    public static CollectionConverter INSTANCE = new CollectionConverter();

    @Override
    public Collection<?> convert(Type targetType, final Object value) {
        if (targetType instanceof Types) {
            targetType = ((Types<?>) targetType).getType();
        }

        return convert(targetType, TypeKit.getTypeArgument(targetType), value);
    }

    /**
     * 转换
     *
     * @param collectionType 集合类型
     * @param elementType    集合中元素类型
     * @param value          被转换的值
     * @return 转换后的集合对象
     */
    public Collection<?> convert(final Type collectionType, final Type elementType, final Object value) {
        final Collection<Object> collection = CollKit.create(TypeKit.getClass(collectionType));
        return CollKit.addAll(collection, value, elementType);
    }

}
