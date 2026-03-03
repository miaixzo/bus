/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
