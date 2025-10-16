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
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MethodKit;

/**
 * A collector for repeatable annotations, used to extract contained repeatable annotations from a given annotation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface RepeatableAnnotationCollector {

    /**
     * Returns a no-op implementation of {@code RepeatableAnnotationCollector}. This collector will not extract any
     * repeatable annotations, returning only the original annotation.
     *
     * @return A {@code RepeatableAnnotationCollector} instance that performs no collection.
     */
    static RepeatableAnnotationCollector none() {
        return None.INSTANCE;
    }

    /**
     * Returns a standard implementation of {@code RepeatableAnnotationCollector}. This collector extracts repeatable
     * annotations when an annotation has exactly one attribute named {@code value}, whose type is an array of
     * annotations, and the component type of that array is itself annotated with {@link Repeatable}.
     * <p>
     * Example:
     * 
     * <pre><code>
     * // Container annotation
     * {@literal @}interface Annotations {
     * 	Item[] value() default {};
     * }
     * // Repeatable annotation
     * {@literal @}Repeatable(Annotations.class)
     * {@literal @}interface Item {}
     * </code></pre>
     * 
     * Parsing any {@code Annotations} object will yield the {@code Item} annotation objects contained within its
     * {@code value} attribute.
     *
     * @return A {@code RepeatableAnnotationCollector} instance for standard repeatable annotation patterns.
     * @see Standard
     */
    static RepeatableAnnotationCollector standard() {
        return Standard.INSTANCE;
    }

    /**
     * Returns a {@code RepeatableAnnotationCollector} that uses a custom predicate to determine if an annotation
     * attribute contains repeatable annotations. The collector will return all repeatable annotations found in matching
     * attributes.
     *
     * @param predicate A {@link BiPredicate} that tests whether an annotation and its method attribute contain
     *                  repeatable annotations.
     * @return A {@code RepeatableAnnotationCollector} instance based on a custom condition.
     */
    static RepeatableAnnotationCollector condition(final BiPredicate<Annotation, Method> predicate) {
        return new Condition(predicate);
    }

    /**
     * Returns a comprehensive implementation of {@code RepeatableAnnotationCollector}. This collector considers any
     * annotation attribute whose type is an array of annotations, and whose component type is itself annotated with
     * {@link Repeatable}, as containing repeatable annotations. The collector will return all repeatable annotations
     * from all such matching attributes.
     * <p>
     * Example:
     * 
     * <pre><code>
     * {@literal @}interface MyAnnotations {
     * 	Item1[] items1() default {};
     * 	Item2[] items2() default {};
     * }
     * </code></pre>
     * 
     * Parsing any {@code MyAnnotations} object will yield {@code Item1} annotation objects from the {@code items1}
     * attribute, and {@code Item2} annotation objects from the {@code items2} attribute.
     *
     * @return A {@code RepeatableAnnotationCollector} instance for full repeatable annotation collection.
     */
    static RepeatableAnnotationCollector full() {
        return Full.INSTANCE;
    }

    /**
     * Clears all singleton caches used by the repeatable annotation collectors. This includes caches within
     * {@link Standard} and {@link Full} implementations.
     */
    static void clearSingletonCaches() {
        Standard.INSTANCE.repeatableMethodCache.clear();
        Full.INSTANCE.repeatableMethodCache.clear();
    }

    /**
     * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
     * annotation objects via its attributes. If the contained annotation objects are also container annotations, the
     * process continues recursively until all non-container annotations are obtained.
     * <p>
     * Example: If there is a nested relationship {@code a -> b -> c}, parsing annotation {@code a} will yield all
     * {@code c} annotations. If the annotation does not contain repeatable annotations, it returns the annotation
     * itself.
     *
     * @param annotation The container annotation.
     * @return A list of the final (non-container) repeatable annotations contained within the given annotation. If
     *         {@code annotation} is not a container, the list will contain only {@code annotation} itself.
     */
    List<Annotation> getFinalRepeatableAnnotations(final Annotation annotation);

    /**
     * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
     * annotation objects via its attributes. If the contained annotation objects are also container annotations, the
     * process continues recursively until all non-container annotations are obtained. If {@code accumulate} is
     * {@code true}, the result will include all annotations in the hierarchy.
     * <p>
     * Example: If there is a nested relationship {@code a -> b -> c}, parsing annotation {@code a} will yield all
     * {@code a}, {@code b}, and {@code c} annotations. If the annotation does not contain repeatable annotations, it
     * returns the annotation itself.
     *
     * @param annotation The container annotation.
     * @return A list of all repeatable annotations (including containers) contained within the given annotation. If
     *         {@code annotation} is not a container, the list will contain only {@code annotation} itself.
     */
    List<Annotation> getAllRepeatableAnnotations(final Annotation annotation);

    /**
     * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
     * annotation objects of a specific type via its attributes. The search is recursive through nested container
     * annotations.
     * <p>
     * Example: If there is a nested relationship {@code a -> b -> c}:
     * <ul>
     * <li>Parsing annotation {@code a} for type {@code T} can yield {@code a}, {@code b}, and {@code c} if they match
     * type {@code T}.</li>
     * <li>Parsing annotation {@code b} for type {@code T} can yield {@code b} and {@code c} if they match type
     * {@code T}.</li>
     * <li>Parsing annotation {@code c} for type {@code T} can yield only {@code c} if it matches type {@code T}.</li>
     * </ul>
     *
     * @param annotation     The container annotation.
     * @param annotationType The type of the repeatable annotation to retrieve.
     * @param <T>            The type of the annotation.
     * @return A list of repeatable annotations of the specified type contained within the given annotation.
     */
    <T extends Annotation> List<T> getRepeatableAnnotations(final Annotation annotation, final Class<T> annotationType);

    /**
     * A no-op implementation of {@code RepeatableAnnotationCollector} that returns only the original annotation.
     */
    class None implements RepeatableAnnotationCollector {

        /**
         * The singleton instance of {@code None}.
         */
        private static final None INSTANCE = new None();

        /**
         * Returns a singleton list containing the original annotation, or an empty list if the annotation is
         * {@code null}.
         *
         * @param annotation The annotation.
         * @return A list containing the annotation, or an empty list.
         */
        @Override
        public List<Annotation> getFinalRepeatableAnnotations(final Annotation annotation) {
            return Objects.isNull(annotation) ? Collections.emptyList() : Collections.singletonList(annotation);
        }

        /**
         * Returns a singleton list containing the original annotation, or an empty list if the annotation is
         * {@code null}.
         *
         * @param annotation The annotation.
         * @return A list containing the annotation, or an empty list.
         */
        @Override
        public List<Annotation> getAllRepeatableAnnotations(final Annotation annotation) {
            return Objects.isNull(annotation) ? Collections.emptyList() : Collections.singletonList(annotation);
        }

        /**
         * Returns a singleton list containing the original annotation if its type matches {@code annotationType}, or an
         * empty list otherwise. Returns an empty list if the annotation is {@code null}.
         *
         * @param annotation     The annotation.
         * @param annotationType The type of the annotation to retrieve.
         * @param <T>            The type of the annotation.
         * @return A list containing the annotation of the specified type, or an empty list.
         */
        @Override
        public <T extends Annotation> List<T> getRepeatableAnnotations(
                final Annotation annotation,
                final Class<T> annotationType) {
            if (Objects.isNull(annotation)) {
                return Collections.emptyList();
            }
            return Objects.equals(annotation.annotationType(), annotationType)
                    ? Collections.singletonList(annotationType.cast(annotation))
                    : Collections.emptyList();
        }

    }

    /**
     * An abstract base class for {@code RepeatableAnnotationCollector} implementations. Provides common logic for
     * traversing and filtering annotations.
     */
    abstract class AbstractCollector implements RepeatableAnnotationCollector {

        /**
         * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
         * annotation objects via its attributes. If the contained annotation objects are also container annotations,
         * the process continues recursively until all non-container annotations are obtained.
         *
         * @param annotation The container annotation.
         * @return A list of the final (non-container) repeatable annotations contained within the given annotation. If
         *         {@code annotation} is not a container, the list will contain only {@code annotation} itself.
         */
        @Override
        public final List<Annotation> getFinalRepeatableAnnotations(final Annotation annotation) {
            return find(annotation, null, false);
        }

        /**
         * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
         * annotation objects via its attributes. If the contained annotation objects are also container annotations,
         * the process continues recursively until all non-container annotations are obtained. If {@code accumulate} is
         * {@code true}, the result will include all annotations in the hierarchy.
         * <p>
         * Example: If there is a nested relationship {@code a -> b -> c}, parsing annotation {@code a} will yield all
         * {@code a}, {@code b}, and {@code c} annotations. If the annotation does not contain repeatable annotations,
         * it returns the annotation itself.
         *
         * @param annotation The container annotation.
         * @return A list of all repeatable annotations (including containers) contained within the given annotation. If
         *         {@code annotation} is not a container, the list will contain only {@code annotation} itself.
         */
        @Override
        public List<Annotation> getAllRepeatableAnnotations(final Annotation annotation) {
            return find(annotation, null, true);
        }

        /**
         * If an annotation is a container for repeatable annotations, this method attempts to retrieve the contained
         * annotation objects of a specific type via its attributes. The search is recursive through nested container
         * annotations.
         *
         * @param annotation     The container annotation.
         * @param annotationType The type of the repeatable annotation to retrieve.
         * @param <T>            The type of the annotation.
         * @return A list of repeatable annotations of the specified type contained within the given annotation.
         */
        @Override
        public <T extends Annotation> List<T> getRepeatableAnnotations(
                final Annotation annotation,
                final Class<T> annotationType) {
            final List<Annotation> annotations = find(
                    annotation,
                    t -> Objects.equals(t.annotationType(), annotationType),
                    false);
            return annotations.stream().map(annotationType::cast).collect(Collectors.toList());
        }

        /**
         * Recursively finds and collects repeatable annotations from a given annotation.
         *
         * @param annotation The starting annotation to search from.
         * @param condition  An optional predicate to filter annotations. If {@code null}, no filtering is applied.
         * @param accumulate If {@code true}, all annotations encountered in the hierarchy are collected; otherwise,
         *                   only the final (non-container) annotations that satisfy the condition are collected.
         * @return A list of collected annotations.
         */
        private List<Annotation> find(
                final Annotation annotation,
                final java.util.function.Predicate<Annotation> condition,
                final boolean accumulate) {
            if (Objects.isNull(annotation)) {
                return Collections.emptyList();
            }
            final boolean hasCondition = Objects.nonNull(condition);
            final List<Annotation> results = new ArrayList<>();
            final Deque<Annotation> deque = new LinkedList<>();
            deque.add(annotation);
            while (!deque.isEmpty()) {
                final Annotation source = deque.removeFirst();
                final List<Method> repeatableMethods = resolveRepeatableMethod(source);
                // If accumulating, record every annotation encountered.
                if (accumulate) {
                    results.add(source);
                }
                final boolean isTarget = hasCondition && condition.test(source);
                if (CollKit.isEmpty(repeatableMethods) || isTarget) {
                    // If not accumulating, record only if the current annotation is not a repeatable container
                    // or if it is a target annotation (matches the condition).
                    final boolean shouldProcess = !accumulate && (!hasCondition || isTarget);
                    if (shouldProcess) {
                        results.add(source);
                    }
                    continue;
                }
                final Annotation[] repeatableAnnotation = repeatableMethods.stream()
                        .map(method -> getRepeatableAnnotationsFormAttribute(source, method))
                        .filter(ArrayKit::isNotEmpty).flatMap(Stream::of).toArray(Annotation[]::new);
                if (ArrayKit.isNotEmpty(repeatableAnnotation)) {
                    CollKit.addAll(deque, repeatableAnnotation);
                }
            }
            return results;
        }

        /**
         * Invokes the specified method on the annotation object to retrieve nested repeatable annotations.
         *
         * @param annotation The annotation object.
         * @param method     The method that returns an array of repeatable annotations.
         * @return An array of repeatable annotations.
         * @throws ClassCastException if the result of invoking {@code method} cannot be cast to {@link Annotation[]}.
         */
        protected Annotation[] getRepeatableAnnotationsFormAttribute(final Annotation annotation, final Method method) {
            return MethodKit.invoke(annotation, method);
        }

        /**
         * Resolves and returns a list of methods within the given annotation that are identified as containing
         * repeatable annotations.
         *
         * @param annotation The annotation to inspect.
         * @return A list of {@link Method} objects that return repeatable annotations.
         */
        protected abstract List<Method> resolveRepeatableMethod(final Annotation annotation);

    }

    /**
     * A standard implementation of {@code RepeatableAnnotationCollector}. This collector identifies a method as
     * containing repeatable annotations if it is named {@code value}, returns an array of annotations, and the
     * component type of that array is itself annotated with {@link Repeatable}.
     */
    class Standard extends AbstractCollector {

        /**
         * The standard name for the attribute that holds repeatable annotations.
         */
        private static final String VALUE = "value";

        /**
         * The singleton instance of {@code Standard}.
         */
        private static final Standard INSTANCE = new Standard();

        /**
         * A sentinel object used in the cache to indicate that no repeatable method was found for an annotation type.
         */
        private static final Object NONE = new Object();

        /**
         * A cache for methods that return repeatable annotations, keyed by annotation type.
         */
        private final Map<Class<? extends Annotation>, Object> repeatableMethodCache = new WeakConcurrentMap<>();

        /**
         * Constructs a new {@code Standard} collector.
         */
        Standard() {
        }

        /**
         * Resolves and returns a list of methods within the given annotation that are identified as containing
         * repeatable annotations. This implementation uses a cache to store the result of the resolution.
         *
         * @param annotation The annotation to inspect.
         * @return A list of {@link Method} objects that return repeatable annotations.
         */
        @Override
        protected List<Method> resolveRepeatableMethod(final Annotation annotation) {
            final Object cache = repeatableMethodCache
                    .computeIfAbsent(annotation.annotationType(), this::resolveRepeatableMethodFromType);
            return (cache == NONE) ? null : Collections.singletonList((Method) cache);
        }

        /**
         * Resolves the method that returns repeatable annotations from the given annotation type. This method checks
         * for a single attribute named "value" that returns an array of {@link Repeatable} annotations.
         *
         * @param annotationType The type of the annotation to inspect.
         * @return The {@link Method} if found, or {@link #NONE} if no such method exists.
         */
        private Object resolveRepeatableMethodFromType(final Class<? extends Annotation> annotationType) {
            final Method[] attributes = AnnoKit.getAnnotationAttributes(annotationType);
            if (attributes.length != 1) {
                return NONE;
            }
            return isRepeatableMethod(attributes[0]) ? attributes[0] : NONE;
        }

        /**
         * Checks if a method is a repeatable container annotation's {@code value} method.
         *
         * @param attribute The method (attribute) of the annotation.
         * @return {@code true} if the attribute is a repeatable method, {@code false} otherwise.
         */
        protected boolean isRepeatableMethod(final Method attribute) {
            // The attribute name must be "value".
            if (!CharsBacker.equals(VALUE, attribute.getName())) {
                return false;
            }
            final Class<?> attributeType = attribute.getReturnType();
            // The return type must be an array.
            return attributeType.isArray()
                    // And the array elements must be annotations.
                    && attributeType.getComponentType().isAnnotation()
                    // The component annotation class must be annotated with @Repeatable.
                    && attributeType.getComponentType().isAnnotationPresent(Repeatable.class);
        }

    }

    /**
     * An implementation of {@code RepeatableAnnotationCollector} that uses a custom predicate. When resolving
     * annotation attributes, it determines whether an attribute contains repeatable annotations based on the provided
     * predicate. The collector will return all repeatable annotations from all matching attributes.
     */
    class Condition extends AbstractCollector {

        /**
         * The predicate used to determine if a method contains repeatable annotations.
         */
        private final BiPredicate<Annotation, Method> predicate;

        /**
         * Constructs a new {@code Condition} collector with the given predicate.
         *
         * @param predicate The predicate to use for identifying repeatable annotation methods. Must not be
         *                  {@code null}.
         */
        Condition(final BiPredicate<Annotation, Method> predicate) {
            this.predicate = Objects.requireNonNull(predicate);
        }

        /**
         * Resolves and returns a list of methods within the given annotation that are identified as containing
         * repeatable annotations based on the custom predicate.
         *
         * @param annotation The annotation to inspect.
         * @return A list of {@link Method} objects that return repeatable annotations.
         */
        @Override
        protected List<Method> resolveRepeatableMethod(final Annotation annotation) {
            return Stream.of(AnnoKit.getAnnotationAttributes(annotation.annotationType()))
                    .filter(method -> predicate.test(annotation, method)).collect(Collectors.toList());
        }

    }

    /**
     * A comprehensive implementation of {@code RepeatableAnnotationCollector}. This collector identifies any annotation
     * attribute as containing repeatable annotations if its type is an array of annotations, and the component type of
     * that array is itself annotated with {@link Repeatable}. The collector will return all repeatable annotations from
     * all such matching attributes.
     */
    class Full extends AbstractCollector {

        /**
         * The singleton instance of {@code Full}.
         */
        private static final Full INSTANCE = new Full();

        /**
         * A sentinel object used in the cache to indicate that no repeatable method was found for an annotation type.
         */
        private static final Object NONE = new Object();

        /**
         * A cache for methods that return repeatable annotations, keyed by annotation type.
         */
        private final Map<Class<? extends Annotation>, Object> repeatableMethodCache = new WeakConcurrentMap<>();

        /**
         * Constructs a new {@code Full} collector.
         */
        Full() {
        }

        /**
         * Resolves and returns a list of methods within the given annotation that are identified as containing
         * repeatable annotations. This implementation uses a cache to store the result of the resolution.
         *
         * @param annotation The annotation to inspect.
         * @return A list of {@link Method} objects that return repeatable annotations.
         */
        @Override
        protected List<Method> resolveRepeatableMethod(final Annotation annotation) {
            final Object cache = repeatableMethodCache
                    .computeIfAbsent(annotation.annotationType(), this::resolveRepeatableMethodFromType);
            return (cache == NONE) ? null : (List<Method>) cache;
        }

        /**
         * Resolves all methods that return repeatable annotations from the given annotation type. This method checks
         * for any attribute that returns an array of {@link Repeatable} annotations.
         *
         * @param annotationType The type of the annotation to inspect.
         * @return A list of {@link Method}s if found, or {@link #NONE} if no such methods exist.
         */
        private Object resolveRepeatableMethodFromType(final Class<? extends Annotation> annotationType) {
            final List<Method> methods = Stream.of(AnnoKit.getAnnotationAttributes(annotationType))
                    .filter(this::isRepeatableMethod).collect(Collectors.toList());
            return methods.isEmpty() ? NONE : methods;
        }

        /**
         * Checks if a method is a repeatable container annotation's attribute method. A method is considered repeatable
         * if its return type is an array of annotations, and the component type of that array is itself annotated with
         * {@link Repeatable}.
         *
         * @param attribute The method (attribute) of the annotation.
         * @return {@code true} if the attribute is a repeatable method, {@code false} otherwise.
         */
        protected boolean isRepeatableMethod(final Method attribute) {
            final Class<?> attributeType = attribute.getReturnType();
            // The return type must be an array.
            return attributeType.isArray()
                    // And the array elements must be annotations.
                    && attributeType.getComponentType().isAnnotation()
                    // The component annotation class must be annotated with @Repeatable.
                    && attributeType.getComponentType().isAnnotationPresent(Repeatable.class);
        }

    }

}
