/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.screw.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a pair of a {@link Field} and its corresponding {@link Method} (typically a setter). This class is used
 * internally for mapping operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldMethod {

    /**
     * The {@link Field} object.
     */
    private Field field;

    /**
     * The corresponding {@link Method} object, usually a setter.
     */
    private Method method;

    /**
     * Gets the {@link Field} object.
     *
     * @return The {@link Field} object.
     */
    public Field getField() {
        return field;
    }

    /**
     * Sets the {@link Field} object.
     *
     * @param field The {@link Field} to set.
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Gets the {@link Method} object.
     *
     * @return The {@link Method} object.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the {@link Method} object.
     *
     * @param method The {@link Method} to set.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

}
