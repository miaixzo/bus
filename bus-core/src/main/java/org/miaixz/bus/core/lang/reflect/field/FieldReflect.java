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
package org.miaixz.bus.core.lang.reflect.field;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Field reflection class. This class holds a cache of fields within a class. If fields are modified in the class, the
 * {@link #clearCaches()} method must be manually called to clear the cache.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldReflect {

    /**
     * The class for which fields are being reflected.
     */
    private final Class<?> clazz;
    /**
     * Cached array of declared fields for the current class (excluding inherited fields). This cache is volatile to
     * ensure visibility across threads.
     */
    private volatile Field[] declaredFields;
    /**
     * Cached array of all fields (declared and inherited) for the current class. This cache is volatile to ensure
     * visibility across threads.
     */
    private volatile Field[] allFields;

    /**
     * Constructs a new {@code FieldReflect} instance for the given class.
     *
     * @param clazz The class to reflect. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public FieldReflect(final Class<?> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * Creates a new {@code FieldReflect} instance for the given class.
     *
     * @param clazz The class to reflect.
     * @return A new {@code FieldReflect} instance.
     */
    public static FieldReflect of(final Class<?> clazz) {
        return new FieldReflect(clazz);
    }

    /**
     * Retrieves the class associated with this {@code FieldReflect} instance.
     *
     * @return The reflected class.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Clears all cached field arrays. This method should be called if the class structure (fields) changes dynamically.
     */
    synchronized public void clearCaches() {
        declaredFields = null;
        allFields = null;
    }

    /**
     * Retrieves all declared fields of the current class (excluding inherited fields), filtered by the given predicate.
     * The result is cached after the first call.
     *
     * @param predicate The filter to apply to the fields. Can be {@code null} to accept all fields.
     * @return An array of declared fields that satisfy the predicate.
     * @throws SecurityException If a security manager exists and its {@code checkMemberAccess} method denies access.
     */
    public Field[] getDeclaredFields(final Predicate<Field> predicate) {
        if (null == declaredFields) {
            synchronized (FieldReflect.class) {
                if (null == declaredFields) {
                    declaredFields = clazz.getDeclaredFields();
                }
            }
        }
        return ArrayKit.filter(declaredFields, predicate);
    }

    /**
     * Retrieves all fields (declared and inherited) of the current class, filtered by the given predicate. The result
     * is cached after the first call.
     *
     * @param predicate The filter to apply to the fields. Can be {@code null} to accept all fields.
     * @return An array of all fields that satisfy the predicate.
     * @throws SecurityException If a security manager exists and its {@code checkMemberAccess} method denies access.
     */
    public Field[] getAllFields(final Predicate<Field> predicate) {
        if (null == allFields) {
            synchronized (FieldReflect.class) {
                if (null == allFields) {
                    allFields = getFieldsDirectly(true);
                }
            }
        }
        return ArrayKit.filter(allFields, predicate);
    }

    /**
     * Retrieves a list of all fields in a class, directly using reflection without caching. If a subclass and
     * superclass have fields with the same name, both fields will be present, with the subclass's field appearing
     * before the superclass's field.
     *
     * @param withSuperClassFields Whether to include fields from superclasses.
     * @return An array of fields.
     * @throws SecurityException If a security manager exists and its {@code checkMemberAccess} method denies access.
     */
    public Field[] getFieldsDirectly(final boolean withSuperClassFields) throws SecurityException {
        Field[] allFields = null;
        Class<?> searchType = this.clazz;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                allFields = ArrayKit.append(allFields, declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields;
    }

}
