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
package org.miaixz.bus.core.center.map.multi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.function.SupplierX;

/**
 * {@link MultiValueMap}的通用实现，可视为值为{@link Collection}集合的{@link Map}集合。
 * 构建时指定一个工厂方法用于生成原始的{@link Map}集合，然后再指定一个工厂方法用于生成自定义类型的值集合。
 * 当调用{@link MultiValueMap}中格式为“putXXX”的方法时，将会为key创建值集合，并将key相同的值追加到集合中
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionValueMap<K, V> extends AbstractCollValueMap<K, V> {

    private static final long serialVersionUID = -1L;

    private final SupplierX<Collection<V>> collFactory;

    /**
     * 创建一个多值映射集合，基于{@code mapFactory}与{@code collFactory}实现
     *
     * @param mapFactory  生成集合的工厂方法
     * @param collFactory 生成值集合的工厂方法
     */
    public CollectionValueMap(final Supplier<Map<K, Collection<V>>> mapFactory,
            final SupplierX<Collection<V>> collFactory) {
        super(mapFactory);
        this.collFactory = collFactory;
    }

    /**
     * 创建一个多值映射集合，默认基于{@link HashMap}与{@code collFactory}生成的集合实现
     *
     * @param collFactory 生成值集合的工厂方法
     */
    public CollectionValueMap(final SupplierX<Collection<V>> collFactory) {
        this.collFactory = collFactory;
    }

    /**
     * 创建一个多值映射集合，默认基于{@link HashMap}与{@link ArrayList}实现
     */
    public CollectionValueMap() {
        this.collFactory = ArrayList::new;
    }

    /**
     * 创建一个多值映射集合，默认基于指定Map与指定List类型实现
     *
     * @param map 提供数据的原始集合
     */
    public CollectionValueMap(final Map<K, Collection<V>> map) {
        super(map);
        this.collFactory = ArrayList::new;
    }

    @Override
    protected Collection<V> createCollection() {
        return collFactory.get();
    }

}
