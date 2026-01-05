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
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.annotation.resolve.AnnotationMapping;
import org.miaixz.bus.core.lang.annotation.resolve.RepeatableAnnotationCollector;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * <p>
 * An enhanced {@link AnnotatedElement} that supports repeatable annotations. Its functionality is similar to
 * {@link MetaAnnotatedElement}, but with the following key differences:
 * <ul>
 * <li>It restricts that within a tree structure extending from the same root annotation (rather than on the
 * {@link AnnotatedElement} itself), only one annotation of each type can be retained. This means that if multiple root
 * annotations have the same meta-annotation, all these meta-annotations will be scanned.</li>
 * <li>It supports scanning for repeatable annotations on the {@link AnnotatedElement}. If the
 * {@link RepeatableAnnotationCollector} specified for the current instance supports extracting repeatable annotations
 * from directly declared annotations on the {@link AnnotatedElement}, these will be automatically expanded until they
 * are no longer container annotations. For example: if annotation <em>X</em> exists on element A, and <em>X</em> is a
 * container annotation containing repeatable annotation <em>Y</em>, after parsing, both annotation <em>X</em> and
 * repeatable annotation <em>Y</em> are obtained. Similarly, if there is a nested relationship like <em>X</em>,
 * <em>Y</em>, <em>X</em>, all three will be obtained after parsing.</li>
 * </ul>
 * Due to the above mechanisms, when obtaining an annotation of a specific type via {@link #getAnnotation(Class)} or
 * {@link #getDeclaredAnnotation(Class)} methods, if multiple annotations of that type exist, only the one scanned first
 * will be returned.
 *
 * @param <T> The type of {@link AnnotationMapping}.
 * @author Kimi Liu
 * @see RepeatableAnnotationCollector
 * @since Java 17+
 */
public class RepeatableMetaAnnotatedElement<T extends AnnotationMapping<Annotation>>
        implements AnnotatedElement, Iterable<T> {

    /**
     * The wrapped {@link AnnotatedElement} object.
     */
    private final AnnotatedElement element;

    /**
     * The factory function for creating {@link AnnotationMapping} instances.
     */
    private final BiFunction<T, Annotation, T> mappingFactory;

    /**
     * A list of {@link Aggregation} objects, each representing a root annotation and its meta-annotations. This list is
     * unmodifiable.
     */
    private final List<Aggregation> aggregations;

    /**
     * The {@link RepeatableAnnotationCollector} used to extract repeatable annotations.
     */
    private final RepeatableAnnotationCollector repeatableCollector;

    /**
     * Constructs a new {@code RepeatableMetaAnnotatedElement} that supports repeatable annotations.
     *
     * @param repeatableCollector The {@link RepeatableAnnotationCollector} to use for extracting repeatable
     *                            annotations.
     * @param element             The {@link AnnotatedElement} object to wrap. Must not be {@code null}.
     * @param mappingFactory      The factory function for creating {@link AnnotationMapping} instances. Must not be
     *                            {@code null}.
     */
    RepeatableMetaAnnotatedElement(final RepeatableAnnotationCollector repeatableCollector,
            final AnnotatedElement element, final BiFunction<T, Annotation, T> mappingFactory) {
        this.element = Objects.requireNonNull(element);
        this.mappingFactory = Objects.requireNonNull(mappingFactory);
        this.repeatableCollector = repeatableCollector;
        this.aggregations = Collections.unmodifiableList(initAggregations(element));
    }

    /**
     * Creates a new {@code RepeatableMetaAnnotatedElement} instance for the given element. This method uses the
     * standard {@link RepeatableAnnotationCollector}.
     *
     * @param element        The {@link AnnotatedElement} to wrap.
     * @param mappingFactory The factory function for creating {@link AnnotationMapping} instances. If it returns
     *                       {@code null}, the annotation will be ignored.
     * @param <A>            The type of {@link AnnotationMapping}.
     * @return A {@code RepeatableMetaAnnotatedElement} instance representing the annotation structure on the
     *         {@link AnnotatedElement}.
     */
    public static <A extends AnnotationMapping<Annotation>> RepeatableMetaAnnotatedElement<A> of(
            final AnnotatedElement element,
            final BiFunction<A, Annotation, A> mappingFactory) {
        return of(RepeatableAnnotationCollector.standard(), element, mappingFactory);
    }

    /**
     * Creates a new {@code RepeatableMetaAnnotatedElement} instance for the given element and a custom collector.
     *
     * @param collector      The {@link RepeatableAnnotationCollector} to use.
     * @param element        The {@link AnnotatedElement} to wrap.
     * @param mappingFactory The factory function for creating {@link AnnotationMapping} instances. If it returns
     *                       {@code null}, the annotation will be ignored.
     * @param <A>            The type of {@link AnnotationMapping}.
     * @return A {@code RepeatableMetaAnnotatedElement} instance representing the annotation structure on the
     *         {@link AnnotatedElement}.
     */
    public static <A extends AnnotationMapping<Annotation>> RepeatableMetaAnnotatedElement<A> of(
            final RepeatableAnnotationCollector collector,
            final AnnotatedElement element,
            final BiFunction<A, Annotation, A> mappingFactory) {
        return new RepeatableMetaAnnotatedElement<>(collector, element, mappingFactory);
    }

    /**
     * Checks if the specified annotation type is present on any directly declared annotation, any repeatable annotation
     * contained within them, or any of their meta-annotations on the {@link #element}.
     *
     * @param annotationType The type of the annotation to check for.
     * @return {@code true} if the annotation is present, {@code false} otherwise.
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        return aggregations.stream().anyMatch(aggregation -> aggregation.getMappings().containsKey(annotationType));
    }

    /**
     * Retrieves an annotation of the specified type from the directly declared annotations on the {@link #element},
     * including repeatable annotations contained within them, and their meta-annotations. If multiple annotations of
     * the same type are found, the first one encountered is returned.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The annotation instance, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return aggregations.stream().map(Aggregation::getMappings).map(annotations -> annotations.get(annotationType))
                .filter(Objects::nonNull).findFirst().map(T::getResolvedAnnotation).map(annotationType::cast)
                .orElse(null);
    }

    /**
     * Retrieves all annotations from the directly declared annotations on the {@link #element}, including repeatable
     * annotations contained within them, and their meta-annotations.
     *
     * @return An array of all found annotation instances.
     */
    @Override
    public Annotation[] getAnnotations() {
        return aggregations.stream().map(aggregation -> aggregation.getMappings().values()).flatMap(Collection::stream)
                .map(T::getResolvedAnnotation).toArray(Annotation[]::new);
    }

    /**
     * Retrieves all annotations of the specified type from the directly declared annotations on the {@link #element},
     * including repeatable annotations contained within them, and their meta-annotations.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array of annotation instances, or an empty array if none are found.
     */
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        return aggregations.stream().map(aggregation -> aggregation.getMappings().get(annotationType))
                .filter(Objects::nonNull).map(T::getResolvedAnnotation).map(annotationType::cast)
                .toArray(size -> ArrayKit.newArray(annotationType, size));
    }

    /**
     * Retrieves all annotations directly declared on the {@link #element}, excluding repeatable annotations contained
     * within directly declared container annotations.
     *
     * @return An array of directly declared annotation instances.
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return aggregations.stream().filter(Aggregation::isDirect).map(Aggregation::getRoot)
                .map(T::getResolvedAnnotation).toArray(Annotation[]::new);
    }

    /**
     * Retrieves a directly declared annotation of the specified type from the {@link #element}, excluding repeatable
     * annotations contained within directly declared container annotations. If multiple annotations of the same type
     * are found, the first one encountered is returned.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return The directly declared annotation instance, or {@code null} if not found.
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(final Class<A> annotationType) {
        return aggregations.stream().filter(Aggregation::isDirect).map(Aggregation::getRoot)
                .filter(annotation -> Objects.equals(annotationType, annotation.annotationType())).findFirst()
                .map(T::getResolvedAnnotation).map(annotationType::cast).orElse(null);
    }

    /**
     * Retrieves all directly declared annotations of the specified type from the {@link #element}, excluding repeatable
     * annotations contained within directly declared container annotations.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <A>            The type of the annotation.
     * @return An array of directly declared annotation instances, or an empty array if none are found.
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(final Class<A> annotationType) {
        return aggregations.stream().filter(Aggregation::isDirect).map(Aggregation::getRoot)
                .filter(annotation -> Objects.equals(annotationType, annotation.annotationType()))
                .map(T::getResolvedAnnotation).map(annotationType::cast)
                .toArray(size -> ArrayKit.newArray(annotationType, size));
    }

    /**
     * Retrieves the original {@link AnnotatedElement} object that was wrapped.
     *
     * @return The wrapped original element.
     */
    public AnnotatedElement getElement() {
        return element;
    }

    /**
     * Compares this {@code RepeatableMetaAnnotatedElement} to the specified object. The result is {@code true} if and
     * only if the argument is not {@code null} and is a {@code RepeatableMetaAnnotatedElement} object that wraps the
     * same {@link AnnotatedElement}, uses the same {@link BiFunction} for mapping, and the same
     * {@link RepeatableAnnotationCollector}.
     *
     * @param o The object to compare this {@code RepeatableMetaAnnotatedElement} against.
     * @return {@code true} if the given object represents a {@code RepeatableMetaAnnotatedElement} equivalent to this
     *         {@code RepeatableMetaAnnotatedElement}, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RepeatableMetaAnnotatedElement<?> that = (RepeatableMetaAnnotatedElement<?>) o;
        return element.equals(that.element) && mappingFactory.equals(that.mappingFactory)
                && repeatableCollector.equals(that.repeatableCollector);
    }

    /**
     * Returns a hash code for this {@code RepeatableMetaAnnotatedElement}.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(element, mappingFactory, repeatableCollector);
    }

    /**
     * Returns an iterator over the {@link AnnotationMapping} objects contained within this element. The iteration order
     * is based on the order of aggregations and then the order of mappings within each aggregation.
     *
     * @return An iterator over {@link AnnotationMapping} objects.
     */
    @Override
    public Iterator<T> iterator() {
        return aggregations.stream().map(Aggregation::getMappings).map(Map::values).flatMap(Collection::stream)
                .iterator();
    }

    /**
     * Initializes the list of {@link Aggregation} objects by collecting repeatable annotations from the wrapped
     * element.
     *
     * @param element The {@link AnnotatedElement} to initialize from.
     * @return A list of {@link Aggregation} objects.
     */
    private List<Aggregation> initAggregations(final AnnotatedElement element) {
        final List<Aggregation> result = new ArrayList<>();
        for (final Annotation declaredAnnotation : AnnoKit.getDeclaredAnnotations(element)) {
            final List<Aggregation> repeatableAnnotations = collectRepeatable(declaredAnnotation);
            if (CollKit.isNotEmpty(repeatableAnnotations)) {
                result.addAll(repeatableAnnotations);
            }
        }
        return result;
    }

    /**
     * Collects repeatable annotations from a given annotation. If the annotation is a container for repeatable
     * annotations, it is flattened, and the contained repeatable annotations are added to the aggregations.
     *
     * @param annotation The annotation to collect repeatable annotations from.
     * @return A list of {@link Aggregation} objects representing the collected repeatable annotations.
     */
    private List<Aggregation> collectRepeatable(final Annotation annotation) {
        return repeatableCollector.getAllRepeatableAnnotations(annotation).stream()
                .map(a -> new Aggregation(a, Objects.equals(a, annotation))).collect(Collectors.toList());
    }

    /**
     * Represents an aggregation of a root annotation and its meta-annotations. This inner class is used to manage the
     * hierarchy of annotations stemming from a single root annotation.
     */
    class Aggregation {

        /**
         * The root annotation of this aggregation.
         */
        private final T root;
        /**
         * Indicates whether this root annotation was directly declared on the {@link #element}.
         */
        private final boolean isDirect;
        /**
         * A lazy-loaded map of annotation types to their {@link AnnotationMapping} instances within this aggregation.
         * This map is {@code null} by default and initialized upon first access.
         */
        private volatile Map<Class<? extends Annotation>, T> mappings;

        /**
         * Constructs a new {@code Aggregation} with a root annotation and its direct declaration status.
         *
         * @param root     The root annotation for this aggregation.
         * @param isDirect {@code true} if the root annotation is directly declared on the {@link #element},
         *                 {@code false} otherwise.
         */
        public Aggregation(final Annotation root, final boolean isDirect) {
            this.root = mappingFactory.apply(null, root);
            this.isDirect = isDirect;
        }

        /**
         * Retrieves the map of annotation types to their {@link AnnotationMapping} instances within this aggregation,
         * initializing it if necessary.
         *
         * @return An unmodifiable map of annotation types to {@link AnnotationMapping} instances.
         */
        private Map<Class<? extends Annotation>, T> getMappings() {
            if (Objects.isNull(mappings)) {
                synchronized (this) {
                    if (Objects.isNull(mappings)) {
                        mappings = Collections.unmodifiableMap(initMetaAnnotations());
                    }
                }
            }
            return mappings;
        }

        /**
         * Initializes the map of meta-annotations for this aggregation. It performs a breadth-first search starting
         * from the root annotation to collect all its meta-annotations.
         *
         * @return A map of annotation types to their {@link AnnotationMapping} instances.
         */
        private Map<Class<? extends Annotation>, T> initMetaAnnotations() {
            final Map<Class<? extends Annotation>, T> collectedMappings = new LinkedHashMap<>();
            final Deque<T> deque = new LinkedList<>();
            deque.add(root);
            while (!deque.isEmpty()) {
                final T source = deque.removeFirst();
                if (!isNeedMapping(collectedMappings, source)) {
                    continue;
                }
                collectedMappings.put(source.annotationType(), source);
                for (final Annotation annotation : AnnoKit.getDeclaredAnnotations(source.annotationType())) {
                    if (collectedMappings.containsKey(annotation.annotationType())) {
                        continue;
                    }
                    final T mapping = mappingFactory.apply(source, annotation);
                    if (Objects.nonNull(mapping) && isNeedMapping(collectedMappings, mapping)) {
                        deque.addLast(mapping);
                    }
                }
            }
            return collectedMappings;
        }

        /**
         * Determines whether a given annotation needs to be mapped within this aggregation. By default, annotations
         * that have already been processed or are in the {@code java.lang} package are not processed.
         *
         * @param mappings   The map of currently processed annotations within this aggregation.
         * @param annotation The annotation object to check.
         * @return {@code true} if the annotation needs mapping, {@code false} otherwise.
         */
        private boolean isNeedMapping(final Map<Class<? extends Annotation>, T> mappings, final Annotation annotation) {
            return !CharsBacker.startWith(annotation.annotationType().getName(), "java.lang.")
                    && !mappings.containsKey(annotation.annotationType());
        }

        /**
         * Checks if the root annotation of this aggregation was directly declared on the {@link #element}.
         *
         * @return {@code true} if the root annotation is directly declared, {@code false} otherwise.
         */
        public boolean isDirect() {
            return isDirect;
        }

        /**
         * Retrieves the root annotation mapping of this aggregation.
         *
         * @return The root annotation mapping.
         */
        public T getRoot() {
            return root;
        }
    }

}
