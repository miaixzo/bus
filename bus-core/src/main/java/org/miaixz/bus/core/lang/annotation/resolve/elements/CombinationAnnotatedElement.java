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

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * An enhanced {@link AnnotatedElement} implementation that supports composite annotations, similar to Spring's
 * mechanism. It recursively retrieves annotations and meta-annotations from the specified element to provide a
 * comprehensive view.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CombinationAnnotatedElement implements AnnotatedElement, Serializable {

    @Serial
    private static final long serialVersionUID = 2852250737317L;

    /**
     * The predicate used to filter annotations. Only annotations for which the predicate returns {@code true} are
     * retained.
     */
    private final Predicate<Annotation> predicate;
    /**
     * A map storing all annotations found on the element and its meta-annotations, keyed by annotation type.
     */
    private Map<Class<? extends Annotation>, Annotation> annotationMap;
    /**
     * A map storing annotations directly declared on the element, keyed by annotation type.
     */
    private Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;

    /**
     * Constructs a new {@code CombinationAnnotatedElement} for the given element with no annotation filtering.
     *
     * @param element The element to parse annotations from. This can be a {@link Class},
     *                {@link java.lang.reflect.Method}, {@link java.lang.reflect.Field},
     *                {@link java.lang.reflect.Constructor}.
     */
    public CombinationAnnotatedElement(final AnnotatedElement element) {
        this(element, null);
    }

    /**
     * Constructs a new {@code CombinationAnnotatedElement} for the given element with a specified annotation filter.
     *
     * @param element   The element to parse annotations from. This can be a {@link Class},
     *                  {@link java.lang.reflect.Method}, {@link java.lang.reflect.Field},
     *                  {@link java.lang.reflect.Constructor}.
     * @param predicate The predicate to filter annotations. Annotations for which {@link Predicate#test(Object)}
     *                  returns {@code true} are retained.
     */
    public CombinationAnnotatedElement(final AnnotatedElement element, final Predicate<Annotation> predicate) {
        this.predicate = predicate;
        init(element);
    }

    /**
     * Creates a new {@code CombinationAnnotatedElement} instance.
     *
     * @param element   The element to parse annotations from. This can be a {@link Class},
     *                  {@link java.lang.reflect.Method}, {@link java.lang.reflect.Field},
     *                  {@link java.lang.reflect.Constructor}.
     * @param predicate The predicate to filter annotations. Annotations for which {@link Predicate#test(Object)}
     *                  returns {@code true} are retained.
     * @return A new {@code CombinationAnnotatedElement} instance.
     */
    public static CombinationAnnotatedElement of(
            final AnnotatedElement element,
            final Predicate<Annotation> predicate) {
        return new CombinationAnnotatedElement(element, predicate);
    }

    /**
     * Checks if an annotation of the specified type is present on this element or any of its meta-annotations.
     *
     * @param annotationClass The type of the annotation to check for.
     * @return {@code true} if an annotation of the specified type is present, {@code false} otherwise.
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return annotationMap.containsKey(annotationClass);
    }

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     *
     * @param annotationClass The Class object corresponding to the annotation type.
     * @param <T>             The type of the annotation.
     * @return This element's annotation for the specified annotation type, or null if no such annotation is present.
     */
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        final Annotation annotation = annotationMap.get(annotationClass);
        return (annotation == null) ? null : (T) annotation;
    }

    /**
     * Returns all annotations present on this element.
     *
     * @return All annotations present on this element.
     */
    @Override
    public Annotation[] getAnnotations() {
        final Collection<Annotation> annotations = this.annotationMap.values();
        return annotations.toArray(new Annotation[0]);
    }

    /**
     * Returns all annotations that are directly present on this element.
     *
     * @return All annotations directly present on this element.
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        final Collection<Annotation> annotations = this.declaredAnnotationMap.values();
        return annotations.toArray(new Annotation[0]);
    }

    /**
     * Initializes the annotation maps by parsing declared and inherited annotations.
     *
     * @param element The annotated element to initialize from.
     */
    private void init(final AnnotatedElement element) {
        final Annotation[] declaredAnnotations = AnnoKit.getDeclaredAnnotations(element);
        this.declaredAnnotationMap = new TableMap<>();
        parseDeclared(declaredAnnotations);

        final Annotation[] annotations = element.getAnnotations();
        // If the number of declared annotations is the same as all annotations, it means there are no inherited
        // annotations
        // or overridden annotations, so the declared map can be reused for the full annotation map.
        if (declaredAnnotations.length == annotations.length) {
            this.annotationMap = this.declaredAnnotationMap;
        } else {
            this.annotationMap = new TableMap<>();
            parse(annotations);
        }
    }

    /**
     * Recursively parses declared annotations and their meta-annotations until all meta-annotations are processed.
     *
     * @param annotations An array of annotations directly declared on an element (Class, Method, Field, etc.).
     */
    private void parseDeclared(final Annotation[] annotations) {
        if (ArrayKit.isEmpty(annotations)) {
            return;
        }
        Class<? extends Annotation> annotationType;
        // Process directly declared annotations
        for (final Annotation annotation : annotations) {
            annotationType = annotation.annotationType();
            // Skip meta-annotations and already processed annotations to prevent infinite recursion.
            if (!AnnoKit.isMetaAnnotation(annotationType) && !declaredAnnotationMap.containsKey(annotationType)) {
                if (test(annotation)) {
                    declaredAnnotationMap.put(annotationType, annotation);
                }
                // Even if the annotation doesn't pass the test, continue to recurse its meta-annotations.
                parseDeclared(AnnoKit.getDeclaredAnnotations(annotationType));
            }
        }
    }

    /**
     * Recursively parses all annotations (including inherited ones) and their meta-annotations.
     *
     * @param annotations An array of annotations present on an element (Class, Method, Field, etc.).
     */
    private void parse(final Annotation[] annotations) {
        Class<? extends Annotation> annotationType;
        for (final Annotation annotation : annotations) {
            annotationType = annotation.annotationType();
            if (!Normal.META_ANNOTATIONS.contains(annotationType)
                    // Skip meta-annotations and already processed annotations to prevent infinite recursion.
                    && !annotationMap.containsKey(annotationType)) {
                if (test(annotation)) {
                    annotationMap.put(annotationType, annotation);
                }
                // Even if the annotation doesn't pass the test, continue to recurse its meta-annotations.
                parse(annotationType.getAnnotations());
            }
        }
    }

    /**
     * Checks if the given annotation satisfies the filter predicate.
     *
     * @param annotation The annotation object to test.
     * @return {@code true} if the annotation passes the filter or if no filter is set, {@code false} otherwise.
     */
    private boolean test(final Annotation annotation) {
        return null == this.predicate || this.predicate.test(annotation);
    }

}
