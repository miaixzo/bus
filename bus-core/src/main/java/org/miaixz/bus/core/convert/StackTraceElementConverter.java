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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.util.Map;

import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Converter for {@link StackTraceElement} objects, only supports Map conversion
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StackTraceElementConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852271899317L;

    /**
     * Constructs a new stack trace element converter.
     */
    public StackTraceElementConverter() {
    }

    /**
     * Converts the given value to a StackTraceElement.
     * <p>
     * Only supports conversion from Map with the following keys: {@code className}, {@code methodName},
     * {@code fileName}, {@code lineNumber}.
     * </p>
     *
     * @param targetClass the target class (should be StackTraceElement.class)
     * @param value       the value to convert (should be a Map)
     * @return the converted StackTraceElement object, or null if value is not a Map
     */
    @Override
    protected StackTraceElement convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Map<?, ?> map) {
            final String declaringClass = MapKit.getString(map, "className");
            final String methodName = MapKit.getString(map, "methodName");
            final String fileName = MapKit.getString(map, "fileName");
            final Integer lineNumber = MapKit.getInt(map, "lineNumber");

            return new StackTraceElement(declaringClass, methodName, fileName, ObjectKit.defaultIfNull(lineNumber, 0));
        }
        return null;
    }

}
