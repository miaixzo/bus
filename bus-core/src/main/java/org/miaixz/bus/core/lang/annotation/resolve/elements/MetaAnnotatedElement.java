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
package org.miaixz.bus.core.lang.annotation.resolve.elements;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.BiFunction;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationMapping;
import org.miaixz.bus.core.lang.annotation.resolve.ResolvedAnnotationMapping;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * <p>
 * An enhanced {@link AnnotatedElement} wrapper that processes annotations directly declared on the wrapped element, as
 * well as their meta-annotations, into {@link ResolvedAnnotationMapping} objects. This enables advanced operations such
 * as accessing meta-annotations and resolving their attributes.
 *
 * <p>
 * By default, annotations within the {@code java.lang} package are not scanned. Additionally, within this instance, the
 * {@link Inherited} annotation does not take effect, meaning that {@code getDeclaredAnnotation} methods will not
 * retrieve {@link Inherited} annotations from superclasses.
 *
 * <p>
 * In a {@link MetaAnnotatedElement}, only one annotation or meta-annotation of the same type from the
 * {@link AnnotatedElement} will be retained. For example, if two root annotations have the same meta-annotation, only
 * the meta-annotation from the first root annotation encountered will be kept. Therefore, methods like
 * {@link #getAnnotationsByType(Class)} or {@link #getDeclaredAnnotationsByType(Class)} will return at most one
 * annotation object.
 *
 * @param <T> The type of {@link AnnotationMapping} used for wrapping annotations.
 * @author Kimi Liu
 * @see ResolvedAnnotationMapping
 * @since Java 17+
 */
public class MetaAnnotatedElement<T extends AnnotationMapping<Annotation>> implements AnnotatedElement, Iterable<T> {

    /**
     * The underlying {@link AnnotatedElement} being wrapped.
     */
    private final AnnotatedElement element;

    /**
     * A factory function for creating {@link AnnotationMapping} instances. If the function returns {@code null}, the
     * annotation will be ignored.
     */
    private final BiFunction<T, Annotation, T> mappingFactory;

    /**
     * A lazy-loaded map of annotation types to their corresponding {@link AnnotationMapping} instances. This map is
     * {@code null} by default and initialized upon the first call to {@link #getAnnotationMappings()}.
     */
    private volatile Map<Class<? extends Annotation>, T> annotationMappings;

    /**
     * Constructs a new {@code MetaAnnotatedElement} with the specified {@link AnnotatedElement} and mapping factory.
     *
     * @param element        The {@link AnnotatedElement} to wrap. Must not be {@code null}.
     * @param mappingFactory The factory function for creating {@link AnnotationMapping} instances. Must not be
     *                       {@code null}.
     */
    public MetaAnnotatedElement(final AnnotatedElement element, final BiFunction<T, Annotation, T> mappingFactory) {
        this.element = Objects.requireNonNull(element);
        this.mappingFactory = Objects.requireNonNull(mappingFactory);
        // Lazy initialization
        this.annotationMappings = null;
    }

    /**
     * Creates a new {@code MetaAnnotatedElement} instance. This method caches the mapping object for the same
     * {@link AnnotatedElement}.
     *
     * @param element        The {@link AnnotatedElement} to wrap.
     * @param mappingFactory The factory function for creating {@link AnnotationMapping} instances. If it returns
     *                       {@code null}, the annotation will be ignored.
     * @param <A>            The type of {@link AnnotationMapping}.
     * @return A {@code MetaAnnotatedElement} instance representing the annotation structure on the
     *         {@link AnnotatedElement}.
     */
    public static <A extends AnnotationMapping<Annotation>> MetaAnnotatedElement<A> of(
            final AnnotatedElement element,
            final BiFunction<A, Annotation, A> mappingFactory) {
        return new MetaAnnotatedElement<>(element, mappingFactory);
    }

    /**
     * Retrieves an {@link AnnotationMapping} object for the specified annotation type from the hierarchical structure
     * of annotations directly declared on the {@link AnnotatedElement}.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @return An {@link Optional} containing the {@link AnnotationMapping} object, or empty if not found.
     */
    public Optional<T> getMapping(final Class<? extends Annotation> annotationType) {
        return Optional.ofNullable(annotationType).map(getAnnotationMappings()::get);
    }

    /**
     * Retrieves the wrapped {@link AnnotatedElement}.
     *
     * @return The wrapped {@link AnnotatedElement}.
     */
    public AnnotatedElement getElement() {
        return element;
    }

    /**
     * Retrieves an {@link AnnotationMapping} object for the specified annotation type from the annotations directly
     * declared on the {@link AnnotatedElement}.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @return An {@link Optional} containing the {@link AnnotationMapping} object, or empty if not found.
     */
    public Optional<T> getDeclaredMapping(final Class<? extends Annotation> annotationType) {
        return EasyStream.of(getAnnotationMappings().values()).filter(T::isRoot)
                .findFirst(mapping -> ObjectKit.equals(annotationType, mapping.annotationType()));
    }

    /**
     * Checks if the specified annotation type is present on the {@link AnnotatedElement} or in its meta-annotation
     * hierarchy.
     *
     * @param annotationType The type of the annotation to check for.
     * @return {@code true} if the annotation is present, {@code false} otherwise.
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        return getMapping(annotationType).isPresent();
    }

    /**
     * Retrieves an annotation of the specified type from the hierarchical structure of annotations directly declared on
     * the {@link AnnotatedElement}.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The annotation instance, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return getMapping(annotationType).map(T::getResolvedAnnotation).map(annotationType::cast).orElse(null);
    }

    /**
     * Retrieves a directly declared annotation of the specified type from the {@link AnnotatedElement}.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The directly declared annotation instance, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(final Class<A> annotationType) {
        return getDeclaredMapping(annotationType).map(T::getResolvedAnnotation).map(annotationType::cast).orElse(null);
    }

    /**
     * Retrieves all annotations of the specified type from the {@link AnnotatedElement} and its meta-annotation
     * hierarchy. Due to the nature of {@code MetaAnnotatedElement} retaining only one instance of each annotation type,
     * this method will return an array with at most one element.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array containing the annotation instance if found, otherwise an empty array.
     */
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        final A result = getAnnotation(annotationType);
        if (Objects.nonNull(result)) {
            return (A[]) new Annotation[] { result };
        }
        return ArrayKit.newArray(annotationType, 0);
    }

    /**
     * Retrieves all directly declared annotations of the specified type from the {@link AnnotatedElement}. Due to the
     * nature of {@code MetaAnnotatedElement} retaining only one instance of each annotation type, this method will
     * return an array with at most one element.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array containing the directly declared annotation instance if found, otherwise an empty array.
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(final Class<A> annotationType) {
        final A result = getDeclaredAnnotation(annotationType);
        if (Objects.nonNull(result)) {
            return (A[]) new Annotation[] { result };
        }
        return ArrayKit.newArray(annotationType, 0);
    }

    /**
     * Retrieves all directly declared annotations on the {@link AnnotatedElement} as resolved annotation objects.
     *
     * @return An array of resolved annotation objects directly declared on the element.
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotationMappings().values().stream().filter(T::isRoot).map(T::getResolvedAnnotation)
                .toArray(Annotation[]::new);
    }

    /**
     * Retrieves all annotations (including meta-annotations) present on the {@link AnnotatedElement} as resolved
     * annotation objects.
     *
     * @return An array of all resolved annotation objects found on the element and its meta-annotation hierarchy.
     */
    @Override
    public Annotation[] getAnnotations() {
        return getAnnotationMappings().values().stream().map(T::getResolvedAnnotation).toArray(Annotation[]::new);
    }

    /**
     * Returns an iterator over the {@link AnnotationMapping} objects representing the annotations on this element.
     *
     * @return An iterator over {@link AnnotationMapping} objects.
     */
    @Override
    public Iterator<T> iterator() {
        return getAnnotationMappings().values().iterator();
    }

    /**
     * Compares this {@code MetaAnnotatedElement} to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is a {@code MetaAnnotatedElement} object that wraps the same
     * {@link AnnotatedElement} and uses the same {@link BiFunction} for mapping.
     *
     * @param o The object to compare this {@code MetaAnnotatedElement} against.
     * @return {@code true} if the given object represents a {@code MetaAnnotatedElement} equivalent to this
     *         {@code MetaAnnotatedElement}, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MetaAnnotatedElement<?> that = (MetaAnnotatedElement<?>) o;
        return element.equals(that.element) && mappingFactory.equals(that.mappingFactory);
    }

    /**
     * Returns a hash code for this {@code MetaAnnotatedElement}.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(element, mappingFactory);
    }

    /**
     * Retrieves the map of annotation types to their {@link AnnotationMapping} instances, initializing it if necessary.
     *
     * @return An unmodifiable map of annotation types to {@link AnnotationMapping} instances.
     */
    protected final Map<Class<? extends Annotation>, T> getAnnotationMappings() {
        initAnnotationMappingsIfNecessary();
        return annotationMappings;
    }

    /**
     * Determines whether a given annotation needs to be mapped. By default, annotations that have already been
     * processed or are in the {@code java.lang} package are not processed.
     *
     * @param mappings   The map of currently processed annotations.
     * @param annotation The annotation object to check.
     * @return {@code true} if the annotation needs mapping, {@code false} otherwise.
     */
    protected boolean isNeedMapping(final Map<Class<? extends Annotation>, T> mappings, final Annotation annotation) {
        return !CharsBacker.startWith(annotation.annotationType().getName(), "java.lang.")
                && !mappings.containsKey(annotation.annotationType());
    }

    /**
     * Creates an {@link AnnotationMapping} instance using the provided factory.
     *
     * @param source     The source {@link AnnotationMapping} (parent annotation in the hierarchy).
     * @param annotation The annotation to map.
     * @return The created {@link AnnotationMapping} instance.
     */
    private T createMapping(final T source, final Annotation annotation) {
        return mappingFactory.apply(source, annotation);
    }

    /**
     * Initializes the {@link #annotationMappings} map if it has not already been initialized. This method uses
     * double-checked locking to ensure thread-safe lazy initialization.
     */
    private void initAnnotationMappingsIfNecessary() {
        if (Objects.isNull(annotationMappings)) {
            synchronized (this) {
                if (Objects.isNull(annotationMappings)) {
                    final Map<Class<? extends Annotation>, T> mappings = new LinkedHashMap<>(8);
                    initAnnotationMappings(mappings);
                    this.annotationMappings = Collections.unmodifiableMap(mappings);
                }
            }
        }
    }

    /**
     * Scans annotations directly declared on the {@link AnnotatedElement} and then performs a breadth-first search
     * through their meta-annotations until all unique annotation types are added to the {@link #annotationMappings}.
     *
     * @param mappings The map to populate with annotation mappings.
     */
    private void initAnnotationMappings(final Map<Class<? extends Annotation>, T> mappings) {
        final Deque<T> deque = new LinkedList<>();
        Arrays.stream(AnnoKit.getDeclaredAnnotations(element)).filter(m -> isNeedMapping(mappings, m))
                .map(annotation -> createMapping(null, annotation)).filter(Objects::nonNull).forEach(deque::addLast);
        while (!deque.isEmpty()) {
            // If an annotation of this type has already been processed, skip it.
            final T mapping = deque.removeFirst();
            if (!isNeedMapping(mappings, mapping)) {
                continue;
            }
            // Save the annotation and add its meta-annotations to the queue for processing.
            mappings.put(mapping.annotationType(), mapping);
            for (final Annotation annotation : AnnoKit.getDeclaredAnnotations(mapping.annotationType())) {
                if (mappings.containsKey(annotation.annotationType())) {
                    continue;
                }
                final T m = createMapping(mapping, annotation);
                if (Objects.nonNull(m) && isNeedMapping(mappings, m)) {
                    deque.addLast(m);
                }
            }
        }
    }

}
