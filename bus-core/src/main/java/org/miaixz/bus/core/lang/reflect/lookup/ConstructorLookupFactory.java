/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.lang.reflect.Constructor;

/**
 * jdk8中如果直接调用{@link MethodHandles#lookup()}获取到的{@link MethodHandles.Lookup}
 * 在调用findSpecial和unreflectSpecial时会出现权限不够问题，抛出"no private access for invokespecial"异常
 * 所以通过反射创建MethodHandles.Lookup解决该问题。
 *
 * <p>
 * 参考：https://blog.csdn.net/u013202238/article/details/108687086
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConstructorLookupFactory implements LookupFactory {

    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    private final Constructor<MethodHandles.Lookup> lookupConstructor;

    /**
     * 构造
     */
    public ConstructorLookupFactory() {
        this.lookupConstructor = createLookupConstructor();
    }

    private static Constructor<MethodHandles.Lookup> createLookupConstructor() {
        final Constructor<MethodHandles.Lookup> constructor;
        try {
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (final NoSuchMethodException e) {
            // 可能是jdk8 以下版本
            throw new IllegalStateException(
                    "There is no 'Lookup(Class, int)' constructor in java.lang.invoke.MethodHandles.", e);
        }
        constructor.setAccessible(true);
        return constructor;
    }

    @Override
    public MethodHandles.Lookup lookup(final Class<?> callerClass) {
        try {
            return lookupConstructor.newInstance(callerClass, ALLOWED_MODES);
        } catch (final Exception e) {
            throw new IllegalStateException("no 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.", e);
        }
    }

}
