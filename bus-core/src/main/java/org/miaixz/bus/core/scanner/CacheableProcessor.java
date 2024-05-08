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
package org.miaixz.bus.core.scanner;

import org.miaixz.bus.core.map.multi.RowKeyTable;
import org.miaixz.bus.core.map.multi.Table;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.util.Collection;
import java.util.Comparator;

/**
 * 带缓存功能的{@link SynthesizedProcessor}实现，
 * 构建时需要传入比较器，获取属性值时将根据比较器对合成注解进行排序，
 * 然后选择具有所需属性的，排序最靠前的注解用于获取属性值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheableProcessor implements SynthesizedProcessor {

    private final Table<String, Class<?>, Object> valueCaches = new RowKeyTable<>();
    private final Comparator<Synthesized> annotationComparator;

    /**
     * 创建一个带缓存的注解值选择器
     *
     * @param annotationComparator 注解比较器，排序更靠前的注解将被优先用于获取值
     */
    public CacheableProcessor(Comparator<Synthesized> annotationComparator) {
        this.annotationComparator = annotationComparator;
    }

    @Override
    public <T> T getAttributeValue(String attributeName, Class<T> attributeType, Collection<? extends Synthesized> synthesizedAnnotations) {
        Object value = valueCaches.get(attributeName, attributeType);
        // 此处理论上不可能出现缓存值为nul的情况
        if (ObjectKit.isNotNull(value)) {
            return (T) value;
        }
        value = synthesizedAnnotations.stream()
                .filter(ma -> ma.hasAttribute(attributeName, attributeType))
                .min(annotationComparator)
                .map(ma -> ma.getAttribute(attributeName))
                .orElse(null);
        valueCaches.put(attributeName, attributeType, value);
        return (T) value;
    }

}
