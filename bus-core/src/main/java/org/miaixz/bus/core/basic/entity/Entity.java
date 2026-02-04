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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.FieldKit;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a base entity with a primary key.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852290719680L;

    /**
     * The primary key of the data.
     */
    @Id
    protected String id;

    /**
     * Checks if the primary key value is not null or empty based on the specified field name.
     *
     * @param <T>    The type of the entity.
     * @param entity The entity object.
     * @param field  The name of the primary key field.
     * @return {@code true} if the primary key has a value, {@code false} otherwise.
     */
    public <T> boolean isPKNotNull(T entity, String field) {
        // Check if the entity's class has the specified field.
        if (!FieldKit.hasField(entity.getClass(), field)) {
            return false;
        }
        // Get the value of the field.
        Object value = FieldKit.getFieldValue(entity, field);
        // Return true if the value is not null and not an empty string.
        return null != value && !Normal.EMPTY.equals(value);
    }

    /**
     * Gets the value of a specific field from the entity.
     *
     * @param <T>    The type of the entity.
     * @param entity The entity object.
     * @param field  The name of the field.
     * @return The value of the field as a string, or {@code null} if the field does not exist or its value is null.
     */
    public <T> Object getValue(T entity, String field) {
        // Check if the entity's class has the specified field.
        if (FieldKit.hasField(entity.getClass(), field)) {
            // Get the value of the field.
            Object value = FieldKit.getFieldValue(entity, field);
            // Return the value as a string, or null if the value is null.
            return null != value ? value.toString() : null;
        }
        return null;
    }

    /**
     * Sets the values of multiple fields on the entity.
     *
     * @param <T>    The type of the entity.
     * @param entity The entity object.
     * @param fields An array of field names.
     * @param value  An array of corresponding values.
     */
    public <T> void setValue(T entity, String[] fields, Object[] value) {
        // Iterate over the fields and set their corresponding values.
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            // Check if the entity's class has the specified field before setting the value.
            if (FieldKit.hasField(entity.getClass(), field)) {
                FieldKit.setFieldValue(entity, field, value[i]);
            }
        }
    }

}
