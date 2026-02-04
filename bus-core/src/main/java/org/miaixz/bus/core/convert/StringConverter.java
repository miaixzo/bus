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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import org.miaixz.bus.core.convert.stringer.BlobStringer;
import org.miaixz.bus.core.convert.stringer.ClobStringer;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.XmlKit;

/**
 * String converter, provides encapsulation of various object-to-string conversion logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852271992351L;

    private Map<Class<?>, Function<Object, String>> stringer;

    /**
     * Adds custom toString rules for object types.
     *
     * @param clazz          the type
     * @param stringFunction the serialization function
     * @return this
     */
    public StringConverter putStringer(final Class<?> clazz, final Function<Object, String> stringFunction) {
        if (null == stringer) {
            stringer = new HashMap<>();
        }
        stringer.put(clazz, stringFunction);
        return this;
    }

    /**
     * Convertinternal method.
     *
     * @return the String value
     */
    @Override
    protected String convertInternal(final Class<?> targetClass, final Object value) {
        // Custom toString
        if (MapKit.isNotEmpty(stringer)) {
            final Function<Object, String> stringFunction = stringer.get(targetClass);
            if (null != stringFunction) {
                return stringFunction.apply(value);
            }
        }

        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        } else if (value instanceof org.w3c.dom.Node) {
            return XmlKit.toString((org.w3c.dom.Node) value);
        } else if (value instanceof java.sql.Clob) {
            return ClobStringer.INSTANCE.apply(value);
        } else if (value instanceof java.sql.Blob) {
            return BlobStringer.INSTANCE.apply(value);
        } else if (value instanceof Type) {
            return ((Type) value).getTypeName();
        }

        // Other cases
        return convertToString(value);
    }

}
