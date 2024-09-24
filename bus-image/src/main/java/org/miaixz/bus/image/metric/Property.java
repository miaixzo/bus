/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Property implements Serializable {

    private static final long serialVersionUID = -1L;

    private final String name;
    private final Object value;

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

    public Property(String s) {
        int endParamName = s.indexOf('=');
        name = s.substring(0, endParamName);
        value = valueOf(s.substring(endParamName + 1));
    }

    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : s.equalsIgnoreCase("false") ? Boolean.FALSE : s;
        }
    }

    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props)
            if (prop.name.equals(name))
                return (T) prop.value;
        return defVal;
    }

    public final String getName() {
        return name;
    }

    public final Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

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

    @Override
    public String toString() {
        return name + '=' + value;
    }

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
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
