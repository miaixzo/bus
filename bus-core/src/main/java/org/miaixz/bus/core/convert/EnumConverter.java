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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Enumers;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.*;

/**
 * Converter for enums without generic type checking
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnumConverter extends AbstractConverter implements MatcherConverter {

    /**
     * Constructs a new EnumConverter. Utility class constructor for static access.
     */
    public EnumConverter() {
    }

    @Serial
    private static final long serialVersionUID = 2852268818368L;

    /**
     * Singleton instance
     */
    public static final EnumConverter INSTANCE = new EnumConverter();
    private static final WeakConcurrentMap<Class<?>, Map<Class<?>, Method>> VALUE_OF_METHOD_CACHE = new WeakConcurrentMap<>();

    /**
     * Attempts conversion with the following rules:
     * <ul>
     * <li>If implementing {@link Enumers} interface, call fromInt or fromStr for conversion</li>
     * <li>Find and use similar conversion static methods with priority</li>
     * <li>Enum classes should provide valueOf(String) and valueOf(Integer) for conversion</li>
     * <li>ordinal / name conversion as fallback</li>
     * </ul>
     *
     * @param value     the value to convert
     * @param enumClass the enum class
     * @return the corresponding enum value
     */
    protected static Enum tryConvertEnum(final Object value, final Class enumClass) {
        if (value == null) {
            return null;
        }

        // EnumItem implementation conversion
        if (Enumers.class.isAssignableFrom(enumClass)) {
            final Enumers first = (Enumers) EnumKit.getEnumAt(enumClass, 0);
            if (null != first) {
                if (value instanceof Integer) {
                    return (Enum) first.from((Integer) value);
                } else if (value instanceof String) {
                    return (Enum) first.from(value.toString());
                }
            }
        }

        // User-defined methods
        // Find all methods in the enum that return the target enum object. If a method with matching parameters is
        // found, execute it
        try {
            final Map<Class<?>, Method> methodMap = getMethodMap(enumClass);
            if (MapKit.isNotEmpty(methodMap)) {
                final Class<?> valueClass = value.getClass();
                for (final Map.Entry<Class<?>, Method> entry : methodMap.entrySet()) {
                    if (ClassKit.isAssignable(entry.getKey(), valueClass)) {
                        return MethodKit.invokeStatic(entry.getValue(), value);
                    }
                }
            }
        } catch (final Exception ignore) {
            // ignore
        }

        // ordinal should be used as a fallback. Taking GB/T 2261.1-2003 gender code as an example,
        // the corresponding integer is not a continuous number, which will cause digital to enum conversion failure
        // 0 - Unknown gender
        // 1 - Male
        // 2 - Female
        // 5 - Female changed to male
        // 6 - Male changed to female
        // 9 - Unspecified gender
        Enum enumResult = null;
        if (value instanceof Integer) {
            enumResult = EnumKit.getEnumAt(enumClass, (Integer) value);
        } else if (value instanceof String) {
            try {
                enumResult = Enum.valueOf(enumClass, (String) value);
            } catch (final IllegalArgumentException e) {
                // ignore
            }
        }

        return enumResult;
    }

    /**
     * Gets all static methods for converting to enum
     *
     * @param enumClass the enum class
     * @return map of conversion methods, key is method parameter type, value is the method
     */
    private static Map<Class<?>, Method> getMethodMap(final Class<?> enumClass) {
        return VALUE_OF_METHOD_CACHE.computeIfAbsent(
                enumClass,
                (key) -> Arrays.stream(enumClass.getMethods()).filter(ModifierKit::isStatic)
                        .filter(m -> m.getReturnType() == enumClass).filter(m -> m.getParameterCount() == 1)
                        .filter(m -> !"valueOf".equals(m.getName()))
                        .collect(Collectors.toMap(m -> m.getParameterTypes()[0], m -> m, (k1, k2) -> k1)));
    }

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isEnum();
    }

    @Override
    protected Object convertInternal(final Class<?> targetClass, final Object value) {
        Enum enumValue = tryConvertEnum(value, targetClass);
        if (null == enumValue && !(value instanceof String)) {
            // Finally try to convert value to String first, then valueOf conversion
            enumValue = Enum.valueOf((Class) targetClass, convertToString(value));
        }

        if (null != enumValue) {
            return enumValue;
        }

        throw new ConvertException("Can not support {} to {}", value, targetClass);
    }

}
