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
package org.miaixz.bus.core.lang.reflect.lookup;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * jdk11中直接调用MethodHandles.lookup()获取到的MethodHandles.Lookup只能对接口类型才会权限获取方法的方法句柄MethodHandle。 如果是普通类型Class,需要使用jdk9开始提供的
 * MethodHandles#privateLookupIn(java.lang.Class, java.lang.invoke.MethodHandles.Lookup)方法.
 * 参考：https://blog.csdn.net/u013202238/article/details/108687086
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodLookupFactory implements LookupFactory {

    private final Method privateLookupInMethod;

    /**
     * 构造
     */
    public MethodLookupFactory() {
        this.privateLookupInMethod = createJdk9PrivateLookupInMethod();
    }

    private static Method createJdk9PrivateLookupInMethod() {
        try {
            return MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (final NoSuchMethodException e) {
            // 可能是jdk9 以下版本
            throw new IllegalStateException(
                    "There is no 'privateLookupIn(Class, Lookup)' method in java.lang.invoke.MethodHandles.", e);
        }
    }

    @Override
    public MethodHandles.Lookup lookup(final Class<?> callerClass) {
        try {
            return (MethodHandles.Lookup) privateLookupInMethod.invoke(MethodHandles.class, callerClass,
                    MethodHandles.lookup());
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        } catch (final InvocationTargetException e) {
            throw new InternalException(e.getTargetException());
        }
    }

}
