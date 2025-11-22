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
package org.miaixz.bus.mapper.support.populate;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.annotation.Created;
import org.miaixz.bus.core.lang.annotation.Creator;
import org.miaixz.bus.core.lang.annotation.Modified;
import org.miaixz.bus.core.lang.annotation.Modifier;
import org.miaixz.bus.logger.Logger;

/**
 * Data fill builder.
 *
 * <p>
 * Responsible for automatic data filling logic. This class handles:
 * </p>
 * <ul>
 * <li>Field metadata caching and management</li>
 * <li>Automatic filling of create/update timestamps</li>
 * <li>Automatic filling of creator/updater information</li>
 * <li>Type conversion for different date/time types</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PopulateBuilder {

    /**
     * Cache for entity field metadata.
     */
    private final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * Data fill configuration.
     */
    private final PopulateConfig config;

    /**
     * Constructor.
     *
     * @param config the data fill configuration
     */
    public PopulateBuilder(PopulateConfig config) {
        this.config = config;
    }

    /**
     * Fill data for INSERT operation.
     *
     * @param entity the entity object
     */
    public void fillInsertData(Object entity) {
        if (entity == null) {
            return;
        }

        // Handle collection
        if (entity instanceof Collection) {
            ((Collection<?>) entity).forEach(this::fillInsertData);
            return;
        }

        // Get entity metadata
        EntityMetadata metadata = getMetadata(entity.getClass());
        if (metadata == null) {
            return;
        }

        try {
            // Fill create time
            if (config.isCreated() && metadata.createTimeField != null) {
                fillTimeField(entity, metadata.createTimeField);
            }

            // Fill update time
            if (config.isModified() && metadata.updateTimeField != null) {
                fillTimeField(entity, metadata.updateTimeField);
            }

            // Fill created by
            if (config.isCreator() && metadata.createdByField != null) {
                fillUserField(entity, metadata.createdByField);
            }

            // Fill updated by
            if (config.isModifier() && metadata.updatedByField != null) {
                fillUserField(entity, metadata.updatedByField);
            }

        } catch (Exception e) {
            Logger.error(
                    false,
                    "Populate",
                    "Failed to fill insert data for entity: {}",
                    entity.getClass().getName(),
                    e);
        }
    }

    /**
     * Fill data for UPDATE operation.
     *
     * @param entity the entity object
     */
    public void fillUpdateData(Object entity) {
        if (entity == null) {
            return;
        }

        // Handle collection
        if (entity instanceof Collection) {
            ((Collection<?>) entity).forEach(this::fillUpdateData);
            return;
        }

        // Get entity metadata
        EntityMetadata metadata = getMetadata(entity.getClass());
        if (metadata == null) {
            return;
        }

        try {
            // Fill update time
            if (config.isModified() && metadata.updateTimeField != null) {
                fillTimeField(entity, metadata.updateTimeField);
            }

            // Fill updated by
            if (config.isModifier() && metadata.updatedByField != null) {
                fillUserField(entity, metadata.updatedByField);
            }

        } catch (Exception e) {
            Logger.error(
                    false,
                    "Populate",
                    "Failed to fill update data for entity: {}",
                    entity.getClass().getName(),
                    e);
        }
    }

    /**
     * Fill time field with current timestamp.
     *
     * @param entity the entity object
     * @param field  the field to fill
     * @throws IllegalAccessException if field access fails
     */
    private void fillTimeField(Object entity, Field field) throws IllegalAccessException {
        field.setAccessible(true);

        // Skip if already has value
        Object currentValue = field.get(entity);
        if (currentValue != null) {
            return;
        }

        // Fill based on field type
        Class<?> fieldType = field.getType();
        Object value = getCurrentTime(fieldType);

        if (value != null) {
            field.set(entity, value);
        }
    }

    /**
     * Fill user field with current user information.
     *
     * @param entity the entity object
     * @param field  the field to fill
     * @throws IllegalAccessException if field access fails
     */
    private void fillUserField(Object entity, Field field) throws IllegalAccessException {
        if (config.getProvider() == null) {
            return;
        }

        field.setAccessible(true);

        // Skip if already has value
        Object currentValue = field.get(entity);
        if (currentValue != null) {
            return;
        }

        // Get current user
        Object user = config.getProvider().getOperator();
        if (user != null) {
            // Convert if necessary
            Object value = convertUserValue(user, field.getType());
            if (value != null) {
                field.set(entity, value);
            }
        }
    }

    /**
     * Get current time value based on field type.
     *
     * @param fieldType the field type
     * @return the current time value
     */
    private Object getCurrentTime(Class<?> fieldType) {
        if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (fieldType == LocalDate.class) {
            return LocalDate.now();
        } else if (fieldType == Date.class) {
            return new Date();
        } else if (fieldType == Timestamp.class) {
            return new Timestamp(System.currentTimeMillis());
        } else if (fieldType == Long.class || fieldType == long.class) {
            return System.currentTimeMillis();
        }
        return null;
    }

    /**
     * Convert user value to target type.
     *
     * @param user       the user object
     * @param targetType the target field type
     * @return the converted value
     */
    private Object convertUserValue(Object user, Class<?> targetType) {
        if (user == null) {
            return null;
        }

        // If types match, return directly
        if (targetType.isAssignableFrom(user.getClass())) {
            return user;
        }

        // Convert to String
        if (targetType == String.class) {
            return user.toString();
        }

        // Convert to Long
        if (targetType == Long.class || targetType == long.class) {
            if (user instanceof Number) {
                return ((Number) user).longValue();
            }
            try {
                return Long.parseLong(user.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Convert to Integer
        if (targetType == Integer.class || targetType == int.class) {
            if (user instanceof Number) {
                return ((Number) user).intValue();
            }
            try {
                return Integer.parseInt(user.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return user;
    }

    /**
     * Get or create entity metadata.
     *
     * @param entityClass the entity class
     * @return the entity metadata
     */
    private EntityMetadata getMetadata(Class<?> entityClass) {
        return metadataCache.computeIfAbsent(entityClass, this::parseMetadata);
    }

    /**
     * Parse entity metadata from class.
     *
     * @param entityClass the entity class
     * @return the entity metadata
     */
    private EntityMetadata parseMetadata(Class<?> entityClass) {
        EntityMetadata metadata = new EntityMetadata();

        // Parse all fields (including inherited)
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Created.class)) {
                    metadata.createTimeField = field;
                }
                if (field.isAnnotationPresent(Modified.class)) {
                    metadata.updateTimeField = field;
                }
                if (field.isAnnotationPresent(Creator.class)) {
                    metadata.createdByField = field;
                }
                if (field.isAnnotationPresent(Modifier.class)) {
                    metadata.updatedByField = field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Return null if no data fill fields found
        if (metadata.createTimeField == null && metadata.updateTimeField == null && metadata.createdByField == null
                && metadata.updatedByField == null) {
            return null;
        }

        return metadata;
    }

    /**
     * Clear metadata cache.
     */
    public void clear() {
        metadataCache.clear();
    }

    /**
     * Entity metadata holder.
     */
    private static class EntityMetadata {

        Field createTimeField;
        Field updateTimeField;
        Field createdByField;
        Field updatedByField;

    }

}
