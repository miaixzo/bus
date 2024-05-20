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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.MethodKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.Registry;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Reflect;

import java.lang.reflect.Method;

/**
 * 反射信息校验
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectMatcher implements Matcher<Object, Reflect> {

    @Override
    public boolean on(Object object, Reflect annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        Class<?> clazz = annotation.target();
        String methodName = annotation.method();
        Object result;
        try {
            Method method = clazz.getDeclaredMethod(methodName, object.getClass());
            Object bean = ClassKit.getClass(clazz);
            result = MethodKit.invoke(bean, method);
        } catch (NoSuchMethodException e) {
            throw new InternalException(e.getMessage(), e);
        }

        for (String name : annotation.validator()) {
            if (!Registry.getInstance().contains(name)) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + name);
            }
            Matcher matcher = (Matcher) Registry.getInstance().require(name);
            if (!matcher.on(result, null, context)) {
                return false;
            }
        }
        return true;
    }

}
