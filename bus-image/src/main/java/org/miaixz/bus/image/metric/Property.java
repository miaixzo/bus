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
package org.miaixz.bus.image.metric;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * Provides DICOM processing details.
 * <p>
 * Gets the related value.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Property implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852261138619L;

    /**
     * Provides DICOM processing details.
     */
    private final String name;

    /**
     * Provides DICOM processing details.
     */
    private final Object value;

    /**
     * Creates a new instance.
     *
     * @param name  the name.
     * @param value the value.
     * @throws NullPointerException     if the operation cannot be completed.
     * @throws IllegalArgumentException if the operation cannot be completed.
     */
    public Property(String name, Object value) {
        if (name == null)
            throw new NullPointerException("name");
        if (value == null)
            throw new NullPointerException("value");
        if (!(value instanceof String || value instanceof Boolean || value instanceof Number))
            throw new IllegalArgumentException("value: " + value.getClass());
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a new instance.
     *
     * @param s the s.
     */
    public Property(String s) {
        int endParamName = s.indexOf('=');
        name = s.substring(0, endParamName);
        value = valueOf(s.substring(endParamName + 1));
    }

    /**
     * Provides DICOM processing details.
     *
     * @param s the s.
     * @return the result.
     */
    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : s.equalsIgnoreCase("false") ? Boolean.FALSE : s;
        }
    }

    /**
     * Provides DICOM processing details.
     *
     * @param ss the ss.
     * @return the result.
     */
    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    /**
     * Gets the related value.
     *
     * @param <T>    the t type.
     * @param props  the props.
     * @param name   the name.
     * @param defVal the def val.
     * @return true if the condition is met; otherwise false.
     */
    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props)
            if (prop.name.equals(name))
                return (T) prop.value;
        return defVal;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final Object getValue() {
        return value;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    /**
     * Determines whether the condition is met.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        Property other = (Property) object;
        return name.equals(other.name) && value.equals(other.value);
    }

    /**
     * Returns the related value.
     *
     * @return the result.
     */
    @Override
    public String toString() {
        return name + '=' + value;
    }

    /**
     * Sets the related value.
     *
     * @param o the o.
     * @throws IllegalArgumentException if the operation cannot be completed.
     */
    public void setAt(Object o) {
        String setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
        try {
            Class<?> clazz = o.getClass();
            if (value instanceof String) {
                clazz.getMethod(setterName, String.class).invoke(o, value);
            } else if (value instanceof Boolean) {
                clazz.getMethod(setterName, boolean.class).invoke(o, value);
            } else { // value instanceof Number
                try {
                    clazz.getMethod(setterName, double.class).invoke(o, ((Number) value).doubleValue());
                } catch (NoSuchMethodException e) {
                    try {
                        clazz.getMethod(setterName, float.class).invoke(o, ((Number) value).floatValue());
                    } catch (NoSuchMethodException e2) {
                        try {
                            clazz.getMethod(setterName, int.class).invoke(o, ((Number) value).intValue());
                        } catch (NoSuchMethodException e3) {
                            throw e;
                        }
                    }
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
