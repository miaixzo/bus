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
package org.miaixz.bus.base.entity;

import java.io.Serial;
import java.util.List;

import org.miaixz.bus.core.basic.entity.Tracer;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.core.lang.annotation.Logical;
import org.miaixz.bus.validate.magic.annotation.NotBlank;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class, containing common fields such as data status, creator, creation time, modifier, modification time,
 * nonce, search parameters, and pagination information. Provides methods for setting access information and operator
 * property values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity extends Tracer {

    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 2852287773629L;

    /**
     * Data status: -1 for deleted, 0 for invalid, 1 for normal.
     */
    @Logical
    @NotBlank
    protected Integer status;

    /**
     * The creator of the entity.
     */
    protected String creator;

    /**
     * The creation timestamp of the entity.
     */
    protected Long created;

    /**
     * The modifier of the entity.
     */
    protected String modifier;

    /**
     * The modification timestamp of the entity.
     */
    protected Long modified;

    /**
     * A random number, used to prevent duplicate submissions or as a unique identifier.
     */
    @Transient
    protected String nonce;

    /**
     * Search parameters, used to store query conditions.
     */
    @Transient
    protected String params;

    /**
     * Page number for pagination.
     */
    @NotBlank
    @Transient
    protected Integer pageNo;

    /**
     * Page size for pagination.
     */
    @NotBlank
    @Transient
    protected Integer pageSize;

    /**
     * Sorting order, can be "asc" (ascending) or "desc" (descending).
     */
    @Transient
    protected String orderBy;

    /**
     * Resets numeric string fields to null to prevent database insertion errors. If a numeric string property's value
     * is an empty string, it will be set to null.
     *
     * @param <T>    the entity type
     * @param entity the entity object
     * @param fields an array of numeric string property names
     * @param values an array of values corresponding to the fields
     */
    public static <T extends BaseEntity> void resetIntField(T entity, String[] fields, String[] values) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (Consts.EMPTY.equals(values[i]) && FieldKit.hasField(entity.getClass(), field)) {
                MethodKit.invokeSetter(entity, field, null);
            }
        }
    }

    /**
     * Sets access information, transferring tenant ID and user ID from a source entity to a target entity.
     *
     * @param <T>    the target entity type
     * @param source the source entity containing access information
     * @param target the target entity to which access information will be set
     */
    public <T extends BaseEntity> void setAccess(T source, T target) {
        if (ObjectKit.isNull(source) || ObjectKit.isNull(target)) {
            return;
        }
        target.setX_tenant_id(source.getX_tenant_id());
        target.setX_user_id(source.getX_user_id());
    }

    /**
     * Sets access information, transferring tenant ID and user ID from a source entity to multiple target entities.
     *
     * @param <T>    the target entity type
     * @param source the source entity containing access information
     * @param target an array of target entities to which access information will be set
     */
    @SafeVarargs
    public final <T extends BaseEntity> void setAccess(T source, T... target) {
        if (ObjectKit.isNull(source) || ArrayKit.isEmpty(target)) {
            return;
        }
        for (T targetEntity : target) {
            this.setAccess(source, targetEntity);
        }
    }

    /**
     * Sets access information, transferring tenant ID and user ID from a source entity to a list of target entities.
     *
     * @param <S>    the source entity type
     * @param <E>    the element type of the target entity list
     * @param source the source entity containing access information
     * @param target the list of target entities to which access information will be set
     */
    public <S extends BaseEntity, E extends BaseEntity> void setAccess(S source, List<E> target) {
        if (ObjectKit.isNull(source) || CollKit.isEmpty(target)) {
            return;
        }
        target.forEach(targetEntity -> this.setAccess(source, targetEntity));
    }

    /**
     * Quickly sets creator property values, including ID, creator, and creation time. If the ID is empty, a new
     * ObjectId is generated. If the creator is empty, the x_user_id is used as the creator.
     *
     * @param <T>    the entity type
     * @param entity the reflective object for which creator properties will be set
     */
    public <T> void setInsert(T entity) {
        String id = ObjectKit.isEmpty(getValue(entity, "id")) ? ID.objectId() : (String) getValue(entity, "id");
        String timestamp = StringKit.toString(DateKit.current());
        String[] fields = { "id", "created" };
        Object[] value = new Object[] { id, timestamp };
        if (ObjectKit.isEmpty(getValue(entity, "creator"))) {
            fields = new String[] { "id", "creator", "created" };
            value = new Object[] { id,
                    ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp };
        }
        this.setValue(entity, fields, value);
    }

    /**
     * Quickly sets updater property values, including modifier and modification time. If the modifier is empty, the
     * x_user_id is used as the modifier.
     *
     * @param <T>    the entity type
     * @param entity the reflective object for which updater properties will be set
     */
    public <T> void setUpdate(T entity) {
        String timestamp = StringKit.toString(DateKit.current());
        String[] fields = { "modified" };
        Object[] value = new Object[] { timestamp };
        if (ObjectKit.isEmpty(getValue(entity, "modifier"))) {
            fields = new String[] { "modifier", "modified" };
            value = new Object[] {
                    ObjectKit.isEmpty(getValue(entity, "x_user_id")) ? "-1" : getValue(entity, "x_user_id"),
                    timestamp };
        }

        this.setValue(entity, fields, value);
    }

    /**
     * Quickly sets operator property values, including both creation and update properties.
     *
     * @param entity the reflective object for which operator properties will be set
     * @param <T>    the entity type
     */
    public <T> void setValue(T entity) {
        this.setInsert(entity);
        this.setUpdate(entity);
    }

}
