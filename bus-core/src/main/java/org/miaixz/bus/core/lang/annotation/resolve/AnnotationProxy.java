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
package org.miaixz.bus.core.lang.annotation.resolve;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A dynamic proxy for a given annotation, allowing for custom logic when its methods are invoked. This proxy is
 * particularly useful for supporting meta-annotations like {@link Alias}, which allows annotation attributes to have
 * alternative names.
 *
 * @param <T> The type of the annotation being proxied.
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnotationProxy<T extends Annotation> implements Annotation, InvocationHandler, Serializable {

    @Serial
    private static final long serialVersionUID = 2852250659959L;

    /**
     * The original annotation instance.
     */
    private final T annotation;
    /**
     * The type of the annotation.
     */
    private final Class<T> type;
    /**
     * A cache of the annotation's attribute values, keyed by method name.
     */
    private final Map<String, Object> attributes;

    /**
     * Constructs a new {@code AnnotationProxy} for the given annotation.
     *
     * @param annotation The annotation to be proxied.
     */
    public AnnotationProxy(final T annotation) {
        this.annotation = annotation;
        this.type = (Class<T>) annotation.annotationType();
        this.attributes = initAttributes();
    }

    /**
     * Returns the type of this annotation.
     *
     * @return The annotation type.
     */
    @Override
    public Class<? extends Annotation> annotationType() {
        return type;
    }

    /**
     * Handles method invocations on the annotation proxy. This method intercepts calls to the annotation's attributes,
     * providing support for aliases defined by the {@link Alias} annotation.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation on the
     *               proxy instance, or {@code null} if the interface method takes no arguments.
     * @return The value to return from the method invocation on the proxy instance.
     * @throws Throwable The exception to throw from the method invocation on the proxy instance.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // Handle aliased attributes
        final Alias alias = method.getAnnotation(Alias.class);
        if (null != alias) {
            final String name = alias.value();
            if (StringKit.isNotBlank(name)) {
                if (!attributes.containsKey(name)) {
                    throw new IllegalArgumentException(StringKit.format("No method for alias: [{}]", name));
                }
                return attributes.get(name);
            }
        }

        final Object value = attributes.get(method.getName());
        if (value != null) {
            return value;
        }
        // Fallback to default annotation behavior if attribute not found (e.g., for toString(), hashCode(), etc.)
        return method.invoke(this, args);
    }

    /**
     * Initializes the attribute cache by invoking all declared methods on the original annotation and storing their
     * results.
     *
     * @return A map of attribute names to their corresponding values.
     */
    private Map<String, Object> initAttributes() {
        // Only cache methods declared in the annotation type
        final Method[] methods = MethodKit.getDeclaredMethods(this.type);
        final Map<String, Object> attributes = new HashMap<>(methods.length, 1);

        for (final Method method : methods) {
            // Skip synthetic methods that might be generated by the compiler
            if (method.isSynthetic()) {
                continue;
            }

            attributes.put(method.getName(), MethodKit.invoke(this.annotation, method));
        }
        return attributes;
    }

}
