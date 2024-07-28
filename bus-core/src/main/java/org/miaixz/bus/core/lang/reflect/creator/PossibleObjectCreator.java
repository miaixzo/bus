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
package org.miaixz.bus.core.lang.reflect.creator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 尝试方式对象实例化器 通过判断类型或调用可能的构造，构建对象，支持：
 * <ul>
 * <li>原始类型</li>
 * <li>接口或抽象类型</li>
 * <li>枚举</li>
 * <li>数组</li>
 * <li>使用默认参数的构造方法</li>
 * </ul>
 * <p>
 * 对于接口或抽象类型，构造其默认实现：
 * 
 * <pre>
 *     Map       - HashMap
 *     Collction - ArrayList
 *     List      - ArrayList
 *     Set       - HashSet
 * </pre>
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PossibleObjectCreator<T> implements ObjectCreator<T> {

    final Class<T> clazz;

    /**
     * 构造
     *
     * @param clazz 实例化的类
     */
    public PossibleObjectCreator(final Class<T> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * 创建默认的对象实例化器
     *
     * @param clazz 实例化的类
     * @param <T>   对象类型
     * @return DefaultObjectCreator
     */
    public static <T> PossibleObjectCreator<T> of(final Class<T> clazz) {
        return new PossibleObjectCreator<>(clazz);
    }

    /**
     * 某些特殊接口的实例化按照默认实现进行
     *
     * @param type 类型
     * @return 默认类型
     */
    private static Class<?> resolveType(final Class<?> type) {
        if (type.isAssignableFrom(AbstractMap.class)) {
            return HashMap.class;
        } else if (type.isAssignableFrom(List.class)) {
            return ArrayList.class;
        } else if (type == SortedSet.class) {
            return TreeSet.class;
        } else if (type.isAssignableFrom(Set.class)) {
            return HashSet.class;
        }

        return type;
    }

    @Override
    public T create() {
        Class<T> type = this.clazz;

        // 原始类型
        if (type.isPrimitive()) {
            return (T) ClassKit.getPrimitiveDefaultValue(type);
        }

        // 处理接口和抽象类的默认值
        type = (Class<T>) resolveType(type);

        // 尝试默认构造实例化
        try {
            return DefaultObjectCreator.of(type).create();
        } catch (final Exception e) {
            // ignore
            // 默认构造不存在的情况下查找其它构造
        }

        // 枚举
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }

        // 数组
        if (type.isArray()) {
            return (T) Array.newInstance(type.getComponentType(), 0);
        }

        // 查找合适构造
        final Constructor<T>[] constructors = ReflectKit.getConstructors(type);
        Class<?>[] parameterTypes;
        for (final Constructor<T> constructor : constructors) {
            parameterTypes = constructor.getParameterTypes();
            if (0 == parameterTypes.length) {
                continue;
            }
            ReflectKit.setAccessible(constructor);
            try {
                return constructor.newInstance(ClassKit.getDefaultValues(parameterTypes));
            } catch (final Exception ignore) {
                // 构造出错时继续尝试下一种构造方式
            }
        }
        return null;
    }

}
