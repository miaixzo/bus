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
package org.miaixz.bus.spring.annotation;

import org.miaixz.bus.core.lang.Normal;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Interface for handling placeholder resolution and binding of environment properties to objects.
 * <p>
 * This interface provides methods to bind environment properties to a target class or to resolve placeholders within a
 * string. It supports both Spring Boot 2.x and 1.x binding mechanisms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PlaceHolderBinder {

    /**
     * Binds global resources from the {@link Environment} to a specified target class.
     * <p>
     * This static method attempts to bind properties from the environment to an instance of the {@code targetClass}. It
     * first tries to use the Spring Boot 2.x {@code Binder} mechanism. If that fails (e.g., due to an older Spring Boot
     * version), it falls back to a Spring Boot 1.x compatible binding mechanism using reflection.
     * </p>
     *
     * @param environment The Spring {@link Environment} containing the configuration properties.
     * @param targetClass The {@link Class} of the target object to which properties will be bound.
     * @param prefix      The prefix for the properties to bind (e.g., "bus.cache").
     * @param <T>         The type of the target object.
     * @return An instance of the {@code targetClass} with bound properties, or {@code null} if binding fails.
     * @throws RuntimeException if an unexpected reflection error occurs during binding.
     */
    static <T> T bind(Environment environment, Class<T> targetClass, String prefix) {
        // Attempt to bind using Spring Boot 2.x Binder
        try {
            Class<?> bindClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = bindClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = bindClass.getDeclaredMethod("bind", String.class, Class.class);
            Object bind = getMethod.invoke(null, environment);
            Object bindResult = bindMethod.invoke(bind, prefix, targetClass);
            Method resultGetMethod = bindResult.getClass().getDeclaredMethod("get");
            Method isBoundMethod = bindResult.getClass().getDeclaredMethod("isBound");
            if ((Boolean) isBoundMethod.invoke(bindResult)) {
                return (T) resultGetMethod.invoke(bindResult);
            }
            return null;
        } catch (Exception e) {
            // Fallback to Spring Boot 1.x binding mechanism
            try {
                // Use reflection to extract configuration information via RelaxedPropertyResolver
                Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
                Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
                Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
                Object resolver = resolverConstructor.newInstance(environment);
                Map<String, Object> properties = (Map<String, Object>) getSubPropertiesMethod
                        .invoke(resolver, Normal.EMPTY);
                // Create an instance of the target class
                T target = targetClass.getConstructor().newInstance();
                // Use reflection for org.springframework.boot.bind.RelaxedDataBinder
                Class<?> binderClass = Class.forName("org.springframework.boot.bind.RelaxedDataBinder");
                Constructor<?> binderConstructor = binderClass.getDeclaredConstructor(Object.class, String.class);
                Method bindMethod = binderClass.getMethod("bind", PropertyValues.class);
                // Create binder and bind data
                Object binder = binderConstructor.newInstance(target, prefix);
                bindMethod.invoke(binder, new MutablePropertyValues(properties));
                return target;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to bind properties using Spring Boot 1.x or 2.x binder", ex);
            }
        }
    }

    /**
     * Resolves placeholders in a given string against the provided {@link Environment}.
     *
     * @param environment The Spring {@link Environment} to use for placeholder resolution.
     * @param string      The string containing placeholders (e.g., "${my.property}").
     * @return The string with all placeholders resolved.
     */
    default String bind(Environment environment, String string) {
        return environment.resolvePlaceholders(string);
    }

    /**
     * Resolves placeholders in a given string. This default implementation returns an empty string. Implementations
     * should override this to provide actual placeholder resolution if needed without an explicit Environment.
     *
     * @param string The string containing placeholders.
     * @return The string with placeholders resolved, or an empty string by default.
     */
    default String bind(String string) {
        return Normal.EMPTY;
    }

}
