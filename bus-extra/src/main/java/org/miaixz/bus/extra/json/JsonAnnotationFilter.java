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
package org.miaixz.bus.extra.json;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.core.lang.annotation.Include;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * Default {@link JsonPropertyFilter} decorator that enforces Bus JSON annotations consistently across providers.
 * {@link Include} takes precedence over {@link Ignore}; when annotation rules allow a property, the caller-supplied
 * filter makes the final decision.
 *
 * @author Kimi Liu
 */
public class JsonAnnotationFilter implements JsonPropertyFilter {

    /**
     * Caller-supplied filter applied after mandatory annotation rules.
     */
    private final JsonPropertyFilter delegate;

    /**
     * Creates an annotation-aware decorator around a caller-supplied filter.
     *
     * @param delegate caller-supplied property filter
     */
    public JsonAnnotationFilter(JsonPropertyFilter delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Returns the supplied annotation-aware filter unchanged, or decorates a plain property filter.
     *
     * @param filter property filter
     * @return annotation-aware filter
     */
    public static JsonAnnotationFilter of(JsonPropertyFilter filter) {
        return filter instanceof JsonAnnotationFilter annotationFilter ? annotationFilter
                : new JsonAnnotationFilter(filter);
    }

    /**
     * Determines whether the decorator contains a caller rule in addition to mandatory annotation filtering.
     *
     * @return {@code true} when a custom filter is configured
     */
    public boolean hasDelegateFilter() {
        return delegate != JsonPropertyFilter.ALWAYS;
    }

    /**
     * Applies mandatory annotation rules followed by the caller-supplied filter.
     *
     * @param source owning object, or {@code null} when unavailable
     * @param name   Java property name
     * @param value  current property value
     * @return {@code true} when both annotation and caller rules include the property
     */
    @Override
    public boolean accept(Object source, String name, Object value) {
        return !isIgnored(source, name) && delegate.accept(source, name, value);
    }

    /**
     * Resolves a JavaBean property and evaluates its field and getter annotations.
     *
     * @param source owning object
     * @param name   Java property name
     * @return {@code true} when {@link Ignore} excludes the property and {@link Include} does not override it
     */
    private static boolean isIgnored(Object source, String name) {
        if (source == null || source instanceof Map<?, ?> || name == null) {
            return false;
        }
        PropDesc property;
        try {
            property = BeanKit.getBeanDesc(source.getClass()).getProp(name);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (property == null) {
            return false;
        }
        Field field = property.getField();
        Method getter = null;
        Invoker getterInvoker = property.getGetter();
        if (getterInvoker instanceof MethodInvoker methodInvoker) {
            getter = methodInvoker.getMethod();
        }
        if (hasAnnotation(field, Include.class) || hasAnnotation(getter, Include.class)) {
            return false;
        }
        return hasAnnotation(field, Ignore.class) || hasAnnotation(getter, Ignore.class);
    }

    /**
     * Checks an annotation without requiring a non-null reflective element.
     *
     * @param element        field or getter to inspect
     * @param annotationType annotation type
     * @return {@code true} when the element carries the annotation
     */
    private static boolean hasAnnotation(
            AnnotatedElement element,
            Class<? extends java.lang.annotation.Annotation> annotationType) {
        return element != null && element.isAnnotationPresent(annotationType);
    }

}
