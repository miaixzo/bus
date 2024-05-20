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
package org.miaixz.bus.core.lang.reflect.kotlin;

import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.MethodKit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * kotlin.reflect.KCallable方法包装调用类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KotlinCallable {

    private static final Method METHOD_GET_PARAMETERS;
    private static final Method METHOD_CALL;

    static {
        final Class<?> kFunctionClass = ClassKit.loadClass("kotlin.reflect.KCallable");
        METHOD_GET_PARAMETERS = MethodKit.getMethod(kFunctionClass, "getParameters");
        METHOD_CALL = MethodKit.getMethodByName(kFunctionClass, "call");
    }

    /**
     * 获取参数列表
     *
     * @param kCallable kotlin的类、方法或构造
     * @return 参数列表
     */
    public static List<KotlinParameter> getParameters(final Object kCallable) {
        final List<?> parameters = MethodKit.invoke(kCallable, METHOD_GET_PARAMETERS);
        final List<KotlinParameter> result = new ArrayList<>(parameters.size());
        for (final Object parameter : parameters) {
            result.add(new KotlinParameter(parameter));
        }
        return result;
    }

    /**
     * 实例化对象，本质上调用KCallable.call方法
     *
     * @param kCallable kotlin的类、方法或构造
     * @param args      参数列表
     * @return 参数列表
     */
    public static Object call(final Object kCallable, final Object... args) {
        return MethodKit.invoke(kCallable, METHOD_CALL, new Object[]{args});
    }

}
