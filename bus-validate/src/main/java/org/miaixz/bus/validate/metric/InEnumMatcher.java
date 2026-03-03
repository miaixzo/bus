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
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.InEnum;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Validator for the {@link InEnum} annotation. Checks if an object's value matches one of the values of a specified
 * enum.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InEnumMatcher implements Matcher<Object, InEnum> {

    /**
     * Checks if the given object's value is present in the specified enum.
     *
     * @param object     The object to validate.
     * @param annotation The {@link InEnum} annotation instance, which provides the enum class and the method to get the
     *                   value from.
     * @param context    The validation context (ignored).
     * @return {@code true} if the object's value is found within the enum's values; {@code false} if the object is null
     *         or not found.
     * @throws InternalException if the method specified in the annotation does not exist in the enum class.
     */
    @Override
    public boolean on(Object object, InEnum annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        Class<? extends Enum> enumClass = annotation.enumClass();
        try {
            Method method = enumClass.getMethod(annotation.method());
            Enum[] enums = enumClass.getEnumConstants();
            for (Enum e : enums) {
                Object value = MethodKit.invoke(e, method);
                if (Objects.equals(value, object)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchMethodException e) {
            throw new InternalException("The method specified in @InEnum does not exist: " + e.getMessage());
        }
    }

}
