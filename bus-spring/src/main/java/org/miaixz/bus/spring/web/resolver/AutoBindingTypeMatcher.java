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
package org.miaixz.bus.spring.web.resolver;

import java.security.Principal;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;

import org.miaixz.bus.spring.annotation.RequestObject;

/**
 * Declares the strict type boundary for explicit RequestObject binding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AutoBindingTypeMatcher {

    /**
     * Creates the stateless request binding matcher.
     */
    public AutoBindingTypeMatcher() {
        // No initialization required.
    }

    /**
     * Returns true only for an explicit, application-owned request object parameter.
     *
     * @param parameter controller method parameter
     * @return {@code true} when explicit request-object binding is allowed
     */
    public boolean matches(MethodParameter parameter) {
        Objects.requireNonNull(parameter, "parameter");
        if (!parameter.hasParameterAnnotation(RequestObject.class)
                || parameter.hasParameterAnnotation(RequestBody.class)
                || parameter.hasParameterAnnotation(RequestPart.class)) {
            return false;
        }
        Class<?> type = parameter.getParameterType();
        return !isFrameworkType(type) && !isSimpleType(type);
    }

    /**
     * Returns whether the type belongs to a framework-managed web contract.
     *
     * @param type parameter type
     * @return {@code true} for framework-managed types
     */
    private static boolean isFrameworkType(Class<?> type) {
        return Principal.class.isAssignableFrom(type) || ServletRequest.class.isAssignableFrom(type)
                || ServletResponse.class.isAssignableFrom(type) || Model.class.isAssignableFrom(type)
                || BindingResult.class.isAssignableFrom(type)
                || hasTypeName(type, "org.springframework.data.domain.Pageable")
                || type.getName().startsWith("org.springframework.security.");
    }

    /**
     * Searches a class hierarchy for an optional type without linking that type.
     *
     * @param type candidate type
     * @param name fully qualified optional type name
     * @return {@code true} when the hierarchy contains the named type
     */
    private static boolean hasTypeName(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (name.equals(current.getName())) {
                return true;
            }
            for (Class<?> contract : current.getInterfaces()) {
                if (name.equals(contract.getName()) || hasTypeName(contract, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a type is scalar, temporal, array, collection, or map data.
     *
     * @param type parameter type
     * @return {@code true} for simple data types
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || type.isArray() || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type) || Boolean.class == type || Character.class == type
                || Temporal.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }

}
