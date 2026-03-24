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
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PredicateKit;

/**
 * Annotation scanner interface for retrieving annotations from supported annotated elements.
 * <p>
 * The following built-in scanner instances are provided:
 * <ul>
 * <li>{@link #NOTHING}: Does nothing; scans no annotations.</li>
 * <li>{@link #DIRECTLY}: Scans annotations directly declared on the element, including those propagated via
 * {@link Inherited}.</li>
 * <li>{@link #DIRECTLY_AND_META_ANNOTATION}: Same as {@link #DIRECTLY}, plus meta-annotations of those
 * annotations.</li>
 * <li>{@link #SUPERCLASS}: Scans annotations from the element and its superclass hierarchy.</li>
 * <li>{@link #SUPERCLASS_AND_META_ANNOTATION}: Same as {@link #SUPERCLASS}, plus meta-annotations.</li>
 * <li>{@link #INTERFACE}: Scans annotations from the element and its interface hierarchy.</li>
 * <li>{@link #INTERFACE_AND_META_ANNOTATION}: Same as {@link #INTERFACE}, plus meta-annotations.</li>
 * <li>{@link #TYPE_HIERARCHY}: Scans annotations from the element, its superclass, and interface hierarchies.</li>
 * <li>{@link #TYPE_HIERARCHY_AND_META_ANNOTATION}: Same as {@link #TYPE_HIERARCHY}, plus meta-annotations.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 * @see TypeAnnotationScanner
 * @see MethodAnnotationScanner
 * @see FieldAnnotationScanner
 * @see MetaAnnotationScanner
 * @see ElementAnnotationScanner
 * @see CompositeAnnotationScanner
 */
public interface AnnotationScanner {

    /**
     * Scanner that does not scan any annotations.
     */
    AnnotationScanner NOTHING = new NoOpAnnotationScanner();

    /**
     * Scanner that scans annotations directly declared on the element, including those propagated via
     * {@link Inherited}.
     */
    AnnotationScanner DIRECTLY = new CompositeAnnotationScanner(false, false, false);

    /**
     * Scanner that scans directly declared annotations (including {@link Inherited}) and their meta-annotations.
     */
    AnnotationScanner DIRECTLY_AND_META_ANNOTATION = new CompositeAnnotationScanner(true, false, false);

    /**
     * Scanner that scans annotations from the element and its superclass hierarchy.
     */
    AnnotationScanner SUPERCLASS = new CompositeAnnotationScanner(false, true, false);

    /**
     * Scanner that scans annotations from the element and its superclass hierarchy, plus meta-annotations.
     */
    AnnotationScanner SUPERCLASS_AND_META_ANNOTATION = new CompositeAnnotationScanner(true, true, false);

    /**
     * Scanner that scans annotations from the element and its interface hierarchy.
     */
    AnnotationScanner INTERFACE = new CompositeAnnotationScanner(false, false, true);

    /**
     * Scanner that scans annotations from the element and its interface hierarchy, plus meta-annotations.
     */
    AnnotationScanner INTERFACE_AND_META_ANNOTATION = new CompositeAnnotationScanner(true, false, true);

    /**
     * Scanner that scans annotations from the element, its superclass, and interface hierarchies.
     */
    AnnotationScanner TYPE_HIERARCHY = new CompositeAnnotationScanner(false, true, true);

    /**
     * Scanner that scans annotations from the type hierarchy (superclass and interfaces), plus meta-annotations.
     */
    AnnotationScanner TYPE_HIERARCHY_AND_META_ANNOTATION = new CompositeAnnotationScanner(true, true, true);

    /**
     * Using the first scanner from the given group that supports the element type, retrieves annotations on the
     * element.
     *
     * @param annotatedEle The element to scan
     * @param scanners     The annotation scanners
     * @return The annotations found
     */
    static List<Annotation> scanByAnySupported(
            final AnnotatedElement annotatedEle,
            final AnnotationScanner... scanners) {
        if (ObjectKit.isNull(annotatedEle) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners).filter(scanner -> scanner.support(annotatedEle)).findFirst()
                .map(scanner -> scanner.getAnnotations(annotatedEle)).orElseGet(Collections::emptyList);
    }

    /**
     * Scans annotations on the element using all supported scanners from the given group.
     *
     * @param annotatedEle The element to scan
     * @param scanners     The annotation scanners
     * @return The annotations found
     */
    static List<Annotation> scanByAllSupported(
            final AnnotatedElement annotatedEle,
            final AnnotationScanner... scanners) {
        if (ObjectKit.isNull(annotatedEle) && ArrayKit.isNotEmpty(scanners)) {
            return Collections.emptyList();
        }
        return Stream.of(scanners).map(scanner -> scanner.getAnnotationsIfSupport(annotatedEle))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Returns whether this scanner supports the given annotated element.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return {@code true} if supported
     */
    default boolean support(final AnnotatedElement annotatedEle) {
        return false;
    }

    /**
     * Returns all annotations on the element. Requires {@link #support(AnnotatedElement)} to return {@code true}.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return The list of annotations
     */
    default List<Annotation> getAnnotations(final AnnotatedElement annotatedEle) {
        final List<Annotation> annotations = new ArrayList<>();
        scan((index, annotation) -> annotations.add(annotation), annotatedEle, null);
        return annotations;
    }

    /**
     * Returns annotations on the element if supported; otherwise returns an empty list.
     *
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @return The list of annotations, or an empty list if not supported
     */
    default List<Annotation> getAnnotationsIfSupport(final AnnotatedElement annotatedEle) {
        return support(annotatedEle) ? getAnnotations(annotatedEle) : Collections.emptyList();
    }

    /**
     * Scans the hierarchy structure of the annotated element (if any), then processes each annotation along with its
     * corresponding hierarchy index. Must call {@link #support(AnnotatedElement)} and verify it returns {@code true}
     * before calling this method.
     *
     * @param consumer     consumer for each (index, annotation) pair
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @param filter       annotation filter; annotations that fail this filter are skipped. May be {@code null}.
     */
    default void scan(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            Predicate<Annotation> filter) {
        filter = ObjectKit.defaultIfNull(filter, PredicateKit.alwaysTrue());
        for (final Annotation annotation : annotatedEle.getAnnotations()) {
            if (!AnnoKit.isMetaAnnotation(annotation.annotationType()) && filter.test(annotation)) {
                consumer.accept(0, annotation);
            }
        }
    }

    /**
     * Calls {@link #scan(BiConsumer, AnnotatedElement, Predicate)} if {@link #support(AnnotatedElement)} returns
     * {@code true}.
     *
     * @param consumer     consumer for each (index, annotation) pair
     * @param annotatedEle {@link AnnotatedElement}, e.g. Class, Method, Field, Constructor
     * @param filter       annotation filter; annotations that fail this filter are skipped. May be {@code null}.
     */
    default void scanIfSupport(
            final BiConsumer<Integer, Annotation> consumer,
            final AnnotatedElement annotatedEle,
            final Predicate<Annotation> filter) {
        if (support(annotatedEle)) {
            scan(consumer, annotatedEle, filter);
        }
    }

}
