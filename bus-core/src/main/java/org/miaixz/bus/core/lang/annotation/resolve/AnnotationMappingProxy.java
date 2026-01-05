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
package org.miaixz.bus.core.lang.annotation.resolve;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * A dynamic proxy invocation handler for {@link AnnotationMapping} that generates a proxy object. When attribute values
 * are retrieved from this proxy object, they are always obtained through
 * {@link AnnotationMapping#getResolvedAttributeValue(String, Class)}.
 *
 * @param <T> The type of the annotation being proxied.
 * @author Kimi Liu
 * @see AnnotationMapping
 * @since Java 17+
 */
public final class AnnotationMappingProxy<T extends Annotation> implements InvocationHandler {

    /**
     * The underlying annotation mapping that provides the actual attribute values.
     */
    private final AnnotationMapping<T> mapping;

    /**
     * A map of method names to functions that handle their invocation, including special methods like {@code equals},
     * {@code hashCode}, and {@code toString}.
     */
    private final Map<String, BiFunction<Method, Object[], Object>> methods;

    /**
     * A cache for resolved attribute values to avoid redundant computations.
     */
    private final Map<String, Object> valueCache;

    /**
     * Constructs a new {@code AnnotationMappingProxy} with the given annotation mapping.
     *
     * @param annotation The annotation mapping to be proxied.
     */
    private AnnotationMappingProxy(final AnnotationMapping<T> annotation) {
        final int methodCount = annotation.getAttributes().length;
        this.methods = new HashMap<>(methodCount + 5);
        this.valueCache = new ConcurrentHashMap<>(methodCount);
        this.mapping = annotation;
        loadMethods();
    }

    /**
     * Creates a new proxy instance for the given annotation type and mapping.
     *
     * @param annotationType The class of the annotation to proxy.
     * @param mapping        The {@link AnnotationMapping} object that provides the annotation's attributes.
     * @param <A>            The type of the annotation.
     * @return A new proxy instance of the specified annotation type.
     * @throws NullPointerException if {@code annotationType} or {@code mapping} is {@code null}.
     */
    public static <A extends Annotation> A of(
            final Class<? extends A> annotationType,
            final AnnotationMapping<A> mapping) {
        Objects.requireNonNull(annotationType);
        Objects.requireNonNull(mapping);
        final AnnotationMappingProxy<A> invocationHandler = new AnnotationMappingProxy<>(mapping);
        return (A) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class[] { annotationType, Proxied.class },
                invocationHandler);
    }

    /**
     * Checks if the given annotation object is a proxy generated by this class.
     *
     * @param annotation The annotation object to check.
     * @return {@code true} if the annotation is a proxy, {@code false} otherwise.
     */
    public static boolean isProxied(final Annotation annotation) {
        return annotation instanceof Proxied;
    }

    /**
     * Invokes a method on the proxy instance. This method intercepts calls to annotation attributes and special methods
     * (e.g., {@code equals}, {@code hashCode}, {@code toString}).
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation on the
     *               proxy instance, or {@code null} if the interface method takes no arguments.
     * @return The value to return from the method invocation on the proxy instance.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        return Optional.ofNullable(methods.get(method.getName())).map(m -> m.apply(method, args))
                .orElseGet(() -> MethodKit.invoke(this, method, args));
    }

    /**
     * Pre-loads the methods that need to be proxied, including standard {@code Object} methods and annotation
     * attributes.
     */
    private void loadMethods() {
        methods.put(Normal.EQUALS, (method, args) -> proxyEquals(args[0]));
        methods.put(Normal.TOSTRING, (method, args) -> proxyToString());
        methods.put(Normal.HASHCODE, (method, args) -> proxyHashCode());
        methods.put("annotationType", (method, args) -> proxyAnnotationType());
        methods.put("getMapping", (method, args) -> proxyGetMapping());
        for (final Method attribute : mapping.getAttributes()) {
            methods.put(
                    attribute.getName(),
                    (method, args) -> getAttributeValue(method.getName(), method.getReturnType()));
        }
    }

    /**
     * Proxies the {@link Annotation#toString()} method to provide a custom string representation.
     *
     * @return A string representation of the proxied annotation.
     */
    private String proxyToString() {
        final String attributes = Stream.of(mapping.getAttributes())
                .map(
                        attribute -> CharsBacker.format(
                                "{}={}",
                                attribute.getName(),
                                getAttributeValue(attribute.getName(), attribute.getReturnType())))
                .collect(Collectors.joining(", "));
        return CharsBacker.format("@{}({})", mapping.annotationType().getName(), attributes);
    }

    /**
     * Proxies the {@link Annotation#hashCode()} method to provide a custom hash code.
     *
     * @return The hash code of the proxied annotation.
     */
    private int proxyHashCode() {
        return this.hashCode();
    }

    /**
     * Proxies the {@link Annotation#equals(Object)} method to provide custom equality comparison.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    private boolean proxyEquals(final Object o) {
        return Objects.equals(mapping, o);
    }

    /**
     * Proxies the {@link Annotation#annotationType()} method.
     *
     * @return The annotation type of the proxied annotation.
     */
    private Class<? extends Annotation> proxyAnnotationType() {
        return mapping.annotationType();
    }

    /**
     * Proxies the {@link Proxied#getMapping()} method to return the underlying {@link AnnotationMapping}.
     *
     * @return The underlying annotation mapping.
     */
    private AnnotationMapping<T> proxyGetMapping() {
        return mapping;
    }

    /**
     * Retrieves the resolved attribute value for the given attribute name and type, utilizing a cache.
     *
     * @param attributeName The name of the attribute.
     * @param attributeType The expected type of the attribute's value.
     * @return The resolved attribute value.
     */
    private Object getAttributeValue(final String attributeName, final Class<?> attributeType) {
        return valueCache.computeIfAbsent(
                attributeName,
                name -> mapping.getResolvedAttributeValue(attributeName, attributeType));
    }

    /**
     * A marker interface implemented by annotation proxies generated by {@link AnnotationMappingProxy}. This interface
     * allows for checking if an annotation is a synthetic proxy and provides access to its underlying mapping.
     */
    public interface Proxied {

        /**
         * Retrieves the underlying {@link AnnotationMapping} object that this proxy wraps.
         *
         * @return The annotation mapping object.
         */
        AnnotationMapping<Annotation> getMapping();

    }

}
