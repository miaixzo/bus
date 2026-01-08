/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.Registry;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Reflect;

import java.lang.reflect.Method;

/**
 * Validator for the {@link Reflect} annotation, which performs validation based on the result of a reflective method
 * invocation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectMatcher implements Matcher<Object, Reflect> {

    /**
     * Performs validation by invoking a method reflectively and then applying other validators to its result.
     *
     * @param object     The object on which the method will be invoked.
     * @param annotation The {@link Reflect} annotation instance, specifying the target class, method, and subsequent
     *                   validators.
     * @param context    The validation context.
     * @return {@code true} if the object is empty (null) or if the result of the reflective call passes all specified
     *         validators, {@code false} otherwise.
     * @throws InternalException if the method cannot be found or invoked reflectively.
     * @throws NoSuchException   if a specified validator cannot be found in the registry.
     */
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
            result = MethodKit.invoke(bean, method, object);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Method not found for reflective validation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new InternalException("Failed to invoke method reflectively for validation: " + e.getMessage(), e);
        }

        for (String name : annotation.validator()) {
            if (!Registry.getInstance().contains(name)) {
                throw new NoSuchException("Attempting to use a non-existent validator: " + name);
            }
            Matcher matcher = (Matcher) Registry.getInstance().require(name);
            if (!matcher.on(result, null, context)) {
                return false;
            }
        }
        return true;
    }

}
