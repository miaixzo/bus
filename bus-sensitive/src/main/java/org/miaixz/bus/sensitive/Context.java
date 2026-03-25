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
package org.miaixz.bus.sensitive;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the execution context for a desensitization operation. It holds information about the current object,
 * field, and annotations being processed.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class Context {

    /**
     * The current object being processed.
     */
    private Object currentObject;

    /**
     * The current field being processed.
     */
    private Field currentField;

    /**
     * A list of all fields in the current object's class.
     */
    private List<Field> allFieldList = new ArrayList<>();

    /**
     * The current {@link Shield} annotation being applied.
     */
    private Shield shield;

    /**
     * The class of the current object.
     */
    private Class<?> beanClass;

    /**
     * The entry object, used when processing elements of a collection or array.
     */
    private Object entry;

    /**
     * An extension property for custom data.
     */
    private String extension;

    /**
     * Creates a new {@code Context} instance.
     *
     * @return this new instance.
     */
    public static Context newInstance() {
        return new Context();
    }

    /**
     * Gets the name of the current field being processed.
     *
     * @return The field name.
     */
    public String getCurrentFieldName() {
        return this.currentField.getName();
    }

    /**
     * Gets the value of the current field being processed.
     *
     * @return The field's value.
     */
    public Object getCurrentFieldValue() {
        try {
            return this.currentField.get(this.currentObject);
        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

}
