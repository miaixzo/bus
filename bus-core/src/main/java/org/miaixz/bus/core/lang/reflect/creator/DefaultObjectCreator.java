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
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.LookupKit;

import java.lang.invoke.MethodHandle;

/**
 * 默认对象实例化器 通过传入对象类型和构造函数的参数，调用对应的构造方法创建对象。
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultObjectCreator<T> implements ObjectCreator<T> {

    final MethodHandle constructor;
    final Object[] params;

    /**
     * 构造
     *
     * @param clazz  实例化的类
     * @param params 构造参数，无参数空
     */
    public DefaultObjectCreator(final Class<T> clazz, final Object... params) {
        final Class<?>[] paramTypes = ClassKit.getClasses(params);
        this.constructor = LookupKit.findConstructor(clazz, paramTypes);
        Assert.notNull(this.constructor, "Constructor not found!");
        this.params = params;
    }

    /**
     * 创建默认的对象实例化器
     *
     * @param fullClassName 类名全程
     * @param <T>           对象类型
     * @return DefaultObjectCreator
     */
    public static <T> DefaultObjectCreator<T> of(final String fullClassName) {
        return of(ClassKit.loadClass(fullClassName));
    }

    /**
     * 创建默认的对象实例化器
     *
     * @param clazz  实例化的类
     * @param params 构造参数，无参数空
     * @param <T>    对象类型
     * @return DefaultObjectCreator
     */
    public static <T> DefaultObjectCreator<T> of(final Class<T> clazz, final Object... params) {
        return new DefaultObjectCreator<>(clazz, params);
    }

    @Override
    public T create() {
        return MethodInvoker.invokeHandle(constructor, params);
    }

}
