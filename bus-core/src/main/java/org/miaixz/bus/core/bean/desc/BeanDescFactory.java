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
package org.miaixz.bus.core.bean.desc;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.reflect.JdkProxy;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.RecordKit;

/**
 * Bean描述信息工厂类
 * 通过不同的类和策略，生成对应的{@link BeanDesc}，策略包括：
 * <ul>
 *     <li>当类为Record时，生成{@link RecordBeanDesc}</li>
 *     <li>当类为普通Bean时，生成{@link StrictBeanDesc}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanDescFactory {

    private static final WeakConcurrentMap<Class<?>, BeanDesc> Cache = new WeakConcurrentMap<>();

    /**
     * 获取{@link BeanDesc} Bean描述信息，使用Weak缓存
     *
     * @param clazz Bean类
     * @return {@link BeanDesc}
     */
    public static BeanDesc getBeanDesc(final Class<?> clazz) {
        return Cache.computeIfAbsent(clazz, (key) -> getBeanDescWithoutCache(clazz));
    }

    /**
     * 获取{@link BeanDesc} Bean描述信息，不使用缓存
     *
     * @param clazz Bean类
     * @return {@link BeanDesc}
     */
    public static BeanDesc getBeanDescWithoutCache(final Class<?> clazz) {
        if (RecordKit.isRecord(clazz)) {
            return new RecordBeanDesc(clazz);
        } else if (JdkProxy.isProxyClass(clazz) || ArrayKit.isEmpty(FieldKit.getFields(clazz))) {
            // 代理类和空字段的Bean不支持属性获取，直接使用方法方式
            return new SimpleBeanDesc(clazz);
        } else {
            return new StrictBeanDesc(clazz);
        }
    }

    /**
     * 清空全局的Bean属性缓存
     */
    public static void clearCache() {
        Cache.clear();
    }

}
