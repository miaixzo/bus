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
package org.miaixz.bus.core.lang.annotation.resolve.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.*;

/**
 * Scans meta-annotations on annotation types, with support for enum instances or enum types.
 * <p>
 * Note: when the element being scanned is an enum class, there may be conflicts with {@link TypeAnnotationScanner}.
 *
 * @author Kimi Liu
 * @since Java 21+
 * @see TypeAnnotationScanner
 */
public class MetaAnnotationScanner implements AnnotationScanner {

    /**
     * Whether to continue recursively scanning meta-annotations of meta-annotations after obtaining the current
     * annotation's meta-annotations.
     */
    private final boolean includeSupperMetaAnnotation;

    /**
     * Constructs a new {@code MetaAnnotationScanner}.
     *
     * @param includeSupperMetaAnnotation Whether to recursively scan meta-annotations of meta-annotations
     */
    public MetaAnnotationScanner(final boolean includeSupperMetaAnnotation) {
        this.includeSupperMetaAnnotation = includeSupperMetaAnnotation;
    }

    /**
     * Constructs a new {@code MetaAnnotationScanner} that recursively scans meta-annotations by default.
     */
    public MetaAnnotationScanner() {
        this(true);
    }

    /**
     * Returns {@code true} only when the element is a {@link Class} that is a subtype of {@link Annotation}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} if the element is an annotation {@link Class}
     */
    @Override
    public boolean support(final AnnotatedElement annotatedEle) {
        return (annotatedEle instanceof Class && ClassKit.isAssignable(Annotation.class, (Class<?>) annotatedEle));
    }

    /**
     * Returns all meta-annotations on the annotation element. Requires {@link #support(AnnotatedElement)} to return
     * {@code true}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return The list of meta-annotations
     */
    @Override
    public List<Annotation> getAnnotations(final AnnotatedElement annotatedEle) {
        final List<Annotation> annotations = new ArrayList<>();
        scan(
                (index, annotation) -> annotations.add(annotation),
                annotatedEle,
                annotation -> ObjectKit.notEquals(annotation, annotatedEle));
        return annotations;
    }

    /**
     * Scans meta-annotations on the specified annotation using breadth-first traversal and processes each annotation
     * with its hierarchy index.
     *
     * @param consumer     The consumer for each (index, annotation) pair
     * @param annotatedEle The annotation element to scan
     * @param filter       The annotation filter
     */
    @Override
    public void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, PredicateKit.alwaysTrue());
        final Set<Class<? extends Annotation>> accessed = new HashSet<>();
        final Deque<List<Class<? extends Annotation>>> deque = ListKit
                .ofLinked(ListKit.of((Class<? extends Annotation>) annotatedEle));
        int distance = 0;
        do {
            final List<Class<? extends Annotation>> annotationTypes = deque.removeFirst();
            for (final Class<? extends Annotation> type : annotationTypes) {
                final List<Annotation> metaAnnotations = Stream.of(type.getAnnotations())
                        .filter(a -> !AnnoKit.isMetaAnnotation(a.annotationType())).filter(filter).toList();
                for (final Annotation metaAnnotation : metaAnnotations) {
                    consumer.accept(distance, metaAnnotation);
                }
                accessed.add(type);
                final List<Class<? extends Annotation>> next = metaAnnotations.stream().map(Annotation::annotationType)
                        .filter(t -> !accessed.contains(t)).collect(Collectors.toList());
                if (CollKit.isNotEmpty(next)) {
                    deque.addLast(next);
                }
            }
            distance++;
        } while (includeSupperMetaAnnotation && !deque.isEmpty());
    }

}
